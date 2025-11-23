package structures;

import java.io.Serializable;

/**
 * Tabla hash simple que mapea un autor (String) a una SimpleList de ids.
 */
public class HashTableAuthors implements Serializable {

    private static final long serialVersionUID = 1L;

    private static class Entry implements Serializable {

        String key;
        SimpleList value;
        Entry next;

        Entry(String k, SimpleList v) {
            key = k;
            value = v;
        }
    }

    /**
     * Tabla interna de entradas (separate chaining).
     */
    private Entry[] table;

    /**
     * Capacidad de la tabla (número de cubetas).
     */
    private int capacity;

    /**
     * Crea una tabla hash para mapear autores a listas de ids.
     */
    public HashTableAuthors() {
        this.capacity = 97;
        this.table = new Entry[capacity];
    }

    private int index(String key) {
        if (key == null) {
            return 0;
        }
        // simple custom hash: sum of chars times a multiplier
        long h = 7;
        for (int i = 0; i < key.length(); i++) {
            h = (h * 31 + key.charAt(i)) % Integer.MAX_VALUE;
        }
        return (int) (Math.abs(h) % capacity);
    }

    /**
     * Asocia un autor con una lista de ids de artículos.
     *
     * @param key autor
     * @param list lista de ids
     */
    public void put(String key, SimpleList list) {
        int idx = index(key);
        Entry e = table[idx];
        while (e != null) {
            if (e.key.equals(key)) {
                e.value = list;
                return;
            }
            e = e.next;
        }
        Entry ne = new Entry(key, list);
        ne.next = table[idx];
        table[idx] = ne;
    }

    /**
     * Recupera la lista asociada a un autor.
     *
     * @param key autor
     * @return SimpleList o null si no existe
     */
    public SimpleList get(String key) {
        int idx = index(key);
        Entry e = table[idx];
        while (e != null) {
            if (e.key.equals(key)) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }

    /**
     * Comprueba si el autor está presente en la tabla.
     *
     * @param key autor
     * @return true si existe
     */
    public boolean containsKey(String key) {
        return get(key) != null;
    }

    /**
     * Devuelve todos los autores registrados.
     *
     * @return arreglo de autores
     */
    public String[] keys() {
        SimpleList tmp = new SimpleList();
        for (int i = 0; i < capacity; i++) {
            Entry e = table[i];
            while (e != null) {
                tmp.add(e.key);
                e = e.next;
            }
        }
        return tmp.toArray();
    }
}
