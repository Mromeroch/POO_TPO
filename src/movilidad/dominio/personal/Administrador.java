package movilidad.dominio.personal;

public class Administrador extends Empleado {
    public Administrador(String legajo, String nombre, String telefono) {
        super(legajo, nombre, telefono);
    }

    @Override
    public String getRol() {
        return "ADMINISTRADOR";
    }
}
