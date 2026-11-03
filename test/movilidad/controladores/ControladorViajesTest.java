package movilidad.controladores;

import movilidad.Escenario;
import movilidad.dominio.EstadoViaje;
import movilidad.dominio.TipoMovimiento;
import movilidad.dominio.Viaje;
import movilidad.dominio.mantenimiento.TipoAlerta;
import movilidad.dominio.tarifas.ResultadoTarifa;
import movilidad.dominio.vehiculos.EstadoVehiculo;
import movilidad.excepciones.*;
import movilidad.util.Dinero;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de los casos de uso modelados en los diagramas de secuencia (CU-05 y CU-08), incluidas las ramas alt/opt. */
class ControladorViajesTest {
    private final Escenario esc = new Escenario();

    // ------------------------------------------------------------ CU-05 Retirar
    @Test
    void retiroExitosoCreaViajeEnCursoYSacaElVehiculoDeLaEstacion() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        assertEquals(EstadoViaje.EN_CURSO, v.getEstado());
        assertEquals(EstadoVehiculo.EN_USO, esc.b001.getEstado());
        assertFalse(esc.e01.contiene(esc.b001));
        assertEquals(Escenario.HOY, v.getInicio());
        assertTrue(esc.repo.buscarPorId(v.getId()).isPresent());
    }

    @Test
    void usuarioInhabilitadoNoPuedeRetirar() {
        esc.ana.inhabilitar();
        assertThrows(UsuarioInhabilitadoException.class, () -> esc.viajes.retirarVehiculo("1", "E01", "B-001"));
        assertTrue(esc.e01.contiene(esc.b001));
    }

    @Test
    void unUsuarioConViajeAbiertoNoPuedeRetirarOtroVehiculo() throws Exception {
        esc.viajes.retirarVehiculo("1", "E01", "B-001");
        assertThrows(ViajeAbiertoException.class, () -> esc.viajes.retirarVehiculo("1", "E01", "E-001"));
    }

    @Test
    void saldoMenorAlMinimoDelVehiculoImpideElRetiro() throws Exception {
        esc.ana.getCuenta().debitar(Dinero.de("4300"), "gasto", Escenario.HOY);   // saldo $700 < $800 (bici común)
        assertThrows(SaldoInsuficienteException.class, () -> esc.viajes.retirarVehiculo("1", "E01", "B-001"));
    }

    @Test
    void elSaldoMinimoDependeDelTipoDeVehiculo() throws Exception {
        esc.ana.getCuenta().debitar(Dinero.de("3300"), "gasto", Escenario.HOY);   // saldo $1700: alcanza bici eléctrica ($1650)...
        esc.viajes.retirarVehiculo("1", "E01", "E-001");
        esc.viajes.cancelarRetiro(esc.repo.buscarViajes().get(0).getId());
        assertThrows(SaldoInsuficienteException.class, () -> esc.viajes.retirarVehiculo("1", "E01", "M-001")); // ...pero no el monopatín ($1800)
    }

    @Test
    void vehiculoEnReparacionNoPuedeRetirarse() {
        esc.b001.cambiarEstado(EstadoVehiculo.EN_REPARACION);
        assertThrows(VehiculoNoDisponibleException.class, () -> esc.viajes.retirarVehiculo("1", "E01", "B-001"));
    }

    @Test
    void electricoConBateriaBajaNoSeOfrece() {
        esc.eb001.setNivelBateria(10);
        assertThrows(VehiculoNoDisponibleException.class, () -> esc.viajes.retirarVehiculo("1", "E01", "E-001"));
    }

    @Test
    void vehiculoQueNoEstaEnLaEstacionNoPuedeRetirarse() {
        assertThrows(VehiculoNoDisponibleException.class, () -> esc.viajes.retirarVehiculo("1", "E01", "B-002"));
    }

    @Test
    void usuarioOEstacionInexistenteLanzanEntidadNoEncontrada() {
        assertThrows(EntidadNoEncontradaException.class, () -> esc.viajes.retirarVehiculo("999", "E01", "B-001"));
        assertThrows(EntidadNoEncontradaException.class, () -> esc.viajes.retirarVehiculo("1", "E99", "B-001"));
    }

    @Test
    void siLaEstacionQuedaVaciaSeGeneraLaAlertaDeRedistribucion() throws Exception {
        esc.viajes.retirarVehiculo("1", "E03", "B-009");   // E03 queda en 0/5
        assertEquals(1, esc.repo.buscarAlertas().size());
        assertEquals(TipoAlerta.VACIA, esc.repo.buscarAlertas().get(0).getTipo());
    }

    // ------------------------------------------------------------ CU-08 Devolver
    @Test
    void devolucionCobraLaTarifaYDejaElVehiculoDisponibleEnLaEstacionDestino() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.reloj.avanzarMinutos(30);
        ResultadoTarifa r = esc.viajes.devolverVehiculo(v.getId(), "E03", 2.5);
        assertEquals(Dinero.de("800"), r.getCostoFinal());                          // 200 + 20 * 30
        assertEquals(Dinero.de("4200"), esc.ana.getCuenta().getSaldo());
        assertEquals(EstadoViaje.FINALIZADO, v.getEstado());
        assertEquals(EstadoVehiculo.DISPONIBLE, esc.b001.getEstado());
        assertTrue(esc.e03.contiene(esc.b001));
        assertEquals(2.5, esc.b001.getKilometrosRecorridos(), 0.001);
        assertEquals(2, esc.ana.getViajesFinalizados());
        assertSame(esc.e03, v.getEstacionDevolucion());
    }

    @Test
    void siElDebitoDejaElSaldoNegativoElUsuarioQuedaInhabilitado() throws Exception {
        esc.ana.getCuenta().debitar(Dinero.de("4200"), "gasto", Escenario.HOY);   // saldo $800: justo el mínimo
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.reloj.avanzarMinutos(60);                                              // cuesta $1400
        esc.viajes.devolverVehiculo(v.getId(), "E01");
        assertEquals(Dinero.de("-600"), esc.ana.getCuenta().getSaldo());
        assertFalse(esc.ana.estaHabilitado());
        assertThrows(UsuarioInhabilitadoException.class, () -> esc.viajes.retirarVehiculo("1", "E01", "B-001"));
    }

    @Test
    void sobreLaMarcaDeDosHorasLosMinutosExcedentesLlevanRecargoDel50() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.reloj.avanzarMinutos(130);
        ResultadoTarifa r = esc.viajes.devolverVehiculo(v.getId(), "E01");
        assertEquals(Dinero.de("2900"), r.getCostoFinal());
    }

    @Test
    void devolverEnEstacionLlenaLanzaExcepcionYRegistraElInstanteDeLaGracia() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.reloj.avanzarMinutos(40);
        assertThrows(EstacionLlenaException.class, () -> esc.viajes.devolverVehiculo(v.getId(), "E02"));
        assertEquals(EstadoViaje.EN_CURSO, v.getEstado());
        assertEquals(Escenario.HOY.plusMinutes(40), v.getInstanteEstacionLlena());
        assertEquals(EstadoVehiculo.EN_USO, esc.b001.getEstado());
    }

    @Test
    void trasEstacionLlenaLosMinutosDeGraciaSeDescuentanDelTiempoFacturable() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.reloj.avanzarMinutos(40);
        assertThrows(EstacionLlenaException.class, () -> esc.viajes.devolverVehiculo(v.getId(), "E02"));
        esc.reloj.avanzarMinutos(5);                                               // llega a E03 a los 45 min
        ResultadoTarifa r = esc.viajes.devolverVehiculo(v.getId(), "E03");
        assertEquals(Dinero.de("1000"), r.getCostoFinal());                        // 200 + 20 * (45 - 5)
    }

    @Test
    void elPrimerViajeGratisNoGeneraMovimientoDeDebito() throws Exception {
        var nuevo = esc.usuarios.registrarUsuario("Nuevo", "50", "n@mail.com");
        nuevo.getCuenta().recargar(Dinero.de("1000"), Escenario.HOY);
        Viaje v = esc.viajes.retirarVehiculo("50", "E01", "B-001");
        esc.reloj.avanzarMinutos(10);
        ResultadoTarifa r = esc.viajes.devolverVehiculo(v.getId(), "E01");
        assertEquals("Primer viaje gratis", r.getPromocionAplicada());
        assertEquals("Primer viaje gratis", v.getPromocionAplicada());
        assertEquals(Dinero.de("1000"), nuevo.getCuenta().getSaldo());
        assertTrue(nuevo.getCuenta().getMovimientos().stream().noneMatch(m -> m.getTipo() == TipoMovimiento.DEBITO));
    }

    @Test
    void devolverUnViajeInexistenteOYaFinalizadoEsUnError() throws Exception {
        assertThrows(EntidadNoEncontradaException.class, () -> esc.viajes.devolverVehiculo(77, "E01"));
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.viajes.devolverVehiculo(v.getId(), "E01");
        assertThrows(EntidadNoEncontradaException.class, () -> esc.viajes.devolverVehiculo(v.getId(), "E01"));
    }

    @Test
    void conPuntosDeRecorridoLaDistanciaSaleDeLosPuntos() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "E-001");
        esc.viajes.registrarPunto(v.getId(), -34.9205, -57.9536);
        esc.reloj.avanzarMinutos(5);
        esc.viajes.registrarPunto(v.getId(), -34.9305, -57.9536);
        esc.reloj.avanzarMinutos(5);
        esc.viajes.devolverVehiculo(v.getId(), "E01");
        assertEquals(1.11, v.getDistanciaRecorrida(), 0.05);
        assertEquals(100 - 2 * v.getDistanciaRecorrida(), esc.eb001.getNivelBateria(), 0.001);
    }

    // ------------------------------------------------------------ Cancelación (RN11)
    @Test
    void cancelarDentroDeLosDosMinutosDevuelveElVehiculoYNoCobra() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.reloj.avanzarMinutos(2);
        esc.viajes.cancelarRetiro(v.getId());
        assertEquals(EstadoViaje.CANCELADO, v.getEstado());
        assertEquals(Dinero.de("5000"), esc.ana.getCuenta().getSaldo());
        assertTrue(esc.e01.contiene(esc.b001));
        assertEquals(EstadoVehiculo.DISPONIBLE, esc.b001.getEstado());
        assertEquals(1, esc.ana.getViajesFinalizados());   // un viaje cancelado no cuenta como realizado
    }

    @Test
    void pasadosLosDosMinutosYaNoSePuedeCancelar() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.reloj.avanzarMinutos(3);
        assertThrows(RetiroNoCancelableException.class, () -> esc.viajes.cancelarRetiro(v.getId()));
        assertEquals(EstadoViaje.EN_CURSO, v.getEstado());
    }
}
