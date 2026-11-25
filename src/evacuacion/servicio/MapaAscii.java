package evacuacion.servicio;

import evacuacion.estructuras.Iterador;
import evacuacion.estructuras.ListaEnlazada;
import evacuacion.estructuras.TablaHash;
import evacuacion.modelo.Arista;
import evacuacion.modelo.Formato;
import evacuacion.modelo.Grafo;
import evacuacion.modelo.Persona;
import evacuacion.modelo.PlanoEdificio;
import evacuacion.modelo.Ubicacion;

/**
 * Dibuja el edificio en ASCII recorriendo la matriz {@link PlanoEdificio} fila por fila (piso por piso).
 *
 * <pre>
 * PISO 3 |               ESC_A_P3       H301 (2)       PAS3           H302 (1)       ESC_B_P3
 * </pre>
 * <p>(n) = personas en el nodo, q = personas en cola, ~ = alguna conexión del nodo tiene humo.</p>
 */
public class MapaAscii {

    private static final int ANCHO_CELDA = 16;

    public String dibujar(PlanoEdificio plano, Grafo grafo, ListaEnlazada<Persona> personas) {
        TablaHash<String, Integer> personasPorUbicacion = contarPersonas(personas);
        StringBuilder sb = new StringBuilder();
        sb.append("PLANO DEL EDIFICIO — matriz ").append(plano.filas()).append(" pisos × ")
                .append(plano.columnas()).append(" columnas\n");
        String separador = Formato.linea('-', 9 + ANCHO_CELDA * plano.columnas());
        sb.append(separador).append('\n');
        for (int fila = 0; fila < plano.filas(); fila++) {
            sb.append("PISO ").append(plano.pisoDeFila(fila)).append(" | ");
            for (int columna = 0; columna < plano.columnas(); columna++) {
                Ubicacion celda = plano.celda(fila, columna);
                String texto = celda == null ? "." : textoCelda(celda, grafo, personasPorUbicacion);
                sb.append(Formato.ajustar(texto, ANCHO_CELDA));
            }
            sb.append('\n').append(separador).append('\n');
        }
        sb.append("Leyenda: (n) personas en el nodo · qN personas en cola · ~ humo/fuego en una conexión\n");
        sb.append("Conexiones bloqueadas: ").append(describirBloqueos(grafo)).append('\n');
        return sb.toString();
    }

    private String textoCelda(Ubicacion ubicacion, Grafo grafo, TablaHash<String, Integer> conteo) {
        StringBuilder sb = new StringBuilder(ubicacion.getId());
        int presentes = conteo.obtenerOPorDefecto(ubicacion.getId(), 0);
        int enCola = ubicacion.getColaEspera().tamano();
        if (presentes > 0) {
            sb.append(" (").append(presentes).append(')');
        }
        if (enCola > 0) {
            sb.append(" q").append(enCola);
        }
        if (grafo.tieneConexionBloqueada(ubicacion)) {
            sb.append(" ~");
        }
        return sb.toString();
    }

    /** Cuenta, con una tabla hash, cuántas personas no evacuadas hay en cada ubicación. O(P). */
    private TablaHash<String, Integer> contarPersonas(ListaEnlazada<Persona> personas) {
        TablaHash<String, Integer> conteo = new TablaHash<>();
        Iterador<Persona> it = personas.iterador();
        while (it.tieneSiguiente()) {
            Persona persona = it.siguiente();
            if (!persona.estaEvacuado() && !persona.estaEnCola()) {
                String id = persona.getUbicacionActual().getId();
                conteo.insertar(id, conteo.obtenerOPorDefecto(id, 0) + 1);
            }
        }
        return conteo;
    }

    private String describirBloqueos(Grafo grafo) {
        ListaEnlazada<Arista> bloqueadas = grafo.aristasBloqueadas();
        return bloqueadas.estaVacia() ? "ninguna" : bloqueadas.toString();
    }
}
