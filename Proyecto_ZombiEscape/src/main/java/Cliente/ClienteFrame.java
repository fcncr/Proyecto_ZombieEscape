package Cliente;

import Models.Direccion;
import Models.EstadoJugador;
import Models.EstadoPartida;
import Models.Jugador;
import Models.Superviviente;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class ClienteFrame extends JFrame {

    private final Cliente cliente;
    private final JTextField campoNombre;
    private final JButton botonConectar;
    private final JLabel etiquetaEstado;
    private final JLabel etiquetaJugador;
    private final JLabel etiquetaPartida;
    private final JLabel etiquetaPosicion;
    private final JLabel etiquetaControles;
    private final JLabel etiquetaTiempo;
    private final JLabel etiquetaOcultamiento;
    private final PanelMapa panelMapa;
    private final JTextArea areaChat;
    private final JTextField campoChat;
    private final JButton botonEnviarChat;
    private boolean preservarErrorAlCerrar;

    public ClienteFrame() {
        cliente = new Cliente();
        cliente.setFrame(this);

        campoNombre = new JTextField(18);
        botonConectar = new JButton("Conectar");
        etiquetaEstado = new JLabel("Desconectado");
        etiquetaJugador = new JLabel("Jugador sin registrar");
        etiquetaPartida = new JLabel("Partida: ESPERANDO");
        etiquetaPosicion = new JLabel("Posicion: sin asignar");
        etiquetaControles = new JLabel("Movimiento: flechas del teclado");
        etiquetaTiempo = new JLabel("Tiempo: --:--");
        etiquetaOcultamiento = new JLabel("Estado jugador: visible");
        panelMapa = new PanelMapa();
        areaChat = new JTextArea();
        campoChat = new JTextField();
        botonEnviarChat = new JButton("Enviar");
        preservarErrorAlCerrar = false;

        configurarVentana();
        construirInterfaz();
        configurarEventos();
        configurarControlesMovimiento();
        configurarChat();
    }

    private void configurarVentana() {
        setTitle("ZombiEscape - Cliente");
        setSize(960, 680);
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
    }

    private void construirInterfaz() {
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        panelSuperior.add(new JLabel("Nombre:"));
        panelSuperior.add(campoNombre);
        panelSuperior.add(botonConectar);

        JPanel panelInformacion = new JPanel(new GridLayout(4, 2, 10, 5));
        panelInformacion.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));

        etiquetaJugador.setFont(etiquetaJugador.getFont().deriveFont(Font.BOLD, 15f));

        panelInformacion.add(etiquetaJugador);
        panelInformacion.add(etiquetaPartida);
        panelInformacion.add(etiquetaPosicion);
        panelInformacion.add(etiquetaTiempo);
        panelInformacion.add(etiquetaOcultamiento);
        panelInformacion.add(etiquetaEstado);
        panelInformacion.add(etiquetaControles);
        panelInformacion.add(new JLabel(""));

        JScrollPane scrollMapa = new JScrollPane(panelMapa);
        scrollMapa.setBorder(BorderFactory.createTitledBorder("Escenario"));

        JPanel panelJuego = new JPanel(new BorderLayout());
        panelJuego.add(panelInformacion, BorderLayout.NORTH);
        panelJuego.add(scrollMapa, BorderLayout.CENTER);

        JPanel panelChat = construirPanelChat();

        JPanel panelCentral = new JPanel(new BorderLayout(10, 0));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panelCentral.add(panelJuego, BorderLayout.CENTER);
        panelCentral.add(panelChat, BorderLayout.EAST);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
    }

    private JPanel construirPanelChat() {
        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);

        JScrollPane scrollChat = new JScrollPane(areaChat);
        scrollChat.setPreferredSize(new Dimension(260, 400));

        JPanel panelEnvio = new JPanel(new BorderLayout(5, 0));
        panelEnvio.add(campoChat, BorderLayout.CENTER);
        panelEnvio.add(botonEnviarChat, BorderLayout.EAST);

        JPanel panelChat = new JPanel(new BorderLayout(5, 5));
        panelChat.setBorder(BorderFactory.createTitledBorder("Chat"));
        panelChat.setPreferredSize(new Dimension(280, 500));
        panelChat.add(scrollChat, BorderLayout.CENTER);
        panelChat.add(panelEnvio, BorderLayout.SOUTH);

        return panelChat;
    }

    private void configurarEventos() {
        botonConectar.addActionListener(evento -> intentarRegistro());
        campoNombre.addActionListener(evento -> intentarRegistro());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent evento) {
                cliente.desconectar();
            }
        });
    }

    private void configurarControlesMovimiento() {
        registrarMovimiento(KeyEvent.VK_UP, "moverArriba", Direccion.ARRIBA);
        registrarMovimiento(KeyEvent.VK_DOWN, "moverAbajo", Direccion.ABAJO);
        registrarMovimiento(KeyEvent.VK_LEFT, "moverIzquierda", Direccion.IZQUIERDA);
        registrarMovimiento(KeyEvent.VK_RIGHT, "moverDerecha", Direccion.DERECHA);
    }

    private void registrarMovimiento(int codigoTecla, String nombreAccion, Direccion direccion) {
        KeyStroke tecla = KeyStroke.getKeyStroke(codigoTecla, 0);

        panelMapa.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(tecla, nombreAccion);

        panelMapa.getActionMap().put(nombreAccion, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                cliente.mover(direccion);
            }
        });
    }

    private void configurarChat() {
        campoChat.setEnabled(false);
        botonEnviarChat.setEnabled(false);

        botonEnviarChat.addActionListener(evento -> enviarMensajeChat());
        campoChat.addActionListener(evento -> enviarMensajeChat());
    }

    private void enviarMensajeChat() {
        String texto = campoChat.getText().trim();

        if (texto.isEmpty()) {
            return;
        }

        if (texto.length() > Cliente.MAX_LONGITUD_CHAT) {
            mostrarError("El mensaje no puede superar " + Cliente.MAX_LONGITUD_CHAT + " caracteres", false);
            return;
        }

        if (cliente.enviarChat(texto)) {
            campoChat.setText("");
            campoChat.requestFocusInWindow();
        }
    }

    private void intentarRegistro() {
        String nombre = campoNombre.getText().trim();

        preservarErrorAlCerrar = false;

        if (nombre.isEmpty()) {
            mostrarError("El nombre no puede estar vacio", true);
            return;
        }

        botonConectar.setEnabled(false);
        campoNombre.setEnabled(false);
        etiquetaEstado.setText("Conectando...");

        Thread conexion = new Thread(() -> {
            try {
                if (cliente.isConectado()) {
                    cliente.solicitarRegistro(nombre);
                } else {
                    cliente.conectarCliente(nombre);
                }

                SwingUtilities.invokeLater(() -> etiquetaEstado.setText("Esperando respuesta del servidor..."));

            } catch (IOException | IllegalArgumentException | IllegalStateException error) {
                SwingUtilities.invokeLater(() -> mostrarError(error.getMessage(), true));
            }
        });

        conexion.setName("ConexionCliente");
        conexion.start();
    }

    public void registroExitoso(int idJugador, String nombre) {
        preservarErrorAlCerrar = false;
        etiquetaJugador.setText("ID " + idJugador + " - " + nombre);
        etiquetaEstado.setText("Conectado y registrado");
        etiquetaPartida.setText("Partida: ESPERANDO");
        etiquetaPosicion.setText("Posicion: sin asignar");
        etiquetaTiempo.setText("Tiempo: --:--");
        etiquetaOcultamiento.setText("Estado jugador: activo");
        campoNombre.setEnabled(false);
        botonConectar.setEnabled(false);
        botonConectar.setText("Conectado");
        campoChat.setEnabled(true);
        botonEnviarChat.setEnabled(true);
        panelMapa.setIdJugadorLocal(idJugador);
        campoChat.requestFocusInWindow();
    }

    public void actualizarEstado(EstadoPartida estado) {
        etiquetaPartida.setText("Partida: " + estado.getFase());
        actualizarTiempo(estado);

        panelMapa.setIdJugadorLocal(cliente.getIdJugador());
        panelMapa.setEstado(estado);

        Jugador jugadorLocal = buscarJugadorLocal(estado);

        if (jugadorLocal == null || jugadorLocal.getPosicion() == null) {
            etiquetaPosicion.setText("Posicion: sin asignar");
            etiquetaOcultamiento.setText("Estado jugador: sin asignar");
            return;
        }

        etiquetaPosicion.setText("Posicion: " + jugadorLocal.getPosicion());
        actualizarEstadoJugador(jugadorLocal);

        if (estado.isDerrotaEquipo()) {
            etiquetaEstado.setText("DERROTA: un jugador fue detectado por un zombie");
        } else {
            etiquetaEstado.setText("Estado sincronizado con el servidor");
        }
    }

    private void actualizarTiempo(EstadoPartida estado) {
        if (EstadoPartida.ESPERANDO.equals(estado.getFase())) {
            etiquetaTiempo.setText("Tiempo: --:--");
            return;
        }

        long segundosTotales = estado.getTiempoRestanteMs() / 1000;
        long minutos = segundosTotales / 60;
        long segundos = segundosTotales % 60;

        etiquetaTiempo.setText(String.format("Tiempo: %02d:%02d", minutos, segundos));
    }

    private void actualizarEstadoJugador(Jugador jugador) {
        if (jugador.getEstado() == EstadoJugador.ESPECTADOR) {
            etiquetaOcultamiento.setText("Estado jugador: ESPECTADOR");
            return;
        }

        if (jugador.getEstado() == EstadoJugador.ESCAPADO) {
            etiquetaOcultamiento.setText("Estado jugador: ESCAPADO");
            return;
        }

        if (jugador.getEstado() == EstadoJugador.DESCONECTADO) {
            etiquetaOcultamiento.setText("Estado jugador: DESCONECTADO");
            return;
        }

        if (jugador instanceof Superviviente && ((Superviviente) jugador).isOculto()) {
            etiquetaOcultamiento.setText("Estado jugador: OCULTO");
            return;
        }

        etiquetaOcultamiento.setText("Estado jugador: ACTIVO");
    }

    private Jugador buscarJugadorLocal(EstadoPartida estado) {
        for (Jugador jugador : estado.getJugadores()) {
            if (jugador.getId() == cliente.getIdJugador()) {
                return jugador;
            }
        }

        return null;
    }

    public void agregarMensajeChat(String nombreRemitente, String texto) {
        areaChat.append(nombreRemitente + ": " + texto + System.lineSeparator());
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    }

    public void mostrarError(String mensaje, boolean permitirNuevoRegistro) {
        etiquetaEstado.setText("Error: " + mensaje);

        if (permitirNuevoRegistro && cliente.getIdJugador() <= 0) {
            preservarErrorAlCerrar = true;
        }

        if (permitirNuevoRegistro) {
            campoNombre.setEnabled(true);
            botonConectar.setEnabled(true);
            botonConectar.setText("Conectar");
            campoNombre.requestFocusInWindow();
            campoNombre.selectAll();
        }
    }

    public void conexionCerrada() {
        if (!isDisplayable()) {
            return;
        }

        if (!preservarErrorAlCerrar) {
            etiquetaEstado.setText("Conexion cerrada");
        }

        etiquetaJugador.setText("Jugador sin registrar");
        etiquetaPartida.setText("Partida: sin conexion");
        etiquetaPosicion.setText("Posicion: sin asignar");
        etiquetaTiempo.setText("Tiempo: --:--");
        etiquetaOcultamiento.setText("Estado jugador: sin conexion");
        campoChat.setEnabled(false);
        botonEnviarChat.setEnabled(false);
        panelMapa.setEstado(null);
    }
}