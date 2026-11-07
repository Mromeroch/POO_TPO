package movilidad.dominio;

import movilidad.dominio.mantenimiento.AlertaEstacion;
import movilidad.dominio.mantenimiento.TipoAlerta;
import movilidad.dominio.vehiculos.Vehiculo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Estación (GRASP Experto): conoce su capacidad, los vehículos alojados y su ocupación.
 * Relación de agregación con Vehiculo: el vehículo puede estar en uso fuera de la estación.
 */
public class Estacion {
    public static final double UMBRAL_VACIA = 0.20;       // RN09
    public static final double UMBRAL_SATURADA = 0.90;    // RN09

    private final String codigo;
    private final String direccion;
    private final int capacidadMaxima;
    private final List<Vehiculo> vehiculos = new ArrayList<>();

    public Estacion(String codigo, String direccion, int capacidadMaxima) {
        if (capacidadMaxima <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a cero");
        }
        this.codigo = codigo;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
    }

    public boolean estaLlena() {
        return vehiculos.size() >= capacidadMaxima;
    }

    /** RN07: no se puede ingresar un vehículo en una estación llena. Quien llama debe verificar antes. */
    public void ingresar(Vehiculo vehiculo) {
        if (estaLlena()) {
            throw new IllegalStateException("La estación " + codigo + " está llena");
        }
        vehiculos.add(vehiculo);
    }

    public void egresar(Vehiculo vehiculo) {
        vehiculos.remove(vehiculo);
    }

    public boolean contiene(Vehiculo vehiculo) {
        return vehiculos.contains(vehiculo);
    }

    public Optional<Vehiculo> buscarVehiculo(String codigoVehiculo) {
        return vehiculos.stream().filter(v -> v.getCodigo().equalsIgnoreCase(codigoVehiculo)).findFirst();
    }

    /** S15: ocupación = vehículos guardados / capacidad. */
    public double ocupacion() {
        return (double) vehiculos.size() / capacidadMaxima;
    }

    /** RN09: menos del 20 % o más del 90 %. */
    public boolean requiereRedistribucion() {
        return ocupacion() < UMBRAL_VACIA || ocupacion() > UMBRAL_SATURADA;
    }

    /** Devuelve la alerta correspondiente si la ocupación está fuera de rango (S15). */
    public Optional<AlertaEstacion> evaluarAlerta(LocalDateTime fecha) {
        if (ocupacion() < UMBRAL_VACIA) {
            return Optional.of(new AlertaEstacion(this, TipoAlerta.VACIA, fecha));
        }
        if (ocupacion() > UMBRAL_SATURADA) {
            return Optional.of(new AlertaEstacion(this, TipoAlerta.SATURADA, fecha));
        }
        return Optional.empty();
    }

    public String getCodigo() { return codigo; }
    public String getDireccion() { return direccion; }
    public int getCapacidadMaxima() { return capacidadMaxima; }
    public int cantidadVehiculos() { return vehiculos.size(); }
    public List<Vehiculo> getVehiculos() { return Collections.unmodifiableList(vehiculos); }

    @Override
    public String toString() { return codigo + " - " + direccion; }
}
