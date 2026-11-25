package evacuacion.servicio;

import evacuacion.algoritmos.ComparacionRutas;
import evacuacion.algoritmos.Dijkstra;
import evacuacion.estructuras.Iterador;
import evacuacion.estructuras.ListaEnlazada;
import evacuacion.estructuras.Pila;
import evacuacion.estructuras.TablaHash;
import evacuacion.modelo.Arista;
import evacuacion.modelo.Grafo;
import evacuacion.modelo.Persona;
import evacuacion.modelo.PlanoEdificio;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.Ubicacion;
import evacuacion.modelo.Zona;

import java.io.File;
import java.io.IOException;

/**
 * Fachada del sistema: es el único punto de entrada que usan la consola y la interfaz gráfica.
 *
 * <p>Reúne todas las estructuras del proyecto:</p>
 * <ul>
 *   <li>{@link Grafo}: el edificio (nodos = ubicaciones, pesos = tiempo, bloqueos).</li>
 *   <li>{@link TablaHash}: {@code idPersona → Persona} para localizar personas en O(1).</li>
 *   <li>{@link ListaEnlazada}: registro ordenado de personas y consultas por piso.</li>
 *   <li>{@link Pila}: historial de rutas calculadas e instrucciones paso a paso.</li>
 *   <li>Árbol binario (en {@link GestorZonas}): zonas por nivel de riesgo.</li>
 *   <li>Colas (en cada {@link Ubicacion}): personas esperando en escaleras y salidas.</li>
 * </ul>
 */
public class EvacuacionService {

    private final Grafo grafo;
    private final PlanoEdificio plano;
    private final TablaHash<String, Persona> personasPorId = new TablaHash<>();
    private final ListaEnlazada<Persona> personas = new ListaEnlazada<>();
    private final Pila<Ruta> historialRutas = new Pila<>();
    private final GestorZonas gestorZonas = new GestorZonas();
    private final Dijkstra dijkstra = new Dijkstra();
    private final GeneradorInstrucciones generadorInstrucciones;
    private final SimuladorEvacuacion simulador;
    private final MapaAscii mapaAscii = new MapaAscii();
    private final ExportadorPlan exportador = new ExportadorPlan();

    public EvacuacionService(Grafo grafo) {
        this.grafo = grafo;
        this.plano = PlanoEdificio.desde(grafo);
        this.generadorInstrucciones = new GeneradorInstrucciones(grafo);
        this.simulador = new SimuladorEvacuacion(grafo, personas, dijkstra);
    }

    // ================================================================== Personas (tabla hash)

    /**
     * Registra una persona en la tabla hash y en la lista general.
     *
     * @throws IllegalArgumentException si el id ya existe o la ubicación no es válida
     * @throws IllegalStateException    si hay una simulación en curso
     */
    public Persona registrarPersona(String id, String nombre, String ubicacionId) {
        if (simulador.estaIniciada()) {
            throw new IllegalStateException("Reinicie la simulación antes de registrar personas");
        }
        String clave = normalizarId(id);
        if (personasPorId.contiene(clave)) {
            throw new IllegalArgumentException("Ya existe una persona con id " + clave);
        }
        Persona persona = new Persona(clave, nombre.trim(), grafo.ubicacionObligatoria(ubicacionId));
        personasPorId.insertar(clave, persona);
        personas.agregarAlFinal(persona);
        return persona;
    }

    /** @return la persona con ese id o {@code null}. O(1) promedio. */
    public Persona buscarPersona(String id) {
        return id == null || id.isBlank() ? null : personasPorId.obtener(normalizarId(id));
    }

    /** "¿Quién queda en el piso N?": recorre la lista enlazada y filtra. O(P). */
    public ListaEnlazada<Persona> personasQueQuedanEnPiso(int piso) {
        return personas.filtrar(p -> !p.estaEvacuado() && p.getPiso() == piso);
    }

    public ListaEnlazada<Persona> getPersonas() {
        return personas;
    }

    public TablaHash<String, Persona> getTablaPersonas() {
        return personasPorId;
    }

    // ================================================================== Rutas

    /**
     * Calcula la ruta más segura y corta (Dijkstra a múltiples salidas) desde la ubicación,
     * genera sus instrucciones y la guarda en el historial (pila).
     * Si no existe ruta libre, sugiere una ruta de emergencia.
     */
    public PlanEvacuacion evacuarDesde(String ubicacionId) {
        PlanEvacuacion plan = generarPlan(ubicacionId);
        Ruta recomendada = plan.rutaRecomendada();
        if (recomendada != null) {
            historialRutas.apilar(recomendada);
        }
        return plan;
    }

