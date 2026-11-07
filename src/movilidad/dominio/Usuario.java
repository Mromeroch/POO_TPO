package movilidad.dominio;

import movilidad.excepciones.OperacionNoPermitidaException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Usuario del servicio. Compone su cuenta (la cuenta no existe sin el usuario, S09)
 * y conoce sus planes y su historial de viajes finalizados.
 */
public class Usuario {
    private final String nombre;
    private final String documento;
    private final String correoElectronico;
    private final LocalDate fechaAlta;
    private boolean habilitado = true;
    private final CuentaUsuario cuenta = new CuentaUsuario();
    private final List<PlanMensual> planes = new ArrayList<>();
    private int viajesFinalizados;

    public Usuario(String nombre, String documento, String correoElectronico, LocalDate fechaAlta) {
        this.nombre = nombre;
        this.documento = documento;
        this.correoElectronico = correoElectronico;
        this.fechaAlta = fechaAlta;
    }

    public boolean estaHabilitado() { return habilitado; }
    public void inhabilitar() { habilitado = false; }
    public void habilitar() { habilitado = true; }

    /** S17: un usuario inhabilitado se rehabilita cuando su saldo vuelve a ser >= 0. */
    public void reevaluarHabilitacion() {
        if (!habilitado && cuenta.getSaldo().signum() >= 0) {
            habilitado = true;
        }
    }

    /** S06: como máximo un plan vigente por usuario. */
    public void suscribir(PlanMensual plan) throws OperacionNoPermitidaException {
        if (getPlanActivo(plan.getFechaInicio()).isPresent()) {
            throw new OperacionNoPermitidaException("El usuario ya tiene un plan mensual vigente");
        }
        planes.add(plan);
    }

    /** Plan vigente por fecha y con viajes disponibles, si existe. */
    public Optional<PlanMensual> getPlanActivo(LocalDateTime fecha) {
        return planes.stream().filter(p -> p.estaActivo(fecha)).findFirst();
    }

    /** Se invoca al finalizar un viaje: suma el viaje al historial y consume el plan (S05, S06). */
    public void registrarViajeFinalizado(LocalDateTime fecha) {
        viajesFinalizados++;
        getPlanActivo(fecha).ifPresent(PlanMensual::consumirViaje);
    }

    public String getNombre() { return nombre; }
    public String getDocumento() { return documento; }
    public String getCorreoElectronico() { return correoElectronico; }
    public LocalDate getFechaAlta() { return fechaAlta; }
    public CuentaUsuario getCuenta() { return cuenta; }
    public int getViajesFinalizados() { return viajesFinalizados; }
    public List<PlanMensual> getPlanes() { return Collections.unmodifiableList(planes); }

    @Override
    public String toString() { return nombre + " (" + documento + ")"; }
}
