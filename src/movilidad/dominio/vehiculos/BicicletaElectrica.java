package movilidad.dominio.vehiculos;

import java.time.LocalDate;

public class BicicletaElectrica extends VehiculoElectrico {
    private final double autonomia;

    public BicicletaElectrica(String codigo, LocalDate fechaAlta, double autonomia) {
        super(codigo, fechaAlta);
        this.autonomia = autonomia;
    }

    @Override
    public TipoVehiculo getTipo() { return TipoVehiculo.BICICLETA_ELECTRICA; }

    @Override
    protected double consumoPorKm() { return 2.0; }

    public double getAutonomia() { return autonomia; }
}
