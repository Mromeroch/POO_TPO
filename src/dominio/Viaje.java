package movilidad.dominio;

import movilidad.dominio.tarifas.PoliticaTarifa;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.util.Dinero;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Viaje (GRASP Experto): conoce sus fechas, recorrido, duración y estado.
 * Natural ordering por fecha de inicio (Comparable). Un viaje cerrado no se modifica (RN15).
 */
public class Viaje implements Comparable<Viaje> {
    public static final int MINUTOS_GRACIA_ESTACION_LLENA = 10;   // RN08, S03
    private static final double RADIO_TIERRA_KM = 6371.0;

    private final long id;
    private final Usuario usuario;
    private final Vehiculo vehiculo;
    private final Estacion estacionRetiro;
    private Estacion estacionDevolucion;                 // 0..1: se completa al devolver
    private final PoliticaTarifa tarifa;                 // S16: el viaje conserva su tarifa
    private final LocalDateTime inicio;
    private LocalDateTime fin;
    private double distanciaRecorrida;
    private EstadoViaje estado = EstadoViaje.EN_CURSO;
    private BigDecimal costo = Dinero.CERO;
    private String promocionAplicada;
    private LocalDateTime instanteEstacionLlena;         // S03: primer intento fallido
    private final List<PuntoRecorrido> puntos = new ArrayList<>();

    public Viaje(long id, Usuario usuario, Vehiculo vehiculo, Estacion estacionRetiro,
                 PoliticaTarifa tarifa, LocalDateTime inicio) {
        this.id = id;
        this.usuario = usuario;
        this.vehiculo = vehiculo;
        this.estacionRetiro = estacionRetiro;
        this.tarifa = tarifa;
        this.inicio = inicio;
    }

    public void agregarPunto(PuntoRecorrido punto) {
        puntos.add(punto);
    }

    /** S03: solo cuenta el primer intento fallido de devolución. */
    public void registrarEstacionLlena(LocalDateTime instante) {
        if (instanteEstacionLlena == null) {
            instanteEstacionLlena = instante;
        }
    }

    /**
     * Cierra el viaje. S13: si hay puntos de recorrido se calcula la distancia con ellos;
     * si no, se usa la distancia informada al devolver.
     */
    public void finalizar(Estacion estacion, LocalDateTime fin, double distanciaInformadaKm) {
        this.estacionDevolucion = estacion;
        this.fin = fin;
        this.distanciaRecorrida = puntos.size() >= 2 ? calcularDistancia() : distanciaInformadaKm;
        this.estado = EstadoViaje.FINALIZADO;
    }

    /** RN11/S12: cancelación sin cargo. */
    public void cancelar() {
        this.estado = EstadoViaje.CANCELADO;
        this.costo = Dinero.CERO;
    }

    /** RN13/S07. */
    public void marcarPerdido() {
        this.estado = EstadoViaje.PERDIDO;
    }

    /** S02: minutos entre inicio y fin, redondeados hacia arriba. */
    public int duracionMinutos() {
        if (fin == null) {
            throw new IllegalStateException("El viaje todavía no finalizó");
        }
        return minutosEntre(inicio, fin);
    }

    /** S03: descuenta hasta 10 minutos contados desde el primer intento fallido por estación llena. */
    public int minutosFacturables() {
        int duracion = duracionMinutos();
        if (instanteEstacionLlena == null) {
            return duracion;
        }
        int gracia = Math.min(MINUTOS_GRACIA_ESTACION_LLENA, minutosEntre(instanteEstacionLlena, fin));
        return Math.max(0, duracion - gracia);
    }

    /** S13: suma de tramos (fórmula de Haversine) entre puntos consecutivos. */
    public double calcularDistancia() {
        double total = 0;
        for (int i = 1; i < puntos.size(); i++) {
            total += distanciaKm(puntos.get(i - 1), puntos.get(i));
        }
        return total;
    }

    private static int minutosEntre(LocalDateTime desde, LocalDateTime hasta) {
        long segundos = Duration.between(desde, hasta).getSeconds();
        return (int) Math.ceil(Math.max(0, segundos) / 60.0);
    }

    private static double distanciaKm(PuntoRecorrido a, PuntoRecorrido b) {
        double dLat = Math.toRadians(b.getLatitud() - a.getLatitud());
        double dLon = Math.toRadians(b.getLongitud() - a.getLongitud());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.getLatitud())) * Math.cos(Math.toRadians(b.getLatitud()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(h));
    }

    /** Orden natural: por fecha de inicio. */
    @Override
    public int compareTo(Viaje otro) {
        return inicio.compareTo(otro.inicio);
    }

    public void setCosto(BigDecimal costo) { this.costo = Dinero.redondear(costo); }
    public void setPromocionAplicada(String promocion) { this.promocionAplicada = promocion; }

    public long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public Estacion getEstacionRetiro() { return estacionRetiro; }
    public Estacion getEstacionDevolucion() { return estacionDevolucion; }
    public PoliticaTarifa getTarifa() { return tarifa; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFin() { return fin; }
    public double getDistanciaRecorrida() { return distanciaRecorrida; }
    public EstadoViaje getEstado() { return estado; }
    public BigDecimal getCosto() { return costo; }
    public String getPromocionAplicada() { return promocionAplicada; }
    public LocalDateTime getInstanteEstacionLlena() { return instanteEstacionLlena; }
    public List<PuntoRecorrido> getPuntos() { return Collections.unmodifiableList(puntos); }
}
