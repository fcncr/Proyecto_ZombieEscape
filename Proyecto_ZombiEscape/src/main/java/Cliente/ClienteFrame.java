package Cliente;

import Models.Direccion;
import Models.EstadoJugador;
import Models.EstadoPartida;
import Models.Francotirador;
import Models.Jugador;
import Models.Mensaje;
import Models.Superviviente;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ClienteFrame extends JFrame {

    private final Cliente cliente;
    private final JTextField campoNombre;
    private final JComboBox<String> comboRol;
    private final JButton botonConectar;
    private final JLabel etiquetaEstado;
    private final JLabel etiquetaJugador;
    private final JLabel etiquetaRol;
    private final JLabel etiquetaPartida;
    private final JLabel etiquetaEscenario;
    private final JLabel etiquetaPosicion;
    private final JLabel etiquetaPuntos;
    private final JLabel etiquetaControles;
    private final JLabel etiquetaTiempo;
    private final JLabel etiquetaOcultamiento;
    private final PanelMapa panelMapa;
    private final JTextArea areaRanking;
    private final JTextArea areaChat;
    private final JTextField campoChat;
    private final JButton botonEnviarChat;
    private final KeyEventDispatcher dispatcherMovimiento;
    private boolean preservarErrorAlCerrar;

    public ClienteFrame() {
        cliente = new Cliente();
        cliente.setFrame(this);

        campoNombre = new JTextField(15);
        comboRol = new JComboBox<>(new String[]{Mensaje.ROL_SUPERVIVIENTE, Mensaje.ROL_FRANCOTIRADOR});
        botonConectar = new JButton("Conectar");
        etiquetaEstado = new JLabel("Desconectado");
        etiquetaJugador = new JLabel("Jugador sin registrar");
        etiquetaRol = new JLabel("Rol: sin seleccionar");
        etiquetaPartida = new JLabel("Partida: ESPERANDO");
        etiquetaEscenario = new JLabel("Escenario: 1 / ?");
        etiquetaPosicion = new JLabel("Posicion: sin asignar");
        etiquetaPuntos = new JLabel("Puntos: 0");
        etiquetaControles = new JLabel("Movimiento: flechas del teclado");
        etiquetaTiempo = new JLabel("Tiempo: --:--");
        etiquetaOcultamiento = new JLabel("Estado jugador: sin asignar");
        panelMapa = new PanelMapa(cliente);
        areaRanking = new JTextArea();
        areaChat = new JTextArea();
        campoChat = new JTextField();
        botonEnviarChat = new JButton("Enviar");
        preservarErrorAlCerrar = false;

        dispatcherMovimiento = evento -> procesarTeclaMovimiento(evento);

        configurarVentana();
        construirInterfaz();
        configurarEventos();
        configurarControlesMovimiento();
        configurarChat();
    }

    private void configurarVentana() {
        setTitle("ZombiEscape - Cliente");
        setSize(1000, 700);
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
    }

    private void construirInterfaz() {
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        panelSuperior.add(new JLabel("Nombre:"));
        panelSuperior.add(campoNombre);
        panelSuperior.add(new JLabel("Rol:"));
        panelSuperior.add(comboRol);
        panelSuperior.add(botonConectar);

        JPanel panelInformacion = new JPanel(new GridLayout(6, 2, 10, 5));

        panelInformacion.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));

        etiquetaJugador.setFont(etiquetaJugador.getFont().deriveFont(Font.BOLD, 15f));

        panelInformacion.add(etiquetaJugador);
        panelInformacion.add(etiquetaRol);

        panelInformacion.add(etiquetaPartida);
        panelInformacion.add(etiquetaEscenario);

        panelInformacion.add(etiquetaTiempo);
        panelInformacion.add(etiquetaPuntos);

        panelInformacion.add(etiquetaPosicion);
        panelInformacion.add(etiquetaOcultamiento);

        panelInformacion.add(etiquetaEstado);
        panelInformacion.add(etiquetaControles);

        panelInformacion.add(new JLabel(""));
        panelInformacion.add(new JLabel(""));

        JScrollPane scrollMapa = new JScrollPane(panelMapa);

        scrollMapa.setBorder(BorderFactory.createTitledBorder("Escenario"));

        JPanel panelJuego = new JPanel(new BorderLayout());

        panelJuego.add(panelInformacion, BorderLayout.NORTH);
        panelJuego.add(scrollMapa, BorderLayout.CENTER);

        JPanel panelCentral = new JPanel(new BorderLayout(10, 0));

        panelCentral.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panelCentral.add(panelJuego, BorderLayout.CENTER);
        panelCentral.add(construirPanelLateral(), BorderLayout.EAST);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
    }

    private JPanel construirPanelLateral() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        panel.setPreferredSize(new Dimension(280, 550));
        panel.add(construirPanelRanking(), BorderLayout.NORTH);
        panel.add(construirPanelChat(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel construirPanelRanking() {
        areaRanking.setEditable(false);
        areaRanking.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaRanking.setText("Sin jugadores");
        areaRanking.setRows(6);

        JScrollPane scrollRanking = new JScrollPane(areaRanking);

        scrollRanking.setPreferredSize(new Dimension(270, 150));

        JPanel panelRanking = new JPanel(new BorderLayout());

        panelRanking.setBorder(BorderFactory.createTitledBorder("Ranking"));
        panelRanking.add(scrollRanking, BorderLayout.CENTER);

        return panelRanking;
    }

    private JPanel construirPanelChat() {
        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);

        JScrollPane scrollChat = new JScrollPane(areaChat);

        JPanel panelEnvio = new JPanel(new BorderLayout(5, 0));

        panelEnvio.add(campoChat, BorderLayout.CENTER);
        panelEnvio.add(botonEnviarChat, BorderLayout.EAST);

        JPanel panelChat = new JPanel(new BorderLayout(5, 5));

        panelChat.setBorder(BorderFactory.createTitledBorder("Chat"));
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
                KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcherMovimiento);
                cliente.desconectar();
            }
        });
    }

    private void configurarControlesMovimiento() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcherMovimiento);
    }

    private boolean procesarTeclaMovimiento(KeyEvent evento) {
        if (evento.getID() != KeyEvent.KEY_PRESSED || !isActive()) {
            return false;
        }

        Direccion direccion;

        switch (evento.getKeyCode()) {
            case KeyEvent.VK_UP:
                direccion = Direccion.ARRIBA;
                break;

            case KeyEvent.VK_DOWN:
                direccion = Direccion.ABAJO;
                break;

            case KeyEvent.VK_LEFT:
                direccion = Direccion.IZQUIERDA;
                break;

            case KeyEvent.VK_RIGHT:
                direccion = Direccion.DERECHA;
                break;

            default:
                return false;
        }

        boolean enviado;

        if (Mensaje.ROL_FRANCOTIRADOR.equals(cliente.getRolJugador())) {
            enviado = cliente.moverCamara(direccion);
        } else {
            enviado = cliente.mover(direccion);
        }

        if (enviado) {
            evento.consume();
            return true;
        }

        return false;
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
        }
    }

    private void intentarRegistro() {
        String nombre = campoNombre.getText().trim();
        String rol = (String) comboRol.getSelectedItem();

        preservarErrorAlCerrar = false;

        if (nombre.isEmpty()) {
            mostrarError("El nombre no puede estar vacio", true);
            return;
        }

        botonConectar.setEnabled(false);
        campoNombre.setEnabled(false);
        comboRol.setEnabled(false);
        etiquetaEstado.setText("Conectando...");

        Thread conexion = new Thread(() -> {
            try {
                if (cliente.isConectado()) {
                    cliente.solicitarRegistro(nombre, rol);
                } else {
                    cliente.conectarCliente(nombre, rol);
                }

                SwingUtilities.invokeLater(() -> etiquetaEstado.setText("Esperando respuesta del servidor..."));

            } catch (IOException | IllegalArgumentException | IllegalStateException error) {
                SwingUtilities.invokeLater(() -> mostrarError(error.getMessage(), true));
            }
        });

        conexion.setName("ConexionCliente");
        conexion.start();
    }

    public void registroExitoso(int idJugador, String nombre, String rol) {
        preservarErrorAlCerrar = false;

        etiquetaJugador.setText("ID " + idJugador + " - " + nombre);
        etiquetaRol.setText("Rol: " + rol);
        etiquetaEstado.setText("Conectado y registrado");
        etiquetaPartida.setText("Partida: ESPERANDO");
        etiquetaTiempo.setText("Tiempo: --:--");
        etiquetaPuntos.setText("Puntos: 0");

        if (Mensaje.ROL_FRANCOTIRADOR.equals(rol)) {
            etiquetaPosicion.setText("Camara: esperando inicio");
            etiquetaOcultamiento.setText("Balas: 10");
            etiquetaControles.setText("Flechas: camara | Clic izquierdo: disparar");

        } else {
            etiquetaPosicion.setText("Posicion: sin asignar");
            etiquetaOcultamiento.setText("Estado jugador: ACTIVO");
            etiquetaControles.setText("Movimiento: flechas del teclado");
        }

        campoNombre.setEnabled(false);
        comboRol.setEnabled(false);
        botonConectar.setEnabled(false);
        botonConectar.setText("Conectado");
        campoChat.setEnabled(true);
        botonEnviarChat.setEnabled(true);

        panelMapa.setIdJugadorLocal(idJugador);
    }

    public void actualizarEstado(EstadoPartida estado) {
        etiquetaPartida.setText("Partida: " + estado.getFase());
        etiquetaEscenario.setText("Escenario: " + estado.getNumeroEscenario() + " / " + estado.getCantidadEscenarios());

        actualizarTiempo(estado);
        actualizarRanking(estado);

        panelMapa.setIdJugadorLocal(cliente.getIdJugador());
        panelMapa.setEstado(estado);

        Jugador jugadorLocal = estado.getJugadorPorId(cliente.getIdJugador());

        if (jugadorLocal == null) {
            etiquetaPosicion.setText("Posicion: sin asignar");
            etiquetaPuntos.setText("Puntos: --");
            return;
        }

        etiquetaPuntos.setText("Puntos: " + jugadorLocal.getPuntos());

        if (jugadorLocal instanceof Francotirador) {
            Francotirador francotirador = (Francotirador) jugadorLocal;

            etiquetaPosicion.setText("Camara: " + francotirador.getCentroVista());
            etiquetaOcultamiento.setText("Balas: " + francotirador.getBalas());

        } else if (jugadorLocal.getPosicion() == null) {
            etiquetaPosicion.setText("Posicion: sin asignar");
            actualizarEstadoJugador(jugadorLocal);

        } else {
            etiquetaPosicion.setText("Posicion: " + jugadorLocal.getPosicion());
            actualizarEstadoJugador(jugadorLocal);
        }

        if (EstadoPartida.DERROTA.equals(estado.getFase())) {
            etiquetaEstado.setText("DERROTA DEL EQUIPO");

        } else if (EstadoPartida.VICTORIA.equals(estado.getFase())) {
            etiquetaEstado.setText("VICTORIA");

        } else if (estado.isDerrotaEquipo()) {
            etiquetaEstado.setText("Alerta: un superviviente murio o se desconecto. La partida continua.");

        } else {
            etiquetaEstado.setText("Estado sincronizado con el servidor");
        }
    }

    private void actualizarRanking(EstadoPartida estado) {
        StringBuilder texto = new StringBuilder();
        int posicion = 1;

        for (Jugador jugador : estado.getRanking()) {
            texto.append(posicion)
                    .append(". ")
                    .append(jugador.getNombre())
                    .append(" - ")
                    .append(jugador.getPuntos())
                    .append(System.lineSeparator());

            posicion++;
        }

        if (texto.length() == 0) {
            texto.append("Sin jugadores");
        }

        areaRanking.setText(texto.toString());
        areaRanking.setCaretPosition(0);
    }

    private void actualizarTiempo(EstadoPartida estado) {
        if (EstadoPartida.ESPERANDO.equals(estado.getFase())) {
            etiquetaTiempo.setText("Tiempo: --:--");
            return;
        }

        long tiempoMs = estado.getTiempoRestanteMs();
        boolean negativo = tiempoMs < 0;
        long valorAbsoluto = Math.abs(tiempoMs);

        long segundosTotales;

        if (negativo) {
            segundosTotales = (valorAbsoluto + 999L) / 1000L;
        } else {
            segundosTotales = valorAbsoluto / 1000L;
        }

        long minutos = segundosTotales / 60L;
        long segundos = segundosTotales % 60L;

        String signo = negativo ? "-" : "";

        etiquetaTiempo.setText(String.format("Tiempo: %s%02d:%02d", signo, minutos, segundos));
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
            comboRol.setEnabled(true);
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
        etiquetaRol.setText("Rol: sin seleccionar");
        etiquetaPartida.setText("Partida: sin conexion");
        etiquetaEscenario.setText("Escenario: -- / --");
        etiquetaPosicion.setText("Posicion: sin asignar");
        etiquetaPuntos.setText("Puntos: --");
        etiquetaTiempo.setText("Tiempo: --:--");
        etiquetaOcultamiento.setText("Estado jugador: sin conexion");

        areaRanking.setText("Sin conexion");

        campoChat.setEnabled(false);
        botonEnviarChat.setEnabled(false);

        panelMapa.setEstado(null);
    }
}