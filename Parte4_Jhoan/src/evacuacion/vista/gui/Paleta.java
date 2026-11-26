package evacuacion.vista.gui;

import evacuacion.modelo.TipoUbicacion;

import java.awt.Color;

/** Colores de la interfaz gráfica, centralizados para mantener un estilo consistente. */
final class Paleta {

    static final Color FONDO = new Color(248, 249, 251);
    static final Color PISO_PAR = new Color(236, 240, 246);
    static final Color PISO_IMPAR = new Color(245, 247, 250);
    static final Color TEXTO = new Color(33, 37, 41);
    static final Color TEXTO_SUAVE = new Color(108, 117, 125);
    static final Color BORDE_NODO = new Color(73, 80, 87);

    static final Color HABITACION = new Color(207, 226, 255);
    static final Color PASILLO = new Color(222, 226, 230);
    static final Color ESCALERA = new Color(255, 224, 178);
    static final Color SALIDA = new Color(183, 235, 196);

    static final Color ARISTA = new Color(160, 168, 180);
    static final Color ARISTA_BLOQUEADA = new Color(220, 53, 69);
    static final Color RUTA_SEGURA = new Color(13, 110, 253);
    static final Color RUTA_EMERGENCIA = new Color(253, 126, 20);
    static final Color SELECCION = new Color(111, 66, 193);
    static final Color ATRAPADO = new Color(220, 53, 69);

    private static final Color[] PERSONAS = {
            new Color(31, 119, 180), new Color(255, 127, 14), new Color(44, 160, 44),
            new Color(148, 103, 189), new Color(140, 86, 75), new Color(227, 119, 194),
            new Color(23, 190, 207), new Color(188, 189, 34), new Color(127, 127, 127)
    };

    private Paleta() {
    }

    static Color colorDe(TipoUbicacion tipo) {
        return switch (tipo) {
            case HABITACION -> HABITACION;
            case PASILLO -> PASILLO;
            case ESCALERA -> ESCALERA;
            case SALIDA -> SALIDA;
        };
    }

    static Color colorPersona(int indice) {
        return PERSONAS[indice % PERSONAS.length];
    }
}
