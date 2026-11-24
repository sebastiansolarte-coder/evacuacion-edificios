package evacuacion.estructuras;

/**
 * Árbol binario de búsqueda (ABB) genérico ordenado por clave.
 *
 * <p>Uso en el proyecto: {@code nivelRiesgo → Zona}. El recorrido <b>inOrden</b> entrega las
 * zonas de menor a mayor riesgo (reporte) y el <b>inOrden inverso</b> de mayor a menor, que es
 * el orden en que se envía la brigada.</p>
 *
 * <p>Se admiten claves repetidas (dos zonas pueden tener el mismo riesgo): se insertan en el
 * subárbol derecho, así el inOrden sigue siendo ordenado y estable.</p>
 *
 * <p>Complejidad: insertar/buscar/eliminar O(h), donde h es la altura
 * (O(log n) si está balanceado, O(n) en el peor caso). Recorridos O(n).</p>
 *
 * @param <K> tipo de la clave (comparable)
 * @param <V> tipo del valor
 */
public class ArbolBinario<K extends Comparable<K>, V> {

    private static final String SANGRIA = "        ";

    private static final class NodoArbol<K, V> {
        private K clave;
        private V valor;
        private NodoArbol<K, V> izquierdo;
        private NodoArbol<K, V> derecho;

        private NodoArbol(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }

    private NodoArbol<K, V> raiz;
    private int tamano;

    /** Inserta un par clave-valor (iterativo, sin recursión). Duplicados van a la derecha. */
    public void insertar(K clave, V valor) {
        validarClave(clave);
        NodoArbol<K, V> nuevo = new NodoArbol<>(clave, valor);
        tamano++;
        if (raiz == null) {
            raiz = nuevo;
            return;
        }
        NodoArbol<K, V> actual = raiz;
        while (true) {
            if (clave.compareTo(actual.clave) < 0) {
                if (actual.izquierdo == null) {
                    actual.izquierdo = nuevo;
                    return;
                }
                actual = actual.izquierdo;
            } else {
                if (actual.derecho == null) {
                    actual.derecho = nuevo;
                    return;
                }
                actual = actual.derecho;
            }
        }
    }

    /** @return el valor de la primera clave igual encontrada, o {@code null}. */
    public V buscar(K clave) {
        validarClave(clave);
        NodoArbol<K, V> actual = raiz;
        while (actual != null) {
            int comparacion = clave.compareTo(actual.clave);
            if (comparacion == 0) {
                return actual.valor;
            }
            actual = comparacion < 0 ? actual.izquierdo : actual.derecho;
        }
        return null;
    }

    /**
     * Elimina un nodo con la clave dada. Si tiene dos hijos se reemplaza por su sucesor
     * inOrden (el mínimo del subárbol derecho).
     *
     * @return el valor eliminado, o {@code null} si la clave no existía
     */
    public V eliminar(K clave) {
        validarClave(clave);
        NodoArbol<K, V> padre = null;
        NodoArbol<K, V> actual = raiz;
        while (actual != null && clave.compareTo(actual.clave) != 0) {
            padre = actual;
            actual = clave.compareTo(actual.clave) < 0 ? actual.izquierdo : actual.derecho;
        }
        if (actual == null) {
            return null;
        }
        V eliminado = actual.valor;
        if (actual.izquierdo != null && actual.derecho != null) {
            eliminarConDosHijos(actual);
        } else {
            NodoArbol<K, V> hijo = actual.izquierdo != null ? actual.izquierdo : actual.derecho;
            reemplazarHijo(padre, actual, hijo);
        }
        tamano--;
        return eliminado;
    }

    /**
     * Reconstruye el árbol con altura mínima en O(n): toma los nodos en inOrden (ya ordenados)
     * y los vuelve a enlazar eligiendo siempre la mediana como raíz de cada subárbol.
     * Evita que el ABB degenere en una lista cuando las claves llegan ordenadas.
     */
    public void rebalancear() {
        NodoArbol<K, V>[] nodos = crearArregloNodos(tamano);
        llenarInOrden(raiz, nodos, 0);
        raiz = enlazarPorMedianas(nodos, 0, tamano - 1);
    }

    /** @return valores de menor a mayor clave (izquierdo, raíz, derecho). */
    public ListaEnlazada<V> inOrden() {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        inOrden(raiz, resultado);
        return resultado;
    }

    /** @return valores de mayor a menor clave (derecho, raíz, izquierdo). */
    public ListaEnlazada<V> inOrdenDescendente() {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        inOrdenDescendente(raiz, resultado);
        return resultado;
    }

    /** @return valores en preorden (raíz, izquierdo, derecho). */
    public ListaEnlazada<V> preOrden() {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        preOrden(raiz, resultado);
        return resultado;
    }

    /** @return valores en postorden (izquierdo, derecho, raíz). */
    public ListaEnlazada<V> postOrden() {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        postOrden(raiz, resultado);
        return resultado;
    }

