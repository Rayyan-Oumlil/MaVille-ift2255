package ca.udem.maville.api.controller;

import ca.udem.maville.modele.*;
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
 * Tests d'intégration pour StpmController
 */
@WebMvcTest(StpmController.class)
class StpmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JsonStorage storage;

    @Autowired
    private ObjectMapper objectMapper;

    private Candidature candidatureTest;
    private Probleme problemeTest;
    private Prestataire prestataireTest;

    @BeforeEach
    void setUp() {
        prestataireTest = new Prestataire("NEQ123456789", "Entreprise Test", 
            "Contact", "514-555-0001", "contact@test.com");
        
        candidatureTest = new Candidature(prestataireTest, Arrays.asList(1), 
            "Projet test", 5000.0, LocalDate.now(), LocalDate.now().plusDays(7));
        candidatureTest.setId(1);
        candidatureTest.setStatut(StatutCandidature.SOUMISE);
        
        Resident resident = new Resident("Tremblay", "Marie", "514-555-0001", 
            "marie@test.com", "123 Rue Test");
        
        problemeTest = new Probleme("123 Rue Test", TypeTravaux.TRAVAUX_ROUTIERS, 
            "Description", resident);
        problemeTest.setId(1);
    }

    @Test
    void testConsulterCandidatures_Success() throws Exception {
        // Arrange
        when(storage.loadCandidatures()).thenReturn(Arrays.asList(candidatureTest));

        // Act & Assert
        mockMvc.perform(get("/api/stpm/candidatures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidatures").isArray())
                .andExpect(jsonPath("$.total").exists());
    }

    @Test
    @SuppressWarnings("null")
    void testValiderCandidature_Accepter() throws Exception {
        // Arrange
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("accepter", true);

        when(storage.loadCandidatures()).thenReturn(Arrays.asList(candidatureTest));
        when(storage.loadProblemes()).thenReturn(Arrays.asList(problemeTest));
        when(storage.loadProjets()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(put("/api/stpm/candidatures/1/valider")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Candidature acceptée et projet créé"))
                .andExpect(jsonPath("$.projetId").exists());

        verify(storage).saveCandidatures(anyList());
        verify(storage).saveProjets(anyList());
    }

    @Test
    @SuppressWarnings("null")
    void testValiderCandidature_Refuser() throws Exception {
        // Arrange
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("accepter", false);

        when(storage.loadCandidatures()).thenReturn(Arrays.asList(candidatureTest));

        // Act & Assert
        mockMvc.perform(put("/api/stpm/candidatures/1/valider")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Candidature refusée"));

        verify(storage).saveCandidatures(anyList());
    }

    @Test
    @SuppressWarnings("null")
    void testValiderCandidature_NotFound() throws Exception {
        // Arrange
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("accepter", true);

        when(storage.loadCandidatures()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(put("/api/stpm/candidatures/999/valider")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @SuppressWarnings("null")
    void testModifierPrioriteProbleme_Success() throws Exception {
        // Arrange
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("priorite", "ELEVEE");

        when(storage.loadProblemes()).thenReturn(Arrays.asList(problemeTest));

        // Act & Assert
        mockMvc.perform(put("/api/stpm/problemes/1/priorite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(storage).saveProblemes(anyList());
    }

    @Test
    void testConsulterProblemes_Success() throws Exception {
        // Arrange
        when(storage.loadProblemes()).thenReturn(Arrays.asList(problemeTest));

        // Act & Assert
        mockMvc.perform(get("/api/stpm/problemes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problemes").isArray())
                .andExpect(jsonPath("$.total").exists());
    }

    @Test
    void testConsulterNotifications_Success() throws Exception {
        // Arrange
        Notification notification = new Notification("stpm@montreal.ca", 
            "Nouveau problème signalé", "NOUVEAU_PROBLEME", 1, "Plateau");

        when(storage.loadNotifications()).thenReturn(Arrays.asList(notification));

        // Act & Assert
        mockMvc.perform(get("/api/stpm/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isArray());
    }
}

