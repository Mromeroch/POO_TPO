package movilidad.persistencia;

import movilidad.dominio.Estacion;
import movilidad.dominio.mantenimiento.AlertaEstacion;

import java.util.List;
import java.util.Optional;

public interface RepositorioEstaciones {
    void guardar(Estacion estacion);

    Optional<Estacion> buscarEstacion(String codigo);

    List<Estacion> buscarEstaciones();

    void guardarAlerta(AlertaEstacion alerta);

    List<AlertaEstacion> buscarAlertas();
}
