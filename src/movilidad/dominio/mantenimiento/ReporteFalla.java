package movilidad.dominio.mantenimiento;

import java.time.LocalDateTime;

/** Reporte de falla de un vehículo (S08): guarda fecha, descripción y quién lo hizo. Inmutable. */
public class ReporteFalla {
    private final LocalDateTime fecha;
    private final String descripcion;
    private final String reportadoPor;   // documento del usuario o legajo del empleado

    public ReporteFalla(LocalDateTime fecha, String descripcion, String reportadoPor) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.reportadoPor = reportadoPor;
    }

    public LocalDateTime getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public String getReportadoPor() { return reportadoPor; }
}
