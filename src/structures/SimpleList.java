package structures;

import java.io.Serializable;

/**
 * Lista dinámica simple para almacenar Strings (p. ej. ids de artículos).
 */
public class SimpleList implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Arreglo interno que guarda los elementos.
     */
    private String[] data;

    /**
     * Número de elementos en la lista.
     */
    private int size;

    /**
     * Crea una lista simple vacía.
     */
    public SimpleList() {
        this.data = new String[8];
        this.size = 0;
    }

    /**
     * Añade un elemento a la lista si no es nulo y no está ya presente.
     *
     * @param s cadena a añadir
     */
    public void add(String s) {
        if (s == null) {
            return;
        }
        if (contains(s)) {
            return; // evitar duplicados

        }
        if (size == data.length) {
            String[] n = new String[data.length * 2];
            System.arraycopy(data, 0, n, 0, data.length);
            data = n;
        }
        data[size++] = s;
    }

    /**
     * Comprueba si la lista contiene la cadena indicada.
     *
     * @param s cadena a buscar
     * @return true si está presente
     */
    public boolean contains(String s) {
        if (s == null) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (s.equals(data[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Devuelve el número de elementos en la lista.
     *
     * @return tamaño de la lista
     */
    public int size() {
        return size;
    }

    /**
     * Convierte la lista a un arreglo de Strings con el tamaño exacto.
     *
     * @return arreglo con los elementos de la lista
     */
    public String[] toArray() {
        String[] r = new String[size];
        System.arraycopy(data, 0, r, 0, size);
        return r;
    }
}
