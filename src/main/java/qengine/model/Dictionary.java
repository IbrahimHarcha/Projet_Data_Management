package qengine.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dictionnaire qui associe des chaînes de caractères à des entiers uniques.
 * Utilisé pour encoder et décoder les ressources RDF de manière efficace.
 */
public class Dictionary {
    // Map pour stocker les associations string -> integer
    private final Map<String, Integer> stringToId;
    // Map pour stocker les associations integer -> string
    private final Map<Integer, String> idToString;
    // Compteur pour générer des IDs uniques
    private final AtomicInteger nextId;

    /**
     * Constructeur du dictionnaire.
     */
    public Dictionary() {
        this.stringToId = new HashMap<>();
        this.idToString = new HashMap<>();
        this.nextId = new AtomicInteger(1); // Commence à 1 pour réserver 0 pour des cas spéciaux si nécessaire
    }

    /**
     * Encode une chaîne en entier, en créant une nouvelle entrée si nécessaire.
     * @param str La chaîne à encoder
     * @return L'identifiant entier associé
     */
    public int encode(String str) {
        if (str == null) {
            throw new IllegalArgumentException("La chaîne ne peut pas être null");
        }
        return stringToId.computeIfAbsent(str, k -> {
            int id = nextId.getAndIncrement();
            idToString.put(id, str);
            return id;
        });
    }

    /**
     * Décode un entier en sa chaîne correspondante.
     * @param id L'identifiant à décoder
     * @return La chaîne associée
     * @throws IllegalArgumentException si l'id n'existe pas dans le dictionnaire
     */
    public String decode(int id) {
        String str = idToString.get(id);
        if (str == null) {
            throw new IllegalArgumentException("ID non trouvé dans le dictionnaire: " + id);
        }
        return str;
    }

    /**
     * Vérifie si une chaîne existe déjà dans le dictionnaire.
     * @param str La chaîne à vérifier
     * @return true si la chaîne existe, false sinon
     */
    public boolean containsString(String str) {
        return stringToId.containsKey(str);
    }

    /**
     * Vérifie si un ID existe déjà dans le dictionnaire.
     * @param id L'ID à vérifier
     * @return true si l'ID existe, false sinon
     */
    public boolean containsId(int id) {
        return idToString.containsKey(id);
    }

    /**
     * Encode un triplet RDF complet.
     * @param atom Le triplet RDF à encoder
     * @return Un tableau d'entiers contenant les IDs encodés [sujet, prédicat, objet]
     */
    public int[] encodeRDFAtom(RDFAtom atom) {
        if (atom == null) {
            throw new IllegalArgumentException("Le RDFAtom ne peut pas être null");
        }

        int[] encoded = new int[3];
        encoded[0] = encode(atom.getTripleSubject().toString());
        encoded[1] = encode(atom.getTriplePredicate().toString());
        encoded[2] = encode(atom.getTripleObject().toString());
        return encoded;
    }

    /**
     * Retourne le nombre d'entrées dans le dictionnaire.
     * @return Le nombre d'entrées
     */
    public int size() {
        return stringToId.size();
    }

    /**
     * Vide le dictionnaire.
     */
    public void clear() {
        stringToId.clear();
        idToString.clear();
        nextId.set(1);
    }
}
