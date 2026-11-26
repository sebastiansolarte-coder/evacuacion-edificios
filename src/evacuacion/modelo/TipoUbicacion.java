package evacuacion.modelo;

/**
 * Tipos de nodo del grafo del edificio.
 *
 * <p>La capacidad por turno indica cuántas personas pueden cruzar la puerta del nodo en un
 * turno de simulación. Un valor 0 significa que el nodo no es un cuello de botella.</p>
 */
public enum TipoUbicacion {

    HABITACION("Habitación", 0),
    PASILLO("Pasillo", 0),
    ESCALERA("Escalera", 1),
    SALIDA("Salida", 2);

    private final String etiqueta;
    private final int capacidadPorDefecto;

    TipoUbicacion(String etiqueta, int capacidadPorDefecto) {
        this.etiqueta = etiqueta;
        this.capacidadPorDefecto = capacidadPorDefecto;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getCapacidadPorDefecto() {
        return capacidadPorDefecto;
    }

    /** Escaleras y salidas se cruzan de a pocos: se forma una cola. */
    public boolean esCuelloDeBotella() {
        return capacidadPorDefecto > 0;
    }
}
