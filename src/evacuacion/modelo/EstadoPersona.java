package evacuacion.modelo;

/** Estado de una persona durante la simulación de desalojo. */
public enum EstadoPersona {

    EN_EDIFICIO("En el edificio"),
    EN_COLA("Esperando en cola"),
    EVACUADO("Evacuado"),
    ATRAPADO("Sin ruta - espera brigada");

    private final String etiqueta;

    EstadoPersona(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
