package evacuacion.modelo;

/**
 * Conexión no dirigida entre dos ubicaciones, con un peso en segundos.
 *
 * <p>Una arista bloqueada (humo, fuego) se comporta como si no existiera para los algoritmos
 * de rutas seguras. Se marca en lugar de borrarse para poder desbloquearla después y para
 * sugerir rutas de emergencia que la atraviesen. El mismo objeto está en la lista de
 * adyacencia de ambos extremos, así que bloquearlo una vez afecta los dos sentidos.</p>
 */
public class Arista {

    private final Ubicacion extremoA;
    private final Ubicacion extremoB;
    private final double tiempo;
    private boolean bloqueada;

    public Arista(Ubicacion extremoA, Ubicacion extremoB, double tiempo) {
        if (extremoA == null || extremoB == null || extremoA == extremoB) {
            throw new IllegalArgumentException("Una arista une dos ubicaciones distintas");
        }
        if (tiempo <= 0) {
            throw new IllegalArgumentException("El tiempo de una arista debe ser positivo");
        }
        this.extremoA = extremoA;
        this.extremoB = extremoB;
        this.tiempo = tiempo;
    }

    /** @return el extremo opuesto a {@code desde}. */
    public Ubicacion otroExtremo(Ubicacion desde) {
        if (desde == extremoA) {
            return extremoB;
        }
        if (desde == extremoB) {
            return extremoA;
        }
        throw new IllegalArgumentException(desde + " no pertenece a la arista " + this);
    }

    /** @return {@code true} si la arista une a {@code u} y {@code v} (en cualquier orden). */
    public boolean conecta(Ubicacion u, Ubicacion v) {
        return (extremoA == u && extremoB == v) || (extremoA == v && extremoB == u);
    }

    public boolean toca(Ubicacion u) {
        return extremoA == u || extremoB == u;
    }

    public void bloquear() {
        bloqueada = true;
    }

    public void desbloquear() {
        bloqueada = false;
    }

    public boolean estaBloqueada() {
        return bloqueada;
    }

    public double getTiempo() {
        return tiempo;
    }

    public Ubicacion getExtremoA() {
        return extremoA;
    }

    public Ubicacion getExtremoB() {
        return extremoB;
    }

    @Override
    public String toString() {
        return extremoA.getId() + " <-> " + extremoB.getId() + " (" + Formato.tiempo(tiempo) + ")"
                + (bloqueada ? " [BLOQUEADA]" : "");
    }
}
