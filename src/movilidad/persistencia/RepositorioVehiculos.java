package movilidad.persistencia;

import movilidad.dominio.vehiculos.Vehiculo;

import java.util.List;
import java.util.Optional;

public interface RepositorioVehiculos {
    void guardar(Vehiculo vehiculo);

    Optional<Vehiculo> buscarVehiculo(String codigo);

    List<Vehiculo> buscarVehiculos();
}
