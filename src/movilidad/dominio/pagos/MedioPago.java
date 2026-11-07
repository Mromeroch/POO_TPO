package movilidad.dominio.pagos;

/** Medio de pago asocado a una cuenta (tarjeta o billetera virtual). Se agregan tipos nuevos sin tocar a los clientes. */
public interface MedioPago {
    /** Tipo para la persistencia: TARJETA O BILLETERA. */
    String getTipo();

    /** Número de tarjeta o alias de la billetera. */
    String getIdentificador();

    /** Valida los datos del medio de pago (RF02, CU-03). */
    boolean validar();

    /** Texto para mostrar al usuario (nunca el número completo de una tarjeta). */
    String descripcion();
}
