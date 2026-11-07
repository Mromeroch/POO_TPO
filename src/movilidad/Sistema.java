package movilidad;

import movilidad.controladores.ControladorMantenimiento;
import movilidad.controladores.ControladorOperaciones;
import movilidad.controladores.ControladorReportes;
import movilidad.controladores.ControladorUsuarios;
import movilidad.controladores.ControladorViajes;
import movilidad.dominio.promociones.PromocionPlanMensual;
import movilidad.dominio.promociones.PromocionPrimerViaje;
import movilidad.dominio.tarifas.TarifaBicicletaComun;
import movilidad.dominio.tarifas.TarifaBicicletaElectrica;
import movilidad.dominio.tarifas.TarifaMonopatinElectrico;
import movilidad.persistencia.RepositorioMemoria;
import movilidad.servicios.ServicioControlViajes;
import movilidad.servicios.ServicioPagosSimulado;
import movilidad.servicios.ServicioPromociones;
import movilidad.servicios.ServicioReportes;
import movilidad.servicios.ServicioTarifas;
import movilidad.util.Reloj;

import java.time.LocalDate;

/**
 * Raíz de composición: es el único lugar donde se crean las clases concretas y se inyectan sus
 * dependencias (DIP). Las pruebas pueden armar un sistema con un reloj de prueba y un repositorio en memoria.
 */
public class Sistema {
    private final Reloj reloj;
    private final RepositorioMemoria repositorio;
    private final ServicioTarifas servicioTarifas = new ServicioTarifas();
    private final ServicioPromociones servicioPromociones = new ServicioPromociones();
    private final ServicioControlViajes servicioControlViajes;
    private final ControladorUsuarios controladorUsuarios;
    private final ControladorViajes controladorViajes;
    private final ControladorMantenimiento controladorMantenimiento;
    private final ControladorOperaciones controladorOperaciones;
    private final ControladorReportes controladorReportes;

    public Sistema(Reloj reloj, RepositorioMemoria repositorio) {
        this.reloj = reloj;
        this.repositorio = repositorio;
        this.servicioControlViajes = new ServicioControlViajes(repositorio, reloj);
        this.controladorUsuarios = new ControladorUsuarios(repositorio, repositorio, new ServicioPagosSimulado(), reloj);
        this.controladorViajes = new ControladorViajes(repositorio, repositorio, repositorio,
                servicioTarifas, servicioPromociones, reloj);
        this.controladorMantenimiento = new ControladorMantenimiento(repositorio, repositorio, repositorio, repositorio, reloj);
        this.controladorOperaciones = new ControladorOperaciones(repositorio, repositorio, repositorio, repositorio,
                servicioControlViajes, reloj);
        this.controladorReportes = new ControladorReportes(new ServicioReportes(repositorio, repositorio, repositorio));
    }

    /** Registra las tarifas de referencia (S25) y las dos promociones del enunciado. */
    public void cargarConfiguracionPorDefecto() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        servicioTarifas.registrarTarifa(new TarifaBicicletaComun(desde));
        servicioTarifas.registrarTarifa(new TarifaBicicletaElectrica(desde));
        servicioTarifas.registrarTarifa(new TarifaMonopatinElectrico(desde));
        servicioPromociones.registrarPromocion(new PromocionPrimerViaje());
        servicioPromociones.registrarPromocion(new PromocionPlanMensual());
    }

    public Reloj getReloj() {
        return reloj;
    }

    public RepositorioMemoria getRepositorio() {
        return repositorio;
    }

    public ServicioTarifas getServicioTarifas() {
        return servicioTarifas;
    }

    public ServicioPromociones getServicioPromociones() {
        return servicioPromociones;
    }

    public ServicioControlViajes getServicioControlViajes() {
        return servicioControlViajes;
    }

    public ControladorUsuarios getControladorUsuarios() {
        return controladorUsuarios;
    }

    public ControladorViajes getControladorViajes() {
        return controladorViajes;
    }

    public ControladorMantenimiento getControladorMantenimiento() {
        return controladorMantenimiento;
    }

    public ControladorOperaciones getControladorOperaciones() {
        return controladorOperaciones;
    }

    public ControladorReportes getControladorReportes() {
        return controladorReportes;
    }
}