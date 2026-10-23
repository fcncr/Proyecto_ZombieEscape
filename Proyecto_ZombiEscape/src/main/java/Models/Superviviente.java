package Models;

public class Superviviente extends Jugador {

    private static final long serialVersionUID = 1L;

    private boolean oculto;
    private long tiempoEscapeMs;

    public Superviviente(int id, String nombre, Posicion posicion) {
        super(id, nombre, posicion);
        this.oculto = false;
        this.tiempoEscapeMs = -1;
    }

    public boolean isOculto() {
        return oculto;
    }

    public void setOculto(boolean oculto) {
        this.oculto = oculto;
    }

    public long getTiempoEscapeMs() {
        return tiempoEscapeMs;
    }

    public void escapar(long tiempoEscapeMs) {
        if (tiempoEscapeMs < 0) {
            throw new IllegalArgumentException("El tiempo de escape no puede ser negativo");
        }

        this.tiempoEscapeMs = tiempoEscapeMs;
        setEstado(EstadoJugador.ESCAPADO);
    }
}