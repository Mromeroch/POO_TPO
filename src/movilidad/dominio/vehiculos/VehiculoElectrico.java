package movilidad.dominio.vehiculos;

import java.time.LocalDate;

/** Concentra el comportamiento de batería de los vehículos eléctricos (RN03, S14). */
public abstract class VehiculoElectrico extends Vehiculo {
    public static final double UMBRAL_BATERIA_BAJA = 15.0;

    private double nivelBateria = 100.0;

    protected VehiculoElectrico(String codigo, LocalDate fechaAlta) {
        super(codigo, fechaAlta);
    }

    /** Porcentaje de batería que se consume por kilómetro (lo define cada tipo). */
    protected abstract double consumoPorKm();

    @Override
    public boolean puedeRetirarse() {
        return super.puedeRetirarse() && nivelBateria >= UMBRAL_BATERIA_BAJA;
    }

    @Override
    protected void descontarBateria(double kilometros) {
        nivelBateria = nivelBateria - (kilometros * consumoPorKm());
        if (nivelBateria < 0.0) {
            nivelBateria = 0.0;
        }
    }

    @Override
    protected EstadoVehiculo estadoTrasViaje() {
        return nivelBateria < UMBRAL_BATERIA_BAJA ? EstadoVehiculo.BATERIA_BAJA : EstadoVehiculo.DISPONIBLE;
    }

    /**
     * Repone la batería al 100 % y, si estaba en BATERIA_BAJA, vuelve a DISPONIBLE (S14, S20).
     *
     *
     */
    @Override
    public void reponerBateria() {
        nivelBateria = 100.0;
        if (estado == EstadoVehiculo.BATERIA_BAJA) {
            estado = EstadoVehiculo.DISPONIBLE;
        }

    }

    /** Fija el nivel (0 a 100). Bajo el 15 % un vehículo disponible pasa a BATERIA_BAJA y no se ofrece (RN03). */
    public void setNivelBateria(double nivel) {
        if (nivel > 100.0) this.nivelBateria = 100.0;
        else this.nivelBateria = Math.max(nivel, 0.0);

        if (nivelBateria < UMBRAL_BATERIA_BAJA && estado == EstadoVehiculo.DISPONIBLE) {
            estado = EstadoVehiculo.BATERIA_BAJA;
        } else if (nivelBateria >= UMBRAL_BATERIA_BAJA && estado == EstadoVehiculo.BATERIA_BAJA) {
            estado = EstadoVehiculo.DISPONIBLE;
        }
    }

    /** Usado por la persistencia: restaura el nivel sin tocar el estado. */
    public void restaurarNivelBateria(double nivel) {
        this.nivelBateria = nivel;
    }

    public double getNivelBateria() {
        return nivelBateria;
    }
}
