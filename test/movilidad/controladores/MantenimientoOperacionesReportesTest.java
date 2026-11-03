package movilidad.controladores;

import movilidad.Escenario;
import movilidad.dominio.Viaje;
import movilidad.dominio.mantenimiento.NotaAjuste;
import movilidad.dominio.mantenimiento.OrdenMantenimiento;
import movilidad.dominio.mantenimiento.EstadoOrden;
import movilidad.dominio.vehiculos.EstadoVehiculo;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.excepciones.*;
import movilidad.servicios.UsoEstacion;
import movilidad.util.Dinero;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MantenimientoOperacionesReportesTest {
    private final Escenario esc = new Escenario();

    // ------------------------------------------------------------ mantenimiento
    @Test
    void elTercerReporteDeFallaActivaElMantenimientoObligatorio() throws Exception {
        assertFalse(esc.mantenimiento.reportarFalla("B-001", "freno", "1"));
        assertFalse(esc.mantenimiento.reportarFalla("B-001", "cadena", "OP-01"));
        assertTrue(esc.mantenimiento.reportarFalla("B-001", "luz", "1"));
        assertEquals(EstadoVehiculo.EN_REPARACION, esc.b001.getEstado());
    }

    @Test
    void reportarFallaSinDescripcionOVehiculoInexistenteFalla() {
        assertThrows(DatosInvalidosException.class, () -> esc.mantenimiento.reportarFalla("B-001", " ", "1"));
        assertThrows(EntidadNoEncontradaException.class, () -> esc.mantenimiento.reportarFalla("ZZZ", "x", "1"));
    }

    @Test
    void cicloCompletoDeUnaOrdenDeMantenimiento() throws Exception {
        esc.eb001.setNivelBateria(40);
        OrdenMantenimiento orden = esc.mantenimiento.generarOrden("E-001", "No enciende", "ALTA", "OP-01");
        assertFalse(esc.e01.contiene(esc.eb001));                          // sale de la estación (S18)
        assertEquals(EstadoVehiculo.EN_REPARACION, esc.eb001.getEstado());

        esc.mantenimiento.asignarTecnico(orden.getId(), "TEC-01");
        assertEquals(1, esc.mantenimiento.consultarOrdenes("TEC-01").size());

        // RN06: sin tareas registradas no se puede cerrar
        assertThrows(OrdenIncompletaException.class, () -> esc.mantenimiento.cerrarOrden(orden.getId(), "TEC-01", "E01"));
        assertEquals(EstadoOrden.ABIERTA, orden.getEstado());

        esc.mantenimiento.registrarTarea(orden.getId(), "TEC-01", "Reemplazo de controlador");
        esc.mantenimiento.registrarRepuesto(orden.getId(), "TEC-01", "Controlador 36V", 1);
        esc.mantenimiento.cerrarOrden(orden.getId(), "TEC-01", "E01");

        assertEquals(EstadoOrden.CERRADA, orden.getEstado());
        assertNotNull(orden.getFechaCierre());
        assertTrue(esc.e01.contiene(esc.eb001));
        assertEquals(EstadoVehiculo.DISPONIBLE, esc.eb001.getEstado());
        assertEquals(100.0, esc.eb001.getNivelBateria(), 0.001);            // batería repuesta
    }

    @Test
    void unaOrdenSinTecnicoAsignadoNoSePuedeCerrar() throws Exception {
        OrdenMantenimiento orden = esc.mantenimiento.generarOrden("B-001", "Freno", "NORMAL", "OP-01");
        assertThrows(OperacionNoPermitidaException.class, () -> esc.mantenimiento.registrarTarea(orden.getId(), "TEC-01", "x"));
    }

    @Test
    void unVehiculoTieneComoMaximoUnaOrdenAbiertaYNoSeMantieneEnUso() throws Exception {
        esc.mantenimiento.generarOrden("B-001", "Freno", "NORMAL", "OP-01");
        assertThrows(OperacionNoPermitidaException.class, () -> esc.mantenimiento.generarOrden("B-001", "Otra", "NORMAL", "OP-01"));
        esc.viajes.retirarVehiculo("1", "E01", "E-001");
        assertThrows(OperacionNoPermitidaException.class, () -> esc.mantenimiento.generarOrden("E-001", "x", "NORMAL", "OP-01"));
    }

    @Test
    void soloUnOperadorPuedeGenerarOrdenes() {
        assertThrows(EntidadNoEncontradaException.class, () -> esc.mantenimiento.generarOrden("B-001", "x", "NORMAL", "TEC-01"));
    }

    // ------------------------------------------------------------ operaciones
    @Test
    void redistribuirMueveVehiculosRepoeLaBateriaYRegistraLaTransferencia() throws Exception {
        esc.m001.setNivelBateria(5);                                        // BATERIA_BAJA
        esc.operaciones.redistribuir("E01", "E03", Arrays.asList("M-001", "B-001"), "OP-01");
        assertTrue(esc.e03.contiene(esc.m001));
        assertTrue(esc.e03.contiene(esc.b001));
        assertFalse(esc.e01.contiene(esc.m001));
        assertEquals(EstadoVehiculo.DISPONIBLE, esc.m001.getEstado());
        assertEquals(100.0, esc.m001.getNivelBateria(), 0.001);
        assertEquals(1, esc.repo.buscarTransferencias().size());
    }

    @Test
    void sinCapacidadEnDestinoNoSeMueveNingunVehiculo() {
        assertThrows(EstacionLlenaException.class, () -> esc.operaciones.redistribuir("E01", "E02", List.of("B-001"), "OP-01"));
        assertTrue(esc.e01.contiene(esc.b001));
        assertTrue(esc.repo.buscarTransferencias().isEmpty());
    }

    @Test
    void noSeTrasladanVehiculosEnUsoNiEnReparacion() throws Exception {
        esc.b001.cambiarEstado(EstadoVehiculo.EN_REPARACION);
        assertThrows(OperacionNoPermitidaException.class, () -> esc.operaciones.redistribuir("E01", "E03", List.of("B-001"), "OP-01"));
        assertThrows(OperacionNoPermitidaException.class, () -> esc.operaciones.redistribuir("E01", "E01", List.of("E-001"), "OP-01"));
    }

    @Test
    void notaDeAjusteGeneraMovimientoSinModificarElViajeYRehabilitaAlUsuario() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.viajes.devolverVehiculo(v.getId(), "E01");
        esc.ana.getCuenta().debitar(Dinero.de("5000"), "ajuste de prueba", Escenario.HOY);
        esc.ana.inhabilitar();                                              // saldo negativo
        var costoOriginal = v.getCosto();
        NotaAjuste nota = esc.operaciones.registrarNotaAjuste(v.getId(), Dinero.de("2000"), "Error de cobro", "OP-01");
        assertEquals(costoOriginal, v.getCosto());                          // el viaje cerrado no se modifica (RN15)
        assertEquals(Dinero.de("2000"), nota.getImporte());
        assertEquals(1, esc.repo.buscarNotasAjuste().size());
        assertTrue(esc.ana.getCuenta().getMovimientos().stream().anyMatch(m -> m.getImporte().compareTo(Dinero.de("2000")) == 0));
    }

    @Test
    void notaDeAjusteSinMotivoOImporteCeroSeRechaza() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        assertThrows(DatosInvalidosException.class, () -> esc.operaciones.registrarNotaAjuste(v.getId(), Dinero.de("0"), "x", "OP-01"));
        assertThrows(DatosInvalidosException.class, () -> esc.operaciones.registrarNotaAjuste(v.getId(), Dinero.de("5"), " ", "OP-01"));
    }

    // ------------------------------------------------------------ reportes
    @Test
    void viajesPorUsuarioSoloFinalizadosOrdenadosPorFechaYFiltrablesPorRango() throws Exception {
        Viaje a = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.viajes.devolverVehiculo(a.getId(), "E01");
        esc.reloj.avanzarHoras(48);
        Viaje b = esc.viajes.retirarVehiculo("1", "E01", "E-001");
        esc.viajes.devolverVehiculo(b.getId(), "E01");
        esc.viajes.retirarVehiculo("1", "E01", "M-001");                    // en curso: no aparece
        List<Viaje> todos = esc.reportes.viajesPorUsuario("1", null, null);
        assertEquals(2, todos.size());
        assertSame(a, todos.get(0));
        List<Viaje> soloUltimo = esc.reportes.viajesPorUsuario("1", Escenario.HOY.toLocalDate().plusDays(1), null);
        assertEquals(1, soloUltimo.size());
        assertSame(b, soloUltimo.get(0));
    }

    @Test
    void usoPorEstacionCuentaRetirosYDevoluciones() throws Exception {
        Viaje a = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.viajes.devolverVehiculo(a.getId(), "E03");
        UsoEstacion e01 = esc.reportes.usoPorEstacion().stream().filter(u -> u.getCodigoEstacion().equals("E01")).findFirst().orElseThrow();
        UsoEstacion e03 = esc.reportes.usoPorEstacion().stream().filter(u -> u.getCodigoEstacion().equals("E03")).findFirst().orElseThrow();
        assertEquals(1, e01.getRetiros());
        assertEquals(0, e01.getDevoluciones());
        assertEquals(1, e03.getDevoluciones());
    }

    @Test
    void vehiculosConMasFallasSeOrdenanDeMayorAMenorConElComparator() throws Exception {
        esc.mantenimiento.reportarFalla("E-001", "a", "1");
        esc.mantenimiento.reportarFalla("B-001", "a", "1");
        esc.mantenimiento.reportarFalla("B-001", "b", "1");
        List<Vehiculo> ranking = esc.reportes.vehiculosConMasFallas();
        assertEquals(2, ranking.size());                                    // los que no tienen fallas no aparecen
        assertSame(esc.b001, ranking.get(0));
        assertSame(esc.eb001, ranking.get(1));
    }
}
