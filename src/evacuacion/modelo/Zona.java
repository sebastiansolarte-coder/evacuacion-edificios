package evacuacion.modelo;

import evacuacion.estructuras.ListaEnlazada;

/**
 * Agrupación de ubicaciones (un piso, una escalera, el lobby) con un nivel de riesgo.
 *
 * <p>El riesgo actual se recalcula como:</p>
 * <pre>
 *   riesgo = riesgoBase + pesoHumo × conexionesBloqueadas + pesoPersona × personasRestantes
 * </pre>
 * <p>y es la clave con la que la zona se guarda en el árbol binario de prioridades.</p>
 */
public class Zona {

    private final String nombre;
    private final int riesgoBase;
    private final ListaEnlazada<Ubicacion> ubicaciones = new ListaEnlazada<>();
    private int riesgoActual;
    private int conexionesBloqueadas;
    private int personasRestantes;

    public Zona(String nombre, int riesgoBase) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("La zona necesita un nombre");
        }
        if (riesgoBase < 0) {
            throw new IllegalArgumentException("El riesgo base no puede ser negativo");
        }
        this.nombre = nombre;
        this.riesgoBase = riesgoBase;
        this.riesgoActual = riesgoBase;
    }

    public void agregarUbicacion(Ubicacion ubicacion) {
        ubicaciones.agregarAlFinal(ubicacion);
    }

    public boolean contiene(Ubicacion ubicacion) {
        return ubicaciones.contiene(ubicacion);
    }

    public void reiniciarContadores() {
        conexionesBloqueadas = 0;
        personasRestantes = 0;
    }

    public void registrarConexionBloqueada() {
        conexionesBloqueadas++;
    }

    public void registrarPersonaRestante() {
        personasRestantes++;
    }

    public void actualizarRiesgo(int pesoPorConexionBloqueada, int pesoPorPersona) {
        riesgoActual = riesgoBase
                + pesoPorConexionBloqueada * conexionesBloqueadas
                + pesoPorPersona * personasRestantes;
    }

    public String getNombre() {
        return nombre;
    }

    public int getRiesgoBase() {
        return riesgoBase;
    }

    public int getRiesgoActual() {
        return riesgoActual;
    }

    public int getConexionesBloqueadas() {
        return conexionesBloqueadas;
    }

    public int getPersonasRestantes() {
        return personasRestantes;
    }

    public ListaEnlazada<Ubicacion> getUbicaciones() {
        return ubicaciones;
    }

    /** Línea de reporte: riesgo, nombre, humo, personas y nodos que abarca. */
    public String resumen() {
        return String.format("riesgo %3d | %-12s | humo en %d conexión(es) | %d persona(s) | %s",
                riesgoActual, nombre, conexionesBloqueadas, personasRestantes, ubicaciones);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
