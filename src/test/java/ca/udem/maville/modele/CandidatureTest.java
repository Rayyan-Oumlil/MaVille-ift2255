package ca.udem.maville.modele;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Collections;

public class CandidatureTest {

    @Test
    void testCreationCandidature() {
        Prestataire p = new Prestataire("NEQ123", "Entreprise X", "Jean", "514-000-0000", "mail@x.com");
        Candidature c = new Candidature(
            p,
            Collections.singletonList(1),
            "Test projet",
            2000.0,
            LocalDate.now(),
            LocalDate.now().plusDays(2)
        );
        assertEquals("Test projet", c.getDescriptionProjet());
        assertEquals(p, c.getPrestataire());
        assertEquals(1, c.getProblemesVises().size());
    }

    @Test
    void testStatutInitial() {
        Prestataire p = new Prestataire("NEQ123", "Entreprise X", "Jean", "514-000-0000", "mail@x.com");
        Candidature c = new Candidature(
            p,
            Collections.singletonList(2),
            "Autre projet",
            3000.0,
            LocalDate.now(),
            LocalDate.now().plusDays(3)
        );
        // Vérifie que la candidature est "SOUMISE" à la création
        assertEquals(StatutCandidature.SOUMISE, c.getStatut());
    }

    @Test
    void testPeutEtreModifiee() {
        Prestataire p = new Prestataire("NEQ999", "Entreprise Y", "Samir", "514-000-1111", "samir@y.com");
        Candidature c = new Candidature(
            p,
            Collections.singletonList(3),
            "Projet modif",
            5000.0,
            LocalDate.now(),
            LocalDate.now().plusDays(4)
        );
        // Par défaut, peut être modifiée
        assertTrue(c.peutEtreModifiee());
        // Change le statut
        c.setStatut(StatutCandidature.APPROUVEE);
        // Ne peut plus être modifiée
        assertFalse(c.peutEtreModifiee());
    }
}
