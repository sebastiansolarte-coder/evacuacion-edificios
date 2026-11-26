package evacuacion.pruebas;

import evacuacion.algoritmos.BusquedaAnchura;
import evacuacion.algoritmos.ComparacionRutas;
import evacuacion.algoritmos.Dijkstra;
import evacuacion.datos.EdificioDemo;
import evacuacion.estructuras.ArbolBinario;
import evacuacion.estructuras.Cola;
import evacuacion.estructuras.EstructuraVaciaException;
import evacuacion.estructuras.ListaEnlazada;
import evacuacion.estructuras.MonticuloMinimo;
import evacuacion.estructuras.Pila;
import evacuacion.estructuras.TablaHash;
import evacuacion.modelo.Grafo;
import evacuacion.modelo.Persona;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.Zona;
import evacuacion.servicio.EvacuacionService;
import evacuacion.servicio.PlanEvacuacion;

/**
 * Pruebas automáticas sin librerías externas (no se usa JUnit para no depender de Maven).
 * Ejecutar con {@code ./probar.sh}.
 */
public final class PruebasProyecto {

    private static int aprobadas;
    private static int fallidas;

    private PruebasProyecto() {
    }

    public static void main(String[] args) {
        probarListaEnlazada();
        probarCola();
        probarPila();
        probarTablaHash();
        probarArbolBinario();
        probarMonticulo();
        probarDijkstraYBfs();
        probarBloqueos();
        probarHabitacionAislada();
        probarInstrucciones();
        probarSimulacionCompleta();
        probarBloqueoDuranteSimulacion();
        probarZonas();
        probarConsultasDePersonas();

        System.out.println();
        System.out.println("Resultado: " + aprobadas + " aprobadas, " + fallidas + " fallidas");
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    // ================================================================== estructuras

    private static void probarListaEnlazada() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        lista.agregarAlFinal("b");
        lista.agregarAlFinal("c");
        lista.agregarAlInicio("a");
        verificar(lista.tamano() == 3 && "a".equals(lista.obtener(0)) && "c".equals(lista.obtener(2)),
                "Lista: agregar al inicio y al final");
        verificar(lista.eliminar("c") && !lista.contiene("c") && lista.tamano() == 2, "Lista: eliminar el último");
        lista.agregarAlFinal("d");
        verificar("d".equals(lista.obtener(2)), "Lista: la referencia al último se actualiza tras eliminar");
        verificar(lista.filtrar(s -> s.compareTo("b") >= 0).tamano() == 2, "Lista: filtrar");
    }

    private static void probarCola() {
        Cola<Integer> cola = new Cola<>();
        for (int i = 1; i <= 3; i++) {
            cola.encolar(i);
        }
        verificar(cola.desencolar() == 1 && cola.desencolar() == 2, "Cola: orden FIFO");
        cola.encolar(4);
        verificar(cola.verFrente() == 3 && cola.tamano() == 2, "Cola: frente y tamaño");
        cola.desencolar();
        cola.desencolar();
        verificar(lanzaVacia(cola::desencolar), "Cola: desencolar vacía lanza excepción");
    }

    private static void probarPila() {
        Pila<String> pila = new Pila<>();
        pila.apilar("salida");
        pila.apilar("pasillo");
        pila.apilar("origen");
        Pila<String> copia = pila.copiar();
        verificar("origen".equals(pila.desapilar()) && "pasillo".equals(pila.verTope()), "Pila: orden LIFO");
        verificar(copia.tamano() == 3 && "origen".equals(copia.verTope()), "Pila: la copia es independiente");
        pila.limpiar();
        verificar(lanzaVacia(pila::desapilar), "Pila: desapilar vacía lanza excepción");
    }

    private static void probarTablaHash() {
        TablaHash<String, Integer> tabla = new TablaHash<>(4);
        for (int i = 0; i < 1000; i++) {
            tabla.insertar("P" + i, i);
        }
        verificar(tabla.tamano() == 1000 && tabla.obtener("P742") == 742, "Hash: 1000 inserciones con rehash");
        verificar(tabla.factorCarga() <= 0.75, "Hash: factor de carga se mantiene <= 0.75");
        tabla.insertar("P5", 55);
        verificar(tabla.obtener("P5") == 55 && tabla.tamano() == 1000, "Hash: actualizar no duplica");
        verificar(tabla.eliminar("P5") == 55 && !tabla.contiene("P5"), "Hash: eliminar");
        verificar(tabla.obtener("NO_EXISTE") == null, "Hash: clave inexistente devuelve null");
    }

