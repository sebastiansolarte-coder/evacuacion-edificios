package evacuacion.modelo;

import evacuacion.estructuras.Iterador;
import evacuacion.estructuras.ListaEnlazada;
import evacuacion.estructuras.TablaHash;

/**
 * Grafo ponderado no dirigido del edificio, representado con <b>listas de adyacencia</b>.
 *
 * <ul>
 *   <li>Nodos: {@link Ubicacion} (habitaciones, pasillos, escaleras, salidas).</li>
 *   <li>Aristas: {@link Arista} con peso = tiempo en segundos; pueden bloquearse.</li>
 *   <li>Búsqueda de nodo por id en O(1) promedio con una {@link TablaHash}.</li>
 * </ul>
 *
 * <p>Memoria O(V + E). Recorrer los vecinos de un nodo cuesta O(grado).</p>
 */
public class Grafo {

    private static final int CAPACIDAD_INICIAL = 16;

    private Ubicacion[] ubicaciones = new Ubicacion[CAPACIDAD_INICIAL];
    private ListaEnlazada<Arista>[] adyacencia = crearAdyacencia(CAPACIDAD_INICIAL);
    private final TablaHash<String, Ubicacion> ubicacionesPorId = new TablaHash<>();
    private final ListaEnlazada<Arista> aristas = new ListaEnlazada<>();
    private int cantidad;

    /** Agrega un nodo al grafo y le asigna su índice interno. */
    public Ubicacion agregarUbicacion(Ubicacion ubicacion) {
        if (ubicacionesPorId.contiene(ubicacion.getId())) {
            throw new IllegalArgumentException("Ya existe la ubicación " + ubicacion.getId());
        }
        asegurarCapacidad();
        ubicacion.asignarIndice(cantidad);
        ubicaciones[cantidad] = ubicacion;
        adyacencia[cantidad] = new ListaEnlazada<>();
        ubicacionesPorId.insertar(ubicacion.getId(), ubicacion);
        cantidad++;
        return ubicacion;
    }

    /** Crea una arista no dirigida entre dos ubicaciones existentes. */
    public Arista conectar(String idA, String idB, double tiempo) {
        Ubicacion a = ubicacionObligatoria(idA);
        Ubicacion b = ubicacionObligatoria(idB);
        if (buscarArista(a, b) != null) {
            throw new IllegalArgumentException("Ya existe una conexión entre " + idA + " y " + idB);
        }
        Arista arista = new Arista(a, b, tiempo);
        adyacencia[a.getIndice()].agregarAlFinal(arista);
        adyacencia[b.getIndice()].agregarAlFinal(arista);
        aristas.agregarAlFinal(arista);
        return arista;
    }

    /**
     * Simula humo o fuego entre dos ubicaciones: la arista deja de ser transitable.
     *
     * @return {@code true} si el estado cambió (estaba libre)
     */
    public boolean bloquearArista(String idU, String idV) {
        Arista arista = aristaObligatoria(idU, idV);
        boolean cambio = !arista.estaBloqueada();
        arista.bloquear();
        return cambio;
    }

    /** @return {@code true} si el estado cambió (estaba bloqueada). */
    public boolean desbloquearArista(String idU, String idV) {
        Arista arista = aristaObligatoria(idU, idV);
        boolean cambio = arista.estaBloqueada();
        arista.desbloquear();
        return cambio;
    }

    /** @return la arista entre {@code a} y {@code b} o {@code null}. O(grado de a). */
    public Arista buscarArista(Ubicacion a, Ubicacion b) {
        return adyacencia[a.getIndice()].buscar(arista -> arista.conecta(a, b));
    }

    public Arista buscarArista(String idA, String idB) {
        Ubicacion a = ubicacion(idA);
        Ubicacion b = ubicacion(idB);
        return a == null || b == null ? null : buscarArista(a, b);
    }

    /** @return la ubicación con ese id o {@code null}. O(1) promedio. */
    public Ubicacion ubicacion(String id) {
        return id == null ? null : ubicacionesPorId.obtener(id.trim().toUpperCase());
    }

    /** Igual que {@link #ubicacion(String)} pero lanza un error descriptivo si no existe. */
    public Ubicacion ubicacionObligatoria(String id) {
        Ubicacion ubicacion = ubicacion(id);
        if (ubicacion == null) {
            throw new IllegalArgumentException("No existe la ubicación '" + id + "'");
        }
        return ubicacion;
    }

