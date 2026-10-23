package Servidor;

import Models.Mensaje;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ThreadConexiones extends Thread {

    private volatile boolean ejecutando;
    private final ChatServer chatServer;

    public ThreadConexiones(ChatServer chatServer) {
        this.chatServer = chatServer;
        this.ejecutando = true;
        setName("ThreadConexiones");
    }

    @Override
    public void run() {
        while (ejecutando && chatServer.isEjecutando()) {
            try {
                Socket socket = chatServer.esperarConexion();
                ThreadServidor threadServidor = new ThreadServidor(chatServer, socket);

                if (!chatServer.registrar(threadServidor)) {
                    rechazarConexion(socket);
                    continue;
                }

                threadServidor.start();

                System.out.println("Cliente conectado desde " + socket.getRemoteSocketAddress());
                System.out.println("Conexiones activas: " + chatServer.getCantidadConexiones());

            } catch (SocketException error) {
                if (ejecutando && chatServer.isEjecutando()) {
                    System.err.println("Error de socket aceptando conexiones: " + error.getMessage());
                }

            } catch (IOException error) {
                if (ejecutando && chatServer.isEjecutando()) {
                    System.err.println("Error aceptando conexion: " + error.getMessage());
                }
            }
        }
    }

    private void rechazarConexion(Socket socket) {
        try {
            ObjectOutputStream escritor = new ObjectOutputStream(socket.getOutputStream());
            escritor.flush();

            Mensaje mensaje = new Mensaje(Mensaje.ERROR, "El servidor alcanzo el maximo de 10 conexiones");
            escritor.writeObject(mensaje);
            escritor.flush();
            escritor.close();

        } catch (IOException error) {
            System.err.println("No se pudo informar el rechazo de la conexion: " + error.getMessage());

        } finally {
            try {
                socket.close();
            } catch (IOException error) {
                System.err.println("No se pudo cerrar la conexion rechazada: " + error.getMessage());
            }
        }

        System.out.println("Conexion rechazada: servidor lleno");
    }

    public void detener() {
        ejecutando = false;
    }
}