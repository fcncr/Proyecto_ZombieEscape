package com.mycompany.proyecto_zombiescape;

import Cliente.ClienteMain;
import Servidor.ServidorMain;
import javax.swing.SwingUtilities;

public class Proyecto_ZombiEscape {

    public static void main(String[] args) {
        if (args.length == 0) {
            abrirSelectorGrafico();
            return;
        }

        String modo = args[0].trim().toLowerCase();

        switch (modo) {
            case "servidor":
                ServidorMain.main(new String[0]);
                break;

            case "cliente":
                ClienteMain.main(new String[0]);
                break;

            default:
                System.out.println("Modo desconocido: " + args[0]);
                mostrarAyuda();
                break;
        }
    }

    private static void abrirSelectorGrafico() {
        SwingUtilities.invokeLater(() -> {
            SelectorInicioFrame frame = new SelectorInicioFrame();
            frame.setVisible(true);
        });
    }

    private static void mostrarAyuda() {
        System.out.println("=== ZombiEscape ===");
        System.out.println("Modos disponibles:");
        System.out.println("servidor");
        System.out.println("cliente");
        System.out.println();
        System.out.println("Sin argumentos se abre el selector grafico.");
    }
}