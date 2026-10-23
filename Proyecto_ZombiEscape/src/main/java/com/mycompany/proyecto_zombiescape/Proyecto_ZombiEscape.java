package com.mycompany.proyecto_zombiescape;

import Cliente.ClienteMain;
import Servidor.ServidorMain;

public class Proyecto_ZombiEscape {

    public static void main(String[] args) {
        if (args.length == 0) {
            mostrarAyuda();
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

    private static void mostrarAyuda() {
        System.out.println("=== ZombiEscape ===");
        System.out.println("Puntos de entrada disponibles:");
        System.out.println("Servidor: Servidor.ServidorMain");
        System.out.println("Cliente: Cliente.ClienteMain");
        System.out.println();
        System.out.println("Tambien puede ejecutar este launcher con:");
        System.out.println("servidor");
        System.out.println("cliente");
    }
}