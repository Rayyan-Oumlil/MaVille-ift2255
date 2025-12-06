package ca.udem.maville.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour les endpoints de santé
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Endpoints pour vérifier l'état de santé de l'API")
public class HealthController {
    
    @GetMapping("/health")
    @Operation(summary = "Vérifier l'état de santé de l'API", 
               description = "Retourne le statut de l'API et des informations de base")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "MaVille API is running");
        response.put("version", "1.0");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Informations sur l'API", 
               description = "Retourne les informations générales sur l'API MaVille")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bienvenue sur l'API MaVille");
        response.put("version", "1.0");
        response.put("endpoints", Map.of(
            "santé", "GET /api/health",
            "résidents", "POST /api/residents/problemes, GET /api/residents/travaux",
            "prestataires", "GET /api/prestataires/problemes, POST /api/prestataires/candidatures"
        ));
        return ResponseEntity.ok(response);
    }
}

