package Servidor;

import Models.Direccion;
import Models.Escenario;
import Models.EstadoJuego;
import Models.EstadoJugador;
import Models.EstadoPartida;
import Models.Francotirador;
import Models.GestorEscenarios;
import Models.Jugador;
import Models.Mensaje;
import Models.Posicion;
import Models.Superviviente;
import Models.Zombie;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

public class Partida {

    public static final int MIN_JUGADORES = 2;
    public static final int MAX_JUGADORES = 10;
    public static final long DURACION_ESCENARIO_MS = 180000L;
    public static final int CANTIDAD_ZOMBIES_BASE = 3;
    public static final int ALCANCE_ZOMBIE_BASE = 3;
    public static final int DISTANCIA_SEGURA_INICIAL = 4;

    private final GestorEscenarios gestorEscenarios;
    private final List<Jugador> jugadores;
    private final List<Zombie> zombies;
    private final Map<Integer, Integer> puntosInicioEscenario;
    private final Random azar;

    private Escenario escenario;
    private EstadoJuego estado;
    private int indiceEscenario;
    private long inicioEscenarioMs;
    private long ultimoSegundoNotificado;
    private int ordenEscape;
    private boolean derrotaEquipo;

    public Partida(GestorEscenarios gestorEscenarios) throws IOException {
        if (gestorEscenarios == null) {
            throw new IllegalArgumentException("El gestor de escenarios no puede ser nulo");
        }

        if (gestorEscenarios.cantidad() == 0) {
            throw new IllegalArgumentException("No existen escenarios disponibles");
        }

        this.gestorEscenarios = gestorEscenarios;
        this.jugadores = new ArrayList<>();
        this.zombies = new ArrayList<>();
        this.puntosInicioEscenario = new HashMap<>();
        this.azar = new Random();
        this.indiceEscenario = 0;
        this.escenario = gestorEscenarios.cargar(indiceEscenario);
        this.estado = EstadoJuego.ESPERA;
        this.inicioEscenarioMs = 0;
        this.ultimoSegundoNotificado = 0;
        this.ordenEscape = 0;
        this.derrotaEquipo = false;
    }

    public synchronized void agregarJugador(int id, String nombre, String rol) {
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

        Jugador jugador;

        if (Mensaje.ROL_SUPERVIVIENTE.equals(rol)) {
            jugador = new Superviviente(id, nombre, null);

        } else if (Mensaje.ROL_FRANCOTIRADOR.equals(rol)) {
            jugador = new Francotirador(id, nombre);

        } else {
            throw new IllegalArgumentException("Rol invalido");
        }

        jugadores.add(jugador);
        jugadores.sort(Comparator.comparingInt(Jugador::getId));
    }

    public synchronized boolean iniciar() {
        if (!estaEsperando()) {
            return false;
        }

        if (jugadores.size() < MIN_JUGADORES || jugadores.size() > MAX_JUGADORES) {
            return false;
        }

        if (contarSupervivientesConectados() == 0) {
            System.out.println("No se puede iniciar una partida sin supervivientes");
            return false;
        }

        try {
            prepararEscenario();
            return true;

        } catch (IOException | IllegalStateException error) {
            throw new IllegalStateException("No se pudo preparar el escenario: " + error.getMessage(), error);
        }
    }

    public synchronized boolean reintentarEscenario() {
        if (estado != EstadoJuego.DERROTA) {
            return false;
        }

        if (contarSupervivientesConectados() == 0) {
            System.out.println("No se puede reintentar: no hay supervivientes conectados");
            return false;
        }

        System.out.println("Reintentando escenario " + getNumeroEscenario());

        restaurarJugadoresAlInicioEscenario();

        try {
            prepararEscenario();

            System.out.println("Reintento preparado correctamente");
            System.out.println("Escenario: " + getNumeroEscenario() + " / " + gestorEscenarios.cantidad());

            return true;

        } catch (IOException | IllegalStateException error) {
            estado = EstadoJuego.DERROTA;
            System.err.println("No se pudo reintentar el escenario: " + error.getMessage());
            return false;
        }
    }

