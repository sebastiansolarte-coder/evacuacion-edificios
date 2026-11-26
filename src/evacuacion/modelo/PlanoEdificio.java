package evacuacion.modelo;

import evacuacion.estructuras.Iterador;

/**
 * Matriz 2D {@code [pisos × columnas]} que complementa al grafo para dibujar el edificio.
 *
 * <p>La fila 0 corresponde al piso más alto (así se imprime de arriba hacia abajo, como se ve
 * un edificio). Cada celda contiene la ubicación de esa posición o {@code null} si está vacía.
 * El grafo sigue siendo la fuente de verdad para las rutas; la matriz solo sirve para la
 * visualización por piso.</p>
 */
public class PlanoEdificio {

    private final Ubicacion[][] celdas;
    private final int pisoMaximo;

    private PlanoEdificio(int pisoMaximo, int columnas) {
        this.pisoMaximo = pisoMaximo;
        this.celdas = new Ubicacion[pisoMaximo][columnas];
    }

    /** Construye la matriz ubicando cada nodo del grafo en su (piso, columna). */
    public static PlanoEdificio desde(Grafo grafo) {
        PlanoEdificio plano = new PlanoEdificio(grafo.pisoMaximo(), grafo.columnaMaxima() + 1);
        Iterador<Ubicacion> it = grafo.todasLasUbicaciones().iterador();
        while (it.tieneSiguiente()) {
            plano.colocar(it.siguiente());
        }
        return plano;
    }

    private void colocar(Ubicacion ubicacion) {
        int fila = filaDePiso(ubicacion.getPiso());
        Ubicacion ocupante = celdas[fila][ubicacion.getColumna()];
        if (ocupante != null) {
            throw new IllegalStateException("La celda de " + ubicacion.getId()
                    + " ya está ocupada por " + ocupante.getId());
        }
        celdas[fila][ubicacion.getColumna()] = ubicacion;
    }

    public Ubicacion celda(int fila, int columna) {
        return celdas[fila][columna];
    }

    public int filas() {
        return celdas.length;
    }

    public int columnas() {
        return celdas[0].length;
    }

    public int getPisoMaximo() {
        return pisoMaximo;
    }

    public int pisoDeFila(int fila) {
        return pisoMaximo - fila;
    }

    public int filaDePiso(int piso) {
        return pisoMaximo - piso;
    }
}
