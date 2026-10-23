package Servidor;

import Models.Direccion;
import Models.Escenario;
import Models.EstadoJuego;
import Models.EstadoJugador;
import Models.EstadoPartida;
import Models.Jugador;
import Models.Posicion;
import Models.Superviviente;
import Models.Zombie;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Partida {

    public static final int MIN_JUGADORES = 2;
    public static final int MAX_JUGADORES = 10;
    public static final long DURACION_ESCENARIO_MS = 180000L;
    public static final int CANTIDAD_ZOMBIES_BASE = 3;
    public static final int ALCANCE_ZOMBIE_BASE = 3;
    public static final int DISTANCIA_SEGURA_INICIAL = 4;

    private final Escenario escenario;
    private final List<Jugador> jugadores;
    private final List<Zombie> zombies;
    private final Random azar;
    private EstadoJuego estado;
    private long inicioEscenarioMs;

    public Partida(Escenario escenario) {
        if (escenario == null) {
            throw new IllegalArgumentException("El escenario no puede ser nulo");
        }

        this.escenario = escenario;
        this.jugadores = new ArrayList<>();
        this.zombies = new ArrayList<>();
        this.azar = new Random();
        this.estado = EstadoJuego.ESPERA;
        this.inicioEscenarioMs = 0;
    }

    public synchronized void agregarJugador(int id, String nombre) {
        if (!estaEsperando()) {
            throw new IllegalStateException("La partida ya fue iniciada");
        }

        if (jugadores.size() >= MAX_JUGADORES) {
            throw new IllegalStateException("La partida alcanzo el maximo de jugadores");
        }

        for (Jugador jugador : jugadores) {
            if (jugador.getId() == id) {
                throw new IllegalArgumentException("Ya existe un jugador con ese ID");
            }

            if (jugador.getNombre().equalsIgnoreCase(nombre)) {
                throw new IllegalArgumentException("Ya existe un jugador con ese nombre");
            }
        }

        Superviviente superviviente = new Superviviente(id, nombre, null);
        jugadores.add(superviviente);
        jugadores.sort(Comparator.comparingInt(Jugador::getId));
    }

    public synchronized boolean iniciar() {
        if (!estaEsperando()) {
            return false;
        }

        if (jugadores.size() < MIN_JUGADORES || jugadores.size() > MAX_JUGADORES) {
            return false;
        }

        List<Posicion> posiciones = buscarPosicionesIniciales(jugadores.size());

        if (posiciones.size() < jugadores.size()) {
            throw new IllegalStateException("El escenario no tiene suficientes posiciones iniciales validas");
        }

        for (int indice = 0; indice < jugadores.size(); indice++) {
            Jugador jugador = jugadores.get(indice);
            jugador.setPosicion(posiciones.get(indice));
            jugador.setEstado(EstadoJugador.ACTIVO);

            if (jugador instanceof Superviviente) {
                Superviviente superviviente = (Superviviente) jugador;
                superviviente.setOculto(false);
            }
        }

        generarZombies();

        inicioEscenarioMs = System.currentTimeMillis();
        estado = EstadoJuego.EN_CURSO;

        evaluarDetecciones();

        return true;
    }

    private List<Posicion> buscarPosicionesIniciales(int cantidad) {
        List<Posicion> posiciones = new ArrayList<>();
        Queue<Posicion> pendientes = new ArrayDeque<>();
        boolean[][] visitadas = new boolean[escenario.getFilas()][escenario.getColumnas()];

        Posicion entrada = escenario.getEntrada();

        pendientes.add(entrada);
        visitadas[entrada.getFila()][entrada.getColumna()] = true;

        while (!pendientes.isEmpty() && posiciones.size() < cantidad) {
            Posicion actual = pendientes.poll();

            if (escenario.esTransitable(actual) && !escenario.esSalida(actual)) {
                posiciones.add(actual);
            }

            for (Direccion direccion : Direccion.values()) {
                Posicion siguiente = actual.siguiente(direccion);

                if (!escenario.dentro(siguiente)) {
                    continue;
                }

                if (visitadas[siguiente.getFila()][siguiente.getColumna()]) {
                    continue;
                }

                if (!escenario.esTransitable(siguiente)) {
                    continue;
                }

                visitadas[siguiente.getFila()][siguiente.getColumna()] = true;
                pendientes.add(siguiente);
            }
        }

        return posiciones;
    }

    private void generarZombies() {
        zombies.clear();

        List<Posicion> candidatas = obtenerPosicionesZombie();
        Collections.shuffle(candidatas, azar);

        int cantidad = Math.min(CANTIDAD_ZOMBIES_BASE, candidatas.size());

        for (int indice = 0; indice < cantidad; indice++) {
            Posicion posicion = candidatas.get(indice);
            Direccion direccion = Direccion.values()[azar.nextInt(Direccion.values().length)];
            Zombie zombie = new Zombie(indice + 1, posicion, direccion, ALCANCE_ZOMBIE_BASE);
            zombies.add(zombie);
        }

        if (zombies.size() < CANTIDAD_ZOMBIES_BASE) {
            System.out.println("Advertencia: solo se pudieron generar " + zombies.size() + " zombies");
        }
    }

    private List<Posicion> obtenerPosicionesZombie() {
        List<Posicion> posiciones = new ArrayList<>();

        for (int fila = 0; fila < escenario.getFilas(); fila++) {
            for (int columna = 0; columna < escenario.getColumnas(); columna++) {
                Posicion posicion = new Posicion(fila, columna);

                if (!escenario.esTransitable(posicion)) {
                    continue;
                }

                if (escenario.esSalida(posicion) || posicion.equals(escenario.getEntrada())) {
                    continue;
                }

                if (escenario.esEscondite(posicion)) {
                    continue;
                }

                if (estaOcupadaPorJugador(posicion)) {
                    continue;
                }

                if (!estaLejosDeJugadores(posicion)) {
                    continue;
                }

                posiciones.add(posicion);
            }
        }

        return posiciones;
    }

    private boolean estaLejosDeJugadores(Posicion posicion) {
        for (Jugador jugador : jugadores) {
            if (jugador.getPosicion() == null) {
                continue;
            }

            int distanciaFila = Math.abs(posicion.getFila() - jugador.getPosicion().getFila());
            int distanciaColumna = Math.abs(posicion.getColumna() - jugador.getPosicion().getColumna());
            int distancia = distanciaFila + distanciaColumna;

            if (distancia < DISTANCIA_SEGURA_INICIAL) {
                return false;
            }
        }

        return true;
    }

    public synchronized boolean mover(int idJugador, Direccion direccion) {
        if (!estaJugando()) {
            return false;
        }

        if (direccion == null) {
            return false;
        }

        Jugador jugador = buscarJugador(idJugador);

        if (jugador == null || !jugador.puedeActuar() || jugador.getPosicion() == null) {
            return false;
        }

        Posicion destino = jugador.getPosicion().siguiente(direccion);

        if (!escenario.dentro(destino)) {
            return false;
        }

        if (!escenario.esTransitable(destino)) {
            return false;
        }

        if (estaOcupadaPorJugador(destino, idJugador)) {
            return false;
        }

        jugador.setPosicion(destino);

        if (jugador instanceof Superviviente) {
            Superviviente superviviente = (Superviviente) jugador;
            superviviente.setOculto(escenario.esEscondite(destino));
        }

        evaluarDetecciones();

        return true;
    }

    public synchronized boolean actualizar(long ahoraMs) {
        if (!estaJugando()) {
            return false;
        }

        boolean huboCambio = false;

        for (Zombie zombie : zombies) {
            if (!zombie.isVivo()) {
                continue;
            }

            Posicion posicionAnterior = zombie.getPosicion();
            Direccion direccionAnterior = zombie.getDireccion();

            zombie.patrullar(escenario, ahoraMs);

            if (!posicionAnterior.equals(zombie.getPosicion()) || direccionAnterior != zombie.getDireccion()) {
                huboCambio = true;
            }
        }

        if (evaluarDetecciones()) {
            huboCambio = true;
        }

        return huboCambio;
    }

    private boolean evaluarDetecciones() {
        if (!estaJugando()) {
            return false;
        }

        for (Zombie zombie : zombies) {
            if (!zombie.isVivo()) {
                continue;
            }

            for (Jugador jugador : jugadores) {
                if (!(jugador instanceof Superviviente)) {
                    continue;
                }

                Superviviente superviviente = (Superviviente) jugador;

                if (!superviviente.puedeActuar()) {
                    continue;
                }

                if (zombie.detecta(superviviente, escenario)) {
                    superviviente.morir();
                    estado = EstadoJuego.DERROTA;
                    System.out.println("Jugador detectado por zombie");
                    System.out.println("Jugador: " + superviviente.getNombre());
                    System.out.println("Zombie: Z" + zombie.getId());
                    System.out.println("Resultado: DERROTA DEL EQUIPO");
                    return true;
                }
            }
        }

        return false;
    }

    private boolean estaOcupadaPorJugador(Posicion posicion) {
        return estaOcupadaPorJugador(posicion, -1);
    }

    private boolean estaOcupadaPorJugador(Posicion posicion, int idJugadorIgnorado) {
        for (Jugador jugador : jugadores) {
            if (jugador.getId() == idJugadorIgnorado) {
                continue;
            }

            if (!jugador.isActivo() || jugador.getPosicion() == null) {
                continue;
            }

            if (jugador.getPosicion().equals(posicion)) {
                return true;
            }
        }

        return false;
    }

    public synchronized void desconectar(int idJugador) {
        Jugador jugador = buscarJugador(idJugador);

        if (jugador == null) {
            return;
        }

        if (estaEsperando()) {
            jugadores.remove(jugador);
            return;
        }

        jugador.setEstado(EstadoJugador.DESCONECTADO);
    }

    private Jugador buscarJugador(int idJugador) {
        for (Jugador jugador : jugadores) {
            if (jugador.getId() == idJugador) {
                return jugador;
            }
        }

        return null;
    }

    public synchronized long getTiempoRestanteMs() {
        if (inicioEscenarioMs <= 0) {
            return 0;
        }

        long transcurrido = System.currentTimeMillis() - inicioEscenarioMs;
        long restante = DURACION_ESCENARIO_MS - transcurrido;

        return Math.max(0, restante);
    }

    public synchronized EstadoPartida crearEstado() {
        boolean derrotaEquipo = estado == EstadoJuego.DERROTA;
        return new EstadoPartida(escenario.getMapa(), jugadores, zombies, estado, getTiempoRestanteMs(), derrotaEquipo);
    }

    public synchronized int getCantidadJugadores() {
        return jugadores.size();
    }

    public synchronized String getFase() {
        switch (estado) {
            case ESPERA:
                return EstadoPartida.ESPERANDO;

            case EN_CURSO:
                return EstadoPartida.JUGANDO;

            case DERROTA:
                return EstadoPartida.DERROTA;

            default:
                return estado.name();
        }
    }

    public synchronized EstadoJuego getEstado() {
        return estado;
    }

    public synchronized boolean estaEsperando() {
        return estado == EstadoJuego.ESPERA;
    }

    public synchronized boolean estaJugando() {
        return estado == EstadoJuego.EN_CURSO;
    }

    public Escenario getEscenario() {
        return escenario;
    }
}