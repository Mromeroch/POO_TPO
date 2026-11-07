package movilidad.controladores;

import movilidad.dominio.Viaje;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.servicios.ServicioReportes;
import movilidad.servicios.UsoEstacion;

import java.time.LocalDate;
import java.util.List;

/** Casos de uso del administrador: reportes (CU-25, CU-26). Solo delega en ServicioReportes. */
public class ControladorReportes {
    private final ServicioReportes servicioReportes;

    public ControladorReportes(ServicioReportes servicioReportes) {
        this.servicioReportes = servicioReportes;
    }

    public List<Viaje> viajesPorUsuario(String documento, LocalDate desde, LocalDate hasta) {
        return servicioReportes.viajesPorUsuario(documento, desde, hasta);
    }

    public List<UsoEstacion> usoPorEstacion() {
        return servicioReportes.usoPorEstacion();
    }

    public List<Vehiculo> vehiculosConMasFallas() {
        return servicioReportes.vehiculosConMasFallas();
    }
}
