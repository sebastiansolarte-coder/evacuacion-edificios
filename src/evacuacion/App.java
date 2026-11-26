package evacuacion;

import evacuacion.datos.EdificioDemo;
import evacuacion.servicio.EvacuacionService;
import evacuacion.vista.consola.MenuConsola;
import evacuacion.vista.gui.VentanaPrincipal;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

/**
 * Punto de entrada del simulador de evacuación.
 *
 * <ul>
 *   <li>{@code java -jar evacuacion.jar} → interfaz gráfica (Swing).</li>
 *   <li>{@code java -jar evacuacion.jar --consola} → menú de texto.</li>
 * </ul>
 */
public final class App {

    private static final String OPCION_CONSOLA = "--consola";

    private App() {
    }

    public static void main(String[] args) {
        EvacuacionService servicio = EdificioDemo.crear();
        if (pidioConsola(args) || GraphicsEnvironment.isHeadless()) {
            new MenuConsola(servicio).iniciar();
        } else {
            SwingUtilities.invokeLater(() -> VentanaPrincipal.mostrar(servicio));
        }
    }

    private static boolean pidioConsola(String[] args) {
        for (String argumento : args) {
            if (OPCION_CONSOLA.equalsIgnoreCase(argumento)) {
                return true;
            }
        }
        return false;
    }
}
