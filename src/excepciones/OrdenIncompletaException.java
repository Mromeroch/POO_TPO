package movilidad.excepciones;

/** RN06: la orden no puede cerrarse sin técnico, fecha y tareas realizadas. */
public class OrdenIncompletaException extends MovilidadException {
    public OrdenIncompletaException(String mensaje) {
        super(mensaje);
    }
}
