package ca.udem.maville.service;

import ca.udem.maville.modele.*;
import ca.udem.maville.storage.JsonStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GestionnaireProblemes
 * Couvre les fonctionnalités de gestion des problèmes signalés
 */
@ExtendWith(MockitoExtension.class)
class GestionnaireProblemeTest {
    
    @Mock
    private JsonStorage mockStorage;
    
    private GestionnaireProblemes gestionnaire;
    private Resident residentTest;
    
    @BeforeEach
    void setUp() {
        // Préparer les données de test
        residentTest = new Resident("Tremblay", "Marie", "514-555-0001", 
            "marie@test.com", "123 Rue Test, Plateau");
        
        // Simuler le storage qui retourne une liste vide au début
        when(mockStorage.loadProblemes()).thenReturn(new ArrayList<>());
        
        // Créer le gestionnaire avec le mock
        gestionnaire = new GestionnaireProblemes(mockStorage);
    }
    
    /**
     * Test 1: Signaler un problème
     * Vérifie qu'un nouveau problème est correctement créé et ajouté
     */
    @Test
    void testSignalerProbleme() {
        // Given
        String lieu = "Rue Saint-Denis, Plateau";
        TypeTravaux type = TypeTravaux.TRAVAUX_ROUTIERS;
        String description = "Gros nid de poule dangereux";
        
        // When
        Probleme probleme = gestionnaire.signalerProbleme(lieu, type, description, residentTest);
        
        // Then
        assertNotNull(probleme, "Le problème créé ne doit pas être null");
        assertEquals(lieu, probleme.getLieu(), "Le lieu doit correspondre");
        assertEquals(type, probleme.getTypeProbleme(), "Le type doit correspondre");
        assertEquals(description, probleme.getDescription(), "La description doit correspondre");
        assertEquals(residentTest, probleme.getDeclarant(), "Le déclarant doit correspondre");
        assertEquals(Priorite.MOYENNE, probleme.getPriorite(), "La priorité par défaut doit être MOYENNE");
        assertFalse(probleme.isResolu(), "Le problème doit être non résolu par défaut");
    }
    
    /**
     * Test 2: Lister les problèmes non résolus
     * Vérifie le filtrage des problèmes selon leur statut de résolution
     */
    @Test
    void testListerProblemesNonResolus() {
        // Given - Préparer une liste avec problèmes résolus et non résolus
        List<Probleme> problemesExistants = new ArrayList<>();
        
        Probleme problemeResolu = new Probleme("Lieu 1", TypeTravaux.TRAVAUX_ROUTIERS, 
            "Description 1", residentTest);
        problemeResolu.setResolu(true);
        problemesExistants.add(problemeResolu);
        
        Probleme problemeNonResolu1 = new Probleme("Lieu 2", TypeTravaux.ENTRETIEN_PAYSAGER, 
            "Description 2", residentTest);
        problemesExistants.add(problemeNonResolu1);
        
        Probleme problemeNonResolu2 = new Probleme("Lieu 3", TypeTravaux.TRAVAUX_SIGNALISATION_ECLAIRAGE, 
            "Description 3", residentTest);
        problemesExistants.add(problemeNonResolu2);
        
        // Configurer le mock pour retourner cette liste
        when(mockStorage.loadProblemes()).thenReturn(problemesExistants);
        
        // Recréer le gestionnaire pour charger les nouvelles données
        gestionnaire = new GestionnaireProblemes(mockStorage);
        
        // When
        List<Probleme> problemesNonResolus = gestionnaire.listerProblemesNonResolus();
        
        // Then
        assertEquals(2, problemesNonResolus.size(), "Doit retourner exactement 2 problèmes non résolus");
        
        for (Probleme p : problemesNonResolus) {
            assertFalse(p.isResolu(), "Tous les problèmes retournés doivent être non résolus");
        }
        
        // Vérifier que les bons problèmes sont retournés
        assertTrue(problemesNonResolus.contains(problemeNonResolu1), "Doit contenir le problème non résolu 1");
        assertTrue(problemesNonResolus.contains(problemeNonResolu2), "Doit contenir le problème non résolu 2");
        assertFalse(problemesNonResolus.contains(problemeResolu), "Ne doit pas contenir le problème résolu");
    }
    
    /**
     * Test 3: Trouver un problème par son ID
     * Vérifie la recherche d'un problème spécifique
     */
    @Test
    void testTrouverProblemeParId() {
        // Given - Préparer des problèmes avec des IDs différents
        List<Probleme> problemesExistants = new ArrayList<>();
        
        Probleme probleme1 = new Probleme("Lieu 1", TypeTravaux.TRAVAUX_ROUTIERS, "Desc 1", residentTest);
        probleme1.setId(1);
        problemesExistants.add(probleme1);
        
        Probleme probleme2 = new Probleme("Lieu 2", TypeTravaux.ENTRETIEN_PAYSAGER, "Desc 2", residentTest);
        probleme2.setId(2);
        problemesExistants.add(probleme2);
        
        Probleme probleme3 = new Probleme("Lieu 3", TypeTravaux.TRAVAUX_SOUTERRAINS, "Desc 3", residentTest);
        probleme3.setId(3);
        problemesExistants.add(probleme3);
        
        when(mockStorage.loadProblemes()).thenReturn(problemesExistants);
        gestionnaire = new GestionnaireProblemes(mockStorage);
        
        // When & Then - Test recherche d'un ID existant
        Probleme problemeRecherche = gestionnaire.trouverProblemeParId(2);
        assertNotNull(problemeRecherche, "Doit trouver le problème avec ID 2");
        assertEquals(2, problemeRecherche.getId(), "L'ID doit correspondre");
        assertEquals("Lieu 2", problemeRecherche.getLieu(), "Le lieu doit correspondre");
        assertEquals(TypeTravaux.ENTRETIEN_PAYSAGER, problemeRecherche.getTypeProbleme(), "Le type doit correspondre");
        
        // Test recherche d'un ID inexistant
        Probleme problemeInexistant = gestionnaire.trouverProblemeParId(999);
        assertNull(problemeInexistant, "Doit retourner null pour un ID inexistant");
        
        // Test recherche avec ID négatif
        Probleme problemeNegatif = gestionnaire.trouverProblemeParId(-1);
        assertNull(problemeNegatif, "Doit retourner null pour un ID négatif");
    }
}