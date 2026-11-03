package movilidad.dominio.mantenimiento;

import movilidad.dominio.vehiculos.Vehiculo;

import java.util.Comparator;

/** Orden personalizado (Comparator): vehículos con más reportes de falla primero (S24). */
public class ComparadorFallasVehiculo implements Comparator<Vehiculo> {
    @Override
    public int compare(Vehiculo a, Vehiculo b) {
        int porFallas = Integer.compare(b.cantidadFallas(), a.cantidadFallas());
        return porFallas != 0 ? porFallas : a.getCodigo().compareTo(b.getCodigo());
    }
}
