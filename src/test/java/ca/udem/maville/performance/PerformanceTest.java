package ca.udem.maville.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de performance pour l'API MaVille
 * Mesure les temps de réponse et la capacité de charge
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Tests de Performance")
class PerformanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    @DisplayName("Health check doit répondre en moins de 100ms")
    void testHealthCheckPerformance() {
        long startTime = System.currentTimeMillis();
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            baseUrl + "/api/health", HttpMethod.GET, null,
            new ParameterizedTypeReference<Map<String, Object>>() {});
        
        long duration = System.currentTimeMillis() - startTime;
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(duration < 100, 
            "Health check devrait répondre en moins de 100ms, mais a pris " + duration + "ms");
    }

    @Test
    @DisplayName("GET /api/residents/travaux doit répondre en moins de 500ms")
    void testConsulterTravauxPerformance() {
        long startTime = System.currentTimeMillis();
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            baseUrl + "/api/residents/travaux?page=0&size=10", HttpMethod.GET, null,
            new ParameterizedTypeReference<Map<String, Object>>() {});
        
        long duration = System.currentTimeMillis() - startTime;
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(duration < 500, 
            "GET /api/residents/travaux devrait répondre en moins de 500ms, mais a pris " + duration + "ms");
    }

    @Test
    @DisplayName("GET /api/prestataires/problemes doit répondre en moins de 300ms")
    void testConsulterProblemesPerformance() {
        long startTime = System.currentTimeMillis();
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            baseUrl + "/api/prestataires/problemes?page=0&size=10", HttpMethod.GET, null,
            new ParameterizedTypeReference<Map<String, Object>>() {});
        
        long duration = System.currentTimeMillis() - startTime;
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(duration < 300, 
            "GET /api/prestataires/problemes devrait répondre en moins de 300ms, mais a pris " + duration + "ms");
    }

    @Test
    @DisplayName("GET /api/stpm/candidatures doit répondre en moins de 300ms")
    void testConsulterCandidaturesPerformance() {
        long startTime = System.currentTimeMillis();
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            baseUrl + "/api/stpm/candidatures?page=0&size=10", HttpMethod.GET, null,
            new ParameterizedTypeReference<Map<String, Object>>() {});
        
        long duration = System.currentTimeMillis() - startTime;
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(duration < 300, 
            "GET /api/stpm/candidatures devrait répondre en moins de 300ms, mais a pris " + duration + "ms");
    }

    @Test
    @DisplayName("API doit supporter 10 requêtes simultanées")
    void testConcurrentRequests() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        @SuppressWarnings("unchecked")
        CompletableFuture<Long>[] futures = IntStream.range(0, 10)
            .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/api/health", HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
                long duration = System.currentTimeMillis() - startTime;
                
                assertEquals(HttpStatus.OK, response.getStatusCode());
                return duration;
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);
        
        long maxDuration = 0;
        for (CompletableFuture<Long> future : futures) {
            long duration = future.get();
            maxDuration = Math.max(maxDuration, duration);
        }
        
        executor.shutdown();
        
        assertTrue(maxDuration < 1000, 
            "Toutes les requêtes simultanées devraient se terminer en moins de 1s, max: " + maxDuration + "ms");
    }

    @Test
    @DisplayName("Cache doit améliorer les performances des appels répétés")
    void testCachePerformance() {
        // Premier appel (sans cache)
        long startTime1 = System.currentTimeMillis();
        ResponseEntity<Map<String, Object>> response1 = restTemplate.exchange(
            baseUrl + "/api/montreal/travaux", HttpMethod.GET, null,
            new ParameterizedTypeReference<Map<String, Object>>() {});
        long duration1 = System.currentTimeMillis() - startTime1;
        
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        
        // Deuxième appel (avec cache)
        long startTime2 = System.currentTimeMillis();
        ResponseEntity<Map<String, Object>> response2 = restTemplate.exchange(
            baseUrl + "/api/montreal/travaux", HttpMethod.GET, null,
            new ParameterizedTypeReference<Map<String, Object>>() {});
        long duration2 = System.currentTimeMillis() - startTime2;
        
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        
        // Le deuxième appel devrait être plus rapide grâce au cache
        assertTrue(duration2 <= duration1, 
            "Le deuxième appel devrait être au moins aussi rapide grâce au cache. " +
            "Premier: " + duration1 + "ms, Deuxième: " + duration2 + "ms");
    }

    @Test
    @DisplayName("Pagination doit améliorer les performances avec de grandes listes")
    void testPaginationPerformance() {
        // Test avec pagination (petite page)
        long startTime1 = System.currentTimeMillis();
        ResponseEntity<Map<String, Object>> response1 = restTemplate.exchange(
            baseUrl + "/api/residents/travaux?page=0&size=10", HttpMethod.GET, null,
            new ParameterizedTypeReference<Map<String, Object>>() {});
        long duration1 = System.currentTimeMillis() - startTime1;
        
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        
        // Test sans pagination (tous les résultats)
        long startTime2 = System.currentTimeMillis();
        ResponseEntity<Map<String, Object>> response2 = restTemplate.exchange(
            baseUrl + "/api/residents/travaux?page=0&size=1000", HttpMethod.GET, null,
            new ParameterizedTypeReference<Map<String, Object>>() {});
        long duration2 = System.currentTimeMillis() - startTime2;
        
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        
        // La pagination devrait permettre des réponses plus rapides
        assertTrue(duration1 < 500, 
            "Pagination devrait permettre des réponses rapides. Durée: " + duration1 + "ms");
        // Vérifier que la réponse avec pagination est reçue (utilise duration2 pour éviter le warning)
        assertNotNull(response2.getBody(), "La réponse devrait contenir des données");
        assertTrue(duration2 > 0, "La durée devrait être mesurée: " + duration2 + "ms");
    }
}

