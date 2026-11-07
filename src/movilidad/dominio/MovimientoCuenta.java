package movilidad.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Movimiento de una cuenta (RF02). Inmutable: un movimiento registrado no se elimina,
 * los errores se corrigen con un ajuste (S19, RN15).
 * El importe lleva signo: positivo acredita, negativo debita.
 */
public class MovimientoCuenta {
    private final TipoMovimiento tipo;
    private final LocalDateTime fecha;
    private final BigDecimal importe;
    private final String descripcion;

    public MovimientoCuenta(TipoMovimiento tipo, LocalDateTime fecha, BigDecimal importe, String descripcion) {
        this.tipo = tipo;
        this.fecha = fecha;
        this.importe = importe;
        this.descripcion = descripcion;
    }

    public TipoMovimiento getTipo() { return tipo; }
    public LocalDateTime getFecha() { return fecha; }
    public BigDecimal getImporte() { return importe; }
    public String getDescripcion() { return descripcion; }
}
