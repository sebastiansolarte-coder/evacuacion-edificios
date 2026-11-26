package evacuacion.algoritmos;

import evacuacion.estructuras.Iterador;
import evacuacion.estructuras.MonticuloMinimo;
import evacuacion.modelo.Arista;
import evacuacion.modelo.Grafo;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.Ubicacion;

/**
 * Algoritmo de Dijkstra: camino de <b>menor tiempo</b> desde un origen a todas las ubicaciones.
 *
 * <p>Para elegir la salida se calculan las distancias a todas las salidas en una sola
 * ejecución y se toma la de menor tiempo ("Dijkstra a múltiples salidas").</p>
 *
 * <p>Implementación con montículo binario propio y <i>eliminación perezosa</i>: cuando una
 * distancia mejora se inserta otra entrada en el montículo y las entradas viejas se descartan
 * al extraerlas (arreglo {@code cerrado}). Complejidad O((V + E) log V).</p>
 *
 * <p>Ruta de emergencia: si no existe una ruta libre, se repite el cálculo permitiendo cruzar
 * conexiones bloqueadas con su tiempo multiplicado por {@link #PENALIZACION_BLOQUEO}; así se
 * sugiere el camino que atraviesa la menor cantidad posible de humo.</p>
 */
public class Dijkstra implements EstrategiaRuta {

    /** Factor con el que se encarece cruzar una conexión bloqueada en la ruta de emergencia. */
    public static final double PENALIZACION_BLOQUEO = 10.0;

    private static final String NOMBRE = "Dijkstra";
    private static final String NOMBRE_EMERGENCIA = "Dijkstra de emergencia";

    @Override
    public String nombre() {
        return NOMBRE;
    }

    @Override
    public Ruta rutaASalidaMasCercana(Grafo grafo, Ubicacion origen) {
        return mejorRutaASalida(grafo, origen, false, NOMBRE);
    }

    /** Ruta alternativa que puede atravesar conexiones bloqueadas (penalizadas). */
    public Ruta rutaDeEmergencia(Grafo grafo, Ubicacion origen) {
        return mejorRutaASalida(grafo, origen, true, NOMBRE_EMERGENCIA);
    }

    /**
     * Ejecuta Dijkstra completo desde {@code origen}.
     *
     * @param permitirBloqueadas si es {@code true}, las aristas bloqueadas se usan con penalización
     */
    public ResultadoBusqueda calcularDistancias(Grafo grafo, Ubicacion origen, boolean permitirBloqueadas) {
        int n = grafo.cantidadUbicaciones();
        double[] distancias = new double[n];
        int[] previos = new int[n];
        boolean[] cerrado = new boolean[n];
        for (int i = 0; i < n; i++) {
            distancias[i] = Double.POSITIVE_INFINITY;
            previos[i] = -1;
        }
        distancias[origen.getIndice()] = 0;

        MonticuloMinimo<Ubicacion> pendientes = new MonticuloMinimo<>();
        pendientes.insertar(origen, 0);

        while (!pendientes.estaVacio()) {
            Ubicacion actual = pendientes.extraerMinimo();
            int u = actual.getIndice();
            if (cerrado[u]) {
                continue; // entrada obsoleta: ya se encontró un camino mejor
            }
            cerrado[u] = true;
            relajarVecinos(grafo, actual, permitirBloqueadas, distancias, previos, cerrado, pendientes);
        }
        return new ResultadoBusqueda(distancias, previos);
    }

    private void relajarVecinos(Grafo grafo, Ubicacion actual, boolean permitirBloqueadas,
                                double[] distancias, int[] previos, boolean[] cerrado,
                                MonticuloMinimo<Ubicacion> pendientes) {
        int u = actual.getIndice();
        Iterador<Arista> it = grafo.aristasDe(actual).iterador();
        while (it.tieneSiguiente()) {
            Arista arista = it.siguiente();
            if (arista.estaBloqueada() && !permitirBloqueadas) {
                continue;
            }
            Ubicacion vecino = arista.otroExtremo(actual);
            int v = vecino.getIndice();
            double nuevaDistancia = distancias[u] + costo(arista);
            if (!cerrado[v] && nuevaDistancia < distancias[v]) {
                distancias[v] = nuevaDistancia;
                previos[v] = u;
                pendientes.insertar(vecino, nuevaDistancia);
            }
        }
    }

    private double costo(Arista arista) {
        return arista.estaBloqueada() ? arista.getTiempo() * PENALIZACION_BLOQUEO : arista.getTiempo();
    }

    private Ruta mejorRutaASalida(Grafo grafo, Ubicacion origen, boolean permitirBloqueadas, String nombre) {
        ResultadoBusqueda resultado = calcularDistancias(grafo, origen, permitirBloqueadas);
        Ubicacion mejorSalida = null;
        Iterador<Ubicacion> salidas = grafo.salidas().iterador();
        while (salidas.tieneSiguiente()) {
            Ubicacion salida = salidas.siguiente();
            if (resultado.esAlcanzable(salida)
                    && (mejorSalida == null || resultado.distanciaA(salida) < resultado.distanciaA(mejorSalida))) {
                mejorSalida = salida;
            }
        }
        if (mejorSalida == null) {
            return Ruta.inexistente(origen, nombre);
        }
        return ConstructorRuta.construir(grafo, resultado.previos(), origen, mejorSalida, nombre);
    }
}
