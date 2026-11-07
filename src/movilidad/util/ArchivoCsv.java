package movilidad.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Lectura y escritura de archivos de texto separados por ';' (S23).
 * Escapa los caracteres especiales para que cualquier texto se pueda guardar sin romper el formato. */
public final class ArchivoCsv {
    private ArchivoCsv() {
    }

    public static String escapar(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace(";", "\\p").replace("\r", "").replace("\n", "\\n");
    }

    public static String desescapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char siguiente = s.charAt(++i);
                sb.append(siguiente == 'p' ? ';' : siguiente == 'n' ? '\n' : siguiente);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Escribe el archivo completo: primero en uno temporal y después lo reemplaza, para no dejarlo a medias. */
    public static void escribir(Path ruta, String encabezado, List<String[]> filas) throws IOException {
        Path temporal = ruta.resolveSibling(ruta.getFileName() + ".tmp");
        BufferedWriter escritor = null;
        try {
            escritor = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8);
            escritor.write(encabezado);
            escritor.newLine();
            for (String[] fila : filas) {
                StringBuilder linea = new StringBuilder();
                for (int i = 0; i < fila.length; i++) {
                    linea.append(i == 0 ? "" : ";").append(escapar(fila[i]));
                }
                escritor.write(linea.toString());
                escritor.newLine();
            }
            escritor.close();
            escritor = null;
            Files.move(temporal, ruta, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            if (escritor != null) {
                escritor.close();
            }
            Files.deleteIfExists(temporal);
        }
    }

    /** Lee las filas de datos (sin el encabezado). Si el archivo no existe devuelve una lista vacía. */
    public static List<String[]> leer(Path ruta) throws IOException {
        List<String[]> filas = new ArrayList<>();
        if (!Files.exists(ruta)) {
            return filas;
        }
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        for (int i = 1; i < lineas.size(); i++) {
            if (lineas.get(i).isBlank()) {
                continue;
            }
            String[] celdas = lineas.get(i).split(";", -1);
            for (int j = 0; j < celdas.length; j++) {
                celdas[j] = desescapar(celdas[j]);
            }
            filas.add(celdas);
        }
        return filas;
    }

    /** Une varios textos en una sola celda (separador '|', con codificación porcentual). */
    public static String unirLista(List<String> elementos) {
        List<String> codificados = new ArrayList<>();
        for (String e : elementos) {
            codificados.add(e.replace("%", "%25").replace("|", "%7C"));
        }
        return String.join("|", codificados);
    }

    public static List<String> separarLista(String celda) {
        List<String> resultado = new ArrayList<>();
        if (celda == null || celda.isEmpty()) {
            return resultado;
        }
        for (String parte : celda.split("\\|", -1)) {
            resultado.add(parte.replace("%7C", "|").replace("%25", "%"));
        }
        return resultado;
    }
}
