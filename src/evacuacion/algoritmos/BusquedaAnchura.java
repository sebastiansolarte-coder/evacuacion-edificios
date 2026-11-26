package evacuacion.algoritmos;

import evacuacion.estructuras.Cola;
import evacuacion.estructuras.Iterador;
import evacuacion.modelo.Arista;
import evacuacion.modelo.Grafo;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.Ubicacion;

/**
 * Búsqueda en anchura (BFS): encuentra la salida alcanzable con el <b>menor número de pasos</b>
 * (aristas), sin considerar los tiempos.
 *
 * <p>Usa la {@link Cola} propia: los nodos se exploran por niveles, así la primera salida que
 * se desencola es la más cercana en cantidad de tramos. Complejidad O(V + E).</p>
 *
 * <p>Sirve para comparar con Dijkstra: BFS puede elegir un camino con menos tramos pero más
 * lento (por ejemplo, un pasillo largo frente a una escalera rápida).</p>
 */
public class BusquedaAnchura implements EstrategiaRuta {

    private static final String NOMBRE = "BFS";

    @Override
    public String nombre() {
        return NOMBRE;
    }

    @Override
    public Ruta rutaASalidaMasCercana(Grafo grafo, Ubicacion origen) {
        int n = grafo.cantidadUbicaciones();
        boolean[] visitado = new boolean[n];
        int[] previos = new int[n];
        for (int i = 0; i < n; i++) {
            previos[i] = -1;
        }

        Cola<Ubicacion> porVisitar = new Cola<>();
        visitado[origen.getIndice()] = true;
        porVisitar.encolar(origen);

        while (!porVisitar.estaVacia()) {
            Ubicacion actual = porVisitar.desencolar();
            if (actual.esSalida()) {
                return ConstructorRuta.construir(grafo, previos, origen, actual, NOMBRE);
            }
            Iterador<Arista> it = grafo.aristasDe(actual).iterador();
            while (it.tieneSiguiente()) {
                Arista arista = it.siguiente();
                if (arista.estaBloqueada()) {
                    continue;
                }
                Ubicacion vecino = arista.otroExtremo(actual);
                if (!visitado[vecino.getIndice()]) {
                    visitado[vecino.getIndice()] = true;
                    previos[vecino.getIndice()] = actual.getIndice();
                    porVisitar.encolar(vecino);
                }
            }
        }
        return Ruta.inexistente(origen, NOMBRE);
    }
}
