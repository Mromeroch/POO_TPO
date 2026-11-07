package movilidad.dominio.promociones;

import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;
import movilidad.dominio.tarifas.PoliticaTarifa;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Promoción abstracta (OCP, Variaciones protegidas): un beneficio nuevo es una subclase que define su
 * condición y cuántos minutos bonifica. La vigencia y el cálculo son comunes. */
public abstract class Promocion {
    private final String nombre;
    private final LocalDate vigenteDesde;
    private final LocalDate vigenteHasta;

    protected Promocion(String nombre, LocalDate vigenteDesde, LocalDate vigenteHasta) {
        this.nombre = nombre;
        this.vigenteDesde = vigenteDesde;
        this.vigenteHasta = vigenteHasta;
    }

    /** Condición propia de cada promoción (viajes previos, plan vigente, etc.). */
    protected abstract boolean cumpleCondicion(Usuario usuario, Viaje viaje);

    /** Minutos de uso que se bonifican, además del desbloqueo. */
    protected abstract int getMinutosBonificados();

    /** Aplicable si está vigente a la fecha del viaje y se cumple su condición. */
    public final boolean esAplicable(Usuario usuario, Viaje viaje) {
        return estaVigente(viaje.getFin().toLocalDate()) && cumpleCondicion(usuario, viaje);
    }

    /** Costo del viaje con la bonificación: sin desbloqueo y con los primeros minutos gratis (S05). */
    public BigDecimal calcularCosto(Viaje viaje, PoliticaTarifa tarifa) {
        return tarifa.calcularPrecio(viaje.minutosFacturables(), getMinutosBonificados(), true);
    }

    public boolean estaVigente(LocalDate fecha) {
        return !fecha.isBefore(vigenteDesde) && (vigenteHasta == null || !fecha.isAfter(vigenteHasta));
    }

    public String getNombre() {
        return nombre;
    }
}