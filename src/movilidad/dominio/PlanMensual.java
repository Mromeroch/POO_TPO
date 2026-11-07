package movilidad.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Plan mensual (S06): dura 30 días e incluye 20 viajes de hasta 30 minutos. */
public class PlanMensual {
    public static final int DURACION_DIAS = 30;
    public static final int VIAJES_INCLUIDOS = 20;
    public static final int MINUTOS_BONIFICADOS = 30;

    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;
    private final BigDecimal precio;
    private int viajesConsumidos;

    public PlanMensual(LocalDateTime fechaInicio, BigDecimal precio) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaInicio.plusDays(DURACION_DIAS);
        this.precio = precio;
    }

    /** Vigencia por fecha (no considera los viajes restantes). */
    public boolean estaVigente(LocalDateTime fecha) {
        return !fecha.isBefore(fechaInicio) && fecha.isBefore(fechaFin);
    }

    public boolean tieneViajesDisponibles() {
        return viajesConsumidos < VIAJES_INCLUIDOS;
    }

    /** Vigente por fecha y con viajes restantes: es el caso en que la promoción es aplicable. */
    public boolean estaActivo(LocalDateTime fecha) {
        return estaVigente(fecha) && tieneViajesDisponibles();
    }

    public void consumirViaje() {
        if (tieneViajesDisponibles()) {
            viajesConsumidos++;
        }
    }

    public void restaurarViajesConsumidos(int viajesConsumidos) {
        this.viajesConsumidos = viajesConsumidos;
    }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public BigDecimal getPrecio() { return precio; }
    public int getViajesConsumidos() { return viajesConsumidos; }
}
