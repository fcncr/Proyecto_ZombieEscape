package Servidor;

public class ThreadPartida extends Thread {

    private static final long INTERVALO_CICLO_MS = 50L;
    private static final long INTERVALO_ESTADO_MS = 1000L;

    private volatile boolean ejecutando;
    private final ChatServer chatServer;
    private final Partida partida;

    public ThreadPartida(ChatServer chatServer, Partida partida) {
        this.chatServer = chatServer;
        this.partida = partida;
        this.ejecutando = true;
        setName("ThreadPartida");
    }

    @Override
    public void run() {
        long ultimoEnvioEstadoMs = 0;

        while (ejecutando && chatServer.isEjecutando() && partida.estaJugando()) {
            long ahoraMs = System.currentTimeMillis();
            boolean huboCambio = partida.actualizar(ahoraMs);

            if (huboCambio || ahoraMs - ultimoEnvioEstadoMs >= INTERVALO_ESTADO_MS) {
                chatServer.enviarEstado();
                ultimoEnvioEstadoMs = ahoraMs;
            }

            try {
                Thread.sleep(INTERVALO_CICLO_MS);

            } catch (InterruptedException error) {
                if (!ejecutando) {
                    break;
                }
            }
        }
    }

    public void detener() {
        ejecutando = false;
        interrupt();
    }
}