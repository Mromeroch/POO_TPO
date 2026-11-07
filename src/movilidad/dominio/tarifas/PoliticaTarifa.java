package movilidad.dominio.tarifas;

import movilidad.dominio.vehiculos.TipoVehiculo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Abstracción de tarifa (OCP, DIP, Variaciones protegidas): los clientes dependen de este contrato
 * y no de la tarifa concreta de cada tipo de vehículo.
 */
public interface PoliticaTarifa {
    TipoVehiculo getTipoVehiculo();

    LocalDate getFechaVigencia();

    BigDecimal getCargoDesbloqueo();

    BigDecimal getPrecioPorMinuto();

    /** Precio normal: desbloqueo + minutos, con 50 % de recargo sobre los minutos posteriores al 120 (RN10, RN12, S04). */
    BigDecimal calcularPrecio(int minutos);

    /** Precio con bonificación: primeros {@code minutosBonificados} gratis y, opcionalmente, desbloqueo gratis (S05). */
    BigDecimal calcularPrecio(int minutos, int minutosBonificados, boolean bonificaDesbloqueo);
}

