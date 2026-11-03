package movilidad.dominio;

import movilidad.Escenario;
import movilidad.dominio.tarifas.*;
import movilidad.dominio.vehiculos.TipoVehiculo;
import movilidad.excepciones.TarifaNoVigenteException;
import movilidad.util.Dinero;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TarifasYPromocionesTest {
    private final Escenario esc = new Escenario();

    private Viaje viajeDe(Usuario u, TipoVehiculo tipo, int minutos) throws Exception {
        PoliticaTarifa tarifa = esc.tarifas.tarifaVigente(tipo, LocalDate.of(2026, 11, 5));
        Viaje v = new Viaje(99, u, esc.b001, esc.e01, tarifa, Escenario.HOY);
        v.finalizar(esc.e01, Escenario.HOY.plusMinutes(minutos), 0);
        return v;
    }

    @Test
    void precioEsDesbloqueoMasMinutos() {
        assertEquals(Dinero.de("1400"), new TarifaBicicletaComun(LocalDate.now()).calcularPrecio(60));
        assertEquals(Dinero.de("200"), new TarifaBicicletaComun(LocalDate.now()).calcularPrecio(0));
    }

    @Test
    void recargoDel50PorCientoSoloParaMinutosPasadosDe120() {
        // 200 + 20*120 + 30*10 = 2900
        assertEquals(Dinero.de("2900"), new TarifaBicicletaComun(LocalDate.now()).calcularPrecio(130));
        assertEquals(Dinero.de("2600"), new TarifaBicicletaComun(LocalDate.now()).calcularPrecio(120));
    }

    @Test
    void saldoMinimoEsDesbloqueoMas30Minutos() throws Exception {
        LocalDate hoy = LocalDate.of(2026, 11, 5);
        assertEquals(Dinero.de("800"), esc.tarifas.saldoMinimo(esc.tarifas.tarifaVigente(TipoVehiculo.BICICLETA_COMUN, hoy)));
        assertEquals(Dinero.de("1650"), esc.tarifas.saldoMinimo(esc.tarifas.tarifaVigente(TipoVehiculo.BICICLETA_ELECTRICA, hoy)));
        assertEquals(Dinero.de("1800"), esc.tarifas.saldoMinimo(esc.tarifas.tarifaVigente(TipoVehiculo.MONOPATIN_ELECTRICO, hoy)));
    }

    @Test
    void tarifaVigenteEligeLaMasRecienteNoPosteriorALaFecha() throws Exception {
        TarifaBicicletaComun nueva = new TarifaBicicletaComun(Dinero.de("250"), Dinero.de("25"), LocalDate.of(2026, 12, 1));
        esc.tarifas.registrarTarifa(nueva);
        assertNotSame(nueva, esc.tarifas.tarifaVigente(TipoVehiculo.BICICLETA_COMUN, LocalDate.of(2026, 11, 30)));
        assertSame(nueva, esc.tarifas.tarifaVigente(TipoVehiculo.BICICLETA_COMUN, LocalDate.of(2026, 12, 1)));
    }

    @Test
    void sinTarifaVigenteLanzaExcepcion() {
        assertThrows(TarifaNoVigenteException.class,
                () -> esc.tarifas.tarifaVigente(TipoVehiculo.BICICLETA_COMUN, LocalDate.of(2025, 1, 1)));
    }

    @Test
    void primerViajeGratisHastaQuinceMinutos() throws Exception {
        Usuario nuevo = new Usuario("Nuevo", "2", "n@mail.com", LocalDate.now());
        ResultadoTarifa r10 = esc.promociones.calcularCostoViaje(viajeDe(nuevo, TipoVehiculo.BICICLETA_COMUN, 10), nuevo);
        assertEquals(Dinero.de("0"), r10.getCostoFinal());
        assertEquals("Primer viaje gratis", r10.getPromocionAplicada());
        // 20 min: se bonifican desbloqueo y 15 min; se cobran 5 min = $100
        ResultadoTarifa r20 = esc.promociones.calcularCostoViaje(viajeDe(nuevo, TipoVehiculo.BICICLETA_COMUN, 20), nuevo);
        assertEquals(Dinero.de("100"), r20.getCostoFinal());
        assertEquals(Dinero.de("600"), r20.getCostoBase());
    }

    @Test
    void primerViajeGratisNoAplicaSiYaHayViajesFinalizados() throws Exception {
        ResultadoTarifa r = esc.promociones.calcularCostoViaje(viajeDe(esc.ana, TipoVehiculo.BICICLETA_COMUN, 10), esc.ana);
        assertFalse(r.tuvoPromocion());
        assertEquals(Dinero.de("400"), r.getCostoFinal());
    }

    @Test
    void planMensualBonificaDesbloqueoYTreintaMinutos() throws Exception {
        esc.ana.suscribir(new PlanMensual(Escenario.HOY.minusDays(1), Dinero.de("6000")));
        // 45 min bici común: normal 1100; con plan se cobran 15 min = 300
        ResultadoTarifa r = esc.promociones.calcularCostoViaje(viajeDe(esc.ana, TipoVehiculo.BICICLETA_COMUN, 45), esc.ana);
        assertEquals(Dinero.de("1100"), r.getCostoBase());
        assertEquals(Dinero.de("300"), r.getCostoFinal());
        assertEquals("Plan mensual", r.getPromocionAplicada());
    }

    @Test
    void conVariasPromocionesAplicablesSeCobraLaMasFavorableSinAcumular() throws Exception {
        Usuario u = new Usuario("Nuevo", "3", "n@mail.com", LocalDate.now());
        u.suscribir(new PlanMensual(Escenario.HOY.minusDays(1), Dinero.de("6000")));
        // 20 min: primer viaje = $100, plan = $0 -> gana el plan
        ResultadoTarifa r = esc.promociones.calcularCostoViaje(viajeDe(u, TipoVehiculo.BICICLETA_COMUN, 20), u);
        assertEquals(Dinero.de("0"), r.getCostoFinal());
        assertEquals("Plan mensual", r.getPromocionAplicada());
    }

    @Test
    void planConLosVeinteViajesConsumidosYaNoEsAplicable() throws Exception {
        PlanMensual plan = new PlanMensual(Escenario.HOY.minusDays(1), Dinero.de("6000"));
        esc.ana.suscribir(plan);
        for (int i = 0; i < PlanMensual.VIAJES_INCLUIDOS; i++) {
            esc.ana.registrarViajeFinalizado(Escenario.HOY);
        }
        assertEquals(20, plan.getViajesConsumidos());
        assertTrue(esc.ana.getPlanActivo(Escenario.HOY).isEmpty());
        assertFalse(esc.promociones.calcularCostoViaje(viajeDe(esc.ana, TipoVehiculo.BICICLETA_COMUN, 20), esc.ana).tuvoPromocion());
    }

    @Test
    void planVencidoALos30DiasNoEsVigente() {
        PlanMensual plan = new PlanMensual(Escenario.HOY, Dinero.de("6000"));
        assertTrue(plan.estaVigente(Escenario.HOY.plusDays(29)));
        assertFalse(plan.estaVigente(Escenario.HOY.plusDays(30)));
    }

    @Test
    void noSePuedeTenerMasDeUnPlanVigente() throws Exception {
        esc.ana.suscribir(new PlanMensual(Escenario.HOY, Dinero.de("6000")));
        assertThrows(movilidad.excepciones.OperacionNoPermitidaException.class,
                () -> esc.ana.suscribir(new PlanMensual(Escenario.HOY.plusDays(1), Dinero.de("6000"))));
    }
}
