package ca.udem.maville.storage;

import ca.udem.maville.modele.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour JsonStorage
 * Couvre les fonctionnalités de persistance des données
 */
class JsonStorageTest {
    
    @TempDir
    Path tempDir;
    
    private JsonStorage storage;
    
    @BeforeEach
    void setUp() {
        // Utiliser un répertoire temporaire pour les tests
        System.setProperty("user.dir", tempDir.toString());
        storage = new JsonStorage();
    }
    
    /**
     * Test 1: Sauvegarder et charger des problèmes
     * Vérifie la persistance complète des problèmes
     */
    @Test
    void testSaveLoadProblemes() {
        // Given
        Resident resident = new Resident("Test", "User", "514-000-0000", 
            "test@email.com", "123 Test Street");
        
        Probleme probleme1 = new Probleme("Lieu 1", TypeTravaux.TRAVAUX_ROUTIERS, 
            "Description 1", resident);
        probleme1.setId(1);
        probleme1.setPriorite(Priorite.ELEVEE);
        
        Probleme probleme2 = new Probleme("Lieu 2", TypeTravaux.ENTRETIEN_PAYSAGER, 
            "Description 2", resident);
        probleme2.setId(2);
        probleme2.setPriorite(Priorite.FAIBLE);
        probleme2.setResolu(true);
        
        List<Probleme> problemesOriginaux = Arrays.asList(probleme1, probleme2);
        
        // When
        storage.saveProblemes(problemesOriginaux);
        List<Probleme> problemesCharges = storage.loadProblemes();
        
        // Then
        assertNotNull(problemesCharges, "Les problèmes chargés ne doivent pas être null");
        assertEquals(2, problemesCharges.size(), "Doit charger exactement 2 problèmes");
        
        // Vérifier le premier problème
        Probleme p1Charge = problemesCharges.stream()
            .filter(p -> p.getId() == 1)
            .findFirst()
            .orElse(null);
        
        assertNotNull(p1Charge, "Le problème avec ID 1 doit être chargé");
        assertEquals("Lieu 1", p1Charge.getLieu(), "Le lieu doit être préservé");
        assertEquals(TypeTravaux.TRAVAUX_ROUTIERS, p1Charge.getTypeProbleme(), "Le type doit être préservé");
        assertEquals("Description 1", p1Charge.getDescription(), "La description doit être préservée");
        assertEquals(Priorite.ELEVEE, p1Charge.getPriorite(), "La priorité doit être préservée");
        assertFalse(p1Charge.isResolu(), "Le statut résolu doit être préservé");
        
        // Vérifier le deuxième problème
        Probleme p2Charge = problemesCharges.stream()
            .filter(p -> p.getId() == 2)
            .findFirst()
            .orElse(null);
        
        assertNotNull(p2Charge, "Le problème avec ID 2 doit être chargé");
        assertEquals("Lieu 2", p2Charge.getLieu(), "Le lieu doit être préservé");
        assertEquals(TypeTravaux.ENTRETIEN_PAYSAGER, p2Charge.getTypeProbleme(), "Le type doit être préservé");
        assertEquals(Priorite.FAIBLE, p2Charge.getPriorite(), "La priorité doit être préservée");
        assertTrue(p2Charge.isResolu(), "Le statut résolu doit être préservé");
    }
    
    /**
     * Test 2: Sauvegarder et charger des candidatures
     * Vérifie la persistance des candidatures avec synchronisation des IDs
     */
    @Test
    void testSaveLoadCandidatures() {
        // Given
        Prestataire prestataire = new Prestataire("NEQ123", "Entreprise Test", 
            "Contact Test", "514-000-0000", "test@entreprise.com");
        
        Candidature candidature1 = new Candidature(
            prestataire,
            Arrays.asList(1, 2),
            "Projet test 1",
            5000.0,
            java.time.LocalDate.now(),
            java.time.LocalDate.now().plusDays(7)
        );
        candidature1.setId(10); // ID spécifique pour le test
        candidature1.setStatut(StatutCandidature.APPROUVEE);
        
        Candidature candidature2 = new Candidature(
            prestataire,
            Arrays.asList(3),
            "Projet test 2",
            3000.0,
            java.time.LocalDate.now().plusDays(1),
            java.time.LocalDate.now().plusDays(5)
        );
        candidature2.setId(20); // ID spécifique pour le test
        candidature2.setStatut(StatutCandidature.REJETEE);
        candidature2.setCommentaireRejet("Budget insuffisant");
        
        List<Candidature> candidaturesOriginales = Arrays.asList(candidature1, candidature2);
        
        // When
        storage.saveCandidatures(candidaturesOriginales);
        List<Candidature> candidaturesChargees = storage.loadCandidatures();
        
        // Then
        assertNotNull(candidaturesChargees, "Les candidatures chargées ne doivent pas être null");
        assertEquals(2, candidaturesChargees.size(), "Doit charger exactement 2 candidatures");
        
        // Vérifier la première candidature
        Candidature c1Chargee = candidaturesChargees.stream()
            .filter(c -> c.getId() == 10)
            .findFirst()
            .orElse(null);
        
        assertNotNull(c1Chargee, "La candidature avec ID 10 doit être chargée");
        assertEquals("Projet test 1", c1Chargee.getDescriptionProjet(), "La description doit être préservée");
        assertEquals(5000.0, c1Chargee.getCoutEstime(), 0.01, "Le coût doit être préservé");
        assertEquals(StatutCandidature.APPROUVEE, c1Chargee.getStatut(), "Le statut doit être préservé");
        assertEquals(Arrays.asList(1, 2), c1Chargee.getProblemesVises(), "Les problèmes visés doivent être préservés");
        
        // Vérifier la deuxième candidature
        Candidature c2Chargee = candidaturesChargees.stream()
            .filter(c -> c.getId() == 20)
            .findFirst()
            .orElse(null);
        
        assertNotNull(c2Chargee, "La candidature avec ID 20 doit être chargée");
        assertEquals(StatutCandidature.REJETEE, c2Chargee.getStatut(), "Le statut doit être préservé");
        assertEquals("Budget insuffisant", c2Chargee.getCommentaireRejet(), "Le commentaire de rejet doit être préservé");
        
        // Vérifier que la synchronisation des IDs fonctionne
        // (Testé indirectement via le chargement réussi)
    }
    
}