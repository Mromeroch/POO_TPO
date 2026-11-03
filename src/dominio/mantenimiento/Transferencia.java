package movilidad.dominio.mantenimiento;

import movilidad.dominio.Estacion;
import movilidad.dominio.personal.Operador;
import movilidad.dominio.vehiculos.Vehiculo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Traslado de vehículos entre estaciones (RF13, S20): origen, destino, vehículos y operador responsable. */
public class Transferencia {
    private final LocalDateTime fecha;
    private final Estacion origen;
    private final Estacion destino;
    private final Operador responsable;
    private final List<Vehiculo> vehiculos;

    public Transferencia(LocalDateTime fecha, Estacion origen, Estacion destino,
                         Operador responsable, List<Vehiculo> vehiculos) {
        this.fecha = fecha;
        this.origen = origen;
        this.destino = destino;
        this.responsable = responsable;
        this.vehiculos = new ArrayList<>(vehiculos);
    }

    public LocalDateTime getFecha() { return fecha; }
    public Estacion getOrigen() { return origen; }
    public Estacion getDestino() { return destino; }
    public Operador getResponsable() { return responsable; }
    public List<Vehiculo> getVehiculos() { return Collections.unmodifiableList(vehiculos); }
}
