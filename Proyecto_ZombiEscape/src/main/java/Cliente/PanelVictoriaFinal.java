package Cliente;

import Models.EstadoPartida;
import Models.Francotirador;
import Models.Jugador;
import UI.BotonImagen;
import UI.CampoTextoRedondeado;
import UI.GestorAssets;
import UI.PanelDisenoEscalable;
import UI.Tema;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PanelVictoriaFinal extends PanelDisenoEscalable {

    public interface Acciones {
        void enviarChat(String texto);
    }

    private static final Color FONDO = new Color(0x1C, 0x30, 0x27);
    private static final Color MUTED = new Color(0x66, 0x71, 0x5C);
    private static final Color CABECERA_TABLA = new Color(0xD7, 0xE0, 0xB3);
    private static final Color LINEA = new Color(0xD9, 0xCD, 0xAE);

    private final Acciones acciones;
    private final JTextArea areaChat;
    private final JScrollPane scrollChat;
    private final CampoTextoRedondeado campoChat;
    private final BotonImagen botonEnviar;
    private final JLabel contadorChat;

    private EstadoPartida estado;
    private int idJugadorLocal;

    public PanelVictoriaFinal(Acciones acciones) {
        super(GestorAssets.getFondoMenu());

        if (acciones == null) {
            throw new IllegalArgumentException("Las acciones son obligatorias");
        }

        this.acciones = acciones;
        this.estado = null;
        this.idJugadorLocal = -1;

        areaChat = new JTextArea();
        scrollChat = new JScrollPane(areaChat);
        campoChat = new CampoTextoRedondeado();
        botonEnviar = new BotonImagen("Enviar", BotonImagen.PRIMARIO, 78, 56);
        contadorChat = new JLabel("0/200", SwingConstants.LEFT);

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
            dibujarRanking(g2);
            dibujarEquipo(g2);
            dibujarChat(g2);
            dibujarPie(g2);
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
        g2.drawString("RESULTADO FINAL", 352, 45);

        g2.setColor(Tema.JARDIN);
        g2.fillRoundRect(1070, 23, 178, 30, 30, 30);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(Tema.CONTORNO);
        dibujarTextoCentrado(g2, "VICTORIA", 1159, 44);
    }

    private void dibujarRanking(Graphics2D g2) {
        g2.setColor(Tema.PANEL);
        g2.fillRoundRect(32, 96, 788, 652, 20, 20);

        g2.drawImage(GestorAssets.getIcono("check"), 371, 121, 80, 80, null);

        g2.setFont(new Font(Font.SERIF, Font.BOLD, 31));
        g2.setColor(Tema.CONTORNO);
        dibujarTextoCentrado(g2, "¡TODO EL EQUIPO A SALVO!", 426, 247);

        int cantidadEscenarios = estado == null ? 10 : estado.getCantidadEscenarios();

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 19));
        g2.setColor(MUTED);
        dibujarTextoCentrado(g2, "Campaña completada · " + cantidadEscenarios + "/" + cantidadEscenarios + " escenarios", 426, 283);

        g2.setColor(CABECERA_TABLA);
        g2.fillRoundRect(60, 320, 732, 43, 9, 9);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g2.setColor(Tema.CONTORNO);
        g2.drawString("PUESTO", 80, 347);
        g2.drawString("JUGADOR", 190, 347);
        g2.drawString("ROL", 451, 347);
        dibujarTextoDerecha(g2, "PUNTOS", 758, 347);

        List<Jugador> ranking = obtenerRanking();

        if (ranking.isEmpty()) {
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
            g2.setColor(MUTED);
            dibujarTextoCentrado(g2, "Sin resultados disponibles", 426, 430);
            return;
        }

        int cantidad = ranking.size();
        int alturaDisponible = 300;
        int altoFila = Math.min(64, alturaDisponible / cantidad);
        altoFila = Math.max(28, altoFila);

        int fuenteNombre = cantidad <= 5 ? 19 : cantidad <= 7 ? 16 : 14;
        int fuenteRol = cantidad <= 5 ? 16 : 13;
        int fuentePuntos = cantidad <= 5 ? 22 : 17;
        int centroY = 398;

        for (int i = 0; i < cantidad; i++) {
            Jugador jugador = ranking.get(i);
            int y = centroY + i * altoFila;
            int plaza = Math.max(0, Math.min(9, jugador.getPlazaVisual()));

            g2.setColor(i == 0 ? Tema.SALIDA : new Color(0xE4, 0xD7, 0xAE));
            g2.fillOval(89, y - 17, 34, 34);

            g2.setColor(Tema.CONTORNO);
            g2.drawOval(89, y - 17, 34, 34);

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            g2.setColor(Tema.CONTORNO);
            dibujarTextoCentrado(g2, Integer.toString(i + 1), 106, y + 6);

            g2.setColor(obtenerColorPlaza(plaza));
            g2.fillOval(192, y - 20, 24, 24);

            g2.setColor(Tema.CONTORNO);
            g2.drawOval(192, y - 20, 24, 24);

            String nombre = jugador.getNombre();

            if (jugador.getId() == idJugadorLocal) {
                nombre += " · tú";
            }

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fuenteNombre));
            g2.setColor(Tema.CONTORNO);
            g2.drawString(nombre, 230, y);

            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fuenteRol));
            g2.drawString(jugador instanceof Francotirador ? "Apoyo" : "Superviviente", 451, y);

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fuentePuntos));
            dibujarTextoDerecha(g2, Integer.toString(jugador.getPuntos()), 758, y);

            g2.setColor(LINEA);
            g2.drawLine(70, y + Math.max(12, altoFila / 3), 782, y + Math.max(12, altoFila / 3));
        }
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

        int cantidad = jugadores.size();
        int separacion = cantidad <= 4 ? 36 : cantidad <= 7 ? 25 : 20;
        int fuente = cantidad <= 4 ? 16 : cantidad <= 7 ? 13 : 11;
        int y = 160;

        for (Jugador jugador : jugadores) {
            int plaza = Math.max(0, Math.min(9, jugador.getPlazaVisual()));

            g2.setColor(obtenerColorPlaza(plaza));
            g2.fillOval(869, y - 17, 24, 24);

            g2.setColor(Tema.CONTORNO);
            g2.drawOval(869, y - 17, 24, 24);

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(8, fuente - 4)));
            g2.setColor(plaza == 0 || plaza == 3 || plaza == 7 ? Color.WHITE : Tema.CONTORNO);
            dibujarTextoCentrado(g2, Integer.toString(plaza + 1), 881, y);

            String nombre = jugador.getNombre();

            if (jugador.getId() == idJugadorLocal) {
                nombre += " · tú";
            }

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fuente));
            g2.setColor(Tema.CONTORNO);
            g2.drawString(nombre, 905, y);

            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(9, fuente - 2)));
            g2.setColor(MUTED);
            dibujarTextoDerecha(g2, jugador instanceof Francotirador ? "Apoyo" : "A salvo", 1224, y);

            y += separacion;
        }

        g2.setColor(LINEA);
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

        g2.setColor(LINEA);
        g2.drawLine(868, 414, 1228, 414);
    }

    private void dibujarPie(Graphics2D g2) {
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        g2.setColor(new Color(0xD6, 0xDC, 0xC7));
        g2.drawString("El chat sigue disponible. Para otra campaña, el anfitrión abre una nueva sesión.", 36, 776);
    }

    private List<Jugador> obtenerRanking() {
        if (estado == null) {
            return new ArrayList<>();
        }

        return estado.getRanking();
    }

    private List<Jugador> obtenerJugadores() {
        if (estado == null) {
            return new ArrayList<>();
        }

        return estado.getJugadores();
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