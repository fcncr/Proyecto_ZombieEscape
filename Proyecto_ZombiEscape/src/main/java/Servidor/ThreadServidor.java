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

    private final ChatServer chatServer;
    private final Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private volatile boolean ejecutando;
    private int idJugador;
    private String nombre;
    private String rolJugador;

    public ThreadServidor(ChatServer chatServer, Socket socket) {
        this.chatServer = chatServer;
        this.socket = socket;
        this.ejecutando = true;
        this.idJugador = -1;
        this.nombre = null;
        this.rolJugador = null;
        setName("ThreadServidor-" + socket.getPort());
    }

    @Override
    public void run() {
        try {
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());

            while (ejecutando && !socket.isClosed()) {
                Object objeto = entrada.readObject();

                if (objeto instanceof Mensaje) {
                    procesarMensaje((Mensaje) objeto);
                }
            }

        } catch (EOFException | SocketException error) {
        } catch (IOException | ClassNotFoundException error) {
            if (ejecutando) {
                System.err.println("Error con cliente: " + error.getMessage());
            }
        } finally {
            ejecutando = false;
            chatServer.retirar(this);
            cerrarSocket();
        }
    }

    private void procesarMensaje(Mensaje mensaje) {
        switch (mensaje.getTipo()) {
            case Mensaje.UNIRSE:
                procesarUnirse(mensaje);
                break;

            case Mensaje.MOVER:
                procesarMover(mensaje);
                break;

            case Mensaje.MOVER_CAMARA:
                procesarMoverCamara(mensaje);
                break;

            case Mensaje.DISPARAR:
                procesarDisparar(mensaje);
                break;

            case Mensaje.CHAT:
                procesarChat(mensaje);
                break;

            default:
                enviarError("Tipo de mensaje desconocido");
                break;
        }
    }

    private void procesarUnirse(Mensaje mensaje) {
        if (estaRegistrado()) {
            enviarError("Esta conexion ya tiene un jugador registrado");
            return;
        }

        String[] parametros = mensaje.getParametros();

        if (parametros.length != 2) {
            enviarError("UNIRSE requiere nombre y rol");
            return;
        }

        try {
            int id = chatServer.registrarJugador(this, parametros[0], parametros[1]);
            enviar(new Mensaje(Mensaje.BIENVENIDO, Integer.toString(id), nombre, rolJugador));
            chatServer.enviarEstado();

        } catch (IllegalArgumentException | IllegalStateException error) {
            enviarError(error.getMessage());
        }
    }

    private void procesarMover(Mensaje mensaje) {
        if (!estaRegistrado()) {
            enviarError("Debe registrarse antes de mover");
            return;
        }

        if (Mensaje.ROL_FRANCOTIRADOR.equals(rolJugador)) {
            enviarError("El francotirador no puede caminar");
            return;
        }

        String[] parametros = mensaje.getParametros();

        if (parametros.length != 1) {
            enviarError("MOVER requiere una direccion");
            return;
        }

        try {
            Direccion direccion = Direccion.valueOf(parametros[0].toUpperCase());
            chatServer.moverJugador(idJugador, direccion);

        } catch (IllegalArgumentException error) {
            enviarError("Direccion invalida");
        }
    }

    private void procesarMoverCamara(Mensaje mensaje) {
        if (!estaRegistrado()) {
            enviarError("Debe registrarse antes de mover la camara");
            return;
        }

        if (!Mensaje.ROL_FRANCOTIRADOR.equals(rolJugador)) {
            enviarError("Solo el francotirador puede mover la camara");
            return;
        }

        String[] parametros = mensaje.getParametros();

        if (parametros.length != 1) {
            enviarError("MOVER_CAMARA requiere una direccion");
            return;
        }

        try {
            Direccion direccion = Direccion.valueOf(parametros[0].toUpperCase());
            chatServer.moverCamaraJugador(idJugador, direccion);

        } catch (IllegalArgumentException error) {
            enviarError("Direccion invalida");
        }
    }

    private void procesarDisparar(Mensaje mensaje) {
        if (!estaRegistrado()) {
            enviarError("Debe registrarse antes de disparar");
            return;
        }

        if (!Mensaje.ROL_FRANCOTIRADOR.equals(rolJugador)) {
            enviarError("Solo los francotiradores pueden disparar");
            return;
        }

        String[] parametros = mensaje.getParametros();

        if (parametros.length != 2) {
            enviarError("DISPARAR requiere fila y columna");
            return;
        }

        try {
            int fila = Integer.parseInt(parametros[0]);
            int columna = Integer.parseInt(parametros[1]);

            if (!chatServer.dispararJugador(idJugador, fila, columna)) {
                enviarError("No se pudo realizar el disparo");
            }

        } catch (NumberFormatException error) {
            enviarError("Las coordenadas del disparo deben ser numeros enteros");
        }
    }

    private void procesarChat(Mensaje mensaje) {
        if (!estaRegistrado()) {
            enviarError("Debe registrarse antes de usar el chat");
            return;
        }

        String[] parametros = mensaje.getParametros();

        if (parametros.length != 1) {
            enviarError("CHAT requiere un mensaje");
            return;
        }

        String texto = parametros[0] == null ? "" : parametros[0].trim();

        if (texto.isEmpty()) {
            return;
        }

        if (texto.length() > MAX_LONGITUD_CHAT) {
            enviarError("El mensaje no puede superar " + MAX_LONGITUD_CHAT + " caracteres");
            return;
        }

        chatServer.broadcast(new Mensaje(Mensaje.CHAT, nombre, texto));
    }

    private void enviarError(String texto) {
        enviar(new Mensaje(Mensaje.ERROR, texto));
    }

    public synchronized void enviar(Mensaje mensaje) {
        if (!ejecutando || salida == null || mensaje == null) {
            return;
        }

        try {
            salida.writeObject(mensaje);
            salida.flush();
            salida.reset();

        } catch (IOException error) {
            cerrar();
        }
    }

    public void cerrar() {
        ejecutando = false;
        cerrarSocket();
    }

    private void cerrarSocket() {
        try {
            socket.close();
        } catch (IOException error) {
        }
    }

    public boolean estaRegistrado() {
        return idJugador > 0 && nombre != null;
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

    public String getRolJugador() {
        return rolJugador;
    }

    public void setRolJugador(String rolJugador) {
        this.rolJugador = rolJugador;
    }
}