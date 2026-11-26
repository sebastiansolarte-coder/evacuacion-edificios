package evacuacion.vista.gui;

import evacuacion.algoritmos.ComparacionRutas;
import evacuacion.estructuras.Iterador;
import evacuacion.estructuras.ListaEnlazada;
import evacuacion.estructuras.Pila;
import evacuacion.modelo.Arista;
import evacuacion.modelo.Persona;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.Ubicacion;
import evacuacion.servicio.EvacuacionService;
import evacuacion.servicio.PlanEvacuacion;
import evacuacion.servicio.SimuladorEvacuacion;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Ventana principal de la interfaz gráfica: plano interactivo, panel de controles,
 * bitácora de eventos y barra de estado. Toda la lógica se delega a {@link EvacuacionService}.
 */
public class VentanaPrincipal extends JFrame implements PanelEdificio.OyenteClic {

    private static final int RETARDO_AUTOMATICO_MS = 700;
    private static final String ARCHIVO_PLAN = "plan_evacuacion.txt";
    private static final String ORIGEN_INICIAL = "H301";

    private final EvacuacionService servicio;
    private final PanelEdificio panelEdificio;
    private final JTextArea bitacora = new JTextArea();
    private final JLabel etiquetaEstado = new JLabel();
    private final JButton botonAutomatico = new JButton("▶ Automático");
    private final Timer temporizador;
    private final String[] idsOrigen;
    private final JComboBox<String> comboOrigen;

    private VentanaPrincipal(EvacuacionService servicio) {
        super("Simulador de rutas de evacuación — Proyecto 4");
        this.servicio = servicio;
        this.panelEdificio = new PanelEdificio(servicio);
        this.idsOrigen = idsDeUbicacionesNoSalida();
        this.comboOrigen = new JComboBox<>(etiquetasDeOrigen());
        this.temporizador = new Timer(RETARDO_AUTOMATICO_MS, e -> ejecutarTurno());
        panelEdificio.setOyente(this);
        seleccionarEnCombo(ORIGEN_INICIAL);
        construirInterfaz();
        actualizarEstado();
        registrar("Bienvenido. Haga clic en una ubicación para ver su ruta de evacuación, o sobre una "
                + "conexión para simular humo/fuego.\n");
    }

