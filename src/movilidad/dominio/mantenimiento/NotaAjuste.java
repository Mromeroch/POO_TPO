package movilidad.dominio.mantenimiento;

import movilidad.dominio.Viaje;
import movilidad.dominio.personal.Operador;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Corrección de un viaje cerrado (RF14, RN15, S19). Importe positivo acredita, negativo debita.
 * No modifica el viaje original; genera un movimiento AJUSTE en la cuenta.
 */
public class NotaAjuste {
    private final Viaje viaje;
    private final BigDecimal importe;
    private final String motivo;
    private final Operador registradaPor;
    private final LocalDateTime fecha;

    public NotaAjuste(Viaje viaje, BigDecimal importe, String motivo, Operador registradaPor, LocalDateTime fecha) {
        this.viaje = viaje;
        this.importe = importe;
        this.motivo = motivo;
        this.registradaPor = registradaPor;
        this.fecha = fecha;
    }

    public Viaje getViaje() { return viaje; }
    public BigDecimal getImporte() { return importe; }
    public String getMotivo() { return motivo; }
    public Operador getRegistradaPor() { return registradaPor; }
    public LocalDateTime getFecha() { return fecha; }
}
