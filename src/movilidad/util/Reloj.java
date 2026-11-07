package movilidad.util;

import java.time.LocalDateTime;

/**
 * Fuente de fecha y hora del sistema (S21). Se inyecta en los controladores y servicios
 * para poder probar los límites temporales (2 min, 10 min, 120 min, 24 h, 30 días).
 */
public interface Reloj {
    LocalDateTime ahora();
}
