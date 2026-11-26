package evacuacion.algoritmos;

import evacuacion.modelo.Ubicacion;

/**
 * Distancias mínimas y predecesores calculados por Dijkstra desde un origen.
 * {@code previos[v]} es el índice del nodo desde el que se llega a {@code v} por el camino
 * mínimo (−1 para el origen o nodos inalcanzables).
 */
public class ResultadoBusqueda {

    private final double[] distancias;
    private final int[] previos;

    ResultadoBusqueda(double[] distancias, int[] previos) {
        this.distancias = distancias;
        this.previos = previos;
    }

    public double distanciaA(Ubicacion destino) {
        return distancias[destino.getIndice()];
    }

    public boolean esAlcanzable(Ubicacion destino) {
        return !Double.isInfinite(distancias[destino.getIndice()]);
    }

    int[] previos() {
        return previos;
    }
}
