package excepciones;

public class UsuarioInhabilitadoException extends ReglaNegocioException {
    public UsuarioInhabilitadoException(String mensaje) {
        super(mensaje);
    }
}
