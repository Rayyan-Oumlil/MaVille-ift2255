package ca.udem.maville.api.controller;

import ca.udem.maville.api.dto.ErrorResponse;
import ca.udem.maville.api.dto.PaginatedResponse;
import ca.udem.maville.api.exception.ResourceNotFoundException;
import ca.udem.maville.modele.*;
import ca.udem.maville.storage.JsonStorage;
// Swagger annotations (décommenter une fois la dépendance téléchargée)
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur REST pour les agents STPM
 */
@RestController
@RequestMapping("/api/stpm")
// @Tag(name = "STPM", description = "Endpoints pour les agents STPM : valider candidatures, gérer problèmes et priorités")
public class StpmController {
    private static final Logger logger = LoggerFactory.getLogger(StpmController.class);
    
    private final JsonStorage storage;
    
    public StpmController(JsonStorage storage) {
        this.storage = storage;
    }
    
    @GetMapping("/candidatures")
    // @Operation(summary = "Consulter les candidatures", 
    //            description = "Retourne la liste paginée de toutes les candidatures soumises par les prestataires")
    public ResponseEntity<?> consulterCandidatures(
            // @Parameter(description = "Numéro de page (0-indexé)") 
            @RequestParam(defaultValue = "0") int page,
            // @Parameter(description = "Taille de la page") 
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Candidature> candidatures = storage.loadCandidatures();
            
            // Pagination
            int total = candidatures.size();
            int start = page * size;
            int end = Math.min(start + size, total);
            
            List<Candidature> pageCandidatures = (start < total) ? 
                candidatures.subList(start, end) : new ArrayList<>();
            
            List<Map<String, Object>> candidaturesJson = new ArrayList<>();
            for (Candidature c : pageCandidatures) {
                Map<String, Object> cJson = new HashMap<>();
                cJson.put("id", c.getId());
                cJson.put("prestataire", c.getPrestataire().getNomEntreprise());
                cJson.put("statut", c.getStatut().getDescription());
                cJson.put("description", c.getDescriptionProjet());
                candidaturesJson.add(cJson);
            }
            
            PaginatedResponse<Map<String, Object>> response = new PaginatedResponse<>(
                candidaturesJson, page, size, total
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la consultation des candidatures", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERROR", "Erreur: " + e.getMessage(), 500, "/api/stpm/candidatures"));
        }
    }
    
    @PutMapping("/candidatures/{id}/valider")
    // @Operation(summary = "Valider ou refuser une candidature", 
    //            description = "Permet à un agent STPM d'accepter ou refuser une candidature. L'acceptation crée automatiquement un projet.")
    // Note: @Transactional non applicable avec stockage JSON (nécessiterait migration vers base de données)
    public ResponseEntity<?> validerCandidature(
            // @Parameter(description = "ID de la candidature") 
            @PathVariable int id, 
            @RequestBody Map<String, Object> requestData) {
        Boolean accepter = (Boolean) requestData.get("accepter");
        List<Candidature> candidatures = storage.loadCandidatures();
        
        Candidature candidature = candidatures.stream()
            .filter(c -> c.getId() == id)
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'ID: " + id));
        
        if (accepter != null && accepter) {
            candidature.setStatut(StatutCandidature.APPROUVEE);
            storage.saveCandidatures(candidatures); // Sauvegarder le changement de statut
            
            // Créer projet automatiquement
            List<Probleme> problemes = storage.loadProblemes();
            List<Probleme> problemesAssocies = problemes.stream()
                .filter(p -> candidature.getProblemesVises().contains(p.getId()))
                .toList();
            
            Projet nouveauProjet = new Projet(candidature, problemesAssocies);
            
            List<Projet> projets = storage.loadProjets();
            projets.add(nouveauProjet);
            storage.saveProjets(projets);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidature acceptée et projet créé");
            response.put("projetId", nouveauProjet.getId());
            return ResponseEntity.ok(response);
        } else {
            candidature.setStatut(StatutCandidature.REJETEE);
            storage.saveCandidatures(candidatures);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidature refusée");
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/problemes")
    // @Operation(summary = "Consulter les problèmes", 
    //            description = "Retourne la liste paginée de tous les problèmes signalés")
    public ResponseEntity<?> consulterProblemes(
            // @Parameter(description = "Numéro de page (0-indexé)") 
            @RequestParam(defaultValue = "0") int page,
            // @Parameter(description = "Taille de la page") 
            @RequestParam(defaultValue = "10") int size) {
        List<Probleme> problemes = storage.loadProblemes();
        
        // Pagination
        int total = problemes.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        
        List<Probleme> pageProblemes = (start < total) ? 
            problemes.subList(start, end) : new ArrayList<>();
        
        List<Map<String, Object>> problemesJson = new ArrayList<>();
        for (Probleme p : pageProblemes) {
            Map<String, Object> pJson = new HashMap<>();
            pJson.put("id", p.getId());
            pJson.put("lieu", p.getLieu());
            pJson.put("description", p.getDescription());
            pJson.put("type", p.getTypeProbleme().getDescription());
            pJson.put("priorite", p.getPriorite().getDescription());
            pJson.put("declarant", p.getDeclarant().getNomComplet());
            pJson.put("date", p.getDateSignalement().toString());
            problemesJson.add(pJson);
        }
        
        PaginatedResponse<Map<String, Object>> response = new PaginatedResponse<>(
            problemesJson, page, size, total
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/notifications")
    public ResponseEntity<?> consulterNotifications() {
        try {
            List<Notification> toutesNotifications = storage.loadNotifications();
            // Filtrer les notifications STPM (typeDestinataire = "STPM")
            List<Notification> notificationsStmp = toutesNotifications.stream()
                .filter(n -> n.estPourStmp())
                .toList();
            
            List<Map<String, Object>> notificationsJson = new ArrayList<>();
            for (Notification n : notificationsStmp) {
                Map<String, Object> nJson = new HashMap<>();
                nJson.put("message", n.getMessage());
                nJson.put("type", n.getTypeChangement());
                nJson.put("date", n.getDateCreation().toString());
                nJson.put("lue", n.isLu());
                notificationsJson.add(nJson);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationsJson);
            response.put("total", notificationsJson.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la consultation des notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERROR", "Erreur: " + e.getMessage(), 500, "/api/stpm/notifications"));
        }
    }
    
    @PutMapping("/problemes/{id}/priorite")
    // @Operation(summary = "Modifier la priorité d'un problème", 
    //            description = "Permet à un agent STPM de modifier la priorité d'un problème (FAIBLE, MOYENNE, ELEVEE)")
    public ResponseEntity<?> modifierPrioriteProbleme(
            // @Parameter(description = "ID du problème") 
            @PathVariable int id, 
            @RequestBody Map<String, Object> requestData) {
        try {
            String prioriteStr = (String) requestData.get("priorite");
            Priorite nouvellePriorite = Priorite.valueOf(prioriteStr);
            
            List<Probleme> problemes = storage.loadProblemes();
            Probleme probleme = problemes.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Problème non trouvé avec l'ID: " + id));
            
            probleme.setPriorite(nouvellePriorite);
            storage.saveProblemes(problemes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Priorité modifiée");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la modification de la priorité", e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("ERROR", "Erreur: " + e.getMessage(), 400, "/api/stpm/problemes/" + id + "/priorite"));
        }
    }
}

