package evacuacion.vista.gui;

import evacuacion.estructuras.Iterador;
import evacuacion.estructuras.ListaEnlazada;
import evacuacion.estructuras.TablaHash;
import evacuacion.modelo.Arista;
import evacuacion.modelo.Formato;
import evacuacion.modelo.Persona;
import evacuacion.modelo.Ruta;
import evacuacion.modelo.TipoUbicacion;
import evacuacion.modelo.Ubicacion;
import evacuacion.servicio.EvacuacionService;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Lienzo que dibuja el edificio: pisos, ubicaciones (nodos), conexiones (aristas), la ruta
 * resaltada, las personas, las colas de las escaleras/salidas y el punto de encuentro.
 *
 * <p>Clic sobre un nodo = calcular su ruta. Clic sobre una conexión = bloquear/desbloquear.</p>
 */
public class PanelEdificio extends JPanel {

    /** Recibe los clics del usuario sobre el plano. */
    public interface OyenteClic {
        void ubicacionSeleccionada(Ubicacion ubicacion);

        void aristaSeleccionada(Arista arista);
    }

    private static final int MARGEN_IZQUIERDO = 76;
    private static final int MARGEN_SUPERIOR = 20;
    private static final int ANCHO_COLUMNA = 122;
    private static final int ALTO_PISO = 180;
    private static final int ANCHO_NODO = 98;
    private static final int ALTO_NODO = 38;
    private static final int Y_HABITACION = 55;
    private static final int Y_CIRCULACION = 128;
    private static final int ALTO_PUNTO_ENCUENTRO = 70;
    private static final int RADIO_PERSONA = 10;
    private static final int TOLERANCIA_CLIC = 12;

