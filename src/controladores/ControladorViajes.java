package movilidad.controladores;

import movilidad.dominio.Estacion;
import movilidad.dominio.EstadoViaje;
import movilidad.dominio.PuntoRecorrido;
import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;
import movilidad.dominio.tarifas.PoliticaTarifa;
import movilidad.dominio.tarifas.ResultadoTarifa;
import movilidad.dominio.vehiculos.EstadoVehiculo;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.excepciones.*;
import movilidad.persistencia.RepositorioEstaciones;
import movilidad.persistencia.RepositorioUsuarios;
import movilidad.persistencia.RepositorioViajes;
import movilidad.servicios.EvaluadorPromociones;
import movilidad.servicios.ServicioTarifas;
import movilidad.util.Reloj;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * GRASP Controlador: coordina los casos de uso de retiro, devolución y cancelación.
 * Implementa tal cual los diagramas de secuencia CU-05 (retirar) y CU-08 (devolver).
 * Delega los cálculos en ServicioTarifas / EvaluadorPromociones (SRP) y depende de abstracciones (DIP).
 */
public class ControladorViajes {
    public static final long MINUTOS_CANCELACION_SIN_CARGO = 2;   // RN11

    private final RepositorioViajes repoViajes;
    private final RepositorioUsuarios repoUsuarios;
    private final RepositorioEstaciones repoEstaciones;
    private final ServicioTarifas servicioTarifas;
    private final EvaluadorPromociones evaluadorPromociones;
    private final Reloj reloj;

    public ControladorViajes(RepositorioViajes repoViajes, RepositorioUsuarios repoUsuarios,
                             RepositorioEstaciones repoEstaciones, ServicioTarifas servicioTarifas,
                             EvaluadorPromociones evaluadorPromociones, Reloj reloj) {
        this.repoViajes = repoViajes;
        this.repoUsuarios = repoUsuarios;
        this.repoEstaciones = repoEstaciones;
        this.servicioTarifas = servicioTarifas;
        this.evaluadorPromociones = evaluadorPromociones;
        this.reloj = reloj;
    }

    /** CU-05 / secuencia 4.1. Las validaciones siguen el orden del diagrama y lanzan la excepción de cada rama alt. */
    public Viaje retirarVehiculo(String documento, String codEstacion, String codVehiculo) throws MovilidadException {
        LocalDateTime ahora = reloj.ahora();
        Usuario u = buscarUsuario(documento);
        Estacion e = buscarEstacion(codEstacion);
        Vehiculo v = e.buscarVehiculo(codVehiculo).orElseThrow(() ->
                new VehiculoNoDisponibleException("El vehículo " + codVehiculo + " no está en la estación " + codEstacion));

        PoliticaTarifa tarifa = servicioTarifas.tarifaVigente(v.getTipo(), ahora.toLocalDate());
        BigDecimal minimo = servicioTarifas.saldoMinimo(tarifa);

        if (!u.estaHabilitado()) {
            throw new UsuarioInhabilitadoException("El usuario está inhabilitado hasta regularizar su saldo");
        }
        if (repoViajes.buscarEnCursoDe(u).isPresent()) {
            throw new ViajeAbiertoException("El usuario ya tiene un viaje en curso");
        }
        if (!u.getCuenta().tieneSaldoMinimo(minimo)) {
            throw new SaldoInsuficienteException("Saldo insuficiente: se requieren al menos $" + minimo);
        }
        if (!v.puedeRetirarse()) {
            throw new VehiculoNoDisponibleException("El vehículo " + codVehiculo + " no está disponible (" + v.getEstado() + ")");
        }

        e.egresar(v);
        v.cambiarEstado(EstadoVehiculo.EN_USO);
        Viaje viaje = new Viaje(repoViajes.siguienteId(), u, v, e, tarifa, ahora);   // Creador
        repoViajes.guardar(viaje);
        e.evaluarAlerta(ahora).ifPresent(repoEstaciones::guardarAlerta);             // RN09: alerta al operador
        return viaje;
    }

    /** CU-08 / secuencia 4.2 (sin distancia informada: se usa la de los puntos del recorrido). */
    public ResultadoTarifa devolverVehiculo(long idViaje, String codEstacion) throws MovilidadException {
        return devolverVehiculo(idViaje, codEstacion, 0);
    }