    private void prepararEscenario() throws IOException {
        escenario = gestorEscenarios.cargar(indiceEscenario);

        List<Superviviente> supervivientes = obtenerSupervivientesConectados();
        List<Posicion> posiciones = buscarPosicionesIniciales(supervivientes.size());

        if (supervivientes.isEmpty()) {
            throw new IllegalStateException("No hay supervivientes conectados");
        }

        if (posiciones.size() < supervivientes.size()) {
            throw new IllegalStateException("El escenario no tiene suficientes posiciones iniciales validas");
        }

        guardarPuntosInicioEscenario();

        ordenEscape = 0;
        derrotaEquipo = false;
        zombies.clear();

        int indicePosicion = 0;

        for (Jugador jugador : jugadores) {
            if (jugador.getEstado() == EstadoJugador.DESCONECTADO) {
                continue;
            }

            if (jugador instanceof Superviviente) {
                Superviviente superviviente = (Superviviente) jugador;

                superviviente.setPosicion(posiciones.get(indicePosicion));
                superviviente.setEstado(EstadoJugador.ACTIVO);
                superviviente.setOculto(false);

                indicePosicion++;

            } else if (jugador instanceof Francotirador) {
                Francotirador francotirador = (Francotirador) jugador;

                francotirador.setEstado(EstadoJugador.ACTIVO);
                francotirador.prepararEscenario(escenario);
            }
        }

        generarZombies();

        inicioEscenarioMs = System.currentTimeMillis();
        ultimoSegundoNotificado = calcularSegundoVisible(getTiempoRestanteMs());
        estado = EstadoJuego.EN_CURSO;

        System.out.println("Escenario preparado");
        System.out.println("Escenario: " + getNumeroEscenario() + " / " + gestorEscenarios.cantidad());
        System.out.println("Archivo: " + escenario.getNombre());
        System.out.println("Alcance de zombis: " + getAlcanceZombieActual());
        System.out.println("Zombis generados: " + zombies.size());
    }

    private void guardarPuntosInicioEscenario() {
        puntosInicioEscenario.clear();

        for (Jugador jugador : jugadores) {
            puntosInicioEscenario.put(jugador.getId(), jugador.getPuntos());
        }
    }

    private void restaurarJugadoresAlInicioEscenario() {
        List<Jugador> restaurados = new ArrayList<>();

        for (Jugador original : jugadores) {
            Jugador nuevo;

            if (original instanceof Superviviente) {
                nuevo = new Superviviente(original.getId(), original.getNombre(), null);

            } else if (original instanceof Francotirador) {
                nuevo = new Francotirador(original.getId(), original.getNombre());

            } else {
                continue;
            }

            int puntos = puntosInicioEscenario.getOrDefault(original.getId(), 0);

            if (puntos > 0) {
                nuevo.sumarPuntos(puntos);
            }

            if (original.getEstado() == EstadoJugador.DESCONECTADO) {
                nuevo.setEstado(EstadoJugador.DESCONECTADO);
            } else {
                nuevo.setEstado(EstadoJugador.ACTIVO);
            }

            restaurados.add(nuevo);
        }

        jugadores.clear();
        jugadores.addAll(restaurados);
        jugadores.sort(Comparator.comparingInt(Jugador::getId));
    }

    private List<Superviviente> obtenerSupervivientesConectados() {
        List<Superviviente> supervivientes = new ArrayList<>();

        for (Jugador jugador : jugadores) {
            if (jugador instanceof Superviviente && jugador.getEstado() != EstadoJugador.DESCONECTADO) {
                supervivientes.add((Superviviente) jugador);
            }
        }

        return supervivientes;
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
        int alcanceActual = getAlcanceZombieActual();

        for (int indice = 0; indice < cantidad; indice++) {
            Posicion posicion = candidatas.get(indice);
            Direccion direccion = Direccion.values()[azar.nextInt(Direccion.values().length)];

            zombies.add(new Zombie(indice + 1, posicion, direccion, alcanceActual));
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

                if (escenario.esSalida(posicion)) {
                    continue;
                }

                if (posicion.equals(escenario.getEntrada())) {
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

                if (estaEnLineaConSuperviviente(posicion)) {
                    continue;
                }

                posiciones.add(posicion);
            }
        }

        return posiciones;
    }

