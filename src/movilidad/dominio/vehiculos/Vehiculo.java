package movilidad.dominio.vehiculos;

import movilidad.dominio.mantenimiento.ReporteFalla;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Clase abstracta de la jerarquía de vehículos (GRASP Polimorfismo). Los atributos comunes son
 * protected para las subclases; lo que varía entre tipos (batería, estado tras el viaje) se sobrescribe. */
public abstract class Vehiculo {
    public static final int FALLAS_PARA_MANTENIMIENTO = 3;   // RN04
    public static final int VENTANA_DIAS_FALLAS = 30;        // RN04, S08

    protected final String codigo;
    protected final LocalDate fechaAlta;
    protected double kilometrosRecorridos;
    protected EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    private boolean mantenimientoPendiente;   // S08: si está EN_USO, el cambio se aplica al terminar el viaje
    private final List<ReporteFalla> fallas = new ArrayList<>();

    protected Vehiculo(String codigo, LocalDate fechaAlta) {
        this.codigo = codigo;
        this.fechaAlta = fechaAlta;
    }

    public abstract TipoVehiculo getTipo();

    /** RN03: solo un vehículo disponible puede retirarse. Los eléctricos lo refinan según su batería. */
    public boolean puedeRetirarse() {
        return estado == EstadoVehiculo.DISPONIBLE;
    }

    /** Las bicicletas comunes no tienen batería: no hace nada. */
    protected void descontarBateria(double kilometros) {
    }
    public void reponerBateria() { }

    /** Al cerrar un viaje suma los kilómetros, descuenta batería (eléctricos) y resuelve el estado:
     * EN_REPARACION si había un mantenimiento pendiente; si no, la subclase decide (DISPONIBLE o BATERIA_BAJA). */
    public void actualizarTrasViaje(double kilometros) {
        kilometrosRecorridos += kilometros;
        descontarBateria(kilometros);
        if (mantenimientoPendiente) {
            mantenimientoPendiente = false;
            estado = EstadoVehiculo.EN_REPARACION;
        } else {
            estado = estadoTrasViaje();
        }
    }

    protected EstadoVehiculo estadoTrasViaje() {
        return EstadoVehiculo.DISPONIBLE;
    }

    /** RN04 / S08: registra la falla. Si con ella hay 3 o más reportes dentro de los 30 días anteriores
     * (contados hacia atrás desde la fecha del nuevo reporte) el vehículo pasa a mantenimiento obligatorio. */
    public boolean registrarFalla(ReporteFalla reporte) {
        fallas.add(reporte);
        LocalDateTime hasta = reporte.getFecha();
        LocalDateTime desde = hasta.minusDays(VENTANA_DIAS_FALLAS);
        long enVentana = fallas.stream()
                .filter(f -> !f.getFecha().isBefore(desde) && !f.getFecha().isAfter(hasta))
                .count();
        if (enVentana < FALLAS_PARA_MANTENIMIENTO) {
            return false;
        }
        if (estado == EstadoVehiculo.EN_USO) {
            mantenimientoPendiente = true;
        } else if (estado == EstadoVehiculo.DISPONIBLE || estado == EstadoVehiculo.BATERIA_BAJA) {
            estado = EstadoVehiculo.EN_REPARACION;
        }
        return true;
    }

    public int cantidadFallas() {
        return fallas.size();
    }

    public List<ReporteFalla> getFallas() {
        return Collections.unmodifiableList(fallas);
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public double getKilometrosRecorridos() {
        return kilometrosRecorridos;
    }

    public EstadoVehiculo getEstado() {
        return estado;
    }

    public void cambiarEstado(EstadoVehiculo nuevo) {
        this.estado = nuevo;
    }

    public boolean tieneMantenimientoPendiente() {
        return mantenimientoPendiente;
    }

    /** Usado por la persistencia: restaura el estado guardado sin aplicar reglas de negocio. */
    public void restaurarEstado(double kilometros, EstadoVehiculo estado, boolean mantenimientoPendiente) {
        this.kilometrosRecorridos = kilometros;
        this.estado = estado;
        this.mantenimientoPendiente = mantenimientoPendiente;
    }

    /** Usado por la persistencia: agrega un reporte guardado sin disparar el mantenimiento obligatorio. */
    public void restaurarFalla(ReporteFalla reporte) {
        fallas.add(reporte);
    }

    @Override
    public String toString() {
        return codigo + " (" + getTipo() + ", " + estado + ")";
    }
}
