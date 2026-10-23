package Servidor;

import java.io.IOException;
import java.util.Scanner;

public class ServidorMain {

    public static void main(String[] args) {
        ChatServer servidor = new ChatServer();

        try {
            servidor.runServer();

            System.out.println("Comandos disponibles: estado, iniciar, reintentar, salir");

            Scanner scanner = new Scanner(System.in);

            while (servidor.isEjecutando() && scanner.hasNextLine()) {
                String comando = scanner.nextLine().trim().toLowerCase();

                switch (comando) {
                    case "estado":
                        System.out.println("Estado de partida: " + servidor.getFasePartida());
                        System.out.println("Conexiones activas: " + servidor.getCantidadConexiones());
                        System.out.println("Jugadores registrados: " + servidor.getCantidadRegistrados());
                        break;

                    case "iniciar":
                        servidor.iniciarPartida();
                        break;

                    case "reintentar":
                        servidor.reintentarPartida();
                        break;

                    case "salir":
                        servidor.cerrar();
                        return;

                    case "":
                        break;

                    default:
                        System.out.println("Comando desconocido");
                        System.out.println("Comandos disponibles: estado, iniciar, reintentar, salir");
                        break;
                }
            }

        } catch (IOException error) {
            System.err.println("No se pudo iniciar el servidor: " + error.getMessage());

        } finally {
            servidor.cerrar();
        }
    }
}