    private static void probarArbolBinario() {
        ArbolBinario<Integer, String> arbol = new ArbolBinario<>();
        int[] claves = {50, 30, 70, 20, 40, 60, 80, 40};
        for (int clave : claves) {
            arbol.insertar(clave, "z" + clave);
        }
        verificar(arbol.inOrden().toString().equals("[z20, z30, z40, z40, z50, z60, z70, z80]"),
                "Árbol: inOrden ordenado (con duplicados)");
        verificar("z80".equals(arbol.inOrdenDescendente().primero()), "Árbol: inOrden inverso empieza por el mayor");
        verificar(arbol.preOrden().primero().equals("z50") && arbol.postOrden().obtener(7).equals("z50"),
                "Árbol: preorden y postorden");
        verificar(arbol.altura() == 4 && "z20".equals(arbol.minimo()) && "z80".equals(arbol.maximo()),
                "Árbol: altura, mínimo y máximo");
        ArbolBinario<Integer, String> degenerado = new ArbolBinario<>();
        for (int i = 1; i <= 15; i++) {
            degenerado.insertar(i, "v" + i);
        }
        int alturaAntes = degenerado.altura();
        degenerado.rebalancear();
        verificar(alturaAntes == 15 && degenerado.altura() == 4 && degenerado.inOrden().tamano() == 15
                        && degenerado.buscar(11).equals("v11"),
                "Árbol: rebalancear reduce la altura de 15 a 4 y conserva el orden");
        arbol.eliminar(30);
        arbol.eliminar(50);
        verificar(arbol.inOrden().toString().equals("[z20, z40, z40, z60, z70, z80]") && arbol.tamano() == 6,
                "Árbol: eliminar nodos con dos hijos");
    }

    private static void probarMonticulo() {
        MonticuloMinimo<String> monticulo = new MonticuloMinimo<>();
        double[] prioridades = {9, 3, 7, 1, 8, 2, 6, 5, 4, 0, 11, 10, 12, 15, 14, 13, 16};
        for (double p : prioridades) {
            monticulo.insertar("e" + (int) p, p);
        }
        boolean ordenado = true;
        double anterior = -1;
        while (!monticulo.estaVacio()) {
            double actual = monticulo.prioridadMinima();
            monticulo.extraerMinimo();
            ordenado &= actual >= anterior;
            anterior = actual;
        }
        verificar(ordenado, "Montículo: extrae en orden creciente (con crecimiento del arreglo)");
    }

    // ================================================================== algoritmos

    private static void probarDijkstraYBfs() {
        Grafo grafo = EdificioDemo.crearGrafo();
        verificar(grafo.cantidadUbicaciones() == 17 && grafo.aristas().tamano() == 22, "Grafo: 17 nodos y 22 conexiones");

        Ruta dijkstra = new Dijkstra().rutaASalidaMasCercana(grafo, grafo.ubicacion("H301"));
        verificar(dijkstra.getTiempoTotal() == 31 && "EXIT_B".equals(dijkstra.getDestino().getId()),
                "Dijkstra: H301 sale por EXIT_B en 31 s");

        Ruta rapida = new Dijkstra().rutaASalidaMasCercana(grafo, grafo.ubicacion("H101"));
        Ruta corta = new BusquedaAnchura().rutaASalidaMasCercana(grafo, grafo.ubicacion("H101"));
        verificar(rapida.getTiempoTotal() == 13 && rapida.numeroTramos() == 3, "Dijkstra: H101 → 13 s, 3 tramos");
        verificar(corta.numeroTramos() == 2 && corta.getTiempoTotal() == 22, "BFS: H101 → 2 tramos, 22 s");

        ComparacionRutas comparacion = ComparacionRutas.comparar(grafo, grafo.ubicacion("H101"));
        verificar(comparacion.informe().contains("Dijkstra ahorra 9 s"), "Comparación: informe con la diferencia");

        Ruta puertaServicio = new Dijkstra().rutaASalidaMasCercana(grafo, grafo.ubicacion("H302"));
        verificar(puertaServicio.getTiempoTotal() == 29 && puertaServicio.descripcion().startsWith("H302 → ESC_B_P3"),
                "Dijkstra: H302 usa la puerta de servicio (29 s)");
    }

    private static void probarBloqueos() {
        EvacuacionService servicio = EdificioDemo.crear();
        servicio.bloquearArista("PAS3", "ESC_B_P3");
        PlanEvacuacion plan = servicio.evacuarDesde("H301");
        verificar(plan.tieneRutaSegura() && plan.getRutaSegura().getTiempoTotal() == 33
                        && "EXIT_A".equals(plan.getRutaSegura().getDestino().getId()),
                "Bloqueo: con humo en PAS3-ESC_B_P3, H301 cambia a EXIT_A (33 s)");
        servicio.desbloquearArista("PAS3", "ESC_B_P3");
        verificar(servicio.evacuarDesde("H301").getRutaSegura().getTiempoTotal() == 31, "Bloqueo: desbloquear restaura la ruta");
        verificar(servicio.getHistorialRutas().tamano() == 2, "Pila: historial guarda cada ruta calculada");
    }

