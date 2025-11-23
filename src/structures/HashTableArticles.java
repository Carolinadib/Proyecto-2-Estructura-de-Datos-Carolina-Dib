package structures;

import java.io.Serializable;
import model.Article;

/**
 * Tabla de dispersión simple (separate chaining) para almacenar artículos por
 * título.
 */
public class HashTableArticles implements Serializable {

    private static final long serialVersionUID = 1L;

    private static class Entry implements Serializable {

        String key;
        Article value;
        Entry next;

        Entry(String k, Article v) {
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
     * Número actual de elementos almacenados.
     */
    private int size;

    /**
     * Crea una tabla hash para almacenar artículos indexados por título.
     */
    public HashTableArticles() {
        this.capacity = 97; // prime
        this.table = new Entry[capacity];
        this.size = 0;
    }

    private int index(String key) {
        if (key == null) {
            return 0;
        }
        // función hash personalizada (polynomial rolling-like)
        long h = 7;
        for (int i = 0; i < key.length(); i++) {
            h = (h * 31 + key.charAt(i)) % Integer.MAX_VALUE;
        }
        return (int) (Math.abs(h) % capacity);
    }

    /**
     * Inserta o reemplaza un artículo asociado a la clave dada.
     *
     * @param key título clave
     * @param value artículo a almacenar
     */
    public void put(String key, Article value) {
        int idx = index(key);
        Entry e = table[idx];
        while (e != null) {
            if (e.key.equals(key)) {
                e.value = value;
                return;
            }
            e = e.next;
        }
        Entry ne = new Entry(key, value);
        ne.next = table[idx];
        table[idx] = ne;
        size++;
    }

    /**
     * Recupera un artículo por su título. Si no se encuentra una clave exacta,
     * realiza una búsqueda tolerante (ignora mayúsculas y espacios alrededor).
     *
     * @param key título a buscar
     * @return Article o null si no existe
     */
    public Article get(String key) {
        if (key == null) {
            return null;
        }
        int idx = index(key);
        Entry e = table[idx];
        while (e != null) {
            if (e.key.equals(key)) {
                return e.value;
            }
            e = e.next;
        }
        // fallback: búsqueda tolerante a mayúsculas y espacios (si no encontró exacto)
        String nk = key.trim().toLowerCase();
        for (int i = 0; i < capacity; i++) {
            e = table[i];
            while (e != null) {
                if (e.key != null && e.key.trim().toLowerCase().equals(nk)) {
                    return e.value;
                }
                e = e.next;
            }
        }
        return null;
    }

    /**
     * Indica si existe un artículo con la clave dada.
     *
     * @param key título a comprobar
     * @return true si existe
     */
    public boolean containsKey(String key) {
        return get(key) != null;
    }

    /**
     * Devuelve las claves (títulos) almacenadas en la tabla.
     *
     * @return arreglo de títulos
     */
    public String[] keys() {
        String[] k = new String[size];
        int p = 0;
        for (int i = 0; i < capacity; i++) {
            Entry e = table[i];
            while (e != null) {
                k[p++] = e.key;
                e = e.next;
            }
        }
        return k;
    }

    /**
     * Devuelve los valores (artículos) almacenados en la tabla.
     *
     * @return arreglo de Article
     */
    public Article[] values() {
        Article[] v = new Article[size];
        int p = 0;
        for (int i = 0; i < capacity; i++) {
            Entry e = table[i];
            while (e != null) {
                v[p++] = e.value;
                e = e.next;
            }
        }
        return v;
    }

    /**
     * Número de elementos en la tabla.
     *
     * @return tamaño
     */
    public int size() {
        return size;
    }
}
