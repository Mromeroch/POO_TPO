package movilidad.excepciones;

/** La operación viola una regla del dominio (traslado inválido, orden duplicada, etc.). */
public class OperacionNoPermitidaException extends MovilidadException {
    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
