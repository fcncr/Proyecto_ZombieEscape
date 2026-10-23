package UI;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JButton;

public class BotonImagen extends JButton {

    public static final String PRIMARIO = "primary";
    public static final String SECUNDARIO = "secondary";
    public static final String PELIGRO = "danger";

    private final BufferedImage normal;
    private final BufferedImage hover;
    private final BufferedImage presionado;
    private final BufferedImage deshabilitado;

    public BotonImagen(String texto, String tipo) {
        this(texto, tipo, 368, 56);
    }

    public BotonImagen(String texto, String tipo, int ancho, int alto) {
        super(texto);

        normal = GestorAssets.getBoton(tipo, "normal");
        hover = GestorAssets.getBoton(tipo, "hover");
        presionado = GestorAssets.getBoton(tipo, "pressed");
        deshabilitado = GestorAssets.getBoton(tipo, "disabled");

        Dimension dimension = new Dimension(ancho, alto);

        setPreferredSize(dimension);
        setMinimumSize(dimension);
        setMaximumSize(dimension);

        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        setForeground(Tema.CONTORNO);

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setFocusable(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            BufferedImage imagen = obtenerImagenActual();

            g2.drawImage(imagen, 0, 0, getWidth(), getHeight(), null);

            String texto = getText();

            if (texto != null && !texto.isEmpty()) {
                g2.setFont(getFont());
                g2.setColor(getForeground());

                FontMetrics metricas = g2.getFontMetrics();

                int x = (getWidth() - metricas.stringWidth(texto)) / 2;
                int y = ((getHeight() - metricas.getHeight()) / 2)
                        + metricas.getAscent();

                if (getModel().isPressed()) {
                    y += 1;
                }

                g2.drawString(texto, x, y);
            }

        } finally {
            g2.dispose();
        }
    }

    private BufferedImage obtenerImagenActual() {
        if (!isEnabled()) {
            return deshabilitado;
        }

        if (getModel().isPressed()) {
            return presionado;
        }

        if (getModel().isRollover()) {
            return hover;
        }

        return normal;
    }
}