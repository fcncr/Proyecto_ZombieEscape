package Cliente;

import Models.Direccion;
import Models.EstadoJugador;
import Models.EstadoPartida;
import Models.Francotirador;
import Models.Jugador;
import Models.Posicion;
import Models.Superviviente;
import Models.Zombie;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PanelMapa extends JPanel {

    private static final int TAMANO_CELDA = 32;

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

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        setPreferredSize(new Dimension(16 * TAMANO_CELDA, 12 * TAMANO_CELDA));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                procesarClic(evento);
            }
        });
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;

        if (estado != null && estado.getMapa() != null) {
            Jugador jugadorLocal = buscarJugadorLocal();

            if (jugadorLocal instanceof Francotirador) {
                Francotirador francotirador = (Francotirador) jugadorLocal;
                int lado = (francotirador.getRadioVista() * 2) + 1;
                setPreferredSize(new Dimension(lado * TAMANO_CELDA, lado * TAMANO_CELDA));

            } else {
                char[][] mapa = estado.getMapa();
                setPreferredSize(new Dimension(mapa[0].length * TAMANO_CELDA, mapa.length * TAMANO_CELDA));
            }
        }

        revalidate();
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
            dibujarMensajeEspera(g);
            return;
        }

        Jugador jugadorLocal = buscarJugadorLocal();

        if (jugadorLocal instanceof Francotirador && ((Francotirador) jugadorLocal).getCentroVista() != null) {
            dibujarVistaFrancotirador(g, (Francotirador) jugadorLocal);

        } else {
            dibujarMapaCompleto(g);
        }

        if (EstadoPartida.DERROTA.equals(estado.getFase())) {
            dibujarDerrota(g);
        }
    }

    private void dibujarMapaCompleto(Graphics g) {
        char[][] mapa = estado.getMapa();

        for (int fila = 0; fila < mapa.length; fila++) {
            for (int columna = 0; columna < mapa[fila].length; columna++) {
                dibujarCelda(g, mapa[fila][columna], fila, columna);
            }
        }

        for (Zombie zombie : estado.getZombies()) {
            if (zombie != null && zombie.isVivo() && zombie.getPosicion() != null) {
                dibujarZombie(g, zombie, zombie.getPosicion().getFila(), zombie.getPosicion().getColumna());
            }
        }

        for (Jugador jugador : estado.getJugadores()) {
            if (jugador == null || jugador.getPosicion() == null || jugador.getEstado() == EstadoJugador.DESCONECTADO) {
                continue;
            }

            dibujarJugador(g, jugador, jugador.getPosicion().getFila(), jugador.getPosicion().getColumna());
        }
    }

    private void dibujarVistaFrancotirador(Graphics g, Francotirador francotirador) {
        char[][] mapa = estado.getMapa();
        int radio = francotirador.getRadioVista();
        int lado = (radio * 2) + 1;

        Posicion centro = francotirador.getCentroVista();
        int origenFila = centro.getFila() - radio;
        int origenColumna = centro.getColumna() - radio;

        for (int filaVisual = 0; filaVisual < lado; filaVisual++) {
            for (int columnaVisual = 0; columnaVisual < lado; columnaVisual++) {
                int filaMapa = origenFila + filaVisual;
                int columnaMapa = origenColumna + columnaVisual;

                if (filaMapa < 0 || filaMapa >= mapa.length || columnaMapa < 0 || columnaMapa >= mapa[0].length) {
                    dibujarFueraMapa(g, filaVisual, columnaVisual);
                    continue;
                }

                dibujarCelda(g, mapa[filaMapa][columnaMapa], filaVisual, columnaVisual);
            }
        }

        for (Zombie zombie : estado.getZombies()) {
            if (zombie == null || !zombie.isVivo() || zombie.getPosicion() == null) {
                continue;
            }

            if (!francotirador.puedeVer(zombie.getPosicion())) {
                continue;
            }

            int filaVisual = zombie.getPosicion().getFila() - origenFila;
            int columnaVisual = zombie.getPosicion().getColumna() - origenColumna;

            dibujarZombie(g, zombie, filaVisual, columnaVisual);
        }

        for (Jugador jugador : estado.getJugadores()) {
            if (jugador == null || jugador.getPosicion() == null || jugador.getEstado() == EstadoJugador.DESCONECTADO) {
                continue;
            }

            if (!francotirador.puedeVer(jugador.getPosicion())) {
                continue;
            }

            int filaVisual = jugador.getPosicion().getFila() - origenFila;
            int columnaVisual = jugador.getPosicion().getColumna() - origenColumna;

            dibujarJugador(g, jugador, filaVisual, columnaVisual);
        }

        dibujarCentroCamara(g, radio, radio);
    }

    private void dibujarFueraMapa(Graphics g, int fila, int columna) {
        int x = columna * TAMANO_CELDA;
        int y = fila * TAMANO_CELDA;

        g.setColor(Color.BLACK);
        g.fillRect(x, y, TAMANO_CELDA, TAMANO_CELDA);

        g.setColor(Color.DARK_GRAY);
        g.drawRect(x, y, TAMANO_CELDA, TAMANO_CELDA);
    }

    private void dibujarCentroCamara(Graphics g, int fila, int columna) {
        int x = columna * TAMANO_CELDA;
        int y = fila * TAMANO_CELDA;

        g.setColor(Color.ORANGE);
        g.drawRect(x + 2, y + 2, TAMANO_CELDA - 4, TAMANO_CELDA - 4);
        g.drawRect(x + 3, y + 3, TAMANO_CELDA - 6, TAMANO_CELDA - 6);
    }

    private void procesarClic(MouseEvent evento) {
        if (cliente == null || estado == null || !SwingUtilities.isLeftMouseButton(evento)) {
            return;
        }

        if (!EstadoPartida.JUGANDO.equals(estado.getFase())) {
            return;
        }

        Jugador jugadorLocal = buscarJugadorLocal();

        if (!(jugadorLocal instanceof Francotirador)) {
            return;
        }

        Francotirador francotirador = (Francotirador) jugadorLocal;

        if (francotirador.getCentroVista() == null) {
            return;
        }

        int columnaVisual = evento.getX() / TAMANO_CELDA;
        int filaVisual = evento.getY() / TAMANO_CELDA;
        int radio = francotirador.getRadioVista();
        int lado = (radio * 2) + 1;

        if (filaVisual < 0 || filaVisual >= lado || columnaVisual < 0 || columnaVisual >= lado) {
            return;
        }

        int filaMapa = francotirador.getCentroVista().getFila() - radio + filaVisual;
        int columnaMapa = francotirador.getCentroVista().getColumna() - radio + columnaVisual;

        char[][] mapa = estado.getMapa();

        if (filaMapa < 0 || filaMapa >= mapa.length || columnaMapa < 0 || columnaMapa >= mapa[0].length) {
            return;
        }

        cliente.disparar(filaMapa, columnaMapa);
    }

    private Jugador buscarJugadorLocal() {
        if (estado == null) {
            return null;
        }

        for (Jugador jugador : estado.getJugadores()) {
            if (jugador.getId() == idJugadorLocal) {
                return jugador;
            }
        }

        return null;
    }

    private void dibujarMensajeEspera(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("Esperando inicio de partida...", 20, 30);
    }

    private void dibujarCelda(Graphics g, char simbolo, int fila, int columna) {
        int x = columna * TAMANO_CELDA;
        int y = fila * TAMANO_CELDA;

        if (simbolo == '#') {
            g.setColor(Color.DARK_GRAY);

        } else if (simbolo == 'H') {
            g.setColor(new Color(220, 235, 220));

        } else if (simbolo == 'S') {
            g.setColor(new Color(255, 235, 180));

        } else if (simbolo == 'E') {
            g.setColor(new Color(220, 235, 255));

        } else {
            g.setColor(Color.WHITE);
        }

        g.fillRect(x, y, TAMANO_CELDA, TAMANO_CELDA);

        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(x, y, TAMANO_CELDA, TAMANO_CELDA);

        if (simbolo == 'E' || simbolo == 'H' || simbolo == 'S') {
            dibujarSimbolo(g, simbolo, x, y);
        }
    }

    private void dibujarSimbolo(Graphics g, char simbolo, int x, int y) {
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));

        String texto = Character.toString(simbolo);
        FontMetrics metricas = g.getFontMetrics();

        int textoX = x + (TAMANO_CELDA - metricas.stringWidth(texto)) / 2;
        int textoY = y + ((TAMANO_CELDA - metricas.getHeight()) / 2) + metricas.getAscent();

        g.drawString(texto, textoX, textoY);
    }

    private void dibujarZombie(Graphics g, Zombie zombie, int filaVisual, int columnaVisual) {
        int x = columnaVisual * TAMANO_CELDA;
        int y = filaVisual * TAMANO_CELDA;
        int margen = 5;
        int tamano = TAMANO_CELDA - (margen * 2);

        g.setColor(new Color(40, 130, 60));
        g.fillRect(x + margen, y + margen, tamano, tamano);

        String texto = "Z" + zombie.getId() + simboloDireccion(zombie.getDireccion());

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));

        FontMetrics metricas = g.getFontMetrics();
        int textoX = x + (TAMANO_CELDA - metricas.stringWidth(texto)) / 2;
        int textoY = y + ((TAMANO_CELDA - metricas.getHeight()) / 2) + metricas.getAscent();

        g.drawString(texto, textoX, textoY);
    }

    private String simboloDireccion(Direccion direccion) {
        switch (direccion) {
            case ARRIBA:
                return "↑";

            case ABAJO:
                return "↓";

            case IZQUIERDA:
                return "←";

            case DERECHA:
                return "→";

            default:
                return "";
        }
    }

    private void dibujarJugador(Graphics g, Jugador jugador, int filaVisual, int columnaVisual) {
        int x = columnaVisual * TAMANO_CELDA;
        int y = filaVisual * TAMANO_CELDA;
        int margen = 4;
        int diametro = TAMANO_CELDA - (margen * 2);

        boolean espectador = jugador.getEstado() == EstadoJugador.ESPECTADOR;
        boolean escapado = jugador.getEstado() == EstadoJugador.ESCAPADO;
        boolean oculto = jugador instanceof Superviviente && ((Superviviente) jugador).isOculto();

        if (espectador) {
            g.setColor(Color.GRAY);

        } else if (escapado) {
            g.setColor(new Color(190, 140, 30));

        } else if (oculto) {
            g.setColor(new Color(90, 90, 90));

        } else if (jugador.getId() == idJugadorLocal) {
            g.setColor(Color.BLUE);

        } else {
            g.setColor(Color.RED);
        }

        g.fillOval(x + margen, y + margen, diametro, diametro);

        String texto;

        if (espectador) {
            texto = "X" + jugador.getId();

        } else if (escapado) {
            texto = "S" + jugador.getId();

        } else if (oculto) {
            texto = "H" + jugador.getId();

        } else {
            texto = "J" + jugador.getId();
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));

        FontMetrics metricas = g.getFontMetrics();
        int textoX = x + (TAMANO_CELDA - metricas.stringWidth(texto)) / 2;
        int textoY = y + ((TAMANO_CELDA - metricas.getHeight()) / 2) + metricas.getAscent();

        g.drawString(texto, textoX, textoY);
    }

    private void dibujarDerrota(Graphics g) {
        String texto = "DERROTA DEL EQUIPO";

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));

        FontMetrics metricas = g.getFontMetrics();
        int x = (getWidth() - metricas.stringWidth(texto)) / 2;
        int y = getHeight() / 2;

        g.setColor(new Color(255, 255, 255, 220));
        g.fillRect(0, y - 45, getWidth(), 70);

        g.setColor(Color.RED);
        g.drawString(texto, Math.max(10, x), y);
    }
}