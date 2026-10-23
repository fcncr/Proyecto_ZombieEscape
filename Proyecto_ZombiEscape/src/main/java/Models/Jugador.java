package Models;

import java.io.Serializable;

public class Jugador implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String nombre;
    private Posicion posicion;
    private int puntos;
    private EstadoJugador estado;
    private int plazaVisual;

    public Jugador(int id, String nombre, Posicion posicion) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser positivo");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }

        this.id = id;
        this.nombre = nombre.trim();
        this.posicion = posicion;
        this.puntos = 0;
        this.estado = EstadoJugador.ACTIVO;
        this.plazaVisual = -1;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Posicion getPosicion() {
        return posicion;
    }

    public void setPosicion(Posicion posicion) {
        if (posicion == null) {
            throw new IllegalArgumentException("La posicion no puede ser nula");
        }

        this.posicion = posicion;
    }

    public int getPuntos() {
        return puntos;
    }

    public void sumarPuntos(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("Los puntos no pueden ser negativos");
        }

        puntos += cantidad;
    }

    public EstadoJugador getEstado() {
        return estado;
    }

    public void setEstado(EstadoJugador estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado del jugador no puede ser nulo");
        }

        this.estado = estado;
    }

    public int getPlazaVisual() {
        return plazaVisual;
    }

    public void setPlazaVisual(int plazaVisual) {
        if (plazaVisual < -1 || plazaVisual > 9) {
            throw new IllegalArgumentException("La plaza visual debe estar entre 0 y 9");
        }

        this.plazaVisual = plazaVisual;
    }

    public boolean isActivo() {
        return estado == EstadoJugador.ACTIVO;
    }

    public void setActivo(boolean activo) {
        estado = activo ? EstadoJugador.ACTIVO : EstadoJugador.DESCONECTADO;
    }

    public boolean puedeActuar() {
        return estado == EstadoJugador.ACTIVO;
    }

    public void morir() {
        estado = EstadoJugador.ESPECTADOR;
    }

    @Override
    public String toString() {
        return "Jugador " + id + ": " + nombre + " - " + posicion + " - " + estado;
    }
}