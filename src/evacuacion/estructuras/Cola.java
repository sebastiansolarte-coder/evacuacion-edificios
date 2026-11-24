package evacuacion.estructuras;

/**
 * Cola FIFO enlazada (primero en entrar, primero en salir).
 *
 * <p>Uso en el proyecto: personas esperando en la puerta de una escalera o de una salida.
 * Modela el <b>cuello de botella</b>: en cada turno solo pasan tantas personas como la
 * capacidad de la puerta, y siempre en orden de llegada.</p>
 *
 * <p>Todas las operaciones principales son O(1).</p>
 *
 * @param <T> tipo de los elementos
 */
public class Cola<T> {

    private NodoSimple<T> frente;
    private NodoSimple<T> fin;
    private int tamano;

    /** Agrega al final de la cola. O(1). */
    public void encolar(T dato) {
        NodoSimple<T> nuevo = new NodoSimple<>(dato);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamano++;
    }

    /** Extrae el elemento del frente. O(1). */
    public T desencolar() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola está vacía");
        }
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamano--;
        return dato;
    }

    /** Consulta el frente sin extraerlo. O(1). */
    public T verFrente() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola está vacía");
        }
        return frente.dato;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int tamano() {
        return tamano;
    }

    public void limpiar() {
        frente = null;
        fin = null;
        tamano = 0;
    }

    /** Recorre la cola del frente al fin sin modificarla (útil para dibujarla). */
    public Iterador<T> iterador() {
        return new Iterador<T>() {
            private NodoSimple<T> actual = frente;

            @Override
            public boolean tieneSiguiente() {
                return actual != null;
            }

            @Override
            public T siguiente() {
                if (actual == null) {
                    throw new EstructuraVaciaException("No quedan elementos en la cola");
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("frente -> [");
        for (NodoSimple<T> n = frente; n != null; n = n.siguiente) {
            sb.append(n.dato);
            if (n.siguiente != null) {
                sb.append(", ");
            }
        }
        return sb.append("] <- fin").toString();
    }
}
