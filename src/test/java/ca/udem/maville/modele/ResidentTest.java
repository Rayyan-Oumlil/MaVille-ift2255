package ca.udem.maville.modele;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests "métier" pour Resident :
 * - égalité/hashCode basés sur l'email
 * - nom complet et toString
 * - déduplication en Set
 * - @JsonIgnore sur getNomComplet()
 */
class ResidentLogicTest {

    @Test
    void testEgaliteEtHashCodeParEmail() {
        Resident r1 = new Resident("Dubois", "Anne", "514", "anne@ex.com", "Rue X");
        Resident r2 = new Resident("Autre", "Nom", "000", "anne@ex.com", "Rue Y");

        assertEquals(r1, r2, "Deux résidents avec le même email doivent être égaux");
        assertEquals(r1.hashCode(), r2.hashCode(), "hashCode doit être identique si email identique");

        Resident r3 = new Resident("Dubois", "Anne", "514", "anne2@ex.com", "Rue X");
        assertNotEquals(r1, r3, "Emails différents => résidents différents");
    }

    @Test
    void testNomCompletEtToString() {
        Resident r = new Resident("Dubois", "Anne", "514", "anne@ex.com", "Rue X");
        assertEquals("Anne Dubois", r.getNomComplet());

        String s = r.toString();
        assertTrue(s.contains("Anne Dubois") && s.contains("anne@ex.com"),
                "toString doit contenir nom complet et email");
    }

    @Test
    void testComportementSetDeduplicationParEmail() {
        Resident a = new Resident("A", "A", "000", "mail@ex.com", "X");
        Resident b = new Resident("B", "B", "111", "mail@ex.com", "Y"); // même email
        Resident c = new Resident("C", "C", "222", "autre@ex.com", "Z");

        Set<Resident> set = new HashSet<>();
        set.add(a);
        set.add(b); // ne doit pas créer un doublon
        set.add(c);

        assertEquals(2, set.size(), "Le Set ne doit contenir qu’un seul élément par email");
        assertTrue(set.contains(a));
        assertTrue(set.contains(c));
    }

    @Test
    void testJsonIgnoreSurNomComplet() throws Exception {
        Resident r = new Resident("Dubois", "Anne", "514", "anne@ex.com", "Rue X");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(r);

        assertFalse(json.contains("nomComplet"), "Le JSON ne doit pas contenir le champ nomComplet");
        assertTrue(json.contains("email") && json.contains("nom") && json.contains("prenom"),
                "Le JSON doit contenir email, nom et prenom");
    }
}
