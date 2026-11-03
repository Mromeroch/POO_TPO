package movilidad.servicios;

import movilidad.dominio.EstadoViaje;
import movilidad.dominio.Viaje;
import movilidad.dominio.vehiculos.EstadoVehiculo;
import movilidad.persistencia.RepositorioViajes;
import movilidad.util.Dinero;
import movilidad.util.Reloj;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Detecta viajes sin devolver pasadas 24 horas y aplica el cargo por pérdida (RN13, S07). */
public class ServicioControlViajes {
    public static final long LIMITE_HORAS = 24;
    public static final BigDecimal CARGO_PERDIDA = Dinero.de("20000");   // S07, configurable

    private final RepositorioViajes repoViajes;
    private final Reloj reloj;

    public ServicioControlViajes(RepositorioViajes repoViajes, Reloj reloj) {
        this.repoViajes = repoViajes;
        this.reloj = reloj;
    }

    /**
     * S07: el viaje pasa a PERDIDO, se debita el cargo, el vehículo queda FUERA_DE_SERVICIO
     * y, si el saldo queda negativo, el usuario se inhabilita.
     *
     * @return los viajes marcados como perdidos en esta ejecución
     */
    public List<Viaje> detectarPerdidos() {
        List<Viaje> perdidos = new ArrayList<>();
        for (Viaje viaje : repoViajes.buscarViajes()) {
            // "Al superar las 24 horas sin devolución" (S07): estrictamente más de 24 h
            boolean vencido = viaje.getEstado() == EstadoViaje.EN_CURSO
                    && Duration.between(viaje.getInicio(), reloj.ahora()).compareTo(Duration.ofHours(LIMITE_HORAS)) > 0;
            if (vencido) {
                viaje.marcarPerdido();
                viaje.setCosto(CARGO_PERDIDA);
                viaje.setPromocionAplicada(null);
                viaje.getVehiculo().cambiarEstado(EstadoVehiculo.FUERA_DE_SERVICIO);
                viaje.getUsuario().getCuenta().debitar(CARGO_PERDIDA, "Cargo por vehículo perdido (viaje " + viaje.getId() + ")", reloj.ahora());
                if (viaje.getUsuario().getCuenta().getSaldo().signum() < 0) {
                    viaje.getUsuario().inhabilitar();
                }
                repoViajes.guardar(viaje);
                perdidos.add(viaje);
            }
        }
        return perdidos;
    }
}