    /** Igual que {@link #evacuarDesde} pero sin registrar la ruta en el historial. */
    public PlanEvacuacion generarPlan(String ubicacionId) {
        Ubicacion origen = grafo.ubicacionObligatoria(ubicacionId);
        Ruta ruta = dijkstra.rutaASalidaMasCercana(grafo, origen);
        if (ruta.existe()) {
            return PlanEvacuacion.conRutaSegura(ruta, generadorInstrucciones.generar(ruta));
        }
        Ruta emergencia = dijkstra.rutaDeEmergencia(grafo, origen);
        return PlanEvacuacion.sinRutaSegura(ruta, emergencia, generadorInstrucciones.generar(emergencia));
    }

    public ComparacionRutas compararAlgoritmos(String ubicacionId) {
        return ComparacionRutas.comparar(grafo, grafo.ubicacionObligatoria(ubicacionId));
    }

    /** @return la pila de rutas calculadas (la última consultada está en el tope). */
    public Pila<Ruta> getHistorialRutas() {
        return historialRutas;
    }

    // ================================================================== Incidentes (bloqueos)

    /** Simula humo/fuego entre dos ubicaciones y reacciona (rutas y riesgos). */
    public ListaEnlazada<String> bloquearArista(String idU, String idV) {
        ListaEnlazada<String> eventos = new ListaEnlazada<>();
        if (grafo.bloquearArista(idU, idV)) {
            eventos.agregarAlFinal("🔥 Conexión bloqueada: " + grafo.buscarArista(idU, idV));
            alCambiarElGrafo(eventos);
        } else {
            eventos.agregarAlFinal("La conexión " + idU + " - " + idV + " ya estaba bloqueada");
        }
        return eventos;
    }

    public ListaEnlazada<String> desbloquearArista(String idU, String idV) {
        ListaEnlazada<String> eventos = new ListaEnlazada<>();
        if (grafo.desbloquearArista(idU, idV)) {
            eventos.agregarAlFinal("✓ Conexión despejada: " + grafo.buscarArista(idU, idV));
            alCambiarElGrafo(eventos);
        } else {
            eventos.agregarAlFinal("La conexión " + idU + " - " + idV + " ya estaba libre");
        }
        return eventos;
    }

    /** Cambia el estado de la arista (útil al hacer clic en la interfaz gráfica). */
    public ListaEnlazada<String> alternarBloqueo(Arista arista) {
        String a = arista.getExtremoA().getId();
        String b = arista.getExtremoB().getId();
        return arista.estaBloqueada() ? desbloquearArista(a, b) : bloquearArista(a, b);
    }

    public ListaEnlazada<String> desbloquearTodo() {
        ListaEnlazada<String> eventos = new ListaEnlazada<>();
        Iterador<Arista> it = grafo.aristasBloqueadas().iterador();
        while (it.tieneSiguiente()) {
            it.siguiente().desbloquear();
        }
        eventos.agregarAlFinal("✓ Todas las conexiones fueron despejadas");
        alCambiarElGrafo(eventos);
        return eventos;
    }

    private void alCambiarElGrafo(ListaEnlazada<String> eventos) {
        if (simulador.estaIniciada()) {
            simulador.recalcularRutas(eventos);
        }
        gestorZonas.recalcularRiesgos(grafo, personas);
    }

    // ================================================================== Zonas (árbol binario)

    public Zona crearZona(String nombre, int riesgoBase, String... idsUbicaciones) {
        Ubicacion[] ubicaciones = new Ubicacion[idsUbicaciones.length];
        for (int i = 0; i < idsUbicaciones.length; i++) {
            ubicaciones[i] = grafo.ubicacionObligatoria(idsUbicaciones[i]);
        }
        return gestorZonas.crearZona(nombre, riesgoBase, ubicaciones);
    }

    public String informeZonas() {
        gestorZonas.recalcularRiesgos(grafo, personas);
        return gestorZonas.informe();
    }

    public GestorZonas getGestorZonas() {
        return gestorZonas;
    }

    // ================================================================== Simulación por turnos

    public ListaEnlazada<String> iniciarSimulacion() {
        ListaEnlazada<String> eventos = simulador.iniciar();
        gestorZonas.recalcularRiesgos(grafo, personas);
        return eventos;
    }

    public ListaEnlazada<String> avanzarTurno() {
        ListaEnlazada<String> eventos = simulador.avanzarTurno();
        gestorZonas.recalcularRiesgos(grafo, personas);
        return eventos;
    }

    public void reiniciarSimulacion() {
        simulador.reiniciar();
        gestorZonas.recalcularRiesgos(grafo, personas);
    }

    public SimuladorEvacuacion getSimulador() {
        return simulador;
    }

    // ================================================================== Visualización y exportación

    public String mapaAscii() {
        return mapaAscii.dibujar(plano, grafo, personas);
    }

    public File exportarPlan(File destino) throws IOException {
        return exportador.exportar(this, destino);
    }

    public Grafo getGrafo() {
        return grafo;
    }

    public PlanoEdificio getPlano() {
        return plano;
    }

    private static String normalizarId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id no puede estar vacío");
        }
        return id.trim().toUpperCase();
    }
}
