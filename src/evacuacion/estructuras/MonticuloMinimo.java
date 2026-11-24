package evacuacion.estructuras;

/**
 * Montículo binario de mínimos (cola de prioridad) implementado sobre arreglos.
 *
 * <p>Uso en el proyecto: cola de prioridad de Dijkstra; siempre se extrae la ubicación con
 * menor tiempo acumulado. Se reemplaza a {@code java.util.PriorityQueue}.</p>
 *
 * <p>Complejidad: insertar O(log n), extraerMinimo O(log n), consultar mínimo O(1).</p>
 *
 * @param <T> tipo de los elementos
 */
public class MonticuloMinimo<T> {

    private static final int CAPACIDAD_INICIAL = 16;

    private Object[] elementos;
    private double[] prioridades;
    private int tamano;

    public MonticuloMinimo() {
        elementos = new Object[CAPACIDAD_INICIAL];
        prioridades = new double[CAPACIDAD_INICIAL];
    }

    /** Inserta un elemento con su prioridad (menor = más urgente). */
    public void insertar(T elemento, double prioridad) {
        asegurarCapacidad();
        elementos[tamano] = elemento;
        prioridades[tamano] = prioridad;
        flotar(tamano);
        tamano++;
    }

    /** Extrae el elemento de menor prioridad. */
    @SuppressWarnings("unchecked")
    public T extraerMinimo() {
        if (estaVacio()) {
            throw new EstructuraVaciaException("El montículo está vacío");
        }
        T minimo = (T) elementos[0];
        tamano--;
        elementos[0] = elementos[tamano];
        prioridades[0] = prioridades[tamano];
        elementos[tamano] = null;
        hundir(0);
        return minimo;
    }

    /** @return la prioridad del elemento en la raíz (el mínimo). */
    public double prioridadMinima() {
        if (estaVacio()) {
            throw new EstructuraVaciaException("El montículo está vacío");
        }
        return prioridades[0];
    }

    public boolean estaVacio() {
        return tamano == 0;
    }

    public int tamano() {
        return tamano;
    }

    /** Sube el elemento mientras sea menor que su padre. */
    private void flotar(int indice) {
        while (indice > 0) {
            int padre = (indice - 1) / 2;
            if (prioridades[indice] >= prioridades[padre]) {
                return;
            }
            intercambiar(indice, padre);
            indice = padre;
        }
    }

    /** Baja el elemento mientras alguno de sus hijos sea menor. */
    private void hundir(int indice) {
        while (true) {
            int izquierdo = 2 * indice + 1;
            int derecho = izquierdo + 1;
            int menor = indice;
            if (izquierdo < tamano && prioridades[izquierdo] < prioridades[menor]) {
                menor = izquierdo;
            }
            if (derecho < tamano && prioridades[derecho] < prioridades[menor]) {
                menor = derecho;
            }
            if (menor == indice) {
                return;
            }
            intercambiar(indice, menor);
            indice = menor;
        }
    }

    private void intercambiar(int i, int j) {
        Object elemento = elementos[i];
        elementos[i] = elementos[j];
        elementos[j] = elemento;
        double prioridad = prioridades[i];
        prioridades[i] = prioridades[j];
        prioridades[j] = prioridad;
    }

    private void asegurarCapacidad() {
        if (tamano < elementos.length) {
            return;
        }
        Object[] nuevosElementos = new Object[elementos.length * 2];
        double[] nuevasPrioridades = new double[prioridades.length * 2];
        for (int i = 0; i < tamano; i++) {
            nuevosElementos[i] = elementos[i];
            nuevasPrioridades[i] = prioridades[i];
        }
        elementos = nuevosElementos;
        prioridades = nuevasPrioridades;
    }
}
