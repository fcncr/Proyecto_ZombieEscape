package Cliente;

import Models.EstadoJugador;
import Models.EstadoPartida;
import Models.Francotirador;
import Models.Jugador;
import UI.BotonImagen;
import UI.CampoTextoRedondeado;
import UI.GestorAssets;
import UI.PanelDisenoEscalable;
import UI.Tema;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PanelObservacionResultado extends PanelDisenoEscalable {

    public interface Acciones {
        void enviarChat(String texto);
    }

    private static final Color FONDO = new Color(0x1C, 0x30, 0x27);
    private static final Color MUTED = new Color(0x66, 0x71, 0x5C);
    private static final Color ALERTA = new Color(0xD4, 0x84, 0x63);

    private final Acciones acciones;
    private final PanelMapa panelMapa;
    private final JTextArea areaChat;
    private final JScrollPane scrollChat;
    private final CampoTextoRedondeado campoChat;
    private final BotonImagen botonEnviar;
    private final JLabel contadorChat;

    private EstadoPartida estado;
    private int idJugadorLocal;

    public PanelObservacionResultado(Cliente cliente, Acciones acciones) {
        super(GestorAssets.getFondoMenu());

        if (cliente == null || acciones == null) {
            throw new IllegalArgumentException("Cliente y acciones son obligatorios");
        }

        this.acciones = acciones;
        this.estado = null;
        this.idJugadorLocal = -1;

        panelMapa = new PanelMapa(cliente);
        areaChat = new JTextArea();
        scrollChat = new JScrollPane(areaChat);
        campoChat = new CampoTextoRedondeado();
        botonEnviar = new BotonImagen("Enviar", BotonImagen.PRIMARIO, 78, 56);
        contadorChat = new JLabel("0/200", SwingConstants.LEFT);

        configurarChat();

        add(panelMapa);
        add(scrollChat);
        add(campoChat);
        add(botonEnviar);
        add(contadorChat);
    }

    private void configurarChat() {
        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        areaChat.setForeground(Tema.CONTORNO);
        areaChat.setBackground(Tema.PANEL);
        areaChat.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        scrollChat.setBorder(null);
        scrollChat.getViewport().setBackground(Tema.PANEL);

        contadorChat.setForeground(MUTED);

        campoChat.addActionListener(evento -> enviarChat());
        botonEnviar.addActionListener(evento -> enviarChat());

        campoChat.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent evento) {
                actualizarContador();
            }

            @Override
            public void removeUpdate(DocumentEvent evento) {
                actualizarContador();
            }

            @Override
            public void changedUpdate(DocumentEvent evento) {
                actualizarContador();
            }
        });
    }

    private void enviarChat() {
        String texto = campoChat.getText().trim();

        if (texto.isEmpty() || texto.length() > Cliente.MAX_LONGITUD_CHAT) {
            return;
        }

        acciones.enviarChat(texto);
        campoChat.setText("");
    }

    private void actualizarContador() {
        contadorChat.setText(campoChat.getText().length() + "/200");
    }

    public void setIdJugadorLocal(int idJugadorLocal) {
        this.idJugadorLocal = idJugadorLocal;
        panelMapa.setIdJugadorLocal(idJugadorLocal);
        repaint();
    }

    public void actualizarEstado(EstadoPartida estado) {
        this.estado = estado;
        panelMapa.setEstado(estado);
        repaint();
    }

    public void agregarMensaje(String nombre, String texto) {
        areaChat.append(nombre + ": " + texto + System.lineSeparator());
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    }

    private boolean esDerrotaFinal() {
        return estado != null && EstadoPartida.DERROTA.equals(estado.getFase());
    }

    @Override
    public void doLayout() {
        panelMapa.setBounds(escalar(40, 158, 768, 576));
        scrollChat.setBounds(escalar(870, 424, 356, 230));
        campoChat.setBounds(escalar(866, 676, 282, 48));
        botonEnviar.setBounds(escalar(1153, 672, 78, 56));
        contadorChat.setBounds(escalar(870, 732, 80, 20));

        double escala = getEscalaDiseno();

        areaChat.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(11, (int) Math.round(15 * escala))));
        campoChat.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(11, (int) Math.round(15 * escala))));
        botonEnviar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(10, (int) Math.round(16 * escala))));
        contadorChat.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(9, (int) Math.round(12 * escala))));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = crearGraphicsDiseno(g);

        try {
            g2.setColor(FONDO);
            g2.fillRect(0, 0, 1280, 800);

            dibujarEncabezado(g2);
            dibujarHUD(g2);
            dibujarMarcoMapa(g2);
            dibujarEquipo(g2);
            dibujarChat(g2);
            dibujarPie(g2);
        } finally {
            g2.dispose();
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        Graphics2D g2 = crearGraphicsDiseno(g);

        try {
            if (esDerrotaFinal()) {
                dibujarDerrota(g2);
            } else {
                dibujarAvisoEspectador(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    private void dibujarEncabezado(Graphics2D g2) {
        g2.setColor(Tema.CONTORNO);
        g2.fillRect(0, 0, 1280, 76);

        g2.setFont(new Font(Font.SERIF, Font.BOLD, 30));
        g2.setColor(Tema.PANEL);
        g2.drawString("ZOMBI ESCAPE", 32, 47);

        Jugador local = obtenerJugadorLocal();
        String rol = local instanceof Francotirador ? "FRANCOTIRADOR" : "SUPERVIVIENTE";

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g2.setColor(new Color(0xC3, 0xD0, 0xB2));
        g2.drawString(rol, 352, 45);

        g2.setColor(esDerrotaFinal() ? ALERTA : Tema.JARDIN);
        g2.fillRoundRect(1070, 23, 178, 30, 30, 30);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(Tema.CONTORNO);
        dibujarTextoCentrado(g2, esDerrotaFinal() ? "DERROTA" : "OBSERVANDO", 1159, 44);
    }

    private void dibujarHUD(Graphics2D g2) {
        g2.setColor(new Color(0xF0, 0xDE, 0xB1));
        g2.fillRoundRect(32, 90, 788, 54, 12, 12);

        Jugador local = obtenerJugadorLocal();

        if (local == null) {
            return;
        }

        int plaza = Math.max(0, Math.min(9, local.getPlazaVisual()));

        g2.setColor(obtenerColorPlaza(plaza));
        g2.fillOval(43, 102, 30, 30);

        g2.setColor(Tema.CONTORNO);
        g2.drawOval(43, 102, 30, 30);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(plaza == 0 || plaza == 3 || plaza == 7 ? Color.WHITE : Tema.CONTORNO);
        dibujarTextoCentrado(g2, Integer.toString(plaza + 1), 58, 123);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g2.setColor(Tema.CONTORNO);
        g2.drawString(local.getNombre() + " · tú", 84, 124);

        g2.drawImage(GestorAssets.getIcono("timer"), 268, 102, 28, 28, null);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        g2.drawString(formatearTiempo(), 308, 124);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g2.drawString("ESCENARIO " + String.format("%02d", estado.getNumeroEscenario()) + " / " + estado.getCantidadEscenarios(), 442, 124);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g2.drawString(local.getPuntos() + " pts", 682, 124);
    }

    private void dibujarMarcoMapa(Graphics2D g2) {
        g2.setColor(new Color(0x7C, 0x92, 0x52));
        g2.fillRoundRect(30, 148, 788, 596, 13, 13);

        g2.setColor(new Color(0xCA, 0xB9, 0x7D));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(30, 148, 788, 596, 13, 13);
    }

    private void dibujarAvisoEspectador(Graphics2D g2) {
        g2.setColor(ALERTA);
        g2.fillRoundRect(93, 162, 680, 30, 30, 30);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(Tema.CONTORNO);
        dibujarTextoCentrado(g2, "EQUIPO DERROTADO · OBSERVACIÓN HASTA FINALIZAR", 433, 183);
    }

    private void dibujarDerrota(Graphics2D g2) {
        g2.setColor(Tema.PANEL);
        g2.fillRoundRect(69, 257, 712, 342, 44, 44);

        g2.setColor(Tema.CONTORNO);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(69, 257, 712, 342, 44, 44);

        g2.drawImage(GestorAssets.getIcono("warning"), 373, 281, 66, 66, null);

        g2.setFont(new Font(Font.SERIF, Font.BOLD, 32));
        g2.setColor(Tema.CONTORNO);
        dibujarTextoCentrado(g2, "DERROTA DEL EQUIPO", 425, 389);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        dibujarTextoCentrado(g2, "Un superviviente fue detectado o se desconecto.", 425, 431);
        dibujarTextoCentrado(g2, "El servidor puede reintentar este escenario.", 425, 461);

        g2.setColor(new Color(0xD4, 0xDF, 0xAF));
        g2.fillRoundRect(249, 508, 352, 30, 30, 30);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(Tema.CONTORNO);
        dibujarTextoCentrado(g2, "CHAT DISPONIBLE", 425, 529);
    }

    private void dibujarEquipo(Graphics2D g2) {
        List<Jugador> jugadores = obtenerJugadores();

        g2.setColor(Tema.PANEL);
        g2.fillRoundRect(848, 94, 400, 248, 14, 14);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2.setColor(Tema.CONTORNO);
        g2.drawString("EQUIPO", 870, 128);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g2.setColor(MUTED);
        dibujarTextoDerecha(g2, jugadores.size() + "/10", 1224, 128);

        int separacion = jugadores.size() <= 4 ? 36 : 20;
        int fuente = jugadores.size() <= 4 ? 16 : 12;
        int y = 160;

        for (Jugador jugador : jugadores) {
            int plaza = Math.max(0, Math.min(9, jugador.getPlazaVisual()));

            g2.setColor(obtenerColorPlaza(plaza));
            g2.fillOval(869, y - 17, 24, 24);

            g2.setColor(Tema.CONTORNO);
            g2.drawOval(869, y - 17, 24, 24);

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(9, fuente - 4)));
            g2.setColor(plaza == 0 || plaza == 3 || plaza == 7 ? Color.WHITE : Tema.CONTORNO);
            dibujarTextoCentrado(g2, Integer.toString(plaza + 1), 881, y);

            String nombre = jugador.getNombre();

            if (jugador.getId() == idJugadorLocal) {
                nombre += " · tú";
            }

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fuente));
            g2.setColor(Tema.CONTORNO);
            g2.drawString(nombre, 905, y);

            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(10, fuente - 2)));
            g2.setColor(MUTED);
            dibujarTextoDerecha(g2, obtenerEstadoJugador(jugador), 1224, y);

            y += separacion;
        }

        g2.setColor(new Color(0xD9, 0xCD, 0xAE));
        g2.drawLine(868, 302, 1228, 302);

        int supervivientes = 0;
        int francotiradores = 0;

        for (Jugador jugador : jugadores) {
            if (jugador instanceof Francotirador) {
                francotiradores++;
            } else {
                supervivientes++;
            }
        }

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        g2.setColor(MUTED);
        g2.drawString(supervivientes + " supervivientes · " + francotiradores + " francotiradores", 870, 327);
    }

    private void dibujarChat(Graphics2D g2) {
        g2.setColor(Tema.PANEL);
        g2.fillRoundRect(848, 362, 400, 386, 14, 14);

        g2.drawImage(GestorAssets.getIcono("chat"), 868, 377, 26, 26, null);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2.setColor(Tema.CONTORNO);
        g2.drawString("CHAT DEL EQUIPO", 906, 398);

        g2.setColor(new Color(0xD9, 0xCD, 0xAE));
        g2.drawLine(868, 414, 1228, 414);
    }

    private void dibujarPie(Graphics2D g2) {
        g2.setColor(FONDO);
        g2.fillRect(0, 750, 1280, 50);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        g2.setColor(new Color(0xD6, 0xDC, 0xC7));

        if (esDerrotaFinal()) {
            g2.drawString("PARTIDA FINALIZADA · Movimiento deshabilitado. El servidor puede reintentar. Chat disponible.", 42, 779);
        } else {
            g2.drawString("ESPECTADOR · No puedes moverte. El chat sigue activo para ayudar al equipo.", 42, 779);
        }
    }

    private Jugador obtenerJugadorLocal() {
        if (estado == null) {
            return null;
        }

        return estado.getJugadorPorId(idJugadorLocal);
    }

    private List<Jugador> obtenerJugadores() {
        if (estado == null) {
            return new ArrayList<>();
        }

        List<Jugador> jugadores = estado.getJugadores();
        jugadores.sort(Comparator.comparingInt(Jugador::getPlazaVisual));
        return jugadores;
    }

    private String obtenerEstadoJugador(Jugador jugador) {
        if (jugador.getEstado() == EstadoJugador.ESPECTADOR) {
            return "Espectador";
        }

        if (jugador.getEstado() == EstadoJugador.ESCAPADO) {
            return "A salvo";
        }

        if (jugador.getEstado() == EstadoJugador.DESCONECTADO) {
            return "Desconectado";
        }

        if (jugador instanceof Francotirador) {
            return "Apoyo";
        }

        return "Activo";
    }

    private String formatearTiempo() {
        if (estado == null) {
            return "00:00";
        }

        long total = Math.max(0, estado.getTiempoRestanteMs()) / 1000L;
        long minutos = total / 60L;
        long segundos = total % 60L;

        return String.format("%02d:%02d", minutos, segundos);
    }

    private Color obtenerColorPlaza(int plaza) {
        Color[] colores = {
            new Color(0x21, 0x8A, 0xC4),
            new Color(0xE4, 0x63, 0x52),
            new Color(0xE4, 0xB5, 0x34),
            new Color(0x8D, 0x67, 0xC6),
            new Color(0x27, 0xB6, 0xAC),
            new Color(0xE5, 0x8B, 0x32),
            new Color(0xD9, 0x6E, 0xA8),
            new Color(0x42, 0x4F, 0xA0),
            new Color(0xA8, 0xCA, 0x40),
            new Color(0xEA, 0xE1, 0xCD)
        };

        return colores[Math.max(0, Math.min(9, plaza))];
    }

    private void dibujarTextoCentrado(Graphics2D g2, String texto, int x, int y) {
        FontMetrics metricas = g2.getFontMetrics();
        g2.drawString(texto, x - metricas.stringWidth(texto) / 2, y);
    }

    private void dibujarTextoDerecha(Graphics2D g2, String texto, int x, int y) {
        FontMetrics metricas = g2.getFontMetrics();
        g2.drawString(texto, x - metricas.stringWidth(texto), y);
    }
}