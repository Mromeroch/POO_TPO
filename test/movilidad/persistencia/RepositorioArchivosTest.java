package movilidad.persistencia;

import movilidad.DatosIniciales;
import movilidad.Sistema;
import movilidad.dominio.EstadoViaje;
import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;
import movilidad.dominio.vehiculos.EstadoVehiculo;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.dominio.vehiculos.VehiculoElectrico;
import movilidad.util.ArchivoCsv;
import movilidad.util.RelojPrueba;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** S23: lo que se guarda en disco debe poder cargarse y dar el mismo estado del sistema. */
class RepositorioArchivosTest {
    private static final String JUAN = "30111222";
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 5, 10, 0);

    @TempDir
    Path carpeta;

    private RepositorioArchivos nuevoRepo() {
        return new RepositorioArchivos(carpeta.toString());
    }

    // ------------------------------------------------------------ ida y vuelta completa
    @Test
    void elEstadoGuardadoSeRecuperaIgualAlCargar() throws Exception {
        RelojPrueba reloj = new RelojPrueba(T0);
        RepositorioArchivos repo1 = nuevoRepo();
        repo1.cargar();
        DatosIniciales.cargarSiVacio(repo1, reloj);
        repo1.guardar();
        Sistema s1 = new Sistema(reloj, repo1);
        s1.cargarConfiguracionPorDefecto();

        // Viaje 1 finalizado, con dos puntos de recorrido y una bicicleta eléctrica
        Viaje v1 = s1.getControladorViajes().retirarVehiculo(JUAN, "E01", "E-001");
        s1.getControladorViajes().registrarPunto(v1.getId(), -34.9205, -57.9536);
        reloj.avanzarMinutos(5);
        s1.getControladorViajes().registrarPunto(v1.getId(), -34.9305, -57.9536);
        reloj.avanzarMinutos(5);
        s1.getControladorViajes().devolverVehiculo(v1.getId(), "E02");

        // Una falla y un viaje 2 que queda EN_CURSO
        s1.getControladorMantenimiento().reportarFalla("B-002", "freno", JUAN);
        Viaje v2 = s1.getControladorViajes().retirarVehiculo(JUAN, "E01", "M-001");

        // ---- se carga todo en un repositorio nuevo, sobre la misma carpeta
        RepositorioArchivos repo2 = nuevoRepo();
        repo2.cargar();

        Usuario u1 = repo1.buscarPorDocumento(JUAN).orElseThrow();
        Usuario u2 = repo2.buscarPorDocumento(JUAN).orElseThrow();
        assertEquals(u1.getCuenta().getSaldo(), u2.getCuenta().getSaldo());
        assertEquals(u1.getCuenta().getMovimientos().size(), u2.getCuenta().getMovimientos().size());
        assertEquals(u1.getCuenta().getMedios().size(), u2.getCuenta().getMedios().size());
        assertEquals(u1.getViajesFinalizados(), u2.getViajesFinalizados());
        assertEquals(u1.estaHabilitado(), u2.estaHabilitado());

        // viaje finalizado
        Viaje f2 = repo2.buscarPorId(v1.getId()).orElseThrow();
        assertEquals(EstadoViaje.FINALIZADO, f2.getEstado());
        assertEquals(v1.getCosto(), f2.getCosto());
        assertEquals(v1.getPromocionAplicada(), f2.getPromocionAplicada());
        assertEquals(v1.getDistanciaRecorrida(), f2.getDistanciaRecorrida(), 0.001);
        assertEquals(2, f2.getPuntos().size());
        assertEquals("E02", f2.getEstacionDevolucion().getCodigo());

        // viaje en curso
        Viaje c2 = repo2.buscarEnCursoDe(u2).orElseThrow();
        assertEquals(v2.getId(), c2.getId());
        assertEquals("M-001", c2.getVehiculo().getCodigo());

        // vehículos: ubicación, estado, kilómetros, batería y fallas
        Vehiculo e001 = repo2.buscarVehiculo("E-001").orElseThrow();
        assertEquals(EstadoVehiculo.DISPONIBLE, e001.getEstado());
        assertTrue(repo2.buscarEstacion("E02").orElseThrow().contiene(e001));
        assertEquals(repo1.buscarVehiculo("E-001").orElseThrow().getKilometrosRecorridos(),
                e001.getKilometrosRecorridos(), 0.001);
        assertEquals(((VehiculoElectrico) repo1.buscarVehiculo("E-001").orElseThrow()).getNivelBateria(),
                ((VehiculoElectrico) e001).getNivelBateria(), 0.001);

        Vehiculo m001 = repo2.buscarVehiculo("M-001").orElseThrow();
        assertEquals(EstadoVehiculo.EN_USO, m001.getEstado());
        assertTrue(repo2.buscarEstaciones().stream().noneMatch(e -> e.contiene(m001)));

        assertEquals(1, repo2.buscarVehiculo("B-002").orElseThrow().cantidadFallas());

        // el próximo id continúa donde quedó (restaurarContadorViajes)
        assertEquals(v2.getId() + 1, repo2.siguienteId());
    }

    @Test
    void elPlanMensualConservaSusViajesConsumidos() throws Exception {
        RelojPrueba reloj = new RelojPrueba(T0);
        RepositorioArchivos repo1 = nuevoRepo();
        DatosIniciales.cargarSiVacio(repo1, reloj);
        Sistema s1 = new Sistema(reloj, repo1);
        s1.cargarConfiguracionPorDefecto();

        s1.getControladorUsuarios().suscribirPlan(JUAN);
        Viaje v = s1.getControladorViajes().retirarVehiculo(JUAN, "E01", "B-001");
        reloj.avanzarMinutos(10);
        s1.getControladorViajes().devolverVehiculo(v.getId(), "E01");

        RepositorioArchivos repo2 = nuevoRepo();
        repo2.cargar();
        Usuario u = repo2.buscarPorDocumento(JUAN).orElseThrow();
        assertEquals(1, u.getPlanes().size());
        assertEquals(1, u.getPlanes().get(0).getViajesConsumidos());
        assertTrue(u.getPlanActivo(reloj.ahora()).isPresent());
    }

    // ------------------------------------------------------------ casos borde
    @Test
    void cargarSinCarpetaNoFallaYDejaTodoVacio() {
        RepositorioArchivos repo = new RepositorioArchivos(carpeta.resolve("no_existe").toString());
        assertDoesNotThrow(repo::cargar);
        assertTrue(repo.buscarUsuarios().isEmpty());
        assertTrue(repo.buscarEstaciones().isEmpty());
    }

    @Test
    void unArchivoDanadoSeInformaConUncheckedIOException() throws Exception {
        Files.writeString(carpeta.resolve("estaciones.csv"), "codigo;direccion;capacidad\nE01;Plaza;no_es_numero\n");
        assertThrows(UncheckedIOException.class, () -> nuevoRepo().cargar());
    }

    @Test
    void datosInicialesNoSeCarganDosVeces() {
        RelojPrueba reloj = new RelojPrueba(T0);
        RepositorioArchivos repo1 = nuevoRepo();
        DatosIniciales.cargarSiVacio(repo1, reloj);
        repo1.guardar();

        RepositorioArchivos repo2 = nuevoRepo();
        repo2.cargar();
        int estaciones = repo2.buscarEstaciones().size();
        DatosIniciales.cargarSiVacio(repo2, reloj);   // ya hay datos: no hace nada
        assertEquals(estaciones, repo2.buscarEstaciones().size());
        assertEquals(2, repo2.buscarUsuarios().size());
    }

    // ------------------------------------------------------------ ArchivoCsv
    @Test
    void escaparYDesescaparSonInversos() {
        String original = "a;b\\c\nd|e%f";
        assertEquals(original, ArchivoCsv.desescapar(ArchivoCsv.escapar(original)));
        assertFalse(ArchivoCsv.escapar(original).contains(";"));
        assertFalse(ArchivoCsv.escapar(original).contains("\n"));
        assertEquals("", ArchivoCsv.escapar(null));
    }

    @Test
    void escribirYLeerConservanCeldasConCaracteresEspeciales() throws Exception {
        Path ruta = carpeta.resolve("prueba.csv");
        List<String[]> filas = new ArrayList<>();
        filas.add(new String[]{"1", "con;punto y coma", "línea1\nlínea2"});
        filas.add(new String[]{"2", "", "barra \\ invertida"});
        ArchivoCsv.escribir(ruta, "id;a;b", filas);

        List<String[]> leidas = ArchivoCsv.leer(ruta);
        assertEquals(2, leidas.size());
        assertArrayEquals(filas.get(0), leidas.get(0));
        assertArrayEquals(filas.get(1), leidas.get(1));
        assertFalse(Files.exists(carpeta.resolve("prueba.csv.tmp")));   // no queda el temporal
    }

    @Test
    void leerUnArchivoInexistenteDevuelveListaVacia() throws Exception {
        assertTrue(ArchivoCsv.leer(carpeta.resolve("nada.csv")).isEmpty());
    }

    @Test
    void unirYSepararListaConservanBarrasYPorcentajes() {
        List<String> original = List.of("a|b", "50%", "%7C", "c");
        assertEquals(original, ArchivoCsv.separarLista(ArchivoCsv.unirLista(original)));
        assertTrue(ArchivoCsv.separarLista("").isEmpty());
    }
}
