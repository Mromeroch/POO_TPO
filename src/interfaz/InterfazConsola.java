package movilidad.interfaz;

import movilidad.Sistema;
import movilidad.controladores.*;
import movilidad.dominio.Estacion;
import movilidad.dominio.MovimientoCuenta;
import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;
import movilidad.dominio.mantenimiento.AlertaEstacion;
import movilidad.dominio.mantenimiento.OrdenMantenimiento;
import movilidad.dominio.pagos.BilleteraVirtual;
import movilidad.dominio.pagos.MedioPago;
import movilidad.dominio.pagos.Tarjeta;
import movilidad.dominio.tarifas.ResultadoTarifa;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.excepciones.MovilidadException;
import movilidad.servicios.UsoEstacion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Interfaz por consola con menú separado por rol, sin autenticación (S22).
 * Solo conoce a los controladores: no contiene reglas de negocio (GRASP Controlador).
 * Las MovilidadException de los controladores se capturan acá y se muestran como mensajes.
 */
public class InterfazConsola {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ControladorViajes viajes;
    private final ControladorUsuarios usuarios;
    private final ControladorMantenimiento mantenimiento;
    private final ControladorOperaciones operaciones;
    private final ControladorReportes reportes;
    private final BufferedReader entrada;
    private final PrintStream salida;

    public InterfazConsola(Sistema sistema, BufferedReader entrada, PrintStream salida) {
        this.viajes = sistema.getControladorViajes();
        this.usuarios = sistema.getControladorUsuarios();
        this.mantenimiento = sistema.getControladorMantenimiento();
        this.operaciones = sistema.getControladorOperaciones();
        this.reportes = sistema.getControladorReportes();
        this.entrada = entrada;
        this.salida = salida;
    }

    /** Bucle del menú principal. Termina con la opción 0 o al agotarse la entrada. */
    public void iniciar() {
        salida.println("=== Sistema de Movilidad Compartida ===");
        try {
            boolean salir = false;
            while (!salir) {
                salida.println("\nMENÚ PRINCIPAL\n 1. Usuario\n 2. Operador\n 3. Técnico\n 4. Administrador\n 0. Salir");
                switch (leerEntero("Opción")) {
                    case 1: menuUsuario(); break;
                    case 2: menuOperador(); break;
                    case 3: menuTecnico(); break;
                    case 4: menuAdministrador(); break;
                    case 0: salir = true; break;
                    default: salida.println("Opción inválida.");
                }
            }
        } catch (FinDeEntradaException ex) {
            salida.println("\n(fin de la entrada)");
        }
        salida.println("Hasta luego.");
    }

    // ------------------------------------------------------------------ usuario
    private void menuUsuario() {
        salida.println("\nUSUARIO\n 1. Registrarme\n 2. Ingresar con mi documento\n 0. Volver");
        int opcion = leerEntero("Opción");
        if (opcion == 1) {
            ejecutar(() -> {
                Usuario u = usuarios.registrarUsuario(leer("Nombre completo"), leer("Documento"), leer("Correo electrónico"));
                salida.println("[OK] Usuario registrado: " + u + ". Su cuenta se creó con saldo $0.");
            });
        } else if (opcion == 2) {
            String documento = leer("Documento");
            try {
                usuarios.buscarUsuario(documento);
            } catch (MovilidadException ex) {
                salida.println("[ERROR] " + ex.getMessage());
                return;
            }
            boolean volver = false;
            while (!volver) {
                salida.println("\nUSUARIO " + documento + "\n 1. Ver saldo y movimientos\n 2. Asociar medio de pago\n 3. Recargar saldo"
                        + "\n 4. Suscribirme al plan mensual\n 5. Retirar vehículo\n 6. Registrar punto del recorrido"
                        + "\n 7. Devolver vehículo\n 8. Cancelar retiro\n 9. Mis viajes\n10. Reportar falla de un vehículo\n 0. Volver");
                switch (leerEntero("Opción")) {
                    case 1: ejecutar(() -> verCuenta(documento)); break;
                    case 2: ejecutar(() -> asociarMedio(documento)); break;
                    case 3: ejecutar(() -> recargar(documento)); break;
                    case 4: ejecutar(() -> {
                        usuarios.suscribirPlan(documento);
                        salida.println("[OK] Plan mensual activado (se debitó $" + usuarios.getPrecioPlan() + ").");
                    }); break;
                    case 5: ejecutar(() -> retirar(documento)); break;
                    case 6: ejecutar(() -> {
                        Viaje v = viajes.viajeEnCursoDe(documento);
                        viajes.registrarPunto(v.getId(), leerDecimal("Latitud").doubleValue(), leerDecimal("Longitud").doubleValue());
                        salida.println("[OK] Punto registrado.");
                    }); break;
                    case 7: ejecutar(() -> devolver(documento)); break;
                    case 8: ejecutar(() -> {
                        viajes.cancelarRetiro(viajes.viajeEnCursoDe(documento).getId());
                        salida.println("[OK] Retiro cancelado sin cargo.");
                    }); break;
                    case 9: ejecutar(() -> mostrarViajes(usuarios.consultarViajes(documento))); break;
                    case 10: ejecutar(() -> {
                        boolean obligatorio = mantenimiento.reportarFalla(leer("Código del vehículo"), leer("Descripción de la falla"), documento);
                        salida.println("[OK] Falla registrada." + (obligatorio ? " El vehículo pasó a mantenimiento obligatorio." : ""));
                    }); break;
                    case 0: volver = true; break;
                    default: salida.println("Opción inválida.");
                }
            }
        }
    }

