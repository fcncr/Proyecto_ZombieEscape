package UI;

import java.awt.Color;
import java.awt.Font;

public final class Tema {

    private Tema() {
    }

    // Colores oficiales de la propuesta visual
    public static final Color CONTORNO = new Color(0x27, 0x3D, 0x30);
    public static final Color JARDIN = new Color(0x87, 0xAB, 0x52);
    public static final Color PANEL = new Color(0xF5, 0xEA, 0xCF);
    public static final Color SALIDA = new Color(0xE4, 0xBD, 0x65);
    public static final Color ALERTA = new Color(0xD4, 0x84, 0x63);

    public static final Color TEXTO_PRINCIPAL = CONTORNO;
    public static final Color TEXTO_CLARO = Color.WHITE;
    public static final Color TRANSPARENTE = new Color(0, 0, 0, 0);

    // Ventana principal propuesta
    public static final int ANCHO_VENTANA = 1280;
    public static final int ALTO_VENTANA = 800;

    // Mapa 16 x 12
    public static final int COLUMNAS_MAPA = 16;
    public static final int FILAS_MAPA = 12;
    public static final int TAMANO_CELDA = 48;

    public static final int ANCHO_MAPA = COLUMNAS_MAPA * TAMANO_CELDA;
    public static final int ALTO_MAPA = FILAS_MAPA * TAMANO_CELDA;

    // Espaciados
    public static final int MARGEN_PEQUENO = 8;
    public static final int MARGEN_NORMAL = 16;
    public static final int MARGEN_GRANDE = 24;

    public static final int RADIO_PANEL = 18;
    public static final int RADIO_BOTON = 14;

    // Tipografía
    public static final Font FUENTE_TITULO = new Font(Font.SERIF, Font.BOLD, 30);
    public static final Font FUENTE_SUBTITULO = new Font(Font.SERIF, Font.BOLD, 22);
    public static final Font FUENTE_SECCION = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    public static final Font FUENTE_NORMAL = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    public static final Font FUENTE_NEGRITA = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    public static final Font FUENTE_PEQUENA = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    // Tamaños mínimos de controles
    public static final int ALTO_BOTON = 56;
    public static final int ALTO_CAMPO = 44;
}