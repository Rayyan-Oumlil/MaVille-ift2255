package ca.udem.maville.modele;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour Projet
 * Couvre les fonctionnalités de gestion des projets de travaux
 */
class ProjetTest {
    
    private Candidature candidatureTest;
    private List<Probleme> problemesTest;
    private Prestataire prestataireTest;
    private Resident residentTest;
    
    @BeforeEach
    void setUp() {
        // Créer les objets de test
        prestataireTest = new Prestataire("NEQ123456789", "Entreprise Test", 
            "Contact Test", "514-555-0001", "contact@test.com");
        
        residentTest = new Resident("Test", "User", "514-555-0002", 
            "user@test.com", "123 Test Street, Plateau");
        
        Probleme probleme1 = new Probleme("Rue Test 1", TypeTravaux.TRAVAUX_ROUTIERS, 
            "Description problème 1", residentTest);
        probleme1.setId(1);
        probleme1.setPriorite(Priorite.ELEVEE);
        
        Probleme probleme2 = new Probleme("Rue Test 2", TypeTravaux.TRAVAUX_ROUTIERS, 
            "Description problème 2", residentTest);
        probleme2.setId(2);
        probleme2.setPriorite(Priorite.MOYENNE);
        
        problemesTest = Arrays.asList(probleme1, probleme2);
        
        candidatureTest = new Candidature(
            prestataireTest,
            Arrays.asList(1, 2),
            "Projet de test pour réparation routière",
            15000.0,
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14)
        );
    }
    
    /**
     * Test 1: Création d'un projet à partir d'une candidature
     * Vérifie que toutes les propriétés sont correctement initialisées
     */
    @Test
    void testCreationProjet() {
        // Given & When
        Projet projet = new Projet(candidatureTest, problemesTest);
        
        // Then - Vérifier les propriétés copiées de la candidature
        assertNotNull(projet, "Le projet ne doit pas être null");
        assertEquals(candidatureTest.getProblemesVises(), projet.getProblemesVises(), 
            "Les problèmes visés doivent correspondre");
        assertEquals(candidatureTest.getPrestataire(), projet.getPrestataire(), 
            "Le prestataire doit correspondre");
        assertEquals(candidatureTest.getDescriptionProjet(), projet.getDescriptionProjet(), 
            "La description doit correspondre");
        assertEquals(candidatureTest.getCoutEstime(), projet.getCout(), 0.01, 
            "Le coût doit correspondre");
        assertEquals(candidatureTest.getDateDebutPrevue(), projet.getDateDebutPrevue(), 
            "La date de début prévue doit correspondre");
        assertEquals(candidatureTest.getDateFinPrevue(), projet.getDateFinPrevue(), 
            "La date de fin prévue doit correspondre");
        
        // Vérifier les propriétés dérivées des problèmes
        assertEquals(problemesTest.get(0).getLieu(), projet.getLocalisation(), 
            "La localisation doit être celle du premier problème");
        assertEquals(problemesTest.get(0).getTypeProbleme(), projet.getTypeTravail(), 
            "Le type de travail doit être celui du premier problème");
        assertEquals(Priorite.ELEVEE, projet.getPriorite(), 
            "La priorité doit être la plus élevée des problèmes");
        
        // Vérifier les valeurs par défaut
        assertEquals(StatutProjet.APPROUVE, projet.getStatut(), 
            "Le statut initial doit être APPROUVE");
        assertNotNull(projet.getDateCreation(), "La date de création doit être définie");
        assertNotNull(projet.getDerniereMiseAJour(), "La date de dernière mise à jour doit être définie");
        assertEquals(problemesTest.size(), projet.getNombreRapports(), 
            "Le nombre de rapports doit correspondre au nombre de problèmes");
        
        // Vérifier que les dates réelles ne sont pas définies
        assertNull(projet.getDateDebutReelle(), "La date de début réelle ne doit pas être définie");
        assertNull(projet.getDateFinReelle(), "La date de fin réelle ne doit pas être définie");
        
        // Vérifier que l'ID est auto-généré
        assertTrue(projet.getId() > 0, "L'ID doit être auto-généré et positif");
    }
    
    /**
     * Test 2: Démarrer un projet
     * Vérifie la transition de APPROUVE vers EN_COURS
     */
    @Test
    void testDemarrerProjet() {
        // Given
        Projet projet = new Projet(candidatureTest, problemesTest);
        assertEquals(StatutProjet.APPROUVE, projet.getStatut(), "Le projet doit être APPROUVE initialement");
        assertNull(projet.getDateDebutReelle(), "La date de début réelle ne doit pas être définie");
        
        LocalDateTime avant = LocalDateTime.now().minusSeconds(1);
        
        // When
        projet.demarrer();
        
        LocalDateTime apres = LocalDateTime.now().plusSeconds(1);
        
        // Then
        assertEquals(StatutProjet.EN_COURS, projet.getStatut(), "Le statut doit passer à EN_COURS");
        assertNotNull(projet.getDateDebutReelle(), "La date de début réelle doit être définie");
        assertEquals(LocalDate.now(), projet.getDateDebutReelle(), 
            "La date de début réelle doit être aujourd'hui");
        
        // Vérifier que la dernière mise à jour a été mise à jour
        assertTrue(projet.getDerniereMiseAJour().isAfter(avant), 
            "La dernière mise à jour doit être récente");
        assertTrue(projet.getDerniereMiseAJour().isBefore(apres), 
            "La dernière mise à jour doit être récente");
        
        // Test qu'on ne peut pas redémarrer un projet déjà en cours
        LocalDate premiereDateDebut = projet.getDateDebutReelle();
        
        try {
            Thread.sleep(10); // Petite pause pour différencier les timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        projet.demarrer(); // Tentative de redémarrage
        
        // Le statut et les dates ne doivent pas changer
        assertEquals(StatutProjet.EN_COURS, projet.getStatut(), 
            "Le statut doit rester EN_COURS");
        assertEquals(premiereDateDebut, projet.getDateDebutReelle(), 
            "La date de début réelle ne doit pas changer");
    }
    
    /**
     * Test 3: Terminer un projet
     * Vérifie la transition vers TERMINE et la gestion des dates
     */
    @Test
    void testTerminerProjet() {
        // Given - Démarrer d'abord le projet
        Projet projet = new Projet(candidatureTest, problemesTest);
        projet.demarrer();
        assertEquals(StatutProjet.EN_COURS, projet.getStatut(), "Le projet doit être EN_COURS");
        
        LocalDateTime avant = LocalDateTime.now().minusSeconds(1);
        
        // When
        projet.terminer();
        
        LocalDateTime apres = LocalDateTime.now().plusSeconds(1);
        
        // Then
        assertEquals(StatutProjet.TERMINE, projet.getStatut(), "Le statut doit passer à TERMINE");
        assertNotNull(projet.getDateFinReelle(), "La date de fin réelle doit être définie");
        assertEquals(LocalDate.now(), projet.getDateFinReelle(), 
            "La date de fin réelle doit être aujourd'hui");
        
        // Vérifier que la dernière mise à jour a été mise à jour
        assertTrue(projet.getDerniereMiseAJour().isAfter(avant), 
            "La dernière mise à jour doit être récente");
        assertTrue(projet.getDerniereMiseAJour().isBefore(apres), 
            "La dernière mise à jour doit être récente");
        
        // Test qu'on ne peut pas terminer un projet déjà terminé
        LocalDate premiereDateFin = projet.getDateFinReelle();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        projet.terminer(); // Tentative de re-terminer
        
        assertEquals(StatutProjet.TERMINE, projet.getStatut(), "Le statut doit rester TERMINE");
        assertEquals(premiereDateFin, projet.getDateFinReelle(), 
            "La date de fin réelle ne doit pas changer");
    }
    
    /**
     * Test 4: Cycle complet de vie d'un projet
     * Teste toutes les transitions de statut possibles
     */
    @Test
    void testCycleCompletProjet() {
        // Given
        Projet projet = new Projet(candidatureTest, problemesTest);
        
        // Phase 1: APPROUVE -> EN_COURS
        assertEquals(StatutProjet.APPROUVE, projet.getStatut());
        projet.demarrer();
        assertEquals(StatutProjet.EN_COURS, projet.getStatut());
        assertNotNull(projet.getDateDebutReelle());
        
        // Phase 2: EN_COURS -> SUSPENDU
        projet.suspendre();
        assertEquals(StatutProjet.SUSPENDU, projet.getStatut());
        
        // Phase 3: SUSPENDU -> EN_COURS (reprise)
        projet.reprendre();
        assertEquals(StatutProjet.EN_COURS, projet.getStatut());
        
        // Phase 4: EN_COURS -> TERMINE
        projet.terminer();
        assertEquals(StatutProjet.TERMINE, projet.getStatut());
        assertNotNull(projet.getDateFinReelle());
        
        // Vérifier que les dates sont cohérentes
        assertTrue(projet.getDateDebutReelle().isBefore(projet.getDateFinReelle()) || 
                  projet.getDateDebutReelle().isEqual(projet.getDateFinReelle()), 
                  "La date de début doit être avant ou égale à la date de fin");
    }

    
    /**
     * Test 5: Modification des propriétés et mise à jour automatique
     * Vérifie que les setters mettent à jour la date de dernière modification
     */
    @Test
    void testModificationProprietesAvecMiseAJour() {
        // Given
        Projet projet = new Projet(candidatureTest, problemesTest);
        LocalDateTime dateInitiale = projet.getDerniereMiseAJour();
        
        try {
            Thread.sleep(10); // Pause pour différencier les timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Test modification du statut
        projet.setStatut(StatutProjet.EN_COURS);
        assertTrue(projet.getDerniereMiseAJour().isAfter(dateInitiale), 
            "Changer le statut doit mettre à jour la date de dernière modification");
        
        // Test modification de la date de début prévue
        LocalDateTime apresStatut = projet.getDerniereMiseAJour();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        projet.setDateDebutPrevue(LocalDate.now().plusDays(1));
        assertTrue(projet.getDerniereMiseAJour().isAfter(apresStatut), 
            "Changer la date de début prévue doit mettre à jour la date de dernière modification");
        
        // Test modification de la date de fin prévue
        LocalDateTime apresDateDebut = projet.getDerniereMiseAJour();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        projet.setDateFinPrevue(LocalDate.now().plusDays(10));
        assertTrue(projet.getDerniereMiseAJour().isAfter(apresDateDebut), 
            "Changer la date de fin prévue doit mettre à jour la date de dernière modification");
    }
}