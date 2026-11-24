package evacuacion.estructuras;

/**
 * Lista simplemente enlazada genérica con referencias a la cabeza y al último nodo.
 *
 * <p>Uso en el proyecto: ocupantes de un piso ("¿quién queda en el piso 3?"), ubicaciones de
 * una zona, listas de adyacencia del grafo y bitácoras de eventos. Se recorre nodo a nodo,
 * sin usar arreglos.</p>
 *
 * <table>
 *   <caption>Complejidad</caption>
 *   <tr><td>agregarAlFinal / agregarAlInicio</td><td>O(1)</td></tr>
 *   <tr><td>obtener / contiene / eliminar / buscar</td><td>O(n)</td></tr>
 * </table>
 *
 * @param <T> tipo de los elementos
 */
public class ListaEnlazada<T> {

    private NodoSimple<T> cabeza;
    private NodoSimple<T> ultimo;
    private int tamano;

    /** Inserta al final en O(1) gracias a la referencia {@code ultimo}. */
    public void agregarAlFinal(T dato) {
        NodoSimple<T> nuevo = new NodoSimple<>(dato);
        if (estaVacia()) {
            cabeza = nuevo;
        } else {
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        tamano++;
    }

    /** Inserta al inicio en O(1). */
    public void agregarAlInicio(T dato) {
        cabeza = new NodoSimple<>(dato, cabeza);
        if (ultimo == null) {
            ultimo = cabeza;
        }
        tamano++;
    }

    /** Devuelve el elemento en la posición indicada (0 = cabeza). O(n). */
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Índice " + indice + " fuera de rango [0, " + tamano + ")");
        }
        NodoSimple<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    /** @return el primer elemento de la lista. */
    public T primero() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La lista está vacía");
        }
        return cabeza.dato;
    }

    /**
     * Elimina la primera aparición del elemento (comparando con {@code equals}).
     *
     * @return {@code true} si se encontró y eliminó
     */
    public boolean eliminar(T dato) {
        NodoSimple<T> anterior = null;
        NodoSimple<T> actual = cabeza;
        while (actual != null && !sonIguales(actual.dato, dato)) {
            anterior = actual;
            actual = actual.siguiente;
        }
        if (actual == null) {
            return false;
        }
        if (anterior == null) {
            cabeza = actual.siguiente;
        } else {
            anterior.siguiente = actual.siguiente;
        }
        if (actual == ultimo) {
            ultimo = anterior;
        }
        tamano--;
        return true;
    }

    public boolean contiene(T dato) {
        for (NodoSimple<T> n = cabeza; n != null; n = n.siguiente) {
            if (sonIguales(n.dato, dato)) {
                return true;
            }
        }
        return false;
    }

    /** @return el primer elemento que cumple la condición, o {@code null} si ninguno la cumple. */
    public T buscar(Condicion<? super T> condicion) {
        for (NodoSimple<T> n = cabeza; n != null; n = n.siguiente) {
            if (condicion.cumple(n.dato)) {
                return n.dato;
            }
        }
        return null;
    }

    /** @return una lista nueva con los elementos que cumplen la condición (conserva el orden). */
    public ListaEnlazada<T> filtrar(Condicion<? super T> condicion) {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        for (NodoSimple<T> n = cabeza; n != null; n = n.siguiente) {
            if (condicion.cumple(n.dato)) {
                resultado.agregarAlFinal(n.dato);
            }
        }
        return resultado;
    }

    /** @return cuántos elementos cumplen la condición. */
    public int contar(Condicion<? super T> condicion) {
        int total = 0;
        for (NodoSimple<T> n = cabeza; n != null; n = n.siguiente) {
            if (condicion.cumple(n.dato)) {
                total++;
            }
        }
        return total;
    }

    /** Ejecuta la acción sobre cada elemento, de la cabeza al final. */
    public void paraCada(Accion<? super T> accion) {
        for (NodoSimple<T> n = cabeza; n != null; n = n.siguiente) {
            accion.ejecutar(n.dato);
        }
    }

    /** @return un iterador que recorre la lista desde la cabeza. */
    public Iterador<T> iterador() {
        return new Iterador<T>() {
            private NodoSimple<T> actual = cabeza;

            @Override
            public boolean tieneSiguiente() {
                return actual != null;
            }

            @Override
            public T siguiente() {
                if (actual == null) {
                    throw new EstructuraVaciaException("No quedan elementos en la lista");
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public void limpiar() {
        cabeza = null;
        ultimo = null;
        tamano = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (NodoSimple<T> n = cabeza; n != null; n = n.siguiente) {
            sb.append(n.dato);
            if (n.siguiente != null) {
                sb.append(", ");
            }
        }
        return sb.append(']').toString();
    }

    private static boolean sonIguales(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
