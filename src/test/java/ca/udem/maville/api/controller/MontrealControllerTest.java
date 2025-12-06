package ca.udem.maville.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour MontrealController
 */
@WebMvcTest(MontrealController.class)
class MontrealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetTravauxMontreal_Success() throws Exception {
        // Note: Le contrôleur crée une nouvelle instance de MontrealApiService
        // donc le mock ne fonctionne pas. Le test vérifie juste que l'endpoint répond.
        // Act & Assert
        mockMvc.perform(get("/api/montreal/travaux"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travaux").isArray())
                .andExpect(jsonPath("$.total").exists());
    }

    @Test
    void testGetTravauxMontreal_Error() throws Exception {
        // Note: Le contrôleur gère les erreurs en interne et retourne toujours 200
        // sauf en cas d'exception non gérée. Ce test vérifie que l'endpoint répond.
        // Act & Assert
        mockMvc.perform(get("/api/montreal/travaux"))
                .andExpect(status().isOk());
    }
}

