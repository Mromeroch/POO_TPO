package movilidad.dominio.mantenimiento;

/** Repuesto utilizado en una orden de mantenimiento. */
public class UsoRepuesto {
    private final String repuesto;
    private final int cantidad;

    public UsoRepuesto(String repuesto, int cantidad) {
        this.repuesto = repuesto;
        this.cantidad = cantidad;
    }

    public String getRepuesto() { return repuesto; }
    public int getCantidad() { return cantidad; }
}
