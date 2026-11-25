package evacuacion.servicio;

import evacuacion.estructuras.Pila;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.Ubicacion;

/**
 * Respuesta de {@link EvacuacionService#evacuarDesde(String)}: la ruta segura (si existe),
 * la ruta de emergencia sugerida cuando no hay ruta libre y las instrucciones paso a paso.
 */
public class PlanEvacuacion {

    private final Ubicacion origen;
    private final Ruta rutaSegura;
    private final Ruta rutaEmergencia;
    private final Pila<String> instrucciones;

    private PlanEvacuacion(Ubicacion origen, Ruta rutaSegura, Ruta rutaEmergencia, Pila<String> instrucciones) {
        this.origen = origen;
        this.rutaSegura = rutaSegura;
        this.rutaEmergencia = rutaEmergencia;
        this.instrucciones = instrucciones;
    }

    static PlanEvacuacion conRutaSegura(Ruta ruta, Pila<String> instrucciones) {
        return new PlanEvacuacion(ruta.getOrigen(), ruta, null, instrucciones);
    }

    static PlanEvacuacion sinRutaSegura(Ruta rutaInexistente, Ruta rutaEmergencia, Pila<String> instrucciones) {
        return new PlanEvacuacion(rutaInexistente.getOrigen(), rutaInexistente, rutaEmergencia, instrucciones);
    }

    public boolean tieneRutaSegura() {
        return rutaSegura.existe();
    }

    public boolean tieneRutaEmergencia() {
        return rutaEmergencia != null && rutaEmergencia.existe();
    }

    /** @return la ruta que conviene mostrar: la segura o, si no hay, la de emergencia (o null). */
    public Ruta rutaRecomendada() {
        if (tieneRutaSegura()) {
            return rutaSegura;
        }
        return tieneRutaEmergencia() ? rutaEmergencia : null;
    }

    public Ruta getRutaSegura() {
        return rutaSegura;
    }

    public Ruta getRutaEmergencia() {
        return rutaEmergencia;
    }

    public Ubicacion getOrigen() {
        return origen;
    }

    /** @return copia de la pila de instrucciones (el tope es el primer paso). */
    public Pila<String> getInstrucciones() {
        return instrucciones.copiar();
    }

    public String informe() {
        StringBuilder sb = new StringBuilder();
        sb.append("PLAN DE EVACUACIÓN DESDE ").append(origen.descripcion()).append(" [").append(origen.getId()).append("]\n");
        if (tieneRutaSegura()) {
            sb.append("  Ruta más segura y corta → ").append(rutaSegura.resumen()).append('\n');
        } else {
            sb.append("  ⚠ No existe ruta libre de humo/fuego desde ").append(origen.getId()).append(".\n");
            sb.append(sugerenciaSinRuta());
        }
        if (!instrucciones.estaVacia()) {
            sb.append("  Instrucciones (se desapilan de la pila):\n");
            Pila<String> copia = instrucciones.copiar();
            while (!copia.estaVacia()) {
                sb.append("     ").append(copia.desapilar()).append('\n');
            }
        }
        return sb.toString();
    }

    private String sugerenciaSinRuta() {
        if (tieneRutaEmergencia()) {
            return "  Ruta alternativa sugerida (cruza zonas bloqueadas; solo con brigada y equipo de protección):\n"
                    + "     " + rutaEmergencia.resumen() + '\n';
        }
        return "  La ubicación está completamente aislada. Sugerencia: permanecer en el lugar, cerrar y sellar\n"
                + "  puertas, hacer señales por la ventana y esperar a la brigada (ya fue priorizada).\n";
    }
}
