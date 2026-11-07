package movilidad.excepciones;

public class SaldoInsuficienteException extends ReglaNegocioException {
    public SaldoInsuficienteException(String mensaje){
        super(mensaje);
    }
}
