
package movilidad.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Utilidades para importes en pesos: BigDecimal con 2 decimales (S25). */
public final class Dinero {
    public static final BigDecimal CERO = redondear(BigDecimal.ZERO);

    private Dinero() { }

    public static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal de(String valor) {
        return redondear(new BigDecimal(valor));
    }

    public static BigDecimal de(double valor) {
        return redondear(BigDecimal.valueOf(valor));
    }
}
