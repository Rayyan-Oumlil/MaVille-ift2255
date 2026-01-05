package ca.udem.maville.api.controller;

import ca.udem.maville.api.dto.CandidatureRequest;
import ca.udem.maville.api.dto.PaginatedResponse;
import ca.udem.maville.api.exception.ResourceNotFoundException;
import ca.udem.maville.entity.*;
import ca.udem.maville.modele.*;
import ca.udem.maville.service.DatabaseStorageService;
import ca.udem.maville.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * REST Controller for service providers
 */
@RestController
@RequestMapping("/api/prestataires")
@Tag(name = "Service Providers", description = "Endpoints for service providers: view problems, submit applications, manage projects")
public class PrestataireController {
    private static final Logger logger = LoggerFactory.getLogger(PrestataireController.class);
    
    private final DatabaseStorageService dbStorage;
    private final NotificationRepository notificationRepository;
    
    public PrestataireController(DatabaseStorageService dbStorage, NotificationRepository notificationRepository) {
        this.dbStorage = dbStorage;
        this.notificationRepository = notificationRepository;
    }
    
    @GetMapping("/problemes")
    @Operation(summary = "View available problems", 
               description = "Returns paginated list of available problems with optional filters")
    public ResponseEntity<?> consulterProblemes(
            @Parameter(description = "Filter by neighborhood") 
            @RequestParam(required = false) String quartier,
            @Parameter(description = "Filter by work type") 
            @RequestParam(required = false) String type,
            @Parameter(description = "Page number (0-indexed)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") 
            @RequestParam(defaultValue = "10") int size) {
        
        // Convert string type to enum if provided
        TypeTravaux typeEnum = null;
        if (type != null && !type.trim().isEmpty()) {
            try {
                typeEnum = TypeTravaux.valueOf(type.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Type non reconnu, on ignore le filtre
            }
        }
        
        // Utiliser le repository avec pagination
        org.springframework.data.domain.Page<ProblemeEntity> pageResult = 
            dbStorage.findNonResolusWithFilters(quartier, typeEnum, page, size);
        
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
    
    @PostMapping("/candidatures")
    @Operation(summary = "Submit an application", 
               description = "Allows a service provider to submit an application for one or more problems")
    @Transactional
    public ResponseEntity<?> soumettreCandidature(@Valid @RequestBody CandidatureRequest request) {
        // Validation automatique via @Valid - gérée par GlobalExceptionHandler
        String prestataireId = request.getPrestataireId();
        String description = request.getDescription();
        LocalDate dateDebut = request.getDateDebut();
        LocalDate dateFin = request.getDateFin();
        Double cout = request.getCout() != null ? request.getCout() : 10000.0;
        
        // Trouver ou créer prestataire
        PrestataireEntity prestataire = dbStorage.findOrCreatePrestataire(
            prestataireId, "Nouvelle Entreprise " + prestataireId,
            "Contact", "514-000-0000", prestataireId.toLowerCase() + "@entreprise.com"
        );
        
        // Trouver problèmes associés
        List<ProblemeEntity> problemesDisponibles = dbStorage.findNonResolus();
        List<ProblemeEntity> problemesVises;
        
        if (request.getProblemesVises() != null && !request.getProblemesVises().isEmpty()) {
            problemesVises = request.getProblemesVises().stream()
                .map(id -> {
                    @SuppressWarnings("null")
                    Long longId = Long.valueOf(id);
                    @SuppressWarnings("null")
                    java.util.Optional<ProblemeEntity> result = dbStorage.findProblemeById(longId);
                    return result;
                })
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
        } else {
            problemesVises = problemesDisponibles.stream()
                .limit(1)
                .toList();
        }
        
        if (problemesVises.isEmpty()) {
            throw new ResourceNotFoundException("Aucun problème disponible pour cette candidature");
        }
        
        // Créer candidature
        CandidatureEntity nouvelleCandidature = dbStorage.createCandidature(
            prestataire, problemesVises, description, cout, dateDebut, dateFin
        );
        
        // Log structuré avec contexte MDC
        org.slf4j.MDC.put("candidatureId", String.valueOf(nouvelleCandidature.getId()));
        org.slf4j.MDC.put("prestataireId", prestataireId);
        org.slf4j.MDC.put("problemesVises", String.valueOf(problemesVises.size()));
        org.slf4j.MDC.put("cout", String.valueOf(cout));
        logger.info("Candidature créée avec succès");
        org.slf4j.MDC.clear();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Candidature #" + nouvelleCandidature.getId() + " créée avec succès");
        response.put("candidatureId", nouvelleCandidature.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{neq}/projets")
    public ResponseEntity<?> consulterProjetsDuPrestataire(@PathVariable String neq) {
        List<ProjetEntity> mesProjets = dbStorage.findProjetsByPrestataire(neq);
        
        List<Map<String, Object>> projetsJson = new ArrayList<>();
        for (ProjetEntity p : mesProjets) {
            Map<String, Object> pJson = new HashMap<>();
            pJson.put("id", p.getId());
            pJson.put("description", p.getDescriptionProjet());
            pJson.put("statut", p.getStatut().getDescription());
            pJson.put("localisation", p.getLocalisation());
            projetsJson.add(pJson);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("projets", projetsJson);
        response.put("total", projetsJson.size());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/projets/{id}")
    @Transactional
    @SuppressWarnings("null")
    public ResponseEntity<?> mettreAJourProjet(@PathVariable int id, @RequestBody Map<String, Object> modifications) {
        ProjetEntity projet = dbStorage.findProjetById(Long.valueOf(id))
            .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé avec l'ID: " + id));
        
        // Appliquer modifications
        if (modifications.containsKey("statut")) {
            projet.setStatut(StatutProjet.valueOf((String) modifications.get("statut")));
        }
        
        dbStorage.updateProjet(projet);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Projet mis à jour");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{neq}/notifications")
    public ResponseEntity<?> consulterNotificationsPrestataire(@PathVariable String neq) {
        List<NotificationEntity> mesNotifications = dbStorage.findPrestataireNotifications(neq);
        
        List<Map<String, Object>> notificationsJson = new ArrayList<>();
        for (NotificationEntity n : mesNotifications) {
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
    
    @PutMapping("/{neq}/notifications/{id}/marquer-lu")
    @Transactional
    @Operation(summary = "Mark single provider notification as read")
    public ResponseEntity<?> marquerNotificationLue(@PathVariable String neq, @PathVariable Long id) {
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
            
            if (!"PRESTATAIRE".equals(notification.getTypeDestinataire()) || 
                (notification.getDestinataire() != null && !neq.equals(notification.getDestinataire()))) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Cette notification ne vous appartient pas");
                return ResponseEntity.status(403).body(error);
            }
            
            boolean updated = dbStorage.markNotificationAsRead(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", updated);
            response.put("message", updated ? "Notification marquée comme lue" : "Erreur lors de la mise à jour");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors du marquage de la notification {} pour prestataire {}", id, neq, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la mise à jour: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/{neq}/notifications/{id}")
    @Transactional
    @Operation(summary = "Delete single provider notification")
    public ResponseEntity<?> supprimerNotification(@PathVariable String neq, @PathVariable Long id) {
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
            
            if (!"PRESTATAIRE".equals(notification.getTypeDestinataire()) || 
                (notification.getDestinataire() != null && !neq.equals(notification.getDestinataire()))) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Cette notification ne vous appartient pas");
                return ResponseEntity.status(403).body(error);
            }
            
            boolean deleted = dbStorage.deleteNotification(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Notification supprimée" : "Erreur lors de la suppression");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de la notification {} pour prestataire {}", id, neq, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/{neq}/notifications")
    @Transactional
    @Operation(summary = "Clear all provider notifications")
    public ResponseEntity<?> supprimerToutesNotifications(@PathVariable String neq) {
        try {
            int count = dbStorage.deleteAllPrestataireNotifications(neq);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", count + " notification(s) supprimée(s)");
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de toutes les notifications pour prestataire {}", neq, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

