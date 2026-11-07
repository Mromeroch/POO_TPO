package movilidad.controladores;

import movilidad.dominio.Estacion;
import movilidad.dominio.mantenimiento.OrdenMantenimiento;
import movilidad.dominio.mantenimiento.ReporteFalla;
import movilidad.dominio.mantenimiento.TareaMantenimiento;
import movilidad.dominio.mantenimiento.UsoRepuesto;
import movilidad.dominio.personal.Empleado;
import movilidad.dominio.personal.Operador;
import movilidad.dominio.personal.Tecnico;
import movilidad.dominio.vehiculos.EstadoVehiculo;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.excepciones.*;
import movilidad.persistencia.RepositorioEstaciones;
import movilidad.persistencia.RepositorioMantenimiento;
import movilidad.persistencia.RepositorioPersonal;
import movilidad.persistencia.RepositorioVehiculos;
import movilidad.util.Reloj;

import java.util.List;

/** Casos de uso de mantenimiento: reportar fallas y gestionar órdenes (CU-14 a CU-20). */
public class ControladorMantenimiento {
    private final RepositorioVehiculos repoVehiculos;
    private final RepositorioEstaciones repoEstaciones;
    private final RepositorioMantenimiento repoMantenimiento;
    private final RepositorioPersonal repoPersonal;
    private final Reloj reloj;

    public ControladorMantenimiento(RepositorioVehiculos repoVehiculos, RepositorioEstaciones repoEstaciones,
                                    RepositorioMantenimiento repoMantenimiento, RepositorioPersonal repoPersonal,
                                    Reloj reloj) {
        this.repoVehiculos = repoVehiculos;
        this.repoEstaciones = repoEstaciones;
        this.repoMantenimiento = repoMantenimiento;
        this.repoPersonal = repoPersonal;
        this.reloj = reloj;
    }

    /** CU-14: registra la falla; devuelve true si activó el mantenimiento obligatorio (RN04). */
    public boolean reportarFalla(String codVehiculo, String descripcion, String reportadoPor) throws MovilidadException {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new DatosInvalidosException("Debe ingresar una descripción de la falla");
        }
        Vehiculo v = buscarVehiculo(codVehiculo);
        boolean obligatorio = v.registrarFalla(new ReporteFalla(reloj.ahora(), descripcion.trim(), reportadoPor));
        repoVehiculos.guardar(v);
        return obligatorio;
    }

    /** CU-16 (GRASP Creador: la orden la solicita el Operador). S18: el vehículo sale de la estación y queda EN_REPARACION. */
    public OrdenMantenimiento generarOrden(String codVehiculo, String descripcion, String prioridad, String legajoOperador)
            throws MovilidadException {
        Operador operador = buscarOperador(legajoOperador);
        Vehiculo v = buscarVehiculo(codVehiculo);
        if (v.getEstado() == EstadoVehiculo.EN_USO) {
            throw new OperacionNoPermitidaException("El vehículo está en uso: no puede ingresar a mantenimiento");
        }
        boolean tieneAbierta = repoMantenimiento.buscarOrdenes().stream()
                .anyMatch(o -> o.getVehiculo() == v && o.estaAbierta());
        if (tieneAbierta) {
            throw new OperacionNoPermitidaException("El vehículo ya tiene una orden de mantenimiento abierta");
        }
        for (Estacion e : repoEstaciones.buscarEstaciones()) {
            if (e.contiene(v)) {
                e.egresar(v);
            }
        }
        v.cambiarEstado(EstadoVehiculo.EN_REPARACION);
        OrdenMantenimiento orden = operador.solicitarOrden(repoMantenimiento.siguienteIdOrden(), v,
                descripcion, prioridad, reloj.ahora());
        repoMantenimiento.guardarOrden(orden);
        return orden;
    }

    /** CU-17. */
    public void asignarTecnico(long idOrden, String legajoTecnico) throws MovilidadException {
        OrdenMantenimiento orden = buscarOrden(idOrden);
        orden.asignar(buscarTecnico(legajoTecnico));
        repoMantenimiento.guardarOrden(orden);
    }

    /** CU-19: tareas realizadas. */
    public void registrarTarea(long idOrden, String legajoTecnico, String descripcion) throws MovilidadException {
        OrdenMantenimiento orden = ordenDelTecnico(idOrden, legajoTecnico);
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new DatosInvalidosException("Debe describir la tarea realizada");
        }
        orden.registrarTarea(new TareaMantenimiento(descripcion.trim()));
        repoMantenimiento.guardarOrden(orden);
    }

    /** CU-19: repuestos utilizados. */
    public void registrarRepuesto(long idOrden, String legajoTecnico, String repuesto, int cantidad) throws MovilidadException {
        OrdenMantenimiento orden = ordenDelTecnico(idOrden, legajoTecnico);
        if (repuesto == null || repuesto.trim().isEmpty() || cantidad <= 0) {
            throw new DatosInvalidosException("Repuesto o cantidad inválidos");
        }
        orden.registrarRepuesto(new UsoRepuesto(repuesto.trim(), cantidad));
        repoMantenimiento.guardarOrden(orden);
    }

    /** CU-20 / RN06 / S18. */
    public void cerrarOrden(long idOrden, String legajoTecnico, String codEstacionDestino) throws MovilidadException {
        OrdenMantenimiento orden = ordenDelTecnico(idOrden, legajoTecnico);
        Estacion destino = repoEstaciones.buscarEstacion(codEstacionDestino)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe la estación " + codEstacionDestino));
        orden.cerrar(destino, reloj.ahora());
        repoMantenimiento.guardarOrden(orden);
        repoVehiculos.guardar(orden.getVehiculo());
        destino.evaluarAlerta(reloj.ahora()).ifPresent(repoEstaciones::guardarAlerta);
    }

    /** CU-18. */
    public List<OrdenMantenimiento> consultarOrdenes(String legajoTecnico) throws MovilidadException {
        buscarTecnico(legajoTecnico);
        return repoMantenimiento.buscarOrdenesDeTecnico(legajoTecnico);
    }

    private OrdenMantenimiento ordenDelTecnico(long idOrden, String legajo) throws MovilidadException {
        OrdenMantenimiento orden = buscarOrden(idOrden);
        if (orden.getTecnico() == null || !orden.getTecnico().getLegajo().equals(legajo)) {
            throw new OperacionNoPermitidaException("La orden " + idOrden + " no está asignada al técnico " + legajo);
        }
        if (!orden.estaAbierta()) {
            throw new OperacionNoPermitidaException("La orden " + idOrden + " ya está cerrada");
        }
        return orden;
    }

    private Vehiculo buscarVehiculo(String codigo) throws EntidadNoEncontradaException {
        return repoVehiculos.buscarVehiculo(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el vehículo " + codigo));
    }

    private OrdenMantenimiento buscarOrden(long id) throws EntidadNoEncontradaException {
        return repoMantenimiento.buscarOrdenPorId(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe la orden " + id));
    }

    private Tecnico buscarTecnico(String legajo) throws EntidadNoEncontradaException {
        Empleado e = repoPersonal.buscarEmpleado(legajo).orElse(null);
        if (!(e instanceof Tecnico)) {
            throw new EntidadNoEncontradaException("No existe el técnico con legajo " + legajo);
        }
        return (Tecnico) e;
    }

    private Operador buscarOperador(String legajo) throws EntidadNoEncontradaException {
        Empleado e = repoPersonal.buscarEmpleado(legajo).orElse(null);
        if (!(e instanceof Operador)) {
            throw new EntidadNoEncontradaException("No existe el operador con legajo " + legajo);
        }
        return (Operador) e;
    }
}
