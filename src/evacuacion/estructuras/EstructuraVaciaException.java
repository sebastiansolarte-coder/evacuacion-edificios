package evacuacion.estructuras;

/**
 * Se lanza cuando se intenta leer o extraer un elemento de una estructura vacía
 * (desencolar una cola vacía, desapilar una pila vacía, etc.).
 */
public class EstructuraVaciaException extends RuntimeException {

    public EstructuraVaciaException(String mensaje) {
        super(mensaje);
    }
}
