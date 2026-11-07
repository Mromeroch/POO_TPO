package movilidad.excepciones;

/** Datos de entrada incompletos o inválidos (CU-01 A1, CU-02 A1, CU-23 A1...). */
public class DatosInvalidosException extends MovilidadException {
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