    private void verCuenta(String documento) throws MovilidadException {
        Usuario u = usuarios.buscarUsuario(documento);
        salida.println("Saldo: $" + u.getCuenta().getSaldo() + (u.estaHabilitado() ? "" : "  (USUARIO INHABILITADO)"));
        for (MovimientoCuenta m : u.getCuenta().getMovimientos()) {
            salida.println("  " + m.getFecha().format(FORMATO) + "  " + m.getTipo() + "  $" + m.getImporte() + "  " + m.getDescripcion());
        }
    }

    private void asociarMedio(String documento) throws MovilidadException {
        salida.println(" 1. Tarjeta\n 2. Billetera virtual");
        MedioPago medio = leerEntero("Tipo") == 1 ? new Tarjeta(leer("Número de tarjeta (16 dígitos)"))
                : new BilleteraVirtual(leer("Alias de la billetera"));
        usuarios.asociarMedioPago(documento, medio);
        salida.println("[OK] Medio asociado: " + medio.descripcion());
    }

    private void recargar(String documento) throws MovilidadException {
        List<MedioPago> medios = usuarios.buscarUsuario(documento).getCuenta().getMedios();
        if (medios.isEmpty()) {
            salida.println("Primero asocie un medio de pago (opción 2).");
            return;
        }
        for (int i = 0; i < medios.size(); i++) {
            salida.println(" " + (i + 1) + ". " + medios.get(i).descripcion());
        }
        int indice = leerEntero("Medio de pago") - 1;
        if (indice < 0 || indice >= medios.size()) {
            salida.println("Medio inválido.");
            return;
        }
        usuarios.recargarSaldo(documento, leerDecimal("Importe a recargar"), medios.get(indice));
        salida.println("[OK] Recarga acreditada. Saldo: $" + usuarios.buscarUsuario(documento).getCuenta().getSaldo());
    }

    private void retirar(String documento) throws MovilidadException {
        mostrarEstaciones();
        Viaje v = viajes.retirarVehiculo(documento, leer("Código de estación"), leer("Código de vehículo"));
        salida.println("[OK] Viaje #" + v.getId() + " en curso con " + v.getVehiculo().getCodigo() + ". ¡Buen viaje!");
        mostrarAlertasNuevas();
    }

    private void devolver(String documento) throws MovilidadException {
        Viaje enCurso = viajes.viajeEnCursoDe(documento);
        mostrarEstaciones();
        String estacion = leer("Código de estación de devolución");
        double km = enCurso.getPuntos().size() >= 2 ? 0 : leerDecimal("Distancia recorrida en km (sin puntos de recorrido)").doubleValue();
        ResultadoTarifa r = viajes.devolverVehiculo(enCurso.getId(), estacion, km);
        salida.println("[OK] Viaje finalizado. Costo: $" + r.getCostoFinal()
                + (r.tuvoPromocion() ? " (promoción: " + r.getPromocionAplicada() + ", sin promoción: $" + r.getCostoBase() + ")" : ""));
        salida.println("  Saldo restante: $" + usuarios.buscarUsuario(documento).getCuenta().getSaldo());
        mostrarAlertasNuevas();
    }

