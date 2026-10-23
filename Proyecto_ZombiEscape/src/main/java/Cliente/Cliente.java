package Cliente;

import Models.Direccion;
import Models.EstadoPartida;
import Models.Mensaje;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.SwingUtilities;

public class Cliente {

    public static final String IP = "localhost";
    public static final int PORT = 8084;
    public static final int MAX_LONGITUD_CHAT = 200;
    public static final int TIMEOUT_CONEXION_MS = 3000;

    private Socket socket;
    private ThreadCliente threadCliente;
    private ObjectOutputStream escritorDeObjetos;
    private int idJugador;
    private String nombre;
    private EstadoPartida ultimoEstado;
    private ClienteFrame frame;
    private volatile boolean conectado;

    public Cliente() {
        this.idJugador = -1;
        this.conectado = false;
    }

    public void setFrame(ClienteFrame frame) {
        this.frame = frame;
    }

    public synchronized void conectarCliente(String nombre) throws IOException {
        if (conectado) {
            throw new IllegalStateException("El cliente ya esta conectado");
        }

        validarNombre(nombre);

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(IP, PORT), TIMEOUT_CONEXION_MS);

            escritorDeObjetos = new ObjectOutputStream(socket.getOutputStream());
            escritorDeObjetos.flush();

            conectado = true;
            idJugador = -1;
            ultimoEstado = null;

            threadCliente = new ThreadCliente(this, socket);
            threadCliente.start();

            solicitarRegistro(nombre);

        } catch (IOException error) {
            conectado = false;
            cerrarRecursos();
            throw new IOException("No se pudo conectar al servidor en " + IP + ":" + PORT, error);

        } catch (RuntimeException error) {
            conectado = false;
            cerrarRecursos();
            throw error;
        }
    }

    public synchronized void solicitarRegistro(String nombre) {
        validarNombre(nombre);

        if (!conectado) {
            throw new IllegalStateException("El cliente no esta conectado");
        }

        if (idJugador > 0) {
            throw new IllegalStateException("El jugador ya esta registrado");
        }

        this.nombre = nombre.trim();

        if (!enviar(new Mensaje(Mensaje.UNIRSE, this.nombre))) {
            throw new IllegalStateException("No se pudo solicitar el registro");
        }
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
    }

    public boolean mover(Direccion direccion) {
        if (direccion == null) {
            return false;
        }

        if (!conectado || idJugador <= 0) {
            return false;
        }

        if (ultimoEstado == null || !EstadoPartida.JUGANDO.equals(ultimoEstado.getFase())) {
            return false;
        }

        return enviar(new Mensaje(Mensaje.MOVER, direccion.name()));
    }

    public boolean enviarChat(String texto) {
        if (!conectado || idJugador <= 0) {
            return false;
        }

        if (texto == null) {
            return false;
        }

        String textoLimpio = texto.trim();

        if (textoLimpio.isEmpty()) {
            return false;
        }

        if (textoLimpio.length() > MAX_LONGITUD_CHAT) {
            notificarError("El mensaje no puede superar " + MAX_LONGITUD_CHAT + " caracteres");
            return false;
        }

        return enviar(new Mensaje(Mensaje.CHAT, textoLimpio));
    }

    public synchronized boolean enviar(Mensaje mensaje) {
        if (!conectado || escritorDeObjetos == null || mensaje == null) {
            return false;
        }

        try {
            escritorDeObjetos.reset();
            escritorDeObjetos.writeObject(mensaje);
            escritorDeObjetos.flush();
            return true;

        } catch (IOException error) {
            notificarError("Se perdio la conexion con el servidor");
            manejarDesconexionRemota();
            return false;
        }
    }

    public void procesar(Mensaje mensaje) {
        if (mensaje == null || mensaje.getTipo() == null) {
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
                System.out.println("Mensaje no reconocido por el cliente: " + mensaje);
                break;
        }
    }

    private void procesarBienvenida(Mensaje mensaje) {
        String[] parametros = mensaje.getParametros();

        if (parametros.length != 1) {
            notificarError("Respuesta BIENVENIDO invalida");
            return;
        }

        try {
            idJugador = Integer.parseInt(parametros[0]);

            if (frame != null) {
                SwingUtilities.invokeLater(() -> frame.registroExitoso(idJugador, nombre));
            }

        } catch (NumberFormatException error) {
            notificarError("El servidor envio un ID invalido");
        }
    }

    private void procesarEstado(Mensaje mensaje) {
        EstadoPartida estado = mensaje.getEstado();

        if (estado == null) {
            notificarError("El servidor envio un estado de partida vacio");
            return;
        }

        ultimoEstado = estado;

        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.actualizarEstado(estado));
        }
    }

    private void procesarChat(Mensaje mensaje) {
        String[] parametros = mensaje.getParametros();

        if (parametros.length != 2) {
            notificarError("El servidor envio un mensaje de chat invalido");
            return;
        }

        String nombreRemitente = parametros[0];
        String texto = parametros[1];

        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.agregarMensajeChat(nombreRemitente, texto));
        }
    }

    private void procesarError(Mensaje mensaje) {
        String[] parametros = mensaje.getParametros();
        String motivo = parametros.length > 0 ? parametros[0] : "Error desconocido";

        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.mostrarError(motivo, idJugador <= 0));
        }
    }

    public void notificarError(String mensaje) {
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.mostrarError(mensaje, false));
        }
    }

    public void manejarDesconexionRemota() {
        boolean debeNotificar;

        synchronized (this) {
            debeNotificar = conectado;
            conectado = false;
            cerrarRecursos();
        }

        if (debeNotificar && frame != null) {
            SwingUtilities.invokeLater(frame::conexionCerrada);
        }
    }

    public synchronized void desconectar() {
        conectado = false;
        cerrarRecursos();
    }

    private void cerrarRecursos() {
        if (threadCliente != null) {
            threadCliente.detener();
        }

        if (escritorDeObjetos != null) {
            try {
                escritorDeObjetos.close();
            } catch (IOException error) {
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException error) {
            }
        }

        escritorDeObjetos = null;
        socket = null;
        threadCliente = null;
    }

    public boolean isConectado() {
        return conectado;
    }

    public int getIdJugador() {
        return idJugador;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoPartida getUltimoEstado() {
        return ultimoEstado;
    }
}