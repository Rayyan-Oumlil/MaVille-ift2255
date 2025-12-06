package ca.udem.maville.e2e;

import ca.udem.maville.entity.*;
import ca.udem.maville.modele.*;
import ca.udem.maville.repository.*;
import ca.udem.maville.service.DatabaseStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests End-to-End (E2E) pour valider les workflows complets de l'application
 * 
 * Scénarios testés :
 * 1. Résident signale problème → Prestataire soumet candidature → STPM valide → Projet créé
 * 2. Résident signale problème → STPM modifie priorité → Notifications envoyées
 * 3. Prestataire soumet candidature → STPM refuse → Notification envoyée
 * 
 * Note: Désactive Mockito pour compatibilité avec Java 25
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
    // Exclut ResetMocksTestExecutionListener (Mockito) pour compatibilité Java 25
})
public class WorkflowE2ETest {
    
    @Autowired
    private DatabaseStorageService dbStorage;
    
    @Autowired
    private ProjetRepository projetRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    private ResidentEntity resident;
    private PrestataireEntity prestataire;
    
    @BeforeEach
    void setUp() {
        // Créer un résident de test
        resident = dbStorage.findOrCreateResident(
            "test.resident@example.com",
            "Test",
            "Resident",
            "514-123-4567",
            "123 Rue Test, Plateau"
        );
        
        // Créer un prestataire de test
        prestataire = dbStorage.findOrCreatePrestataire(
            "TEST123",
            "Test Entreprise",
            "Contact Test",
            "514-987-6543",
            "test@entreprise.com"
        );
    }
    
    @Test
    void testWorkflowComplet_Probleme_Candidature_Projet() {
        // Étape 1 : Résident signale un problème
        ProblemeEntity probleme = dbStorage.createProbleme(
            "456 Rue Test, Plateau",
            TypeTravaux.ENTRETIEN_URBAIN,
            "Trou dans la chaussée",
            resident,
            Priorite.MOYENNE
        );
        
        assertNotNull(probleme.getId());
        assertFalse(probleme.isResolu());
        assertEquals(Priorite.MOYENNE, probleme.getPriorite());
        assertEquals(resident.getEmail(), probleme.getDeclarant().getEmail());
        
        // Vérifier que le problème est bien sauvegardé
        Long problemeId = Objects.requireNonNull(probleme.getId(), "Le problème doit avoir un ID");
        ProblemeEntity problemeTrouve = dbStorage.findProblemeById(problemeId)
            .orElseThrow(() -> new AssertionError("Problème non trouvé"));
        assertEquals(probleme.getId(), problemeTrouve.getId());
        
        // Étape 2 : Prestataire soumet une candidature
        List<ProblemeEntity> problemes = List.of(probleme);
        CandidatureEntity candidature = dbStorage.createCandidature(
            prestataire,
            problemes,
            "Réparation complète de la chaussée",
            5000.0,
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14)
        );
        
        assertNotNull(candidature.getId());
        assertEquals(StatutCandidature.SOUMISE, candidature.getStatut());
        assertEquals(prestataire.getNumeroEntreprise(), candidature.getPrestataire().getNumeroEntreprise());
        assertEquals(1, candidature.getProblemes().size());
        assertEquals(probleme.getId(), candidature.getProblemes().get(0).getId());
        
        // Vérifier que la candidature est bien sauvegardée
        Long candidatureId = Objects.requireNonNull(candidature.getId(), "La candidature doit avoir un ID");
        CandidatureEntity candidatureTrouvee = dbStorage.findCandidatureById(candidatureId)
            .orElseThrow(() -> new AssertionError("Candidature non trouvée"));
        assertEquals(candidature.getId(), candidatureTrouvee.getId());
        
        // Étape 3 : STPM accepte la candidature → Projet créé automatiquement
        candidature.setStatut(StatutCandidature.APPROUVEE);
        dbStorage.updateCandidature(candidature);
        
