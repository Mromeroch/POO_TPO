package movilidad.dominio.mantenimiento;

import movilidad.dominio.Estacion;
import movilidad.dominio.personal.Tecnico;
import movilidad.dominio.vehiculos.EstadoVehiculo;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.dominio.vehiculos.VehiculoElectrico;
import movilidad.excepciones.EstacionLlenaException;
import movilidad.excepciones.OrdenIncompletaException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orden de mantenimiento. No se cierra sin técnico, fecha y tareas (RN06).
 * Compone sus tareas y repuestos. Un vehículo tiene como máximo una orden abierta (S18).
 */
public class OrdenMantenimiento {
    private final long id;
    private final Vehiculo vehiculo;
    private final LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private EstadoOrden estado = EstadoOrden.ABIERTA;
    private final String prioridad;
    private final String descripcion;
    private Tecnico tecnico;
    private final List<TareaMantenimiento> tareas = new ArrayList<>();
    private final List<UsoRepuesto> repuestos = new ArrayList<>();

    public OrdenMantenimiento(long id, Vehiculo vehiculo, LocalDateTime fechaApertura,
                              String prioridad, String descripcion) {
        this.id = id;
        this.vehiculo = vehiculo;
        this.fechaApertura = fechaApertura;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
    }

    public void asignar(Tecnico tecnico) { this.tecnico = tecnico; }
    public void registrarTarea(TareaMantenimiento tarea) { tareas.add(tarea); }
    public void registrarRepuesto(UsoRepuesto repuesto) { repuestos.add(repuesto); }

    /**
     * S18: al cerrar, el técnico indica la estación (con lugar) donde el vehículo queda DISPONIBLE,
     * con la batería repuesta.
     */
    public void cerrar(Estacion estacion, LocalDateTime fecha) throws OrdenIncompletaException, EstacionLlenaException {
        if (tecnico == null || fecha == null || tareas.isEmpty()) {
            throw new OrdenIncompletaException("La orden " + id + " no se puede cerrar sin técnico, fecha y tareas realizadas");
        }
        if (estacion.estaLlena()) {
            throw new EstacionLlenaException("La estación " + estacion.getCodigo() + " no tiene lugar para el vehículo");
        }
        estacion.ingresar(vehiculo);
        if (vehiculo instanceof VehiculoElectrico) {
            ((VehiculoElectrico) vehiculo).reponerBateria();
        }
        vehiculo.cambiarEstado(EstadoVehiculo.DISPONIBLE);
        this.fechaCierre = fecha;
        this.estado = EstadoOrden.CERRADA;
    }

    public void restaurarCierre(LocalDateTime fecha) {
        this.fechaCierre = fecha;
        this.estado = EstadoOrden.CERRADA;
    }

    public boolean estaAbierta() { return estado == EstadoOrden.ABIERTA; }

    public long getId() { return id; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public EstadoOrden getEstado() { return estado; }
    public String getPrioridad() { return prioridad; }
    public String getDescripcion() { return descripcion; }
    public Tecnico getTecnico() { return tecnico; }
    public List<TareaMantenimiento> getTareas() { return Collections.unmodifiableList(tareas); }
    public List<UsoRepuesto> getRepuestos() { return Collections.unmodifiableList(repuestos); }
}
