package movilidad.excepciones;

/**
 * Raíz de las excepciones propias del sistema (errores de reglas de negocio).
 * Es abstracta y verificada: los controladores la declaran con throws y la interfaz la captura.
 */
public abstract class MovilidadException extends Exception {
    protected MovilidadException(String mensaje) {
        super(mensaje);
    }
}
