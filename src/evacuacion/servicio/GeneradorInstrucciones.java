package evacuacion.servicio;

import evacuacion.estructuras.Iterador;
import evacuacion.estructuras.Pila;
import evacuacion.modelo.Formato;
import evacuacion.modelo.Grafo;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.TipoUbicacion;
import evacuacion.modelo.Ubicacion;

/**
 * Convierte una ruta en instrucciones en lenguaje natural guardadas en una {@link Pila}.
 *
 * <p>Las instrucciones se apilan <b>desde la salida hacia el origen</b>; por eso el tope es la
 * primera instrucción y basta con desapilar para leerlas en orden:</p>
 * <pre>
 *   Inicio: Habitación 301
 *   1. Avance por el Pasillo del piso 3 (4 s)
 *   2. Entre a la Escalera B en el piso 3 (7 s)
 *   3. Baje por la Escalera B hasta el piso 2 (8 s)
 *   ...
 * </pre>
 */
public class GeneradorInstrucciones {

    private final Grafo grafo;

    public GeneradorInstrucciones(Grafo grafo) {
        this.grafo = grafo;
    }

    public Pila<String> generar(Ruta ruta) {
        Pila<String> instrucciones = new Pila<>();
        if (!ruta.existe()) {
            return instrucciones;
        }
        Ubicacion[] pasos = aArreglo(ruta);
        for (int i = pasos.length - 1; i >= 1; i--) {
            instrucciones.apilar(i + ". " + describirTramo(pasos[i - 1], pasos[i]));
        }
        instrucciones.apilar("Inicio: " + pasos[0].descripcion());
        return instrucciones;
    }

    private String describirTramo(Ubicacion desde, Ubicacion hacia) {
        String tiempo = " (" + Formato.tiempo(grafo.buscarArista(desde, hacia).getTiempo()) + ")";
        String advertencia = grafo.buscarArista(desde, hacia).estaBloqueada() ? " ⚠ HUMO: cubra boca y nariz" : "";
        return textoDelMovimiento(desde, hacia) + tiempo + advertencia;
    }

    private String textoDelMovimiento(Ubicacion desde, Ubicacion hacia) {
        switch (hacia.getTipo()) {
            case SALIDA:
                return "Salga del edificio por la " + hacia.getNombre();
            case ESCALERA:
                if (desde.getTipo() == TipoUbicacion.ESCALERA) {
                    String verbo = hacia.getPiso() < desde.getPiso() ? "Baje" : "Suba";
                    return verbo + " por la " + hacia.getNombre() + " hasta el piso " + hacia.getPiso();
                }
                return "Entre a la " + hacia.getNombre() + " en el piso " + hacia.getPiso();
            case PASILLO:
                return "Avance por el " + hacia.getNombre();
            default:
                return "Atraviese la " + hacia.getNombre();
        }
    }

    /** Copia la ruta a un arreglo para poder recorrerla de atrás hacia adelante. */
    private Ubicacion[] aArreglo(Ruta ruta) {
        Ubicacion[] pasos = new Ubicacion[ruta.numeroTramos() + 1];
        Iterador<Ubicacion> it = ruta.pasosEnOrden().iterador();
        for (int i = 0; it.tieneSiguiente(); i++) {
            pasos[i] = it.siguiente();
        }
        return pasos;
    }
}
