package movilidad.dominio;

import movilidad.dominio.mantenimiento.ReporteFalla;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.dominio.vehiculos.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VehiculoTest {
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 1, 10, 0);
    private final LocalDate alta = LocalDate.of(2026, 1, 1);

    @Test
    void soloUnVehiculoDisponiblePuedeRetirarse() {
        BicicletaComun b = new BicicletaComun("B", alta, 26);
        assertTrue(b.puedeRetirarse());
        b.cambiarEstado(EstadoVehiculo.EN_REPARACION);
        assertFalse(b.puedeRetirarse());
    }

    @Test
    void electricoConBateriaMenorAl15PorCientoNoPuedeRetirarseYPasaABateriaBaja() {
        BicicletaElectrica e = new BicicletaElectrica("E", alta, 60);
        e.setNivelBateria(14);
        assertEquals(EstadoVehiculo.BATERIA_BAJA, e.getEstado());
        assertFalse(e.puedeRetirarse());
    }

    @Test
    void electricoConBateriaJustoEnElLimiteSiPuedeRetirarse() {
        MonopatinElectrico m = new MonopatinElectrico("M", alta, 25);
        m.setNivelBateria(15);
        assertTrue(m.puedeRetirarse());
    }

    @Test
    void polimorfismoCadaTipoDescuentaBateriaConSuPropioConsumo() {
        BicicletaElectrica e = new BicicletaElectrica("E", alta, 60);
        MonopatinElectrico m = new MonopatinElectrico("M", alta, 25);
        e.actualizarTrasViaje(10);   // 2 % por km
        m.actualizarTrasViaje(10);   // 4 % por km
        assertEquals(80.0, e.getNivelBateria(), 0.001);
        assertEquals(60.0, m.getNivelBateria(), 0.001);
    }

    @Test
    void trasUnViajeLargoElMonopatinQuedaEnBateriaBaja() {
        MonopatinElectrico m = new MonopatinElectrico("M", alta, 25);
        m.cambiarEstado(EstadoVehiculo.EN_USO);
        m.actualizarTrasViaje(22);   // 100 - 88 = 12 %
        assertEquals(EstadoVehiculo.BATERIA_BAJA, m.getEstado());
        m.reponerBateria();
        assertEquals(EstadoVehiculo.DISPONIBLE, m.getEstado());
        assertEquals(100.0, m.getNivelBateria(), 0.001);
    }

    @Test
    void bicicletaComunNoTieneBateriaYQuedaDisponibleTrasElViaje() {
        BicicletaComun b = new BicicletaComun("B", alta, 26);
        b.cambiarEstado(EstadoVehiculo.EN_USO);
        b.actualizarTrasViaje(5.5);
        assertEquals(EstadoVehiculo.DISPONIBLE, b.getEstado());
        assertEquals(5.5, b.getKilometrosRecorridos(), 0.001);
    }

    @Test
    void tresFallasEnTreintaDiasPasanAMantenimientoObligatorio() {
        BicicletaComun b = new BicicletaComun("B", alta, 26);
        assertFalse(b.registrarFalla(new ReporteFalla(T0, "freno", "1")));
        assertFalse(b.registrarFalla(new ReporteFalla(T0.plusDays(10), "cadena", "1")));
        assertTrue(b.registrarFalla(new ReporteFalla(T0.plusDays(29), "luz", "OP-01")));
        assertEquals(EstadoVehiculo.EN_REPARACION, b.getEstado());
    }

    @Test
    void fallasFueraDeLaVentanaDeTreintaDiasNoCuentan() {
        BicicletaComun b = new BicicletaComun("B", alta, 26);
        b.registrarFalla(new ReporteFalla(T0, "a", "1"));
        b.registrarFalla(new ReporteFalla(T0.plusDays(15), "b", "1"));
        assertFalse(b.registrarFalla(new ReporteFalla(T0.plusDays(31), "c", "1")));   // la 1.ª quedó fuera de la ventana
        assertEquals(EstadoVehiculo.DISPONIBLE, b.getEstado());
    }

    @Test
    void siElVehiculoEstaEnUsoElMantenimientoSeAplicaAlFinalizarElViaje() {
        BicicletaComun b = new BicicletaComun("B", alta, 26);
        b.cambiarEstado(EstadoVehiculo.EN_USO);
        b.registrarFalla(new ReporteFalla(T0, "a", "1"));
        b.registrarFalla(new ReporteFalla(T0.plusDays(1), "b", "1"));
        assertTrue(b.registrarFalla(new ReporteFalla(T0.plusDays(2), "c", "1")));
        assertEquals(EstadoVehiculo.EN_USO, b.getEstado());
        assertTrue(b.tieneMantenimientoPendiente());
        b.actualizarTrasViaje(1);
        assertEquals(EstadoVehiculo.EN_REPARACION, b.getEstado());
    }

    @Test
    void sePuedeTratarATodosLosTiposComoVehiculo() {
        List<Vehiculo> flota = Arrays.asList(new BicicletaComun("B", alta, 26),
                new BicicletaElectrica("E", alta, 60), new MonopatinElectrico("M", alta, 25));
        assertEquals(TipoVehiculo.BICICLETA_COMUN, flota.get(0).getTipo());
        assertEquals(TipoVehiculo.BICICLETA_ELECTRICA, flota.get(1).getTipo());
        assertEquals(TipoVehiculo.MONOPATIN_ELECTRICO, flota.get(2).getTipo());
        flota.forEach(v -> assertTrue(v.puedeRetirarse()));
    }
}