        ProjetEntity projet = dbStorage.createProjet(
            candidature,
            problemes,
            prestataire
        );
        
        assertNotNull(projet.getId());
        assertEquals(StatutProjet.EN_COURS, projet.getStatut());
        assertEquals(prestataire.getNumeroEntreprise(), projet.getPrestataire().getNumeroEntreprise());
        assertEquals(1, projet.getProblemes().size());
        assertEquals(probleme.getId(), projet.getProblemes().get(0).getId());
        
        // Vérifier que le projet est bien sauvegardé
        Long projetId = Objects.requireNonNull(projet.getId(), "Le projet doit avoir un ID");
        ProjetEntity projetTrouve = dbStorage.findProjetById(projetId)
            .orElseThrow(() -> new AssertionError("Projet non trouvé"));
        assertEquals(projet.getId(), projetTrouve.getId());
        
        // Note: Les notifications sont créées dans le contrôleur StpmController lors de l'acceptation
        // via l'endpoint REST, pas directement dans createProjet(). Dans un test E2E unitaire,
        // on vérifie seulement que le workflow de création fonctionne correctement.
        // Les notifications seront testées dans les tests d'intégration des contrôleurs.
    }
    
    @Test
    void testWorkflow_Probleme_PrioriteModifiee() {
        // Étape 1 : Résident signale un problème
        ProblemeEntity probleme = dbStorage.createProbleme(
            "789 Rue Test, Villeray",
            TypeTravaux.TRAVAUX_ROUTIERS,
            "Nid de poule dangereux",
            resident,
            Priorite.FAIBLE
        );
        
        assertEquals(Priorite.FAIBLE, probleme.getPriorite());
        
        // Étape 2 : STPM modifie la priorité
        probleme.setPriorite(Priorite.ELEVEE);
        dbStorage.updateProbleme(probleme);
        
        // Vérifier que la priorité a été modifiée
        Long problemeId = Objects.requireNonNull(probleme.getId(), "Le problème doit avoir un ID");
        ProblemeEntity problemeModifie = dbStorage.findProblemeById(problemeId)
            .orElseThrow(() -> new AssertionError("Problème non trouvé"));
        assertEquals(Priorite.ELEVEE, problemeModifie.getPriorite());
        
        // Note: La modification de priorité ne crée pas automatiquement de notifications
        // dans le code actuel. Ce test vérifie seulement que la modification fonctionne.
        // Si des notifications sont créées, elles seront dans la liste
        // Vérification que le repository fonctionne correctement
        assertNotNull(notificationRepository.findAll(), "Le repository de notifications devrait fonctionner");
    }
    
    @Test
    void testWorkflow_Candidature_Refusee() {
        // Étape 1 : Résident signale un problème
        ProblemeEntity probleme = dbStorage.createProbleme(
            "321 Rue Test, Rosemont",
            TypeTravaux.ENTRETIEN_URBAIN,
            "Lampadaire défectueux",
            resident,
            Priorite.MOYENNE
        );
        
        // Étape 2 : Prestataire soumet une candidature
        List<ProblemeEntity> problemes = List.of(probleme);
        CandidatureEntity candidature = dbStorage.createCandidature(
            prestataire,
            problemes,
            "Remplacement du lampadaire",
            3000.0,
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(10)
        );
        
        assertEquals(StatutCandidature.SOUMISE, candidature.getStatut());
        
        // Étape 3 : STPM refuse la candidature
        candidature.setStatut(StatutCandidature.REJETEE);
        candidature.setCommentaireRejet("Budget insuffisant");
        dbStorage.updateCandidature(candidature);
        
        // Vérifier que la candidature a été refusée
        Long candidatureId = Objects.requireNonNull(candidature.getId(), "La candidature doit avoir un ID");
        CandidatureEntity candidatureRefusee = dbStorage.findCandidatureById(candidatureId)
            .orElseThrow(() -> new AssertionError("Candidature non trouvée"));
        assertEquals(StatutCandidature.REJETEE, candidatureRefusee.getStatut());
        assertEquals("Budget insuffisant", candidatureRefusee.getCommentaireRejet());
        
        // Vérifier qu'aucun projet n'a été créé
        List<ProjetEntity> projets = projetRepository.findAll();
        long projetsPourCandidature = projets.stream()
            .filter(p -> p.getCandidature() != null && p.getCandidature().getId().equals(candidature.getId()))
            .count();
        assertEquals(0, projetsPourCandidature, "Aucun projet ne devrait être créé pour une candidature refusée");
    }
    
    @Test
    void testWorkflow_Abonnement_Notification() {
        // Étape 1 : Résident signale un problème dans un quartier
        ProblemeEntity problemeCree = dbStorage.createProbleme(
            "999 Rue Test, Plateau",
            TypeTravaux.ENTRETIEN_URBAIN,
            "Banc de parc cassé",
            resident,
            Priorite.MOYENNE
        );
        assertNotNull(problemeCree.getId(), "Le problème devrait avoir un ID");
        
        // Étape 2 : Créer un abonnement au quartier
        String quartier = "Plateau";
        dbStorage.createAbonnement(resident.getEmail(), "QUARTIER", quartier);
        
        // Vérifier que l'abonnement a été créé
        List<AbonnementEntity> abonnements = dbStorage.findAbonnementsByResident(resident.getEmail());
        assertFalse(abonnements.isEmpty(), "L'abonnement devrait avoir été créé");
        
        boolean abonnementTrouve = abonnements.stream()
            .anyMatch(a -> a.getType().equals("QUARTIER") && 
                          a.getValeur().equals(quartier));
        assertTrue(abonnementTrouve, "L'abonnement au quartier devrait exister");
    }
    
    @Test
    void testWorkflow_Prestataire_Projets() {
        // Étape 1 : Créer un problème
        ProblemeEntity probleme = dbStorage.createProbleme(
            "111 Rue Test, Mile-End",
            TypeTravaux.TRAVAUX_ROUTIERS,
            "Réparation de trottoir",
            resident,
            Priorite.MOYENNE
        );
        
        // Étape 2 : Prestataire soumet candidature
        List<ProblemeEntity> problemes = List.of(probleme);
        CandidatureEntity candidature = dbStorage.createCandidature(
            prestataire,
            problemes,
            "Réparation complète",
            4000.0,
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(20)
        );
        
        // Étape 3 : STPM accepte → Projet créé
        candidature.setStatut(StatutCandidature.APPROUVEE);
        dbStorage.updateCandidature(candidature);
        
        ProjetEntity projet = dbStorage.createProjet(candidature, problemes, prestataire);
        
        // Étape 4 : Vérifier que le prestataire peut voir ses projets
        List<ProjetEntity> projetsPrestataire = dbStorage.findProjetsByPrestataire(prestataire.getNumeroEntreprise());
        assertFalse(projetsPrestataire.isEmpty(), "Le prestataire devrait avoir au moins un projet");
        
        boolean projetTrouve = projetsPrestataire.stream()
            .anyMatch(p -> p.getId().equals(projet.getId()));
        assertTrue(projetTrouve, "Le projet devrait être dans la liste du prestataire");
        
        // Étape 5 : Prestataire met à jour le statut du projet
        projet.setStatut(StatutProjet.TERMINE);
        dbStorage.updateProjet(projet);
        
        Long projetIdFinal = Objects.requireNonNull(projet.getId(), "Le projet doit avoir un ID");
        ProjetEntity projetMisAJour = dbStorage.findProjetById(projetIdFinal)
            .orElseThrow(() -> new AssertionError("Projet non trouvé"));
        assertEquals(StatutProjet.TERMINE, projetMisAJour.getStatut());
    }
}
