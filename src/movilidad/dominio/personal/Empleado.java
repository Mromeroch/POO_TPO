package movilidad.dominio.personal;

public abstract class Empleado {
    private final String legajo;
    private final String nombre;
    private final String telefono;

    public Empleado(String legajo, String nombre, String telefono) {
        this.legajo = legajo;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public abstract String getRol();


    public String getLegajo() {
        return legajo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return getRol() + " " + nombre + " (legajo " + legajo + ")";
    }
}

