package evacuacion.estructuras;

/**
 * Interfaz funcional propia para ejecutar una acción sobre cada elemento de una estructura.
 * Permite escribir {@code lista.paraCada(p -> ...)} sin depender de {@code java.util.function}.
 *
 * @param <T> tipo del elemento
 */
@FunctionalInterface
public interface Accion<T> {

    void ejecutar(T elemento);
}