    // ----------------------------------------------------------------- operador
    private void menuOperador() {
        String legajo = leer("Legajo del operador");
        boolean volver = false;
        while (!volver) {
            salida.println("\nOPERADOR " + legajo + "\n 1. Ocupación de estaciones y alertas\n 2. Redistribuir vehículos"
                    + "\n 3. Reportar falla\n 4. Generar orden de mantenimiento\n 5. Asignar técnico a una orden"
                    + "\n 6. Registrar nota de ajuste\n 7. Detectar vehículos perdidos\n 0. Volver");
            switch (leerEntero("Opción")) {
                case 1: mostrarEstaciones(); mostrarAlertasNuevas(); break;
                case 2: ejecutar(() -> {
                    mostrarEstaciones();
                    String origen = leer("Estación de origen"), destino = leer("Estación de destino");
                    List<String> cods = Arrays.asList(leer("Códigos de vehículos separados por coma").split("\\s*,\\s*"));
                    operaciones.redistribuir(origen, destino, new ArrayList<>(cods), legajo);
                    salida.println("[OK] Traslado registrado.");
                }); break;
                case 3: ejecutar(() -> {
                    boolean obligatorio = mantenimiento.reportarFalla(leer("Código del vehículo"), leer("Descripción de la falla"), legajo);
                    salida.println("[OK] Falla registrada." + (obligatorio ? " Mantenimiento obligatorio: genere la orden." : ""));
                }); break;
                case 4: ejecutar(() -> {
                    OrdenMantenimiento o = mantenimiento.generarOrden(leer("Código del vehículo"), leer("Descripción del trabajo"),
                            leer("Prioridad (NORMAL/ALTA)"), legajo);
                    salida.println("[OK] Orden #" + o.getId() + " generada. Falta asignar técnico.");
                }); break;
                case 5: ejecutar(() -> {
                    mantenimiento.asignarTecnico(leerEntero("Número de orden"), leer("Legajo del técnico"));
                    salida.println("[OK] Técnico asignado.");
                }); break;
                case 6: ejecutar(() -> {
                    operaciones.registrarNotaAjuste(leerEntero("Número de viaje"), leerDecimal("Importe (positivo acredita, negativo debita)"),
                            leer("Motivo"), legajo);
                    salida.println("[OK] Nota de ajuste registrada.");
                }); break;
                case 7: {
                    List<Viaje> perdidos = operaciones.detectarPerdidos();
                    salida.println(perdidos.isEmpty() ? "No hay viajes perdidos." : perdidos.size() + " viaje(s) marcados como PERDIDO.");
                    break;
                }
                case 0: volver = true; break;
                default: salida.println("Opción inválida.");
            }
        }
    }

    // ------------------------------------------------------------------ técnico
    private void menuTecnico() {
        String legajo = leer("Legajo del técnico");
        boolean volver = false;
        while (!volver) {
            salida.println("\nTÉCNICO " + legajo + "\n 1. Mis órdenes\n 2. Registrar tarea\n 3. Registrar repuesto\n 4. Cerrar orden\n 0. Volver");
            switch (leerEntero("Opción")) {
                case 1: ejecutar(() -> {
                    List<OrdenMantenimiento> ordenes = mantenimiento.consultarOrdenes(legajo);
                    if (ordenes.isEmpty()) salida.println("No tiene órdenes asignadas.");
                    for (OrdenMantenimiento o : ordenes) {
                        salida.println("  #" + o.getId() + " " + o.getVehiculo().getCodigo() + " [" + o.getEstado() + "] "
                                + o.getPrioridad() + " - " + o.getDescripcion());
                    }
                }); break;
                case 2: ejecutar(() -> {
                    mantenimiento.registrarTarea(leerEntero("Número de orden"), legajo, leer("Tarea realizada"));
                    salida.println("[OK] Tarea registrada.");
                }); break;
                case 3: ejecutar(() -> {
                    mantenimiento.registrarRepuesto(leerEntero("Número de orden"), legajo, leer("Repuesto"), leerEntero("Cantidad"));
                    salida.println("[OK] Repuesto registrado.");
                }); break;
                case 4: ejecutar(() -> {
                    mantenimiento.cerrarOrden(leerEntero("Número de orden"), legajo, leer("Estación donde queda el vehículo"));
                    salida.println("[OK] Orden cerrada: el vehículo quedó DISPONIBLE.");
                }); break;
                case 0: volver = true; break;
                default: salida.println("Opción inválida.");
            }
        }
    }

