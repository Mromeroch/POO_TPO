package movilidad.excepciones;

public class ReglaNegocioException extends Exception{
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
