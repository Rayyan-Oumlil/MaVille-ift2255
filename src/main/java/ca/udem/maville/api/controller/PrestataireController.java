package ca.udem.maville.api.controller;

import ca.udem.maville.api.dto.CandidatureRequest;
import ca.udem.maville.api.dto.PaginatedResponse;
import ca.udem.maville.api.exception.ResourceNotFoundException;
import ca.udem.maville.entity.*;
import ca.udem.maville.modele.*;
import ca.udem.maville.service.DatabaseStorageService;
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
 * Contrôleur REST pour les prestataires
 */
@RestController
@RequestMapping("/api/prestataires")
@Tag(name = "Prestataires", description = "Endpoints pour les prestataires : consulter problèmes, soumettre candidatures, gérer projets")
public class PrestataireController {
    private static final Logger logger = LoggerFactory.getLogger(PrestataireController.class);
    
    private final DatabaseStorageService dbStorage;
    
    public PrestataireController(DatabaseStorageService dbStorage) {
        this.dbStorage = dbStorage;
    }
    
    @GetMapping("/problemes")
    @Operation(summary = "Consulter les problèmes disponibles", 
               description = "Retourne la liste paginée des problèmes disponibles avec filtres optionnels")
    public ResponseEntity<?> consulterProblemes(
            @Parameter(description = "Filtrer par quartier") 
            @RequestParam(required = false) String quartier,
            @Parameter(description = "Filtrer par type de travaux") 
            @RequestParam(required = false) String type,
            @Parameter(description = "Numéro de page (0-indexé)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "10") 
            @RequestParam(defaultValue = "10") int size) {
        
        // Convertir type string en enum si fourni
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
    @Operation(summary = "Soumettre une candidature", 
               description = "Permet à un prestataire de soumettre une candidature pour un ou plusieurs problèmes")
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
}

