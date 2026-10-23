package Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EstadoPartida implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ESPERANDO = "ESPERANDO";
    public static final String JUGANDO = "JUGANDO";
    public static final String DERROTA = "DERROTA";
    public static final String VICTORIA = "VICTORIA";
    public static final String NIVEL_SUPERADO = "NIVEL_SUPERADO";

    private final char[][] mapa;
    private final List<Jugador> jugadores;
    private final List<Zombie> zombies;
    private final List<Jugador> ranking;
    private final EstadoJuego estado;
    private final long tiempoRestanteMs;
    private final boolean derrotaEquipo;
    private final int numeroEscenario;
    private final int cantidadEscenarios;

    public EstadoPartida(char[][] mapa, List<Jugador> jugadores, String fase) {
        this(mapa, jugadores, new ArrayList<>(), convertirFase(fase), 0, false, 1, 1, jugadores);
    }

    public EstadoPartida(char[][] mapa, List<Jugador> jugadores, String fase, long tiempoRestanteMs) {
        this(mapa, jugadores, new ArrayList<>(), convertirFase(fase), tiempoRestanteMs, DERROTA.equals(fase), 1, 1, jugadores);
    }

    public EstadoPartida(char[][] mapa, List<Jugador> jugadores, List<Zombie> zombies, String fase, long tiempoRestanteMs) {
        this(mapa, jugadores, zombies, convertirFase(fase), tiempoRestanteMs, DERROTA.equals(fase), 1, 1, jugadores);
    }

    public EstadoPartida(char[][] mapa, List<Jugador> jugadores, List<Zombie> zombies, EstadoJuego estado, long tiempoRestanteMs, boolean derrotaEquipo) {
        this(mapa, jugadores, zombies, estado, tiempoRestanteMs, derrotaEquipo, 1, 1, jugadores);
    }

    public EstadoPartida(char[][] mapa, List<Jugador> jugadores, List<Zombie> zombies, EstadoJuego estado, long tiempoRestanteMs, boolean derrotaEquipo, int numeroEscenario, int cantidadEscenarios) {
        this(mapa, jugadores, zombies, estado, tiempoRestanteMs, derrotaEquipo, numeroEscenario, cantidadEscenarios, jugadores);
    }

    public EstadoPartida(char[][] mapa, List<Jugador> jugadores, List<Zombie> zombies, EstadoJuego estado, long tiempoRestanteMs, boolean derrotaEquipo, int numeroEscenario, int cantidadEscenarios, List<Jugador> ranking) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado de partida no puede ser nulo");
        }

        if (numeroEscenario <= 0) {
            throw new IllegalArgumentException("El numero de escenario debe ser positivo");
        }

        if (cantidadEscenarios <= 0) {
            throw new IllegalArgumentException("La cantidad de escenarios debe ser positiva");
        }

        this.mapa = copiarMapa(mapa);
        this.jugadores = copiarJugadores(jugadores);
        this.zombies = copiarZombies(zombies);
        this.ranking = copiarJugadores(ranking);
        this.estado = estado;
        this.tiempoRestanteMs = tiempoRestanteMs;
        this.derrotaEquipo = derrotaEquipo;
        this.numeroEscenario = numeroEscenario;
        this.cantidadEscenarios = cantidadEscenarios;
    }

    private static EstadoJuego convertirFase(String fase) {
        if (ESPERANDO.equals(fase)) {
            return EstadoJuego.ESPERA;
        }

        if (JUGANDO.equals(fase)) {
            return EstadoJuego.EN_CURSO;
        }

        if (DERROTA.equals(fase)) {
            return EstadoJuego.DERROTA;
        }

        if (VICTORIA.equals(fase)) {
            return EstadoJuego.VICTORIA;
        }

        if (NIVEL_SUPERADO.equals(fase)) {
            return EstadoJuego.NIVEL_SUPERADO;
        }

        throw new IllegalArgumentException("Fase de partida invalida");
    }

    private char[][] copiarMapa(char[][] original) {
        if (original == null) {
            return null;
        }

        char[][] copia = new char[original.length][];

        for (int i = 0; i < original.length; i++) {
            copia[i] = original[i] == null ? null : original[i].clone();
        }

        return copia;
    }

    private List<Jugador> copiarJugadores(List<Jugador> originales) {
        List<Jugador> copia = new ArrayList<>();

        if (originales == null) {
            return copia;
        }

        for (Jugador jugador : originales) {
            if (jugador == null) {
                continue;
            }

            Jugador nuevo;

            if (jugador instanceof Superviviente) {
                Superviviente original = (Superviviente) jugador;
                Superviviente superviviente = new Superviviente(original.getId(), original.getNombre(), original.getPosicion());

                superviviente.setOculto(original.isOculto());

                if (original.getEstado() == EstadoJugador.ESCAPADO) {
                    superviviente.escapar(original.getTiempoEscapeMs());
                }

                nuevo = superviviente;

            } else if (jugador instanceof Francotirador) {
                Francotirador original = (Francotirador) jugador;

                nuevo = new Francotirador(
                        original.getId(),
                        original.getNombre(),
                        original.getBalas(),
                        original.getCentroVista(),
                        original.getRadioVista()
                );

            } else {
                nuevo = new Jugador(jugador.getId(), jugador.getNombre(), jugador.getPosicion());
            }

            nuevo.sumarPuntos(jugador.getPuntos());
            nuevo.setEstado(jugador.getEstado());

            copia.add(nuevo);
        }

        return copia;
    }

    private List<Zombie> copiarZombies(List<Zombie> originales) {
        List<Zombie> copia = new ArrayList<>();

        if (originales == null) {
            return copia;
        }

        for (Zombie zombie : originales) {
            if (zombie == null) {
                continue;
            }

            Zombie nuevo = new Zombie(zombie.getId(), zombie.getPosicion(), zombie.getDireccion(), zombie.getAlcance());

            if (!zombie.isVivo()) {
                nuevo.morir();
            }

            copia.add(nuevo);
        }

        return copia;
    }

    public char[][] getMapa() {
        return copiarMapa(mapa);
    }

    public List<Jugador> getJugadores() {
        return copiarJugadores(jugadores);
    }

    public List<Zombie> getZombies() {
        return copiarZombies(zombies);
    }

    public List<Jugador> getRanking() {
        return copiarJugadores(ranking);
    }

    public Jugador getJugadorPorId(int idJugador) {
        for (Jugador jugador : jugadores) {
            if (jugador.getId() == idJugador) {
                List<Jugador> copia = copiarJugadores(List.of(jugador));
                return copia.isEmpty() ? null : copia.get(0);
            }
        }

        return null;
    }

    public EstadoJuego getEstado() {
        return estado;
    }

    public String getFase() {
        switch (estado) {
            case ESPERA:
                return ESPERANDO;

            case EN_CURSO:
                return JUGANDO;

            case DERROTA:
                return DERROTA;

            case VICTORIA:
                return VICTORIA;

            case NIVEL_SUPERADO:
                return NIVEL_SUPERADO;

            default:
                return estado.name();
        }
    }

    public long getTiempoRestanteMs() {
        return tiempoRestanteMs;
    }

    public boolean isDerrotaEquipo() {
        return derrotaEquipo;
    }

    public int getNumeroEscenario() {
        return numeroEscenario;
    }

    public int getCantidadEscenarios() {
        return cantidadEscenarios;
    }
}