package evacuacion.algoritmos;

import evacuacion.modelo.Grafo;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.Ubicacion;

/**
 * Contrato común de los algoritmos de búsqueda de rutas (patrón Estrategia).
 * El simulador y el servicio trabajan contra esta interfaz, de modo que se puede cambiar
 * Dijkstra por BFS (u otro algoritmo) sin modificar el resto del código.
 */
public interface EstrategiaRuta {

    /** Nombre legible del algoritmo para reportes. */
    String nombre();

    /**
     * Calcula la ruta desde {@code origen} hasta la salida más conveniente, ignorando las
     * conexiones bloqueadas.
     *
     * @return la ruta encontrada o {@link Ruta#inexistente} si no hay camino
     */
    Ruta rutaASalidaMasCercana(Grafo grafo, Ubicacion origen);
}
