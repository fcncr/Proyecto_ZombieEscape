package Servidor;

import Models.Direccion;
import Models.Mensaje;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ThreadServidor extends Thread {

    private static final int MAX_LONGITUD_CHAT = 200;

    private volatile boolean ejecutando;
    private boolean cerrado;
    private final ChatServer chatServer;
    private final Socket socket;
    private ObjectInputStream lectorDeObjetos;
    private ObjectOutputStream escritorDeObjetos;
    private int idJugador;
    private String nombre;

    public ThreadServidor(ChatServer chatServer, Socket socket) {
        this.chatServer = chatServer;
        this.socket = socket;
        this.ejecutando = true;
        this.cerrado = false;
        this.idJugador = -1;
        this.nombre = null;
        setName("ThreadServidor-" + socket.getPort());
    }

    @Override
    public void run() {
        try {
            escritorDeObjetos = new ObjectOutputStream(socket.getOutputStream());
            escritorDeObjetos.flush();
            lectorDeObjetos = new ObjectInputStream(socket.getInputStream());

            while (ejecutando && chatServer.isEjecutando()) {
                Object objeto = lectorDeObjetos.readObject();

                if (!(objeto instanceof Mensaje)) {
                    enviar(new Mensaje(Mensaje.ERROR, "El objeto recibido no es un mensaje valido"));
                    continue;
                }

                Mensaje mensaje = (Mensaje) objeto;
                procesar(mensaje);
            }

        } catch (EOFException error) {
            System.out.println("El cliente cerro la conexion");

        } catch (SocketException error) {
            if (ejecutando) {
                System.err.println("Conexion interrumpida: " + error.getMessage());
            }

        } catch (ClassNotFoundException error) {
            System.err.println("Se recibio una clase desconocida: " + error.getMessage());

        } catch (IOException error) {
            if (ejecutando) {
                System.err.println("Error comunicando con el cliente: " + error.getMessage());
            }

        } finally {
            cerrar();
        }
    }

    public void procesar(Mensaje mensaje) {
        if (mensaje == null || mensaje.getTipo() == null) {
            enviar(new Mensaje(Mensaje.ERROR, "Mensaje invalido"));
            return;
        }

        switch (mensaje.getTipo()) {
            case Mensaje.UNIRSE:
                procesarRegistro(mensaje);
                break;

            case Mensaje.MOVER:
                procesarMovimiento(mensaje);
                break;

            case Mensaje.CHAT:
                procesarChat(mensaje);
                break;

            default:
                enviar(new Mensaje(Mensaje.ERROR, "Tipo de mensaje desconocido: " + mensaje.getTipo()));
                break;
        }
    }

    private void procesarRegistro(Mensaje mensaje) {
        String[] parametros = mensaje.getParametros();

        if (parametros.length != 1) {
            enviar(new Mensaje(Mensaje.ERROR, "UNIRSE requiere exactamente un nombre"));
            return;
        }

        try {
            int idAsignado = chatServer.registrarJugador(this, parametros[0]);
            enviar(new Mensaje(Mensaje.BIENVENIDO, Integer.toString(idAsignado)));

            System.out.println("Jugador registrado");
            System.out.println("ID: " + idAsignado);
            System.out.println("Nombre: " + nombre);
            System.out.println("Jugadores registrados: " + chatServer.getCantidadRegistrados());

        } catch (IllegalArgumentException | IllegalStateException error) {
            enviar(new Mensaje(Mensaje.ERROR, error.getMessage()));
        }
    }

    private void procesarMovimiento(Mensaje mensaje) {
        if (!estaRegistrado()) {
            enviar(new Mensaje(Mensaje.ERROR, "El jugador no esta registrado"));
            return;
        }

        String[] parametros = mensaje.getParametros();

        if (parametros.length != 1 || parametros[0] == null) {
            enviar(new Mensaje(Mensaje.ERROR, "MOVER requiere exactamente una direccion"));
            return;
        }

        Direccion direccion;

        try {
            direccion = Direccion.valueOf(parametros[0]);

        } catch (IllegalArgumentException error) {
            enviar(new Mensaje(Mensaje.ERROR, "Direccion de movimiento invalida"));
            return;
        }

        chatServer.moverJugador(idJugador, direccion);
    }

    private void procesarChat(Mensaje mensaje) {
        if (!estaRegistrado()) {
            enviar(new Mensaje(Mensaje.ERROR, "El jugador no esta registrado"));
            return;
        }

        String[] parametros = mensaje.getParametros();

        if (parametros.length != 1 || parametros[0] == null) {
            enviar(new Mensaje(Mensaje.ERROR, "CHAT requiere exactamente un texto"));
            return;
        }

        String texto = parametros[0].trim();

        if (texto.isEmpty()) {
            enviar(new Mensaje(Mensaje.ERROR, "El mensaje de chat no puede estar vacio"));
            return;
        }

        if (texto.length() > MAX_LONGITUD_CHAT) {
            enviar(new Mensaje(Mensaje.ERROR, "El mensaje de chat no puede superar " + MAX_LONGITUD_CHAT + " caracteres"));
            return;
        }

        Mensaje respuesta = new Mensaje(Mensaje.CHAT, nombre, texto);
        chatServer.broadcast(respuesta);

        System.out.println("CHAT - " + nombre + ": " + texto);
    }

    public synchronized boolean enviar(Mensaje mensaje) {
        if (!ejecutando || escritorDeObjetos == null || mensaje == null) {
            return false;
        }

        try {
            escritorDeObjetos.reset();
            escritorDeObjetos.writeObject(mensaje);
            escritorDeObjetos.flush();
            return true;

        } catch (IOException error) {
            System.err.println("Error enviando mensaje al cliente: " + error.getMessage());
            cerrar();
            return false;
        }
    }

    public int getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(int idJugador) {
        this.idJugador = idJugador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean estaRegistrado() {
        return idJugador > 0 && nombre != null;
    }

    public synchronized void cerrar() {
        if (cerrado) {
            return;
        }

        cerrado = true;
        ejecutando = false;

        if (lectorDeObjetos != null) {
            try {
                lectorDeObjetos.close();

            } catch (IOException error) {
                System.err.println("Error cerrando lector: " + error.getMessage());
            }
        }

        if (escritorDeObjetos != null) {
            try {
                escritorDeObjetos.close();

            } catch (IOException error) {
                System.err.println("Error cerrando escritor: " + error.getMessage());
            }
        }

        try {
            socket.close();

        } catch (IOException error) {
            System.err.println("Error cerrando socket: " + error.getMessage());
        }

        chatServer.retirar(this);
    }
}