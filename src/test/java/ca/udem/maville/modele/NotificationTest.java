package ca.udem.maville.modele;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour Notification
 * Couvre les fonctionnalités du système de notifications
 */
class NotificationTest {
    
    private String residentEmail;
    private String message;
    private String typeChangement;
    private int projetId;
    private String quartier;
    
    @BeforeEach
    void setUp() {
        residentEmail = "test@email.com";
        message = "Nouveau projet dans votre quartier";
        typeChangement = "NOUVEAU_PROJET";
        projetId = 123;
        quartier = "Plateau";
    }
    
    /**
     * Test 1: Création d'une notification
     * Vérifie que tous les attributs sont correctement initialisés
     */
    @Test
    void testCreationNotification() {
        // Given & When
        Notification notification = new Notification(residentEmail, message, typeChangement, projetId, quartier);
        
        // Then
        assertNotNull(notification, "La notification ne doit pas être null");
        assertEquals(residentEmail, notification.getResidentEmail(), "L'email du résident doit correspondre");
        assertEquals(message, notification.getMessage(), "Le message doit correspondre");
        assertEquals(typeChangement, notification.getTypeChangement(), "Le type de changement doit correspondre");
        assertEquals(projetId, notification.getProjetId(), "L'ID du projet doit correspondre");
        assertEquals(quartier, notification.getQuartier(), "Le quartier doit correspondre");
        
        // Vérifier les valeurs par défaut
        assertFalse(notification.isLu(), "La notification doit être non lue par défaut");
        assertNotNull(notification.getDateCreation(), "La date de création doit être définie");
        
        // Vérifier que la date de création est récente (dans les 5 dernières secondes)
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime dateCreation = notification.getDateCreation();
        assertTrue(dateCreation.isBefore(maintenant.plusSeconds(1)), "La date de création doit être récente");
        assertTrue(dateCreation.isAfter(maintenant.minusSeconds(5)), "La date de création ne doit pas être trop ancienne");
    }
    
    /**
     * Test 2: Marquer une notification comme lue
     * Vérifie le changement d'état de la notification
     */
    @Test
    void testMarquerCommeLue() {
        // Given
        Notification notification = new Notification(residentEmail, message, typeChangement, projetId, quartier);
        assertFalse(notification.isLu(), "La notification doit être non lue initialement");
        
        // When
        notification.setLu(true);
        
        // Then
        assertTrue(notification.isLu(), "La notification doit être marquée comme lue");
        
        // Test du changement inverse
        notification.setLu(false);
        assertFalse(notification.isLu(), "La notification doit pouvoir être marquée comme non lue");
    }
    
    /**
     * Test 3: Types de changements valides
     * Vérifie les différents types de notifications possibles
     */
    @Test
    void testTypesChangementValides() {
        // Test des différents types de changement
        String[] typesValides = {
            "NOUVEAU_PROJET",
            "STATUT_CHANGE", 
            "PRIORITE_CHANGE",
            "DATE_CHANGE",
            "PROBLEME_RESOLU"
        };
        
        for (String type : typesValides) {
            // Given & When
            Notification notification = new Notification(
                "resident" + type + "@email.com", 
                "Message pour " + type, 
                type, 
                100 + type.hashCode(), 
                "Quartier-" + type
            );
            
            // Then
            assertEquals(type, notification.getTypeChangement(), 
                "Le type de changement " + type + " doit être correctement défini");
            assertFalse(notification.isLu(), "Toute nouvelle notification doit être non lue");
            assertNotNull(notification.getDateCreation(), "La date de création doit toujours être définie");
        }
    }
    
    
    
}