package movilidad.excepciones;

/** CU-11 A1: no existe tarifa vigente para el tipo de vehículo y la fecha. */
public class TarifaNoVigenteException extends MovilidadException {
    public TarifaNoVigenteException(String mensaje) {
        super(mensaje);
    }
}