    private static void probarHabitacionAislada() {
        EvacuacionService servicio = EdificioDemo.crear();
        servicio.bloquearArista("H301", "PAS3");
        PlanEvacuacion plan = servicio.evacuarDesde("H301");
        verificar(!plan.tieneRutaSegura() && plan.tieneRutaEmergencia() && plan.getRutaEmergencia().cruzaBloqueos(),
                "Caso borde: sin ruta libre se sugiere ruta de emergencia");
        verificar(plan.informe().contains("No existe ruta libre"), "Caso borde: mensaje de sin ruta");
    }

    private static void probarInstrucciones() {
        EvacuacionService servicio = EdificioDemo.crear();
        Pila<String> instrucciones = servicio.evacuarDesde("H301").getInstrucciones();
        verificar(instrucciones.desapilar().startsWith("Inicio: Habitación 301"), "Instrucciones: el tope es el inicio");
        verificar(instrucciones.desapilar().startsWith("1. Avance por el Pasillo del piso 3"), "Instrucciones: primer paso");
        String ultimo = "";
        while (!instrucciones.estaVacia()) {
            ultimo = instrucciones.desapilar();
        }
        verificar(ultimo.contains("Salida de emergencia B"), "Instrucciones: el último paso es la salida");
    }

    // ================================================================== simulación

    private static void probarSimulacionCompleta() {
        EvacuacionService servicio = EdificioDemo.crear();
        servicio.iniciarSimulacion();
        int turnos = 0;
        while (!servicio.getSimulador().estaTerminada() && turnos < 200) {
            servicio.avanzarTurno();
            turnos++;
        }
        verificar(servicio.getSimulador().evacuados() == 8, "Simulación: las 8 personas son evacuadas");
        verificar(servicio.getTablaPersonas().obtener("P01").estaEvacuado(), "Simulación: hash marca evacuado = sí");
        verificar(turnos > 8, "Simulación: termina en " + turnos + " turnos (hay espera en colas)");
        servicio.reiniciarSimulacion();
        verificar(servicio.getSimulador().evacuados() == 0
                        && "H301".equals(servicio.buscarPersona("P01").getUbicacionActual().getId()),
                "Simulación: reiniciar devuelve a todos a su habitación");
    }

    private static void probarBloqueoDuranteSimulacion() {
        EvacuacionService servicio = EdificioDemo.crear();
        servicio.iniciarSimulacion();
        servicio.bloquearArista("H102", "PAS1");
        Persona pedro = servicio.buscarPersona("P08");
        verificar(pedro.estaAtrapado(), "Simulación: quien queda aislado pasa a ATRAPADO");
        for (int i = 0; i < 3; i++) {
            servicio.avanzarTurno();
        }
        servicio.desbloquearArista("H102", "PAS1");
        verificar(!pedro.estaAtrapado(), "Simulación: al despejar recupera su ruta");
        while (!servicio.getSimulador().estaTerminada()) {
            servicio.avanzarTurno();
        }
        verificar(servicio.getSimulador().evacuados() == 8, "Simulación: todos salen tras el incidente");
    }

    private static void probarZonas() {
        EvacuacionService servicio = EdificioDemo.crear();
        servicio.bloquearArista("H301", "PAS3");
        servicio.bloquearArista("PAS3", "ESC_A_P3");
        ListaEnlazada<Zona> prioridad = servicio.getGestorZonas().zonasPorPrioridad();
        verificar("Piso 3".equals(prioridad.primero().getNombre()) && prioridad.primero().getRiesgoActual() == 30 + 50 + 15,
                "Zonas: Piso 3 es la más riesgosa (30 + 2×25 + 3×5 = 95)");
        verificar(servicio.informeZonas().contains("inOrden"), "Zonas: reporte del árbol");
    }

    private static void probarConsultasDePersonas() {
        EvacuacionService servicio = EdificioDemo.crear();
        verificar(servicio.personasQueQuedanEnPiso(3).tamano() == 3, "Lista: quedan 3 personas en el piso 3");
        verificar(servicio.buscarPersona("p04") != null && servicio.buscarPersona("P04").getPiso() == 2,
                "Hash: búsqueda por ID sin distinguir mayúsculas");
        boolean rechazaDuplicado;
        try {
            servicio.registrarPersona("P01", "Otra", "H101");
            rechazaDuplicado = false;
        } catch (IllegalArgumentException e) {
            rechazaDuplicado = true;
        }
        verificar(rechazaDuplicado, "Hash: no permite IDs repetidos");
        verificar(servicio.mapaAscii().contains("H301 (2)"), "Mapa ASCII: muestra ocupación por celda");
    }

    // ================================================================== utilidades

    private static boolean lanzaVacia(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (EstructuraVaciaException e) {
            return true;
        }
    }

    private static void verificar(boolean condicion, String descripcion) {
        if (condicion) {
            aprobadas++;
            System.out.println("  ✔ " + descripcion);
        } else {
            fallidas++;
            System.out.println("  ✘ FALLA: " + descripcion);
        }
    }
}
