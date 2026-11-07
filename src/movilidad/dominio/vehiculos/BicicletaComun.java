package movilidad.dominio.vehiculos;

import java.time.LocalDate;

public class BicicletaComun extends Vehiculo {
    private final int tamanioRodado;

    public BicicletaComun(String codigo, LocalDate fechaAlta, int tamanioRodado) {
        super(codigo, fechaAlta);
        this.tamanioRodado = tamanioRodado;
    }

    @Override
    public TipoVehiculo getTipo() { return TipoVehiculo.BICICLETA_COMUN; }

    public int getTamanioRodado() { return tamanioRodado; }
}