package UI;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class PanelFondo extends JPanel {

    private final BufferedImage imagen;

    public PanelFondo(BufferedImage imagen) {
        if (imagen == null) {
            throw new IllegalArgumentException("La imagen de fondo no puede ser nula");
        }

        this.imagen = imagen;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            int anchoPanel = getWidth();
            int altoPanel = getHeight();

            int anchoImagen = imagen.getWidth();
            int altoImagen = imagen.getHeight();

            double escalaX = (double) anchoPanel / anchoImagen;
            double escalaY = (double) altoPanel / altoImagen;

            double escala = Math.max(escalaX, escalaY);

            int anchoFinal = (int) Math.ceil(anchoImagen * escala);
            int altoFinal = (int) Math.ceil(altoImagen * escala);

            int x = (anchoPanel - anchoFinal) / 2;
            int y = (altoPanel - altoFinal) / 2;

            g2.drawImage(imagen, x, y, anchoFinal, altoFinal, null);

        } finally {
            g2.dispose();
        }
    }
}