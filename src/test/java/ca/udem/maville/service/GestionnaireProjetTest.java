package ca.udem.maville.service;

import ca.udem.maville.modele.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour GestionnaireProjets
 * Couvre les fonctionnalités de gestion des candidatures et projets
 */
class GestionnaireProjetTest {
    
    private GestionnaireProjets gestionnaire;
    private Prestataire prestataireTest;
    
    @BeforeEach
    void setUp() {
        gestionnaire = new GestionnaireProjets();
        prestataireTest = new Prestataire("NEQ123456789", "Entreprise Test Inc.", 
            "Jean Dupont", "514-555-0001", "jean@test.com");
    }
    
    /**
     * Test 1: Soumettre une candidature
     * Vérifie qu'une candidature est correctement créée et enregistrée
     */
    @Test
    void testSoumettreCandidat() {
        // Given
        List<Integer> problemesVises = Arrays.asList(1, 2);
        String description = "Réparation urgente des routes";
        double cout = 15000.0;
        LocalDate dateDebut = LocalDate.now().plusDays(7);
        LocalDate dateFin = LocalDate.now().plusDays(14);
        
        // When
        Candidature candidature = gestionnaire.soumettreCandidat(
            prestataireTest, problemesVises, description, cout, dateDebut, dateFin);
        
        // Then
        assertNotNull(candidature, "La candidature créée ne doit pas être null");
        assertEquals(prestataireTest, candidature.getPrestataire(), "Le prestataire doit correspondre");
        assertEquals(problemesVises, candidature.getProblemesVises(), "Les problèmes visés doivent correspondre");
        assertEquals(description, candidature.getDescriptionProjet(), "La description doit correspondre");
        assertEquals(cout, candidature.getCoutEstime(), 0.01, "Le coût doit correspondre");
        assertEquals(dateDebut, candidature.getDateDebutPrevue(), "La date de début doit correspondre");
        assertEquals(dateFin, candidature.getDateFinPrevue(), "La date de fin doit correspondre");
        assertEquals(StatutCandidature.SOUMISE, candidature.getStatut(), "Le statut initial doit être SOUMISE");
        assertNotNull(candidature.getDateDepot(), "La date de dépôt doit être définie");
        
        // Vérifier que la candidature est dans la liste du gestionnaire
        List<Candidature> candidatures = gestionnaire.listerCandidatures();
        assertTrue(candidatures.contains(candidature), "La candidature doit être dans la liste");
        assertEquals(1, candidatures.size(), "Il doit y avoir exactement 1 candidature");
    }
    
