package movilidad.dominio.pagos;

/** Tarjeta de crédito o débito. Se acepta un número de 13 a 19 dígitos, con o sin espacios. */
public class Tarjeta implements MedioPago {
    private final String numero;   // solo dígitos

    public Tarjeta(String numero) {
        this.numero = numero == null ? "" : numero.replaceAll("\\s+", "");
    }

    @Override
    public boolean validar() {
        return numero.matches("\\d{13,19}");
    }

    /** Ejemplo: "**** **** **** 1234". */
    public String getNumeroEnmascarado() {
        String ultimos = numero.length() >= 4 ? numero.substring(numero.length() - 4) : numero;
        return "**** **** **** " + ultimos;
    }

    @Override
    public String getTipo() { return "TARJETA"; }

    @Override
    public String getIdentificador() { return numero; }

    @Override
    public String descripcion() { return "Tarjeta " + getNumeroEnmascarado(); }
}