package movilidad.dominio.promociones;

import movilidad.dominio.PlanMensual;
import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;

import java.time.LocalDate;

/** S05/S06: con un plan vigente y con viajes disponibles se bonifica el desbloqueo y hasta 30 minutos por viaje. */
public class PromocionPlanMensual extends Promocion {
    public static final String NOMBRE = "Plan mensual";

    public PromocionPlanMensual() {
        this(LocalDate.of(2000, 1, 1), null);
    }

    public PromocionPlanMensual(LocalDate vigenteDesde, LocalDate vigenteHasta) {
        super(NOMBRE, vigenteDesde, vigenteHasta);
    }

    @Override
    protected boolean cumpleCondicion(Usuario usuario, Viaje viaje) {
        return usuario.getPlanActivo(viaje.getFin()).isPresent();
    }

    @Override
    protected int getMinutosBonificados() {
        return PlanMensual.MINUTOS_BONIFICADOS;
    }
}