    /** Crea la ventana con el estilo Nimbus (si existe) y la muestra centrada. */
    public static void mostrar(EvacuacionService servicio) {
        aplicarEstiloNimbus();
        VentanaPrincipal ventana = new VentanaPrincipal(servicio);
        ventana.setDefaultCloseOperation(EXIT_ON_CLOSE);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    // ================================================================== construcción

    private void construirInterfaz() {
        bitacora.setEditable(false);
        bitacora.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        bitacora.setMargin(new java.awt.Insets(6, 8, 6, 8));

        JScrollPane scrollPlano = new JScrollPane(panelEdificio);
        JScrollPane scrollBitacora = new JScrollPane(bitacora);
        scrollBitacora.setBorder(BorderFactory.createTitledBorder("Bitácora"));
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPlano, scrollBitacora);
        division.setResizeWeight(0.7);
        division.setDividerLocation(650);

        etiquetaEstado.setBorder(new EmptyBorder(6, 12, 6, 12));
        etiquetaEstado.setFont(etiquetaEstado.getFont().deriveFont(Font.BOLD, 13f));

        JScrollPane scrollControles = new JScrollPane(crearPanelControles(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollControles.setBorder(null);

        setLayout(new BorderLayout());
        add(etiquetaEstado, BorderLayout.NORTH);
        add(division, BorderLayout.CENTER);
        add(scrollControles, BorderLayout.EAST);
        setPreferredSize(new Dimension(1340, 900));
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        panel.add(grupo("Rutas de evacuación (grafo)",
                new JLabel("Origen:"), comboOrigen,
                boton("Ruta más segura (Dijkstra)", e -> mostrarPlan(idSeleccionado())),
                boton("Comparar BFS vs Dijkstra", e -> compararAlgoritmos()),
                boton("Historial de rutas (pila)", e -> mostrarHistorial())));
        panel.add(grupo("Incidentes: humo / fuego",
                boton("Bloquear / despejar conexión…", e -> elegirConexion()),
                boton("Despejar todo", e -> registrarEventos(servicio.desbloquearTodo())),
                nota("Tip: clic sobre una línea del plano")));
        panel.add(grupo("Simulación por turnos (colas)",
                boton("Iniciar / reiniciar", e -> iniciarSimulacion()),
                boton("Siguiente turno", e -> ejecutarTurno()),
                botonAutomatico,
                boton("Volver al estado inicial", e -> reiniciarSimulacion())));
        botonAutomatico.addActionListener(e -> alternarAutomatico());
        panel.add(grupo("Personas (tabla hash)",
                boton("Registrar persona…", e -> registrarPersona()),
                boton("Buscar por ID…", e -> buscarPersona()),
                boton("¿Quién queda en un piso?", e -> listarPiso())));
        panel.add(grupo("Reportes",
                boton("Zonas por riesgo (árbol)", e -> registrar(servicio.informeZonas())),
                boton("Plano ASCII (matriz)", e -> registrar(servicio.mapaAscii())),
                boton("Lista de adyacencia", e -> registrar(servicio.getGrafo().describirAdyacencia())),
                boton("Exportar plan .txt…", e -> exportarPlan()),
                boton("Limpiar bitácora", e -> bitacora.setText(""))));
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel grupo(String titulo, JComponent... componentes) {
        JPanel grupo = new JPanel();
        grupo.setLayout(new BoxLayout(grupo, BoxLayout.Y_AXIS));
        grupo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titulo), new EmptyBorder(4, 6, 6, 6)));
        grupo.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent componente : componentes) {
            componente.setAlignmentX(Component.LEFT_ALIGNMENT);
            componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, componente.getPreferredSize().height));
            grupo.add(componente);
            grupo.add(Box.createVerticalStrut(4));
        }
        grupo.setMaximumSize(new Dimension(290, grupo.getPreferredSize().height));
        grupo.setPreferredSize(new Dimension(270, grupo.getPreferredSize().height));
        return grupo;
    }

    private JButton boton(String texto, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(accion);
        return boton;
    }

    private JLabel nota(String texto) {
        JLabel etiqueta = new JLabel("<html><i>" + texto + "</i></html>");
        etiqueta.setFont(etiqueta.getFont().deriveFont(11f));
        return etiqueta;
    }

    // ================================================================== clics sobre el plano

    @Override
    public void ubicacionSeleccionada(Ubicacion ubicacion) {
        if (ubicacion.esSalida()) {
            registrar(ubicacion.descripcion() + ": es una salida de emergencia.\n");
            return;
        }
        seleccionarEnCombo(ubicacion.getId());
        mostrarPlan(ubicacion.getId());
    }

    @Override
    public void aristaSeleccionada(Arista arista) {
        registrarEventos(servicio.alternarBloqueo(arista));
        refrescarRutaSeleccionada();
    }

    // ================================================================== acciones

    private void mostrarPlan(String idUbicacion) {
        PlanEvacuacion plan = servicio.evacuarDesde(idUbicacion);
        panelEdificio.resaltarRuta(plan.rutaRecomendada(), plan.getOrigen());
        registrar(plan.informe());
    }

    /** Tras un bloqueo, vuelve a calcular la ruta resaltada sin ensuciar el historial. */
    private void refrescarRutaSeleccionada() {
        PlanEvacuacion plan = servicio.generarPlan(idSeleccionado());
        panelEdificio.resaltarRuta(plan.rutaRecomendada(), plan.getOrigen());
        actualizarEstado();
    }

    private void compararAlgoritmos() {
        ComparacionRutas comparacion = servicio.compararAlgoritmos(idSeleccionado());
        panelEdificio.resaltarRuta(comparacion.getRutaDijkstra(), comparacion.getRutaDijkstra().getOrigen());
        registrar(comparacion.informe());
    }

    private void mostrarHistorial() {
        Pila<Ruta> historial = servicio.getHistorialRutas();
        if (historial.estaVacia()) {
            registrar("Aún no se han calculado rutas.\n");
            return;
        }
        StringBuilder sb = new StringBuilder("HISTORIAL DE RUTAS (tope = la más reciente, ")
                .append(historial.tamano()).append(" en total)\n");
        Iterador<Ruta> it = historial.iterador();
        for (int i = 1; it.tieneSiguiente(); i++) {
            sb.append("  ").append(i).append(". ").append(it.siguiente().resumen()).append('\n');
        }
        registrar(sb.toString());
    }

    private void elegirConexion() {
        ListaEnlazada<Arista> aristas = servicio.getGrafo().aristas();
        Arista[] opciones = new Arista[aristas.tamano()];
        String[] textos = new String[aristas.tamano()];
        Iterador<Arista> it = aristas.iterador();
        for (int i = 0; it.tieneSiguiente(); i++) {
            opciones[i] = it.siguiente();
            textos[i] = opciones[i].toString();
        }
        JComboBox<String> combo = new JComboBox<>(textos);
        int respuesta = JOptionPane.showConfirmDialog(this, combo, "Bloquear o despejar conexión",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta == JOptionPane.OK_OPTION) {
            aristaSeleccionada(opciones[combo.getSelectedIndex()]);
        }
    }

    private void iniciarSimulacion() {
        detenerAutomatico();
        registrarEventos(servicio.iniciarSimulacion());
    }

    private void ejecutarTurno() {
        SimuladorEvacuacion simulador = servicio.getSimulador();
        if (simulador.estaTerminada()) {
            detenerAutomatico();
            registrar("La simulación terminó. " + simulador.resumen() + "\n");
            return;
        }
        registrarEventos(servicio.avanzarTurno());
        if (simulador.estaTerminada()) {
            detenerAutomatico();
        }
    }

    private void alternarAutomatico() {
        if (temporizador.isRunning()) {
            detenerAutomatico();
        } else {
            if (servicio.getSimulador().estaTerminada()) {
                registrarEventos(servicio.iniciarSimulacion());
            }
            temporizador.start();
            botonAutomatico.setText("⏸ Pausar");
        }
    }

    private void detenerAutomatico() {
        temporizador.stop();
        botonAutomatico.setText("▶ Automático");
    }

    private void reiniciarSimulacion() {
        detenerAutomatico();
        servicio.reiniciarSimulacion();
        registrar("Simulación reiniciada: todos vuelven a su habitación.\n");
        actualizarEstado();
    }

    private void registrarPersona() {
        JTextField campoId = new JTextField();
        JTextField campoNombre = new JTextField();
        JComboBox<String> comboUbicacion = new JComboBox<>(idsOrigen);
        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("ID:"));
        formulario.add(campoId);
        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);
        formulario.add(new JLabel("Ubicación:"));
        formulario.add(comboUbicacion);
        int respuesta = JOptionPane.showConfirmDialog(this, formulario, "Registrar persona",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Persona persona = servicio.registrarPersona(campoId.getText(), campoNombre.getText(),
                    (String) comboUbicacion.getSelectedItem());
            registrar("✓ Persona registrada → " + persona.ficha() + "\n");
            actualizarEstado();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError(e.getMessage());
        }
    }

    private void buscarPersona() {
        String id = JOptionPane.showInputDialog(this, "ID de la persona (ej. P03):", "Buscar en tabla hash",
                JOptionPane.QUESTION_MESSAGE);
        if (id == null) {
            return;
        }
        Persona persona = servicio.buscarPersona(id);
        registrar(persona == null ? "No existe una persona con ID '" + id + "'.\n" : persona.ficha() + "\n");
    }

    private void listarPiso() {
        int pisos = servicio.getPlano().getPisoMaximo();
        Integer[] opciones = new Integer[pisos];
        for (int i = 0; i < pisos; i++) {
            opciones[i] = pisos - i;
        }
        Object piso = JOptionPane.showInputDialog(this, "Piso:", "¿Quién queda?", JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);
        if (piso == null) {
            return;
        }
        ListaEnlazada<Persona> quedan = servicio.personasQueQuedanEnPiso((Integer) piso);
        StringBuilder sb = new StringBuilder("Quedan " + quedan.tamano() + " persona(s) en el piso " + piso + ":\n");
        quedan.paraCada(p -> sb.append("  • ").append(p.ficha()).append('\n'));
        registrar(sb.toString());
    }

    private void exportarPlan() {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File(ARCHIVO_PLAN));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            File archivo = servicio.exportarPlan(selector.getSelectedFile());
            registrar("✓ Plan de evacuación exportado en " + archivo.getAbsolutePath() + "\n");
        } catch (IOException e) {
            mostrarError("No se pudo exportar: " + e.getMessage());
        }
    }

    // ================================================================== utilidades

    private void registrarEventos(ListaEnlazada<String> eventos) {
        StringBuilder sb = new StringBuilder();
        eventos.paraCada(evento -> sb.append(evento).append('\n'));
        registrar(sb.toString());
        actualizarEstado();
    }

    private void registrar(String texto) {
        bitacora.append(texto.endsWith("\n") ? texto + "\n" : texto + "\n\n");
        bitacora.setCaretPosition(bitacora.getDocument().getLength());
        panelEdificio.repaint();
    }

    private void actualizarEstado() {
        SimuladorEvacuacion simulador = servicio.getSimulador();
        String estado = !simulador.estaIniciada() ? "sin iniciar"
                : simulador.estaTerminada() ? "finalizada" : "en curso";
        etiquetaEstado.setText("Simulación " + estado
                + "   |   Turno: " + simulador.getTurnoActual()
                + "   |   Evacuados: " + simulador.evacuados() + "/" + servicio.getPersonas().tamano()
                + "   |   Atrapados: " + simulador.atrapados()
                + "   |   Conexiones con humo: " + servicio.getGrafo().aristasBloqueadas().tamano());
        panelEdificio.repaint();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String idSeleccionado() {
        return idsOrigen[comboOrigen.getSelectedIndex()];
    }

    private void seleccionarEnCombo(String id) {
        for (int i = 0; i < idsOrigen.length; i++) {
            if (idsOrigen[i].equals(id)) {
                comboOrigen.setSelectedIndex(i);
                return;
            }
        }
    }

    private String[] idsDeUbicacionesNoSalida() {
        ListaEnlazada<Ubicacion> origenes = servicio.getGrafo().todasLasUbicaciones().filtrar(u -> !u.esSalida());
        String[] ids = new String[origenes.tamano()];
        Iterador<Ubicacion> it = origenes.iterador();
        for (int i = 0; it.tieneSiguiente(); i++) {
            ids[i] = it.siguiente().getId();
        }
        return ids;
    }

    private String[] etiquetasDeOrigen() {
        String[] etiquetas = new String[idsOrigen.length];
        for (int i = 0; i < idsOrigen.length; i++) {
            etiquetas[i] = idsOrigen[i] + " · " + servicio.getGrafo().ubicacion(idsOrigen[i]).descripcion();
        }
        return etiquetas;
    }

    private static void aplicarEstiloNimbus() {
        for (UIManager.LookAndFeelInfo estilo : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(estilo.getName())) {
                try {
                    UIManager.setLookAndFeel(estilo.getClassName());
                } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
                    // Si falla, se mantiene el estilo por defecto de Java.
                }
                return;
            }
        }
    }
}
