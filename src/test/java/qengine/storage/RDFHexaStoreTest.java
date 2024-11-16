package qengine.storage;

import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import org.apache.commons.lang3.NotImplementedException;
import qengine.model.RDFAtom;
import qengine.storage.RDFHexaStore;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link RDFHexaStore}.
 */
import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import org.apache.commons.lang3.NotImplementedException;
import qengine.model.RDFAtom;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link RDFHexaStore}.
 */
public class RDFHexaStoreTest {
    private static final Literal<String> SUBJECT_1 = SameObjectTermFactory.instance().createOrGetLiteral("subject1");
    private static final Literal<String> PREDICATE_1 = SameObjectTermFactory.instance().createOrGetLiteral("predicate1");
    private static final Literal<String> OBJECT_1 = SameObjectTermFactory.instance().createOrGetLiteral("object1");
    private static final Literal<String> SUBJECT_2 = SameObjectTermFactory.instance().createOrGetLiteral("subject2");
    private static final Literal<String> PREDICATE_2 = SameObjectTermFactory.instance().createOrGetLiteral("predicate2");
    private static final Literal<String> OBJECT_2 = SameObjectTermFactory.instance().createOrGetLiteral("object2");
    private static final Literal<String> OBJECT_3 = SameObjectTermFactory.instance().createOrGetLiteral("object3");
    private static final Variable VAR_X = SameObjectTermFactory.instance().createOrGetVariable("?x");
    private static final Variable VAR_Y = SameObjectTermFactory.instance().createOrGetVariable("?y");

    @Test
    public void testAddAllRDFAtoms() {
        RDFHexaStore store = new RDFHexaStore();

        RDFAtom rdfAtom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_2);

        Set<RDFAtom> rdfAtoms = Set.of(rdfAtom1, rdfAtom2);

        // Ajout en tant que stream
        assertTrue(store.addAll(rdfAtoms.stream()), "Les RDFAtoms devraient être ajoutés avec succès.");
        Collection<Atom> atoms = store.getAtoms();
        assertTrue(atoms.contains(rdfAtom1), "La base devrait contenir le premier RDFAtom.");
        assertTrue(atoms.contains(rdfAtom2), "La base devrait contenir le second RDFAtom.");

        // Réinitialisation et ajout via collection
        store = new RDFHexaStore();
        assertTrue(store.addAll(rdfAtoms), "Les RDFAtoms devraient être ajoutés via une collection.");
        atoms = store.getAtoms();
        assertTrue(atoms.contains(rdfAtom1), "La base devrait contenir le premier RDFAtom.");
        assertTrue(atoms.contains(rdfAtom2), "La base devrait contenir le second RDFAtom.");
    }

    @Test
    public void testAddRDFAtom() {
        RDFHexaStore store = new RDFHexaStore();
        RDFAtom rdfAtom = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);

        // Ajouter un atome RDF
        assertTrue(store.add(rdfAtom), "Le RDFAtom devrait être ajouté avec succès.");
        assertFalse(store.add(rdfAtom), "L'ajout d'un doublon ne devrait pas être autorisé.");
    }

    @Test
    public void testAddDuplicateAtom() {
        RDFHexaStore store = new RDFHexaStore();
        RDFAtom rdfAtom = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);

        // Ajouter un atome RDF et un doublon
        assertTrue(store.add(rdfAtom), "Le RDFAtom devrait être ajouté avec succès.");
        assertFalse(store.add(rdfAtom), "Le doublon ne devrait pas être ajouté.");
        assertEquals(1, store.size(), "La taille du store devrait être 1 après ajout d'un doublon.");
    }

    @Test
    public void testSize() {
        RDFHexaStore store = new RDFHexaStore();
        assertEquals(0, store.size(), "La taille initiale du store devrait être 0.");

        RDFAtom rdfAtom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_2);

        store.add(rdfAtom1);
        store.add(rdfAtom2);
        assertEquals(2, store.size(), "La taille du store devrait être 2 après ajout de deux atomes.");
    }

    @Test
    public void testMatchAtom() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_2));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_3));

        // Recherche avec une variable
        RDFAtom matchingAtom = new RDFAtom(SUBJECT_1, PREDICATE_1, VAR_X);
        Iterator<Substitution> matchedAtoms = store.match(matchingAtom);
        List<Substitution> matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        // Création des substitutions attendues
        Substitution firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, OBJECT_1);
        Substitution secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, OBJECT_3);

        assertEquals(2, matchedList.size(), "Il devrait y avoir deux correspondances.");
        assertTrue(matchedList.contains(firstResult), "Il manque la substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Il manque la substitution: " + secondResult);
    }

    @Test
    public void testMatchStarQuery() {
        throw new NotImplementedException();
    }
}