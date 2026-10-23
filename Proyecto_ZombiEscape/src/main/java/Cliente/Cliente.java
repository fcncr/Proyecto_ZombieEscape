package Cliente;

import Models.Direccion;
import Models.EstadoPartida;
import Models.Mensaje;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.SwingUtilities;
import Models.Francotirador;
import Models.Jugador;

public class Cliente {

    public static final String HOST = "localhost";
    public static final int PORT = 8084;
    public static final int TIMEOUT_CONEXION_MS = 3000;
    public static final int MAX_LONGITUD_CHAT = 200;

    private Socket socket;
    private ObjectOutputStream salida;
    private ThreadCliente threadCliente;
    private ClienteFrame frame;
    private int idJugador;
    private String nombre;
    private String rolJugador;
    private EstadoPartida ultimoEstado;
    private volatile boolean conectado;

    public Cliente() {
        this.idJugador = -1;
        this.nombre = null;
        this.rolJugador = null;
        this.ultimoEstado = null;
        this.conectado = false;
    }

    public void setFrame(ClienteFrame frame) {
        this.frame = frame;
    }

    public synchronized void conectarCliente(String nombre, String rol) throws IOException {
        if (conectado) {
            throw new IllegalStateException("El cliente ya esta conectado");
        }

        socket = new Socket();
        socket.connect(new InetSocketAddress(HOST, PORT), TIMEOUT_CONEXION_MS);

        salida = new ObjectOutputStream(socket.getOutputStream());
        salida.flush();

        conectado = true;

        threadCliente = new ThreadCliente(this, socket);
        threadCliente.start();

        solicitarRegistro(nombre, rol);
    }

