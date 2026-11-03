package movilidad.servicios;

import movilidad.Escenario;
import movilidad.dominio.EstadoViaje;
import movilidad.dominio.Viaje;
import movilidad.dominio.vehiculos.EstadoVehiculo;
import movilidad.util.Dinero;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** RN13 / S07: vehículo perdido a las 24 horas sin devolución. */
class ServicioControlViajesTest {
    private final Escenario esc = new Escenario();

    @Test
    void conExactamente24HorasTodaviaNoSeConsideraPerdido() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.reloj.avanzarHoras(24);
        assertTrue(esc.control.detectarPerdidos().isEmpty());
        assertEquals(EstadoViaje.EN_CURSO, v.getEstado());
    }

    @Test
    void pasadasLas24HorasElViajeEsPerdidoSeCobraElCargoYElVehiculoQuedaFueraDeServicio() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.reloj.avanzarHoras(24);
        esc.reloj.avanzarMinutos(1);
        List<Viaje> perdidos = esc.control.detectarPerdidos();
        assertEquals(1, perdidos.size());
        assertEquals(EstadoViaje.PERDIDO, v.getEstado());
        assertEquals(Dinero.de("20000"), v.getCosto());
        assertEquals(EstadoVehiculo.FUERA_DE_SERVICIO, esc.b001.getEstado());
        assertEquals(Dinero.de("-15000"), esc.ana.getCuenta().getSaldo());
        assertFalse(esc.ana.estaHabilitado());   // saldo negativo: se inhabilita
    }

    @Test
    void laDeteccionNoSeRepiteSobreViajesYaMarcados() throws Exception {
        esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.reloj.avanzarHoras(25);
        assertEquals(1, esc.control.detectarPerdidos().size());
        assertTrue(esc.control.detectarPerdidos().isEmpty());
        assertEquals(Dinero.de("-15000"), esc.ana.getCuenta().getSaldo());   // se cobró una sola vez
    }

    @Test
    void unViajeFinalizadoNuncaSeMarcaPerdido() throws Exception {
        Viaje v = esc.viajes.retirarVehiculo("1", "E01", "B-001");
        esc.viajes.devolverVehiculo(v.getId(), "E01");
        esc.reloj.avanzarHoras(48);
        assertTrue(esc.control.detectarPerdidos().isEmpty());
    }
}
