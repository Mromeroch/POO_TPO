package modelo;

import java.time.LocalDate;

public class Usuario {
    private String nombre;
    private String documento;
    private String correoElectronico;
    private LocalDate fechaAlta;
    private boolean habilitado;
    private Cuenta cuenta;

    // Constructor
    public Usuario(String nombre, String documento, String correoElectronico, LocalDate fechaAlta) {
        this.nombre = nombre;
        this.documento = documento;
        this.correoElectronico = correoElectronico;
        this.fechaAlta = fechaAlta;
        this.habilitado = true;
        this.cuenta = new Cuenta(0.0);
    }

    public boolean estaHabilitado() {
        if (cuenta.getSaldo() < 0) {
            this.habilitado = false;
        }
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }
}
