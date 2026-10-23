package Models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Escenario implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final char PARED = '#';
    public static final char CAMINO = '.';
    public static final char ENTRADA = 'E';
    public static final char ESCONDITE = 'H';
    public static final char SALIDA = 'S';

    private final String nombre;
    private final char[][] mapa;
    private final Posicion entrada;
    private final Posicion salida;

    private Escenario(String nombre, char[][] mapa, Posicion entrada, Posicion salida) {
        this.nombre = nombre;
        this.mapa = mapa;
        this.entrada = entrada;
        this.salida = salida;
    }

    public static Escenario cargar(String ruta) throws IOException {
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException("La ruta del escenario no puede estar vacia");
        }

        Path archivo = Path.of(ruta);

        if (!Files.isRegularFile(archivo)) {
            throw new IOException("No existe el archivo del escenario: " + ruta);
        }

        List<String> filas = new ArrayList<>();

        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                filas.add(linea);
            }
        }

        if (filas.isEmpty()) {
            throw new IllegalArgumentException("El escenario esta vacio");
        }

        int cantidadFilas = filas.size();
        int cantidadColumnas = filas.get(0).length();

        if (cantidadColumnas == 0) {
            throw new IllegalArgumentException("El escenario no puede tener filas vacias");
        }

        char[][] celdas = new char[cantidadFilas][cantidadColumnas];

        Posicion posicionEntrada = null;
        Posicion posicionSalida = null;

        int cantidadEntradas = 0;
        int cantidadSalidas = 0;
        int cantidadEscondites = 0;
        int cantidadTransitables = 0;

        for (int fila = 0; fila < cantidadFilas; fila++) {
            String contenido = filas.get(fila);

            if (contenido.length() != cantidadColumnas) {
                throw new IllegalArgumentException("La fila " + (fila + 1) + " tiene una cantidad incorrecta de columnas");
            }

            for (int columna = 0; columna < cantidadColumnas; columna++) {
                char simbolo = contenido.charAt(columna);

                if (!simboloValido(simbolo)) {
                    throw new IllegalArgumentException("Simbolo desconocido '" + simbolo + "' en fila " + (fila + 1) + ", columna " + (columna + 1));
                }

                celdas[fila][columna] = simbolo;

                if (simbolo != PARED) {
                    cantidadTransitables++;
                }

                if (simbolo == ENTRADA) {
                    cantidadEntradas++;
                    posicionEntrada = new Posicion(fila, columna);
                }

                if (simbolo == SALIDA) {
                    cantidadSalidas++;
                    posicionSalida = new Posicion(fila, columna);
                }

                if (simbolo == ESCONDITE) {
                    cantidadEscondites++;
                }
            }
        }

        if (cantidadEntradas != 1) {
            throw new IllegalArgumentException("El escenario debe tener exactamente una entrada E");
        }

        if (cantidadSalidas != 1) {
            throw new IllegalArgumentException("El escenario debe tener exactamente una salida S");
        }

        if (cantidadEscondites == 0) {
            throw new IllegalArgumentException("El escenario debe tener al menos un escondite H");
        }

        if (cantidadTransitables < 10) {
            throw new IllegalArgumentException("El escenario debe tener al menos 10 casillas transitables");
        }

        if (!existeCamino(celdas, posicionEntrada, posicionSalida)) {
            throw new IllegalArgumentException("No existe un camino entre la entrada y la salida");
        }

        return new Escenario(archivo.getFileName().toString(), celdas, posicionEntrada, posicionSalida);
    }

    private static boolean simboloValido(char simbolo) {
        return simbolo == PARED
                || simbolo == CAMINO
                || simbolo == ENTRADA
                || simbolo == ESCONDITE
                || simbolo == SALIDA;
    }

    private static boolean existeCamino(char[][] celdas, Posicion inicio, Posicion destino) {
        boolean[][] visitadas = new boolean[celdas.length][celdas[0].length];
        Queue<Posicion> pendientes = new ArrayDeque<>();

        pendientes.add(inicio);
        visitadas[inicio.getFila()][inicio.getColumna()] = true;

        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.poll();

            if (actual.equals(destino)) {
                return true;
            }

            for (Direccion direccion : Direccion.values()) {
                Posicion siguiente = actual.siguiente(direccion);

                int fila = siguiente.getFila();
                int columna = siguiente.getColumna();

                if (fila < 0 || fila >= celdas.length || columna < 0 || columna >= celdas[0].length) {
                    continue;
                }

                if (visitadas[fila][columna] || celdas[fila][columna] == PARED) {
                    continue;
                }

                visitadas[fila][columna] = true;
                pendientes.add(siguiente);
            }
        }

        return false;
    }

    public boolean hayVision(Posicion origen, Posicion destino) {
        if (!dentro(origen) || !dentro(destino)) {
            return false;
        }

        if (origen.equals(destino)) {
            return true;
        }

        int diferenciaFila = destino.getFila() - origen.getFila();
        int diferenciaColumna = destino.getColumna() - origen.getColumna();

        if (diferenciaFila != 0 && diferenciaColumna != 0) {
            return false;
        }

        int pasoFila = Integer.compare(destino.getFila(), origen.getFila());
        int pasoColumna = Integer.compare(destino.getColumna(), origen.getColumna());

        int fila = origen.getFila() + pasoFila;
        int columna = origen.getColumna() + pasoColumna;

        while (fila != destino.getFila() || columna != destino.getColumna()) {
            if (!esTransitable(fila, columna)) {
                return false;
            }

            fila += pasoFila;
            columna += pasoColumna;
        }

        return esTransitable(destino);
    }

    public String getNombre() {
        return nombre;
    }

    public int getFilas() {
        return mapa.length;
    }

    public int getColumnas() {
        return mapa[0].length;
    }

    public Posicion getEntrada() {
        return entrada;
    }

    public Posicion getSalida() {
        return salida;
    }

    public char[][] getMapa() {
        char[][] copia = new char[mapa.length][];

        for (int fila = 0; fila < mapa.length; fila++) {
            copia[fila] = mapa[fila].clone();
        }

        return copia;
    }

    public boolean dentro(int fila, int columna) {
        return fila >= 0 && fila < getFilas() && columna >= 0 && columna < getColumnas();
    }

    public boolean dentro(Posicion posicion) {
        return posicion != null && dentro(posicion.getFila(), posicion.getColumna());
    }

    public boolean esTransitable(int fila, int columna) {
        return dentro(fila, columna) && mapa[fila][columna] != PARED;
    }

    public boolean esTransitable(Posicion posicion) {
        return posicion != null && esTransitable(posicion.getFila(), posicion.getColumna());
    }

    public boolean esEscondite(Posicion posicion) {
        return dentro(posicion) && mapa[posicion.getFila()][posicion.getColumna()] == ESCONDITE;
    }

    public boolean esSalida(Posicion posicion) {
        return dentro(posicion) && mapa[posicion.getFila()][posicion.getColumna()] == SALIDA;
    }

    public char getCelda(int fila, int columna) {
        if (!dentro(fila, columna)) {
            throw new IllegalArgumentException("La posicion esta fuera del escenario");
        }

        return mapa[fila][columna];
    }

    @Override
    public String toString() {
        return "Escenario: " + nombre + " (" + getFilas() + "x" + getColumnas() + ")";
    }
}