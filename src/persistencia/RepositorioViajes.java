package movilidad.persistencia;

import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;

import java.util.List;
import java.util.Optional;

public interface RepositorioViajes {
    /** Siguiente identificador de viaje (el modelo no lo asigna: lo hace la persistencia). */
    long siguienteId();

    void guardar(Viaje viaje);

    Optional<Viaje> buscarPorId(long id);

    /** Viaje EN_CURSO del usuario, si existe (RN01). */
    Optional<Viaje> buscarEnCursoDe(Usuario usuario);

    List<Viaje> buscarPorUsuario(String documento);

    List<Viaje> buscarViajes();
}
