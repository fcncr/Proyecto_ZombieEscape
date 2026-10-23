
package Models;

import java.io.Serializable;
import java.util.Objects;

public class Posicion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int fila;
    private final int columna;

    public Posicion(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    public Posicion siguiente(Direccion direccion) {
        Objects.requireNonNull(direccion, "La direccion no puede ser nula");
        return new Posicion(fila + direccion.getCambioFila(), columna + direccion.getCambioColumna());
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Posicion)) {
            return false;
        }

        Posicion otra = (Posicion) objeto;
        return fila == otra.fila && columna == otra.columna;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fila, columna);
    }

    @Override
    public String toString() {
        return "(" + fila + ", " + columna + ")";
    }
}
