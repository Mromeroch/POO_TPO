
package movilidad.dominio;

import movilidad.dominio.pagos.MedioPago;
import movilidad.util.Dinero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cuenta virtual del usuario (GRASP Experto: conoce el saldo y sus movimientos y es
 * Creador de MovimientoCuenta). El saldo puede quedar negativo tras un débito (RN02).
 */
public class CuentaUsuario {
    private BigDecimal saldo = Dinero.CERO;                                  // S09: saldo inicial $0
    private final List<MovimientoCuenta> movimientos = new ArrayList<>();
    private final List<MedioPago> medios = new ArrayList<>();

    public MovimientoCuenta recargar(BigDecimal monto, LocalDateTime fecha) {
        exigirPositivo(monto);
        return registrar(TipoMovimiento.RECARGA, monto, "Recarga de saldo", fecha);
    }

    public MovimientoCuenta debitar(BigDecimal monto, String descripcion, LocalDateTime fecha) {
        exigirPositivo(monto);
        return registrar(TipoMovimiento.DEBITO, monto.negate(), descripcion, fecha);
    }

    public MovimientoCuenta acreditar(BigDecimal monto, String descripcion, LocalDateTime fecha) {
        exigirPositivo(monto);
        return registrar(TipoMovimiento.AJUSTE, monto, descripcion, fecha);
    }

    /** S19: ajuste con signo (positivo acredita, negativo debita). */
    public MovimientoCuenta aplicarAjuste(BigDecimal importe, String motivo, LocalDateTime fecha) {
        return registrar(TipoMovimiento.AJUSTE, importe, motivo, fecha);
    }

    public void restaurarMovimiento(MovimientoCuenta movimiento) {
        movimientos.add(movimiento);
        saldo = Dinero.redondear(saldo.add(movimiento.getImporte()));
    }

    public boolean tieneSaldoMinimo(BigDecimal minimo) {
        return saldo.compareTo(minimo) >= 0;
    }

    public void asociarMedio(MedioPago medio) { medios.add(medio); }

    private MovimientoCuenta registrar(TipoMovimiento tipo, BigDecimal importeConSigno, String desc, LocalDateTime fecha) {
        BigDecimal importe = Dinero.redondear(importeConSigno);
        MovimientoCuenta mov = new MovimientoCuenta(tipo, fecha, importe, desc);
        movimientos.add(mov);
        saldo = Dinero.redondear(saldo.add(importe));
        return mov;
    }

    private static void exigirPositivo(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor a cero");
        }
    }

    public BigDecimal getSaldo() { return saldo; }
    public List<MovimientoCuenta> getMovimientos() { return Collections.unmodifiableList(movimientos); }
    public List<MedioPago> getMedios() { return Collections.unmodifiableList(medios); }
}
