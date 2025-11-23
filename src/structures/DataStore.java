package structures;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Contenedor de las estructuras principales y métodos de persistencia.
 */
public class DataStore implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Tabla de artículos indexada por título.
     */
    public HashTableArticles articles;

    /**
     * AVL con las palabras clave del repositorio.
     */
    public AVLTree keywordsAVL;

    /**
     * AVL con los autores registrados.
     */
    public AVLTree authorsAVL;

    /**
     * Mapa de palabra clave -> lista de ids.
     */
    public HashTableKeywords keywordMap;

    /**
     * Mapa de autor -> lista de ids.
     */
    public HashTableAuthors authorMap;

    /**
     * Crea un DataStore vacío con las estructuras inicializadas.
     */
    public DataStore() {
        articles = new HashTableArticles();
        keywordsAVL = new AVLTree();
        authorsAVL = new AVLTree();
        keywordMap = new HashTableKeywords();
        authorMap = new HashTableAuthors();
    }

    /**
     * Persiste el DataStore en el archivo indicado usando serialización.
     *
     * @param f archivo destino
     * @throws Exception en caso de error de E/S
     */
    public void saveToFile(File f) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        }
    }

    /**
     * Carga un DataStore desde un archivo serializado.
     *
     * @param f archivo fuente
     * @return DataStore cargado
     * @throws Exception en caso de error de E/S o formato
     */
    public static DataStore loadFromFile(File f) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof DataStore) {
                return (DataStore) obj;
            }
            throw new IllegalStateException("El archivo no contiene un DataStore");
        }
    }
}
