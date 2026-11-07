package movilidad.dominio.personal;

public class Tecnico extends Empleado {
    public Tecnico(String legajo, String nombre, String telefono) {
        super(legajo, nombre, telefono);
    }

    @Override
    public String getRol() {
        return "TECNICO";
    }
}
