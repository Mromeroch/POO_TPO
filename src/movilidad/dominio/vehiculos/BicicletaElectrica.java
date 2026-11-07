package movilidad.dominio.vehiculos;

import java.time.LocalDate;

public class BicicletaElectrica extends VehiculoElectrico {
    private double autonomia;

    public BicicletaElectrica(String codigo, LocalDate fechaAlta, double nivelBateria, double autonomia) {
        super(codigo, fechaAlta, nivelBateria);
        this.autonomia = autonomia;
    }
}
