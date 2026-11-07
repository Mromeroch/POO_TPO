package movilidad.servicios;

import movilidad.dominio.Estacion;
import movilidad.dominio.EstadoViaje;
import movilidad.dominio.Viaje;
import movilidad.dominio.mantenimiento.ComparadorFallasVehiculo;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.persistencia.RepositorioEstaciones;
import movilidad.persistencia.RepositorioVehiculos;
import movilidad.persistencia.RepositorioViajes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Genera los tres reportes del administrador (RF15, S24). Usa Comparable (Viaje) y Comparator (fallas). */
public class ServicioReportes {
    private final RepositorioViajes repoViajes;
    private final RepositorioEstaciones repoEstaciones;
    private final RepositorioVehiculos repoVehiculos;

    public ServicioReportes(RepositorioViajes repoViajes, RepositorioEstaciones repoEstaciones,
                            RepositorioVehiculos repoVehiculos) {
        this.repoViajes = repoViajes;
        this.repoEstaciones = repoEstaciones;
        this.repoVehiculos = repoVehiculos;
    }

    /** Viajes finalizados de un usuario, por documento y rango de fechas opcional (null = sin límite), ordenados por fecha. */
    public List<Viaje> viajesPorUsuario(String documento, LocalDate desde, LocalDate hasta) {
        List<Viaje> resultado = repoViajes.buscarPorUsuario(documento).stream()
                .filter(v -> v.getEstado() == EstadoViaje.FINALIZADO)
                .filter(v -> desde == null || !v.getInicio().toLocalDate().isBefore(desde))
                .filter(v -> hasta == null || !v.getInicio().toLocalDate().isAfter(hasta))
                .collect(Collectors.toList());
        Collections.sort(resultado);   // orden natural (Comparable) por fecha de inicio
        return resultado;
    }

    /** Cantidad de retiros y devoluciones por estación. */
    public List<UsoEstacion> usoPorEstacion() {
        List<UsoEstacion> filas = new ArrayList<>();
        for (Estacion estacion : repoEstaciones.buscarEstaciones()) {
            long retiros = repoViajes.buscarViajes().stream().filter(v -> v.getEstacionRetiro() == estacion).count();
            long devoluciones = repoViajes.buscarViajes().stream()
                    .filter(v -> v.getEstado() == EstadoViaje.FINALIZADO && v.getEstacionDevolucion() == estacion).count();
            filas.add(new UsoEstacion(estacion.getCodigo(), retiros, devoluciones));
        }
        return filas;
    }

    /** Ranking descendente por cantidad total de reportes de falla. */
    public List<Vehiculo> vehiculosConMasFallas() {
        List<Vehiculo> vehiculos = repoVehiculos.buscarVehiculos().stream()
                .filter(v -> v.cantidadFallas() > 0)
                .collect(Collectors.toList());
        vehiculos.sort(new ComparadorFallasVehiculo());   // orden personalizado (Comparator)
        return vehiculos;
    }
}
