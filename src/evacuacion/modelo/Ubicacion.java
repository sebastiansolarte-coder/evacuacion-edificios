package evacuacion.modelo;

import evacuacion.estructuras.Cola;

/**
 * Nodo del grafo: una habitación, pasillo, escalera o salida del edificio.
 *
 * <p>Las escaleras y salidas son cuellos de botella: tienen una {@link Cola} FIFO de personas
 * esperando cruzar la puerta y una capacidad máxima de personas por turno.</p>
 *
 * <p>{@code piso} y {@code columna} ubican el nodo en la matriz 2D del plano
 * ({@link PlanoEdificio}) para dibujarlo en ASCII y en la interfaz gráfica.</p>
 */
public class Ubicacion {

    private final String id;
    private final String nombre;
    private final TipoUbicacion tipo;
    private final int piso;
    private final int columna;
    private final int capacidadPorTurno;
    private final Cola<Persona> colaEspera = new Cola<>();
    private int indice = -1;

    public Ubicacion(String id, String nombre, TipoUbicacion tipo, int piso, int columna) {
        this(id, nombre, tipo, piso, columna, tipo.getCapacidadPorDefecto());
    }

    public Ubicacion(String id, String nombre, TipoUbicacion tipo, int piso, int columna, int capacidadPorTurno) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("La ubicación necesita un id");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("La ubicación necesita un tipo");
        }
        if (piso < 1 || columna < 0) {
            throw new IllegalArgumentException("Piso (>=1) o columna (>=0) inválidos para " + id);
        }
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.piso = piso;
        this.columna = columna;
        this.capacidadPorTurno = capacidadPorTurno;
    }

    public boolean esSalida() {
        return tipo == TipoUbicacion.SALIDA;
    }

    public boolean esCuelloDeBotella() {
        return tipo.esCuelloDeBotella();
    }

    /** Nombre legible; para escaleras se agrega el piso porque hay un descanso por piso. */
    public String descripcion() {
        return tipo == TipoUbicacion.ESCALERA ? nombre + " (piso " + piso + ")" : nombre;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoUbicacion getTipo() {
        return tipo;
    }

    public int getPiso() {
        return piso;
    }

    public int getColumna() {
        return columna;
    }

    public int getCapacidadPorTurno() {
        return capacidadPorTurno;
    }

    public Cola<Persona> getColaEspera() {
        return colaEspera;
    }

    /** Posición del nodo en los arreglos internos del grafo (la asigna {@link Grafo}). */
    public int getIndice() {
        return indice;
    }

    void asignarIndice(int indice) {
        this.indice = indice;
    }

    @Override
    public String toString() {
        return id;
    }
}
