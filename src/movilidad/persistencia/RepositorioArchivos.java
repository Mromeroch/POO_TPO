package movilidad.persistencia;

import movilidad.dominio.EstadoViaje;
import movilidad.dominio.Estacion;
import movilidad.dominio.MovimientoCuenta;
import movilidad.dominio.PlanMensual;
import movilidad.dominio.PuntoRecorrido;
import movilidad.dominio.TipoMovimiento;
import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;
import movilidad.dominio.mantenimiento.AlertaEstacion;
import movilidad.dominio.mantenimiento.NotaAjuste;
import movilidad.dominio.mantenimiento.OrdenMantenimiento;
import movilidad.dominio.mantenimiento.ReporteFalla;
import movilidad.dominio.mantenimiento.TareaMantenimiento;
import movilidad.dominio.mantenimiento.TipoAlerta;
import movilidad.dominio.mantenimiento.Transferencia;
import movilidad.dominio.mantenimiento.UsoRepuesto;
import movilidad.dominio.pagos.BilleteraVirtual;
import movilidad.dominio.pagos.MedioPago;
import movilidad.dominio.pagos.Tarjeta;
import movilidad.dominio.personal.Administrador;
import movilidad.dominio.personal.Empleado;
import movilidad.dominio.personal.Operador;
import movilidad.dominio.personal.Tecnico;
import movilidad.dominio.tarifas.PoliticaTarifa;
import movilidad.dominio.tarifas.TarifaBicicletaComun;
import movilidad.dominio.tarifas.TarifaBicicletaElectrica;
import movilidad.dominio.tarifas.TarifaMonopatinElectrico;
import movilidad.dominio.vehiculos.BicicletaComun;
import movilidad.dominio.vehiculos.BicicletaElectrica;
import movilidad.dominio.vehiculos.EstadoVehiculo;
import movilidad.dominio.vehiculos.MonopatinElectrico;
import movilidad.dominio.vehiculos.TipoVehiculo;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.dominio.vehiculos.VehiculoElectrico;
import movilidad.util.ArchivoCsv;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistencia en archivos CSV dentro de la carpeta data (S23). Hereda de RepositorioMemoria: los datos
 * viven en memoria y se vuelcan a disco completos después de cada operación que los modifica.
 * Un archivo por entidad; al cargar se respeta el orden de dependencias (personal, estaciones,
 * vehículos, usuarios, viajes, órdenes, traslados y notas).
 */
public class RepositorioArchivos extends RepositorioMemoria {
    private final Path carpetaData;

    public RepositorioArchivos(String carpetaData) {
        this.carpetaData = Paths.get(carpetaData);
    }

    // ------------------------------------------------ guardado automático tras cada cambio
    @Override public void guardar(Usuario u) { super.guardar(u); guardar(); }
    @Override public void guardar(Vehiculo v) { super.guardar(v); guardar(); }
    @Override public void guardar(Estacion e) { super.guardar(e); guardar(); }
    @Override public void guardar(Viaje v) { super.guardar(v); guardar(); }
    @Override public void guardar(Empleado e) { super.guardar(e); guardar(); }
    @Override public void guardarAlerta(AlertaEstacion a) { super.guardarAlerta(a); guardar(); }
    @Override public void guardarOrden(OrdenMantenimiento o) { super.guardarOrden(o); guardar(); }
    @Override public void guardarTransferencia(Transferencia t) { super.guardarTransferencia(t); guardar(); }
    @Override public void guardarNotaAjuste(NotaAjuste n) { super.guardarNotaAjuste(n); guardar(); }

    /** Escribe el estado completo. Lanza UncheckedIOException si no se puede escribir en la carpeta data. */
    public synchronized void guardar() {
        try {
            Files.createDirectories(carpetaData);
            escribirPersonal();
            escribirEstaciones();
            escribirVehiculos();
            escribirUsuarios();
            escribirViajes();
            escribirOrdenes();
            escribirTraslados();
            escribirNotasYAlertas();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudieron guardar los datos en " + carpetaData, e);
        }
    }

