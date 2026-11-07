package movilidad.servicios;

import movilidad.dominio.pagos.MedioPago;

import java.math.BigDecimal;

/** Simula el Sistema de Pago Externo (S10): aprueba recargas > $0 con un medio de pago válido. */
public class ServicioPagosSimulado {
    public boolean procesarRecarga(MedioPago medio, BigDecimal monto) {
        return medio != null && medio.validar() && monto != null && monto.signum() > 0;
    }
}
