package Models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

        this.directorio = Path.of(rutaDirectorio).toAbsolutePath().normalize();
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

    public Path importar(Path archivoOrigen) throws IOException {
        if (archivoOrigen == null) {
            throw new IllegalArgumentException("Debe seleccionar un archivo");
        }

        Path origen = archivoOrigen.toAbsolutePath().normalize();

        if (!Files.isRegularFile(origen)) {
            throw new IOException("El archivo seleccionado no existe");
        }

        String nombre = origen.getFileName().toString().toLowerCase();

        if (!nombre.endsWith(".txt")) {
            throw new IllegalArgumentException("El escenario debe ser un archivo .txt");
        }

        try {
            Escenario.cargar(origen.toString());
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Escenario invalido: " + error.getMessage(), error);
        }

        actualizarLista();

        int numero = obtenerSiguienteNumero();
        Path destino = directorio.resolve("escenario" + numero + ".txt");

        Files.copy(origen, destino, StandardCopyOption.COPY_ATTRIBUTES);

        try {
            Escenario.cargar(destino.toString());
        } catch (IOException | IllegalArgumentException error) {
            Files.deleteIfExists(destino);
            throw error;
        }

        actualizarLista();

        return destino;
    }

    private int obtenerSiguienteNumero() {
        int mayor = 0;

        for (Path archivo : archivos) {
            mayor = Math.max(mayor, obtenerNumeroEscenario(archivo));
        }

        return mayor + 1;
    }

    private boolean esArchivoEscenario(Path archivo) {
        if (archivo == null || archivo.getFileName() == null) {
            return false;
        }

        return PATRON_ESCENARIO.matcher(archivo.getFileName().toString()).matches();
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

        return Escenario.cargar(archivos.get(indice).toString());
    }

    public List<Path> listarEscenarios() {
        return new ArrayList<>(archivos);
    }

    public Path getDirectorio() {
        return directorio;
    }
}