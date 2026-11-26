package evacuacion.modelo;

import evacuacion.estructuras.Iterador;
import evacuacion.estructuras.ListaEnlazada;
import evacuacion.estructuras.Pila;

/**
 * Resultado de un algoritmo de rutas: secuencia de ubicaciones desde un origen hasta una salida.
 *
 * <p>Los pasos se guardan en una {@link Pila} con el <b>origen en el tope</b>: así se construye
 * caminando hacia atrás desde la salida (siguiendo predecesores) y al desapilar se recorre en
 * el orden en que la persona camina.</p>
 */
public class Ruta {

    private final Pila<Ubicacion> pasos;
    private final Ubicacion origen;
    private final Ubicacion destino;
    private final double tiempoTotal;
    private final String algoritmo;
    private final boolean cruzaBloqueos;

    public Ruta(Pila<Ubicacion> pasos, Ubicacion origen, Ubicacion destino,
                double tiempoTotal, String algoritmo, boolean cruzaBloqueos) {
        this.pasos = pasos;
        this.origen = origen;
        this.destino = destino;
        this.tiempoTotal = tiempoTotal;
        this.algoritmo = algoritmo;
        this.cruzaBloqueos = cruzaBloqueos;
    }

    /** Ruta vacía: se usa cuando no hay camino desde el origen a ninguna salida. */
    public static Ruta inexistente(Ubicacion origen, String algoritmo) {
        return new Ruta(new Pila<>(), origen, null, Double.POSITIVE_INFINITY, algoritmo, false);
    }

    public boolean existe() {
        return destino != null;
    }

    /** @return número de tramos (aristas) recorridos. */
    public int numeroTramos() {
        return existe() ? pasos.tamano() - 1 : 0;
    }

    /** @return una copia de la pila de pasos (el tope es el origen); la ruta no se modifica. */
    public Pila<Ubicacion> copiarPasos() {
        return pasos.copiar();
    }

    /** @return los pasos en orden de recorrido (origen → salida). */
    public ListaEnlazada<Ubicacion> pasosEnOrden() {
        ListaEnlazada<Ubicacion> enOrden = new ListaEnlazada<>();
        Iterador<Ubicacion> it = pasos.iterador();
        while (it.tieneSiguiente()) {
            enOrden.agregarAlFinal(it.siguiente());
        }
        return enOrden;
    }

    /** @return {@code true} si la ruta pasa por la arista que une a {@code u} y {@code v}. */
    public boolean usaTramo(Ubicacion u, Ubicacion v) {
        Ubicacion anterior = null;
        Iterador<Ubicacion> it = pasos.iterador();
        while (it.tieneSiguiente()) {
            Ubicacion actual = it.siguiente();
            if (anterior != null && ((anterior == u && actual == v) || (anterior == v && actual == u))) {
                return true;
            }
            anterior = actual;
        }
        return false;
    }

    /** "H301 → PAS3 → ESC_B_P3 → … → EXIT_B" */
    public String descripcion() {
        if (!existe()) {
            return "(sin ruta)";
        }
        StringBuilder sb = new StringBuilder();
        Iterador<Ubicacion> it = pasos.iterador();
        while (it.tieneSiguiente()) {
            sb.append(it.siguiente().getId());
            if (it.tieneSiguiente()) {
                sb.append(" → ");
            }
        }
        return sb.toString();
    }

    /** Línea compacta con algoritmo, camino, tramos, tiempo y salida. */
    public String resumen() {
        if (!existe()) {
            return algoritmo + ": no existe ruta desde " + origen.getId();
        }
        return algoritmo + ": " + descripcion() + " | " + numeroTramos() + " tramos | "
                + Formato.tiempo(tiempoTotal) + " | salida " + destino.getId()
                + (cruzaBloqueos ? " | ⚠ cruza zonas bloqueadas" : "");
    }

    public Ubicacion getOrigen() {
        return origen;
    }

    public Ubicacion getDestino() {
        return destino;
    }

    public double getTiempoTotal() {
        return tiempoTotal;
    }

    public String getAlgoritmo() {
        return algoritmo;
    }

    public boolean cruzaBloqueos() {
        return cruzaBloqueos;
    }

    @Override
    public String toString() {
        return resumen();
    }
}
