package movilidad.dominio.tarifas;

import java.math.BigDecimal;

/** Resultado del cálculo económico de un viaje: costo sin promoción, costo final y promoción aplicada (S05). */
public class ResultadoTarifa {
    private final BigDecimal costoBase;
    private final BigDecimal costoFinal;
    private final String promocionAplicada;   // null si no se aplicó ninguna

    public ResultadoTarifa(BigDecimal costoBase, BigDecimal costoFinal, String promocionAplicada) {
        this.costoBase = costoBase;
        this.costoFinal = costoFinal;
        this.promocionAplicada = promocionAplicada;
    }

    public boolean tuvoPromocion() {
        return promocionAplicada != null;
    }

    public BigDecimal getCostoBase() {
        return costoBase;
    }

    public BigDecimal getCostoFinal() {
        return costoFinal;
    }

    public String getPromocionAplicada() {
        return promocionAplicada;
    }
}
