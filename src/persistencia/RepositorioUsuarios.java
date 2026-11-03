package movilidad.persistencia;

import movilidad.dominio.Usuario;

import java.util.List;
import java.util.Optional;

/** Abstracción de acceso a usuarios (DIP): los controladores no conocen el almacenamiento. */
public interface RepositorioUsuarios {
    void guardar(Usuario usuario);

    Optional<Usuario> buscarPorDocumento(String documento);

    boolean existe(String documento);

    List<Usuario> buscarUsuarios();
}
