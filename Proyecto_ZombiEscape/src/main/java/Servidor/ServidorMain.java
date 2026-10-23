package Servidor;

import javax.swing.SwingUtilities;

public class ServidorMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServidorFrame frame = new ServidorFrame();
            frame.setVisible(true);
        });
    }
}