    public Ubicacion ubicacionEn(int indice) {
        if (indice < 0 || indice >= cantidad) {
            throw new IndexOutOfBoundsException("Índice de ubicación inválido: " + indice);
        }
        return ubicaciones[indice];
    }

    public int cantidadUbicaciones() {
        return cantidad;
    }

    /** @return las aristas incidentes en la ubicación (incluye las bloqueadas). */
    public ListaEnlazada<Arista> aristasDe(Ubicacion ubicacion) {
        return adyacencia[ubicacion.getIndice()];
    }

    public ListaEnlazada<Arista> aristas() {
        return aristas;
    }

    public ListaEnlazada<Arista> aristasBloqueadas() {
        return aristas.filtrar(Arista::estaBloqueada);
    }

    /** @return {@code true} si alguna conexión de la ubicación tiene humo o fuego. */
    public boolean tieneConexionBloqueada(Ubicacion ubicacion) {
        return aristasDe(ubicacion).buscar(Arista::estaBloqueada) != null;
    }

    public ListaEnlazada<Ubicacion> salidas() {
        return filtrarUbicaciones(TipoUbicacion.SALIDA);
    }

    public ListaEnlazada<Ubicacion> filtrarUbicaciones(TipoUbicacion tipo) {
        ListaEnlazada<Ubicacion> resultado = new ListaEnlazada<>();
        for (int i = 0; i < cantidad; i++) {
            if (ubicaciones[i].getTipo() == tipo) {
                resultado.agregarAlFinal(ubicaciones[i]);
            }
        }
        return resultado;
    }

    public ListaEnlazada<Ubicacion> todasLasUbicaciones() {
        ListaEnlazada<Ubicacion> resultado = new ListaEnlazada<>();
        for (int i = 0; i < cantidad; i++) {
            resultado.agregarAlFinal(ubicaciones[i]);
        }
        return resultado;
    }

    public int pisoMaximo() {
        int maximo = 0;
        for (int i = 0; i < cantidad; i++) {
            maximo = Math.max(maximo, ubicaciones[i].getPiso());
        }
        return maximo;
    }

    public int columnaMaxima() {
        int maximo = 0;
        for (int i = 0; i < cantidad; i++) {
            maximo = Math.max(maximo, ubicaciones[i].getColumna());
        }
        return maximo;
    }

    /** Texto con cada nodo y sus vecinos; muestra la lista de adyacencia tal cual. */
    public String describirAdyacencia() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cantidad; i++) {
            Ubicacion u = ubicaciones[i];
            sb.append(Formato.ajustar(u.getId(), 10)).append(" -> ");
            Iterador<Arista> it = adyacencia[i].iterador();
            while (it.tieneSiguiente()) {
                Arista a = it.siguiente();
                sb.append(a.otroExtremo(u).getId()).append('(').append(Formato.tiempo(a.getTiempo()))
                        .append(a.estaBloqueada() ? ",X" : "").append(") ");
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private Arista aristaObligatoria(String idU, String idV) {
        Arista arista = buscarArista(ubicacionObligatoria(idU), ubicacionObligatoria(idV));
        if (arista == null) {
            throw new IllegalArgumentException("No hay conexión directa entre " + idU + " y " + idV);
        }
        return arista;
    }

    private void asegurarCapacidad() {
        if (cantidad < ubicaciones.length) {
            return;
        }
        int nuevaCapacidad = ubicaciones.length * 2;
        Ubicacion[] nuevasUbicaciones = new Ubicacion[nuevaCapacidad];
        ListaEnlazada<Arista>[] nuevaAdyacencia = crearAdyacencia(nuevaCapacidad);
        for (int i = 0; i < cantidad; i++) {
            nuevasUbicaciones[i] = ubicaciones[i];
            nuevaAdyacencia[i] = adyacencia[i];
        }
        ubicaciones = nuevasUbicaciones;
        adyacencia = nuevaAdyacencia;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ListaEnlazada<Arista>[] crearAdyacencia(int capacidad) {
        return (ListaEnlazada<Arista>[]) new ListaEnlazada[capacidad];
    }
}
