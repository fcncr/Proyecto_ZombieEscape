package Models;

import java.io.Serializable;

public class Zombie implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final long INTERVALO_MOVIMIENTO_MS = 600L;

    private final int id;
    private Posicion posicion;
    private Direccion direccion;
    private final int alcance;
    private boolean vivo;
    private long ultimoMovimientoMs;

    public Zombie(int id, Posicion posicion, Direccion direccion, int alcance) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del zombie debe ser positivo");
        }

        if (posicion == null) {
            throw new IllegalArgumentException("La posicion del zombie no puede ser nula");
        }

        if (direccion == null) {
            throw new IllegalArgumentException("La direccion del zombie no puede ser nula");
        }

        if (alcance <= 0) {
            throw new IllegalArgumentException("El alcance del zombie debe ser positivo");
        }

        this.id = id;
        this.posicion = posicion;
        this.direccion = direccion;
        this.alcance = alcance;
        this.vivo = true;
        this.ultimoMovimientoMs = 0;
    }

    public void patrullar(Escenario escenario, long ahoraMs) {
        if (!vivo || escenario == null) {
            return;
        }

        if (ahoraMs - ultimoMovimientoMs < INTERVALO_MOVIMIENTO_MS) {
            return;
        }

        ultimoMovimientoMs = ahoraMs;

        for (int intento = 0; intento < Direccion.values().length; intento++) {
            Posicion destino = posicion.siguiente(direccion);

            if (escenario.esTransitable(destino)) {
                posicion = destino;
                return;
            }

            girar();
        }
    }

    public boolean detecta(Superviviente superviviente, Escenario escenario) {
        if (!vivo || superviviente == null || escenario == null) {
            return false;
        }

        if (!superviviente.puedeActuar() || superviviente.isOculto() || superviviente.getPosicion() == null) {
            return false;
        }

        Posicion destino = superviviente.getPosicion();

        if (posicion.equals(destino)) {
            return true;
        }

        int diferenciaFila = destino.getFila() - posicion.getFila();
        int diferenciaColumna = destino.getColumna() - posicion.getColumna();
        int distancia;

        switch (direccion) {
            case ARRIBA:
                if (diferenciaColumna != 0 || diferenciaFila >= 0) {
                    return false;
                }

                distancia = -diferenciaFila;
                break;

            case ABAJO:
                if (diferenciaColumna != 0 || diferenciaFila <= 0) {
                    return false;
                }

                distancia = diferenciaFila;
                break;

            case IZQUIERDA:
                if (diferenciaFila != 0 || diferenciaColumna >= 0) {
                    return false;
                }

                distancia = -diferenciaColumna;
                break;

            case DERECHA:
                if (diferenciaFila != 0 || diferenciaColumna <= 0) {
                    return false;
                }

                distancia = diferenciaColumna;
                break;

            default:
                return false;
        }

        if (distancia > alcance) {
            return false;
        }

        return escenario.hayVision(posicion, destino);
    }

    public void girar() {
        switch (direccion) {
            case ARRIBA:
                direccion = Direccion.DERECHA;
                break;

            case DERECHA:
                direccion = Direccion.ABAJO;
                break;

            case ABAJO:
                direccion = Direccion.IZQUIERDA;
                break;

            case IZQUIERDA:
                direccion = Direccion.ARRIBA;
                break;
        }
    }

    public void morir() {
        vivo = false;
    }

    public int getId() {
        return id;
    }

    public Posicion getPosicion() {
        return posicion;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public int getAlcance() {
        return alcance;
    }

    public boolean isVivo() {
        return vivo;
    }

    public long getUltimoMovimientoMs() {
        return ultimoMovimientoMs;
    }

    @Override
    public String toString() {
        return "Zombie " + id + " - " + posicion + " - " + direccion;
    }
}