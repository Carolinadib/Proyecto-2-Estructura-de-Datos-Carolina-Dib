package app;

import model.Article;
import structures.AVLTree;
import structures.DataStore;
import structures.HashTableKeywords;
import structures.HashTableArticles;
import structures.SimpleList;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.Collator;

/**
 * Aplicación principal con interfaz Swing.
 *
 * Suposición de formato de archivo de resumen (si no se ajusta al anexo del
 * enunciado): Líneas al inicio con prefijos opcionales: Title: título Authors:
 * Autor1; Autor2 Keywords: kw1, kw2, kw3 (línea en blanco) (cuerpo del resumen)
 */
public class Proyecto2CarolinaDib {

    private static final String DATA_FILE = "mendeley_data.ser";
    private final JFrame frame;
    private DataStore store;

    /**
     * Crea la aplicación Mendeley (inicializa el datastore y la interfaz).
     */
    public Proyecto2CarolinaDib() {
        frame = new JFrame("Mendeley - Mini sistema");
        initStore();
        initUI();
    }

    private void initStore() {
        File f = new File(DATA_FILE);
        if (f.exists()) {
            // Preguntar al usuario si desea cargar datos persistidos para evitar
            // sorpresas en entornos de prueba donde hay un archivo previo.
            int opt = JOptionPane.showConfirmDialog(null, "Se encontró un archivo de datos (" + DATA_FILE + ").\n¿Desea cargar los datos guardados?\n(Si no, el repositorio empezará vacío)", "Cargar datos previos", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    store = DataStore.loadFromFile(f);
                } catch (Exception e) {
                    e.printStackTrace();
                    store = new DataStore();
                }
            } else {
                store = new DataStore();
            }
        } else {
            store = new DataStore();
        }
    }

    private void preload() {
        // Precargar un par de investigaciones de ejemplo
        addArticleInternal(new Article("Efecto de X en Y", new String[]{"María Pérez"}, "Este estudio analiza el efecto de X sobre Y. Se observó aumento en X.", new String[]{"X", "Y", "efecto"}));
        addArticleInternal(new Article("Análisis de datos con Z", new String[]{"Juan Guerrero", "María Pérez"}, "Se presentan métodos para analizar datos con Z. Z mostró rendimiento superior.", new String[]{"Z", "análisis", "datos"}));
    }

    private void initUI() {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("Archivo");
        JMenuItem add = new JMenuItem("Agregar resumen...");
        add.addActionListener(e -> addSummaryAction());
        JMenuItem analyze = new JMenuItem("Analizar resumen...");
        analyze.addActionListener(e -> analyzeAction());
        JMenuItem searchK = new JMenuItem("Buscar por palabra clave...");
        searchK.addActionListener(e -> searchByKeywordAction());
        JMenuItem searchA = new JMenuItem("Buscar por autor...");
        searchA.addActionListener(e -> searchByAuthorAction());
        JMenuItem listK = new JMenuItem("Lista de palabras clave");
        listK.addActionListener(e -> listKeywordsAction());
        JMenuItem loadExamples = new JMenuItem("Cargar ejemplos (recursos)");
        loadExamples.addActionListener(e -> loadExamplesFromRecursos());
        JMenuItem clearRepo = new JMenuItem("Limpiar repositorio");
        clearRepo.addActionListener(e -> clearRepository());
        JMenuItem exit = new JMenuItem("Salir");
        exit.addActionListener(e -> exitAndSave());
        menu.add(add);
        menu.add(analyze);
        menu.addSeparator();
        menu.add(searchK);
        menu.add(searchA);
        menu.add(listK);
        menu.add(loadExamples);
        menu.add(clearRepo);
        menu.addSeparator();
        menu.add(exit);
        mb.add(menu);
        frame.setJMenuBar(mb);

        JLabel welcome = new JLabel("Mendeley - administrador de resúmenes", SwingConstants.CENTER);
        frame.getContentPane().add(welcome, BorderLayout.CENTER);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitAndSave();
            }
        });

        frame.setVisible(true);
    }

    private void addSummaryAction() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar archivo de resumen");
        fc.setFileFilter(new FileNameExtensionFilter("Text files", "txt", "text"));
        int res = fc.showOpenDialog(frame);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                Article a = parseArticleFile(f);
                if (a == null) {
                    JOptionPane.showMessageDialog(frame, "No se pudo leer el archivo o formato inválido.");
                    return;
                }
                if (store.articles.containsKey(a.getTitle())) {
                    JOptionPane.showMessageDialog(frame, "El resumen ya existe (mismo título). No se añadirá.");
                    return;
                }
                addArticleInternal(a);
                JOptionPane.showMessageDialog(frame, "Resumen agregado: " + a.getTitle());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error al leer el archivo: " + ex.getMessage());
            }
        }
    }

    private Article parseArticleFile(File f) throws Exception {
        // Leer todo el archivo en memoria y luego analizar por secciones para
        // permitir que la línea de "Palabras claves" esté al final del archivo.
        // Usamos un arreglo dinámico local en lugar de java.util.List.
        String[] lines = new String[256];
        int linesSize = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (linesSize == lines.length) {
                    String[] n = new String[lines.length * 2];
                    System.arraycopy(lines, 0, n, 0, lines.length);
                    lines = n;
                }
                lines[linesSize++] = l;
            }
        }

        if (linesSize == 0) {
            return null;
        }

        // índice y detección de secciones
        int n = linesSize;
        int idxResumen = -1;
        int idxAutores = -1;
        int idxPalabras = -1;
        for (int i = 0; i < n; i++) {
            String t = lines[i].trim();
            String lo = t.toLowerCase();
            if (idxResumen == -1 && lo.equals("resumen")) {
                idxResumen = i;
            }
            if (idxAutores == -1 && lo.equals("autores")) {
                idxAutores = i;
            }
            if (idxPalabras == -1 && lo.startsWith("palabras")) {
                idxPalabras = i;
            }
        }

        // Título: primera línea no vacía antes de 'Autores' o 'Resumen'
        String title = null;
        for (int i = 0; i < n; i++) {
            String t = lines[i].trim();
            if (t.isEmpty()) {
                continue;
            }
            // si la línea es un encabezado, saltarla
            String lo = t.toLowerCase();
            if (lo.equals("autores") || lo.equals("resumen") || lo.startsWith("palabras")) {
                continue;
            }
            title = t;
            break;
        }

        // Autores: si hay encabezado 'Autores', tomar las líneas siguientes hasta 'Resumen' o 'Palabras' o hasta línea vacía
        SimpleList authorsList = new SimpleList();
        if (idxAutores != -1) {
            int i = idxAutores + 1;
            while (i < n) {
                String t = lines[i].trim();
                String lo = t.toLowerCase();
                if (t.isEmpty() || lo.equals("resumen") || lo.startsWith("palabras")) {
                    break;
                }
                authorsList.add(t);
                i++;
            }
        } else {
            // si no hay encabezado, intentar inferir autores entre título y 'Resumen' o 'Palabras'
            int start = -1;
            for (int i = 0; i < n; i++) {
                if (lines[i].trim().equals(title)) {
                    start = i + 1;
                    break;
                }
            }
            if (start != -1) {
                int i = start;
                while (i < n) {
                    String t = lines[i].trim();
                    String lo = t.toLowerCase();
                    if (t.isEmpty() || lo.equals("resumen") || lo.startsWith("palabras")) {
                        break;
                    }
                    // si la línea contiene separadores, asumir lista de autores en una línea
                    if (t.contains(",") || t.contains(";")) {
                        String[] arr = t.split("[,;]\\s*");
                        for (String a : arr) {
                            if (!a.trim().isEmpty()) {
                                authorsList.add(a.trim());
                            }
                        }
                        break;
                    }
                    authorsList.add(t);
                    i++;
                }
            }
        }

        // Keywords: si existe línea 'Palabras...' tomarla (puede estar al final)
        String[] keywords = null;
        if (idxPalabras != -1) {
            String t = lines[idxPalabras].trim();
            int colon = t.indexOf(":");
            String kpart = colon >= 0 ? t.substring(colon + 1).trim() : t.replaceFirst("(?i)palabras\\s*(claves|claves:)?", "").trim();
            if (!kpart.isEmpty()) {
                keywords = kpart.split("[,;]\\s*");
            }
        } else {
            // buscar patrón 'Palabras claves:' en cualquier línea
            for (int i = 0; i < n; i++) {
                String t = lines[i].trim();
                String lo = t.toLowerCase();
                if (lo.startsWith("palabras")) {
                    int colon = t.indexOf(":");
                    String kpart = colon >= 0 ? t.substring(colon + 1).trim() : t.replaceFirst("(?i)palabras\\s*(claves|claves:)?", "").trim();
                    if (!kpart.isEmpty()) {
                        keywords = kpart.split("[,;]\\s*");
                        break;
                    }
                }
            }
        }

        // Cuerpo: desde la línea después de 'Resumen' hasta antes de 'Palabras' (si existe), o desde la primera línea después de autores hasta 'Palabras'
        StringBuilder body = new StringBuilder();
        int bodyStart = -1;
        if (idxResumen != -1) {
            bodyStart = idxResumen + 1;
        } else if (idxAutores != -1) {
            bodyStart = idxAutores + 1 + (authorsList.size());
        } else {
            // después del título y autores inferidos
            int idxTitle = -1;
            for (int i = 0; i < n; i++) {
                if (lines[i].trim().equals(title)) {
                    idxTitle = i;
                    break;
                }
            }
            bodyStart = idxTitle >= 0 ? idxTitle + 1 + authorsList.size() : 0;
        }
        int bodyEnd = n;
        if (idxPalabras != -1 && idxPalabras > bodyStart) {
            bodyEnd = idxPalabras;
        }
        for (int i = bodyStart; i < bodyEnd && i < n; i++) {
            body.append(lines[i]).append("\n");
        }

        String[] authors = authorsList.size() == 0 ? null : authorsList.toArray();
        if (title == null) {
            return null;
        }
        return new Article(title, authors, body.toString().trim(), keywords);
    }

    private void addArticleInternal(Article a) {
        store.articles.put(a.getTitle(), a);
        // registrar por keywords
        String[] kws = a.getKeywords();
        if (kws != null) {
            for (String k : kws) {
                if (k == null) {
                    continue;
                }
                String key = k.trim();
                // limpiar puntuación/espacios al inicio y fin (ej.: "realidad virtual.")
                key = key.replaceAll("^[\\p{Punct}\\s]+|[\\p{Punct}\\s]+$", "");
                if (key.isEmpty()) {
                    continue;
                }
                // insertar clave en AVL (solo la clave)
                store.keywordsAVL.insert(key);
                SimpleList list = store.keywordMap.get(key);
                if (list == null) {
                    list = new SimpleList();
                    store.keywordMap.put(key, list);
                }
                list.add(a.getId());
            }
        }
        // registrar por autores
        String[] auth = a.getAuthors();
        if (auth != null) {
            for (String au : auth) {
                if (au == null) {
                    continue;
                }
                String an = au.trim();
                if (an.isEmpty()) {
                    continue;
                }
                store.authorsAVL.insert(an);
                SimpleList list = store.authorMap.get(an);
                if (list == null) {
                    list = new SimpleList();
                    store.authorMap.put(an, list);
                }
                list.add(a.getId());
            }
        }
    }

    private void analyzeAction() {
        // mostrar lista de investigaciones en orden alfabético
        String[] keys = store.articles.keys();
        Collator coll = Collator.getInstance();
        coll.setStrength(Collator.PRIMARY);
        // usar un sort O(n log n) en lugar del insertion sort para evitar
        // comportamiento cuadrático en arreglos grandes
        sortStrings(keys, coll);
        String sel = (String) JOptionPane.showInputDialog(frame, "Seleccione investigación:", "Analizar resumen", JOptionPane.PLAIN_MESSAGE, null, keys, keys.length > 0 ? keys[0] : null);
        if (sel != null) {
            Article a = store.articles.get(sel);
            if (a != null) {
                showAnalysis(a);
            }
        }
    }

    private void showAnalysis(Article a) {
        StringBuilder sb = new StringBuilder();
        sb.append(a.getTitle()).append("\nID: ").append(a.getId()).append("\n\nAutores:\n");
        String[] authors = a.getAuthors() == null ? new String[0] : a.getAuthors();
        for (String au : authors) {
            sb.append(au).append("\n");
        }
        sb.append("\n");
        // Según nuevo requerimiento, mostrar frecuencia de TODAS las palabras
        // clave almacenadas en el repositorio respecto al cuerpo del resumen.
        String body = a.getBody() == null ? "" : a.getBody();
        String[] repoKeys = store.keywordsAVL.inorderKeys();
        // `inorderKeys()` del AVL ya devuelve las claves en orden ascendente
        // por lo que no es necesario re-ordenarlas aquí.
        sb.append("Palabras clave (frecuencia en este resumen):\n");
        if (repoKeys == null || repoKeys.length == 0) {
            sb.append("(no hay palabras clave en el repositorio)\n");
        } else {
            for (String k : repoKeys) {
                if (k == null) {
                    continue;
                }
                int freqPhrase = countOccurrences(body, k);
                int freqTokens = countTokenOccurrences(body, k);
                // metadata presente si el artículo figura en la lista del keyword en el mapa
                int freqMeta = 0;
                SimpleList metaList = store.keywordMap.get(k);
                if (metaList != null && metaList.contains(a.getId())) {
                    freqMeta = 1;
                }
                int freqExtended = freqTokens + freqMeta;
                sb.append(k).append(": frase=").append(freqPhrase).append(", tokens/meta=").append(freqExtended).append("\n");
            }
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(frame, sp, "Análisis - " + a.getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    private int countOccurrences(String text, String word) {
        if (word == null || word.isEmpty() || text == null || text.isEmpty()) {
            return 0;
        }
        // Normalizar: eliminar diacríticos, pasar a minúsculas y reemplazar puntuación por espacios
        String normText = normalize(text);
        String normWord = normalize(word);

        // Tokenizar texto
        String[] tokens = normText.split("\\s+");
        String[] kws = normWord.split("\\s+");
        if (kws.length == 0) {
            return 0;
        }

        int count = 0;
        // Permitir una pequeña separación entre palabras clave (por ejemplo, permitir hasta gap de 4 palabras entre términos)
        int maxGap = 4;
        for (int i = 0; i < tokens.length; i++) {
            if (!tokens[i].equals(kws[0])) {
                continue;
            }
            int cur = i;
            boolean ok = true;
            for (int j = 1; j < kws.length; j++) {
                // buscar kws[j] en la ventana a partir de cur+1 hasta cur+1+maxGap
                int found = -1;
                for (int p = cur + 1; p <= Math.min(tokens.length - 1, cur + maxGap + 1); p++) {
                    if (tokens[p].equals(kws[j])) {
                        found = p;
                        break;
                    }
                }
                if (found == -1) {
                    ok = false;
                    break;
                }
                cur = found;
            }
            if (ok) {
                count++;
                // avanzar i para evitar recuentos superpuestos (colocar i después de cur)
                i = cur;
            }
        }
        return count;
    }

    /**
     * Cuenta ocurrencias por token: para una palabra clave compuesta, cuenta
     * cuántas veces aparecen sus tokens individuales en el texto normalizado.
     * Ejemplo: "realidad virtual" -> cuenta apariciones de "realidad" y de
     * "virtual".
     */
    private int countTokenOccurrences(String text, String word) {
        if (word == null || word.isEmpty() || text == null || text.isEmpty()) {
            return 0;
        }
        String normText = normalize(text);
        String normWord = normalize(word);
        String[] tokens = normText.split("\\s+");
        String[] kws = normWord.split("\\s+");
        if (kws.length == 0) {
            return 0;
        }
        int count = 0;
        for (String kw : kws) {
            if (kw == null || kw.isEmpty()) {
                continue;
            }
            for (String t : tokens) {
                if (t.equals(kw)) {
                    count++;
                }
            }
        }
        return count;
    }

    private String normalize(String s) {
        if (s == null) {
            return "";
        }
        String low = s.toLowerCase();
        // remover diacríticos
        String nf = java.text.Normalizer.normalize(low, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        // reemplazar guiones y puntuación por espacios
        nf = nf.replaceAll("[-_\\p{Punct}]+", " ");
        // colapsar espacios
        nf = nf.replaceAll("\\s+", " ").trim();
        return nf;
    }

    private void searchByKeywordAction() {
        String k = JOptionPane.showInputDialog(frame, "Ingrese palabra clave:");
        if (k == null || k.trim().isEmpty()) {
            return;
        }
        SimpleList list = store.keywordMap.get(k.trim());
        if (list == null || list.size() == 0) {
            JOptionPane.showMessageDialog(frame, "No hay investigaciones para la palabra clave: " + k);
            return;
        }
        String[] ids = list.toArray();
        // map ids a títulos
        String[] titles = mapIdsToTitles(ids);
        String sel = (String) JOptionPane.showInputDialog(frame, "Investigaciones:", "Resultados para '" + k + "'", JOptionPane.PLAIN_MESSAGE, null, titles, titles[0]);
        if (sel != null) {
            Article a = store.articles.get(sel);
            if (a != null) {
                showAnalysis(a);
            }
        }
    }

    private String[] mapIdsToTitles(String[] ids) {
        SimpleList out = new SimpleList();
        for (String id : ids) {
            // buscar en articles values
            for (Article a : store.articles.values()) {
                if (a.getId().equals(id)) {
                    out.add(a.getTitle());
                    break;
                }
            }
        }
        return out.toArray();
    }

    /**
     * Ordena un arreglo de Strings usando un Collator (insertion sort simple).
     */
    private static void sortStrings(String[] arr, Collator coll) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        String[] aux = new String[arr.length];
        mergeSortRec(arr, aux, 0, arr.length - 1, coll);
    }

    private static void mergeSortRec(String[] arr, String[] aux, int left, int right, Collator coll) {
        if (left >= right) {
            return;
        }
        int mid = left + (right - left) / 2;
        mergeSortRec(arr, aux, left, mid, coll);
        mergeSortRec(arr, aux, mid + 1, right, coll);
        // merge
        int i = left;
        int j = mid + 1;
        int k = left;
        while (i <= mid && j <= right) {
            if (coll.compare(arr[i], arr[j]) <= 0) {
                aux[k++] = arr[i++];
            } else {
                aux[k++] = arr[j++];
            }
        }
        while (i <= mid) {
            aux[k++] = arr[i++];
        }
        while (j <= right) {
            aux[k++] = arr[j++];
        }
        for (int p = left; p <= right; p++) {
            arr[p] = aux[p];
        }
    }

    private void searchByAuthorAction() {
        String[] authors = store.authorsAVL.inorderKeys();
        // `authors` ya viene ordenado desde el AVL (inorder), no reordenar.
        String sel = (String) JOptionPane.showInputDialog(frame, "Seleccione autor:", "Buscar por autor", JOptionPane.PLAIN_MESSAGE, null, authors, authors.length > 0 ? authors[0] : null);
        if (sel != null) {
            // Verificación en AVL (O(log n)) y recuperación en hash (O(1))
            if (!store.authorsAVL.contains(sel)) {
                JOptionPane.showMessageDialog(frame, "Autor no encontrado: " + sel);
                return;
            }
            SimpleList lst = store.authorMap.get(sel);
            if (lst == null || lst.size() == 0) {
                JOptionPane.showMessageDialog(frame, "No hay trabajos para: " + sel);
                return;
            }
            String[] ids = lst.toArray();
            String[] titles = mapIdsToTitles(ids);
            String s2 = (String) JOptionPane.showInputDialog(frame, "Investigaciones:", "Resultados para '" + sel + "'", JOptionPane.PLAIN_MESSAGE, null, titles, titles[0]);
            if (s2 != null) {
                Article a = store.articles.get(s2);
                if (a != null) {
                    showAnalysis(a);

                }
            }
        }
    }

    private void listKeywordsAction() {
        // obtener claves del AVL (ya vienen ordenadas por inorder)
        String[] keys = store.keywordsAVL.inorderKeys();
        String sel = (String) JOptionPane.showInputDialog(frame, "Palabras clave:", "Lista de palabras clave", JOptionPane.PLAIN_MESSAGE, null, keys, keys.length > 0 ? keys[0] : null);
        if (sel != null) {
            SimpleList lst = store.keywordMap.get(sel);
            String[] ids = lst == null ? new String[0] : lst.toArray();
            String[] titles = mapIdsToTitles(ids);
            StringBuilder sb = new StringBuilder();
            // calcular apariciones por artículo
            int total = 0;
            sb.append("Palabra: ").append(sel).append("\n\n");
            sb.append("Apariciones por artículo:\n");
            for (String id : ids) {
                for (Article a : store.articles.values()) {
                    if (a.getId().equals(id)) {
                        int freqPhrase = countOccurrences(a.getBody(), sel);
                        int freqTokens = countTokenOccurrences(a.getBody(), sel);
                        SimpleList metaList = store.keywordMap.get(sel);
                        int freqMeta = (metaList != null && metaList.contains(a.getId())) ? 1 : 0;
                        int freqExt = freqTokens + freqMeta;
                        sb.append(" - ").append(a.getTitle()).append(": frase=").append(freqPhrase).append(", tokens/meta=").append(freqExt).append("\n");
                        total += freqPhrase; // mantener total de apariciones exactas en cuerpo como antes
                        break;
                    }
                }
            }
            sb.append("\nApariciones totales en el repositorio: ").append(total).append("\n\n");
            sb.append("Artículos que contienen la palabra:\n");
            for (String t : titles) {
                sb.append(t).append("\n");
            }
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            JOptionPane.showMessageDialog(frame, new JScrollPane(ta), "Detalle palabra clave", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadExamplesFromRecursos() {
        File dir = new File("recursos");
        if (!dir.exists() || !dir.isDirectory()) {
            JOptionPane.showMessageDialog(frame, "No se encontró el directorio 'recursos' en el directorio de trabajo.");
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(frame, "No hay archivos .txt en 'recursos'.");
            return;
        }
        int added = 0;
        int skipped = 0;
        for (File f : files) {
            try {
                Article a = parseArticleFile(f);
                if (a == null) {
                    skipped++;
                    continue;
                }
                if (store.articles.containsKey(a.getTitle())) {
                    skipped++;
                    continue;
                }
                addArticleInternal(a);
                added++;
            } catch (Exception ex) {
                // no interrumpir la carga por un archivo malo
                skipped++;
            }
        }
        JOptionPane.showMessageDialog(frame, "Carga completada. Agregados: " + added + ", omitidos: " + skipped);
    }

    private void clearRepository() {
        int ok = JOptionPane.showConfirmDialog(frame, "¿Desea vaciar el repositorio actual? Esto eliminará los artículos cargados en memoria.\n(El archivo persistido no será cargado a menos que lo confirme en el próximo inicio)", "Confirmar limpieza", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        store = new DataStore();
        // intentar eliminar el archivo persistente para evitar recargas futuras accidentales
        try {
            File f = new File(DATA_FILE);
            if (f.exists()) {
                boolean d = f.delete();
                if (d) {
                    JOptionPane.showMessageDialog(frame, "Repositorio vaciado. Archivo persistente eliminado: " + DATA_FILE);
                    return;
                }
            }
        } catch (Exception e) {
            // ignorar
        }
        JOptionPane.showMessageDialog(frame, "Repositorio vaciado.");
    }

    private void exitAndSave() {
        try {
            store.saveToFile(new File(DATA_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Punto de entrada de la aplicación.
     *
     * @param args argumentos de la JVM (no usados)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Proyecto2CarolinaDib());
    }
}

