package Cliente;

import Models.Mensaje;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;

public class ThreadCliente extends Thread {

    private volatile boolean ejecutando;
    private final Cliente cliente;
    private final Socket socket;
    private ObjectInputStream lectorDeObjetos;

    public ThreadCliente(Cliente cliente, Socket socket) {
        this.cliente = cliente;
        this.socket = socket;
        this.ejecutando = true;
        setName("ThreadCliente");
    }

    @Override
    public void run() {
        try {
            lectorDeObjetos = new ObjectInputStream(socket.getInputStream());

            while (ejecutando && cliente.isConectado()) {
                Object objeto = lectorDeObjetos.readObject();

                if (!(objeto instanceof Mensaje)) {
                    cliente.notificarError("El servidor envio un objeto desconocido");
                    continue;
                }

                Mensaje mensaje = (Mensaje) objeto;
                cliente.procesar(mensaje);
            }

        } catch (EOFException error) {
            if (ejecutando) {
                System.out.println("El servidor cerro la conexion");
            }

        } catch (SocketException error) {
            if (ejecutando) {
                System.out.println("La conexion con el servidor fue cerrada");
            }

        } catch (ClassNotFoundException error) {
            if (ejecutando) {
                cliente.notificarError("El servidor envio una clase desconocida");
            }

        } catch (IOException error) {
            if (ejecutando) {
                cliente.notificarError("Error recibiendo datos del servidor");
            }

        } finally {
            cerrarLector();
            cliente.manejarDesconexionRemota();
        }
    }

    private void cerrarLector() {
        if (lectorDeObjetos == null) {
            return;
        }

        try {
            lectorDeObjetos.close();
        } catch (IOException error) {
        }

        lectorDeObjetos = null;
    }

    public void detener() {
        ejecutando = false;
    }
}