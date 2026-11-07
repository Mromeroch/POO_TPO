package movilidad.dominio.tarifas;

import movilidad.dominio.vehiculos.TipoVehiculo;
import movilidad.util.Dinero;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TarifaMonopatinElectrico extends TarifaBase {
    /** Valores de referencia del enunciado (S25): $300 de desbloqueo + $50 por minuto. */
    public static final BigDecimal CARGO_REFERENCIA = Dinero.de("300");
    public static final BigDecimal PRECIO_MINUTO_REFERENCIA = Dinero.de("50");

    public TarifaMonopatinElectrico(LocalDate fechaVigencia) {
        this(CARGO_REFERENCIA, PRECIO_MINUTO_REFERENCIA, fechaVigencia);
    }

    public TarifaMonopatinElectrico(BigDecimal cargoDesbloqueo, BigDecimal precioPorMinuto, LocalDate fechaVigencia) {
        super(cargoDesbloqueo, precioPorMinuto, fechaVigencia);
    }

    @Override
    public TipoVehiculo getTipoVehiculo() {
        return TipoVehiculo.MONOPATIN_ELECTRICO;
    }
}
