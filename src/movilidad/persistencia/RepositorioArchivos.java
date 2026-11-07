package movilidad.persistencia;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Persistencia en archivos de texto (S23: CSV o JSON dentro de la carpeta data).
 * Mantiene los datos en memoria (hereda de RepositorioMemoria) y los vuelca a disco.
 *
 * ESTADO: esqueleto. cargar() y guardar() están pendientes (primera tarea de la Etapa 2).
 * Sugerencia: un archivo por entidad (usuarios.csv, estaciones.csv, vehiculos.csv, viajes.csv, ...)
 * y cargar en este orden: vehículos -> estaciones -> usuarios -> personal -> viajes -> órdenes,
 * porque las entidades posteriores referencian a las anteriores.
 */
public class RepositorioArchivos extends RepositorioMemoria {
    private final Path carpetaData;

    public RepositorioArchivos(String carpetaData) {
        this.carpetaData = Paths.get(carpetaData);
    }

    /** Lee los archivos de la carpeta data al iniciar el sistema. */
    public void cargar() {
        // TODO (Etapa 2): leer los archivos y reconstruir el grafo de objetos.
    }

    /** Escribe el estado completo; se invoca después de cada operación que modifica datos. */
    public void guardar() {
        // TODO (Etapa 2): escribir un archivo por entidad con try/catch/finally (requisito de excepciones).
    }

    public Path getCarpetaData() { return carpetaData; }
}
