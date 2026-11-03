package movilidad.servicios;

import movilidad.dominio.Usuario;
import movilidad.dominio.Viaje;
import movilidad.dominio.tarifas.ResultadoTarifa;

/**
 * Contrato para evaluar promociones (DIP, §5.1.3 del informe): ControladorViajes depende de esta
 * abstracción y no de ServicioPromociones. (No figura en el diagrama final: ver docs/NOTAS_DE_DISENO.md.)
 */
public interface EvaluadorPromociones {
    /** Calcula el costo normal y el de cada promoción aplicable y devuelve el menor (RN14, S05). */
    ResultadoTarifa calcularCostoViaje(Viaje viaje, Usuario usuario);
}

