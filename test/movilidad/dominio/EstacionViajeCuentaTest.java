package movilidad.dominio;

import movilidad.Escenario;
import movilidad.dominio.mantenimiento.TipoAlerta;
import movilidad.dominio.vehiculos.BicicletaComun;
import movilidad.util.Dinero;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EstacionViajeCuentaTest {
    private final Escenario esc = new Escenario();
    private static final LocalDateTime T0 = Escenario.HOY;

    // ---------------- Estacion ----------------
    @Test
    void estacionLlenaNoAceptaMasVehiculos() {
        assertTrue(esc.e02.estaLlena());
        assertThrows(IllegalStateException.class, () -> esc.e02.ingresar(new BicicletaComun("X", LocalDate.now(), 26)));
    }

    @Test
    void ocupacionEsVehiculosSobreCapacidad() {
        assertEquals(0.3, esc.e01.ocupacion(), 0.0001);
        assertFalse(esc.e01.requiereRedistribucion());
    }

    @Test
    void ocupacionMenorAl20PorCientoGeneraAlertaVacia() {
        esc.e03.egresar(esc.b009);   // 0 / 5
        assertTrue(esc.e03.requiereRedistribucion());
        assertEquals(TipoAlerta.VACIA, esc.e03.evaluarAlerta(T0).orElseThrow().getTipo());
    }

    @Test
    void ocupacionMayorAl90PorCientoGeneraAlertaSaturada() {
        assertEquals(TipoAlerta.SATURADA, esc.e02.evaluarAlerta(T0).orElseThrow().getTipo());   // 2/2 = 100 %
    }

    @Test
    void ocupacionJustoEnElLimiteNoGeneraAlerta() {
        esc.e03.ingresar(new BicicletaComun("X", LocalDate.now(), 26));   // 2/5 = 40 %
        assertTrue(esc.e03.evaluarAlerta(T0).isEmpty());
    }

    // ---------------- Viaje ----------------
    private Viaje nuevoViaje() throws Exception {
        return new Viaje(1, esc.ana, esc.b001, esc.e01,
                esc.tarifas.tarifaVigente(esc.b001.getTipo(), T0.toLocalDate()), T0);
    }

    @Test
    void laDuracionSeRedondeaHaciaArriba() throws Exception {
        Viaje v = nuevoViaje();
        v.finalizar(esc.e01, T0.plusMinutes(10).plusSeconds(1), 0);
        assertEquals(11, v.duracionMinutos());
    }

    @Test
    void laDuracionExactaNoSeRedondea() throws Exception {
        Viaje v = nuevoViaje();
        v.finalizar(esc.e01, T0.plusMinutes(10), 0);
        assertEquals(10, v.duracionMinutos());
    }

    @Test
    void duracionDeUnViajeSinFinalizarEsUnError() throws Exception {
        assertThrows(IllegalStateException.class, () -> nuevoViaje().duracionMinutos());
    }

    @Test
    void laDistanciaEsLaSumaDeTramosEntreLosPuntos() throws Exception {
        Viaje v = nuevoViaje();
        v.agregarPunto(new PuntoRecorrido(-34.9205, -57.9536, T0));
        v.agregarPunto(new PuntoRecorrido(-34.9305, -57.9536, T0.plusMinutes(5)));   // ~1,11 km al sur
        v.agregarPunto(new PuntoRecorrido(-34.9405, -57.9536, T0.plusMinutes(10)));  // ~1,11 km más
        v.finalizar(esc.e01, T0.plusMinutes(10), 99);   // con puntos se ignora la distancia informada
        assertEquals(2.22, v.getDistanciaRecorrida(), 0.05);
    }

    @Test
    void sinPuntosSeUsaLaDistanciaInformada() throws Exception {
        Viaje v = nuevoViaje();
        v.finalizar(esc.e01, T0.plusMinutes(10), 3.5);
        assertEquals(3.5, v.getDistanciaRecorrida(), 0.0001);
    }

    @Test
    void laGraciaPorEstacionLlenaDescuentaHasta10MinutosDesdeElPrimerIntento() throws Exception {
        Viaje v = nuevoViaje();
        v.registrarEstacionLlena(T0.plusMinutes(40));
        v.registrarEstacionLlena(T0.plusMinutes(42));   // solo cuenta el primer intento
        v.finalizar(esc.e01, T0.plusMinutes(45), 0);
        assertEquals(40, v.minutosFacturables());       // 45 - min(10, 5)
        Viaje w = nuevoViaje();
        w.registrarEstacionLlena(T0.plusMinutes(20));
        w.finalizar(esc.e01, T0.plusMinutes(45), 0);
        assertEquals(35, w.minutosFacturables());       // 45 - 10 (pasado el plazo se vuelve a cobrar)
    }

    @Test
    void losViajesSeOrdenanNaturalmentePorFechaDeInicio() throws Exception {
        Viaje tarde = new Viaje(2, esc.ana, esc.b001, esc.e01, nuevoViaje().getTarifa(), T0.plusHours(2));
        Viaje temprano = nuevoViaje();
        List<Viaje> lista = new ArrayList<>(List.of(tarde, temprano));
        Collections.sort(lista);
        assertSame(temprano, lista.get(0));
    }

    @Test
    void cancelarDejaCostoCeroYEstadoCancelado() throws Exception {
        Viaje v = nuevoViaje();
        v.cancelar();
        assertEquals(EstadoViaje.CANCELADO, v.getEstado());
        assertEquals(Dinero.CERO, v.getCosto());
    }

    // ---------------- CuentaUsuario / Usuario ----------------
    @Test
    void recargarYDebitarActualizanSaldoYMovimientos() {
        CuentaUsuario c = new CuentaUsuario();
        c.recargar(Dinero.de("1000"), T0);
        c.debitar(Dinero.de("300.50"), "viaje", T0);
        assertEquals(Dinero.de("699.50"), c.getSaldo());
        assertEquals(2, c.getMovimientos().size());
        assertEquals(TipoMovimiento.DEBITO, c.getMovimientos().get(1).getTipo());
    }

    @Test
    void unDebitoPuedeDejarElSaldoNegativo() {
        CuentaUsuario c = new CuentaUsuario();
        c.debitar(Dinero.de("100"), "viaje", T0);
        assertEquals(Dinero.de("-100"), c.getSaldo());
    }

    @Test
    void losImportesNoPositivosSeRechazan() {
        CuentaUsuario c = new CuentaUsuario();
        assertThrows(IllegalArgumentException.class, () -> c.recargar(Dinero.de("0"), T0));
        assertThrows(IllegalArgumentException.class, () -> c.debitar(Dinero.de("-5"), "x", T0));
    }

    @Test
    void elAjusteConSignoRegistraMovimientoDeTipoAjuste() {
        CuentaUsuario c = new CuentaUsuario();
        c.aplicarAjuste(Dinero.de("-250"), "corrección", T0);
        assertEquals(TipoMovimiento.AJUSTE, c.getMovimientos().get(0).getTipo());
        assertEquals(Dinero.de("-250"), c.getSaldo());
    }

    @Test
    void usuarioInhabilitadoSeRehabilitaCuandoElSaldoVuelveASerNoNegativo() {
        Usuario u = new Usuario("U", "9", "u@mail.com", LocalDate.now());
        u.getCuenta().debitar(Dinero.de("100"), "viaje", T0);
        u.inhabilitar();
        u.reevaluarHabilitacion();
        assertFalse(u.estaHabilitado());
        u.getCuenta().recargar(Dinero.de("100"), T0);   // saldo 0: ya alcanza (S17: >= 0)
        u.reevaluarHabilitacion();
        assertTrue(u.estaHabilitado());
    }
}
