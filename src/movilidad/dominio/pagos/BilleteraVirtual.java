package movilidad.dominio.pagos;

public class BilleteraVirtual implements MedioPago{
    private final String alias;

    public BilleteraVirtual(String alias) {
        this.alias = alias == null ? "" : alias.trim();
    }
    
    @Override
    public boolean validar() {
        return alias.length() >= 3;
    }
    
    @Override
    public String getTipo() { return "BILLETERA"; }

    @Override
    public String getIdentificador() { return alias; }

    @Override
    public String descripcion() { return "Billetera virtual " + alias; }
}
