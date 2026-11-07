package movilidad.persistencia;

import movilidad.dominio.personal.Empleado;

import java.util.List;
import java.util.Optional;

/** Operadores, técnicos y administradores. (No figura en el diagrama final: ver docs/NOTAS_DE_DISENO.md.) */
public interface RepositorioPersonal {
    void guardar(Empleado empleado);

    Optional<Empleado> buscarEmpleado(String legajo);

    List<Empleado> buscarEmpleados();
}
