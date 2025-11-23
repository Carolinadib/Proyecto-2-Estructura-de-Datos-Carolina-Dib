package model;

import java.io.Serializable;
import java.security.SecureRandom;

/**
 * Representa un artículo/ resumen de investigación.
 */
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único del artículo.
     */
    private final String id;

    /**
     * Título del artículo.
     */
    private final String title;

    /**
     * Arreglo de autores (puede ser vacío).
     */
    private final String[] authors;

    /**
     * Cuerpo o resumen del artículo.
     */
    private final String body;

    /**
     * Palabras clave asociadas al artículo.
     */
    private final String[] keywords;

    // Generador de identificadores simple y seguro (evita java.util.UUID)
    private static final SecureRandom RNG = new SecureRandom();

    /**
     * Crea un nuevo artículo. El id se genera de forma local usando tiempo +
     * número aleatorio seguro.
     *
     * @param title título del artículo
     * @param authors arreglo de autores (puede ser null)
     * @param body cuerpo/resumen (puede ser null)
     * @param keywords palabras clave (puede ser null)
     */
    public Article(String title, String[] authors, String body, String[] keywords) {
        this.id = generateId();
        this.title = title;
        this.authors = authors != null ? authors : new String[0];
        this.body = body != null ? body : "";
        this.keywords = keywords != null ? keywords : new String[0];
    }

    /**
     * Genera un identificador único razonable sin usar java.util.UUID.
     */
    private static String generateId() {
        long t = System.currentTimeMillis();
        long r = RNG.nextLong();
        return Long.toHexString(t) + "-" + Long.toHexString(r);
    }

    /**
     * Devuelve el identificador único del artículo.
     *
     * @return id único como String
     */
    public String getId() {
        return id;
    }

    /**
     * Devuelve el título del artículo.
     *
     * @return título
     */
    public String getTitle() {
        return title;
    }

    /**
     * Devuelve el arreglo de autores. Puede ser un arreglo vacío si no hay
     * autores.
     *
     * @return arreglo de nombres de autores
     */
    public String[] getAuthors() {
        return authors;
    }

    /**
     * Devuelve el cuerpo o resumen del artículo.
     *
     * @return texto del resumen (cadena posiblemente vacía)
     */
    public String getBody() {
        return body;
    }

    /**
     * Devuelve las palabras clave asociadas al artículo.
     *
     * @return arreglo de keywords (posiblemente vacío)
     */
    public String[] getKeywords() {
        return keywords;
    }

    @Override
    public String toString() {
        return title;
    }
}
