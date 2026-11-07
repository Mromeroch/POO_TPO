package movilidad;

import movilidad.controladores.ControladorMantenimiento;
import movilidad.controladores.ControladorOperaciones;
import movilidad.controladores.ControladorReportes;
import movilidad.controladores.ControladorUsuarios;
import movilidad.controladores.ControladorViajes;
import movilidad.dominio.Estacion;
import movilidad.dominio.Usuario;
import movilidad.dominio.personal.Operador;
import movilidad.dominio.personal.Tecnico;
import movilidad.dominio.vehiculos.BicicletaComun;
import movilidad.dominio.vehiculos.BicicletaElectrica;
import movilidad.dominio.vehiculos.MonopatinElectrico;
import movilidad.persistencia.RepositorioMemoria;
import movilidad.servicios.ServicioControlViajes;
import movilidad.servicios.ServicioPromociones;
import movilidad.servicios.ServicioTarifas;
import movilidad.util.Dinero;
import movilidad.util.RelojPrueba;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Escenario común de las pruebas: sistema completo en memoria con reloj controlable.
 * Datos: usuaria "1" (Ana) con $5.000 y un viaje previo finalizado; estaciones E01 (3/10: B-001, E-001, M-001),
 * E02 (2/2, llena: B-002, B-003) y E03 (1/5: B-009); operador OP-01 y técnico TEC-01.
 * Tarifas de referencia ($200+$20, $300+$45, $300+$50) y las dos promociones del enunciado. */
public class Escenario {
    public static final LocalDateTime HOY = LocalDateTime.of(2026, 11, 5, 10, 0);

    public final RelojPrueba reloj = new RelojPrueba(HOY);
    public final RepositorioMemoria repo = new RepositorioMemoria();
    public final Sistema sistema = new Sistema(reloj, repo);

    public final ServicioTarifas tarifas = sistema.getServicioTarifas();
    public final ServicioPromociones promociones = sistema.getServicioPromociones();
    public final ServicioControlViajes control = sistema.getServicioControlViajes();
    public final ControladorUsuarios usuarios = sistema.getControladorUsuarios();
    public final ControladorViajes viajes = sistema.getControladorViajes();
    public final ControladorMantenimiento mantenimiento = sistema.getControladorMantenimiento();
    public final ControladorOperaciones operaciones = sistema.getControladorOperaciones();
    public final ControladorReportes reportes = sistema.getControladorReportes();

    public final Estacion e01 = new Estacion("E01", "Plaza Moreno", 10);
    public final Estacion e02 = new Estacion("E02", "Terminal", 2);
    public final Estacion e03 = new Estacion("E03", "Parque Saavedra", 5);

    private final LocalDate alta = LocalDate.of(2026, 1, 1);
    public final BicicletaComun b001 = new BicicletaComun("B-001", alta, 26);
    public final BicicletaComun b002 = new BicicletaComun("B-002", alta, 26);
    public final BicicletaComun b003 = new BicicletaComun("B-003", alta, 26);
    public final BicicletaComun b009 = new BicicletaComun("B-009", alta, 26);
    public final BicicletaElectrica eb001 = new BicicletaElectrica("E-001", alta, 60);
    public final MonopatinElectrico m001 = new MonopatinElectrico("M-001", alta, 25);

    public final Usuario ana = new Usuario("Ana Gómez", "1", "ana@mail.com", alta);

    public Escenario() {
        sistema.cargarConfiguracionPorDefecto();

        e01.ingresar(b001);
        e01.ingresar(eb001);
        e01.ingresar(m001);
        e02.ingresar(b002);
        e02.ingresar(b003);
        e03.ingresar(b009);
        for (var v : new movilidad.dominio.vehiculos.Vehiculo[]{b001, b002, b003, b009, eb001, m001}) {
            repo.guardar(v);
        }
        repo.guardar(e01);
        repo.guardar(e02);
        repo.guardar(e03);

        ana.getCuenta().recargar(Dinero.de("5000"), HOY);
        ana.registrarViajeFinalizado(HOY.minusDays(7));   // ya hizo un viaje: no le corresponde "primer viaje gratis"
        repo.guardar(ana);

        repo.guardar(new Operador("OP-01", "Oscar Operador", "1100000001"));
        repo.guardar(new Tecnico("TEC-01", "Tomás Técnico", "1100000002"));
    }
}
