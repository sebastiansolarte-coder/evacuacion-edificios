package evacuacion.datos;

import evacuacion.modelo.Grafo;
import evacuacion.modelo.TipoUbicacion;
import evacuacion.modelo.Ubicacion;
import evacuacion.servicio.EvacuacionService;

/**
 * Crea el edificio de ejemplo: 3 pisos, 2 escaleras (A y B), 2 salidas (EXIT_A y EXIT_B),
 * 17 ubicaciones, 22 conexiones, 8 personas y 5 zonas de riesgo.
 *
 * <pre>
 * Columna:   0        1          2        3        4        5          6
 * PISO 3            ESC_A_P3    H301     PAS3     H302    ESC_B_P3
 * PISO 2            ESC_A_P2    H201     PAS2     H202    ESC_B_P2
 * PISO 1   EXIT_A   ESC_A_P1    H101     PAS1     H102    ESC_B_P1   EXIT_B
 * </pre>
 *
 * <p>Los pesos están pensados para que BFS y Dijkstra a veces difieran: desde H101, BFS
 * prefiere el largo pasillo PAS1 → EXIT_A (2 tramos, 22 s) mientras Dijkstra usa la escalera A
 * (3 tramos, 13 s).</p>
 */
public final class EdificioDemo {

    private static final int PISOS = 3;

    // Tiempos en segundos
    private static final double HABITACION_1_A_PASILLO = 4;
    private static final double HABITACION_2_A_PASILLO = 6;
    private static final double PASILLO_A_ESCALERA_A = 5;
    private static final double PASILLO_A_ESCALERA_B = 7;
    private static final double TRAMO_ESCALERA_A = 10;
    private static final double TRAMO_ESCALERA_B = 8;
    private static final double DESCANSO_A_SALIDA = 4;
    private static final double LOBBY_A_SALIDA_A = 18; // pocos pasos pero lento
    private static final double LOBBY_A_SALIDA_B = 15;
    private static final double PUERTA_SERVICIO_H302 = 9;
    private static final double PUERTA_SERVICIO_H201 = 8;

    private EdificioDemo() {
    }

    /** @return un servicio listo para usar con el edificio, las personas y las zonas de ejemplo. */
    public static EvacuacionService crear() {
        EvacuacionService servicio = new EvacuacionService(crearGrafo());
        crearZonas(servicio);
        registrarPersonas(servicio);
        servicio.getGestorZonas().recalcularRiesgos(servicio.getGrafo(), servicio.getPersonas());
        return servicio;
    }

    public static Grafo crearGrafo() {
        Grafo grafo = new Grafo();
        for (int piso = PISOS; piso >= 1; piso--) {
            agregarPiso(grafo, piso);
        }
        grafo.agregarUbicacion(new Ubicacion("EXIT_A", "Salida de emergencia A", TipoUbicacion.SALIDA, 1, 0));
        grafo.agregarUbicacion(new Ubicacion("EXIT_B", "Salida de emergencia B", TipoUbicacion.SALIDA, 1, 6));

        for (int piso = PISOS; piso >= 1; piso--) {
            conectarPiso(grafo, piso);
        }
        conectarEscaleras(grafo);
        conectarPlantaBaja(grafo);
        conectarPuertasDeServicio(grafo);
        return grafo;
    }

    private static void agregarPiso(Grafo grafo, int piso) {
        grafo.agregarUbicacion(new Ubicacion(escaleraA(piso), "Escalera A", TipoUbicacion.ESCALERA, piso, 1));
        grafo.agregarUbicacion(new Ubicacion(habitacion(piso, 1), "Habitación " + piso + "01", TipoUbicacion.HABITACION, piso, 2));
        grafo.agregarUbicacion(new Ubicacion(pasillo(piso), "Pasillo del piso " + piso, TipoUbicacion.PASILLO, piso, 3));
        grafo.agregarUbicacion(new Ubicacion(habitacion(piso, 2), "Habitación " + piso + "02", TipoUbicacion.HABITACION, piso, 4));
        grafo.agregarUbicacion(new Ubicacion(escaleraB(piso), "Escalera B", TipoUbicacion.ESCALERA, piso, 5));
    }

    private static void conectarPiso(Grafo grafo, int piso) {
        grafo.conectar(habitacion(piso, 1), pasillo(piso), HABITACION_1_A_PASILLO);
        grafo.conectar(habitacion(piso, 2), pasillo(piso), HABITACION_2_A_PASILLO);
        grafo.conectar(pasillo(piso), escaleraA(piso), PASILLO_A_ESCALERA_A);
        grafo.conectar(pasillo(piso), escaleraB(piso), PASILLO_A_ESCALERA_B);
    }

    private static void conectarEscaleras(Grafo grafo) {
        for (int piso = PISOS; piso > 1; piso--) {
            grafo.conectar(escaleraA(piso), escaleraA(piso - 1), TRAMO_ESCALERA_A);
            grafo.conectar(escaleraB(piso), escaleraB(piso - 1), TRAMO_ESCALERA_B);
        }
    }

    private static void conectarPlantaBaja(Grafo grafo) {
        grafo.conectar(escaleraA(1), "EXIT_A", DESCANSO_A_SALIDA);
        grafo.conectar(escaleraB(1), "EXIT_B", DESCANSO_A_SALIDA);
        grafo.conectar(pasillo(1), "EXIT_A", LOBBY_A_SALIDA_A);
        grafo.conectar(pasillo(1), "EXIT_B", LOBBY_A_SALIDA_B);
    }

    /** Rutas alternativas que no pasan por el pasillo (útiles cuando hay humo). */
    private static void conectarPuertasDeServicio(Grafo grafo) {
        grafo.conectar(habitacion(3, 2), escaleraB(3), PUERTA_SERVICIO_H302);
        grafo.conectar(habitacion(2, 1), escaleraA(2), PUERTA_SERVICIO_H201);
    }

    private static void crearZonas(EvacuacionService servicio) {
        servicio.crearZona("Piso 3", 30, habitacion(3, 1), habitacion(3, 2), pasillo(3));
        servicio.crearZona("Piso 2", 20, habitacion(2, 1), habitacion(2, 2), pasillo(2));
        servicio.crearZona("Piso 1", 10, habitacion(1, 1), habitacion(1, 2), pasillo(1), "EXIT_A", "EXIT_B");
        servicio.crearZona("Escalera A", 15, escaleraA(3), escaleraA(2), escaleraA(1));
        servicio.crearZona("Escalera B", 15, escaleraB(3), escaleraB(2), escaleraB(1));
    }

    private static void registrarPersonas(EvacuacionService servicio) {
        servicio.registrarPersona("P01", "Ana Torres", "H301");
        servicio.registrarPersona("P02", "Luis Gómez", "H301");
        servicio.registrarPersona("P03", "Marta Ruiz", "H302");
        servicio.registrarPersona("P04", "Carlos Díaz", "H201");
        servicio.registrarPersona("P05", "Sofía Peña", "H202");
        servicio.registrarPersona("P06", "Jorge Ríos", "H202");
        servicio.registrarPersona("P07", "Elena Vargas", "H101");
        servicio.registrarPersona("P08", "Pedro Castro", "H102");
    }

    private static String habitacion(int piso, int numero) {
        return "H" + piso + "0" + numero;
    }

    private static String pasillo(int piso) {
        return "PAS" + piso;
    }

    private static String escaleraA(int piso) {
        return "ESC_A_P" + piso;
    }

    private static String escaleraB(int piso) {
        return "ESC_B_P" + piso;
    }
}
