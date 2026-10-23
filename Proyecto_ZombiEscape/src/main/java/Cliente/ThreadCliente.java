package Cliente;

import Models.Mensaje;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;

public class ThreadCliente extends Thread {

    private final Cliente cliente;
    private final Socket socket;
    private volatile boolean ejecutando;

    public ThreadCliente(Cliente cliente, Socket socket) {
        this.cliente = cliente;
        this.socket = socket;
        this.ejecutando = true;
        setName("ThreadCliente");
    }

    @Override
    public void run() {
        try (ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())) {
            while (ejecutando && !socket.isClosed()) {
                Object objeto = entrada.readObject();

                if (objeto instanceof Mensaje) {
                    cliente.procesarMensaje((Mensaje) objeto);
                }
            }

        } catch (EOFException | SocketException error) {
        } catch (IOException | ClassNotFoundException error) {
            if (ejecutando) {
                System.err.println("Conexion con el servidor finalizada: " + error.getMessage());
            }
        } finally {
            ejecutando = false;
            cliente.conexionRemotaCerrada();
        }
    }

    public void detener() {
        ejecutando = false;
        interrupt();
    }
}