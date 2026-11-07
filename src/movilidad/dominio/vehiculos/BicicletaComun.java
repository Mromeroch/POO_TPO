package movilidad.dominio.vehiculos;

import java.time.LocalDate;

public class BicicletaComun extends Vehiculo {
    private int tamanioRodado;

    public BicicletaComun(String codigo, LocalDate fechaAlta, int tamanioRodado) {
        super(codigo, fechaAlta);
    }

    @Override
    public boolean estaDisponibleParaRetiro() {
        return "DISPONIBLE".equalsIgnoreCase(this.estado);
    }

    public int getTamanioRodado() {
        return tamanioRodado;
    }

}
