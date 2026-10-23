package Models;

public class Francotirador extends Jugador {

    private static final long serialVersionUID = 1L;

    public static final int BALAS_POR_ESCENARIO = 10;
    public static final double PROBABILIDAD_FALLO = 0.15;
    public static final int PUNTOS_POR_ZOMBIE = 10;
    public static final int RADIO_VISTA = 4;

    private int balas;
    private Posicion centroVista;
    private int radioVista;

    public Francotirador(int id, String nombre) {
        super(id, nombre, null);
        this.balas = BALAS_POR_ESCENARIO;
        this.centroVista = null;
        this.radioVista = RADIO_VISTA;
    }

    Francotirador(int id, String nombre, int balas, Posicion centroVista, int radioVista) {
        super(id, nombre, null);

        if (balas < 0) {
            throw new IllegalArgumentException("La cantidad de balas no puede ser negativa");
        }

        if (radioVista <= 0) {
            throw new IllegalArgumentException("El radio de vista debe ser positivo");
        }

        this.balas = balas;
        this.centroVista = centroVista;
        this.radioVista = radioVista;
    }

    public void prepararEscenario(Escenario escenario) {
        if (escenario == null) {
            throw new IllegalArgumentException("El escenario no puede ser nulo");
        }

        this.balas = BALAS_POR_ESCENARIO;
        this.centroVista = escenario.getEntrada();
    }

    public boolean puedeVer(Posicion posicion) {
        if (posicion == null || centroVista == null) {
            return false;
        }

        int distanciaFila = Math.abs(posicion.getFila() - centroVista.getFila());
        int distanciaColumna = Math.abs(posicion.getColumna() - centroVista.getColumna());

        return distanciaFila <= radioVista && distanciaColumna <= radioVista;
    }

    public boolean tieneBalas() {
        return balas > 0;
    }

    public void consumirBala() {
        if (balas <= 0) {
            return;
        }

        balas--;
    }

    public int getBalas() {
        return balas;
    }

    public Posicion getCentroVista() {
        return centroVista;
    }

    public void setCentroVista(Posicion centroVista) {
        this.centroVista = centroVista;
    }

    public int getRadioVista() {
        return radioVista;
    }
}