package movilidad.excepciones;

/** No existe la entidad buscada (usuario, estación, vehículo, viaje, orden...). */
public class EntidadNoEncontradaException extends MovilidadException {
    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
