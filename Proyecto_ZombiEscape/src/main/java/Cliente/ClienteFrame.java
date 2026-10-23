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
import java.awt.CardLayout;

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
    private static final String VISTA_JUEGO_SUPERVIVIENTE = "JUEGO_SUPERVIVIENTE";
    private static final String VISTA_CONEXION = "CONEXION";
    private static final String VISTA_JUEGO_ACTUAL = "JUEGO_ACTUAL";
    private static final String VISTA_VICTORIA_FINAL = "VICTORIA_FINAL";
    private final CardLayout gestorVistas;
    private final JPanel panelVistas;
    private final PanelConexion panelConexion;
    private static final String VISTA_SALA_ESPERA = "SALA_ESPERA";
    private final PanelSalaEspera panelSalaEspera;
    private final PanelPartidaSuperviviente panelPartidaSuperviviente;
    private static final String VISTA_JUEGO_FRANCOTIRADOR = "JUEGO_FRANCOTIRADOR";
    private final PanelPartidaFrancotirador panelPartidaFrancotirador;
    private static final String VISTA_OBSERVACION_RESULTADO = "OBSERVACION_RESULTADO";
    private final PanelObservacionResultado panelObservacionResultado;
    private final PanelVictoriaFinal panelVictoriaFinal;
    
    public ClienteFrame() {
        cliente = new Cliente();
        cliente.setFrame(this);

        gestorVistas = new CardLayout();
        panelVistas = new JPanel(gestorVistas);

        panelConexion = new PanelConexion(this::conectarDesdeNuevaInterfaz);
        panelSalaEspera = new PanelSalaEspera(this::enviarChatDesdeSala);
        panelPartidaSuperviviente = new PanelPartidaSuperviviente(cliente, this::enviarChatDesdePartida);
        panelPartidaFrancotirador = new PanelPartidaFrancotirador(cliente, this::enviarChatDesdePartida);
        panelObservacionResultado = new PanelObservacionResultado(cliente, this::enviarChatDesdePartida);
        panelVictoriaFinal = new PanelVictoriaFinal(this::enviarChatDesdePartida);

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

    private JPanel construirInterfazJuegoActual() {
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

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelCentral, BorderLayout.CENTER);

        return panelPrincipal;
    }
    
    private void conectarDesdeNuevaInterfaz(String nombre, String rol) {
        panelConexion.setConectando(true);

        Thread conexion = new Thread(() -> {
            try {
                if (cliente.isConectado()) {
                    cliente.solicitarRegistro(nombre, rol);
                } else {
                    cliente.conectarCliente(nombre, rol);
                }
            } catch (IOException | IllegalArgumentException | IllegalStateException error) {
                SwingUtilities.invokeLater(() -> panelConexion.mostrarError(error.getMessage()));
            }
        });

        conexion.setName("ConexionCliente");
        conexion.start();
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
    
    private void enviarChatDesdeSala(String texto) {
        cliente.enviarChat(texto);
    }
    
    private void enviarChatDesdePartida(String texto) {
        if (cliente.enviarChat(texto)) {
            return;
        }

        panelPartidaSuperviviente.agregarMensaje("Sistema", "No se pudo enviar el mensaje.");
        panelPartidaFrancotirador.agregarMensaje("Sistema", "No se pudo enviar el mensaje.");
    }
    
    private void construirInterfaz() {
        panelVistas.add(panelConexion, VISTA_CONEXION);
        panelVistas.add(panelSalaEspera, VISTA_SALA_ESPERA);
        panelVistas.add(panelPartidaSuperviviente, VISTA_JUEGO_SUPERVIVIENTE);
        panelVistas.add(panelPartidaFrancotirador, VISTA_JUEGO_FRANCOTIRADOR);
        panelVistas.add(panelObservacionResultado, VISTA_OBSERVACION_RESULTADO);
        panelVistas.add(panelVictoriaFinal, VISTA_VICTORIA_FINAL);
        panelVistas.add(construirInterfazJuegoActual(), VISTA_JUEGO_ACTUAL);

        panelVistas.setPreferredSize(new Dimension(1024, 640));

        setContentPane(panelVistas);
        pack();
        setLocationRelativeTo(null);

        gestorVistas.show(panelVistas, VISTA_CONEXION);
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

        if (Mensaje.ROL_FRANCOTIRADOR.equals(cliente.getRolJugador()) && evento.getKeyCode() == KeyEvent.VK_ENTER) {
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof JTextField) {
                return false;
            }

            boolean enviado = cliente.dispararMira();

            if (enviado) {
                evento.consume();
                return true;
            }

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
            etiquetaControles.setText("Flechas: camara | Enter: disparar");
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
        panelSalaEspera.setIdJugadorLocal(idJugador);
        panelPartidaSuperviviente.setIdJugadorLocal(idJugador);
        panelPartidaFrancotirador.setIdJugadorLocal(idJugador);
        panelObservacionResultado.setIdJugadorLocal(idJugador);

        gestorVistas.show(panelVistas, VISTA_SALA_ESPERA);
    }

    public void actualizarEstado(EstadoPartida estado) {
        if (estado == null) {
            return;
        }

        int idJugador = cliente.getIdJugador();
        Jugador jugadorLocal = estado.getJugadorPorId(idJugador);

        if (EstadoPartida.ESPERANDO.equals(estado.getFase())) {
            panelSalaEspera.setIdJugadorLocal(idJugador);
            panelSalaEspera.actualizarEstado(estado);
            gestorVistas.show(panelVistas, VISTA_SALA_ESPERA);
            return;
        }

        if (EstadoPartida.VICTORIA.equals(estado.getFase())) {
            panelVictoriaFinal.setIdJugadorLocal(idJugador);
            panelVictoriaFinal.actualizarEstado(estado);
            gestorVistas.show(panelVistas, VISTA_VICTORIA_FINAL);
            return;
        }

        if (EstadoPartida.DERROTA.equals(estado.getFase())) {
            panelObservacionResultado.setIdJugadorLocal(idJugador);
            panelObservacionResultado.actualizarEstado(estado);
            gestorVistas.show(panelVistas, VISTA_OBSERVACION_RESULTADO);
            return;
        }

        if (EstadoPartida.JUGANDO.equals(estado.getFase()) && jugadorLocal != null && jugadorLocal.getEstado() == EstadoJugador.ESPECTADOR) {
            panelObservacionResultado.setIdJugadorLocal(idJugador);
            panelObservacionResultado.actualizarEstado(estado);
            gestorVistas.show(panelVistas, VISTA_OBSERVACION_RESULTADO);
            return;
        }

        if (EstadoPartida.JUGANDO.equals(estado.getFase()) && jugadorLocal instanceof Superviviente) {
            panelPartidaSuperviviente.setIdJugadorLocal(idJugador);
            panelPartidaSuperviviente.actualizarEstado(estado);
            gestorVistas.show(panelVistas, VISTA_JUEGO_SUPERVIVIENTE);
            return;
        }

        if (EstadoPartida.JUGANDO.equals(estado.getFase()) && jugadorLocal instanceof Francotirador) {
            panelPartidaFrancotirador.setIdJugadorLocal(idJugador);
            panelPartidaFrancotirador.actualizarEstado(estado);
            gestorVistas.show(panelVistas, VISTA_JUEGO_FRANCOTIRADOR);
            return;
        }

        gestorVistas.show(panelVistas, VISTA_JUEGO_ACTUAL);

        etiquetaPartida.setText("Partida: " + estado.getFase());
        etiquetaEscenario.setText("Escenario: " + estado.getNumeroEscenario() + " / " + estado.getCantidadEscenarios());

        actualizarTiempo(estado);
        actualizarRanking(estado);

        panelMapa.setIdJugadorLocal(idJugador);
        panelMapa.setEstado(estado);

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
        panelSalaEspera.agregarMensaje(nombreRemitente, texto);
        panelPartidaSuperviviente.agregarMensaje(nombreRemitente, texto);
        panelPartidaFrancotirador.agregarMensaje(nombreRemitente, texto);
        panelObservacionResultado.agregarMensaje(nombreRemitente, texto);
        panelVictoriaFinal.agregarMensaje(nombreRemitente, texto);

        areaChat.append(nombreRemitente + ": " + texto + System.lineSeparator());
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    }
    public void mostrarError(String mensaje, boolean permitirNuevoRegistro) {
        if (permitirNuevoRegistro && cliente.getIdJugador() <= 0) {
            preservarErrorAlCerrar = true;

            gestorVistas.show(panelVistas, VISTA_CONEXION);
            panelConexion.mostrarError(mensaje);

            return;
        }

        etiquetaEstado.setText("Error: " + mensaje);
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