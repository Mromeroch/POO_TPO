package movilidad.dominio;

import java.time.LocalDateTime;

/** Punto del recorrido de un viaje en curso (coordenadas y hora). Inmutable. */
public class PuntoRecorrido {
    private final double latitud;
    private final double longitud;
    private final LocalDateTime momento;

    public PuntoRecorrido(double latitud, double longitud, LocalDateTime momento) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.momento = momento;
    }

    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public LocalDateTime getMomento() { return momento; }
}