    private boolean estaLejosDeJugadores(Posicion posicion) {
        for (Jugador jugador : jugadores) {
            if (!(jugador instanceof Superviviente)) {
                continue;
            }

            if (jugador.getEstado() != EstadoJugador.ACTIVO || jugador.getPosicion() == null) {
                continue;
            }

            int distanciaFila = Math.abs(posicion.getFila() - jugador.getPosicion().getFila());
            int distanciaColumna = Math.abs(posicion.getColumna() - jugador.getPosicion().getColumna());

            if (distanciaFila + distanciaColumna < DISTANCIA_SEGURA_INICIAL) {
                return false;
            }
        }

        return true;
    }

    private boolean estaEnLineaConSuperviviente(Posicion posicion) {
        for (Jugador jugador : jugadores) {
            if (!(jugador instanceof Superviviente)) {
                continue;
            }

            if (jugador.getEstado() != EstadoJugador.ACTIVO || jugador.getPosicion() == null) {
                continue;
            }

            Posicion posicionJugador = jugador.getPosicion();

            if (posicionJugador.getFila() == posicion.getFila()) {
                return true;
            }

            if (posicionJugador.getColumna() == posicion.getColumna()) {
                return true;
            }
        }

        return false;
    }

    public synchronized boolean mover(int idJugador, Direccion direccion) {
        if (!estaJugando() || direccion == null) {
            return false;
        }

        Jugador jugador = buscarJugador(idJugador);

        if (!(jugador instanceof Superviviente) || !jugador.puedeActuar() || jugador.getPosicion() == null) {
            return false;
        }

        Posicion destino = jugador.getPosicion().siguiente(direccion);

        if (!escenario.dentro(destino) || !escenario.esTransitable(destino) || estaOcupadaPorJugador(destino, idJugador)) {
            return false;
        }

        jugador.setPosicion(destino);

        Superviviente superviviente = (Superviviente) jugador;

        superviviente.setOculto(escenario.esEscondite(destino));

        if (escenario.esSalida(destino)) {
            registrarEscape(superviviente);
        }

        if (estaJugando()) {
            evaluarDetecciones();
        }

        return true;
    }

    public synchronized boolean moverCamara(int idJugador, Direccion direccion) {
        if (!estaJugando() || direccion == null) {
            return false;
        }

        Jugador jugador = buscarJugador(idJugador);

        if (!(jugador instanceof Francotirador)) {
            return false;
        }

        Francotirador francotirador = (Francotirador) jugador;

        if (!francotirador.puedeActuar() || francotirador.getCentroVista() == null) {
            return false;
        }

        Posicion destino = francotirador.getCentroVista().siguiente(direccion);

        if (!escenario.dentro(destino)) {
            return false;
        }

        francotirador.setCentroVista(destino);

        return true;
    }

    public synchronized boolean disparar(int idJugador, int fila, int columna) {
        if (!estaJugando()) {
            return false;
        }

        Jugador jugador = buscarJugador(idJugador);

        if (!(jugador instanceof Francotirador)) {
            return false;
        }

        Francotirador francotirador = (Francotirador) jugador;

        if (!francotirador.puedeActuar() || !francotirador.tieneBalas()) {
            return false;
        }

        Posicion objetivo = new Posicion(fila, columna);

        if (!escenario.dentro(objetivo)) {
            return false;
        }

        if (!francotirador.puedeVer(objetivo)) {
            return false;
        }

        francotirador.consumirBala();

        Zombie zombie = buscarZombieVivo(objetivo);

        if (zombie == null) {
            return true;
        }

        if (azar.nextDouble() < Francotirador.PROBABILIDAD_FALLO) {
            return true;
        }

        zombie.morir();
        francotirador.sumarPuntos(Francotirador.PUNTOS_POR_ZOMBIE);

        return true;
    }

