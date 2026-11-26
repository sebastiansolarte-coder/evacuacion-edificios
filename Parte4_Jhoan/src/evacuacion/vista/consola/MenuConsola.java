package evacuacion.vista.consola;

import evacuacion.estructuras.Iterador;
import evacuacion.estructuras.ListaEnlazada;
import evacuacion.estructuras.Pila;
import evacuacion.modelo.Arista;
import evacuacion.modelo.Persona;
import evacuacion.modelo.Ruta;
import evacuacion.servicio.EvacuacionService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Interfaz de texto del simulador. Todas las operaciones se delegan a {@link EvacuacionService}.
 */
public class MenuConsola {

    private static final int MAXIMO_TURNOS_AUTOMATICOS = 500;
    private static final String ARCHIVO_PLAN = "plan_evacuacion.txt";

    private final EvacuacionService servicio;
    private final BufferedReader entrada;
    private final PrintStream salida;

    public MenuConsola(EvacuacionService servicio) {
        this.servicio = servicio;
        this.entrada = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        this.salida = System.out;
    }

    public void iniciar() {
        salida.println("\n=== SIMULADOR DE RUTAS DE EVACUACIÓN EN EDIFICIOS ===");
        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            String opcion = leerLinea("Opción: ");
            if (opcion == null) {
                return; // fin de la entrada estándar
            }
            try {
                continuar = ejecutar(opcion.trim());
            } catch (IllegalArgumentException | IllegalStateException e) {
                salida.println("✗ " + e.getMessage());
            }
        }
        salida.println("¡Hasta luego!");
    }

    private void mostrarMenu() {
        salida.println();
        salida.println(" 1. Ver plano ASCII del edificio (matriz pisos × columnas)");
        salida.println(" 2. Ruta más segura desde una ubicación (Dijkstra + instrucciones)");
        salida.println(" 3. Comparar BFS vs Dijkstra");
        salida.println(" 4. Bloquear conexión (humo / fuego)");
        salida.println(" 5. Desbloquear conexión");
        salida.println(" 6. Registrar persona");
        salida.println(" 7. Buscar persona por ID (tabla hash)");
        salida.println(" 8. ¿Quién queda en un piso? (lista enlazada)");
        salida.println(" 9. Reporte de zonas por riesgo (árbol binario)");
        salida.println("10. Iniciar / reiniciar simulación");
        salida.println("11. Avanzar un turno");
        salida.println("12. Simular hasta el final");
        salida.println("13. Historial de rutas calculadas (pila)");
        salida.println("14. Ver lista de adyacencia del grafo");
        salida.println("15. Exportar plan de evacuación a " + ARCHIVO_PLAN);
        salida.println(" 0. Salir");
    }

    private boolean ejecutar(String opcion) {
        switch (opcion) {
            case "1" -> salida.println(servicio.mapaAscii());
            case "2" -> salida.println(servicio.evacuarDesde(leerTexto("Id de la ubicación (ej. H301): ")).informe());
            case "3" -> salida.println(servicio.compararAlgoritmos(leerTexto("Id de la ubicación (ej. H101): ")).informe());
            case "4" -> cambiarBloqueo(true);
            case "5" -> cambiarBloqueo(false);
            case "6" -> registrarPersona();
            case "7" -> buscarPersona();
            case "8" -> listarPiso();
            case "9" -> salida.println(servicio.informeZonas());
            case "10" -> imprimir(servicio.iniciarSimulacion());
            case "11" -> imprimir(servicio.avanzarTurno());
            case "12" -> simularHastaElFinal();
            case "13" -> mostrarHistorial();
            case "14" -> salida.println(servicio.getGrafo().describirAdyacencia());
            case "15" -> exportarPlan();
            case "0" -> {
                return false;
            }
            default -> salida.println("Opción no válida");
        }
        return true;
    }

    private void cambiarBloqueo(boolean bloquear) {
        ListaEnlazada<Arista> candidatas = servicio.getGrafo().aristas()
                .filtrar(a -> a.estaBloqueada() != bloquear);
        if (candidatas.estaVacia()) {
            salida.println(bloquear ? "Todas las conexiones ya están bloqueadas" : "No hay conexiones bloqueadas");
            return;
        }
        Iterador<Arista> it = candidatas.iterador();
        for (int i = 1; it.tieneSiguiente(); i++) {
            salida.printf("%3d. %s%n", i, it.siguiente());
        }
        int eleccion = leerEntero("Número de la conexión: ");
        if (eleccion < 1 || eleccion > candidatas.tamano()) {
            salida.println("Número fuera de rango");
            return;
        }
        Arista arista = candidatas.obtener(eleccion - 1);
        imprimir(servicio.alternarBloqueo(arista));
    }

    private void registrarPersona() {
        String id = leerTexto("ID: ");
        String nombre = leerTexto("Nombre: ");
        String habitacion = leerTexto("Habitación (ej. H202): ");
        Persona persona = servicio.registrarPersona(id, nombre, habitacion);
        salida.println("✓ Registrada → " + persona.ficha());
    }

    private void buscarPersona() {
        Persona persona = servicio.buscarPersona(leerTexto("ID a buscar: "));
        salida.println(persona == null ? "No existe una persona con ese ID" : persona.ficha());
    }

    private void listarPiso() {
        int piso = leerEntero("Piso: ");
        ListaEnlazada<Persona> quedan = servicio.personasQueQuedanEnPiso(piso);
        salida.println("Quedan " + quedan.tamano() + " persona(s) en el piso " + piso + ":");
        quedan.paraCada(p -> salida.println("  • " + p.ficha()));
    }

    private void simularHastaElFinal() {
        if (!servicio.getSimulador().estaIniciada()) {
            imprimir(servicio.iniciarSimulacion());
        }
        for (int i = 0; i < MAXIMO_TURNOS_AUTOMATICOS && !servicio.getSimulador().estaTerminada(); i++) {
            imprimir(servicio.avanzarTurno());
        }
        salida.println(servicio.mapaAscii());
    }

    private void mostrarHistorial() {
        Pila<Ruta> historial = servicio.getHistorialRutas();
        if (historial.estaVacia()) {
            salida.println("Aún no se han calculado rutas");
            return;
        }
        salida.println("Rutas calculadas (tope = la más reciente):");
        Iterador<Ruta> it = historial.iterador();
        for (int i = 1; it.tieneSiguiente(); i++) {
            salida.println("  " + i + ". " + it.siguiente().resumen());
        }
    }

    private void exportarPlan() {
        try {
            File archivo = servicio.exportarPlan(new File(ARCHIVO_PLAN));
            salida.println("✓ Plan exportado en " + archivo.getAbsolutePath());
        } catch (IOException e) {
            salida.println("✗ No se pudo exportar: " + e.getMessage());
        }
    }

    private void imprimir(ListaEnlazada<String> eventos) {
        eventos.paraCada(salida::println);
    }

    private String leerLinea(String mensaje) {
        salida.print(mensaje);
        try {
            return entrada.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private String leerTexto(String mensaje) {
        String texto = leerLinea(mensaje);
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Se esperaba un valor");
        }
        return texto.trim();
    }

    private int leerEntero(String mensaje) {
        String texto = leerTexto(mensaje);
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + texto + "' no es un número");
        }
    }
}
