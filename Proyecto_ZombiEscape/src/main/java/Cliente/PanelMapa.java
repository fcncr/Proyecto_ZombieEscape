package Cliente;

import Models.Direccion;
import Models.EstadoJugador;
import Models.EstadoPartida;
import Models.Francotirador;
import Models.Jugador;
import Models.Posicion;
import Models.Superviviente;
import Models.Zombie;
import UI.GestorAssets;
import UI.Tema;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PanelMapa extends JPanel {

    private static final Color[] COLORES = {
        new Color(0x21, 0x8A, 0xC4),
        new Color(0xE4, 0x63, 0x52),
        new Color(0xE4, 0xB5, 0x34),
        new Color(0x8D, 0x67, 0xC6),
        new Color(0x27, 0xB6, 0xAC),
        new Color(0xE5, 0x8B, 0x32),
        new Color(0xD9, 0x6E, 0xA8),
        new Color(0x42, 0x4F, 0xA0),
        new Color(0xA8, 0xCA, 0x40),
        new Color(0xEA, 0xE1, 0xCD)
    };

    private final Cliente cliente;
    private EstadoPartida estado;
    private int idJugadorLocal;

    public PanelMapa() {
        this(null);
    }

    public PanelMapa(Cliente cliente) {
        this.cliente = cliente;
        this.estado = null;
        this.idJugadorLocal = -1;

        setOpaque(true);
        setBackground(new Color(0x7C, 0x92, 0x52));
        setPreferredSize(new Dimension(768, 576));
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;
        repaint();
    }

    public void setIdJugadorLocal(int idJugadorLocal) {
        this.idJugadorLocal = idJugadorLocal;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (estado == null || estado.getMapa() == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Jugador local = buscarJugadorLocal();

            if (local instanceof Francotirador && ((Francotirador) local).getCentroVista() != null) {
                dibujarVistaFrancotirador(g2, (Francotirador) local);
            } else {
                dibujarMapaCompleto(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    private void dibujarMapaCompleto(Graphics2D g2) {
        char[][] mapa = estado.getMapa();
        double anchoCelda = (double) getWidth() / mapa[0].length;
        double altoCelda = (double) getHeight() / mapa.length;

        for (int fila = 0; fila < mapa.length; fila++) {
            for (int columna = 0; columna < mapa[fila].length; columna++) {
                dibujarTile(g2, mapa[fila][columna], fila, columna, anchoCelda, altoCelda);
            }
        }

        for (Zombie zombie : estado.getZombies()) {
            if (zombie != null && zombie.isVivo() && zombie.getPosicion() != null) {
                dibujarZombie(g2, zombie, zombie.getPosicion().getFila(), zombie.getPosicion().getColumna(), anchoCelda, altoCelda);
            }
        }

        for (Jugador jugador : estado.getJugadores()) {
            if (jugador == null || jugador.getPosicion() == null || jugador.getEstado() == EstadoJugador.DESCONECTADO) {
                continue;
            }

            dibujarJugador(g2, jugador, jugador.getPosicion().getFila(), jugador.getPosicion().getColumna(), anchoCelda, altoCelda);
        }
    }

    private void dibujarVistaFrancotirador(Graphics2D g2, Francotirador francotirador) {
        char[][] mapa = estado.getMapa();
        int radio = francotirador.getRadioVista();
        int lado = radio * 2 + 1;
        int origenFila = francotirador.getCentroVista().getFila() - radio;
        int origenColumna = francotirador.getCentroVista().getColumna() - radio;

        double anchoCelda = (double) getWidth() / lado;
        double altoCelda = (double) getHeight() / lado;

        for (int filaVisual = 0; filaVisual < lado; filaVisual++) {
            for (int columnaVisual = 0; columnaVisual < lado; columnaVisual++) {
                int filaMapa = origenFila + filaVisual;
                int columnaMapa = origenColumna + columnaVisual;

                if (filaMapa < 0 || filaMapa >= mapa.length || columnaMapa < 0 || columnaMapa >= mapa[0].length) {
                    dibujarImagen(g2, GestorAssets.getTile(' '), filaVisual, columnaVisual, anchoCelda, altoCelda);
                } else {
                    dibujarTile(g2, mapa[filaMapa][columnaMapa], filaVisual, columnaVisual, anchoCelda, altoCelda);
                }
            }
        }

        for (Zombie zombie : estado.getZombies()) {
            if (zombie == null || !zombie.isVivo() || zombie.getPosicion() == null || !francotirador.puedeVer(zombie.getPosicion())) {
                continue;
            }

            int filaVisual = zombie.getPosicion().getFila() - origenFila;
            int columnaVisual = zombie.getPosicion().getColumna() - origenColumna;

            dibujarZombie(g2, zombie, filaVisual, columnaVisual, anchoCelda, altoCelda);
        }

        for (Jugador jugador : estado.getJugadores()) {
            if (jugador == null || jugador.getPosicion() == null || jugador.getEstado() == EstadoJugador.DESCONECTADO || !francotirador.puedeVer(jugador.getPosicion())) {
                continue;
            }

            int filaVisual = jugador.getPosicion().getFila() - origenFila;
            int columnaVisual = jugador.getPosicion().getColumna() - origenColumna;

            dibujarJugador(g2, jugador, filaVisual, columnaVisual, anchoCelda, altoCelda);
        }

        int x = (int) Math.round(radio * anchoCelda);
        int y = (int) Math.round(radio * altoCelda);
        int ancho = (int) Math.ceil(anchoCelda);
        int alto = (int) Math.ceil(altoCelda);

        g2.setColor(new Color(0xE4, 0xBD, 0x65));
        g2.setStroke(new java.awt.BasicStroke(3f));
        g2.drawRoundRect(x + 2, y + 2, ancho - 5, alto - 5, 8, 8);

        int tamanoIcono = Math.max(24, (int) Math.round(Math.min(anchoCelda, altoCelda) * 0.55));
        int iconoX = x + (ancho - tamanoIcono) / 2;
        int iconoY = y + (alto - tamanoIcono) / 2;

        g2.drawImage(GestorAssets.getIcono("sniper"), iconoX, iconoY, tamanoIcono, tamanoIcono, null);
    }

    private void dibujarTile(Graphics2D g2, char simbolo, int fila, int columna, double anchoCelda, double altoCelda) {
        dibujarImagen(g2, GestorAssets.getTile(simbolo), fila, columna, anchoCelda, altoCelda);

        int x = (int) Math.round(columna * anchoCelda);
        int y = (int) Math.round(fila * altoCelda);
        int ancho = (int) Math.ceil(anchoCelda);
        int alto = (int) Math.ceil(altoCelda);

        g2.setColor(new Color(0x27, 0x3D, 0x30, 70));
        g2.drawRect(x, y, ancho, alto);
    }

    private void dibujarImagen(Graphics2D g2, BufferedImage imagen, int fila, int columna, double anchoCelda, double altoCelda) {
        int x = (int) Math.round(columna * anchoCelda);
        int y = (int) Math.round(fila * altoCelda);
        int ancho = (int) Math.ceil(anchoCelda);
        int alto = (int) Math.ceil(altoCelda);

        g2.drawImage(imagen, x, y, ancho, alto, null);
    }

    private void dibujarZombie(Graphics2D g2, Zombie zombie, int fila, int columna, double anchoCelda, double altoCelda) {
        int x = (int) Math.round(columna * anchoCelda);
        int y = (int) Math.round(fila * altoCelda);
        int ancho = (int) Math.ceil(anchoCelda);
        int alto = (int) Math.ceil(altoCelda);

        g2.drawImage(GestorAssets.getZombie(zombie.getDireccion()), x, y, ancho, alto, null);

        BufferedImage flecha = obtenerFlecha(zombie.getDireccion());
        int tamanoFlecha = Math.max(10, (int) Math.round(Math.min(anchoCelda, altoCelda) * 0.32));

        g2.drawImage(flecha, x + ancho - tamanoFlecha, y + alto - tamanoFlecha, tamanoFlecha, tamanoFlecha, null);
    }

    private BufferedImage obtenerFlecha(Direccion direccion) {
        switch (direccion) {
            case ARRIBA:
                return GestorAssets.getIcono("arrow_n");
            case DERECHA:
                return GestorAssets.getIcono("arrow_e");
            case ABAJO:
                return GestorAssets.getIcono("arrow_s");
            case IZQUIERDA:
                return GestorAssets.getIcono("arrow_w");
            default:
                return GestorAssets.getIcono("arrow_n");
        }
    }

    private void dibujarJugador(Graphics2D g2, Jugador jugador, int fila, int columna, double anchoCelda, double altoCelda) {
        int x = (int) Math.round(columna * anchoCelda);
        int y = (int) Math.round(fila * altoCelda);
        int ancho = (int) Math.ceil(anchoCelda);
        int alto = (int) Math.ceil(altoCelda);

        int plaza = jugador.getPlazaVisual();

        if (plaza < 0 || plaza > 9) {
            plaza = Math.max(0, Math.min(9, jugador.getId() - 1));
        }

        if (jugador.getId() == idJugadorLocal) {
            g2.setColor(new Color(0xFF, 0xEF, 0x8A));
            g2.setStroke(new java.awt.BasicStroke(3f));
            g2.drawRoundRect(x + 2, y + 2, ancho - 5, alto - 5, 8, 8);
        }

        java.awt.Composite compositeOriginal = g2.getComposite();

        if (jugador.getEstado() == EstadoJugador.ESPECTADOR) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        }

        g2.drawImage(GestorAssets.getJugador(plaza), x, y - Math.max(0, alto / 12), ancho, alto, null);
        g2.setComposite(compositeOriginal);

        int circulo = Math.max(16, (int) Math.round(Math.min(anchoCelda, altoCelda) * 0.38));
        int cx = x + ancho - circulo;
        int cy = y + alto - circulo;

        g2.setColor(obtenerColorPlaza(plaza));
        g2.fillOval(cx, cy, circulo, circulo);
        g2.setColor(Tema.CONTORNO);
        g2.drawOval(cx, cy, circulo, circulo);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(9, circulo / 2)));
        g2.setColor(plaza == 0 || plaza == 3 || plaza == 7 ? Color.WHITE : Tema.CONTORNO);

        String numero = Integer.toString(plaza + 1);
        FontMetrics metricas = g2.getFontMetrics();
        int textoX = cx + (circulo - metricas.stringWidth(numero)) / 2;
        int textoY = cy + (circulo - metricas.getHeight()) / 2 + metricas.getAscent();

        g2.drawString(numero, textoX, textoY);

        if (jugador instanceof Superviviente && ((Superviviente) jugador).isOculto()) {
            int icono = Math.max(14, (int) Math.round(Math.min(anchoCelda, altoCelda) * 0.32));
            g2.drawImage(GestorAssets.getIcono("hidden"), x, y, icono, icono, null);
        }

        if (jugador.getEstado() == EstadoJugador.ESCAPADO) {
            int icono = Math.max(14, (int) Math.round(Math.min(anchoCelda, altoCelda) * 0.32));
            g2.drawImage(GestorAssets.getIcono("check"), x, y, icono, icono, null);
        }
    }

    private Color obtenerColorPlaza(int plaza) {
        Color[] colores = {
            new Color(0x21, 0x8A, 0xC4),
            new Color(0xE4, 0x63, 0x52),
            new Color(0xE4, 0xB5, 0x34),
            new Color(0x8D, 0x67, 0xC6),
            new Color(0x27, 0xB6, 0xAC),
            new Color(0xE5, 0x8B, 0x32),
            new Color(0xD9, 0x6E, 0xA8),
            new Color(0x42, 0x4F, 0xA0),
            new Color(0xA8, 0xCA, 0x40),
            new Color(0xEA, 0xE1, 0xCD)
        };

        return colores[Math.max(0, Math.min(9, plaza))];
    }

    private Jugador buscarJugadorLocal() {
        if (estado == null) {
            return null;
        }

        return estado.getJugadorPorId(idJugadorLocal);
    }
}