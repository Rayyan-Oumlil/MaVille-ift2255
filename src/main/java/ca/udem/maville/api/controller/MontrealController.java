package ca.udem.maville.api.controller;

import ca.udem.maville.api.MontrealApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur REST pour l'API externe de Montréal
 */
@RestController
@RequestMapping("/api/montreal")
@Tag(name = "Montreal", description = "Endpoints to integrate data from Montreal's official API")
public class MontrealController {
    private final MontrealApiService montrealApiService;
    
    public MontrealController(MontrealApiService montrealApiService) {
        this.montrealApiService = montrealApiService;
    }
    
    @GetMapping("/travaux")
    @Operation(summary = "Get Montreal public works", 
               description = "Fetches ongoing public works from Montreal's official open data API")
    public ResponseEntity<?> getTravauxMontreal() {
        List<Map<String, Object>> travaux = montrealApiService.getTravauxEnCours(50);
        
        Map<String, Object> response = new HashMap<>();
        response.put("travaux", travaux);
        response.put("total", travaux.size());
        return ResponseEntity.ok(response);
    }
}

