package evacuacion.estructuras;

/**
 * Tabla hash genérica con <b>encadenamiento separado</b> (cada cubeta es una lista enlazada
 * de entradas) y redimensionamiento automático cuando el factor de carga supera 0.75.
 *
 * <p>Uso en el proyecto: {@code idPersona → Persona} (piso, habitación, evacuado sí/no),
 * {@code idUbicacion → Ubicacion} dentro del grafo y {@code idUbicacion → Zona}.</p>
 *
 * <p>Complejidad: insertar, obtener y eliminar en O(1) promedio; O(n) en el peor caso
 * (todas las claves en la misma cubeta). El rehash cuesta O(n) pero ocurre pocas veces,
 * por lo que la inserción es O(1) amortizada.</p>
 *
 * @param <K> tipo de la clave (debe implementar {@code equals} y {@code hashCode})
 * @param <V> tipo del valor
 */
public class TablaHash<K, V> {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA_MAXIMO = 0.75;

    /** Entrada de una cubeta: par clave-valor enlazado con la siguiente entrada que colisionó. */
    private static final class Entrada<K, V> {
        private final K clave;
        private V valor;
        private Entrada<K, V> siguiente;

        private Entrada(K clave, V valor, Entrada<K, V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    private Entrada<K, V>[] cubetas;
    private int tamano;

    public TablaHash() {
        this(CAPACIDAD_INICIAL);
    }

    public TablaHash(int capacidadInicial) {
        if (capacidadInicial < 1) {
            throw new IllegalArgumentException("La capacidad debe ser positiva");
        }
        cubetas = crearCubetas(capacidadInicial);
    }

    /**
     * Inserta o actualiza el valor asociado a la clave.
     *
     * @return el valor anterior, o {@code null} si la clave era nueva
     */
    public V insertar(K clave, V valor) {
        validarClave(clave);
        int indice = indiceDe(clave, cubetas.length);
        for (Entrada<K, V> e = cubetas[indice]; e != null; e = e.siguiente) {
            if (e.clave.equals(clave)) {
                V anterior = e.valor;
                e.valor = valor;
                return anterior;
            }
        }
        cubetas[indice] = new Entrada<>(clave, valor, cubetas[indice]);
        tamano++;
        if (factorCarga() > FACTOR_CARGA_MAXIMO) {
            redimensionar();
        }
        return null;
    }

    /** @return el valor asociado, o {@code null} si la clave no existe. O(1) promedio. */
    public V obtener(K clave) {
        Entrada<K, V> entrada = buscarEntrada(clave);
        return entrada == null ? null : entrada.valor;
    }

    /** @return el valor asociado o {@code porDefecto} si la clave no existe. */
    public V obtenerOPorDefecto(K clave, V porDefecto) {
        Entrada<K, V> entrada = buscarEntrada(clave);
        return entrada == null ? porDefecto : entrada.valor;
    }

    public boolean contiene(K clave) {
        return buscarEntrada(clave) != null;
    }

    /** @return el valor eliminado, o {@code null} si la clave no existía. */
    public V eliminar(K clave) {
        validarClave(clave);
        int indice = indiceDe(clave, cubetas.length);
        Entrada<K, V> anterior = null;
        for (Entrada<K, V> e = cubetas[indice]; e != null; e = e.siguiente) {
            if (e.clave.equals(clave)) {
                if (anterior == null) {
                    cubetas[indice] = e.siguiente;
                } else {
                    anterior.siguiente = e.siguiente;
                }
                tamano--;
                return e.valor;
            }
            anterior = e;
        }
        return null;
    }

    /** @return todas las claves (el orden depende de la función hash). */
    public ListaEnlazada<K> claves() {
        ListaEnlazada<K> claves = new ListaEnlazada<>();
        for (Entrada<K, V> cubeta : cubetas) {
            for (Entrada<K, V> e = cubeta; e != null; e = e.siguiente) {
                claves.agregarAlFinal(e.clave);
            }
        }
        return claves;
    }

    /** @return todos los valores (el orden depende de la función hash). */
    public ListaEnlazada<V> valores() {
        ListaEnlazada<V> valores = new ListaEnlazada<>();
        for (Entrada<K, V> cubeta : cubetas) {
            for (Entrada<K, V> e = cubeta; e != null; e = e.siguiente) {
                valores.agregarAlFinal(e.valor);
            }
        }
        return valores;
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int capacidad() {
        return cubetas.length;
    }

    public double factorCarga() {
        return (double) tamano / cubetas.length;
    }

    /** Longitud de la cubeta más larga: mide qué tan bien distribuye la función hash. */
    public int longitudMaximaCubeta() {
        int maximo = 0;
        for (Entrada<K, V> cubeta : cubetas) {
            int longitud = 0;
            for (Entrada<K, V> e = cubeta; e != null; e = e.siguiente) {
                longitud++;
            }
            maximo = Math.max(maximo, longitud);
        }
        return maximo;
    }

    private Entrada<K, V> buscarEntrada(K clave) {
        validarClave(clave);
        for (Entrada<K, V> e = cubetas[indiceDe(clave, cubetas.length)]; e != null; e = e.siguiente) {
            if (e.clave.equals(clave)) {
                return e;
            }
        }
        return null;
    }

    /** Duplica la capacidad y reubica cada entrada en su nueva cubeta. O(n). */
    private void redimensionar() {
        Entrada<K, V>[] anteriores = cubetas;
        cubetas = crearCubetas(anteriores.length * 2);
        for (Entrada<K, V> cubeta : anteriores) {
            Entrada<K, V> e = cubeta;
            while (e != null) {
                Entrada<K, V> siguiente = e.siguiente;
                int indice = indiceDe(e.clave, cubetas.length);
                e.siguiente = cubetas[indice];
                cubetas[indice] = e;
                e = siguiente;
            }
        }
    }

    /** Dispersa los bits altos del hashCode para repartir mejor las claves entre las cubetas. */
    private static int indiceDe(Object clave, int numeroCubetas) {
        int hash = clave.hashCode();
        hash ^= (hash >>> 16);
        return (hash & 0x7fffffff) % numeroCubetas;
    }

    private static void validarClave(Object clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser nula");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <K, V> Entrada<K, V>[] crearCubetas(int cantidad) {
        return (Entrada<K, V>[]) new Entrada[cantidad];
    }
}
