package evacuacion.servicio;

import evacuacion.algoritmos.EstrategiaRuta;
import evacuacion.estructuras.Cola;
import evacuacion.estructuras.Iterador;
import evacuacion.estructuras.ListaEnlazada;
import evacuacion.estructuras.Pila;
import evacuacion.modelo.Grafo;
import evacuacion.modelo.Persona;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.Ubicacion;

/**
 * Simulación del desalojo <b>por turnos</b>.
 *
 * <p>Reglas de cada turno:</p>
 * <ol>
 *   <li><b>Cuellos de botella:</b> cada escalera y salida deja pasar, en orden FIFO, como
 *       máximo {@code capacidadPorTurno} personas de su cola. Quien cruza una salida queda
 *       marcado como evacuado en la tabla hash.</li>
 *   <li><b>Movimiento libre:</b> el resto avanza un nodo por su ruta. Si el siguiente nodo es
 *       una escalera o salida, la persona se forma en la cola de esa puerta.</li>
 * </ol>
 * <p>Cada persona actúa como máximo una vez por turno. Si se bloquea una conexión durante la
 * simulación, se recalculan las rutas desde la posición actual de cada persona y se
 * reorganizan las colas.</p>
 */
public class SimuladorEvacuacion {

    private final Grafo grafo;
    private final ListaEnlazada<Persona> personas;
    private final EstrategiaRuta estrategia;
    private int turnoActual;
    private boolean iniciada;

    public SimuladorEvacuacion(Grafo grafo, ListaEnlazada<Persona> personas, EstrategiaRuta estrategia) {
        this.grafo = grafo;
        this.personas = personas;
        this.estrategia = estrategia;
    }

    /** Devuelve a todos a su habitación, vacía las colas y calcula las rutas iniciales. */
    public ListaEnlazada<String> iniciar() {
        reiniciar();
        iniciada = true;
        ListaEnlazada<String> eventos = new ListaEnlazada<>();
        eventos.agregarAlFinal("== Simulación iniciada con " + personas.tamano() + " personas (ruta: "
                + estrategia.nombre() + ") ==");
        recalcularRutas(eventos);
        return eventos;
    }

    public void reiniciar() {
        turnoActual = 0;
        iniciada = false;
        vaciarColas();
        personas.paraCada(Persona::reiniciar);
    }

    /** Ejecuta un turno completo y devuelve la bitácora de lo ocurrido. */
    public ListaEnlazada<String> avanzarTurno() {
        ListaEnlazada<String> eventos = iniciada ? new ListaEnlazada<>() : iniciar();
        if (estaTerminada()) {
            eventos.agregarAlFinal("La simulación ya terminó. " + resumen());
            return eventos;
        }
        turnoActual++;
        eventos.agregarAlFinal("── Turno " + turnoActual + " ──");
        procesarColas(eventos);
        moverPersonasLibres(eventos);
        if (estaTerminada()) {
            eventos.agregarAlFinal("== Evacuación finalizada. " + resumen() + " ==");
        }
        return eventos;
    }

    /**
     * Recalcula la ruta de cada persona que sigue dentro (o atrapada) desde su posición actual.
     * Se llama al iniciar y cada vez que se bloquea o desbloquea una conexión.
     */
    public void recalcularRutas(ListaEnlazada<String> eventos) {
        Iterador<Persona> it = personas.iterador();
        while (it.tieneSiguiente()) {
            Persona persona = it.siguiente();
            if (!persona.estaEvacuado()) {
                asignarRuta(persona, eventos);
            }
        }
        reorganizarColas();
    }

    private void asignarRuta(Persona persona, ListaEnlazada<String> eventos) {
        Ruta ruta = estrategia.rutaASalidaMasCercana(grafo, persona.getUbicacionActual());
        if (!ruta.existe()) {
            if (!persona.estaAtrapado()) {
                eventos.agregarAlFinal("⚠ " + persona + " quedó sin ruta en "
                        + persona.getUbicacionActual().descripcion() + ": espera a la brigada");
            }
            persona.asignarRuta(new Pila<>());
            persona.marcarAtrapado();
            return;
        }
        Pila<Ubicacion> pasos = ruta.copiarPasos();
        pasos.desapilar(); // el tope era la ubicación actual
        persona.asignarRuta(pasos);
        if (persona.estaAtrapado()) {
            persona.liberar();
            eventos.agregarAlFinal("✓ " + persona + " vuelve a tener ruta: " + ruta.descripcion());
        }
    }

