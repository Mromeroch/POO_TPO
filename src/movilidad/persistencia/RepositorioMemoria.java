package movilidad.persistencia;

import movilidad.dominio.Estacion;
import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;
import movilidad.dominio.EstadoViaje;
import movilidad.dominio.mantenimiento.AlertaEstacion;
import movilidad.dominio.mantenimiento.NotaAjuste;
import movilidad.dominio.mantenimiento.OrdenMantenimiento;
import movilidad.dominio.mantenimiento.Transferencia;
import movilidad.dominio.personal.Empleado;
import movilidad.dominio.vehiculos.Vehiculo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación en memoria de todos los repositorios. Sirve para las pruebas unitarias
 * y es la base de RepositorioArchivos (que agrega la lectura/escritura en disco).
 */
public class RepositorioMemoria implements RepositorioUsuarios, RepositorioVehiculos, RepositorioEstaciones,
        RepositorioViajes, RepositorioMantenimiento, RepositorioPersonal {

    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();
    private final Map<String, Vehiculo> vehiculos = new LinkedHashMap<>();
    private final Map<String, Estacion> estaciones = new LinkedHashMap<>();
    private final List<AlertaEstacion> alertas = new ArrayList<>();
    private final Map<Long, Viaje> viajes = new LinkedHashMap<>();
    private final Map<Long, OrdenMantenimiento> ordenes = new LinkedHashMap<>();
    private final List<Transferencia> transferencias = new ArrayList<>();
    private final List<NotaAjuste> notas = new ArrayList<>();
    private final Map<String, Empleado> personal = new LinkedHashMap<>();
    private long ultimoIdViaje = 0;
    private long ultimoIdOrden = 0;

    // ---- usuarios ----
    @Override public void guardar(Usuario u) { usuarios.put(u.getDocumento(), u); }
    @Override public Optional<Usuario> buscarPorDocumento(String documento) { return Optional.ofNullable(usuarios.get(documento)); }
    @Override public boolean existe(String documento) { return usuarios.containsKey(documento); }
    @Override public List<Usuario> buscarUsuarios() { return new ArrayList<>(usuarios.values()); }

    // ---- vehiculos ----
    @Override public void guardar(Vehiculo v) { vehiculos.put(v.getCodigo().toUpperCase(), v); }
    @Override public Optional<Vehiculo> buscarVehiculo(String codigo) { return Optional.ofNullable(vehiculos.get(codigo.toUpperCase())); }
    @Override public List<Vehiculo> buscarVehiculos() { return new ArrayList<>(vehiculos.values()); }

    // ---- estaciones ----
    @Override public void guardar(Estacion e) { estaciones.put(e.getCodigo().toUpperCase(), e); }
    @Override public Optional<Estacion> buscarEstacion(String codigo) { return Optional.ofNullable(estaciones.get(codigo.toUpperCase())); }
    @Override public List<Estacion> buscarEstaciones() { return new ArrayList<>(estaciones.values()); }
    @Override public void guardarAlerta(AlertaEstacion a) { alertas.add(a); }
    @Override public List<AlertaEstacion> buscarAlertas() { return new ArrayList<>(alertas); }

    // ---- viajes ----
    @Override public long siguienteId() { return ++ultimoIdViaje; }
        /** Usado por la persistencia: después de cargar, el próximo id continúa desde el mayor id existente. */
    public void restaurarContadorViajes() {
        ultimoIdViaje = viajes.keySet().stream().mapToLong(Long::longValue).max().orElse(0);
    }
    @Override public void guardar(Viaje v) { viajes.put(v.getId(), v); }
    @Override public Optional<Viaje> buscarPorId(long id) { return Optional.ofNullable(viajes.get(id)); }
    @Override public List<Viaje> buscarViajes() { return new ArrayList<>(viajes.values()); }
    @Override public Optional<Viaje> buscarEnCursoDe(Usuario u) {
        return viajes.values().stream().filter(v -> v.getUsuario() == u && v.getEstado() == EstadoViaje.EN_CURSO).findFirst();
    }
    @Override public List<Viaje> buscarPorUsuario(String documento) {
        return viajes.values().stream().filter(v -> v.getUsuario().getDocumento().equals(documento)).collect(Collectors.toList());
    }

    // ---- mantenimiento ----
    @Override public long siguienteIdOrden() { return ++ultimoIdOrden; }
        /** Usado por la persistencia: después de cargar, el próximo id continúa desde el mayor id existente. */
    public void restaurarContadorOrdenes() {
        ultimoIdOrden = ordenes.keySet().stream().mapToLong(Long::longValue).max().orElse(0);
    }
        
    @Override public void guardarOrden(OrdenMantenimiento o) { ordenes.put(o.getId(), o); }
    @Override public Optional<OrdenMantenimiento> buscarOrdenPorId(long id) { return Optional.ofNullable(ordenes.get(id)); }
    @Override public List<OrdenMantenimiento> buscarOrdenes() { return new ArrayList<>(ordenes.values()); }
    @Override public List<OrdenMantenimiento> buscarOrdenesDeTecnico(String legajo) {
        return ordenes.values().stream()
                .filter(o -> o.getTecnico() != null && o.getTecnico().getLegajo().equals(legajo))
                .collect(Collectors.toList());
    }
    @Override public void guardarTransferencia(Transferencia t) { transferencias.add(t); }
    @Override public List<Transferencia> buscarTransferencias() { return new ArrayList<>(transferencias); }
    @Override public void guardarNotaAjuste(NotaAjuste n) { notas.add(n); }
    @Override public List<NotaAjuste> buscarNotasAjuste() { return new ArrayList<>(notas); }

    // ---- personal ----
    @Override public void guardar(Empleado e) { personal.put(e.getLegajo(), e); }
    @Override public Optional<Empleado> buscarEmpleado(String legajo) { return Optional.ofNullable(personal.get(legajo)); }
    @Override public List<Empleado> buscarEmpleados() { return new ArrayList<>(personal.values()); }
    

}
