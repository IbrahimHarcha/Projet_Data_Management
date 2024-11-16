package qengine.storage;

import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import qengine.model.RDFAtom;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Implémentation d'un HexaStore pour stocker et interroger des RDFAtoms.
 */
public class RDFHexaStore {
    // Six index pour Hexastore avec les valeurs encodées
    private final Map<Integer, Map<Integer, Map<Integer, Set<RDFAtom>>>> spo;
    private final Map<Integer, Map<Integer, Map<Integer, Set<RDFAtom>>>> sop;
    private final Map<Integer, Map<Integer, Map<Integer, Set<RDFAtom>>>> pso;
    private final Map<Integer, Map<Integer, Map<Integer, Set<RDFAtom>>>> pos;
    private final Map<Integer, Map<Integer, Map<Integer, Set<RDFAtom>>>> osp;
    private final Map<Integer, Map<Integer, Map<Integer, Set<RDFAtom>>>> ops;

    // Collection d'atomes
    private final Set<RDFAtom> atoms;

    // Structures du dictionnaire intégré
    private final Map<String, Integer> stringToId;
    private final Map<Integer, String> idToString;
    private final AtomicInteger nextId;

    // Constructeur
    public RDFHexaStore() {
        this.spo = new HashMap<>();
        this.sop = new HashMap<>();
        this.pso = new HashMap<>();
        this.pos = new HashMap<>();
        this.osp = new HashMap<>();
        this.ops = new HashMap<>();
        this.atoms = new HashSet<>();

        // Initialisation des structures du dictionnaire
        this.stringToId = new HashMap<>();
        this.idToString = new HashMap<>();
        this.nextId = new AtomicInteger(1);
    }

    /**
     * Encode une chaîne en entier.
     */
    private int encode(String str) {
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
     * Décode un entier en chaîne.
     */
    private String decode(int id) {
        String str = idToString.get(id);
        if (str == null) {
            throw new IllegalArgumentException("ID non trouvé dans le dictionnaire: " + id);
        }
        return str;
    }

    /**
     * Encode un RDFAtom en tableau d'entiers.
     */
    private int[] encodeRDFAtom(RDFAtom atom) {
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
     * Ajoute un atome RDF à l'HexaStore avec encodage.
     */
    public boolean add(RDFAtom atom) {
        if (!atoms.add(atom)) {
            return false; // Duplicate
        }

        int[] encoded = encodeRDFAtom(atom);

        // Ajout aux index avec les valeurs encodées
        addToIndex(spo, encoded[0], encoded[1], encoded[2], atom);
        addToIndex(sop, encoded[0], encoded[2], encoded[1], atom);
        addToIndex(pso, encoded[1], encoded[0], encoded[2], atom);
        addToIndex(pos, encoded[1], encoded[2], encoded[0], atom);
        addToIndex(osp, encoded[2], encoded[0], encoded[1], atom);
        addToIndex(ops, encoded[2], encoded[1], encoded[0], atom);

        return true;
    }

    /**
     * Modifié pour utiliser directement les valeurs encodées.
     */
    private void addToIndex(Map<Integer, Map<Integer, Map<Integer, Set<RDFAtom>>>> index,
                            int first, int second, int third, RDFAtom atom) {
        index.computeIfAbsent(first, k -> new HashMap<>())
                .computeIfAbsent(second, k -> new HashMap<>())
                .computeIfAbsent(third, k -> new HashSet<>())
                .add(atom);
    }

    /**
     * Recherche modifiée pour utiliser l'encodage.
     */
    public Iterator<Substitution> match(RDFAtom atom) {
        Term subject = atom.getTripleSubject();
        Term predicate = atom.getTriplePredicate();
        Term object = atom.getTripleObject();

        Integer subjectId = null;
        Integer predicateId = null;
        Integer objectId = null;

        // Encode seulement les termes non-variables
        if (!(subject instanceof Variable)) {
            subjectId = stringToId.get(subject.toString());
        }
        if (!(predicate instanceof Variable)) {
            predicateId = stringToId.get(predicate.toString());
        }
        if (!(object instanceof Variable)) {
            objectId = stringToId.get(object.toString());
        }

        Set<RDFAtom> matchingAtoms = findMatchingAtoms(subjectId, predicateId, objectId);

        List<Substitution> substitutions = new ArrayList<>();
        for (RDFAtom matchingAtom : matchingAtoms) {
            Substitution substitution = new SubstitutionImpl();

            if (subject instanceof Variable) {
                substitution.add((Variable) subject, matchingAtom.getTripleSubject());
            }
            if (predicate instanceof Variable) {
                substitution.add((Variable) predicate, matchingAtom.getTriplePredicate());
            }
            if (object instanceof Variable) {
                substitution.add((Variable) object, matchingAtom.getTripleObject());
            }

            substitutions.add(substitution);
        }

        return substitutions.iterator();
    }

    /**
     * Version modifiée pour utiliser les IDs encodés.
     */
    private Set<RDFAtom> findMatchingAtoms(Integer subjectId, Integer predicateId, Integer objectId) {
        Set<RDFAtom> result = new HashSet<>();

        // Choisir l'index le plus efficace basé sur les termes connus
        Map<Integer, Map<Integer, Map<Integer, Set<RDFAtom>>>> index;
        Integer first = null, second = null, third = null;

        if (subjectId != null) {
            index = spo;
            first = subjectId;
            second = predicateId;
            third = objectId;
        } else if (predicateId != null) {
            index = pso;
            first = predicateId;
            second = subjectId;
            third = objectId;
        } else if (objectId != null) {
            index = ops;
            first = objectId;
            second = predicateId;
            third = subjectId;
        } else {
            // Si aucun terme n'est connu, retourner tous les atomes
            return new HashSet<>(atoms);
        }

        // Navigation dans l'index choisi
        if (first != null) {
            Map<Integer, Map<Integer, Set<RDFAtom>>> secondLevel = index.get(first);
            if (secondLevel != null) {
                if (second != null) {
                    Map<Integer, Set<RDFAtom>> thirdLevel = secondLevel.get(second);
                    if (thirdLevel != null) {
                        if (third != null) {
                            Set<RDFAtom> atoms = thirdLevel.get(third);
                            if (atoms != null) {
                                result.addAll(atoms);
                            }
                        } else {
                            thirdLevel.values().forEach(result::addAll);
                        }
                    }
                } else {
                    secondLevel.values().forEach(m -> m.values().forEach(result::addAll));
                }
            }
        }

        return result;
    }

    // Les autres méthodes restent inchangées
    public boolean addAll(Collection<RDFAtom> rdfAtoms) {
        boolean added = false;
        for (RDFAtom atom : rdfAtoms) {
            added |= add(atom);
        }
        return added;
    }

    public boolean addAll(Stream<RDFAtom> rdfAtoms) {
        return addAll(rdfAtoms.toList());
    }

    public long size() {
        return atoms.size();
    }

    public Collection<Atom> getAtoms() {
        return new HashSet<>(atoms);
    }
}