    /** Lee los archivos de la carpeta data (si existen) al iniciar el sistema. */
    public synchronized void cargar() {
        try {
            if (!Files.isDirectory(carpetaData)) {
                return;
            }
            leerPersonal();
            leerEstaciones();
            leerVehiculos();
            leerUsuarios();
            leerViajes();
            leerOrdenes();
            leerTraslados();
            leerNotasYAlertas();
        } catch (IOException | RuntimeException e) {
            throw new UncheckedIOException(new IOException(
                    "Los archivos de " + carpetaData + " están dañados o incompletos: " + e.getMessage(), e));
        }
    }

    public Path getCarpetaData() {
        return carpetaData;
    }

    private Path archivo(String nombre) {
        return carpetaData.resolve(nombre);
    }

    private static String t(Object o) { return o == null ? "" : o.toString(); }

    private static LocalDateTime fechaHora(String s) { return s.isEmpty() ? null : LocalDateTime.parse(s); }

    // ====================================================================== ESCRITURA
    private void escribirPersonal() throws IOException {
        List<String[]> filas = new ArrayList<>();
        for (Empleado e : buscarEmpleados()) {
            filas.add(new String[]{e.getRol(), e.getLegajo(), e.getNombre(), e.getTelefono()});
        }
        ArchivoCsv.escribir(archivo("personal.csv"), "rol;legajo;nombre;telefono", filas);
    }

    private void escribirEstaciones() throws IOException {
        List<String[]> filas = new ArrayList<>();
        for (Estacion e : buscarEstaciones()) {
            filas.add(new String[]{e.getCodigo(), e.getDireccion(), t(e.getCapacidadMaxima())});
        }
        ArchivoCsv.escribir(archivo("estaciones.csv"), "codigo;direccion;capacidad", filas);
    }

    private void escribirVehiculos() throws IOException {
        Map<String, String> ubicacion = new HashMap<>();
        for (Estacion e : buscarEstaciones()) {
            for (Vehiculo v : e.getVehiculos()) {
                ubicacion.put(v.getCodigo(), e.getCodigo());
            }
        }
        List<String[]> filas = new ArrayList<>();
        List<String[]> fallas = new ArrayList<>();
        for (Vehiculo v : buscarVehiculos()) {
            String extra = "";
            double bateria = 0;
            if (v instanceof BicicletaComun) {
                extra = t(((BicicletaComun) v).getTamanioRodado());
            } else if (v instanceof BicicletaElectrica) {
                extra = t(((BicicletaElectrica) v).getAutonomia());
            } else if (v instanceof MonopatinElectrico) {
                extra = t(((MonopatinElectrico) v).getVelocidadMaxima());
            }
            if (v instanceof VehiculoElectrico) {
                bateria = ((VehiculoElectrico) v).getNivelBateria();
            }
            filas.add(new String[]{v.getCodigo(), v.getTipo().name(), t(v.getFechaAlta()), extra,
                    t(v.getKilometrosRecorridos()), v.getEstado().name(), t(v.tieneMantenimientoPendiente()),
                    t(bateria), ubicacion.getOrDefault(v.getCodigo(), "")});
            for (ReporteFalla f : v.getFallas()) {
                fallas.add(new String[]{v.getCodigo(), t(f.getFecha()), f.getDescripcion(), f.getReportadoPor()});
            }
        }
        ArchivoCsv.escribir(archivo("vehiculos.csv"),
                "codigo;tipo;fechaAlta;extra;kilometros;estado;mantenimientoPendiente;bateria;estacion", filas);
        ArchivoCsv.escribir(archivo("fallas.csv"), "vehiculo;fecha;descripcion;reportadoPor", fallas);
    }

