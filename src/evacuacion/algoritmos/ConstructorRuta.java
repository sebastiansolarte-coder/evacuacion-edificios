package evacuacion.algoritmos;

import evacuacion.estructuras.Pila;
import evacuacion.modelo.Arista;
import evacuacion.modelo.Grafo;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.Ubicacion;

/**
 * Reconstruye una {@link Ruta} a partir del arreglo de predecesores que dejan BFS y Dijkstra.
 *
 * <p>Se camina <b>hacia atrás</b> desde la salida hasta el origen apilando cada nodo; al
 * terminar, el tope de la pila es el origen y la ruta queda en el orden correcto.
 * Complejidad O(L · grado), con L = longitud de la ruta.</p>
 */
final class ConstructorRuta {

    private ConstructorRuta() {
    }

    static Ruta construir(Grafo grafo, int[] previos, Ubicacion origen, Ubicacion destino, String algoritmo) {
        Pila<Ubicacion> pasos = new Pila<>();
        double tiempoReal = 0;
        boolean cruzaBloqueos = false;

        Ubicacion actual = destino;
        pasos.apilar(actual);
        while (actual != origen) {
            Ubicacion anterior = grafo.ubicacionEn(previos[actual.getIndice()]);
            Arista tramo = grafo.buscarArista(anterior, actual);
            tiempoReal += tramo.getTiempo();
            cruzaBloqueos |= tramo.estaBloqueada();
            pasos.apilar(anterior);
            actual = anterior;
        }
        return new Ruta(pasos, origen, destino, tiempoReal, algoritmo, cruzaBloqueos);
    }
}
