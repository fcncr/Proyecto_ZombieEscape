package UI;

import Models.Direccion;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public final class GestorAssets {

    private static final String RAIZ = "/assets/";

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static final Map<String, BufferedImage> CACHE_ESCALADO = new HashMap<>();

    private GestorAssets() {
    }

    public static BufferedImage cargar(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException("La ruta del asset no puede estar vacia");
        }

        String rutaNormalizada = normalizarRuta(ruta);

        BufferedImage imagen = CACHE.get(rutaNormalizada);

        if (imagen != null) {
            return imagen;
        }

        imagen = cargarDesdeRecursos(rutaNormalizada);
        CACHE.put(rutaNormalizada, imagen);

        return imagen;
    }

    public static BufferedImage cargarEscalada(String ruta, int ancho, int alto) {
        if (ancho <= 0 || alto <= 0) {
            throw new IllegalArgumentException("El tamaño de la imagen debe ser positivo");
        }

        String rutaNormalizada = normalizarRuta(ruta);
        String clave = rutaNormalizada + "#" + ancho + "x" + alto;

        BufferedImage imagenEscalada = CACHE_ESCALADO.get(clave);

        if (imagenEscalada != null) {
            return imagenEscalada;
        }

        BufferedImage original = cargar(rutaNormalizada);

        BufferedImage nueva = new BufferedImage(
                ancho,
                alto,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = nueva.createGraphics();

        try {
            g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            g.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.drawImage(original, 0, 0, ancho, alto, null);

        } finally {
            g.dispose();
        }

        CACHE_ESCALADO.put(clave, nueva);

        return nueva;
    }

    public static BufferedImage getFondoMenu() {
        return cargar("backgrounds/garden_menu.png");
    }

    public static BufferedImage getJugador(int plazaVisual) {
        if (plazaVisual < 0 || plazaVisual > 9) {
            throw new IllegalArgumentException("La plaza visual debe estar entre 0 y 9");
        }

        String numero = String.format("%02d", plazaVisual + 1);

        return cargar("players/player_" + numero + ".png");
    }

    public static BufferedImage getJugadorEscalado(int plazaVisual, int ancho, int alto) {
        if (plazaVisual < 0 || plazaVisual > 9) {
            throw new IllegalArgumentException("La plaza visual debe estar entre 0 y 9");
        }

        String numero = String.format("%02d", plazaVisual + 1);

        return cargarEscalada(
                "players/player_" + numero + ".png",
                ancho,
                alto
        );
    }

    public static BufferedImage getZombie(Direccion direccion) {
        if (direccion == null) {
            throw new IllegalArgumentException("La direccion del zombie no puede ser nula");
        }

        switch (direccion) {
            case ARRIBA:
                return cargar("zombies/zombie_n.png");

            case DERECHA:
                return cargar("zombies/zombie_e.png");

            case ABAJO:
                return cargar("zombies/zombie_s.png");

            case IZQUIERDA:
                return cargar("zombies/zombie_w.png");

            default:
                throw new IllegalArgumentException("Direccion de zombie no soportada");
        }
    }

    public static BufferedImage getZombieEscalado(Direccion direccion, int ancho, int alto) {
        if (direccion == null) {
            throw new IllegalArgumentException("La direccion del zombie no puede ser nula");
        }

        String archivo;

        switch (direccion) {
            case ARRIBA:
                archivo = "zombies/zombie_n.png";
                break;

            case DERECHA:
                archivo = "zombies/zombie_e.png";
                break;

            case ABAJO:
                archivo = "zombies/zombie_s.png";
                break;

            case IZQUIERDA:
                archivo = "zombies/zombie_w.png";
                break;

            default:
                throw new IllegalArgumentException("Direccion de zombie no soportada");
        }

        return cargarEscalada(archivo, ancho, alto);
    }

    public static BufferedImage getTile(char simbolo) {
        switch (simbolo) {
            case '#':
                return cargar("tiles/wall.png");

            case 'H':
                return cargar("tiles/hiding.png");

            case 'E':
                return cargar("tiles/entry.png");

            case 'S':
                return cargar("tiles/exit.png");

            case '.':
                return cargar("tiles/ground.png");

            default:
                return getTileExterior();
        }
    }

    public static BufferedImage getTileExterior() {
        return cargar("tiles/outside.png");
    }

    public static BufferedImage getIcono(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del icono no puede estar vacio");
        }

        return cargar("icons/" + nombre + ".png");
    }

    public static BufferedImage getBoton(String tipo, String estado) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de boton no puede estar vacio");
        }

        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado del boton no puede estar vacio");
        }

        return cargar(
                "ui/button_"
                + tipo.toLowerCase()
                + "_"
                + estado.toLowerCase()
                + ".png"
        );
    }

    public static BufferedImage getPanelCrema() {
        return cargar("ui/panel_cream.png");
    }

    public static void verificarAssetsPrincipales() {
        getFondoMenu();

        getTile('.');
        getTile('#');
        getTile('H');
        getTile('E');
        getTile('S');
        getTileExterior();

        for (int indice = 0; indice < 10; indice++) {
            getJugador(indice);
        }

        for (Direccion direccion : Direccion.values()) {
            getZombie(direccion);
        }

        getIcono("timer");
        getIcono("chat");
        getIcono("bullet");
        getIcono("warning");
        getIcono("hidden");
        getIcono("sniper");
        getIcono("connection");
        getIcono("check");
        getIcono("folder");
        getIcono("eye");
        getIcono("exit");

        getIcono("arrow_n");
        getIcono("arrow_e");
        getIcono("arrow_s");
        getIcono("arrow_w");

        String[] tipos = {
            "primary",
            "secondary",
            "danger"
        };

        String[] estados = {
            "normal",
            "hover",
            "pressed",
            "disabled"
        };

        for (String tipo : tipos) {
            for (String estado : estados) {
                getBoton(tipo, estado);
            }
        }

        getPanelCrema();
    }

    public static void limpiarCache() {
        CACHE.clear();
        CACHE_ESCALADO.clear();
    }

    private static BufferedImage cargarDesdeRecursos(String ruta) {
        try (InputStream entrada = GestorAssets.class.getResourceAsStream(ruta)) {

            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontro el asset: " + ruta
                );
            }

            BufferedImage imagen = ImageIO.read(entrada);

            if (imagen == null) {
                throw new IllegalStateException(
                        "No se pudo interpretar la imagen: " + ruta
                );
            }

            return imagen;

        } catch (IOException error) {
            throw new IllegalStateException(
                    "No se pudo cargar el asset: " + ruta,
                    error
            );
        }
    }

    private static String normalizarRuta(String ruta) {
        String valor = ruta.replace("\\", "/").trim();

        while (valor.startsWith("/")) {
            valor = valor.substring(1);
        }

        if (valor.startsWith("assets/")) {
            valor = valor.substring("assets/".length());
        }

        return RAIZ + valor;
    }
}