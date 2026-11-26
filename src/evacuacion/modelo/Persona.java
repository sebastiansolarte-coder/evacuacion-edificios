package evacuacion.modelo;

import evacuacion.estructuras.Pila;

/**
 * Ocupante del edificio. Se registra en la tabla hash {@code idPersona → Persona}, que permite
 * consultar en O(1) promedio su piso, habitación y si ya fue evacuado.
 *
 * <p>Durante la simulación guarda su ruta pendiente en una {@link Pila}: el tope es el
 * siguiente nodo al que debe moverse.</p>
 */
public class Persona {

    private final String id;
    private final String nombre;
    private final Ubicacion ubicacionInicial;
    private Ubicacion ubicacionActual;
    private EstadoPersona estado;
    private Pila<Ubicacion> rutaPendiente;
    private int turnoEvacuacion;
    private int turnoUltimoMovimiento;

    public Persona(String id, String nombre, Ubicacion ubicacionInicial) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("La persona necesita un id");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("La persona necesita un nombre");
        }
        if (ubicacionInicial == null || ubicacionInicial.esSalida()) {
            throw new IllegalArgumentException("La persona debe estar dentro del edificio");
        }
        this.id = id;
        this.nombre = nombre;
        this.ubicacionInicial = ubicacionInicial;
        reiniciar();
    }

    /** Devuelve a la persona a su habitación, sin ruta y sin evacuar. */
    public void reiniciar() {
        ubicacionActual = ubicacionInicial;
        estado = EstadoPersona.EN_EDIFICIO;
        rutaPendiente = new Pila<>();
        turnoEvacuacion = -1;
        turnoUltimoMovimiento = -1;
    }

    /** @param pasos pila cuyo tope es el siguiente nodo a visitar (sin incluir el actual) */
    public void asignarRuta(Pila<Ubicacion> pasos) {
        rutaPendiente = pasos;
    }

    /** @return el siguiente nodo de la ruta o {@code null} si no tiene ruta. */
    public Ubicacion siguientePaso() {
        return rutaPendiente.estaVacia() ? null : rutaPendiente.verTope();
    }

    public int pasosRestantes() {
        return rutaPendiente.tamano();
    }

    /** Se mueve al siguiente nodo de la ruta (desapila) y queda registrado el turno. */
    public void avanzar(int turno) {
        ubicacionActual = rutaPendiente.desapilar();
        turnoUltimoMovimiento = turno;
        if (estado == EstadoPersona.EN_COLA) {
            estado = EstadoPersona.EN_EDIFICIO;
        }
    }

    /** Una persona se mueve como máximo una vez por turno y solo si no está en cola. */
    public boolean puedeMoverse(int turno) {
        return estado == EstadoPersona.EN_EDIFICIO && turnoUltimoMovimiento != turno;
    }

    public void ponerEnCola() {
        estado = EstadoPersona.EN_COLA;
    }

    public void salirDeCola() {
        if (estado == EstadoPersona.EN_COLA) {
            estado = EstadoPersona.EN_EDIFICIO;
        }
    }

    public void marcarEvacuado(int turno) {
        estado = EstadoPersona.EVACUADO;
        turnoEvacuacion = turno;
    }

    public void marcarAtrapado() {
        estado = EstadoPersona.ATRAPADO;
    }

    /** Una persona atrapada vuelve a moverse cuando aparece una ruta libre. */
    public void liberar() {
        if (estado == EstadoPersona.ATRAPADO) {
            estado = EstadoPersona.EN_EDIFICIO;
        }
    }

    public boolean estaEvacuado() {
        return estado == EstadoPersona.EVACUADO;
    }

    public boolean estaAtrapado() {
        return estado == EstadoPersona.ATRAPADO;
    }

    public boolean estaEnCola() {
        return estado == EstadoPersona.EN_COLA;
    }

    /** @return {@code true} si aún debe salir (no evacuado ni atrapado). */
    public boolean sigueEvacuando() {
        return estado == EstadoPersona.EN_EDIFICIO || estado == EstadoPersona.EN_COLA;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPiso() {
        return ubicacionActual.getPiso();
    }

    /** Habitación donde fue registrada la persona. */
    public Ubicacion getHabitacion() {
        return ubicacionInicial;
    }

    public Ubicacion getUbicacionActual() {
        return ubicacionActual;
    }

    public EstadoPersona getEstado() {
        return estado;
    }

    public int getTurnoEvacuacion() {
        return turnoEvacuacion;
    }

    /** Ficha completa: lo que devuelve la consulta por ID en la tabla hash. */
    public String ficha() {
        return "ID: " + id
                + " | Nombre: " + nombre
                + " | Piso: " + getPiso()
                + " | Habitación: " + ubicacionInicial.getId()
                + " | Ubicación actual: " + ubicacionActual.descripcion()
                + " | Evacuado: " + (estaEvacuado() ? "sí (turno " + turnoEvacuacion + ")" : "no")
                + " | Estado: " + estado.getEtiqueta();
    }

    @Override
    public String toString() {
        return id + " " + nombre;
    }
}
