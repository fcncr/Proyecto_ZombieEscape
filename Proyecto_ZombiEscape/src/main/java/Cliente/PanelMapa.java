package Cliente;

import Models.Direccion;
import Models.EstadoPartida;
import Models.Jugador;
import Models.Posicion;
import Models.Superviviente;
import Models.Zombie;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class PanelMapa extends JPanel {

    private static final int TAMANO_CELDA = 32;

    private EstadoPartida estado;
    private int idJugadorLocal;

    public PanelMapa() {
        this.estado = null;
        this.idJugadorLocal = -1;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        setPreferredSize(new Dimension(16 * TAMANO_CELDA, 12 * TAMANO_CELDA));
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;

        if (estado != null && estado.getMapa() != null) {
            char[][] mapa = estado.getMapa();
            int ancho = mapa[0].length * TAMANO_CELDA;
            int alto = mapa.length * TAMANO_CELDA;
            setPreferredSize(new Dimension(ancho, alto));
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

        char[][] mapa = estado.getMapa();

        dibujarMapa(g, mapa);
        dibujarZombies(g, estado.getZombies());
        dibujarJugadores(g, estado.getJugadores());

        if (estado.isDerrotaEquipo()) {
            dibujarDerrota(g);
        }
    }

    private void dibujarMensajeEspera(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("Esperando inicio de partida...", 20, 30);
    }

    private void dibujarMapa(Graphics g, char[][] mapa) {
        for (int fila = 0; fila < mapa.length; fila++) {
            for (int columna = 0; columna < mapa[fila].length; columna++) {
                dibujarCelda(g, mapa[fila][columna], fila, columna);
            }
        }
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

    private void dibujarZombies(Graphics g, List<Zombie> zombies) {
        for (Zombie zombie : zombies) {
            if (zombie == null || zombie.getPosicion() == null || !zombie.isVivo()) {
                continue;
            }

            dibujarZombie(g, zombie);
        }
    }

    private void dibujarZombie(Graphics g, Zombie zombie) {
        Posicion posicion = zombie.getPosicion();

        int x = posicion.getColumna() * TAMANO_CELDA;
        int y = posicion.getFila() * TAMANO_CELDA;
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

    private void dibujarJugadores(Graphics g, List<Jugador> jugadores) {
        for (Jugador jugador : jugadores) {
            if (jugador == null || jugador.getPosicion() == null || !jugador.isActivo()) {
                continue;
            }

            dibujarJugador(g, jugador);
        }
    }

    private void dibujarJugador(Graphics g, Jugador jugador) {
        Posicion posicion = jugador.getPosicion();

        int x = posicion.getColumna() * TAMANO_CELDA;
        int y = posicion.getFila() * TAMANO_CELDA;
        int margen = 4;
        int diametro = TAMANO_CELDA - (margen * 2);

        boolean oculto = jugador instanceof Superviviente && ((Superviviente) jugador).isOculto();

        if (oculto) {
            g.setColor(Color.GRAY);

        } else if (jugador.getId() == idJugadorLocal) {
            g.setColor(Color.BLUE);

        } else {
            g.setColor(Color.RED);
        }

        g.fillOval(x + margen, y + margen, diametro, diametro);

        String texto = oculto ? "H" + jugador.getId() : "J" + jugador.getId();

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));

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