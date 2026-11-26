package evacuacion.modelo;

/** Utilidades de formato de texto compartidas por reportes, consola e interfaz gráfica. */
public final class Formato {

    private Formato() {
    }

    /** Muestra un tiempo en segundos sin decimales innecesarios: 31.0 → "31 s", 7.5 → "7.5 s". */
    public static String tiempo(double segundos) {
        if (Double.isInfinite(segundos)) {
            return "∞";
        }
        if (segundos == Math.rint(segundos)) {
            return (long) segundos + " s";
        }
        return String.format("%.1f s", segundos);
    }

    /** Rellena con espacios a la derecha hasta el ancho indicado (o recorta si sobra). */
    public static String ajustar(String texto, int ancho) {
        if (texto.length() >= ancho) {
            return texto.substring(0, ancho);
        }
        StringBuilder sb = new StringBuilder(texto);
        while (sb.length() < ancho) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /** Repite un carácter {@code veces} veces (para líneas separadoras). */
    public static String linea(char caracter, int veces) {
        StringBuilder sb = new StringBuilder(veces);
        for (int i = 0; i < veces; i++) {
            sb.append(caracter);
        }
        return sb.toString();
    }
}
