package movilidad.dominio.tarifas;

import movilidad.dominio.vehiculos.TipoVehiculo;
import movilidad.util.Dinero;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TarifaBicicletaElectrica extends TarifaBase {
    /** Valores de referencia del enunciado (S25): $300 de desbloqueo + $45 por minuto. */
    public static final BigDecimal CARGO_REFERENCIA = Dinero.de("300");
    public static final BigDecimal PRECIO_MINUTO_REFERENCIA = Dinero.de("45");

    public TarifaBicicletaElectrica(LocalDate fechaVigencia) {
        this(CARGO_REFERENCIA, PRECIO_MINUTO_REFERENCIA, fechaVigencia);
    }

    public TarifaBicicletaElectrica(BigDecimal cargoDesbloqueo, BigDecimal precioPorMinuto, LocalDate fechaVigencia) {
        super(cargoDesbloqueo, precioPorMinuto, fechaVigencia);
    }

    @Override
    public TipoVehiculo getTipoVehiculo() {
        return TipoVehiculo.BICICLETA_ELECTRICA;
    }
}