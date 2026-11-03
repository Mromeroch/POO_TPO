package movilidad.controladores;

import movilidad.Escenario;
import movilidad.dominio.PlanMensual;
import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;
import movilidad.dominio.pagos.BilleteraVirtual;
import movilidad.dominio.pagos.MedioPago;
import movilidad.dominio.pagos.Tarjeta;
import movilidad.excepciones.*;
import movilidad.util.Dinero;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ControladorUsuariosTest {
    private final Escenario esc = new Escenario();

    @Test
    void registrarCreaElUsuarioConCuentaEnCero() throws Exception {
        Usuario u = esc.usuarios.registrarUsuario("Juan Pérez", "30111222", "juan@mail.com");
        assertEquals(Dinero.CERO, u.getCuenta().getSaldo());
        assertTrue(u.estaHabilitado());
        assertTrue(esc.repo.existe("30111222"));
    }

    @Test
    void noSePuedeRegistrarUnDocumentoRepetido() {
        assertThrows(DatosInvalidosException.class, () -> esc.usuarios.registrarUsuario("Otra", "1", "o@mail.com"));
    }

    @Test
    void faltanDatosObligatorios() {
        assertThrows(DatosInvalidosException.class, () -> esc.usuarios.registrarUsuario("", "5", "x@mail.com"));
        assertThrows(DatosInvalidosException.class, () -> esc.usuarios.registrarUsuario("X", " ", "x@mail.com"));
        assertThrows(DatosInvalidosException.class, () -> esc.usuarios.registrarUsuario("X", "5", null));
    }

    @Test
    void tarjetaInvalidaNoSePuedeAsociar() {
        assertThrows(DatosInvalidosException.class, () -> esc.usuarios.asociarMedioPago("1", new Tarjeta("1234")));
        assertThrows(DatosInvalidosException.class, () -> esc.usuarios.asociarMedioPago("1", new BilleteraVirtual("ab")));
        assertTrue(esc.ana.getCuenta().getMedios().isEmpty());
    }

    @Test
    void tarjetaValidaSeGuardaEnmascarada() throws Exception {
        Tarjeta t = new Tarjeta("4111 1111 1111 1234");
        esc.usuarios.asociarMedioPago("1", t);
        assertEquals("**** **** **** 1234", t.getNumeroEnmascarado());
        assertEquals(1, esc.ana.getCuenta().getMedios().size());
    }

    @Test
    void recargaConMedioAsociadoAcreditaYRegistraElMovimiento() throws Exception {
        MedioPago billetera = new BilleteraVirtual("ana.mp");
        esc.usuarios.asociarMedioPago("1", billetera);
        esc.usuarios.recargarSaldo("1", Dinero.de("1500"), billetera);
        assertEquals(Dinero.de("6500"), esc.ana.getCuenta().getSaldo());
    }

    @Test
    void recargaConMedioNoAsociadoOImporteInvalidoSeRechaza() throws Exception {
        MedioPago ajeno = new BilleteraVirtual("otro");
        assertThrows(DatosInvalidosException.class, () -> esc.usuarios.recargarSaldo("1", Dinero.de("100"), ajeno));
        MedioPago propio = new BilleteraVirtual("ana.mp");
        esc.usuarios.asociarMedioPago("1", propio);
        assertThrows(DatosInvalidosException.class, () -> esc.usuarios.recargarSaldo("1", Dinero.de("0"), propio));
        assertEquals(Dinero.de("5000"), esc.ana.getCuenta().getSaldo());   // el saldo no cambió
    }

    @Test
    void unaRecargaQueRegularizaLaDeudaRehabilitaAlUsuario() throws Exception {
        esc.ana.getCuenta().debitar(Dinero.de("6000"), "deuda", Escenario.HOY);   // saldo -1000
        esc.ana.inhabilitar();
        MedioPago m = new BilleteraVirtual("ana.mp");
        esc.usuarios.asociarMedioPago("1", m);
        esc.usuarios.recargarSaldo("1", Dinero.de("500"), m);
        assertFalse(esc.ana.estaHabilitado());                                    // saldo -500: sigue inhabilitada
        esc.usuarios.recargarSaldo("1", Dinero.de("500"), m);
        assertTrue(esc.ana.estaHabilitado());                                     // saldo 0: habilitada
    }

    @Test
    void suscribirPlanDebitaElPrecioDelPlan() throws Exception {
        esc.usuarios.setPrecioPlan(Dinero.de("3000"));
        PlanMensual plan = esc.usuarios.suscribirPlan("1");
        assertEquals(Dinero.de("2000"), esc.ana.getCuenta().getSaldo());
        assertEquals(Dinero.de("3000"), plan.getPrecio());
        assertTrue(esc.ana.getPlanActivo(Escenario.HOY).isPresent());
    }

    @Test
    void sinSaldoParaElPlanNoSeSuscribe() {
        esc.ana.getCuenta().debitar(Dinero.de("4500"), "gasto", Escenario.HOY);   // saldo $500 < $6000
        assertThrows(SaldoInsuficienteException.class, () -> esc.usuarios.suscribirPlan("1"));
        assertTrue(esc.ana.getPlanes().isEmpty());
    }

    @Test
    void soloSePuedeTenerUnPlanVigente() throws Exception {
        esc.usuarios.setPrecioPlan(Dinero.de("1000"));
        esc.usuarios.suscribirPlan("1");
        assertThrows(OperacionNoPermitidaException.class, () -> esc.usuarios.suscribirPlan("1"));
        assertEquals(1, esc.ana.getPlanes().size());
        assertEquals(Dinero.de("4000"), esc.ana.getCuenta().getSaldo());   // el segundo intento no debitó
    }

    @Test
    void elHistorialDeViajesSeDevuelveOrdenadoPorFecha() throws Exception {
        Viaje primero = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.viajes.devolverVehiculo(primero.getId(), "E01");
        esc.reloj.avanzarHoras(2);
        Viaje segundo = esc.viajes.retirarVehiculo("1", "E01", "E-001");
        esc.viajes.devolverVehiculo(segundo.getId(), "E01");
        List<Viaje> historial = esc.usuarios.consultarViajes("1");
        assertEquals(2, historial.size());
        assertSame(primero, historial.get(0));
        assertSame(segundo, historial.get(1));
    }

    @Test
    void consultarViajesDeUnUsuarioInexistenteEsUnError() {
        assertThrows(EntidadNoEncontradaException.class, () -> esc.usuarios.consultarViajes("404"));
    }
}
