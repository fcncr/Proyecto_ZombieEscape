package com.mycompany.proyecto_zombiescape;

import Cliente.ClienteMain;
import Servidor.ServidorMain;
import UI.BotonImagen;
import UI.GestorAssets;
import UI.PanelFondo;
import UI.PanelRedondeado;
import UI.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class SelectorInicioFrame extends JFrame {

    private final BotonImagen botonServidor;
    private final BotonImagen botonCliente;

    private volatile boolean servidorIniciado;

    public SelectorInicioFrame() {
        servidorIniciado = false;

        botonServidor = new BotonImagen(
                "Abrir servidor",
                BotonImagen.PRIMARIO
        );

        botonCliente = new BotonImagen(
                "Abrir cliente",
                BotonImagen.SECUNDARIO
        );

        configurarVentana();
        construirInterfaz();
        configurarEventos();
    }

    private void configurarVentana() {
        setTitle("Zombi Escape");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setSize(
                Tema.ANCHO_VENTANA,
                Tema.ALTO_VENTANA
        );

        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void construirInterfaz() {
        PanelFondo fondo = new PanelFondo(
                GestorAssets.getFondoMenu()
        );

        fondo.setLayout(new BorderLayout());

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        contenido.setBorder(
                BorderFactory.createEmptyBorder(
                        65,
                        0,
                        30,
                        0
                )
        );

        JLabel titulo = crearTitulo();
        JLabel subtitulo = crearSubtitulo();

        titulo.setAlignmentX(CENTER_ALIGNMENT);
        subtitulo.setAlignmentX(CENTER_ALIGNMENT);

        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(subtitulo);
        contenido.add(Box.createVerticalStrut(70));

        JPanel tarjeta = crearTarjetaInicio();

        tarjeta.setAlignmentX(CENTER_ALIGNMENT);

        contenido.add(tarjeta);
        contenido.add(Box.createVerticalGlue());

        fondo.add(contenido, BorderLayout.CENTER);

        setContentPane(fondo);
    }

    private JLabel crearTitulo() {
        JLabel titulo = new JLabel(
                "ZOMBI ESCAPE",
                SwingConstants.CENTER
        );

        titulo.setForeground(
                new Color(0xFF, 0xF4, 0xD8)
        );

        titulo.setFont(
                new Font(
                        Font.SERIF,
                        Font.BOLD,
                        68
                )
        );

        return titulo;
    }

    private JLabel crearSubtitulo() {
        JLabel subtitulo = new JLabel(
                "",
                SwingConstants.CENTER
        );

        subtitulo.setForeground(
                new Color(0xFF, 0xF4, 0xD8)
        );

        subtitulo.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.PLAIN,
                        24
                )
        );

        return subtitulo;
    }

    private JPanel crearTarjetaInicio() {
        PanelRedondeado tarjeta = new PanelRedondeado(
                Tema.PANEL,
                Tema.CONTORNO,
                30,
                3
        );

        tarjeta.setLayout(
                new BoxLayout(
                        tarjeta,
                        BoxLayout.Y_AXIS
                )
        );

        tarjeta.setPreferredSize(
                new Dimension(500, 250)
        );

        tarjeta.setMinimumSize(
                new Dimension(500, 250)
        );

        tarjeta.setMaximumSize(
                new Dimension(500, 250)
        );

        tarjeta.setBorder(
                BorderFactory.createEmptyBorder(
                        28,
                        45,
                        22,
                        45
                )
        );

        JLabel encabezado = new JLabel(
                ""
        );

        encabezado.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        20
                )
        );

        encabezado.setForeground(Tema.CONTORNO);
        encabezado.setAlignmentX(CENTER_ALIGNMENT);

        botonServidor.setAlignmentX(CENTER_ALIGNMENT);
        botonCliente.setAlignmentX(CENTER_ALIGNMENT);

        JLabel informacion = new JLabel(
                ""
        );

        informacion.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.PLAIN,
                        17
                )
        );

        informacion.setForeground(Tema.CONTORNO);
        informacion.setAlignmentX(CENTER_ALIGNMENT);

        JLabel informacionSecundaria = new JLabel(
                ""
        );

        informacionSecundaria.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.PLAIN,
                        15
                )
        );

        informacionSecundaria.setForeground(
                new Color(0x68, 0x78, 0x61)
        );

        informacionSecundaria.setAlignmentX(CENTER_ALIGNMENT);

        tarjeta.add(encabezado);
        tarjeta.add(Box.createVerticalStrut(30));

        tarjeta.add(botonServidor);
        tarjeta.add(Box.createVerticalStrut(18));

        tarjeta.add(botonCliente);
        tarjeta.add(Box.createVerticalStrut(28));

        tarjeta.add(informacion);
        tarjeta.add(Box.createVerticalStrut(6));

        tarjeta.add(informacionSecundaria);

        return tarjeta;
    }

    private void configurarEventos() {
        botonServidor.addActionListener(
                evento -> abrirServidor()
        );

        botonCliente.addActionListener(
                evento -> abrirCliente()
        );
    }

    private void abrirServidor() {
        if (servidorIniciado) {
            return;
        }

        servidorIniciado = true;

        botonServidor.setEnabled(false);
        botonServidor.setText("Servidor abierto");

        Thread hiloServidor = new Thread(() -> {
            try {
                ServidorMain.main(new String[0]);

            } finally {
                servidorIniciado = false;

                SwingUtilities.invokeLater(() -> {
                    if (isDisplayable()) {
                        botonServidor.setText("Abrir servidor");
                        botonServidor.setEnabled(true);
                    }
                });
            }
        });

        hiloServidor.setName("Launcher-Servidor");
        hiloServidor.start();
    }

    private void abrirCliente() {
        ClienteMain.main(new String[0]);
    }
}