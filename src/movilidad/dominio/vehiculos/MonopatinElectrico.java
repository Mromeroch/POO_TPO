package movilidad.dominio.vehiculos;

import java.time.LocalDate;

public class MonopatinElectrico extends VehiculoElectrico {
    private final double velocidadMaxima;

    public MonopatinElectrico(String codigo, LocalDate fechaAlta, double velocidadMaxima) {
        super(codigo, fechaAlta);
        this.velocidadMaxima = velocidadMaxima;
    }

    @Override
    public TipoVehiculo getTipo() { return TipoVehiculo.MONOPATIN_ELECTRICO; }

    @Override
    protected double consumoPorKm() { return 4.0; }

    public double getVelocidadMaxima() { return velocidadMaxima; }
}
