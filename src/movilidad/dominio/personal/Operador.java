package movilidad.dominio.personal;

import movilidad.dominio.mantenimiento.OrdenMantenimiento;
import movilidad.dominio.vehiculos.Vehiculo;

import java.time.LocalDateTime;

/** Operador del sistema: redistribuye y solicita las ordenas de mantenimiento (GRASP Creador). */
public class Operador extends Empleado {
    public Operador(String legajo, String nombre, String telefono) {
        super(legajo, nombre, telefono);
    }

    /** Crea una orden de mantenimiento abierta para el vehiculo (CU-16). */
    public OrdenMantenimiento solicitarOrden(long id, Vehiculo vehiculo, String descripcion, String prioridad, LocalDateTime fecha) {
        return new OrdenMantenimiento(id, vehiculo, fecha, prioridad, descripcion);
    }

    @Override
    public String getRol() {
        return "OPERADOR";
    }
}
