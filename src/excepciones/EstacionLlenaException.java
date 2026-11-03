package movilidad.excepciones;

/** RN07: la estación no tiene lugar para recibir el vehículo. */
public class EstacionLlenaException extends MovilidadException {
    public EstacionLlenaException(String mensaje) {
        super(mensaje);
    }
}

