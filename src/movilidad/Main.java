package movilidad;

import movilidad.interfaz.InterfazConsola;
import movilidad.persistencia.RepositorioArchivos;
import movilidad.util.Reloj;
import movilidad.util.RelojSistema;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String carpeta = args.length > 0 ? args[0] : "data";
        Reloj reloj = new RelojSistema();

        RepositorioArchivos repositorio = new RepositorioArchivos(carpeta);
        repositorio.cargar();                                  // S23: se leen los datos guardados
        DatosIniciales.cargarSiVacio(repositorio, reloj);      // solo la primera vez
        repositorio.guardar();

        Sistema sistema = new Sistema(reloj, repositorio);
        sistema.cargarConfiguracionPorDefecto();

        int perdidos = sistema.getControladorOperaciones().detectarPerdidos().size();   // S07: al iniciar
        PrintStream salida = new PrintStream(System.out, true, "UTF-8");
        if (perdidos > 0) {
            salida.println("Atención: se detectaron " + perdidos + " vehículo(s) perdido(s) al iniciar.");
        }
        BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        new InterfazConsola(sistema, entrada, salida).iniciar();
    }
}
