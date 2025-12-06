package ca.udem.maville.api.controller;

import ca.udem.maville.api.service.ApiService;
import ca.udem.maville.modele.*;
import ca.udem.maville.service.GestionnaireProblemes;
import ca.udem.maville.storage.JsonStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour ResidentController
 * Teste tous les endpoints REST pour les résidents
 */
@WebMvcTest(ResidentController.class)
class ResidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GestionnaireProblemes gestionnaireProblemes;

    @MockBean
    private JsonStorage storage;

    @MockBean
    private ApiService apiService;

    @Autowired
    private ObjectMapper objectMapper;

    private Resident residentTest;
    private Probleme problemeTest;

    @BeforeEach
    void setUp() {
        residentTest = new Resident("Tremblay", "Marie", "514-555-0001", 
            "marie@test.com", "123 Rue Test, Plateau");
        
        problemeTest = new Probleme("123 Rue Test, Plateau", 
            TypeTravaux.ENTRETIEN_URBAIN, "Trou dans la route", residentTest);
        problemeTest.setId(1);
        problemeTest.setPriorite(Priorite.MOYENNE);
    }

    @Test
    @SuppressWarnings("null")
    void testSignalerProbleme_Success() throws Exception {
        // Arrange
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("lieu", "123 Rue Test, Plateau");
        requestData.put("description", "Trou dans la route qui nécessite réparation");
        requestData.put("residentId", "marie@test.com");

        when(apiService.extraireQuartier(anyString())).thenReturn("Plateau");
        when(gestionnaireProblemes.signalerProbleme(anyString(), any(), anyString(), any()))
            .thenReturn(problemeTest);
        when(gestionnaireProblemes.listerProblemes()).thenReturn(Arrays.asList(problemeTest));
        when(storage.loadAbonnements()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(post("/api/residents/problemes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.problemeId").exists())
                .andExpect(jsonPath("$.quartierAbonnement").value("Plateau"));

        verify(gestionnaireProblemes).signalerProbleme(anyString(), any(), anyString(), any());
        verify(storage).saveProblemes(anyList());
    }

    @Test
    @SuppressWarnings("null")
    void testSignalerProbleme_ValidationError_LieuManquant() throws Exception {
        // Arrange
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("description", "Description du problème");
        requestData.put("residentId", "marie@test.com");

        // Act & Assert
        mockMvc.perform(post("/api/residents/problemes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Le lieu est requis"));

        verify(gestionnaireProblemes, never()).signalerProbleme(anyString(), any(), anyString(), any());
    }

    @Test
    @SuppressWarnings("null")
    void testSignalerProbleme_ValidationError_DescriptionTropCourte() throws Exception {
        // Arrange
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("lieu", "123 Rue Test");
        requestData.put("description", "Court"); // Moins de 10 caractères
        requestData.put("residentId", "marie@test.com");

        // Act & Assert
        mockMvc.perform(post("/api/residents/problemes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("La description doit contenir au moins 10 caractères"));
    }

    @Test
    void testConsulterTravaux_Success() throws Exception {
        // Arrange
        Projet projet = new Projet();
        projet.setId(1);
        projet.setLocalisation("123 Rue Test, Plateau");
        projet.setTypeTravail(TypeTravaux.ENTRETIEN_URBAIN);
        projet.setDescriptionProjet("Réparation de la route");
        projet.setDateDebutPrevue(LocalDate.now().plusDays(7));
        projet.setDateFinPrevue(LocalDate.now().plusDays(14));
        projet.setCout(5000.0);
        projet.setStatut(StatutProjet.APPROUVE);

        when(storage.loadProjets()).thenReturn(Arrays.asList(projet));
        when(apiService.extraireQuartier(anyString())).thenReturn("Plateau");

        // Act & Assert
        mockMvc.perform(get("/api/residents/travaux"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travaux").isArray())
                .andExpect(jsonPath("$.total").exists());
    }

    @Test
    void testConsulterTravaux_WithFilters() throws Exception {
        // Arrange
        Projet projet = new Projet();
        projet.setId(1);
        projet.setLocalisation("123 Rue Test, Plateau");
        projet.setTypeTravail(TypeTravaux.ENTRETIEN_URBAIN);
        projet.setDescriptionProjet("Réparation");
        projet.setStatut(StatutProjet.APPROUVE);

        when(storage.loadProjets()).thenReturn(Arrays.asList(projet));
        when(apiService.extraireQuartier(anyString())).thenReturn("Plateau");

        // Act & Assert
        mockMvc.perform(get("/api/residents/travaux")
                .param("quartier", "Plateau")
                .param("type", "Entretien urbain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travaux").isArray());
    }

    @Test
    void testConsulterNotifications_Success() throws Exception {
        // Arrange
        Notification notification = new Notification("marie@test.com", 
            "Nouveau projet approuvé", "NOUVEAU_PROJET", 1, "Plateau");
        notification.setLu(false);

        when(storage.loadNotifications()).thenReturn(Arrays.asList(notification));

        // Act & Assert
        mockMvc.perform(get("/api/residents/marie@test.com/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.non_lues").exists());
    }

    @Test
    @SuppressWarnings("null")
    void testCreerAbonnement_Success() throws Exception {
        // Arrange
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("type", "QUARTIER");
        requestData.put("valeur", "Plateau");

        when(storage.loadAbonnements()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(post("/api/residents/marie@test.com/abonnements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Abonnement créé avec succès"));

        verify(storage).saveAbonnements(anyList());
    }

    @Test
    void testConsulterAbonnements_Success() throws Exception {
        // Arrange
        Abonnement abonnement = new Abonnement("marie@test.com", "QUARTIER", "Plateau");
        when(storage.loadAbonnements()).thenReturn(Arrays.asList(abonnement));

        // Act & Assert
        mockMvc.perform(get("/api/residents/marie@test.com/abonnements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abonnements").isArray())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void testMarquerNotificationsLues_Success() throws Exception {
        // Arrange
        Notification notification = new Notification("marie@test.com", 
            "Message", "TYPE", 1, "Plateau");
        notification.setLu(false);

        when(storage.loadNotifications()).thenReturn(Arrays.asList(notification));

        // Act & Assert
        mockMvc.perform(put("/api/residents/marie@test.com/notifications/marquer-lu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notifications marquées comme lues"));

        verify(storage).saveNotifications(anyList());
    }

    @Test
    @SuppressWarnings("null")
    void testModifierPreferences_Success() throws Exception {
        // Arrange
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("frequence", "QUOTIDIENNE");
        preferences.put("actives", true);

        // Act & Assert
        mockMvc.perform(put("/api/residents/marie@test.com/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(preferences)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.preferences").exists());

        verify(storage).savePreferencesNotification(eq("marie@test.com"), any());
    }
}