    private static final Font FUENTE_NODO = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    private static final Font FUENTE_PEQUENA = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private static final Font FUENTE_PERSONA = new Font(Font.SANS_SERIF, Font.BOLD, 9);
    private static final Font FUENTE_PISO = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Stroke TRAZO_LINEA = new BasicStroke(2f);
    private static final Stroke TRAZO_BLOQUEADO = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            1f, new float[]{8f, 6f}, 0f);
    private static final Stroke TRAZO_RUTA = new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private final EvacuacionService servicio;
    private final int pisoMaximo;
    private final int columnas;
    private OyenteClic oyente;
    private Ruta rutaResaltada;
    private Ubicacion seleccionada;

    public PanelEdificio(EvacuacionService servicio) {
        this.servicio = servicio;
        this.pisoMaximo = servicio.getPlano().getPisoMaximo();
        this.columnas = servicio.getPlano().columnas();
        setBackground(Paleta.FONDO);
        setPreferredSize(new Dimension(MARGEN_IZQUIERDO + columnas * ANCHO_COLUMNA + 20,
                MARGEN_SUPERIOR + pisoMaximo * ALTO_PISO + ALTO_PUNTO_ENCUENTRO + 20));
        setToolTipText("");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                manejarClic(evento.getX(), evento.getY());
            }
        });
    }

    public void setOyente(OyenteClic oyente) {
        this.oyente = oyente;
    }

    /** Resalta una ruta (azul si es segura, naranja si es de emergencia). {@code null} la borra. */
    public void resaltarRuta(Ruta ruta, Ubicacion origen) {
        this.rutaResaltada = ruta;
        this.seleccionada = origen;
        repaint();
    }

    // ================================================================== interacción

    private void manejarClic(int x, int y) {
        if (oyente == null) {
            return;
        }
        Ubicacion ubicacion = ubicacionEn(x, y);
        if (ubicacion != null) {
            oyente.ubicacionSeleccionada(ubicacion);
            return;
        }
        Arista arista = aristaEn(x, y);
        if (arista != null) {
            oyente.aristaSeleccionada(arista);
        }
    }

    private Ubicacion ubicacionEn(int x, int y) {
        Iterador<Ubicacion> it = servicio.getGrafo().todasLasUbicaciones().iterador();
        while (it.tieneSiguiente()) {
            Ubicacion ubicacion = it.siguiente();
            if (rectanguloDe(ubicacion).contains(x, y)) {
                return ubicacion;
            }
        }
        return null;
    }

    private Arista aristaEn(int x, int y) {
        BasicStroke zonaClic = new BasicStroke(TOLERANCIA_CLIC);
        Iterador<Arista> it = servicio.getGrafo().aristas().iterador();
        while (it.tieneSiguiente()) {
            Arista arista = it.siguiente();
            if (zonaClic.createStrokedShape(formaDe(arista)).contains(x, y)) {
                return arista;
            }
        }
        return null;
    }

    @Override
    public String getToolTipText(MouseEvent evento) {
        Ubicacion ubicacion = ubicacionEn(evento.getX(), evento.getY());
        if (ubicacion != null) {
            return "<html><b>" + ubicacion.descripcion() + "</b> [" + ubicacion.getId() + "]<br>"
                    + ubicacion.getTipo().getEtiqueta()
                    + (ubicacion.esCuelloDeBotella()
                    ? "<br>Capacidad: " + ubicacion.getCapacidadPorTurno() + " persona(s)/turno"
                    + "<br>En cola: " + ubicacion.getColaEspera().tamano() : "")
                    + "<br><i>Clic: calcular ruta de evacuación</i></html>";
        }
        Arista arista = aristaEn(evento.getX(), evento.getY());
        if (arista != null) {
            return "<html>" + arista + "<br><i>Clic: " + (arista.estaBloqueada() ? "despejar" : "bloquear (humo)")
                    + "</i></html>";
        }
        return null;
    }

    // ================================================================== geometría

    private int centroX(Ubicacion ubicacion) {
        return MARGEN_IZQUIERDO + ubicacion.getColumna() * ANCHO_COLUMNA + ANCHO_COLUMNA / 2;
    }

    private int centroY(Ubicacion ubicacion) {
        int desplazamiento = ubicacion.getTipo() == TipoUbicacion.HABITACION ? Y_HABITACION : Y_CIRCULACION;
        return yPiso(ubicacion.getPiso()) + desplazamiento;
    }

    private int yPiso(int piso) {
        return MARGEN_SUPERIOR + (pisoMaximo - piso) * ALTO_PISO;
    }

    private RoundRectangle2D rectanguloDe(Ubicacion ubicacion) {
        return new RoundRectangle2D.Double(centroX(ubicacion) - ANCHO_NODO / 2.0, centroY(ubicacion) - ALTO_NODO / 2.0,
                ANCHO_NODO, ALTO_NODO, 12, 12);
    }

    /**
     * Línea recta entre nodos, salvo conexiones largas en el mismo piso que pasarían por encima
     * de otros nodos: esas se dibujan como curva por debajo.
     */
    private Shape formaDe(Arista arista) {
        Ubicacion a = arista.getExtremoA();
        Ubicacion b = arista.getExtremoB();
        int x1 = centroX(a);
        int y1 = centroY(a);
        int x2 = centroX(b);
        int y2 = centroY(b);
        boolean largaEnMismaFila = y1 == y2 && Math.abs(a.getColumna() - b.getColumna()) >= 3;
        if (largaEnMismaFila) {
            return new QuadCurve2D.Double(x1, y1, (x1 + x2) / 2.0, y1 + 70, x2, y2);
        }
        return new Line2D.Double(x1, y1, x2, y2);
    }

    private int[] puntoEtiqueta(Arista arista) {
        Shape forma = formaDe(arista);
        if (forma instanceof QuadCurve2D curva) {
            return new int[]{(int) curva.getCtrlX(), (int) (curva.getY1() + 35)};
        }
        Line2D linea = (Line2D) forma;
        return new int[]{(int) ((linea.getX1() + linea.getX2()) / 2), (int) ((linea.getY1() + linea.getY2()) / 2)};
    }

    // ================================================================== dibujo

    @Override
    protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);
        Graphics2D g = (Graphics2D) graficos.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        dibujarPisos(g);
        dibujarAristas(g);
        dibujarRuta(g);
        dibujarUbicaciones(g);
        dibujarPersonas(g);
        dibujarPuntoDeEncuentro(g);
        g.dispose();
    }

    private void dibujarPisos(Graphics2D g) {
        int ancho = MARGEN_IZQUIERDO + columnas * ANCHO_COLUMNA;
        for (int piso = pisoMaximo; piso >= 1; piso--) {
            g.setColor(piso % 2 == 0 ? Paleta.PISO_PAR : Paleta.PISO_IMPAR);
            g.fillRoundRect(8, yPiso(piso) + 4, ancho, ALTO_PISO - 8, 16, 16);
            g.setColor(Paleta.TEXTO_SUAVE);
            g.setFont(FUENTE_PISO);
            g.drawString("PISO " + piso, 18, yPiso(piso) + ALTO_PISO / 2);
        }
    }

    private void dibujarAristas(Graphics2D g) {
        g.setFont(FUENTE_PEQUENA);
        Iterador<Arista> it = servicio.getGrafo().aristas().iterador();
        while (it.tieneSiguiente()) {
            Arista arista = it.siguiente();
            boolean bloqueada = arista.estaBloqueada();
            g.setColor(bloqueada ? Paleta.ARISTA_BLOQUEADA : Paleta.ARISTA);
            g.setStroke(bloqueada ? TRAZO_BLOQUEADO : TRAZO_LINEA);
            g.draw(formaDe(arista));
            int[] punto = puntoEtiqueta(arista);
            dibujarEtiqueta(g, (bloqueada ? "HUMO " : "") + Formato.tiempo(arista.getTiempo()), punto[0], punto[1],
                    bloqueada ? Paleta.ARISTA_BLOQUEADA : Paleta.TEXTO_SUAVE);
        }
    }

    private void dibujarRuta(Graphics2D g) {
        if (rutaResaltada == null || !rutaResaltada.existe()) {
            return;
        }
        g.setStroke(TRAZO_RUTA);
        Color color = rutaResaltada.cruzaBloqueos() ? Paleta.RUTA_EMERGENCIA : Paleta.RUTA_SEGURA;
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 170));
        Iterador<Ubicacion> it = rutaResaltada.pasosEnOrden().iterador();
        Ubicacion anterior = it.siguiente();
        while (it.tieneSiguiente()) {
            Ubicacion actual = it.siguiente();
            g.draw(formaDe(servicio.getGrafo().buscarArista(anterior, actual)));
            anterior = actual;
        }
    }

    private void dibujarUbicaciones(Graphics2D g) {
        Iterador<Ubicacion> it = servicio.getGrafo().todasLasUbicaciones().iterador();
        while (it.tieneSiguiente()) {
            Ubicacion ubicacion = it.siguiente();
            RoundRectangle2D rectangulo = rectanguloDe(ubicacion);
            g.setColor(Paleta.colorDe(ubicacion.getTipo()));
            g.fill(rectangulo);
            boolean esSeleccionada = ubicacion == seleccionada;
            g.setColor(esSeleccionada ? Paleta.SELECCION
                    : servicio.getGrafo().tieneConexionBloqueada(ubicacion) ? Paleta.ARISTA_BLOQUEADA : Paleta.BORDE_NODO);
            g.setStroke(new BasicStroke(esSeleccionada ? 3f : 1.5f));
            g.draw(rectangulo);

            g.setColor(Paleta.TEXTO);
            g.setFont(FUENTE_NODO);
            dibujarCentrado(g, ubicacion.getId(), centroX(ubicacion), centroY(ubicacion) - 2);
            g.setFont(FUENTE_PEQUENA);
            g.setColor(Paleta.TEXTO_SUAVE);
            dibujarCentrado(g, ubicacion.getTipo().getEtiqueta(), centroX(ubicacion), centroY(ubicacion) + 12);
        }
    }

    /** Personas dentro de un nodo (sobre el recuadro) y en cola (debajo, en orden FIFO). */
    private void dibujarPersonas(Graphics2D g) {
        TablaHash<String, Integer> ocupacion = new TablaHash<>();
        ListaEnlazada<Persona> personas = servicio.getPersonas();
        Iterador<Persona> it = personas.iterador();
        for (int indice = 0; it.tieneSiguiente(); indice++) {
            Persona persona = it.siguiente();
            if (persona.estaEvacuado() || persona.estaEnCola()) {
                continue;
            }
            Ubicacion ubicacion = persona.getUbicacionActual();
            int posicion = ocupacion.obtenerOPorDefecto(ubicacion.getId(), 0);
            ocupacion.insertar(ubicacion.getId(), posicion + 1);
            int x = centroX(ubicacion) - ANCHO_NODO / 2 + RADIO_PERSONA + posicion * (2 * RADIO_PERSONA + 2);
            int y = centroY(ubicacion) - ALTO_NODO / 2 - RADIO_PERSONA - 3;
            dibujarPersona(g, persona, indice, x, y);
        }
        dibujarColas(g, personas);
    }

    private void dibujarColas(Graphics2D g, ListaEnlazada<Persona> personas) {
        Iterador<Ubicacion> it = servicio.getGrafo().todasLasUbicaciones().iterador();
        while (it.tieneSiguiente()) {
            Ubicacion puerta = it.siguiente();
            if (puerta.getColaEspera().estaVacia()) {
                continue;
            }
            int x = centroX(puerta) + 8;
            int y = centroY(puerta) + ALTO_NODO / 2 + RADIO_PERSONA + 4;
            g.setFont(FUENTE_PEQUENA);
            g.setColor(Paleta.ARISTA_BLOQUEADA);
            g.drawString("cola", x - 34, y + 4);
            Iterador<Persona> cola = puerta.getColaEspera().iterador();
            for (int posicion = 0; cola.tieneSiguiente(); posicion++) {
                Persona persona = cola.siguiente();
                dibujarPersona(g, persona, indiceDe(personas, persona), x + posicion * (2 * RADIO_PERSONA + 2), y);
            }
        }
    }

    private void dibujarPuntoDeEncuentro(Graphics2D g) {
        int y = MARGEN_SUPERIOR + pisoMaximo * ALTO_PISO + 8;
        g.setColor(Paleta.SALIDA);
        g.fillRoundRect(8, y, MARGEN_IZQUIERDO + columnas * ANCHO_COLUMNA, ALTO_PUNTO_ENCUENTRO - 12, 16, 16);
        g.setColor(Paleta.TEXTO);
        g.setFont(FUENTE_NODO);
        g.drawString("Punto de encuentro (evacuados)", 18, y + 20);

        Iterador<Persona> it = servicio.getPersonas().iterador();
        int x = 30;
        for (int indice = 0; it.tieneSiguiente(); indice++) {
            Persona persona = it.siguiente();
            if (persona.estaEvacuado()) {
                dibujarPersona(g, persona, indice, x, y + 40);
                g.setFont(FUENTE_PEQUENA);
                g.setColor(Paleta.TEXTO_SUAVE);
                g.drawString(persona.getNombre() + " (t" + persona.getTurnoEvacuacion() + ")", x + 14, y + 44);
                x += 140;
            }
        }
    }

    private void dibujarPersona(Graphics2D g, Persona persona, int indice, int x, int y) {
        g.setColor(persona.estaAtrapado() ? Paleta.ATRAPADO : Paleta.colorPersona(indice));
        g.fillOval(x - RADIO_PERSONA, y - RADIO_PERSONA, 2 * RADIO_PERSONA, 2 * RADIO_PERSONA);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x - RADIO_PERSONA, y - RADIO_PERSONA, 2 * RADIO_PERSONA, 2 * RADIO_PERSONA);
        g.setFont(FUENTE_PERSONA);
        String etiqueta = persona.getId().length() > 3 ? persona.getId().substring(persona.getId().length() - 3) : persona.getId();
        dibujarCentrado(g, etiqueta, x, y + 3);
    }

    private void dibujarEtiqueta(Graphics2D g, String texto, int x, int y, Color color) {
        FontMetrics metricas = g.getFontMetrics();
        int ancho = metricas.stringWidth(texto) + 6;
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRoundRect(x - ancho / 2, y - 8, ancho, 15, 6, 6);
        g.setColor(color);
        g.drawString(texto, x - ancho / 2 + 3, y + 4);
    }

    private void dibujarCentrado(Graphics2D g, String texto, int x, int y) {
        g.drawString(texto, x - g.getFontMetrics().stringWidth(texto) / 2, y);
    }

    private static int indiceDe(ListaEnlazada<Persona> personas, Persona buscada) {
        Iterador<Persona> it = personas.iterador();
        for (int i = 0; it.tieneSiguiente(); i++) {
            if (it.siguiente() == buscada) {
                return i;
            }
        }
        return 0;
    }
}
