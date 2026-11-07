package movilidad.excepciones;

/** RN01: el usuario ya tiene un viaje en curso. */
public class ViajeAbiertoException extends MovilidadException {
    public ViajeAbiertoException(String mensaje) {
        super(mensaje);
    }
}
