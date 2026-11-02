package modelo;

import java.time.LocalDate;

public abstract class VehiculoElectrico extends Vehiculo {
    protected double nivelBateria;

    public VehiculoElectrico(String codigo, LocalDate fechaAlta, double nivelBateria) {
        super(codigo, fechaAlta);
        this.nivelBateria = nivelBateria;
    }

    @Override
    public boolean estaDisponibleParaRetiro() {
        if (this.nivelBateria < 15.0) {
            this.estado = "BATERIA_BAJA";
            return false;
        }
        return "DISPONIBLE".equalsIgnoreCase(this.estado);
    }

    public double getNivelBateria() {
        return nivelBateria;
    }

    public void setNivelBateria(double nivelBateria) {
        this.nivelBateria = nivelBateria;
        if (this.nivelBateria < 15.0) {
            this.estado = "BATERIA_BAJA";
        }
    }
}
