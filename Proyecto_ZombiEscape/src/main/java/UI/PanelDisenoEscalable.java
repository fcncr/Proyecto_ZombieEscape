package UI;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class PanelDisenoEscalable extends JPanel {

    public static final int ANCHO_DISENO = 1280;
    public static final int ALTO_DISENO = 800;

    private final BufferedImage fondo;

    public PanelDisenoEscalable(BufferedImage fondo) {
        if (fondo == null) {
            throw new IllegalArgumentException("El fondo no puede ser nulo");
        }

        this.fondo = fondo;

        setLayout(null);
        setOpaque(true);
        setPreferredSize(new Dimension(ANCHO_DISENO, ALTO_DISENO));
    }

    protected double getEscalaDiseno() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return 1.0;
        }

        double escalaX = (double) getWidth() / ANCHO_DISENO;
        double escalaY = (double) getHeight() / ALTO_DISENO;

        return Math.min(escalaX, escalaY);
    }

    protected int getOrigenX() {
        double escala = getEscalaDiseno();
        int ancho = (int) Math.round(ANCHO_DISENO * escala);
        return (getWidth() - ancho) / 2;
    }

    protected int getOrigenY() {
        double escala = getEscalaDiseno();
        int alto = (int) Math.round(ALTO_DISENO * escala);
        return (getHeight() - alto) / 2;
    }

    protected Rectangle escalar(int x, int y, int ancho, int alto) {
        double escala = getEscalaDiseno();
        int origenX = getOrigenX();
        int origenY = getOrigenY();

        return new Rectangle(
                origenX + (int) Math.round(x * escala),
                origenY + (int) Math.round(y * escala),
                (int) Math.round(ancho * escala),
                (int) Math.round(alto * escala)
        );
    }

    protected Graphics2D crearGraphicsDiseno(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double escala = getEscalaDiseno();

        g2.translate(getOrigenX(), getOrigenY());
        g2.scale(escala, escala);

        return g2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
        } finally {
            g2.dispose();
        }
    }
}