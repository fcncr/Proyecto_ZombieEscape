package UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class PanelRedondeado extends JPanel {

    private final Color colorFondo;
    private final Color colorBorde;
    private final int radio;
    private final int grosorBorde;

    public PanelRedondeado(Color colorFondo, Color colorBorde, int radio, int grosorBorde) {
        this.colorFondo = colorFondo;
        this.colorBorde = colorBorde;
        this.radio = radio;
        this.grosorBorde = grosorBorde;

        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int margen = Math.max(1, grosorBorde / 2);

            g2.setColor(colorFondo);
            g2.fillRoundRect(
                    margen,
                    margen,
                    getWidth() - (margen * 2) - 1,
                    getHeight() - (margen * 2) - 1,
                    radio,
                    radio
            );

            if (colorBorde != null && grosorBorde > 0) {
                g2.setColor(colorBorde);
                g2.setStroke(new java.awt.BasicStroke(grosorBorde));

                g2.drawRoundRect(
                        margen,
                        margen,
                        getWidth() - (margen * 2) - 1,
                        getHeight() - (margen * 2) - 1,
                        radio,
                        radio
                );
            }

        } finally {
            g2.dispose();
        }

        super.paintComponent(g);
    }
}