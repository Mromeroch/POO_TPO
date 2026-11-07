package movilidad;

import movilidad.dominio.Estacion;
import movilidad.dominio.Usuario;
import movilidad.dominio.pagos.BilleteraVirtual;
import movilidad.dominio.pagos.Tarjeta;
import movilidad.dominio.personal.Administrador;
import movilidad.dominio.personal.Operador;
import movilidad.dominio.personal.Tecnico;
import movilidad.dominio.vehiculos.BicicletaComun;
import movilidad.dominio.vehiculos.BicicletaElectrica;
import movilidad.dominio.vehiculos.MonopatinElectrico;
import movilidad.dominio.vehiculos.Vehiculo;
import movilidad.persistencia.RepositorioMemoria;
import movilidad.util.Dinero;
import movilidad.util.Reloj;

import java.time.LocalDate;

/** Datos de prueba que se cargan solo la primera vez, si la carpeta de datos está vacía (ver README). */
public final class DatosIniciales {
    private DatosIniciales() {
    }

    public static void cargarSiVacio(RepositorioMemoria repo, Reloj reloj) {
        if (!repo.buscarEstaciones().isEmpty() || !repo.buscarEmpleados().isEmpty()) {
            return;
        }
        LocalDate alta = LocalDate.of(2026, 1, 1);

        Estacion e01 = new Estacion("E01", "Plaza Central", 10);
        Estacion e02 = new Estacion("E02", "Terminal de Ómnibus", 6);
        Estacion e03 = new Estacion("E03", "Parque Norte", 8);
        Vehiculo[] enE01 = {new BicicletaComun("B-001", alta, 26), new BicicletaComun("B-002", alta, 28),
                new BicicletaElectrica("E-001", alta, 60), new MonopatinElectrico("M-001", alta, 25)};
        Vehiculo[] enE02 = {new BicicletaComun("B-003", alta, 26), new BicicletaElectrica("E-002", alta, 60),
                new MonopatinElectrico("M-002", alta, 25)};
        Vehiculo[] enE03 = {new MonopatinElectrico("M-003", alta, 25), new BicicletaComun("B-004", alta, 24)};
        for (Vehiculo v : enE01) { e01.ingresar(v); repo.guardar(v); }
        for (Vehiculo v : enE02) { e02.ingresar(v); repo.guardar(v); }
        for (Vehiculo v : enE03) { e03.ingresar(v); repo.guardar(v); }
        repo.guardar(e01);
        repo.guardar(e02);
        repo.guardar(e03);

        repo.guardar(new Operador("OP-01", "Laura Gómez", "1155550001"));
        repo.guardar(new Tecnico("TEC-01", "Carlos Pérez", "1155550002"));
        repo.guardar(new Administrador("ADM-01", "Marta Díaz", "1155550003"));

        Usuario juan = new Usuario("Juan Pérez", "30111222", "juan@mail.com", reloj.ahora().toLocalDate());
        juan.getCuenta().asociarMedio(new Tarjeta("4111111111111111"));
        juan.getCuenta().recargar(Dinero.de("20000"), reloj.ahora());
        Usuario ana = new Usuario("Ana López", "28999888", "ana@mail.com", reloj.ahora().toLocalDate());
        ana.getCuenta().asociarMedio(new BilleteraVirtual("ana.lopez.mp"));
        repo.guardar(juan);
        repo.guardar(ana);
    }
}