    /** CU-08 con distancia informada al devolver (S13), útil cuando el viaje no tiene puntos de recorrido. */
    public ResultadoTarifa devolverVehiculo(long idViaje, String codEstacion, double distanciaInformadaKm)
            throws MovilidadException {
        LocalDateTime ahora = reloj.ahora();
        Viaje viaje = buscarViajeEnCurso(idViaje);
        Estacion e = buscarEstacion(codEstacion);
        Usuario u = viaje.getUsuario();
        Vehiculo v = viaje.getVehiculo();

        if (e.estaLlena()) {                                  // rama alt: estación llena (RN07, RN08)
            viaje.registrarEstacionLlena(ahora);              // S03: arranca la gracia de 10 minutos
            repoViajes.guardar(viaje);
            throw new EstacionLlenaException("La estación " + codEstacion
                    + " está llena: tiene 10 minutos sin cargo para llegar a otra");
        }

        e.ingresar(v);
        viaje.finalizar(e, ahora, distanciaInformadaKm);
        v.actualizarTrasViaje(viaje.getDistanciaRecorrida());
        ResultadoTarifa resultado = evaluadorPromociones.calcularCostoViaje(viaje, u);
        viaje.setCosto(resultado.getCostoFinal());
        viaje.setPromocionAplicada(resultado.getPromocionAplicada());
        u.registrarViajeFinalizado(ahora);                    // después de evaluar promociones (S05, S06)
        if (resultado.getCostoFinal().signum() > 0) {         // un viaje gratis (promoción) no genera débito
            u.getCuenta().debitar(resultado.getCostoFinal(), "Viaje #" + viaje.getId(), ahora);
        }
        if (u.getCuenta().getSaldo().signum() < 0) {          // RN02
            u.inhabilitar();
        }
        repoViajes.guardar(viaje);
        e.evaluarAlerta(ahora).ifPresent(repoEstaciones::guardarAlerta);
        return resultado;
    }

    /** RF05 / RN11 / S12: cancelación sin cargo dentro de los 2 minutos posteriores al retiro. */
    public void cancelarRetiro(long idViaje) throws MovilidadException {
        LocalDateTime ahora = reloj.ahora();
        Viaje viaje = buscarViajeEnCurso(idViaje);
        if (Duration.between(viaje.getInicio(), ahora).compareTo(Duration.ofMinutes(MINUTOS_CANCELACION_SIN_CARGO)) > 0) {
            throw new RetiroNoCancelableException("Pasaron más de " + MINUTOS_CANCELACION_SIN_CARGO
                    + " minutos: debe devolver el vehículo normalmente");
        }
        Estacion origen = viaje.getEstacionRetiro();
        if (origen.estaLlena()) {
            throw new OperacionNoPermitidaException("La estación de retiro ya no tiene lugar para reponer el vehículo");
        }
        origen.ingresar(viaje.getVehiculo());
        viaje.getVehiculo().cambiarEstado(EstadoVehiculo.DISPONIBLE);
        viaje.cancelar();
        repoViajes.guardar(viaje);
    }

    /** Registra un punto del recorrido mientras el viaje está EN_CURSO (RF06). */
    public void registrarPunto(long idViaje, double latitud, double longitud) throws MovilidadException {
        Viaje viaje = buscarViajeEnCurso(idViaje);
        viaje.agregarPunto(new PuntoRecorrido(latitud, longitud, reloj.ahora()));
        repoViajes.guardar(viaje);
    }

    /** Viaje en curso del usuario (para que la interfaz no pida el id). */
    public Viaje viajeEnCursoDe(String documento) throws MovilidadException {
        Usuario u = buscarUsuario(documento);
        return repoViajes.buscarEnCursoDe(u)
                .orElseThrow(() -> new EntidadNoEncontradaException("El usuario no tiene ningún viaje en curso"));
    }

    private Usuario buscarUsuario(String documento) throws EntidadNoEncontradaException {
        return repoUsuarios.buscarPorDocumento(documento)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el usuario con documento " + documento));
    }

    private Estacion buscarEstacion(String codigo) throws EntidadNoEncontradaException {
        return repoEstaciones.buscarEstacion(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe la estación " + codigo));
    }

    private Viaje buscarViajeEnCurso(long idViaje) throws EntidadNoEncontradaException {
        Viaje viaje = repoViajes.buscarPorId(idViaje)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el viaje " + idViaje));
        if (viaje.getEstado() != EstadoViaje.EN_CURSO) {
            throw new EntidadNoEncontradaException("El viaje " + idViaje + " no está en curso");
        }
        return viaje;
    }
}