    /**
     * Test 2: Modifier une candidature existante
     * Vérifie qu'une candidature peut être modifiée tant qu'elle n'est pas traitée
     */
    @Test
    void testModifierCandidature() {
        // Given - Créer une candidature d'abord
        List<Integer> problemesVises = Arrays.asList(1);
        Candidature candidature = gestionnaire.soumettreCandidat(
            prestataireTest, problemesVises, "Description initiale", 5000.0, 
            LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        
        int candidatureId = candidature.getId();
        
        // When - Modifier la candidature
        String nouvelleDescription = "Description modifiée avec plus de détails";
        double nouveauCout = 7500.0;
        LocalDate nouvelleDateDebut = LocalDate.now().plusDays(3);
        LocalDate nouvelleDateFin = LocalDate.now().plusDays(10);
        
        boolean resultatModification = gestionnaire.modifierCandidature(
            candidatureId, nouvelleDescription, nouveauCout, nouvelleDateDebut, nouvelleDateFin);
        
        // Then
        assertTrue(resultatModification, "La modification doit réussir pour une candidature SOUMISE");
        
        // Vérifier que les modifications ont été appliquées
        Candidature candidatureModifiee = gestionnaire.trouverCandidatureParId(candidatureId);
        assertNotNull(candidatureModifiee, "La candidature doit toujours exister");
        assertEquals(nouvelleDescription, candidatureModifiee.getDescriptionProjet(), "La description doit être mise à jour");
        assertEquals(nouveauCout, candidatureModifiee.getCoutEstime(), 0.01, "Le coût doit être mis à jour");
        assertEquals(nouvelleDateDebut, candidatureModifiee.getDateDebutPrevue(), "La date de début doit être mise à jour");
        assertEquals(nouvelleDateFin, candidatureModifiee.getDateFinPrevue(), "La date de fin doit être mise à jour");
        
        // Test modification d'une candidature approuvée (doit échouer)
        candidature.setStatut(StatutCandidature.APPROUVEE);
        boolean resultatModificationApprouvee = gestionnaire.modifierCandidature(
            candidatureId, "Nouvelle description", 8000.0, LocalDate.now(), LocalDate.now().plusDays(1));
        
        assertFalse(resultatModificationApprouvee, "La modification doit échouer pour une candidature APPROUVEE");
        
        // Test modification d'une candidature inexistante
        boolean resultatModificationInexistante = gestionnaire.modifierCandidature(
            9999, "Description", 1000.0, LocalDate.now(), LocalDate.now().plusDays(1));
        
        assertFalse(resultatModificationInexistante, "La modification doit échouer pour une candidature inexistante");
    }
    
    /**
     * Test 3: Lister les projets actifs
     * Vérifie le filtrage des projets selon leur statut
     */
    @Test
    void testListerProjetsActifs() {
        // Given - Le gestionnaire commence avec une liste vide
        List<Projet> projetsInitiaux = gestionnaire.listerProjetsActifs();
        assertEquals(0, projetsInitiaux.size(), "Aucun projet ne doit exister initialement");
        
        // Créer des candidatures et les transformer en projets manuellement pour le test
        // (Note: Dans le vrai système, cela se ferait via l'API STPM)
        Candidature candidature1 = gestionnaire.soumettreCandidat(
            prestataireTest, Arrays.asList(1), "Projet 1", 5000.0, 
            LocalDate.now(), LocalDate.now().plusDays(7));
        
        @SuppressWarnings("unused")
        Candidature candidature2 = gestionnaire.soumettreCandidat(
            prestataireTest, Arrays.asList(2), "Projet 2", 3000.0, 
            LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        
        @SuppressWarnings("unused")
        Candidature candidature3 = gestionnaire.soumettreCandidat(
            prestataireTest, Arrays.asList(3), "Projet 3", 8000.0, 
            LocalDate.now().plusDays(2), LocalDate.now().plusDays(10));
        
        // When - Vérifier que les candidatures sont bien créées
        List<Candidature> candidatures = gestionnaire.listerCandidatures();
        assertEquals(3, candidatures.size(), "Doit avoir exactement 3 candidatures");
        
        // Vérifier les statuts des candidatures
        for (Candidature c : candidatures) {
            assertEquals(StatutCandidature.SOUMISE, c.getStatut(), "Toutes les candidatures doivent être SOUMISE");
            assertTrue(c.peutEtreModifiee(), "Les candidatures SOUMISE doivent pouvoir être modifiées");
            assertTrue(c.peutEtreAnnulee(), "Les candidatures SOUMISE doivent pouvoir être annulées");
        }
        
        // Then - Vérifier qu'on peut trouver les candidatures par ID
        Candidature candidatureTrouvee = gestionnaire.trouverCandidatureParId(candidature1.getId());
        assertNotNull(candidatureTrouvee, "Doit pouvoir trouver la candidature par ID");
        assertEquals(candidature1.getId(), candidatureTrouvee.getId(), "L'ID doit correspondre");
        
        // Test recherche d'une candidature inexistante
        Candidature candidatureInexistante = gestionnaire.trouverCandidatureParId(9999);
        assertNull(candidatureInexistante, "Doit retourner null pour un ID inexistant");
        
        // Note: Les projets actifs seraient testés dans un test d'intégration
        // car ils nécessitent la validation STPM et la création de projets
        List<Projet> projetsActifs = gestionnaire.listerProjetsActifs();
        assertEquals(0, projetsActifs.size(), "Aucun projet actif tant que les candidatures ne sont pas approuvées");
    }
}