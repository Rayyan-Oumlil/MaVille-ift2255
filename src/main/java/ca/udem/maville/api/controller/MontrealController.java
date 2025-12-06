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
@Tag(name = "Montréal", description = "Endpoints pour intégrer les données de l'API officielle de Montréal")
public class MontrealController {
    private final MontrealApiService montrealApiService;
    
    public MontrealController(MontrealApiService montrealApiService) {
        this.montrealApiService = montrealApiService;
    }
    
    @GetMapping("/travaux")
    @Operation(summary = "Récupérer les travaux de Montréal", 
               description = "Récupère les travaux en cours depuis l'API officielle de données ouvertes de Montréal")
    public ResponseEntity<?> getTravauxMontreal() {
        List<Map<String, Object>> travaux = montrealApiService.getTravauxEnCours(50);
        
        Map<String, Object> response = new HashMap<>();
        response.put("travaux", travaux);
        response.put("total", travaux.size());
        return ResponseEntity.ok(response);
    }
}

