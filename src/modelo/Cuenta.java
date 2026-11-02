package modelo;

import java.util.ArrayList;
import java.util.List;

public class Cuenta {
    private double saldo;
    private List<MovimientoCuenta> movimientos;

    public Cuenta(double saldo) {
        this.saldo = saldo;
        this.movimientos = new ArrayList<>();
    }
    public double getSaldo() {
        return saldo;
    }

    public void recargar(double monto) {
        if (monto > 0) {
            this.saldo += monto;
        }
    }

    public void debitar(double monto) {
        this.saldo -= monto;
    }

    public List<MovimientoCuenta> getMovimientos() {
        return movimientos;
    }
}
