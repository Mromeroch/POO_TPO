package modelo;

import java.time.LocalDate;

public class MovimientoCuenta {
    private LocalDate fecha;
    private double importe;
    private String tipo;

    public MovimientoCuenta(LocalDate fecha, double importe, String tipo) {
        this.fecha = fecha;
        this.importe = importe;
        this.tipo = tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getImporte() {
        return importe;
    }

    public String getTipo() {
        return tipo;
    }
}
