package structures;

import java.io.Serializable;
import java.text.Collator;

/**
 * Árbol AVL con claves String (cada nodo guarda únicamente la palabra clave y
 * enlaces a los hijos). No almacena listas de ids dentro de los nodos.
 */
public class AVLTree implements Serializable {

    private static final long serialVersionUID = 1L;

    private static class Node implements Serializable {

        String key;
        Node left, right;
        int height;

        Node(String k) {
            key = k;
            height = 1;
        }
    }

    /**
     * Raíz del árbol.
     */
    private Node root;
    private transient Collator collator;

    /**
     * Crea un árbol AVL vacío para claves de tipo String.
     */
    public AVLTree() {
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
    }

    private int height(Node n) {
        return n == null ? 0 : n.height;
    }

    private int balanceFactor(Node n) {
        return n == null ? 0 : height(n.left) - height(n.right);
    }

    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T2 = x.right;
        x.right = y;
        y.left = T2;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T2 = y.left;
        y.left = x;
        x.right = T2;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        return y;
    }

    private int cmp(String a, String b) {
        if (collator == null) {
            collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);
        }
        return collator.compare(a, b);
    }

    /**
     * Inserta la clave en el árbol si no existe.
     *
     * @param key clave a insertar (no nula)
     */
    public void insert(String key) {
        if (key == null) {
            return;
        }
        root = insert(root, key);
    }

    /**
     * Inserta una clave en el árbol (operación pública, sin duplicados).
     *
     * @param key clave a insertar (no nula)
     */
    private Node insert(Node node, String key) {
        if (node == null) {
            return new Node(key);
        }
        int c = cmp(key, node.key);
        if (c < 0) {
            node.left = insert(node.left, key);
        } else if (c > 0) {
            node.right = insert(node.right, key);
        } else {
            return node; // ya existe

        }
        node.height = 1 + Math.max(height(node.left), height(node.right));
        int balance = balanceFactor(node);
        if (balance > 1 && cmp(key, node.left.key) < 0) {
            return rotateRight(node);
        }
        if (balance < -1 && cmp(key, node.right.key) > 0) {
            return rotateLeft(node);
        }
        if (balance > 1 && cmp(key, node.left.key) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }
        if (balance < -1 && cmp(key, node.right.key) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }
        return node;
    }

    /**
     * Busca si la clave existe en el árbol.
     *
     * @param key clave a buscar
     * @return true si la clave existe en el árbol
     */
    public boolean contains(String key) {
        Node n = root;
        while (n != null) {
            int c = cmp(key, n.key);
            if (c == 0) {
                return true;
            }
            n = c < 0 ? n.left : n.right;
        }
        return false;
    }

    /**
     * Devuelve las claves del árbol en orden ascendente según el Collator
     * configurado (orden lingüístico sensible a acentos y mayúsculas según
     * configuración del Collator).
     *
     * @return arreglo con las claves en orden
     */
    public String[] inorderKeys() {
        SimpleList out = new SimpleList();
        inorder(root, out);
        return out.toArray();
    }

    private void inorder(Node n, SimpleList out) {
        if (n == null) {
            return;
        }
        inorder(n.left, out);
        out.add(n.key);
        inorder(n.right, out);
    }
}
