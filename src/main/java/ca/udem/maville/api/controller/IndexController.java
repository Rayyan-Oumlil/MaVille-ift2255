package ca.udem.maville.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Contrôleur pour la route racine
 */
@RestController
public class IndexController {
    
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> index() {
        return ResponseEntity.ok(Map.of(
            "message", "MaVille API",
            "version", "1.0",
            "docs", "/swagger-ui.html"
        ));
    }
}