    private void escribirUsuarios() throws IOException {
        List<String[]> usuarios = new ArrayList<>();
        List<String[]> movimientos = new ArrayList<>();
        List<String[]> medios = new ArrayList<>();
        List<String[]> planes = new ArrayList<>();
        for (Usuario u : buscarUsuarios()) {
            usuarios.add(new String[]{u.getDocumento(), u.getNombre(), u.getCorreoElectronico(), t(u.getFechaAlta()),
                    t(u.estaHabilitado()), t(u.getViajesFinalizados())});
            for (MovimientoCuenta m : u.getCuenta().getMovimientos()) {
                movimientos.add(new String[]{u.getDocumento(), m.getTipo().name(), t(m.getFecha()),
                        m.getImporte().toPlainString(), m.getDescripcion()});
            }
            for (MedioPago m : u.getCuenta().getMedios()) {
                medios.add(new String[]{u.getDocumento(), m.getTipo(), m.getIdentificador()});
            }
            for (PlanMensual p : u.getPlanes()) {
                planes.add(new String[]{u.getDocumento(), t(p.getFechaInicio()), p.getPrecio().toPlainString(),
                        t(p.getViajesConsumidos())});
            }
        }
        ArchivoCsv.escribir(archivo("usuarios.csv"), "documento;nombre;correo;fechaAlta;habilitado;viajesFinalizados", usuarios);
        ArchivoCsv.escribir(archivo("movimientos.csv"), "documento;tipo;fecha;importe;descripcion", movimientos);
        ArchivoCsv.escribir(archivo("medios_pago.csv"), "documento;tipo;identificador", medios);
        ArchivoCsv.escribir(archivo("planes.csv"), "documento;inicio;precio;viajesConsumidos", planes);
    }

    private void escribirViajes() throws IOException {
        List<String[]> viajes = new ArrayList<>();
        List<String[]> puntos = new ArrayList<>();
        for (Viaje v : buscarViajes()) {
            PoliticaTarifa tf = v.getTarifa();
            viajes.add(new String[]{t(v.getId()), v.getUsuario().getDocumento(), v.getVehiculo().getCodigo(),
                    v.getEstacionRetiro().getCodigo(),
                    v.getEstacionDevolucion() == null ? "" : v.getEstacionDevolucion().getCodigo(),
                    t(v.getInicio()), t(v.getFin()), t(v.getDistanciaRecorrida()), v.getEstado().name(),
                    tf.getTipoVehiculo().name(), tf.getCargoDesbloqueo().toPlainString(),
                    tf.getPrecioPorMinuto().toPlainString(), t(tf.getFechaVigencia()),
                    v.getCosto().toPlainString(), t(v.getPromocionAplicada()), t(v.getInstanteEstacionLlena())});
            for (PuntoRecorrido p : v.getPuntos()) {
                puntos.add(new String[]{t(v.getId()), t(p.getLatitud()), t(p.getLongitud()), t(p.getMomento())});
            }
        }
        ArchivoCsv.escribir(archivo("viajes.csv"),
                "id;documento;vehiculo;retiro;devolucion;inicio;fin;distancia;estado;tarifaTipo;tarifaCargo;"
                        + "tarifaPrecioMinuto;tarifaVigencia;costo;promocion;instanteEstacionLlena", viajes);
        ArchivoCsv.escribir(archivo("puntos_recorrido.csv"), "viaje;latitud;longitud;momento", puntos);
    }

