package Servidor;

import Models.Escenario;
import Models.GestorEscenarios;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DialogoEscenarios extends JDialog {

    private final GestorEscenarios gestor;
    private final DefaultListModel<String> modelo;
    private final JList<String> lista;
    private final JLabel etiquetaCantidad;
    private final JLabel etiquetaRuta;
    private final JButton botonImportar;
    private final JButton botonCerrar;

    public DialogoEscenarios(JFrame propietario) throws IOException {
        super(propietario, "Gestión de escenarios", true);

        gestor = new GestorEscenarios(ChatServer.DIRECTORIO_ESCENARIOS);
        modelo = new DefaultListModel<>();
        lista = new JList<>(modelo);
        etiquetaCantidad = new JLabel();
        etiquetaRuta = new JLabel();
        botonImportar = new JButton("Importar escenario");
        botonCerrar = new JButton("Cerrar");

        configurarVentana();
        construirInterfaz();
        configurarEventos();
        actualizarLista();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(650, 520);
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    private void construirInterfaz() {
        JPanel superior = new JPanel(new BorderLayout(5, 5));
        superior.setBorder(BorderFactory.createEmptyBorder(18, 18, 12, 18));

        JLabel titulo = new JLabel("Escenarios disponibles");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));

        superior.add(titulo, BorderLayout.NORTH);
        superior.add(etiquetaCantidad, BorderLayout.CENTER);

        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 18, 10, 18));

        JPanel inferior = new JPanel(new BorderLayout());
        inferior.setBorder(BorderFactory.createEmptyBorder(4, 18, 18, 18));

        etiquetaRuta.setFont(etiquetaRuta.getFont().deriveFont(11f));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(botonImportar);
        botones.add(botonCerrar);

        inferior.add(etiquetaRuta, BorderLayout.NORTH);
        inferior.add(botones, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(superior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);
    }

    private void configurarEventos() {
        botonImportar.addActionListener(evento -> importarEscenario());
        botonCerrar.addActionListener(evento -> dispose());
    }

    private void actualizarLista() {
        try {
            gestor.actualizarLista();

            modelo.clear();

            List<Path> archivos = gestor.listarEscenarios();

            for (int i = 0; i < archivos.size(); i++) {
                try {
                    Escenario escenario = gestor.cargar(i);

                    modelo.addElement(
                            String.format(
                                    "%02d  %-18s  %dx%d",
                                    i + 1,
                                    escenario.getNombre(),
                                    escenario.getColumnas(),
                                    escenario.getFilas()
                            )
                    );
                } catch (IOException | IllegalArgumentException error) {
                    modelo.addElement((i + 1) + "  " + archivos.get(i).getFileName() + "  [INVALIDO]");
                }
            }

            etiquetaCantidad.setText("Total: " + archivos.size() + " escenarios");
            etiquetaRuta.setText("Carpeta: " + gestor.getDirectorio());

        } catch (IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Escenarios", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importarEscenario() {
        JFileChooser selector = new JFileChooser();

        selector.setDialogTitle("Seleccionar escenario");
        selector.setFileSelectionMode(JFileChooser.FILES_ONLY);
        selector.setAcceptAllFileFilterUsed(false);
        selector.setFileFilter(new FileNameExtensionFilter("Escenarios de texto (*.txt)", "txt"));

        int resultado = selector.showOpenDialog(this);

        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path origen = selector.getSelectedFile().toPath();

        try {
            Path destino = gestor.importar(origen);

            actualizarLista();

            JOptionPane.showMessageDialog(
                    this,
                    "Escenario importado como:\n" + destino.getFileName(),
                    "Escenario importado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException | IllegalArgumentException error) {
            JOptionPane.showMessageDialog(
                    this,
                    error.getMessage(),
                    "Escenario inválido",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}