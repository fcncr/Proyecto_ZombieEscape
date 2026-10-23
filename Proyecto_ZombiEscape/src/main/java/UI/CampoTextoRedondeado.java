package UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

public class CampoTextoRedondeado extends JTextField {

    private final Color fondo;
    private final Color borde;

    public CampoTextoRedondeado() {
        fondo = new Color(0xFF, 0xFC, 0xF2);
        borde = new Color(0xB5, 0xAB, 0x8E);

        setOpaque(false);
        setForeground(Tema.CONTORNO);
        setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fondo);
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
        } finally {
            g2.dispose();
        }

        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borde);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
        } finally {
            g2.dispose();
        }
    }
}