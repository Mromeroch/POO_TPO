package movilidad.dominio.vehiculos;

import java.time.LocalDate;

public abstract class Vehiculo {
    protected String codigo;
    protected LocalDate fechaAlta;
    protected double kilometrosRecorridos;
    protected String estado;

    public Vehiculo(String codigo, LocalDate fechaAlta) {
        this.codigo = codigo;
        this.fechaAlta = fechaAlta;
        this.kilometrosRecorridos = 0.0;
        this.estado = "DISPONIBLE";
    }

    public void registrarFalla() {
    }

    public abstract boolean estaDisponibleParaRetiro();

    public String getCodigo() {
        return codigo;
    }

    public String getEstado() {
        return estado;
    }

    public void setKilometrosRecorridos(double kilometrosRecorridos) {
        this.kilometrosRecorridos = kilometrosRecorridos;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void sumarKilometros(double km) {
        this.kilometrosRecorridos += km;
    }
}
