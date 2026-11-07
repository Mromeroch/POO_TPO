package movilidad.servicios;

import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;
import movilidad.dominio.promociones.Promocion;
import movilidad.dominio.tarifas.PoliticaTarifa;
import movilidad.dominio.tarifas.ResultadoTarifa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evalúa las promociones por separado y cobra el menor importe (RN14, S05).
 * Variaciones protegidas: agregar una promoción nueva no obliga a tocar esta clase.
 */
public class ServicioPromociones implements EvaluadorPromociones {
    private final List<Promocion> promociones = new ArrayList<>();

    public void registrarPromocion(Promocion promocion) {
        promociones.add(promocion);
    }

    @Override
    public ResultadoTarifa calcularCostoViaje(Viaje viaje, Usuario usuario) {
        PoliticaTarifa tarifa = viaje.getTarifa();
        BigDecimal costoBase = tarifa.calcularPrecio(viaje.minutosFacturables());
        BigDecimal mejorCosto = costoBase;
        String mejorPromocion = null;
        for (Promocion promocion : promociones) {
            if (promocion.esAplicable(usuario, viaje)) {
                BigDecimal costo = promocion.calcularCosto(viaje, tarifa);
                if (costo.compareTo(mejorCosto) < 0) {    // no se acumulan: gana la más favorable
                    mejorCosto = costo;
                    mejorPromocion = promocion.getNombre();
                }
            }
        }
        return new ResultadoTarifa(costoBase, mejorCosto, mejorPromocion);
    }

    public List<Promocion> getPromociones() { return Collections.unmodifiableList(promociones); }
}
