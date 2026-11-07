package movilidad.excepciones;

/** RN11: pasaron más de 2 minutos desde el retiro, el viaje ya no puede cancelarse. */
public class RetiroNoCancelableException extends MovilidadException {
    public RetiroNoCancelableException(String mensaje) {
        super(mensaje);
    }
}