    private Zombie buscarZombieVivo(Posicion posicion) {
        for (Zombie zombie : zombies) {
            if (zombie.isVivo() && zombie.getPosicion() != null && zombie.getPosicion().equals(posicion)) {
                return zombie;
            }
        }

        return null;
    }

    private void registrarEscape(Superviviente superviviente) {
        if (superviviente == null || superviviente.getEstado() != EstadoJugador.ACTIVO) {
            return;
        }

        ordenEscape++;

        long tiempoEscapeMs = Math.max(0, System.currentTimeMillis() - inicioEscenarioMs);
        int segundosRestantes = (int) (Math.max(0L, getTiempoRestanteMs()) / 1000L);
        int cantidadSupervivientes = contarSupervivientesParticipantes();
        int bonoOrden = Math.max(1, cantidadSupervivientes - ordenEscape + 1);
        int puntosEscape = segundosRestantes + bonoOrden;

        superviviente.escapar(tiempoEscapeMs);
        superviviente.sumarPuntos(puntosEscape);

        System.out.println("Escape: " + superviviente.getNombre());
        System.out.println("Orden: " + ordenEscape);
        System.out.println("Puntos obtenidos: " + puntosEscape);

        verificarFinEscenario();
    }

    private int contarSupervivientesConectados() {
        int cantidad = 0;

        for (Jugador jugador : jugadores) {
            if (jugador instanceof Superviviente && jugador.getEstado() != EstadoJugador.DESCONECTADO) {
                cantidad++;
            }
        }

        return cantidad;
    }

    private int contarSupervivientesParticipantes() {
        int cantidad = 0;

        for (Jugador jugador : jugadores) {
            if (jugador instanceof Superviviente) {
                cantidad++;
            }
        }

        return cantidad;
    }

    private boolean haySupervivientes() {
        for (Jugador jugador : jugadores) {
            if (jugador instanceof Superviviente) {
                return true;
            }
        }

        return false;
    }

    private boolean haySupervivientesActivos() {
        for (Jugador jugador : jugadores) {
            if (jugador instanceof Superviviente && jugador.getEstado() == EstadoJugador.ACTIVO) {
                return true;
            }
        }

        return false;
    }

    private boolean todosLosSupervivientesEscaparon() {
        boolean haySupervivientes = false;

        for (Jugador jugador : jugadores) {
            if (!(jugador instanceof Superviviente)) {
                continue;
            }

            haySupervivientes = true;

            if (jugador.getEstado() != EstadoJugador.ESCAPADO) {
                return false;
            }
        }

        return haySupervivientes;
    }

    private void verificarFinEscenario() {
        if (!estaJugando()) {
            return;
        }

        if (!haySupervivientes()) {
            estado = EstadoJuego.DERROTA;
            System.out.println("DERROTA: no existen supervivientes");
            return;
        }

        if (haySupervivientesActivos()) {
            return;
        }

        if (derrotaEquipo) {
            estado = EstadoJuego.DERROTA;
            System.out.println("DERROTA: hubo una baja o desconexion");
            return;
        }

        if (!todosLosSupervivientesEscaparon()) {
            estado = EstadoJuego.DERROTA;
            System.out.println("DERROTA: no todos los supervivientes escaparon");
            return;
        }

        estado = EstadoJuego.NIVEL_SUPERADO;

        System.out.println("NIVEL SUPERADO: escenario " + getNumeroEscenario());

        avanzarEscenario();
    }

    private boolean avanzarEscenario() {
        if (indiceEscenario + 1 >= gestorEscenarios.cantidad()) {
            estado = EstadoJuego.VICTORIA;

            System.out.println("VICTORIA: todos los escenarios fueron superados");

            return false;
        }

        indiceEscenario++;

        try {
            prepararEscenario();
            return true;

        } catch (IOException | IllegalStateException error) {
            estado = EstadoJuego.DERROTA;

            System.err.println("DERROTA: no se pudo cargar el siguiente escenario");
            System.err.println(error.getMessage());

            return false;
        }
    }

