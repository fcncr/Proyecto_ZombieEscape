package Servidor;

import Models.EstadoJugador;
import Models.EstadoPartida;
import Models.Francotirador;
import Models.GestorEscenarios;
import Models.Jugador;
import UI.BotonImagen;
import UI.GestorAssets;
import UI.PanelDisenoEscalable;
import UI.Tema;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class ServidorFrame extends JFrame {

    private final ChatServer servidor;
    private final PanelServidor panelServidor;
    private final Timer temporizador;

    private volatile boolean iniciandoServidor;
    private String ultimaFase;
    private int ultimosRegistrados;
    private int ultimoEscenario;
    private int escenariosLocales;

    public ServidorFrame() {
        servidor = new ChatServer();
        panelServidor = new PanelServidor(this::encenderServidor, this::iniciarPartida, this::reintentarPartida, this::gestionarEscenarios, this::cerrarServidor);

        iniciandoServidor = false;
        ultimaFase = "APAGADO";
        ultimosRegistrados = 0;
        ultimoEscenario = 0;
        escenariosLocales = contarEscenariosLocales();

        configurarVentana();

        panelServidor.agregarEvento("Listo para abrir servidor.");
        panelServidor.agregarEvento(escenariosLocales + " escenarios disponibles.");

        temporizador = new Timer(500, evento -> actualizarDesdeServidor());
        temporizador.start();

        actualizarDesdeServidor();
    }

    private void configurarVentana() {
        setTitle("ZombiEscape - Servidor");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        panelServidor.setPreferredSize(new java.awt.Dimension(1024, 640));
        setContentPane(panelServidor);

        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                cerrarVentana();
            }
        });
    }

    private int contarEscenariosLocales() {
        try {
            GestorEscenarios gestor = new GestorEscenarios(ChatServer.DIRECTORIO_ESCENARIOS);
            return gestor.cantidad();
        } catch (IOException | IllegalArgumentException error) {
            return 0;
        }
    }

    private void encenderServidor() {
        if (servidor.isEjecutando() || iniciandoServidor) {
            return;
        }

        iniciandoServidor = true;
        panelServidor.agregarEvento("Abriendo servidor en localhost:8084...");
        actualizarDesdeServidor();

        Thread hilo = new Thread(() -> {
            try {
                servidor.runServer();

                SwingUtilities.invokeLater(() -> {
                    escenariosLocales = servidor.getCantidadEscenarios();
                    panelServidor.agregarEvento("Servidor activo · " + escenariosLocales + " escenarios cargados.");
                    actualizarDesdeServidor();
                });
            } catch (IOException | IllegalStateException error) {
                SwingUtilities.invokeLater(() -> {
                    panelServidor.agregarEvento("Error: " + error.getMessage());
                    JOptionPane.showMessageDialog(this, error.getMessage(), "No se pudo abrir el servidor", JOptionPane.ERROR_MESSAGE);
                });
            } finally {
                iniciandoServidor = false;
                SwingUtilities.invokeLater(this::actualizarDesdeServidor);
            }
        });

        hilo.setName("InicioServidor");
        hilo.start();
    }

    private void iniciarPartida() {
        if (!servidor.isEjecutando()) {
            return;
        }

        Thread hilo = new Thread(() -> {
            boolean iniciada = servidor.iniciarPartida();

            SwingUtilities.invokeLater(() -> {
                if (iniciada) {
                    panelServidor.agregarEvento("Partida iniciada.");
                } else {
                    panelServidor.agregarEvento("No se pudo iniciar la partida.");
                }

                actualizarDesdeServidor();
            });
        });

        hilo.setName("InicioPartida");
        hilo.start();
    }

    private void reintentarPartida() {
        if (!servidor.isEjecutando()) {
            return;
        }

        Thread hilo = new Thread(() -> {
            boolean reintentada = servidor.reintentarPartida();

            SwingUtilities.invokeLater(() -> {
                if (reintentada) {
                    panelServidor.agregarEvento("Escenario reiniciado.");
                } else {
                    panelServidor.agregarEvento("No se pudo reintentar el escenario.");
                }

                actualizarDesdeServidor();
            });
        });

        hilo.setName("ReintentoPartida");
        hilo.start();
    }

    private void gestionarEscenarios() {
        if (servidor.isEjecutando() || iniciandoServidor) {
            JOptionPane.showMessageDialog(this, "Apaga el servidor antes de modificar los escenarios.", "Escenarios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            DialogoEscenarios dialogo = new DialogoEscenarios(this);
            dialogo.setVisible(true);

            escenariosLocales = contarEscenariosLocales();
            panelServidor.agregarEvento(escenariosLocales + " escenarios disponibles.");
            actualizarDesdeServidor();

        } catch (IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Escenarios", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarServidor() {
        if (!servidor.isEjecutando()) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this, "Los clientes conectados se desconectarán.", "Cerrar servidor", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        servidor.cerrar();
        panelServidor.agregarEvento("Servidor cerrado.");

        ultimaFase = "APAGADO";
        ultimosRegistrados = 0;
        ultimoEscenario = 0;

        actualizarDesdeServidor();
    }

    private void cerrarVentana() {
        if (servidor.isEjecutando()) {
            int respuesta = JOptionPane.showConfirmDialog(this, "Los clientes conectados se desconectarán.", "Salir", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            servidor.cerrar();
        }

        temporizador.stop();
        dispose();
    }

    private void actualizarDesdeServidor() {
        boolean encendido = servidor.isEjecutando();

        EstadoPartida estado = encendido ? servidor.getEstadoActual() : null;
        List<Jugador> jugadores = estado == null ? new ArrayList<>() : estado.getJugadores();

        String fase = encendido && estado != null ? estado.getFase() : "APAGADO";
        int cantidadEscenarios = encendido && estado != null ? estado.getCantidadEscenarios() : escenariosLocales;
        int numeroEscenario = encendido && estado != null ? estado.getNumeroEscenario() : 0;
        String nombreEscenario = encendido ? servidor.getNombreEscenarioActual() : "";

        detectarCambios(encendido, fase, jugadores.size(), numeroEscenario);

        panelServidor.actualizar(encendido, iniciandoServidor, fase, jugadores, cantidadEscenarios, numeroEscenario, nombreEscenario);
    }

    private void detectarCambios(boolean encendido, String fase, int registrados, int numeroEscenario) {
        if (!encendido) {
            ultimaFase = "APAGADO";
            ultimosRegistrados = 0;
            ultimoEscenario = 0;
            return;
        }

        if (registrados != ultimosRegistrados) {
            panelServidor.agregarEvento(registrados + " jugadores registrados.");
            ultimosRegistrados = registrados;
        }

        if (!fase.equals(ultimaFase)) {
            if (EstadoPartida.DERROTA.equals(fase)) {
                panelServidor.agregarEvento("Derrota confirmada. Reintento disponible.");
            } else if (EstadoPartida.VICTORIA.equals(fase)) {
                panelServidor.agregarEvento("Campaña completada.");
            }

            ultimaFase = fase;
        }

        if (numeroEscenario > 0 && numeroEscenario != ultimoEscenario) {
            panelServidor.agregarEvento("Escenario " + numeroEscenario + " activo.");
            ultimoEscenario = numeroEscenario;
        }
    }

    private static class PanelServidor extends PanelDisenoEscalable {

        private static final Color FONDO = new Color(0x1C, 0x30, 0x27);
        private static final Color MUTED = new Color(0x66, 0x71, 0x5C);
        private static final Color TARJETA = new Color(0xE6, 0xD9, 0xB1);

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

        private final BotonImagen botonEncender;
        private final BotonImagen botonIniciar;
        private final BotonImagen botonReintentar;
        private final BotonImagen botonEscenarios;
        private final BotonImagen botonCerrar;

        private final List<String> eventos;

        private boolean encendido;
        private boolean iniciando;
        private String fase;
        private List<Jugador> jugadores;
        private int cantidadEscenarios;
        private int numeroEscenario;
        private String nombreEscenario;

        public PanelServidor(Runnable encender, Runnable iniciar, Runnable reintentar, Runnable escenarios, Runnable cerrar) {
            super(GestorAssets.getFondoMenu());

            setLayout(null);

            botonEncender = new BotonImagen("Encender servidor", BotonImagen.PRIMARIO, 356, 56);
            botonIniciar = new BotonImagen("Iniciar partida", BotonImagen.PRIMARIO, 356, 56);
            botonReintentar = new BotonImagen("Reintentar escenario", BotonImagen.SECUNDARIO, 356, 56);
            botonEscenarios = new BotonImagen("Gestionar escenarios", BotonImagen.SECUNDARIO, 356, 56);
            botonCerrar = new BotonImagen("Cerrar servidor", BotonImagen.PELIGRO, 356, 56);

            eventos = new ArrayList<>();
            jugadores = new ArrayList<>();
            fase = "APAGADO";
            nombreEscenario = "";

            botonEncender.addActionListener(evento -> encender.run());
            botonIniciar.addActionListener(evento -> iniciar.run());
            botonReintentar.addActionListener(evento -> reintentar.run());
            botonEscenarios.addActionListener(evento -> escenarios.run());
            botonCerrar.addActionListener(evento -> cerrar.run());

            add(botonEncender);
            add(botonIniciar);
            add(botonReintentar);
            add(botonEscenarios);
            add(botonCerrar);
        }

        public void actualizar(boolean encendido, boolean iniciando, String fase, List<Jugador> jugadores, int cantidadEscenarios, int numeroEscenario, String nombreEscenario) {
            this.encendido = encendido;
            this.iniciando = iniciando;
            this.fase = fase;
            this.jugadores = new ArrayList<>(jugadores);
            this.cantidadEscenarios = cantidadEscenarios;
            this.numeroEscenario = numeroEscenario;
            this.nombreEscenario = nombreEscenario == null ? "" : nombreEscenario;

            int supervivientes = 0;

            for (Jugador jugador : jugadores) {
                if (!(jugador instanceof Francotirador) && jugador.getEstado() != EstadoJugador.DESCONECTADO) {
                    supervivientes++;
                }
            }

            boolean espera = EstadoPartida.ESPERANDO.equals(fase);
            boolean derrota = EstadoPartida.DERROTA.equals(fase);

            botonEncender.setEnabled(!encendido && !iniciando);
            botonIniciar.setEnabled(encendido && espera && jugadores.size() >= Partida.MIN_JUGADORES && supervivientes > 0);
            botonReintentar.setEnabled(encendido && derrota);
            botonEscenarios.setEnabled(!encendido && !iniciando);
            botonCerrar.setEnabled(encendido);

            repaint();
        }

        public void agregarEvento(String texto) {
            if (texto == null || texto.isBlank()) {
                return;
            }

            eventos.add(texto);

            while (eventos.size() > 20) {
                eventos.remove(0);
            }

            repaint();
        }

        @Override
        public void doLayout() {
            botonEncender.setBounds(escalar(870, 163, 356, 56));
            botonIniciar.setBounds(escalar(870, 235, 356, 56));
            botonReintentar.setBounds(escalar(870, 307, 356, 56));
            botonEscenarios.setBounds(escalar(870, 379, 356, 56));
            botonCerrar.setBounds(escalar(870, 451, 356, 56));

            double escala = getEscalaDiseno();
            int fuente = Math.max(11, (int) Math.round(18 * escala));

            botonEncender.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fuente));
            botonIniciar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fuente));
            botonReintentar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fuente));
            botonEscenarios.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fuente));
            botonCerrar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fuente));
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
                dibujarMetricas(g2);
                dibujarJugadores(g2);
                dibujarEventos(g2);
                dibujarAcciones(g2);
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
            g2.drawString("PANEL DEL SERVIDOR", 352, 45);

            g2.setColor(Tema.JARDIN);
            g2.fillRoundRect(1070, 23, 178, 30, 30, 30);

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g2.setColor(Tema.CONTORNO);
            dibujarTextoCentrado(g2, obtenerTextoEstadoSuperior(), 1159, 44);
        }

        private void dibujarPanelPrincipal(Graphics2D g2) {
            g2.setColor(Tema.PANEL);
            g2.fillRoundRect(32, 96, 788, 652, 14, 14);

            g2.setFont(new Font(Font.SERIF, Font.BOLD, 30));
            g2.setColor(Tema.CONTORNO);
            g2.drawString("Centro de control", 60, 139);

            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
            g2.setColor(MUTED);
            g2.drawString("localhost:8084  ·  Solo esta computadora", 60, 171);
        }

        private void dibujarMetricas(Graphics2D g2) {
            int francotiradores = 0;

            for (Jugador jugador : jugadores) {
                if (jugador instanceof Francotirador && jugador.getEstado() != EstadoJugador.DESCONECTADO) {
                    francotiradores++;
                }
            }

            String[] etiquetas = {"JUGADORES", "APOYO", "ESCENARIOS", "ESTADO"};
            String[] valores = {
                (encendido ? jugadores.size() : 0) + "/10",
                (encendido ? francotiradores : 0) + "/2",
                Integer.toString(cantidadEscenarios),
                obtenerTextoEstado()
            };

            for (int i = 0; i < 4; i++) {
                int x = 60 + i * 184;

                g2.setColor(TARJETA);
                g2.fillRoundRect(x, 198, 166, 85, 12, 12);

                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                g2.setColor(MUTED);
                g2.drawString(etiquetas[i], x + 14, 224);

                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
                g2.setColor(Tema.CONTORNO);
                g2.drawString(valores[i], x + 14, 262);
            }
        }

        private void dibujarJugadores(Graphics2D g2) {
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
            g2.setColor(Tema.CONTORNO);
            g2.drawString("JUGADORES REGISTRADOS", 60, 323);

            if (!encendido || jugadores.isEmpty()) {
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
                g2.setColor(MUTED);
                g2.drawString("Sin jugadores registrados.", 60, 371);
                return;
            }

            int cantidad = jugadores.size();
            int separacion = cantidad <= 4 ? 49 : cantidad <= 7 ? 28 : 20;
            int fuenteNombre = cantidad <= 4 ? 17 : cantidad <= 7 ? 14 : 12;
            int fuenteDetalle = cantidad <= 4 ? 16 : cantidad <= 7 ? 13 : 11;
            int y = 363;

            for (Jugador jugador : jugadores) {
                int plaza = Math.max(0, Math.min(9, jugador.getPlazaVisual()));

                g2.setColor(COLORES[plaza]);
                g2.fillOval(64, y - 19, 26, 26);

                g2.setColor(Tema.CONTORNO);
                g2.drawOval(64, y - 19, 26, 26);

                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(9, fuenteDetalle - 3)));
                g2.setColor(plaza == 0 || plaza == 3 || plaza == 7 ? Color.WHITE : Tema.CONTORNO);
                dibujarTextoCentrado(g2, Integer.toString(plaza + 1), 77, y);

                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fuenteNombre));
                g2.setColor(Tema.CONTORNO);
                g2.drawString(jugador.getNombre(), 104, y);

                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fuenteDetalle));
                g2.drawString(jugador instanceof Francotirador ? "Francotirador" : "Superviviente", 300, y);

                g2.setColor(MUTED);
                g2.drawString(obtenerEstadoJugador(jugador), 602, y);

                g2.setColor(new Color(0xD9, 0xCD, 0xAE));
                g2.drawLine(60, y + 18, 790, y + 18);

                y += separacion;
            }
        }

        private void dibujarEventos(Graphics2D g2) {
            g2.setColor(FONDO);
            g2.fillRoundRect(60, 570, 730, 145, 12, 12);

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g2.setColor(new Color(0xC4, 0xD0, 0x98));
            g2.drawString("REGISTRO DE EVENTOS", 80, 602);

            int inicio = Math.max(0, eventos.size() - 4);
            int y = 635;

            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            g2.setColor(Tema.PANEL);

            for (int i = inicio; i < eventos.size(); i++) {
                g2.drawString(eventos.get(i), 80, y);
                y += 23;
            }
        }

        private void dibujarAcciones(Graphics2D g2) {
            g2.setColor(Tema.PANEL);
            g2.fillRoundRect(848, 96, 400, 652, 14, 14);

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
            g2.setColor(Tema.CONTORNO);
            g2.drawString("ACCIONES", 872, 139);

            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            g2.setColor(MUTED);

            g2.drawString("Para iniciar: 2–10 jugadores", 876, 552);
            g2.drawString("y al menos un superviviente.", 876, 582);
            g2.drawString("Estado actualizado automáticamente.", 876, 612);
            g2.drawString("Reintentar solo después de derrota.", 876, 642);

            if (encendido && numeroEscenario > 0) {
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                g2.setColor(Tema.CONTORNO);
                g2.drawString("Escenario " + numeroEscenario + ": " + nombreEscenario, 876, 690);
            }
        }

        private void dibujarPie(Graphics2D g2) {
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            g2.setColor(new Color(0xD6, 0xDC, 0xC7));
            g2.drawString("El servidor controla reglas, escenarios y sincronización. No hay pausa ni salto de nivel.", 36, 776);
        }

        private String obtenerTextoEstadoSuperior() {
            if (iniciando) {
                return "ABRIENDO";
            }

            if (!encendido) {
                return "APAGADO";
            }

            if (EstadoPartida.ESPERANDO.equals(fase)) {
                return "EN ESPERA";
            }

            if (EstadoPartida.JUGANDO.equals(fase)) {
                return "EN PARTIDA";
            }

            if (EstadoPartida.DERROTA.equals(fase)) {
                return "DERROTA";
            }

            if (EstadoPartida.VICTORIA.equals(fase)) {
                return "VICTORIA";
            }

            return fase;
        }

        private String obtenerTextoEstado() {
            if (!encendido) {
                return "Apagado";
            }

            if (EstadoPartida.ESPERANDO.equals(fase)) {
                return "Espera";
            }

            if (EstadoPartida.JUGANDO.equals(fase)) {
                return "Jugando";
            }

            if (EstadoPartida.DERROTA.equals(fase)) {
                return "Derrota";
            }

            if (EstadoPartida.VICTORIA.equals(fase)) {
                return "Victoria";
            }

            return fase;
        }

        private String obtenerEstadoJugador(Jugador jugador) {
            if (!encendido || EstadoPartida.ESPERANDO.equals(fase)) {
                return "Conectado";
            }

            if (jugador.getEstado() == EstadoJugador.ESCAPADO) {
                return "A salvo";
            }

            if (jugador.getEstado() == EstadoJugador.ESPECTADOR) {
                return "Espectador";
            }

            if (jugador.getEstado() == EstadoJugador.DESCONECTADO) {
                return "Desconectado";
            }

            return "Activo";
        }

        private void dibujarTextoCentrado(Graphics2D g2, String texto, int x, int y) {
            FontMetrics metricas = g2.getFontMetrics();
            g2.drawString(texto, x - metricas.stringWidth(texto) / 2, y);
        }
    }
}