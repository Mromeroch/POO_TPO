package movilidad.servicios;

/** Fila del reporte "uso por estación" (S24): retiros y devoluciones. */
public class UsoEstacion {
    private final String codigoEstacion;
    private final long retiros;
    private final long devoluciones;

    public UsoEstacion(String codigoEstacion, long retiros, long devoluciones) {
        this.codigoEstacion = codigoEstacion;
        this.retiros = retiros;
        this.devoluciones = devoluciones;
    }

    public String getCodigoEstacion() { return codigoEstacion; }
    public long getRetiros() { return retiros; }
    public long getDevoluciones() { return devoluciones; }
}