    private void escribirOrdenes() throws IOException {
        List<String[]> ordenes = new ArrayList<>();
        List<String[]> tareas = new ArrayList<>();
        List<String[]> repuestos = new ArrayList<>();
        for (OrdenMantenimiento o : buscarOrdenes()) {
            ordenes.add(new String[]{t(o.getId()), o.getVehiculo().getCodigo(), t(o.getFechaApertura()), o.getPrioridad(),
                    o.getDescripcion(), o.getTecnico() == null ? "" : o.getTecnico().getLegajo(),
                    t(o.getFechaCierre()), o.getEstado().name()});
            for (TareaMantenimiento tarea : o.getTareas()) {
                tareas.add(new String[]{t(o.getId()), tarea.getDescripcion()});
            }
            for (UsoRepuesto r : o.getRepuestos()) {
                repuestos.add(new String[]{t(o.getId()), r.getRepuesto(), t(r.getCantidad())});
            }
        }
        ArchivoCsv.escribir(archivo("ordenes_mantenimiento.csv"),
                "id;vehiculo;apertura;prioridad;descripcion;tecnico;cierre;estado", ordenes);
        ArchivoCsv.escribir(archivo("tareas.csv"), "orden;descripcion", tareas);
        ArchivoCsv.escribir(archivo("repuestos.csv"), "orden;repuesto;cantidad", repuestos);
    }

    private void escribirTraslados() throws IOException {
        List<String[]> filas = new ArrayList<>();
        for (Transferencia tr : buscarTransferencias()) {
            List<String> codigos = new ArrayList<>();
            tr.getVehiculos().forEach(v -> codigos.add(v.getCodigo()));
            filas.add(new String[]{t(tr.getFecha()), tr.getOrigen().getCodigo(), tr.getDestino().getCodigo(),
                    tr.getResponsable().getLegajo(), ArchivoCsv.unirLista(codigos)});
        }
        ArchivoCsv.escribir(archivo("traslados.csv"), "fecha;origen;destino;responsable;vehiculos", filas);
    }

    private void escribirNotasYAlertas() throws IOException {
        List<String[]> notas = new ArrayList<>();
        for (NotaAjuste n : buscarNotasAjuste()) {
            notas.add(new String[]{t(n.getViaje().getId()), n.getImporte().toPlainString(), n.getMotivo(),
                    n.getRegistradaPor().getLegajo(), t(n.getFecha())});
        }
        ArchivoCsv.escribir(archivo("notas_ajuste.csv"), "viaje;importe;motivo;operador;fecha", notas);
        List<String[]> alertas = new ArrayList<>();
        for (AlertaEstacion a : buscarAlertas()) {
            alertas.add(new String[]{a.getEstacion().getCodigo(), a.getTipo().name(), t(a.getFecha())});
        }
        ArchivoCsv.escribir(archivo("alertas.csv"), "estacion;tipo;fecha", alertas);
    }

    // ======================================================================= LECTURA
    // Dentro de la lectura se usa super.guardar(...) para no volver a escribir los archivos en cada registro.

    private void leerPersonal() throws IOException {
        for (String[] f : ArchivoCsv.leer(archivo("personal.csv"))) {
            Empleado e;
            switch (f[0]) {
                case "OPERADOR": e = new Operador(f[1], f[2], f[3]); break;
                case "TECNICO": e = new Tecnico(f[1], f[2], f[3]); break;
                default: e = new Administrador(f[1], f[2], f[3]);
            }
            super.guardar(e);
        }
    }

    private void leerEstaciones() throws IOException {
        for (String[] f : ArchivoCsv.leer(archivo("estaciones.csv"))) {
            super.guardar(new Estacion(f[0], f[1], Integer.parseInt(f[2])));
        }
    }

