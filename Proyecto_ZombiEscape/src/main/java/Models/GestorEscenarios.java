package Models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GestorEscenarios {

    private static final Pattern PATRON_ESCENARIO = Pattern.compile("^escenario(\\d+)\\.txt$", Pattern.CASE_INSENSITIVE);

    private final Path directorio;
    private final List<Path> archivos;

    public GestorEscenarios(String rutaDirectorio) throws IOException {
        if (rutaDirectorio == null || rutaDirectorio.isBlank()) {
            throw new IllegalArgumentException("La ruta del directorio no puede estar vacia");
        }

        this.directorio = Path.of(rutaDirectorio);
        this.archivos = new ArrayList<>();

        actualizarLista();
    }

    public void actualizarLista() throws IOException {
        archivos.clear();

        if (!Files.isDirectory(directorio)) {
            throw new IOException("No existe el directorio de escenarios: " + directorio);
        }

        try (Stream<Path> contenido = Files.list(directorio)) {
            contenido.filter(Files::isRegularFile).filter(this::esArchivoEscenario).sorted(Comparator.comparingInt(this::obtenerNumeroEscenario)).forEach(archivos::add);
        }
    }

    private boolean esArchivoEscenario(Path archivo) {
        if (archivo == null || archivo.getFileName() == null) {
            return false;
        }

        Matcher matcher = PATRON_ESCENARIO.matcher(archivo.getFileName().toString());
        return matcher.matches();
    }

    private int obtenerNumeroEscenario(Path archivo) {
        Matcher matcher = PATRON_ESCENARIO.matcher(archivo.getFileName().toString());

        if (!matcher.matches()) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException error) {
            return Integer.MAX_VALUE;
        }
    }

    public int cantidad() {
        return archivos.size();
    }

    public Escenario cargar(int indice) throws IOException {
        if (indice < 0 || indice >= archivos.size()) {
            throw new IndexOutOfBoundsException("Indice de escenario invalido: " + indice);
        }

        Path archivo = archivos.get(indice);
        return Escenario.cargar(archivo.toString());
    }

    public List<Path> listarEscenarios() {
        return new ArrayList<>(archivos);
    }

    public Path getDirectorio() {
        return directorio;
    }
}