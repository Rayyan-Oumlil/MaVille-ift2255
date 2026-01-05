package ca.udem.maville.api.controller;

import ca.udem.maville.api.dto.PaginatedResponse;
import ca.udem.maville.api.exception.ResourceNotFoundException;
import ca.udem.maville.entity.*;
import ca.udem.maville.modele.*;
import ca.udem.maville.service.DatabaseStorageService;
import ca.udem.maville.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur REST pour les agents STPM
 */
@RestController
@RequestMapping("/api/stpm")
@Tag(name = "STPM", description = "Endpoints for STPM agents: validate applications, manage problems and priorities")
public class StpmController {
    private static final Logger logger = LoggerFactory.getLogger(StpmController.class);
    
    private final DatabaseStorageService dbStorage;
    private final NotificationRepository notificationRepository;
    
    public StpmController(DatabaseStorageService dbStorage, NotificationRepository notificationRepository) {
        this.dbStorage = dbStorage;
        this.notificationRepository = notificationRepository;
    }
    
    @GetMapping("/candidatures")
    @Operation(summary = "View applications", 
               description = "Returns paginated list of all applications submitted by service providers")
    public ResponseEntity<?> consulterCandidatures(
            @Parameter(description = "Page number (0-indexed)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "10") 
            @RequestParam(defaultValue = "10") int size) {
        
        org.springframework.data.domain.Page<CandidatureEntity> pageResult = 
            dbStorage.findAllCandidatures(page, size);
        
        List<Map<String, Object>> candidaturesJson = new ArrayList<>();
        for (CandidatureEntity c : pageResult.getContent()) {
            Map<String, Object> cJson = new HashMap<>();
            cJson.put("id", c.getId());
            cJson.put("prestataire", c.getPrestataire().getNomEntreprise());
            cJson.put("statut", c.getStatut().getDescription());
            cJson.put("description", c.getDescriptionProjet());
            candidaturesJson.add(cJson);
        }
        
        PaginatedResponse<Map<String, Object>> response = new PaginatedResponse<>(
            candidaturesJson, page, size, (int) pageResult.getTotalElements()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/candidatures/{id}/valider")
    @Operation(summary = "Validate or reject an application", 
               description = "Allows an STPM agent to accept or reject an application. Acceptance automatically creates a project.")
    @Transactional
    public ResponseEntity<?> validerCandidature(
            @Parameter(description = "ID de la candidature") 
            @PathVariable int id, 
            @RequestBody Map<String, Object> requestData) {
        Boolean accepter = (Boolean) requestData.get("accepter");
        
        @SuppressWarnings("null")
        CandidatureEntity candidature = dbStorage.findCandidatureById(Long.valueOf(id))
            .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'ID: " + id));
        
        if (accepter != null && accepter) {
            candidature.setStatut(StatutCandidature.APPROUVEE);
            dbStorage.updateCandidature(candidature);
            
            // Créer projet automatiquement
            List<ProblemeEntity> problemesAssocies = candidature.getProblemes();
            ProjetEntity nouveauProjet = dbStorage.createProjet(
                candidature, problemesAssocies, candidature.getPrestataire()
            );
            
            // Log structuré avec contexte MDC
            org.slf4j.MDC.put("candidatureId", String.valueOf(id));
            org.slf4j.MDC.put("projetId", String.valueOf(nouveauProjet.getId()));
            org.slf4j.MDC.put("prestataire", candidature.getPrestataire().getNomEntreprise());
            org.slf4j.MDC.put("action", "ACCEPTED");
            logger.info("Candidature acceptée et projet créé");
            org.slf4j.MDC.clear();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidature acceptée et projet créé");
            response.put("projetId", nouveauProjet.getId());
            return ResponseEntity.ok(response);
        } else {
            candidature.setStatut(StatutCandidature.REJETEE);
            dbStorage.updateCandidature(candidature);
            
            // Log structuré avec contexte MDC
            org.slf4j.MDC.put("candidatureId", String.valueOf(id));
            org.slf4j.MDC.put("prestataire", candidature.getPrestataire().getNomEntreprise());
            org.slf4j.MDC.put("action", "REJECTED");
            logger.info("Candidature refusée");
            org.slf4j.MDC.clear();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidature refusée");
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/problemes")
    @Operation(summary = "View problems", 
               description = "Returns paginated list of all reported problems")
    public ResponseEntity<?> consulterProblemes(
            @Parameter(description = "Page number (0-indexed)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "10") 
            @RequestParam(defaultValue = "10") int size) {
        
        org.springframework.data.domain.Page<ProblemeEntity> pageResult = 
            dbStorage.findNonResolusWithFilters(null, null, page, size);
        
        List<Map<String, Object>> problemesJson = new ArrayList<>();
        for (ProblemeEntity p : pageResult.getContent()) {
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
            problemesJson, page, size, (int) pageResult.getTotalElements()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/notifications")
    public ResponseEntity<?> consulterNotifications() {
        List<NotificationEntity> notificationsStmp = dbStorage.findStpmNotifications();
        
        List<Map<String, Object>> notificationsJson = new ArrayList<>();
        for (NotificationEntity n : notificationsStmp) {
            Map<String, Object> nJson = new HashMap<>();
            nJson.put("id", n.getId() != null ? n.getId().toString() : null);
            nJson.put("message", n.getMessage());
            nJson.put("type", n.getTypeChangement());
            nJson.put("date", n.getDateCreation() != null ? n.getDateCreation().toString() : null);
            nJson.put("lu", n.isLu());
            notificationsJson.add(nJson);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notificationsJson);
        response.put("total", notificationsJson.size());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/notifications/{id}/marquer-lu")
    @Transactional
    @Operation(summary = "Mark single STPM notification as read")
    public ResponseEntity<?> marquerNotificationLue(@PathVariable Long id) {
        try {
            if (id == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "ID de notification invalide");
                return ResponseEntity.status(400).body(error);
            }
            NotificationEntity notification = notificationRepository.findById(id).orElse(null);
            if (notification == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Notification non trouvée");
                return ResponseEntity.status(404).body(error);
            }
            
            if (!"STPM".equals(notification.getTypeDestinataire())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Cette notification n'appartient pas à STPM");
                return ResponseEntity.status(403).body(error);
            }
            
            boolean updated = dbStorage.markNotificationAsRead(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", updated);
            response.put("message", updated ? "Notification marquée comme lue" : "Erreur lors de la mise à jour");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors du marquage de la notification {} pour STPM", id, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la mise à jour: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/notifications/{id}")
    @Transactional
    @Operation(summary = "Delete single STPM notification")
    public ResponseEntity<?> supprimerNotification(@PathVariable Long id) {
        try {
            if (id == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "ID de notification invalide");
                return ResponseEntity.status(400).body(error);
            }
            NotificationEntity notification = notificationRepository.findById(id).orElse(null);
            if (notification == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Notification non trouvée");
                return ResponseEntity.status(404).body(error);
            }
            
            if (!"STPM".equals(notification.getTypeDestinataire())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Cette notification n'appartient pas à STPM");
                return ResponseEntity.status(403).body(error);
            }
            
            boolean deleted = dbStorage.deleteNotification(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Notification supprimée" : "Erreur lors de la suppression");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de la notification {} pour STPM", id, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/notifications")
    @Transactional
    @Operation(summary = "Clear all STPM notifications")
    public ResponseEntity<?> supprimerToutesNotifications() {
        try {
            int count = dbStorage.deleteAllStpmNotifications();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", count + " notification(s) supprimée(s)");
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de toutes les notifications STPM", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PutMapping("/problemes/{id}/priorite")
    @Operation(summary = "Change problem priority", 
               description = "Allows an STPM agent to modify a problem's priority (LOW, MEDIUM, HIGH)")
    @Transactional
    public ResponseEntity<?> modifierPrioriteProbleme(
            @Parameter(description = "Problem ID") 
            @PathVariable int id, 
            @RequestBody Map<String, Object> requestData) {
        String prioriteStr = (String) requestData.get("priorite");
        Priorite nouvellePriorite = Priorite.valueOf(prioriteStr);
        
        @SuppressWarnings("null")
        ProblemeEntity probleme = dbStorage.findProblemeById(Long.valueOf(id))
            .orElseThrow(() -> new ResourceNotFoundException("Problème non trouvé avec l'ID: " + id));
        
        probleme.setPriorite(nouvellePriorite);
        dbStorage.updateProbleme(probleme);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Priorité modifiée");
        return ResponseEntity.ok(response);
    }
}

