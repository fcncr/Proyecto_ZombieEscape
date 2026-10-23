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
import java.awt.Rectangle;
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

public class PanelPartidaFrancotirador extends PanelDisenoEscalable {

    public interface AccionesPartida {
        void enviarChat(String texto);
    }

    private static final Color FONDO = new Color(0x1C, 0x30, 0x27);
    private static final Color MUTED = new Color(0x66, 0x71, 0x5C);
    private static final Color PANEL_CAMARA = new Color(0x10, 0x1F, 0x19);

    private final AccionesPartida acciones;
    private final PanelMapa panelMapa;
    private final JTextArea areaChat;
    private final JScrollPane scrollChat;
    private final CampoTextoRedondeado campoChat;
    private final BotonImagen botonEnviar;
    private final JLabel contadorChat;

    private EstadoPartida estado;
    private int idJugadorLocal;

    public PanelPartidaFrancotirador(Cliente cliente, AccionesPartida acciones) {
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
        botonEnviar = new BotonImagen("Enviar", BotonImagen.PRIMARIO, 82, 56);
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

    @Override
    public void doLayout() {
        panelMapa.setBounds(escalar(147, 198, 522, 522));
        scrollChat.setBounds(escalar(870, 529, 356, 126));
        campoChat.setBounds(escalar(866, 671, 270, 48));
        botonEnviar.setBounds(escalar(1148, 667, 82, 56));
        contadorChat.setBounds(escalar(870, 729, 80, 20));

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
            dibujarCamara(g2);
            dibujarCoordenadas(g2);
            dibujarEquipo(g2);
            dibujarBalas(g2);
            dibujarChat(g2);
            dibujarControles(g2);
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
        g2.drawString("FRANCOTIRADOR", 352, 45);

        g2.setColor(Tema.JARDIN);
        g2.fillRoundRect(1070, 23, 178, 30, 30, 30);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(Tema.CONTORNO);
        dibujarTextoCentrado(g2, "EN PARTIDA", 1159, 44);
    }

    private void dibujarHUD(Graphics2D g2) {
        g2.setColor(new Color(0xF0, 0xDE, 0xB1));
        g2.fillRoundRect(32, 90, 788, 54, 12, 12);

        Francotirador local = obtenerFrancotiradorLocal();

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
        g2.drawString(local.getNombre() + " · apoyo", 84, 124);

        g2.drawImage(GestorAssets.getIcono("timer"), 268, 102, 28, 28, null);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        g2.drawString(formatearTiempo(), 308, 124);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g2.drawString("ESCENARIO " + String.format("%02d", estado.getNumeroEscenario()) + " / " + estado.getCantidadEscenarios(), 442, 124);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g2.drawString(local.getPuntos() + " pts", 682, 124);
    }

    private void dibujarCamara(Graphics2D g2) {
        g2.setColor(PANEL_CAMARA);
        g2.fillRoundRect(32, 157, 788, 581, 15, 15);
    }

    private void dibujarCoordenadas(Graphics2D g2) {
        Francotirador local = obtenerFrancotiradorLocal();

        if (local == null || local.getCentroVista() == null) {
            return;
        }

        int radio = local.getRadioVista();
        int filaInicial = local.getCentroVista().getFila() - radio;
        int columnaInicial = local.getCentroVista().getColumna() - radio;

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g2.setColor(Tema.PANEL);

        for (int i = 0; i < 9; i++) {
            int columna = columnaInicial + i;
            int fila = filaInicial + i;

            dibujarTextoCentrado(g2, Integer.toString(columna), 176 + i * 58, 183);
            dibujarTextoDerecha(g2, Integer.toString(fila), 132, 239 + i * 58);
        }
    }

    private void dibujarEquipo(Graphics2D g2) {
        List<Jugador> jugadores = obtenerJugadores();

        int alto = 104 + jugadores.size() * 36;

        g2.setColor(Tema.PANEL);
        g2.fillRoundRect(848, 94, 400, alto, 14, 14);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2.setColor(Tema.CONTORNO);
        g2.drawString("EQUIPO", 870, 128);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g2.setColor(MUTED);
        dibujarTextoDerecha(g2, jugadores.size() + "/10", 1224, 128);

        int y = 160;

        for (Jugador jugador : jugadores) {
            int plaza = Math.max(0, Math.min(9, jugador.getPlazaVisual()));

            g2.setColor(obtenerColorPlaza(plaza));
            g2.fillOval(869, y - 17, 24, 24);

            g2.setColor(Tema.CONTORNO);
            g2.drawOval(869, y - 17, 24, 24);

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            g2.setColor(plaza == 0 || plaza == 3 || plaza == 7 ? Color.WHITE : Tema.CONTORNO);
            dibujarTextoCentrado(g2, Integer.toString(plaza + 1), 881, y);

            String nombre = jugador.getNombre();

            if (jugador.getId() == idJugadorLocal) {
                nombre += " · tú";
            }

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            g2.setColor(Tema.CONTORNO);
            g2.drawString(nombre, 905, y);

            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            g2.setColor(MUTED);
            dibujarTextoDerecha(g2, jugador instanceof Francotirador ? "Apoyo" : estadoJugador(jugador), 1224, y);

            y += 36;
        }

        int linea = 94 + alto - 40;

        g2.setColor(new Color(0xD9, 0xCD, 0xAE));
        g2.drawLine(868, linea, 1228, linea);

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
        g2.drawString(supervivientes + " supervivientes · " + francotiradores + " francotiradores", 870, 94 + alto - 15);
    }

    private void dibujarBalas(Graphics2D g2) {
        Francotirador local = obtenerFrancotiradorLocal();

        if (local == null) {
            return;
        }

        g2.setColor(new Color(0xF0, 0xDE, 0xB1));
        g2.fillRoundRect(848, 350, 400, 101, 14, 14);

        g2.drawImage(GestorAssets.getIcono("bullet"), 866, 370, 37, 37, null);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g2.setColor(Tema.CONTORNO);
        g2.drawString("BALAS  " + local.getBalas() + " / " + Francotirador.BALAS_POR_ESCENARIO, 918, 389);

        int fallo = (int) Math.round(Francotirador.PROBABILIDAD_FALLO * 100);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        g2.drawString(fallo + " % de fallo · sin recarga manual", 918, 422);
    }

    private void dibujarChat(Graphics2D g2) {
        g2.setColor(Tema.PANEL);
        g2.fillRoundRect(848, 468, 400, 280, 14, 14);

        g2.drawImage(GestorAssets.getIcono("chat"), 868, 485, 28, 28, null);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2.setColor(Tema.CONTORNO);
        g2.drawString("CHAT DEL EQUIPO", 906, 507);

        g2.setColor(new Color(0xD9, 0xCD, 0xAE));
        g2.drawLine(868, 520, 1228, 520);
    }

    private void dibujarControles(Graphics2D g2) {
        Francotirador local = obtenerFrancotiradorLocal();

        String centro = "--";

        if (local != null && local.getCentroVista() != null) {
            centro = "F" + local.getCentroVista().getFila() + ", C" + local.getCentroVista().getColumna();
        }

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        g2.setColor(new Color(0xD6, 0xDC, 0xC7));
        g2.drawString("Flechas: mover mira  |  Enter: disparar  |  Centro: " + centro, 42, 776);
    }

    private Francotirador obtenerFrancotiradorLocal() {
        if (estado == null) {
            return null;
        }

        Jugador jugador = estado.getJugadorPorId(idJugadorLocal);

        if (jugador instanceof Francotirador) {
            return (Francotirador) jugador;
        }

        return null;
    }

    private List<Jugador> obtenerJugadores() {
        if (estado == null) {
            return new ArrayList<>();
        }

        List<Jugador> jugadores = estado.getJugadores();
        jugadores.sort(Comparator.comparingInt(Jugador::getPlazaVisual));
        return jugadores;
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

    private String estadoJugador(Jugador jugador) {
        switch (jugador.getEstado()) {
            case ESCAPADO:
                return "A salvo";
            case ESPECTADOR:
                return "Espectador";
            case DESCONECTADO:
                return "Desconectado";
            default:
                return "Activo";
        }
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