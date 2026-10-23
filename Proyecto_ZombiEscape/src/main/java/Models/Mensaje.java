
package Models;

import java.io.Serializable;
import java.util.Arrays;

public class Mensaje implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String UNIRSE = "UNIRSE";
    public static final String BIENVENIDO = "BIENVENIDO";
    public static final String ESTADO = "ESTADO";
    public static final String MOVER = "MOVER";
    public static final String CHAT = "CHAT";
    public static final String ERROR = "ERROR";

    private final String tipo;
    private final String[] parametros;
    private EstadoPartida estado;

    public Mensaje(String tipo, int cantidadParametros) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de mensaje no puede estar vacio");
        }

        if (cantidadParametros < 0) {
            throw new IllegalArgumentException("La cantidad de parametros no puede ser negativa");
        }

        this.tipo = tipo;
        this.parametros = new String[cantidadParametros];
    }

    public Mensaje(String tipo, String... parametros) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de mensaje no puede estar vacio");
        }

        this.tipo = tipo;
        this.parametros = parametros == null ? new String[0] : parametros.clone();
    }

    public String getTipo() {
        return tipo;
    }

    public String[] getParametros() {
        return parametros.clone();
    }

    public String getParametro(int indice) {
        if (indice < 0 || indice >= parametros.length) {
            throw new IllegalArgumentException("Indice de parametro invalido");
        }

        return parametros[indice];
    }

    public void setParametro(int indice, String valor) {
        if (indice < 0 || indice >= parametros.length) {
            throw new IllegalArgumentException("Indice de parametro invalido");
        }

        parametros[indice] = valor;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Mensaje{tipo=" + tipo + ", parametros=" + Arrays.toString(parametros) + "}";
    }
}