    public synchronized boolean actualizar(long ahoraMs) {
        if (!estaJugando()) {
            return false;
        }

        boolean huboCambio = false;

        long segundoActual = calcularSegundoVisible(getTiempoRestanteMs());

        if (segundoActual != ultimoSegundoNotificado) {
            ultimoSegundoNotificado = segundoActual;
            huboCambio = true;
        }

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

    private long calcularSegundoVisible(long tiempoMs) {
        if (tiempoMs >= 0) {
            return tiempoMs / 1000L;
        }

        return -((Math.abs(tiempoMs) + 999L) / 1000L);
    }

    private boolean evaluarDetecciones() {
        if (!estaJugando()) {
            return false;
        }

        boolean huboCambio = false;

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
                    superviviente.setOculto(false);

                    derrotaEquipo = true;
                    huboCambio = true;

                    System.out.println("Superviviente eliminado: " + superviviente.getNombre());
                    System.out.println("El jugador pasa a ESPECTADOR");

                    verificarFinEscenario();
                }
            }
        }

        return huboCambio;
    }

    private boolean estaOcupadaPorJugador(Posicion posicion) {
        return estaOcupadaPorJugador(posicion, -1);
    }

    private boolean estaOcupadaPorJugador(Posicion posicion, int idJugadorIgnorado) {
        for (Jugador jugador : jugadores) {
            if (jugador.getId() == idJugadorIgnorado || !jugador.isActivo() || jugador.getPosicion() == null) {
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

        if (jugador instanceof Superviviente) {
            if (jugador.getEstado() == EstadoJugador.ACTIVO) {
                jugador.setEstado(EstadoJugador.DESCONECTADO);
                derrotaEquipo = true;

                System.out.println("Superviviente desconectado durante la partida: " + jugador.getNombre());

            } else if (jugador.getEstado() == EstadoJugador.ESPECTADOR) {
                jugador.setEstado(EstadoJugador.DESCONECTADO);
            }

        } else {
            jugador.setEstado(EstadoJugador.DESCONECTADO);
        }

        if (estaJugando()) {
            verificarFinEscenario();
        }
    }

    private Jugador buscarJugador(int idJugador) {
        for (Jugador jugador : jugadores) {
            if (jugador.getId() == idJugador) {
                return jugador;
            }
        }

        return null;
    }

    public synchronized List<Jugador> obtenerRanking() {
        List<Jugador> ranking = new ArrayList<>(jugadores);

        ranking.sort(
                Comparator.comparingInt(Jugador::getPuntos)
                        .reversed()
                        .thenComparingInt(Jugador::getId)
        );

        return ranking;
    }

    public synchronized long getTiempoRestanteMs() {
        if (inicioEscenarioMs <= 0) {
            return 0;
        }

        return DURACION_ESCENARIO_MS - (System.currentTimeMillis() - inicioEscenarioMs);
    }

    public synchronized EstadoPartida crearEstado() {
        return new EstadoPartida(
                escenario.getMapa(),
                jugadores,
                zombies,
                estado,
                getTiempoRestanteMs(),
                derrotaEquipo,
                getNumeroEscenario(),
                gestorEscenarios.cantidad(),
                obtenerRanking()
        );
    }

    public synchronized int getCantidadJugadores() {
        return jugadores.size();
    }

    public synchronized int getNumeroEscenario() {
        return indiceEscenario + 1;
    }

    public synchronized int getCantidadEscenarios() {
        return gestorEscenarios.cantidad();
    }

    public synchronized int getAlcanceZombieActual() {
        return ALCANCE_ZOMBIE_BASE + indiceEscenario;
    }

    public synchronized String getFase() {
        switch (estado) {
            case ESPERA:
                return EstadoPartida.ESPERANDO;

            case EN_CURSO:
                return EstadoPartida.JUGANDO;

            case NIVEL_SUPERADO:
                return EstadoPartida.NIVEL_SUPERADO;

            case VICTORIA:
                return EstadoPartida.VICTORIA;

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

    public synchronized boolean isDerrotaEquipo() {
        return derrotaEquipo;
    }

    public Escenario getEscenario() {
        return escenario;
    }
}