    private void leerVehiculos() throws IOException {
        for (String[] f : ArchivoCsv.leer(archivo("vehiculos.csv"))) {
            LocalDate alta = LocalDate.parse(f[2]);
            double extra = f[3].isEmpty() ? 0 : Double.parseDouble(f[3]);
            Vehiculo v;
            switch (TipoVehiculo.valueOf(f[1])) {
                case BICICLETA_COMUN: v = new BicicletaComun(f[0], alta, (int) extra); break;
                case BICICLETA_ELECTRICA: v = new BicicletaElectrica(f[0], alta, extra); break;
                default: v = new MonopatinElectrico(f[0], alta, extra);
            }
            v.restaurarEstado(Double.parseDouble(f[4]), EstadoVehiculo.valueOf(f[5]), Boolean.parseBoolean(f[6]));
            if (v instanceof VehiculoElectrico) {
                ((VehiculoElectrico) v).restaurarNivelBateria(Double.parseDouble(f[7]));
            }
            super.guardar(v);
            if (!f[8].isEmpty()) {
                buscarEstacion(f[8]).orElseThrow().ingresar(v);
            }
        }
        for (String[] f : ArchivoCsv.leer(archivo("fallas.csv"))) {
            buscarVehiculo(f[0]).orElseThrow().restaurarFalla(new ReporteFalla(LocalDateTime.parse(f[1]), f[2], f[3]));
        }
    }

    private void leerUsuarios() throws IOException {
        for (String[] f : ArchivoCsv.leer(archivo("usuarios.csv"))) {
            Usuario u = new Usuario(f[1], f[0], f[2], LocalDate.parse(f[3]));
            u.restaurarEstado(Boolean.parseBoolean(f[4]), Integer.parseInt(f[5]));
            super.guardar(u);
        }
        for (String[] f : ArchivoCsv.leer(archivo("movimientos.csv"))) {
            buscarPorDocumento(f[0]).orElseThrow().getCuenta().restaurarMovimiento(
                    new MovimientoCuenta(TipoMovimiento.valueOf(f[1]), LocalDateTime.parse(f[2]), new BigDecimal(f[3]), f[4]));
        }
        for (String[] f : ArchivoCsv.leer(archivo("medios_pago.csv"))) {
            MedioPago medio = f[1].equals("TARJETA") ? new Tarjeta(f[2]) : new BilleteraVirtual(f[2]);
            buscarPorDocumento(f[0]).orElseThrow().getCuenta().asociarMedio(medio);
        }
        for (String[] f : ArchivoCsv.leer(archivo("planes.csv"))) {
            PlanMensual plan = new PlanMensual(LocalDateTime.parse(f[1]), new BigDecimal(f[2]));
            plan.restaurarViajesConsumidos(Integer.parseInt(f[3]));
            buscarPorDocumento(f[0]).orElseThrow().restaurarPlan(plan);
        }
    }

    private static PoliticaTarifa tarifa(TipoVehiculo tipo, BigDecimal cargo, BigDecimal precio, LocalDate vigencia) {
        return switch (tipo) {
            case BICICLETA_COMUN -> new TarifaBicicletaComun(cargo, precio, vigencia);
            case BICICLETA_ELECTRICA -> new TarifaBicicletaElectrica(cargo, precio, vigencia);
            default -> new TarifaMonopatinElectrico(cargo, precio, vigencia);
        };
    }

    private void leerViajes() throws IOException {
        Map<Long, List<PuntoRecorrido>> puntos = new HashMap<>();
        for (String[] f : ArchivoCsv.leer(archivo("puntos_recorrido.csv"))) {
            puntos.computeIfAbsent(Long.parseLong(f[0]), k -> new ArrayList<>()).add(
                    new PuntoRecorrido(Double.parseDouble(f[1]), Double.parseDouble(f[2]), LocalDateTime.parse(f[3])));
        }
        for (String[] f : ArchivoCsv.leer(archivo("viajes.csv"))) {
            long id = Long.parseLong(f[0]);
            PoliticaTarifa tf = tarifa(TipoVehiculo.valueOf(f[9]), new BigDecimal(f[10]), new BigDecimal(f[11]), LocalDate.parse(f[12]));
            Viaje v = new Viaje(id, buscarPorDocumento(f[1]).orElseThrow(), buscarVehiculo(f[2]).orElseThrow(),
                    buscarEstacion(f[3]).orElseThrow(), tf, LocalDateTime.parse(f[5]));
            puntos.getOrDefault(id, List.of()).forEach(v::agregarPunto);
            if (!f[15].isEmpty()) {
                v.registrarEstacionLlena(LocalDateTime.parse(f[15]));
            }
            switch (EstadoViaje.valueOf(f[8])) {
                case FINALIZADO: v.finalizar(buscarEstacion(f[4]).orElseThrow(), LocalDateTime.parse(f[6]), Double.parseDouble(f[7])); break;
                case CANCELADO: v.cancelar(); break;
                case PERDIDO: v.marcarPerdido(); break;
                default: break;   // EN_CURSO
            }
            v.setCosto(new BigDecimal(f[13]));
            v.setPromocionAplicada(f[14].isEmpty() ? null : f[14]);
            super.guardar(v);
        }
        restaurarContadorViajes();
    }

