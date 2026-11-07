package movilidad.dominio.tarifas;

import movilidad.util.Dinero;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cálculo común a todas las tarifas; cada subclase solo define su tipo de vehículo. */
public abstract class TarifaBase implements PoliticaTarifa {
    public static final int LIMITE_MINUTOS = 120;                            // RN12
    public static final BigDecimal FACTOR_RECARGO = new BigDecimal("1.5");   // S04: +50 %

    private final BigDecimal cargoDesbloqueo;
    private final BigDecimal precioPorMinuto;
    private final LocalDate fechaVigencia;

    protected TarifaBase(BigDecimal cargoDesbloqueo, BigDecimal precioPorMinuto, LocalDate fechaVigencia) {
        this.cargoDesbloqueo = Dinero.redondear(cargoDesbloqueo);
        this.precioPorMinuto = Dinero.redondear(precioPorMinuto);
        this.fechaVigencia = fechaVigencia;
    }

    @Override
    public BigDecimal calcularPrecio(int minutos) {
        return calcularPrecio(minutos, 0, false);
    }

    @Override
    public BigDecimal calcularPrecio(int minutos, int minutosBonificados, boolean bonificaDesbloqueo) {
        int desde = Math.min(Math.max(minutosBonificados, 0), minutos);
        int normales = Math.max(0, Math.min(minutos, LIMITE_MINUTOS) - Math.min(desde, LIMITE_MINUTOS));
        int conRecargo = Math.max(0, minutos - Math.max(desde, LIMITE_MINUTOS));
        BigDecimal total = bonificaDesbloqueo ? Dinero.CERO : cargoDesbloqueo;
        total = total.add(precioPorMinuto.multiply(BigDecimal.valueOf(normales)));
        total = total.add(precioPorMinuto.multiply(FACTOR_RECARGO).multiply(BigDecimal.valueOf(conRecargo)));
        return Dinero.redondear(total);
    }

    @Override
    public BigDecimal getCargoDesbloqueo() {
        return cargoDesbloqueo;
    }

    @Override
    public BigDecimal getPrecioPorMinuto() {
        return precioPorMinuto;
    }

    @Override
    public LocalDate getFechaVigencia() {
        return fechaVigencia;
    }
}
