package movilidad.dominio.tarifas;

import movilidad.dominio.vehiculos.TipoVehiculo;
import movilidad.util.Dinero;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TarifaBicicletaComun extends TarifaBase {
    /** Valores de referencia del enunciado (S25): $200 de desbloqueo + $20 por minuto. */
    public static final BigDecimal CARGO_REFERENCIA = Dinero.de("200");
    public static final BigDecimal PRECIO_MINUTO_REFERENCIA = Dinero.de("20");

    public TarifaBicicletaComun(LocalDate fechaVigencia) {
        this(CARGO_REFERENCIA, PRECIO_MINUTO_REFERENCIA, fechaVigencia);
    }

    public TarifaBicicletaComun(BigDecimal cargoDesbloqueo, BigDecimal precioPorMinuto, LocalDate fechaVigencia) {
        super(cargoDesbloqueo, precioPorMinuto, fechaVigencia);
    }

    @Override
    public TipoVehiculo getTipoVehiculo() {
        return TipoVehiculo.BICICLETA_COMUN;
    }
}