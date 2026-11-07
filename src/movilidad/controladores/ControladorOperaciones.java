package movilidad.controladores;

import movilidad.dominio.Estacion;
import movilidad.dominio.Viaje;
import movilidad.dominio.mantenimiento.AlertaEstacion;
import movilidad.dominio.mantenimiento.NotaAjuste;
import movilidad.dominio.mantenimiento.Transferencia;
import movilidad.dominio.personal.Empleado;
import movilidad.dominio.personal.Operador;
import movilidad.dominio.vehiculos.EstadoVehiculo;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.dominio.vehiculos.VehiculoElectrico;
import movilidad.excepciones.*;
import movilidad.persistencia.RepositorioEstaciones;
import movilidad.persistencia.RepositorioMantenimiento;
import movilidad.persistencia.RepositorioPersonal;
import movilidad.persistencia.RepositorioViajes;
import movilidad.servicios.ServicioControlViajes;
import movilidad.util.Reloj;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Casos de uso del operador: redistribución, notas de ajuste, alertas y detección de viajes perdidos (CU-21, CU-22, RF09, RF14). */
public class ControladorOperaciones {
    private final RepositorioEstaciones repoEstaciones;
    private final RepositorioViajes repoViajes;
    private final RepositorioMantenimiento repoMantenimiento;
    private final RepositorioPersonal repoPersonal;
    private final ServicioControlViajes servicioControlViajes;
    private final Reloj reloj;

    public ControladorOperaciones(RepositorioEstaciones repoEstaciones, RepositorioViajes repoViajes,
                                  RepositorioMantenimiento repoMantenimiento, RepositorioPersonal repoPersonal,
                                  ServicioControlViajes servicioControlViajes, Reloj reloj) {
        this.repoEstaciones = repoEstaciones;
        this.repoViajes = repoViajes;
        this.repoMantenimiento = repoMantenimiento;
        this.repoPersonal = repoPersonal;
        this.servicioControlViajes = servicioControlViajes;
        this.reloj = reloj;
    }

    /** CU-21: estaciones con su ocupación (la interfaz marca las que requieren redistribución). */
    public List<Estacion> consultarOcupacion() {
        return repoEstaciones.buscarEstaciones();
    }

    public List<AlertaEstacion> consultarAlertas() {
        return repoEstaciones.buscarAlertas();
    }

    /**
     * CU-21 + CU-22 / S20: mueve vehículos DISPONIBLES o con BATERIA_BAJA hacia una estación con lugar.
     * Al trasladar, la batería se repone y BATERIA_BAJA pasa a DISPONIBLE. Es atómico: si algo no es válido, no se mueve nada.
     */
    public Transferencia redistribuir(String codOrigen, String codDestino, List<String> codVehiculos, String legajoOperador)
            throws MovilidadException {
        Operador operador = buscarOperador(legajoOperador);
        Estacion origen = buscarEstacion(codOrigen);
        Estacion destino = buscarEstacion(codDestino);
        if (origen == destino) {
            throw new OperacionNoPermitidaException("La estación de origen y de destino deben ser distintas");
        }
        if (codVehiculos == null || codVehiculos.isEmpty()) {
            throw new DatosInvalidosException("Debe seleccionar al menos un vehículo");
        }
        List<Vehiculo> aMover = new ArrayList<>();
        for (String cod : codVehiculos) {
            Vehiculo v = origen.buscarVehiculo(cod).orElseThrow(() ->
                    new EntidadNoEncontradaException("El vehículo " + cod + " no está en la estación " + codOrigen));
            if (v.getEstado() != EstadoVehiculo.DISPONIBLE && v.getEstado() != EstadoVehiculo.BATERIA_BAJA) {
                throw new OperacionNoPermitidaException("El vehículo " + cod + " no puede trasladarse (" + v.getEstado() + ")");
            }
            aMover.add(v);
        }
        if (destino.cantidadVehiculos() + aMover.size() > destino.getCapacidadMaxima()) {
            throw new EstacionLlenaException("La estación " + codDestino + " no tiene capacidad para recibir " + aMover.size() + " vehículo(s)");
        }
        for (Vehiculo v : aMover) {
            origen.egresar(v);
            destino.ingresar(v);
            if (v instanceof VehiculoElectrico) {
                ((VehiculoElectrico) v).reponerBateria();
            }
            v.cambiarEstado(EstadoVehiculo.DISPONIBLE);
        }
        Transferencia transferencia = new Transferencia(reloj.ahora(), origen, destino, operador, aMover);
        repoMantenimiento.guardarTransferencia(transferencia);
        origen.evaluarAlerta(reloj.ahora()).ifPresent(repoEstaciones::guardarAlerta);
        destino.evaluarAlerta(reloj.ahora()).ifPresent(repoEstaciones::guardarAlerta);
        return transferencia;
    }

    /**
     * RF14 / RN15 / S19 / S17: el viaje cerrado no se modifica; se genera un movimiento AJUSTE en la cuenta
     * y se reevalúa la habilitación del usuario.
     */
    public NotaAjuste registrarNotaAjuste(long idViaje, BigDecimal importe, String motivo, String legajoOperador)
            throws MovilidadException {
        Operador operador = buscarOperador(legajoOperador);
        Viaje viaje = repoViajes.buscarPorId(idViaje)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el viaje " + idViaje));
        if (importe == null || importe.signum() == 0 || motivo == null || motivo.trim().isEmpty()) {
            throw new DatosInvalidosException("El ajuste requiere un importe distinto de cero y un motivo");
        }
        NotaAjuste nota = new NotaAjuste(viaje, importe, motivo.trim(), operador, reloj.ahora());
        viaje.getUsuario().getCuenta().aplicarAjuste(importe, "Ajuste viaje #" + idViaje + ": " + motivo.trim(), reloj.ahora());
        viaje.getUsuario().reevaluarHabilitacion();
        repoMantenimiento.guardarNotaAjuste(nota);
        return nota;
    }

    /** RF09 / S07: se ejecuta al iniciar el sistema y cada vez que el operador lo solicita. */
    public List<Viaje> detectarPerdidos() {
        return servicioControlViajes.detectarPerdidos();
    }

    private Estacion buscarEstacion(String codigo) throws EntidadNoEncontradaException {
        return repoEstaciones.buscarEstacion(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe la estación " + codigo));
    }

    private Operador buscarOperador(String legajo) throws EntidadNoEncontradaException {
        Empleado e = repoPersonal.buscarEmpleado(legajo).orElse(null);
        if (!(e instanceof Operador)) {
            throw new EntidadNoEncontradaException("No existe el operador con legajo " + legajo);
        }
        return (Operador) e;
    }
}