    private void leerOrdenes() throws IOException {
        Map<Long, OrdenMantenimiento> porId = new HashMap<>();
        for (String[] f : ArchivoCsv.leer(archivo("ordenes_mantenimiento.csv"))) {
            OrdenMantenimiento o = new OrdenMantenimiento(Long.parseLong(f[0]), buscarVehiculo(f[1]).orElseThrow(),
                    LocalDateTime.parse(f[2]), f[3], f[4]);
            if (!f[5].isEmpty()) {
                o.asignar((Tecnico) buscarEmpleado(f[5]).orElseThrow());
            }
            porId.put(o.getId(), o);
        }
        for (String[] f : ArchivoCsv.leer(archivo("tareas.csv"))) {
            porId.get(Long.parseLong(f[0])).registrarTarea(new TareaMantenimiento(f[1]));
        }
        for (String[] f : ArchivoCsv.leer(archivo("repuestos.csv"))) {
            porId.get(Long.parseLong(f[0])).registrarRepuesto(new UsoRepuesto(f[1], Integer.parseInt(f[2])));
        }
        for (String[] f : ArchivoCsv.leer(archivo("ordenes_mantenimiento.csv"))) {
            OrdenMantenimiento o = porId.get(Long.parseLong(f[0]));
            if (f[7].equals("CERRADA")) {
                o.restaurarCierre(fechaHora(f[6]));
            }
            super.guardarOrden(o);
        }
        restaurarContadorOrdenes();
    }

    private void leerTraslados() throws IOException {
        for (String[] f : ArchivoCsv.leer(archivo("traslados.csv"))) {
            List<Vehiculo> vehiculos = new ArrayList<>();
            for (String codigo : ArchivoCsv.separarLista(f[4])) {
                vehiculos.add(buscarVehiculo(codigo).orElseThrow());
            }
            Empleado emp = buscarEmpleado(f[3]).orElseThrow();
            if (!(emp instanceof Operador)) {
                throw new IllegalStateException("El empleado con legajo " + f[3] + " no es un Operador válido para el traslado.");
            }
            super.guardarTransferencia(new Transferencia(LocalDateTime.parse(f[0]), buscarEstacion(f[1]).orElseThrow(),
                    buscarEstacion(f[2]).orElseThrow(), (Operador) emp, vehiculos));
        }
    }

    private void leerNotasYAlertas() throws IOException {
        for (String[] f : ArchivoCsv.leer(archivo("notas_ajuste.csv"))) {
            super.guardarNotaAjuste(new NotaAjuste(buscarPorId(Long.parseLong(f[0])).orElseThrow(), new BigDecimal(f[1]), f[2],
                    (Operador) buscarEmpleado(f[3]).orElseThrow(), LocalDateTime.parse(f[4])));
        }
        for (String[] f : ArchivoCsv.leer(archivo("alertas.csv"))) {
            super.guardarAlerta(new AlertaEstacion(buscarEstacion(f[0]).orElseThrow(), TipoAlerta.valueOf(f[1]), LocalDateTime.parse(f[2])));
        }
    }
}
