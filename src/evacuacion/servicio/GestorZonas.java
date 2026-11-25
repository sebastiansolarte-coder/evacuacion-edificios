package evacuacion.servicio;

import evacuacion.estructuras.ArbolBinario;
import evacuacion.estructuras.Iterador;
import evacuacion.estructuras.ListaEnlazada;
import evacuacion.estructuras.TablaHash;
import evacuacion.modelo.Arista;
import evacuacion.modelo.Grafo;
import evacuacion.modelo.Persona;
import evacuacion.modelo.Ubicacion;
import evacuacion.modelo.Zona;

/**
 * Administra las zonas del edificio y su árbol binario de prioridades
 * {@code nivelRiesgo → Zona}.
 *
 * <p>Cada vez que cambia el estado (humo, personas que salen) se recalcula el riesgo de cada
 * zona y se reconstruye el árbol. El recorrido inOrden inverso da el orden en que la brigada
 * debe atender las zonas (mayor riesgo primero).</p>
 */
public class GestorZonas {

    /** Puntos de riesgo que suma cada conexión con humo/fuego dentro de la zona. */
    public static final int RIESGO_POR_CONEXION_BLOQUEADA = 25;
    /** Puntos de riesgo que suma cada persona que sigue dentro de la zona. */
    public static final int RIESGO_POR_PERSONA = 5;

    private final ListaEnlazada<Zona> zonas = new ListaEnlazada<>();
    private final TablaHash<String, Zona> zonaPorUbicacion = new TablaHash<>();
    private ArbolBinario<Integer, Zona> arbolRiesgo = new ArbolBinario<>();

    /** Crea una zona con las ubicaciones indicadas. Cada ubicación pertenece a una sola zona. */
    public Zona crearZona(String nombre, int riesgoBase, Ubicacion... ubicaciones) {
        Zona zona = new Zona(nombre, riesgoBase);
        for (Ubicacion ubicacion : ubicaciones) {
            if (zonaPorUbicacion.contiene(ubicacion.getId())) {
                throw new IllegalArgumentException(ubicacion.getId() + " ya pertenece a otra zona");
            }
            zona.agregarUbicacion(ubicacion);
            zonaPorUbicacion.insertar(ubicacion.getId(), zona);
        }
        zonas.agregarAlFinal(zona);
        arbolRiesgo.insertar(zona.getRiesgoActual(), zona);
        return zona;
    }

    /** @return la zona a la que pertenece la ubicación (O(1) promedio) o {@code null}. */
    public Zona zonaDe(Ubicacion ubicacion) {
        return zonaPorUbicacion.obtener(ubicacion.getId());
    }

    /** Recalcula el riesgo de todas las zonas y reconstruye el árbol. O(Z log Z + E + P). */
    public void recalcularRiesgos(Grafo grafo, ListaEnlazada<Persona> personas) {
        zonas.paraCada(Zona::reiniciarContadores);
        contarConexionesBloqueadas(grafo);
        contarPersonasRestantes(personas);

        arbolRiesgo = new ArbolBinario<>();
        Iterador<Zona> it = zonas.iterador();
        while (it.tieneSiguiente()) {
            Zona zona = it.siguiente();
            zona.actualizarRiesgo(RIESGO_POR_CONEXION_BLOQUEADA, RIESGO_POR_PERSONA);
            arbolRiesgo.insertar(zona.getRiesgoActual(), zona);
        }
        arbolRiesgo.rebalancear();
    }

    private void contarConexionesBloqueadas(Grafo grafo) {
        Iterador<Arista> it = grafo.aristasBloqueadas().iterador();
        while (it.tieneSiguiente()) {
            Arista arista = it.siguiente();
            Zona zonaA = zonaDe(arista.getExtremoA());
            Zona zonaB = zonaDe(arista.getExtremoB());
            if (zonaA != null) {
                zonaA.registrarConexionBloqueada();
            }
            if (zonaB != null && zonaB != zonaA) {
                zonaB.registrarConexionBloqueada();
            }
        }
    }

    private void contarPersonasRestantes(ListaEnlazada<Persona> personas) {
        Iterador<Persona> it = personas.iterador();
        while (it.tieneSiguiente()) {
            Persona persona = it.siguiente();
            Zona zona = persona.estaEvacuado() ? null : zonaDe(persona.getUbicacionActual());
            if (zona != null) {
                zona.registrarPersonaRestante();
            }
        }
    }

    /** @return zonas de mayor a menor riesgo (orden de atención de la brigada). */
    public ListaEnlazada<Zona> zonasPorPrioridad() {
        return arbolRiesgo.inOrdenDescendente();
    }

    /** @return zonas de menor a mayor riesgo (recorrido inOrden). */
    public ListaEnlazada<Zona> zonasInOrden() {
        return arbolRiesgo.inOrden();
    }

    public ListaEnlazada<Zona> getZonas() {
        return zonas;
    }

    public ArbolBinario<Integer, Zona> getArbolRiesgo() {
        return arbolRiesgo;
    }

    /** Reporte con el inOrden, la prioridad de la brigada y el dibujo del árbol. */
    public String informe() {
        StringBuilder sb = new StringBuilder();
        sb.append("REPORTE DE ZONAS POR NIVEL DE RIESGO (árbol binario de búsqueda)\n");
        sb.append("  Fórmula: riesgo = base + ").append(RIESGO_POR_CONEXION_BLOQUEADA)
                .append("×conexiones con humo + ").append(RIESGO_POR_PERSONA).append("×personas dentro\n\n");

        sb.append("  Recorrido inOrden (menor → mayor riesgo):\n");
        agregarLineas(sb, zonasInOrden(), false);

        sb.append("\n  Prioridad para enviar la brigada (inOrden inverso, mayor → menor):\n");
        agregarLineas(sb, zonasPorPrioridad(), true);

        sb.append("\n  Forma del árbol (raíz a la izquierda, mayor riesgo arriba) — altura ")
                .append(arbolRiesgo.altura()).append(":\n");
        sb.append(arbolRiesgo.dibujar());
        return sb.toString();
    }

    private void agregarLineas(StringBuilder sb, ListaEnlazada<Zona> lista, boolean numerar) {
        Iterador<Zona> it = lista.iterador();
        for (int posicion = 1; it.tieneSiguiente(); posicion++) {
            sb.append("    ").append(numerar ? posicion + ". " : "• ").append(it.siguiente().resumen()).append('\n');
        }
    }
}
