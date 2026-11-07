package movilidad.util;

import java.time.LocalDateTime;

/** Reloj controlable para pruebas unitarias: la hora solo cambia cuando el test lo indica. */
public class RelojPrueba implements Reloj {
    private LocalDateTime actual;

    public RelojPrueba(LocalDateTime inicial) {
        this.actual = inicial;
    }

    @Override
    public LocalDateTime ahora() {
        return actual;
    }

    public void fijar(LocalDateTime nueva) {
        this.actual = nueva;
    }

    public void avanzarMinutos(long minutos) {
        this.actual = actual.plusMinutes(minutos);
    }

    public void avanzarHoras(long horas) {
        this.actual = actual.plusHours(horas);
    }
}
