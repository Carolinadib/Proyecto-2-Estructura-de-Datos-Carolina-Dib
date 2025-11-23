package structures;

import java.io.Serializable;

/**
 * Tabla hash simple que mapea una palabra clave (String) a una SimpleList de
 * ids.
 */
public class HashTableKeywords implements Serializable {

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
     * Crea una tabla hash para mapear palabras clave a listas de ids.
     */
    public HashTableKeywords() {
        this.capacity = 97;
        this.table = new Entry[capacity];
    }

    private int index(String key) {
        int h = key == null ? 0 : Math.abs(key.hashCode());
        return h % capacity;
    }

    /**
     * Asocia una palabra clave con una lista de ids.
     *
     * @param key palabra clave
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
     * Recupera la lista asociada a una palabra clave.
     *
     * @param key palabra clave
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
     * Comprueba si la palabra clave está presente en la tabla.
     *
     * @param key palabra clave
     * @return true si existe
     */
    public boolean containsKey(String key) {
        return get(key) != null;
    }

    /**
     * Devuelve todas las palabras clave registradas.
     *
     * @return arreglo de palabras clave
     */
    public String[] keys() {
        // not optimized
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

