package Cliente;

import Models.Mensaje;
import UI.BotonImagen;
import UI.CampoTextoRedondeado;
import UI.GestorAssets;
import UI.PanelDisenoEscalable;
import UI.Tema;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class PanelConexion extends PanelDisenoEscalable {

    public interface AccionesConexion {
        void conectar(String nombre, String rol);
    }

    private static final Color OSCURO = new Color(0x1C, 0x30, 0x27);
    private static final Color CREMA_TEXTO = new Color(0xFF, 0xF4, 0xD8);
    private static final Color TEXTO_SECUNDARIO = new Color(0x66, 0x71, 0x5C);
    private static final Color ERROR = new Color(0x87, 0x3F, 0x2C);

    private final AccionesConexion acciones;
    private final CampoTextoRedondeado campoNombre;
    private final TarjetaRol tarjetaSuperviviente;
    private final TarjetaRol tarjetaFrancotirador;
    private final BotonImagen botonConectar;

    private String rolSeleccionado;
    private String textoEstado;
    private Color colorEstado;

    public PanelConexion(AccionesConexion acciones) {
        super(GestorAssets.getFondoMenu());

        if (acciones == null) {
            throw new IllegalArgumentException("Las acciones de conexion no pueden ser nulas");
        }

        this.acciones = acciones;

        campoNombre = new CampoTextoRedondeado();

        tarjetaSuperviviente = new TarjetaRol(
                "Superviviente",
                "Muévete, escóndete y escapa.",
                GestorAssets.getIcono("exit")
        );

        tarjetaFrancotirador = new TarjetaRol(
                "Francotirador",
                "Cubre al equipo. Máximo 2.",
                GestorAssets.getIcono("sniper")
        );

        botonConectar = new BotonImagen("Conectar al equipo", BotonImagen.PRIMARIO, 580, 70);

        rolSeleccionado = Mensaje.ROL_SUPERVIVIENTE;
        textoEstado = "El servidor debe estar encendido antes de conectar.";
        colorEstado = TEXTO_SECUNDARIO;

        add(campoNombre);
        add(tarjetaSuperviviente);
        add(tarjetaFrancotirador);
        add(botonConectar);

        configurarEventos();
        actualizarSeleccionRol();
    }

    private void configurarEventos() {
        tarjetaSuperviviente.setAccion(() -> {
            rolSeleccionado = Mensaje.ROL_SUPERVIVIENTE;
            actualizarSeleccionRol();
        });

        tarjetaFrancotirador.setAccion(() -> {
            rolSeleccionado = Mensaje.ROL_FRANCOTIRADOR;
            actualizarSeleccionRol();
        });

        botonConectar.addActionListener(evento -> solicitarConexion());
        campoNombre.addActionListener(evento -> solicitarConexion());
    }

    private void solicitarConexion() {
        String nombre = campoNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarError("Escribe un nombre para continuar.");
            campoNombre.requestFocusInWindow();
            return;
        }

        acciones.conectar(nombre, rolSeleccionado);
    }

    private void actualizarSeleccionRol() {
        tarjetaSuperviviente.setSeleccionada(Mensaje.ROL_SUPERVIVIENTE.equals(rolSeleccionado));
        tarjetaFrancotirador.setSeleccionada(Mensaje.ROL_FRANCOTIRADOR.equals(rolSeleccionado));
    }

    @Override
    public void doLayout() {
        Rectangle nombre = escalar(350, 250, 580, 54);
        Rectangle superviviente = escalar(350, 369, 280, 120);
        Rectangle francotirador = escalar(650, 369, 280, 120);
        Rectangle conectar = escalar(350, 530, 580, 70);

        campoNombre.setBounds(nombre);
        tarjetaSuperviviente.setBounds(superviviente);
        tarjetaFrancotirador.setBounds(francotirador);
        botonConectar.setBounds(conectar);

        double escala = getEscalaDiseno();

        int fuenteCampo = Math.max(12, (int) Math.round(22 * escala));
        int fuenteBoton = Math.max(11, (int) Math.round(18 * escala));

        campoNombre.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fuenteCampo));
        botonConectar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fuenteBoton));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D pantalla = (Graphics2D) g.create();

        try {
            pantalla.setColor(new Color(0x1C, 0x30, 0x27, 71));
            pantalla.fillRect(0, 0, getWidth(), getHeight());
        } finally {
            pantalla.dispose();
        }

        Graphics2D g2 = crearGraphicsDiseno(g);

        try {
            dibujarTitulo(g2);
            dibujarPanel(g2);
            dibujarTextos(g2);
        } finally {
            g2.dispose();
        }
    }

    private void dibujarTitulo(Graphics2D g2) {
        Font fuente = new Font(Font.SERIF, Font.BOLD, 46);

        g2.setFont(fuente);
        g2.setColor(CREMA_TEXTO);

        dibujarTextoCentrado(g2, "PREPARA TU ESCAPE", 640, 121);
    }

    private void dibujarPanel(Graphics2D g2) {
        g2.setColor(Tema.PANEL);
        g2.fillRoundRect(310, 179, 660, 530, 48, 48);

        g2.setColor(Tema.CONTORNO);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(310, 179, 660, 530, 48, 48);
    }

    private void dibujarTextos(Graphics2D g2) {
        dibujarTexto(g2, "Nombre del jugador", 350, 233, 18, Tema.CONTORNO, true);
        dibujarTexto(g2, "Elige tu rol", 350, 350, 18, Tema.CONTORNO, true);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        g2.setColor(colorEstado);
        dibujarTextoCentrado(g2, textoEstado, 640, 638);
    }

    private void dibujarTexto(Graphics2D g2, String texto, int x, int y, int tamano, Color color, boolean negrita) {
        g2.setFont(new Font(Font.SANS_SERIF, negrita ? Font.BOLD : Font.PLAIN, tamano));
        g2.setColor(color);
        g2.drawString(texto, x, y);
    }

    private void dibujarTextoCentrado(Graphics2D g2, String texto, int centroX, int y) {
        FontMetrics metricas = g2.getFontMetrics();
        int x = centroX - metricas.stringWidth(texto) / 2;
        g2.drawString(texto, x, y);
    }

    public void setConectando(boolean conectando) {
        campoNombre.setEnabled(!conectando);
        tarjetaSuperviviente.setEnabled(!conectando);
        tarjetaFrancotirador.setEnabled(!conectando);
        botonConectar.setEnabled(!conectando);

        if (conectando) {
            botonConectar.setText("Conectando...");
            textoEstado = "Esperando respuesta del servidor...";
        } else {
            botonConectar.setText("Conectar al equipo");
        }

        repaint();
    }

    public void mostrarError(String mensaje) {
        setConectando(false);
        textoEstado = mensaje == null ? "No se pudo conectar." : mensaje;
        colorEstado = ERROR;
        repaint();
    }

    public void mostrarInformacion(String mensaje) {
        textoEstado = mensaje;
        colorEstado = TEXTO_SECUNDARIO;
        repaint();
    }

    public void prepararNuevoIntento() {
        setConectando(false);
        mostrarInformacion("El servidor debe estar encendido antes de conectar.");
        campoNombre.requestFocusInWindow();
        campoNombre.selectAll();
    }

    private static class TarjetaRol extends javax.swing.JPanel {

        private final String titulo;
        private final String descripcion;
        private final BufferedImage icono;

        private boolean seleccionada;
        private Runnable accion;

        public TarjetaRol(String titulo, String descripcion, BufferedImage icono) {
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.icono = icono;

            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evento) {
                    if (isEnabled() && accion != null) {
                        accion.run();
                    }
                }
            });
        }

        public void setAccion(Runnable accion) {
            this.accion = accion;
        }

        public void setSeleccionada(boolean seleccionada) {
            this.seleccionada = seleccionada;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                double escalaX = getWidth() / 280.0;
                double escalaY = getHeight() / 120.0;

                g2.scale(escalaX, escalaY);

                Color fondo;

                if (!isEnabled()) {
                    fondo = new Color(0xDF, 0xDC, 0xD0);
                } else if (seleccionada) {
                    fondo = new Color(0xE5, 0xEB, 0xCB);
                } else {
                    fondo = new Color(0xF2, 0xE6, 0xC9);
                }

                g2.setColor(fondo);
                g2.fillRoundRect(1, 1, 278, 118, 28, 28);

                g2.setColor(seleccionada ? Tema.JARDIN : new Color(0xCA, 0xBE, 0x9E));
                g2.setStroke(new BasicStroke(seleccionada ? 3f : 2f));
                g2.drawRoundRect(1, 1, 278, 118, 28, 28);

                g2.drawImage(icono, 17, 19, 35, 35, null);

                g2.setColor(Tema.CONTORNO);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
                g2.drawString(titulo, 61, 42);

                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
                g2.drawString(descripcion, 18, 80);
            } finally {
                g2.dispose();
            }
        }
    }
}