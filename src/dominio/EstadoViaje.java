package movilidad.dominio;

/** Estados de un viaje (S11). Solo los FINALIZADO cuentan como viajes realizados. */
public enum EstadoViaje {
    EN_CURSO, FINALIZADO, CANCELADO, PERDIDO
}
