package movilidad.dominio.promociones;

import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;

import java.time.LocalDate;

/** S05: bonifica el desbloqueo y los primeros 15 minutos; solo si el usuario no tiene viajes finalizados previos. */
public class PromocionPrimerViaje extends Promocion {
    public static final String NOMBRE = "Primer viaje gratis";
    public static final int MINUTOS_BONIFICADOS = 15;

    public PromocionPrimerViaje() {
        this(LocalDate.of(2000, 1, 1), null);
    }

    public PromocionPrimerViaje(LocalDate vigenteDesde, LocalDate vigenteHasta) {
        super(NOMBRE, vigenteDesde, vigenteHasta);
    }

    @Override
    protected boolean cumpleCondicion(Usuario usuario, Viaje viaje) {
        return usuario.getViajesFinalizados() == 0;
    }

    @Override
    protected int getMinutosBonificados() {
        return MINUTOS_BONIFICADOS;
    }
}
