package Servidor;

import java.io.IOException;
import java.net.BindException;
import java.util.Scanner;

public class ServidorMain {

    public static void main(String[] args) {
        ChatServer servidor = new ChatServer();

        try {
            servidor.runServer();

            Runtime.getRuntime().addShutdownHook(new Thread(servidor::cerrar));

            Scanner scanner = new Scanner(System.in);
            boolean continuar = true;

            mostrarComandos();

            while (continuar && scanner.hasNextLine()) {
                String comando = scanner.nextLine().trim();

                switch (comando.toLowerCase()) {
                    case "estado":
                        mostrarEstado(servidor);
                        break;

                    case "iniciar":
                        servidor.iniciarPartida();
                        break;

                    case "salir":
                        continuar = false;
                        break;

                    case "":
                        break;

                    default:
                        System.out.println("Comando desconocido");
                        mostrarComandos();
                        break;
                }
            }

        } catch (BindException error) {
            System.err.println("No se puede iniciar el servidor porque el puerto " + ChatServer.PORT + " ya esta ocupado");

        } catch (IOException error) {
            System.err.println("No se pudo iniciar el servidor: " + error.getMessage());

        } finally {
            servidor.cerrar();
        }
    }

    private static void mostrarEstado(ChatServer servidor) {
        System.out.println("Conexiones activas: " + servidor.getCantidadConexiones());
        System.out.println("Jugadores registrados: " + servidor.getCantidadRegistrados());
        System.out.println("Estado de partida: " + servidor.getFasePartida());
    }

    private static void mostrarComandos() {
        System.out.println("Comandos disponibles: estado, iniciar, salir");
    }
}