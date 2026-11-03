package movilidad.servicios;

import movilidad.dominio.tarifas.PoliticaTarifa;
import movilidad.dominio.vehiculos.TipoVehiculo;
import movilidad.excepciones.TarifaNoVigenteException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Selecciona la tarifa vigente y calcula el saldo mínimo de retiro (S01, S16). Trabaja con PoliticaTarifa. */
public class ServicioTarifas {
    /** S01: el saldo mínimo equivale al desbloqueo más 30 minutos de uso. */
    public static final int MINUTOS_SALDO_MINIMO = 30;

    private final List<PoliticaTarifa> tarifas = new ArrayList<>();

    public void registrarTarifa(PoliticaTarifa tarifa) {
        tarifas.add(tarifa);
    }

    /** S16: la tarifa del tipo con la mayor fecha de vigencia menor o igual a la fecha pedida. */
    public PoliticaTarifa tarifaVigente(TipoVehiculo tipo, LocalDate fecha) throws TarifaNoVigenteException {
        return tarifas.stream()
                .filter(t -> t.getTipoVehiculo() == tipo && !t.getFechaVigencia().isAfter(fecha))
                .max((a, b) -> a.getFechaVigencia().compareTo(b.getFechaVigencia()))
                .orElseThrow(() -> new TarifaNoVigenteException("No hay tarifa vigente para " + tipo + " al " + fecha));
    }

    public BigDecimal saldoMinimo(PoliticaTarifa tarifa) {
        return tarifa.calcularPrecio(MINUTOS_SALDO_MINIMO);
    }

    public List<PoliticaTarifa> getTarifas() { return Collections.unmodifiableList(tarifas); }
}
