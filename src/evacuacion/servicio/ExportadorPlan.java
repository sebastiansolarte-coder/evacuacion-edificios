package evacuacion.servicio;

import evacuacion.algoritmos.ComparacionRutas;
import evacuacion.estructuras.Iterador;
import evacuacion.modelo.Formato;
import evacuacion.modelo.Persona;
import evacuacion.modelo.TipoUbicacion;
import evacuacion.modelo.Ubicacion;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Genera el plan de evacuación completo y lo guarda en un archivo de texto (.txt).
 */
public class ExportadorPlan {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SEPARADOR = Formato.linea('=', 100);

    /** Escribe el plan en {@code destino} (UTF-8) y devuelve el archivo creado. */
    public File exportar(EvacuacionService servicio, File destino) throws IOException {
        try (PrintWriter salida = new PrintWriter(destino, StandardCharsets.UTF_8)) {
            salida.print(generarTexto(servicio));
        }
        return destino;
    }

    public String generarTexto(EvacuacionService servicio) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARADOR).append('\n')
                .append("PLAN DE EVACUACIÓN DEL EDIFICIO — generado ").append(LocalDateTime.now().format(FORMATO_FECHA)).append('\n')
                .append(SEPARADOR).append("\n\n");

        sb.append("1. ").append(servicio.mapaAscii()).append('\n');

        sb.append("2. LISTA DE ADYACENCIA DEL GRAFO (X = conexión bloqueada)\n")
                .append(servicio.getGrafo().describirAdyacencia()).append('\n');

        sb.append("3. RUTAS POR HABITACIÓN\n");
        Iterador<Ubicacion> habitaciones = servicio.getGrafo().filtrarUbicaciones(TipoUbicacion.HABITACION).iterador();
        while (habitaciones.tieneSiguiente()) {
            Ubicacion habitacion = habitaciones.siguiente();
            sb.append(servicio.generarPlan(habitacion.getId()).informe());
            ComparacionRutas comparacion = servicio.compararAlgoritmos(habitacion.getId());
            sb.append("  ").append(comparacion.getRutaBfs().resumen()).append("\n\n");
        }

        sb.append("4. PERSONAS REGISTRADAS (tabla hash idPersona → datos)\n");
        Iterador<Persona> personas = servicio.getPersonas().iterador();
        while (personas.tieneSiguiente()) {
            sb.append("  ").append(personas.siguiente().ficha()).append('\n');
        }
        sb.append('\n');

        sb.append("5. ").append(servicio.informeZonas()).append('\n');

        sb.append("6. ESTADO DE LA SIMULACIÓN\n  ").append(servicio.getSimulador().resumen()).append('\n');
        return sb.toString();
    }
}
