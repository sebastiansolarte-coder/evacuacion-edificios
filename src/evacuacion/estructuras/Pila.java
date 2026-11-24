package evacuacion.estructuras;

/**
 * Pila LIFO enlazada (último en entrar, primero en salir).
 *
 * <p>Uso en el proyecto:</p>
 * <ul>
 *   <li>Reconstruir una ruta: Dijkstra/BFS guardan el predecesor de cada nodo; se camina
 *       <b>hacia atrás</b> desde la salida apilando nodos, de modo que el tope queda en el origen
 *       y al desapilar se obtienen los pasos en el orden correcto.</li>
 *   <li>Instrucciones ("Baje por la Escalera B → Avance por el pasillo → Salga por la Salida A").</li>
 *   <li>Historial de rutas calculadas (la última consultada queda en el tope).</li>
 * </ul>
 *
 * <p>apilar, desapilar y verTope son O(1).</p>
 *
 * @param <T> tipo de los elementos
 */
public class Pila<T> {

    private NodoSimple<T> tope;
    private int tamano;

    /** Coloca un elemento en el tope. O(1). */
    public void apilar(T dato) {
        tope = new NodoSimple<>(dato, tope);
        tamano++;
    }

    /** Extrae el elemento del tope. O(1). */
    public T desapilar() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La pila está vacía");
        }
        T dato = tope.dato;
        tope = tope.siguiente;
        tamano--;
        return dato;
    }

    /** Consulta el tope sin extraerlo. O(1). */
    public T verTope() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La pila está vacía");
        }
        return tope.dato;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int tamano() {
        return tamano;
    }

    public void limpiar() {
        tope = null;
        tamano = 0;
    }

    /**
     * Crea una copia independiente con el mismo orden (el tope de la copia es el mismo
     * elemento que el tope original). Los elementos no se clonan. O(n).
     */
    public Pila<T> copiar() {
        Pila<T> copia = new Pila<>();
        NodoSimple<T> ultimoCopiado = null;
        for (NodoSimple<T> n = tope; n != null; n = n.siguiente) {
            NodoSimple<T> nuevo = new NodoSimple<>(n.dato);
            if (ultimoCopiado == null) {
                copia.tope = nuevo;
            } else {
                ultimoCopiado.siguiente = nuevo;
            }
            ultimoCopiado = nuevo;
        }
        copia.tamano = tamano;
        return copia;
    }

    /** Recorre la pila del tope hacia el fondo sin modificarla. */
    public Iterador<T> iterador() {
        return new Iterador<T>() {
            private NodoSimple<T> actual = tope;

            @Override
            public boolean tieneSiguiente() {
                return actual != null;
            }

            @Override
            public T siguiente() {
                if (actual == null) {
                    throw new EstructuraVaciaException("No quedan elementos en la pila");
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("tope -> [");
        for (NodoSimple<T> n = tope; n != null; n = n.siguiente) {
            sb.append(n.dato);
            if (n.siguiente != null) {
                sb.append(", ");
            }
        }
        return sb.append(']').toString();
    }
}