    // ------------------------------------------------------------ administrador
    private void menuAdministrador() {
        boolean volver = false;
        while (!volver) {
            salida.println("\nADMINISTRADOR\n 1. Viajes por usuario\n 2. Uso por estación\n 3. Vehículos con más fallas\n 0. Volver");
            switch (leerEntero("Opción")) {
                case 1: ejecutar(() -> {
                    String doc = leer("Documento del usuario");
                    LocalDate desde = leerFechaOpcional("Desde (dd/MM/yyyy, vacío = sin límite)");
                    LocalDate hasta = leerFechaOpcional("Hasta (dd/MM/yyyy, vacío = sin límite)");
                    mostrarViajes(reportes.viajesPorUsuario(doc, desde, hasta));
                }); break;
                case 2: {
                    for (UsoEstacion u : reportes.usoPorEstacion()) {
                        salida.println("  " + u.getCodigoEstacion() + ": " + u.getRetiros() + " retiros, " + u.getDevoluciones() + " devoluciones");
                    }
                    break;
                }
                case 3: {
                    List<Vehiculo> ranking = reportes.vehiculosConMasFallas();
                    if (ranking.isEmpty()) salida.println("Ningún vehículo tiene fallas reportadas.");
                    for (Vehiculo v : ranking) {
                        salida.println("  " + v.getCodigo() + ": " + v.cantidadFallas() + " reporte(s)");
                    }
                    break;
                }
                case 0: volver = true; break;
                default: salida.println("Opción inválida.");
            }
        }
    }

    // --------------------------------------------------------------- utilidades
    private void mostrarEstaciones() {
        for (Estacion e : operaciones.consultarOcupacion()) {
            salida.println(" " + e + "  [" + e.cantidadVehiculos() + "/" + e.getCapacidadMaxima() + "]"
                    + (e.requiereRedistribucion() ? "  [!] requiere redistribución" : ""));
            for (Vehiculo v : e.getVehiculos()) {
                salida.println("     - " + v);
            }
        }
    }

    private void mostrarAlertasNuevas() {
        List<AlertaEstacion> alertas = operaciones.consultarAlertas();
        if (!alertas.isEmpty()) {
            salida.println("  [!] " + alertas.get(alertas.size() - 1));
        }
    }

    private void mostrarViajes(List<Viaje> lista) {
        if (lista.isEmpty()) {
            salida.println("No hay viajes registrados.");
        }
        for (Viaje v : lista) {
            salida.println("  #" + v.getId() + " " + v.getInicio().format(FORMATO) + " " + v.getVehiculo().getCodigo()
                    + " " + v.getEstado() + " $" + v.getCosto()
                    + (v.getPromocionAplicada() != null ? " (" + v.getPromocionAplicada() + ")" : ""));
        }
    }

    /** Ejecuta una acción mostrando como mensaje cualquier error de regla de negocio. */
    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (MovilidadException ex) {
            salida.println("[ERROR] " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            salida.println("[ERROR] Dato inválido: " + ex.getMessage());
        }
    }

    private String leer(String etiqueta) {
        salida.print(etiqueta + ": ");
        salida.flush();
        try {
            String linea = entrada.readLine();
            if (linea == null) {
                throw new FinDeEntradaException();
            }
            return linea.trim();
        } catch (IOException ex) {
            throw new FinDeEntradaException();
        }
    }

    private int leerEntero(String etiqueta) {
        while (true) {
            try {
                return Integer.parseInt(leer(etiqueta));
            } catch (NumberFormatException ex) {
                salida.println("Ingrese un número entero.");
            }
        }
    }

    private BigDecimal leerDecimal(String etiqueta) {
        while (true) {
            try {
                return new BigDecimal(leer(etiqueta).replace(',', '.'));
            } catch (NumberFormatException ex) {
                salida.println("Ingrese un número (ej. 1500 o 12.5).");
            }
        }
    }

    private LocalDate leerFechaOpcional(String etiqueta) {
        while (true) {
            String texto = leer(etiqueta);
            if (texto.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(texto, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException ex) {
                salida.println("Formato de fecha inválido.");
            }
        }
    }

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws MovilidadException;
    }

    /** Se lanza cuando la entrada estándar se cierra (permite terminar limpio en scripts/pruebas). */
    private static class FinDeEntradaException extends RuntimeException { }
}
