package movilidad.persistencia;

import movilidad.dominio.mantenimiento.NotaAjuste;
import movilidad.dominio.mantenimiento.OrdenMantenimiento;
import movilidad.dominio.mantenimiento.Transferencia;

import java.util.List;
import java.util.Optional;

/** Órdenes de mantenimiento, traslados y notas de ajuste. (No figura en el diagrama final: ver docs/NOTAS_DE_DISENO.md.) */
public interface RepositorioMantenimiento {
    long siguienteIdOrden();

    void guardarOrden(OrdenMantenimiento orden);

    Optional<OrdenMantenimiento> buscarOrdenPorId(long id);

    List<OrdenMantenimiento> buscarOrdenes();

    List<OrdenMantenimiento> buscarOrdenesDeTecnico(String legajo);

    void guardarTransferencia(Transferencia transferencia);

    List<Transferencia> buscarTransferencias();

    void guardarNotaAjuste(NotaAjuste nota);

    List<NotaAjuste> buscarNotasAjuste();
}
