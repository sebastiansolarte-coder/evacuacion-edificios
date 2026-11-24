package evacuacion.estructuras;

/**
 * Iterador propio del proyecto (no se usa {@code java.util.Iterator}).
 *
 * <pre>
 * Iterador&lt;Persona&gt; it = lista.iterador();
 * while (it.tieneSiguiente()) {
 *     Persona p = it.siguiente();
 * }
 * </pre>
 *
 * @param <T> tipo de los elementos recorridos
 */
public interface Iterador<T> {

    /** @return {@code true} si aún quedan elementos por recorrer. */
    boolean tieneSiguiente();

    /**
     * Devuelve el elemento actual y avanza.
     *
     * @throws EstructuraVaciaException si ya no quedan elementos
     */
    T siguiente();
}
