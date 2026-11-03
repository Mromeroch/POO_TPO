package movilidad.excepciones;

/** RN03/RN05: el vehículo no está disponible o tiene batería baja. */
public class VehiculoNoDisponibleException extends MovilidadException {
    public VehiculoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
