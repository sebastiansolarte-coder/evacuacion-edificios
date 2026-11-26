package evacuacion.algoritmos;

import evacuacion.modelo.Formato;
import evacuacion.modelo.Grafo;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.Ubicacion;

/**
 * Compara BFS (menos pasos) contra Dijkstra (menos tiempo) desde un mismo origen.
 */
public class ComparacionRutas {

    private final Ruta rutaDijkstra;
    private final Ruta rutaBfs;

    private ComparacionRutas(Ruta rutaDijkstra, Ruta rutaBfs) {
        this.rutaDijkstra = rutaDijkstra;
        this.rutaBfs = rutaBfs;
    }

    public static ComparacionRutas comparar(Grafo grafo, Ubicacion origen) {
        return new ComparacionRutas(
                new Dijkstra().rutaASalidaMasCercana(grafo, origen),
                new BusquedaAnchura().rutaASalidaMasCercana(grafo, origen));
    }

    public Ruta getRutaDijkstra() {
        return rutaDijkstra;
    }

    public Ruta getRutaBfs() {
        return rutaBfs;
    }

    /** Texto con ambas rutas y una conclusión. */
    public String informe() {
        StringBuilder sb = new StringBuilder();
        sb.append("COMPARACIÓN BFS vs DIJKSTRA desde ").append(rutaDijkstra.getOrigen().descripcion()).append('\n');
        sb.append("  • ").append(rutaDijkstra.resumen()).append('\n');
        sb.append("  • ").append(rutaBfs.resumen()).append('\n');
        sb.append("  Conclusión: ").append(conclusion()).append('\n');
        return sb.toString();
    }

    private String conclusion() {
        if (!rutaDijkstra.existe()) {
            return "ningún algoritmo encuentra una salida libre de bloqueos.";
        }
        if (rutaDijkstra.descripcion().equals(rutaBfs.descripcion())) {
            return "ambos algoritmos eligen el mismo camino.";
        }
        double tiempoAhorrado = rutaBfs.getTiempoTotal() - rutaDijkstra.getTiempoTotal();
        int tramosExtra = rutaDijkstra.numeroTramos() - rutaBfs.numeroTramos();
        if (tiempoAhorrado <= 0) {
            return "las rutas difieren pero tardan lo mismo.";
        }
        return "Dijkstra ahorra " + Formato.tiempo(tiempoAhorrado)
                + (tramosExtra > 0 ? " aunque recorre " + tramosExtra + " tramo(s) más" : "")
                + ". BFS solo cuenta pasos; en una evacuación importa el tiempo, por eso se usa Dijkstra.";
    }
}
