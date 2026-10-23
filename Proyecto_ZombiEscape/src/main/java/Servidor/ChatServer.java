package Servidor;

import Models.Direccion;
import Models.EstadoPartida;
import Models.GestorEscenarios;
import Models.Jugador;
import Models.Mensaje;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    public static final String HOST = "localhost";
    public static final int PORT = 8084;
    public static final int MAX_CLIENTES = 10;
    public static final int MAX_FRANCOTIRADORES = 2;
    public static final String DIRECTORIO_ESCENARIOS = "escenarios";

    private ServerSocket serverSocket;
    private final List<ThreadServidor> threadServidores;
    private ThreadConexiones threadConexiones;
    private ThreadPartida threadPartida;
    private Partida partida;
    private volatile boolean ejecutando;
    private int siguienteIdJugador;

    public ChatServer() {
        this.threadServidores = new ArrayList<>();
        this.ejecutando = false;
        this.siguienteIdJugador = 1;
    }

    public void runServer() throws IOException {
        if (ejecutando) {
            throw new IllegalStateException("El servidor ya esta ejecutandose");
        }

        cargarPartida();
        siguienteIdJugador = 1;

        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), PORT));
        ejecutando = true;

        threadConexiones = new ThreadConexiones(this);
        threadConexiones.start();

        System.out.println("Servidor ZombiEscape iniciado");
        System.out.println("Escuchando en " + HOST + ":" + PORT);
        System.out.println("Maximo de conexiones: " + MAX_CLIENTES);
        System.out.println("Maximo de francotiradores: " + MAX_FRANCOTIRADORES);
        System.out.println("Escenarios disponibles: " + partida.getCantidadEscenarios());
        System.out.println("Primer escenario: " + partida.getEscenario().getNombre());
        System.out.println("Estado de partida: " + partida.getFase());
    }

    private void cargarPartida() throws IOException {
        try {
            GestorEscenarios gestorEscenarios = new GestorEscenarios(DIRECTORIO_ESCENARIOS);
            partida = new Partida(gestorEscenarios);

        } catch (IllegalArgumentException error) {
            throw new IOException("No se pudo preparar la partida: " + error.getMessage(), error);
        }
    }

    public Socket esperarConexion() throws IOException {
        if (!ejecutando || serverSocket == null) {
            throw new IOException("El servidor no esta disponible");
        }

        return serverSocket.accept();
    }

    public synchronized boolean registrar(ThreadServidor threadServidor) {
        if (!ejecutando || threadServidor == null) {
            return false;
        }

        if (threadServidores.size() >= MAX_CLIENTES) {
            return false;
        }

        threadServidores.add(threadServidor);

        return true;
    }

    public synchronized int registrarJugador(ThreadServidor threadServidor, String nombre, String rol) {
        if (threadServidor == null || !threadServidores.contains(threadServidor)) {
            throw new IllegalArgumentException("La conexion no pertenece al servidor");
        }

        if (threadServidor.estaRegistrado()) {
            throw new IllegalArgumentException("Esta conexion ya tiene un jugador registrado");
        }

        if (partida == null || !partida.estaEsperando()) {
            throw new IllegalArgumentException("La partida ya fue iniciada y no acepta nuevos jugadores");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }

        String nombreLimpio = nombre.trim();
        String rolLimpio = normalizarRol(rol);

        for (ThreadServidor conexion : threadServidores) {
            if (conexion.estaRegistrado() && conexion.getNombre().equalsIgnoreCase(nombreLimpio)) {
                throw new IllegalArgumentException("Ya existe un jugador con ese nombre");
            }
        }

        if (Mensaje.ROL_FRANCOTIRADOR.equals(rolLimpio) && getCantidadFrancotiradores() >= MAX_FRANCOTIRADORES) {
            throw new IllegalArgumentException("Ya existen dos francotiradores");
        }

        int idJugador = siguienteIdJugador;

        partida.agregarJugador(idJugador, nombreLimpio, rolLimpio);

        siguienteIdJugador++;

        threadServidor.setIdJugador(idJugador);
        threadServidor.setNombre(nombreLimpio);
        threadServidor.setRolJugador(rolLimpio);

        System.out.println("Jugador registrado: ID " + idJugador + " - " + nombreLimpio + " - " + rolLimpio);

        return idJugador;
    }

    private String normalizarRol(String rol) {
        if (rol == null) {
            throw new IllegalArgumentException("Debe seleccionar un rol");
        }

        String valor = rol.trim().toUpperCase();

        if (!Mensaje.ROL_SUPERVIVIENTE.equals(valor) && !Mensaje.ROL_FRANCOTIRADOR.equals(valor)) {
            throw new IllegalArgumentException("Rol invalido");
        }

        return valor;
    }

    public synchronized int getCantidadFrancotiradores() {
        int cantidad = 0;

        for (ThreadServidor conexion : threadServidores) {
            if (conexion.estaRegistrado() && Mensaje.ROL_FRANCOTIRADOR.equals(conexion.getRolJugador())) {
                cantidad++;
            }
        }

        return cantidad;
    }

    public boolean iniciarPartida() {
        if (partida == null) {
            System.out.println("No hay una partida cargada");
            return false;
        }

        if (!partida.estaEsperando()) {
            System.out.println("La partida ya fue iniciada");
            return false;
        }

        if (partida.getCantidadJugadores() < Partida.MIN_JUGADORES) {
            System.out.println("Se requieren al menos dos jugadores");
            return false;
        }

        try {
            if (!partida.iniciar()) {
                System.out.println("No se pudo iniciar la partida");
                return false;
            }

            EstadoPartida estado = partida.crearEstado();

            System.out.println("Partida iniciada");
            System.out.println("Escenario: " + estado.getNumeroEscenario() + " / " + estado.getCantidadEscenarios());

            for (Jugador jugador : estado.getJugadores()) {
                System.out.println("ID " + jugador.getId() + " - " + jugador.getNombre());
            }

            enviarEstado();
            iniciarThreadPartida();

            return true;

        } catch (IllegalStateException error) {
            System.err.println("No se pudo iniciar la partida: " + error.getMessage());
            return false;
        }
    }

    public boolean reintentarPartida() {
        if (partida == null) {
            System.out.println("No hay una partida cargada");
            return false;
        }

        if (!EstadoPartida.DERROTA.equals(partida.getFase())) {
            System.out.println("Solo se puede reintentar despues de una derrota");
            return false;
        }

        if (!partida.reintentarEscenario()) {
            System.out.println("No se pudo reintentar el escenario");
            return false;
        }

        System.out.println("Partida reanudada");
        System.out.println("Escenario: " + partida.getNumeroEscenario() + " / " + partida.getCantidadEscenarios());

        enviarEstado();
        reiniciarThreadPartida();

        return true;
    }

    private void iniciarThreadPartida() {
        if (threadPartida != null && threadPartida.isAlive()) {
            return;
        }

        threadPartida = new ThreadPartida(this, partida);
        threadPartida.start();
    }

    private void reiniciarThreadPartida() {
        if (threadPartida != null) {
            threadPartida.detener();
        }

        threadPartida = new ThreadPartida(this, partida);
        threadPartida.start();
    }

    public boolean moverJugador(int idJugador, Direccion direccion) {
        if (partida == null || !partida.estaJugando()) {
            return false;
        }

        boolean movio = partida.mover(idJugador, direccion);

        if (movio) {
            enviarEstado();
        }

        return movio;
    }

    public boolean moverCamaraJugador(int idJugador, Direccion direccion) {
        if (partida == null || !partida.estaJugando()) {
            return false;
        }

        boolean movio = partida.moverCamara(idJugador, direccion);

        if (movio) {
            enviarEstado();
        }

        return movio;
    }

    public boolean dispararJugador(int idJugador, int fila, int columna) {
        if (partida == null || !partida.estaJugando()) {
            return false;
        }

        boolean disparoRealizado = partida.disparar(idJugador, fila, columna);

        if (disparoRealizado) {
            enviarEstado();
        }

        return disparoRealizado;
    }

    public void enviarEstado() {
        if (partida == null) {
            return;
        }

        Mensaje mensaje = new Mensaje(Mensaje.ESTADO, 0);

        mensaje.setEstado(partida.crearEstado());

        broadcast(mensaje);
    }

    public void broadcast(Mensaje mensaje) {
        List<ThreadServidor> conexiones;

        synchronized (this) {
            conexiones = new ArrayList<>(threadServidores);
        }

        for (ThreadServidor threadServidor : conexiones) {
            if (threadServidor.estaRegistrado()) {
                threadServidor.enviar(mensaje);
            }
        }
    }

    public synchronized void retirar(ThreadServidor threadServidor) {
        if (threadServidor == null) {
            return;
        }

        boolean registrado = threadServidor.estaRegistrado();
        int idJugador = threadServidor.getIdJugador();
        String descripcion = registrado ? threadServidor.getNombre() + " (ID " + idJugador + ")" : "cliente sin registrar";

        if (!threadServidores.remove(threadServidor)) {
            return;
        }

        if (registrado && partida != null) {
            partida.desconectar(idJugador);
        }

        System.out.println("Desconectado: " + descripcion);
        System.out.println("Conexiones activas: " + threadServidores.size());

        if (ejecutando && partida != null) {
            enviarEstado();
        }
    }

    public synchronized int getCantidadConexiones() {
        return threadServidores.size();
    }

    public synchronized int getCantidadRegistrados() {
        int cantidad = 0;

        for (ThreadServidor threadServidor : threadServidores) {
            if (threadServidor.estaRegistrado()) {
                cantidad++;
            }
        }

        return cantidad;
    }

    public String getFasePartida() {
        return partida == null ? "SIN_PARTIDA" : partida.getFase();
    }
    
    public synchronized EstadoPartida getEstadoActual() {
        if (partida == null) {
            return null;
        }

        return partida.crearEstado();
    }

    public synchronized int getCantidadEscenarios() {
        if (partida == null) {
            return 0;
        }

        return partida.getCantidadEscenarios();
    }

    public synchronized int getNumeroEscenarioActual() {
        if (partida == null) {
            return 0;
        }

        return partida.getNumeroEscenario();
    }

    public synchronized String getNombreEscenarioActual() {
        if (partida == null || partida.getEscenario() == null) {
            return "";
        }

        return partida.getEscenario().getNombre();
    }

    public boolean isEjecutando() {
        return ejecutando;
    }

    public void cerrar() {
        if (!ejecutando) {
            return;
        }

        ejecutando = false;

        if (threadPartida != null) {
            threadPartida.detener();
        }

        if (threadConexiones != null) {
            threadConexiones.detener();
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();

            } catch (IOException error) {
                System.err.println("Error al cerrar el ServerSocket: " + error.getMessage());
            }
        }

        List<ThreadServidor> conexiones;

        synchronized (this) {
            conexiones = new ArrayList<>(threadServidores);
        }

        for (ThreadServidor threadServidor : conexiones) {
            threadServidor.cerrar();
        }

        synchronized (this) {
            threadServidores.clear();
        }

        System.out.println("Servidor cerrado");
    }
}