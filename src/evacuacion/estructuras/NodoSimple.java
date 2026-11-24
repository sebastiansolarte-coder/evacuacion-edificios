package evacuacion.estructuras;

/**
 * Nodo de enlace simple compartido por {@link ListaEnlazada}, {@link Cola} y {@link Pila}.
 * <p>Es de visibilidad de paquete: los usuarios de las estructuras nunca lo manipulan.</p>
 *
 * @param <T> tipo del dato almacenado
 */
final class NodoSimple<T> {

    T dato;
    NodoSimple<T> siguiente;

    NodoSimple(T dato) {
        this(dato, null);
    }

    NodoSimple(T dato, NodoSimple<T> siguiente) {
        this.dato = dato;
        this.siguiente = siguiente;
    }
}