    public synchronized void solicitarRegistro(String nombre, String rol) {
        if (!conectado) {
            throw new IllegalStateException("El cliente no esta conectado");
        }

        if (idJugador > 0) {
            throw new IllegalStateException("El jugador ya esta registrado");
        }

        String nombreLimpio = nombre == null ? "" : nombre.trim();
        String rolLimpio = normalizarRol(rol);

        if (nombreLimpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }

        enviar(new Mensaje(Mensaje.UNIRSE, nombreLimpio, rolLimpio));
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

    public synchronized boolean mover(Direccion direccion) {
        if (!conectado || idJugador <= 0 || direccion == null || ultimoEstado == null) {
            return false;
        }

        if (!EstadoPartida.JUGANDO.equals(ultimoEstado.getFase())) {
            return false;
        }

        if (Mensaje.ROL_FRANCOTIRADOR.equals(rolJugador)) {
            return false;
        }

        enviar(new Mensaje(Mensaje.MOVER, direccion.name()));
        return true;
    }

    public synchronized boolean moverCamara(Direccion direccion) {
        if (!conectado || idJugador <= 0 || direccion == null || ultimoEstado == null) {
            return false;
        }

        if (!EstadoPartida.JUGANDO.equals(ultimoEstado.getFase())) {
            return false;
        }

        if (!Mensaje.ROL_FRANCOTIRADOR.equals(rolJugador)) {
            return false;
        }

        enviar(new Mensaje(Mensaje.MOVER_CAMARA, direccion.name()));
        return true;
    }
    
    public synchronized boolean dispararMira() {
        if (!conectado || idJugador <= 0 || ultimoEstado == null) {
            return false;
        }

        if (!EstadoPartida.JUGANDO.equals(ultimoEstado.getFase())) {
            return false;
        }

        if (!Mensaje.ROL_FRANCOTIRADOR.equals(rolJugador)) {
            return false;
        }

        Jugador jugador = ultimoEstado.getJugadorPorId(idJugador);

        if (!(jugador instanceof Francotirador)) {
            return false;
        }

        Francotirador francotirador = (Francotirador) jugador;

        if (francotirador.getCentroVista() == null || francotirador.getBalas() <= 0) {
            return false;
        }

        int fila = francotirador.getCentroVista().getFila();
        int columna = francotirador.getCentroVista().getColumna();

        enviar(new Mensaje(Mensaje.DISPARAR, Integer.toString(fila), Integer.toString(columna)));
        return true;
    }

    public synchronized boolean disparar(int fila, int columna) {
        if (!conectado || idJugador <= 0 || ultimoEstado == null) {
            return false;
        }

        if (!EstadoPartida.JUGANDO.equals(ultimoEstado.getFase())) {
            return false;
        }

        if (!Mensaje.ROL_FRANCOTIRADOR.equals(rolJugador)) {
            return false;
        }

        enviar(new Mensaje(Mensaje.DISPARAR, Integer.toString(fila), Integer.toString(columna)));
        return true;
    }

    public synchronized boolean enviarChat(String texto) {
        if (!conectado || idJugador <= 0 || texto == null) {
            return false;
        }

        String textoLimpio = texto.trim();

        if (textoLimpio.isEmpty() || textoLimpio.length() > MAX_LONGITUD_CHAT) {
            return false;
        }

        enviar(new Mensaje(Mensaje.CHAT, textoLimpio));
        return true;
    }

    private synchronized void enviar(Mensaje mensaje) {
        if (!conectado || salida == null || mensaje == null) {
            return;
        }

        try {
            salida.writeObject(mensaje);
            salida.flush();
            salida.reset();

        } catch (IOException error) {
            conexionRemotaCerrada();
        }
    }

    public void procesarMensaje(Mensaje mensaje) {
        if (mensaje == null) {
            return;
        }

        switch (mensaje.getTipo()) {
            case Mensaje.BIENVENIDO:
                procesarBienvenida(mensaje);
                break;

            case Mensaje.ESTADO:
                procesarEstado(mensaje);
                break;

            case Mensaje.CHAT:
                procesarChat(mensaje);
                break;

            case Mensaje.ERROR:
                procesarError(mensaje);
                break;

            default:
                break;
        }
    }

    private void procesarBienvenida(Mensaje mensaje) {
        String[] parametros = mensaje.getParametros();

        if (parametros.length < 3) {
            return;
        }

        try {
            idJugador = Integer.parseInt(parametros[0]);
            nombre = parametros[1];
            rolJugador = parametros[2];

            if (frame != null) {
                SwingUtilities.invokeLater(() -> frame.registroExitoso(idJugador, nombre, rolJugador));
            }

        } catch (NumberFormatException error) {
            procesarError(new Mensaje(Mensaje.ERROR, "ID de jugador invalido"));
        }
    }

    private void procesarEstado(Mensaje mensaje) {
        EstadoPartida estado = mensaje.getEstado();

        if (estado == null) {
            return;
        }

        ultimoEstado = estado;

        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.actualizarEstado(estado));
        }
    }

    private void procesarChat(Mensaje mensaje) {
        String[] parametros = mensaje.getParametros();

        if (parametros.length < 2) {
            return;
        }

        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.agregarMensajeChat(parametros[0], parametros[1]));
        }
    }

    private void procesarError(Mensaje mensaje) {
        String[] parametros = mensaje.getParametros();
        String texto = parametros.length > 0 ? parametros[0] : "Error desconocido";

        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.mostrarError(texto, idJugador <= 0));
        }
    }

    public synchronized void desconectar() {
        cerrarRecursos();
    }

    public synchronized void conexionRemotaCerrada() {
        if (!conectado) {
            return;
        }

        cerrarRecursos();

        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.conexionCerrada());
        }
    }

    private void cerrarRecursos() {
        conectado = false;

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException error) {
            }
        }

        socket = null;
        salida = null;
        ultimoEstado = null;
    }

    public int getIdJugador() {
        return idJugador;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRolJugador() {
        return rolJugador;
    }

    public EstadoPartida getUltimoEstado() {
        return ultimoEstado;
    }

    public boolean isConectado() {
        return conectado;
    }
}