    private void procesarColas(ListaEnlazada<String> eventos) {
        for (int i = 0; i < grafo.cantidadUbicaciones(); i++) {
            Ubicacion puerta = grafo.ubicacionEn(i);
            if (puerta.esCuelloDeBotella()) {
                dejarPasar(puerta, eventos);
            }
        }
    }

    /** Desencola hasta {@code capacidadPorTurno} personas de la puerta (FIFO). */
    private void dejarPasar(Ubicacion puerta, ListaEnlazada<String> eventos) {
        Cola<Persona> cola = puerta.getColaEspera();
        for (int pasan = 0; pasan < puerta.getCapacidadPorTurno() && !cola.estaVacia(); pasan++) {
            Persona persona = cola.desencolar();
            persona.avanzar(turnoActual);
            if (puerta.esSalida()) {
                persona.marcarEvacuado(turnoActual);
                eventos.agregarAlFinal("✔ " + persona + " salió por " + puerta.getId()
                        + " (evacuado = sí, turno " + turnoActual + ")");
            } else {
                eventos.agregarAlFinal("  " + persona + " cruza " + puerta.descripcion()
                        + (cola.estaVacia() ? "" : " — siguen " + cola.tamano() + " en cola"));
            }
        }
    }

    private void moverPersonasLibres(ListaEnlazada<String> eventos) {
        Iterador<Persona> it = personas.iterador();
        while (it.tieneSiguiente()) {
            Persona persona = it.siguiente();
            Ubicacion siguiente = persona.siguientePaso();
            if (!persona.puedeMoverse(turnoActual) || siguiente == null) {
                continue;
            }
            if (siguiente.esCuelloDeBotella()) {
                siguiente.getColaEspera().encolar(persona);
                persona.ponerEnCola();
                eventos.agregarAlFinal("  " + persona + " hace fila para " + siguiente.descripcion()
                        + " (posición " + siguiente.getColaEspera().tamano() + ")");
            } else {
                persona.avanzar(turnoActual);
                eventos.agregarAlFinal("  " + persona + " avanza a " + siguiente.descripcion());
            }
        }
    }

    /**
     * Tras recalcular rutas, deja en cada cola solo a quienes siguen necesitando esa puerta,
     * conservando su orden de llegada. O(total de personas en colas).
     */
    private void reorganizarColas() {
        for (int i = 0; i < grafo.cantidadUbicaciones(); i++) {
            Ubicacion puerta = grafo.ubicacionEn(i);
            Cola<Persona> cola = puerta.getColaEspera();
            int enEspera = cola.tamano();
            for (int k = 0; k < enEspera; k++) {
                Persona persona = cola.desencolar();
                if (persona.estaEnCola() && persona.siguientePaso() == puerta) {
                    cola.encolar(persona);
                } else {
                    persona.salirDeCola();
                }
            }
        }
    }

    private void vaciarColas() {
        for (int i = 0; i < grafo.cantidadUbicaciones(); i++) {
            grafo.ubicacionEn(i).getColaEspera().limpiar();
        }
    }

    /** La simulación termina cuando nadie puede seguir avanzando (todos evacuados o atrapados). */
    public boolean estaTerminada() {
        return iniciada && personas.buscar(Persona::sigueEvacuando) == null;
    }

    public boolean estaIniciada() {
        return iniciada;
    }

    public int getTurnoActual() {
        return turnoActual;
    }

    public int evacuados() {
        return personas.contar(Persona::estaEvacuado);
    }

    public int atrapados() {
        return personas.contar(Persona::estaAtrapado);
    }

    public String resumen() {
        return "Turno " + turnoActual + ": " + evacuados() + "/" + personas.tamano() + " evacuados, "
                + atrapados() + " atrapado(s) esperando brigada.";
    }
}
