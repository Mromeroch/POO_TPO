package movilidad.util;

import java.time.LocalDateTime;

/** Reloj real: devuelve la hora actual de la máquina. */
public class RelojSistema implements Reloj {
    @Override
    public LocalDateTime ahora() {
        return LocalDateTime.now();
    }
}
