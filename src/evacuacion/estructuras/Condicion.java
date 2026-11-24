package evacuacion.estructuras;

/**
 * Predicado propio usado para buscar y filtrar elementos (reemplaza a {@code java.util.function.Predicate}).
 *
 * @param <T> tipo del elemento evaluado
 */
@FunctionalInterface
public interface Condicion<T> {

    boolean cumple(T elemento);
}
