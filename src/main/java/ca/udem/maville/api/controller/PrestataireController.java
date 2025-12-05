package ca.udem.maville.api.controller;

import ca.udem.maville.api.dto.CandidatureRequest;
import ca.udem.maville.api.dto.ErrorResponse;
import ca.udem.maville.api.dto.PaginatedResponse;
import ca.udem.maville.api.exception.ResourceNotFoundException;
import ca.udem.maville.modele.*;
import ca.udem.maville.storage.JsonStorage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    
    private final JsonStorage storage;
    
    public PrestataireController(JsonStorage storage) {
        this.storage = storage;
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
        try {
            List<Probleme> problemes = storage.loadProblemes();
            
            // Filtrer par quartier
            if (quartier != null && !quartier.trim().isEmpty()) {
                String quartierRecherche = quartier.trim().toLowerCase();
                problemes = problemes.stream()
                    .filter(p -> {
                        String lieu = p.getLieu().toLowerCase();
                        return lieu.contains(quartierRecherche) || quartierRecherche.contains(lieu);
                    })
                    .toList();
            }
            
            // Filtrer par type
            if (type != null && !type.trim().isEmpty()) {
                String typeRecherche = type.trim();
                problemes = problemes.stream()
                    .filter(p -> {
                        String enumName = p.getTypeProbleme().name();
                        String enumDescription = p.getTypeProbleme().getDescription();
                        return enumName.equalsIgnoreCase(typeRecherche) || 
                               enumDescription.equalsIgnoreCase(typeRecherche);
                    })
                    .toList();
            }
            
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
        } catch (Exception e) {
            logger.error("Erreur lors de la consultation des problèmes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERROR", "Erreur: " + e.getMessage(), 500, "/api/prestataires/problemes"));
        }
    }
    
    @PostMapping("/candidatures")
    @Operation(summary = "Soumettre une candidature", 
               description = "Permet à un prestataire de soumettre une candidature pour un ou plusieurs problèmes")
    public ResponseEntity<?> soumettreCandidature(@Valid @RequestBody CandidatureRequest request) {
        // Validation automatique via @Valid - gérée par GlobalExceptionHandler
        String prestataireId = request.getPrestataireId();
        String description = request.getDescription();
        LocalDate dateDebut = request.getDateDebut();
        LocalDate dateFin = request.getDateFin();
        Double cout = request.getCout() != null ? request.getCout() : 10000.0;
        
        // Trouver ou créer prestataire
        List<Prestataire> prestataires = storage.loadPrestataires();
        Prestataire prestataire = prestataires.stream()
            .filter(p -> p.getNumeroEntreprise().equals(prestataireId))
            .findFirst()
            .orElseGet(() -> {
                Prestataire p = new Prestataire(prestataireId, "Nouvelle Entreprise " + prestataireId,
                    "Contact", "514-000-0000", prestataireId.toLowerCase() + "@entreprise.com");
                prestataires.add(p);
                storage.savePrestataires(prestataires);
                return p;
            });
        
        // Trouver problème associé
        List<Probleme> problemes = storage.loadProblemes();
        List<Integer> problemesVises = request.getProblemesVises() != null && !request.getProblemesVises().isEmpty() ?
            request.getProblemesVises() :
            problemes.stream()
                .filter(p -> !p.isResolu())
                .limit(1)
                .map(Probleme::getId)
                .toList();
        
        if (problemesVises.isEmpty()) {
            problemesVises = List.of(1);
        }
        
        // Créer candidature
        Candidature nouvelleCandidature = new Candidature(
            prestataire, problemesVises, description, cout, dateDebut, dateFin
        );
        
        List<Candidature> candidatures = storage.loadCandidatures();
        candidatures.add(nouvelleCandidature);
        storage.saveCandidatures(candidatures);
        
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
        List<Projet> tousProjets = storage.loadProjets();
        List<Projet> mesProjets = tousProjets.stream()
            .filter(p -> neq.equals(p.getPrestataire().getNumeroEntreprise()))
            .toList();
        
        List<Map<String, Object>> projetsJson = new ArrayList<>();
        for (Projet p : mesProjets) {
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
    public ResponseEntity<?> mettreAJourProjet(@PathVariable int id, @RequestBody Map<String, Object> modifications) {
        try {
            List<Projet> projets = storage.loadProjets();
            Projet projet = projets.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé avec l'ID: " + id));
            
            // Appliquer modifications
            if (modifications.containsKey("statut")) {
                projet.setStatut(StatutProjet.valueOf((String) modifications.get("statut")));
            }
            
            storage.saveProjets(projets);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Projet mis à jour");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du projet", e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("ERROR", "Erreur: " + e.getMessage(), 400, "/api/prestataires/projets/" + id));
        }
    }
    
    @GetMapping("/{neq}/notifications")
    public ResponseEntity<?> consulterNotificationsPrestataire(@PathVariable String neq) {
        List<Notification> toutesNotifications = storage.loadNotifications();
        // Filtrer les notifications pour prestataires (typeDestinataire = "PRESTATAIRE" ou destinataire = neq)
        List<Notification> mesNotifications = toutesNotifications.stream()
            .filter(n -> n.estPourPrestataire() || 
                (n.getDestinataire() != null && n.getDestinataire().equals(neq)))
            .toList();
        
        List<Map<String, Object>> notificationsJson = new ArrayList<>();
        for (Notification n : mesNotifications) {
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
    }
}

