package movilidad.dominio.mantenimiento;

import movilidad.dominio.Estacion;

import java.time.LocalDateTime;

/** Alerta de redistribución: ocupación menor al 20 % (VACIA) o mayor al 90 % (SATURADA). */
public class AlertaEstacion {
    private final Estacion estacion;
    private final TipoAlerta tipo;
    private final LocalDateTime fecha;

    public AlertaEstacion(Estacion estacion, TipoAlerta tipo, LocalDateTime fecha) {
        this.estacion = estacion;
        this.tipo = tipo;
        this.fecha = fecha;
    }

    public Estacion getEstacion() { return estacion; }
    public TipoAlerta getTipo() { return tipo; }
    public LocalDateTime getFecha() { return fecha; }

    @Override
    public String toString() {
        return "Alerta " + tipo + " en " + estacion.getCodigo() + " (" + Math.round(estacion.ocupacion() * 100) + " % de ocupación)";
    }
}