    /** @return el valor de la clave mínima o {@code null} si el árbol está vacío. */
    public V minimo() {
        if (raiz == null) {
            return null;
        }
        NodoArbol<K, V> actual = raiz;
        while (actual.izquierdo != null) {
            actual = actual.izquierdo;
        }
        return actual.valor;
    }

    /** @return el valor de la clave máxima (la zona más peligrosa) o {@code null}. */
    public V maximo() {
        if (raiz == null) {
            return null;
        }
        NodoArbol<K, V> actual = raiz;
        while (actual.derecho != null) {
            actual = actual.derecho;
        }
        return actual.valor;
    }

    /** @return la altura del árbol (vacío = 0, solo raíz = 1). */
    public int altura() {
        return altura(raiz);
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacio() {
        return tamano == 0;
    }

    public void limpiar() {
        raiz = null;
        tamano = 0;
    }

    /**
     * Dibuja el árbol "acostado": la raíz a la izquierda, el subárbol derecho arriba y el
     * izquierdo abajo. Útil para explicar la estructura en consola.
     */
    public String dibujar() {
        if (raiz == null) {
            return "(árbol vacío)\n";
        }
        StringBuilder sb = new StringBuilder();
        dibujar(raiz, "", sb);
        return sb.toString();
    }

    // ---------------------------------------------------------------- auxiliares recursivos

    private void inOrden(NodoArbol<K, V> nodo, ListaEnlazada<V> destino) {
        if (nodo == null) {
            return;
        }
        inOrden(nodo.izquierdo, destino);
        destino.agregarAlFinal(nodo.valor);
        inOrden(nodo.derecho, destino);
    }

    private void inOrdenDescendente(NodoArbol<K, V> nodo, ListaEnlazada<V> destino) {
        if (nodo == null) {
            return;
        }
        inOrdenDescendente(nodo.derecho, destino);
        destino.agregarAlFinal(nodo.valor);
        inOrdenDescendente(nodo.izquierdo, destino);
    }

    private void preOrden(NodoArbol<K, V> nodo, ListaEnlazada<V> destino) {
        if (nodo == null) {
            return;
        }
        destino.agregarAlFinal(nodo.valor);
        preOrden(nodo.izquierdo, destino);
        preOrden(nodo.derecho, destino);
    }

    private void postOrden(NodoArbol<K, V> nodo, ListaEnlazada<V> destino) {
        if (nodo == null) {
            return;
        }
        postOrden(nodo.izquierdo, destino);
        postOrden(nodo.derecho, destino);
        destino.agregarAlFinal(nodo.valor);
    }

    private int altura(NodoArbol<K, V> nodo) {
        return nodo == null ? 0 : 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    private void dibujar(NodoArbol<K, V> nodo, String prefijo, StringBuilder sb) {
        if (nodo == null) {
            return;
        }
        dibujar(nodo.derecho, prefijo + SANGRIA, sb);
        sb.append(prefijo).append("[").append(nodo.clave).append("] ").append(nodo.valor).append('\n');
        dibujar(nodo.izquierdo, prefijo + SANGRIA, sb);
    }

    private int llenarInOrden(NodoArbol<K, V> nodo, NodoArbol<K, V>[] destino, int posicion) {
        if (nodo == null) {
            return posicion;
        }
        posicion = llenarInOrden(nodo.izquierdo, destino, posicion);
        destino[posicion++] = nodo;
        return llenarInOrden(nodo.derecho, destino, posicion);
    }

    private NodoArbol<K, V> enlazarPorMedianas(NodoArbol<K, V>[] nodos, int inicio, int fin) {
        if (inicio > fin) {
            return null;
        }
        int medio = (inicio + fin) >>> 1;
        NodoArbol<K, V> nodo = nodos[medio];
        nodo.izquierdo = enlazarPorMedianas(nodos, inicio, medio - 1);
        nodo.derecho = enlazarPorMedianas(nodos, medio + 1, fin);
        return nodo;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <K, V> NodoArbol<K, V>[] crearArregloNodos(int cantidad) {
        return (NodoArbol<K, V>[]) new NodoArbol[cantidad];
    }

    private void eliminarConDosHijos(NodoArbol<K, V> nodo) {
        NodoArbol<K, V> padreSucesor = nodo;
        NodoArbol<K, V> sucesor = nodo.derecho;
        while (sucesor.izquierdo != null) {
            padreSucesor = sucesor;
            sucesor = sucesor.izquierdo;
        }
        nodo.clave = sucesor.clave;
        nodo.valor = sucesor.valor;
        if (padreSucesor == nodo) {
            padreSucesor.derecho = sucesor.derecho;
        } else {
            padreSucesor.izquierdo = sucesor.derecho;
        }
    }

    private void reemplazarHijo(NodoArbol<K, V> padre, NodoArbol<K, V> viejo, NodoArbol<K, V> nuevo) {
        if (padre == null) {
            raiz = nuevo;
        } else if (padre.izquierdo == viejo) {
            padre.izquierdo = nuevo;
        } else {
            padre.derecho = nuevo;
        }
    }

    private static void validarClave(Object clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave del árbol no puede ser nula");
        }
    }
}
