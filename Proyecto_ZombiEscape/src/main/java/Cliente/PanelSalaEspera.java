package Cliente;

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
import java.awt.Rectangle;
import java.awt.RenderingHints;
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

public class PanelSalaEspera extends PanelDisenoEscalable {

    public interface AccionesSala {
        void enviarChat(String texto);
    }

    private static final Color FONDO = new Color(0x1C, 0x30, 0x27);
    private static final Color MUTED = new Color(0x66, 0x71, 0x5C);
    private static final Color TARJETA = new Color(0xE4, 0xDB, 0xBA);
    private static final Color BORDE_TARJETA = new Color(0xC8, 0xBC, 0x99);
    private static final Color CAMPO = new Color(0xFF, 0xFC, 0xF2);

    private static final Color[] COLORES = {
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

    private final AccionesSala acciones;
    private final JTextArea areaChat;
    private final JScrollPane scrollChat;
    private final CampoTextoRedondeado campoChat;
    private final BotonImagen botonEnviar;
    private final JLabel contadorChat;

    private EstadoPartida estado;
    private int idJugadorLocal;

    public PanelSalaEspera(AccionesSala acciones) {
        super(GestorAssets.getFondoMenu());

        if (acciones == null) {
            throw new IllegalArgumentException("Las acciones de la sala no pueden ser nulas");
        }

        this.acciones = acciones;
        this.estado = null;
        this.idJugadorLocal = -1;

        areaChat = new JTextArea();
        campoChat = new CampoTextoRedondeado();
        botonEnviar = new BotonImagen("Enviar", BotonImagen.PRIMARIO, 82, 56);
        contadorChat = new JLabel("0/200", SwingConstants.LEFT);
        scrollChat = new JScrollPane(areaChat);

        configurarChat();

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
        scrollChat.setOpaque(false);
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

        if (texto.isEmpty() || texto.length() > 200) {
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
        repaint();
    }

    public void actualizarEstado(EstadoPartida estado) {
        this.estado = estado;
        repaint();
    }

    public void agregarMensaje(String nombre, String texto) {
        areaChat.append(nombre + ": " + texto + System.lineSeparator());
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    }

    @Override
    public void doLayout() {
        Rectangle chat = escalar(870, 424, 356, 230);
        Rectangle campo = escalar(866, 676, 270, 48);
        Rectangle enviar = escalar(1148, 672, 82, 56);
        Rectangle contador = escalar(870, 732, 80, 22);

        scrollChat.setBounds(chat);
        campoChat.setBounds(campo);
        botonEnviar.setBounds(enviar);
        contadorChat.setBounds(contador);

        double escala = getEscalaDiseno();

        areaChat.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(11, (int) Math.round(15 * escala))));
        campoChat.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(11, (int) Math.round(15 * escala))));
        botonEnviar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(10, (int) Math.round(14 * escala))));
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
            dibujarPanelPrincipal(g2);
            dibujarJugadores(g2);
            dibujarEquipo(g2);
            dibujarChat(g2);
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

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g2.setColor(new Color(0xC3, 0xD0, 0xB2));
        g2.drawString("SALA DE ESPERA", 352, 45);

        g2.setColor(Tema.JARDIN);
        g2.fillRoundRect(1070, 23, 178, 30, 30, 30);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(Tema.CONTORNO);
        dibujarTextoCentrado(g2, "ESPERANDO", 1159, 44);
    }

    private void dibujarPanelPrincipal(Graphics2D g2) {
        g2.setColor(Tema.PANEL);
        g2.fillRoundRect(32, 96, 788, 652, 22, 22);

        g2.setFont(new Font(Font.SERIF, Font.BOLD, 30));
        g2.setColor(Tema.CONTORNO);
        g2.drawString("El equipo se está reuniendo", 64, 145);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        g2.setColor(MUTED);
        g2.drawString("El servidor inicia la partida cuando todo esté listo.", 64, 181);

        List<Jugador> jugadores = obtenerJugadores();
        int supervivientes = contarSupervivientes(jugadores);
        int francotiradores = contarFrancotiradores(jugadores);

        g2.setColor(Tema.JARDIN);
        g2.fillRoundRect(64, 665, 724, 30, 30, 30);

        String texto = jugadores.size() + "/10 JUGADORES · " + supervivientes + " SUPERVIVIENTES · " + francotiradores + " FRANCOTIRADORES";

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(Tema.CONTORNO);
        dibujarTextoCentrado(g2, texto, 426, 686);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        g2.setColor(new Color(0xD6, 0xDC, 0xC7));
        g2.drawString("La partida se inicia desde el servidor.", 36, 776);
    }

    private void dibujarJugadores(Graphics2D g2) {
        List<Jugador> jugadores = obtenerJugadores();

        for (int plaza = 0; plaza < 10; plaza++) {
            int x = 60 + (plaza % 5) * 146;
            int y = 222 + (plaza / 5) * 207;

            dibujarTarjetaBase(g2, x, y);

            Jugador jugador = buscarPorPlaza(jugadores, plaza);

            if (jugador == null) {
                dibujarPlazaLibre(g2, x, y);
            } else {
                dibujarJugador(g2, jugador, plaza, x, y);
            }
        }
    }

    private void dibujarTarjetaBase(Graphics2D g2, int x, int y) {
        g2.setColor(TARJETA);
        g2.fillRoundRect(x, y, 132, 182, 20, 20);

        g2.setColor(BORDE_TARJETA);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, 132, 182, 20, 20);
    }

    private void dibujarPlazaLibre(Graphics2D g2, int x, int y) {
        g2.setColor(MUTED);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 38));
        dibujarTextoCentrado(g2, "+", x + 66, y + 87);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        dibujarTextoCentrado(g2, "Libre", x + 66, y + 140);
    }

    private void dibujarJugador(Graphics2D g2, Jugador jugador, int plaza, int x, int y) {
        g2.drawImage(GestorAssets.getJugadorEscalado(plaza, 100, 100), x + 17, y + 5, null);

        g2.setColor(COLORES[plaza]);
        g2.fillOval(x + 10, y + 7, 28, 28);

        g2.setColor(Tema.CONTORNO);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x + 10, y + 7, 28, 28);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g2.setColor(colorNumero(plaza));
        dibujarTextoCentrado(g2, Integer.toString(plaza + 1), x + 24, y + 27);

        String nombre = jugador.getNombre();

        if (jugador.getId() == idJugadorLocal) {
            nombre += " · tú";
        }

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g2.setColor(Tema.CONTORNO);
        dibujarTextoCentrado(g2, nombre, x + 66, y + 139);

        String rol = jugador instanceof Francotirador ? "Francotirador" : "Superviviente";

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g2.setColor(MUTED);
        dibujarTextoCentrado(g2, rol, x + 66, y + 160);
    }

    private void dibujarEquipo(Graphics2D g2) {
        List<Jugador> jugadores = obtenerJugadores();

        int x = 848;
        int y = 94;
        int w = 400;
        int h = 248;

        g2.setColor(Tema.PANEL);
        g2.fillRoundRect(x, y, w, h, 20, 20);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2.setColor(Tema.CONTORNO);
        g2.drawString("EQUIPO", x + 22, y + 34);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g2.setColor(MUTED);
        dibujarTextoDerecha(g2, jugadores.size() + "/10", x + w - 24, y + 34);

        int separacion = jugadores.size() <= 4 ? 36 : 14;
        int tamanoFuente = jugadores.size() <= 4 ? 15 : 11;
        int filaY = y + 66;

        for (Jugador jugador : jugadores) {
            int plaza = jugador.getPlazaVisual();

            g2.setColor(COLORES[plaza]);
            g2.fillOval(x + 21, filaY - 17, 24, 24);

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, jugadores.size() <= 4 ? 12 : 9));
            g2.setColor(colorNumero(plaza));
            dibujarTextoCentrado(g2, Integer.toString(plaza + 1), x + 33, filaY);

            String nombre = jugador.getNombre();

            if (jugador.getId() == idJugadorLocal) {
                nombre += " · tú";
            }

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamanoFuente));
            g2.setColor(Tema.CONTORNO);
            g2.drawString(nombre, x + 57, filaY);

            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, tamanoFuente - 1));
            g2.setColor(MUTED);
            dibujarTextoDerecha(g2, jugador instanceof Francotirador ? "Apoyo" : "Activo", x + w - 24, filaY);

            filaY += separacion;
        }

        g2.setColor(new Color(0xD9, 0xCD, 0xAE));
        g2.drawLine(x + 20, y + h - 40, x + w - 20, y + h - 40);

        int supervivientes = contarSupervivientes(jugadores);
        int francotiradores = contarFrancotiradores(jugadores);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g2.setColor(MUTED);
        g2.drawString(supervivientes + " supervivientes · " + francotiradores + " francotiradores", x + 22, y + h - 15);
    }

    private void dibujarChat(Graphics2D g2) {
        g2.setColor(Tema.PANEL);
        g2.fillRoundRect(848, 362, 400, 386, 20, 20);

        g2.drawImage(GestorAssets.getIcono("chat"), 868, 377, 26, 26, null);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2.setColor(Tema.CONTORNO);
        g2.drawString("CHAT DEL EQUIPO", 906, 398);

        g2.setColor(new Color(0xD9, 0xCD, 0xAE));
        g2.drawLine(868, 414, 1228, 414);
    }

    private List<Jugador> obtenerJugadores() {
        if (estado == null) {
            return new ArrayList<>();
        }

        List<Jugador> jugadores = estado.getJugadores();
        jugadores.sort(Comparator.comparingInt(Jugador::getPlazaVisual));
        return jugadores;
    }

    private Jugador buscarPorPlaza(List<Jugador> jugadores, int plaza) {
        for (Jugador jugador : jugadores) {
            if (jugador.getPlazaVisual() == plaza) {
                return jugador;
            }
        }

        return null;
    }

    private int contarSupervivientes(List<Jugador> jugadores) {
        int cantidad = 0;

        for (Jugador jugador : jugadores) {
            if (!(jugador instanceof Francotirador)) {
                cantidad++;
            }
        }

        return cantidad;
    }

    private int contarFrancotiradores(List<Jugador> jugadores) {
        int cantidad = 0;

        for (Jugador jugador : jugadores) {
            if (jugador instanceof Francotirador) {
                cantidad++;
            }
        }

        return cantidad;
    }

    private Color colorNumero(int plaza) {
        if (plaza == 0 || plaza == 3 || plaza == 7) {
            return Color.WHITE;
        }

        return Tema.CONTORNO;
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