package Models;

import java.io.Serializable;

public class Mensaje implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String UNIRSE = "UNIRSE";
    public static final String BIENVENIDO = "BIENVENIDO";
    public static final String ESTADO = "ESTADO";
    public static final String MOVER = "MOVER";
    public static final String MOVER_CAMARA = "MOVER_CAMARA";
    public static final String DISPARAR = "DISPARAR";
    public static final String CHAT = "CHAT";
    public static final String ERROR = "ERROR";

    public static final String ROL_SUPERVIVIENTE = "SUPERVIVIENTE";
    public static final String ROL_FRANCOTIRADOR = "FRANCOTIRADOR";

    private final String tipo;
    private final String[] parametros;
    private EstadoPartida estado;

    public Mensaje(String tipo, int cantidadParametros) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de mensaje no puede estar vacio");
        }

        if (cantidadParametros < 0) {
            throw new IllegalArgumentException("La cantidad de parametros no puede ser negativa");
        }

        this.tipo = tipo;
        this.parametros = new String[cantidadParametros];
        this.estado = null;
    }

    public Mensaje(String tipo, String... parametros) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de mensaje no puede estar vacio");
        }

        this.tipo = tipo;
        this.parametros = parametros == null ? new String[0] : parametros.clone();
        this.estado = null;
    }

    public String getTipo() {
        return tipo;
    }

    public String[] getParametros() {
        return parametros.clone();
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        StringBuilder resultado = new StringBuilder(tipo);

        for (String parametro : parametros) {
            resultado.append(" ").append(parametro);
        }

        return resultado.toString();
    }
}