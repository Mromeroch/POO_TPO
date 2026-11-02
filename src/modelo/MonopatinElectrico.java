package modelo;

import java.time.LocalDate;

public class MonopatinElectrico extends VehiculoElectrico {
    private double velocidadMaxima;

    public MonopatinElectrico(String codigo, LocalDate fechaAlta, double nivelBateria, double velocidadMaxima) {
        super(codigo, fechaAlta, nivelBateria);
        this.velocidadMaxima = velocidadMaxima;
    }

    public double getVelocidadMaxima() {
        return velocidadMaxima;
    }
}
