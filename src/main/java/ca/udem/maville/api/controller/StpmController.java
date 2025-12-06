package ca.udem.maville.api.controller;

import ca.udem.maville.api.dto.PaginatedResponse;
import ca.udem.maville.api.exception.ResourceNotFoundException;
import ca.udem.maville.entity.*;
import ca.udem.maville.modele.*;
import ca.udem.maville.service.DatabaseStorageService;
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
@Tag(name = "STPM", description = "Endpoints pour les agents STPM : valider candidatures, gérer problèmes et priorités")
public class StpmController {
    private static final Logger logger = LoggerFactory.getLogger(StpmController.class);
    
    private final DatabaseStorageService dbStorage;
    
    public StpmController(DatabaseStorageService dbStorage) {
        this.dbStorage = dbStorage;
    }
    
    @GetMapping("/candidatures")
    @Operation(summary = "Consulter les candidatures", 
               description = "Retourne la liste paginée de toutes les candidatures soumises par les prestataires")
    public ResponseEntity<?> consulterCandidatures(
            @Parameter(description = "Numéro de page (0-indexé)", example = "0") 
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
    @Operation(summary = "Valider ou refuser une candidature", 
               description = "Permet à un agent STPM d'accepter ou refuser une candidature. L'acceptation crée automatiquement un projet.")
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
    @Operation(summary = "Consulter les problèmes", 
               description = "Retourne la liste paginée de tous les problèmes signalés")
    public ResponseEntity<?> consulterProblemes(
            @Parameter(description = "Numéro de page (0-indexé)", example = "0") 
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
    
    @PutMapping("/problemes/{id}/priorite")
    @Operation(summary = "Modifier la priorité d'un problème", 
               description = "Permet à un agent STPM de modifier la priorité d'un problème (FAIBLE, MOYENNE, ELEVEE)")
    @Transactional
    public ResponseEntity<?> modifierPrioriteProbleme(
            @Parameter(description = "ID du problème") 
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

