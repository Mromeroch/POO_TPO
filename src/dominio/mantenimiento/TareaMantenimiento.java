package movilidad.dominio.mantenimiento;

/** Tarea realizada dentro de una orden de mantenimiento. */
public class TareaMantenimiento {
    private final String descripcion;

    public TareaMantenimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() { return descripcion; }
}
