package ca.udem.maville.api.controller;

import ca.udem.maville.api.dto.ErrorResponse;
import ca.udem.maville.api.dto.PaginatedResponse;
import ca.udem.maville.api.dto.ProblemeRequest;
import ca.udem.maville.api.service.ApiService;
import ca.udem.maville.api.util.ValidationUtil;
import ca.udem.maville.modele.*;
import ca.udem.maville.service.GestionnaireProblemes;
import ca.udem.maville.storage.JsonStorage;
// Swagger annotations (décommenter une fois la dépendance téléchargée)
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur REST pour les résidents
 */
@RestController
@RequestMapping("/api/residents")
// @Tag(name = "Résidents", description = "Endpoints pour les résidents : signaler des problèmes, consulter les travaux et notifications")
public class ResidentController {
    private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);
    
    private final GestionnaireProblemes gestionnaireProblemes;
    private final JsonStorage storage;
    private final ApiService apiService;
    private final ca.udem.maville.api.MontrealApiService montrealApiService;
    
    public ResidentController(GestionnaireProblemes gestionnaireProblemes, 
                             JsonStorage storage, 
                             ApiService apiService,
                             ca.udem.maville.api.MontrealApiService montrealApiService) {
        this.gestionnaireProblemes = gestionnaireProblemes;
        this.storage = storage;
        this.apiService = apiService;
        this.montrealApiService = montrealApiService;
    }
    
    @PostMapping("/problemes")
    // @Operation(summary = "Signaler un problème", 
    //            description = "Permet à un résident de signaler un problème routier ou urbain")
    // Note: @Transactional non applicable avec stockage JSON (nécessiterait migration vers base de données)
    public ResponseEntity<?> signalerProbleme(@Valid @RequestBody ProblemeRequest request) {
        // Validation automatique via @Valid - gérée par GlobalExceptionHandler
        // Sanitization
        String lieu = ValidationUtil.sanitize(request.getLieu());
        String description = ValidationUtil.sanitize(request.getDescription());
        String residentEmail = request.getResidentId();
        
        // Extraire le quartier
        String quartier = apiService.extraireQuartier(lieu);
        
        // Créer résident temporaire
        Resident declarant = new Resident("Demo", "User", "514-000-0000", residentEmail, lieu);
        
        // Créer le problème
        Probleme nouveauProbleme = gestionnaireProblemes.signalerProbleme(
            lieu, TypeTravaux.ENTRETIEN_URBAIN, description, declarant
        );
        
        // Sauvegarder
        List<Probleme> tousLesProblemes = gestionnaireProblemes.listerProblemes();
        storage.saveProblemes(tousLesProblemes);
        
        // Créer abonnement automatique
        List<Abonnement> abonnements = storage.loadAbonnements();
        Abonnement nouvelAbo = new Abonnement(residentEmail, "QUARTIER", quartier);
        
        boolean existe = abonnements.stream()
            .anyMatch(a -> a.getResidentEmail().equals(residentEmail) && 
                          a.getType().equals("QUARTIER") && 
                          a.getValeur().equals(quartier));
        
        if (!existe) {
            abonnements.add(nouvelAbo);
            storage.saveAbonnements(abonnements);
            logger.debug("Abonnement automatique créé pour le quartier : {}", quartier);
        }
        
        // Notifier STPM
        storage.creerNotificationStmp(
            "Nouveau problème #" + nouveauProbleme.getId() + " signalé dans " + quartier + 
            " (" + nouveauProbleme.getTypeProbleme().getDescription() + ")",
            "NOUVEAU_PROBLEME", nouveauProbleme.getId(), quartier
        );
        logger.info("Notification STPM créée pour le nouveau problème #{}", nouveauProbleme.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Problème #" + nouveauProbleme.getId() + " signalé avec succès");
        response.put("problemeId", nouveauProbleme.getId());
        response.put("quartierAbonnement", quartier);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/travaux")
    // @Operation(summary = "Consulter les travaux", 
    //            description = "Retourne la liste paginée des travaux en cours (MaVille + API Montréal) avec filtres optionnels")
    public ResponseEntity<?> consulterTravaux(
            // @Parameter(description = "Filtrer par quartier") 
            @RequestParam(required = false) String quartier,
            // @Parameter(description = "Filtrer par type de travaux") 
            @RequestParam(required = false) String type,
            // @Parameter(description = "Numéro de page (0-indexé)") 
            @RequestParam(defaultValue = "0") int page,
            // @Parameter(description = "Taille de la page") 
            @RequestParam(defaultValue = "10") int size) {
            List<Map<String, Object>> tousTravaux = new ArrayList<>();
            
            // 1. Récupérer les projets MaVille
            List<Projet> projetsMaVille = storage.loadProjets();
            for (Projet projet : projetsMaVille) {
                // Filtrer par quartier si spécifié
                if (quartier != null && !quartier.isEmpty()) {
                    String quartierProjet = apiService.extraireQuartier(projet.getLocalisation());
                    if (!quartier.equalsIgnoreCase(quartierProjet)) {
                        continue;
                    }
                }
                
                // Filtrer par type si spécifié
                if (type != null && !type.isEmpty()) {
                    String typeProjet = projet.getTypeTravail() != null ? 
                        projet.getTypeTravail().getDescription() : "";
                    if (!type.equalsIgnoreCase(typeProjet)) {
                        continue;
                    }
                }
                
                Map<String, Object> travail = new HashMap<>();
                travail.put("id", "MAVILLE-" + projet.getId());
                travail.put("source", "MaVille");
                travail.put("titre", projet.getDescriptionProjet() != null ? 
                    projet.getDescriptionProjet() : "Projet #" + projet.getId());
                travail.put("description", projet.getDescriptionProjet());
                travail.put("lieu", projet.getLocalisation());
                travail.put("quartier", apiService.extraireQuartier(projet.getLocalisation()));
                travail.put("type", projet.getTypeTravail() != null ? 
                    projet.getTypeTravail().getDescription() : "Non spécifié");
                travail.put("date_debut", projet.getDateDebutPrevue() != null ? 
                    projet.getDateDebutPrevue().toString() : null);
                travail.put("date_fin", projet.getDateFinPrevue() != null ? 
                    projet.getDateFinPrevue().toString() : null);
                travail.put("cout", projet.getCout());
                travail.put("statut", projet.getStatut() != null ? 
                    projet.getStatut().toString() : "EN_COURS");
                tousTravaux.add(travail);
            }
            
            // 2. Récupérer les travaux de l'API Montréal
            try {
                List<Map<String, Object>> travauxMontreal = montrealApiService.getTravauxEnCours(50);
                
                for (Map<String, Object> travailMontreal : travauxMontreal) {
                    // Filtrer par quartier si spécifié
                    if (quartier != null && !quartier.isEmpty()) {
                        Object arrondissement = travailMontreal.get("arrondissement");
                        if (arrondissement == null || 
                            !quartier.equalsIgnoreCase(arrondissement.toString())) {
                            continue;
                        }
                    }
                    
                    // Filtrer par type si spécifié
                    if (type != null && !type.isEmpty()) {
                        Object motif = travailMontreal.get("motif");
                        if (motif == null || 
                            !type.equalsIgnoreCase(motif.toString())) {
                            continue;
                        }
                    }
                    
                    travailMontreal.put("source", "Montreal");
                    tousTravaux.add(travailMontreal);
                }
            } catch (Exception e) {
                logger.warn("Erreur lors de la récupération des travaux de Montréal: {}", e.getMessage());
            }
            
            // Pagination
            int total = tousTravaux.size();
            int start = page * size;
            int end = Math.min(start + size, total);
            
            List<Map<String, Object>> pageTravaux = (start < total) ? 
                tousTravaux.subList(start, end) : new ArrayList<>();
            
            PaginatedResponse<Map<String, Object>> response = new PaginatedResponse<>(
                pageTravaux, page, size, total
            );
            
            return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{email}/notifications")
    public ResponseEntity<?> consulterNotifications(@PathVariable String email) {
        try {
            List<Notification> toutesNotifs = storage.loadNotifications();
            List<Notification> mesNotifs = toutesNotifs.stream()
                .filter(n -> email.equals(n.getResidentEmail()))
                .sorted((n1, n2) -> {
                    if (n1.getDateCreation() == null || n2.getDateCreation() == null) return 0;
                    return n2.getDateCreation().compareTo(n1.getDateCreation());
                })
                .toList();
            
            List<Map<String, Object>> notifications = new ArrayList<>();
            for (Notification n : mesNotifs) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("message", n.getMessage() != null ? n.getMessage() : "Message manquant");
                notif.put("lu", n.isLu());
                notif.put("date", n.getDateCreation() != null ? n.getDateCreation().toLocalDate().toString() : "Date inconnue");
                notif.put("type", n.getTypeChangement() != null ? n.getTypeChangement() : "Type inconnu");
                notif.put("projetId", n.getProjetId());
                notifications.add(notif);
            }
            
            long nonLues = mesNotifs.stream().filter(n -> !n.isLu()).count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("total", notifications.size());
            response.put("non_lues", nonLues);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la consultation des notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERROR", "Erreur: " + e.getMessage(), 500, "/api/residents/" + email + "/notifications"));
        }
    }
    
    @PostMapping("/{email}/abonnements")
    public ResponseEntity<?> creerAbonnement(@PathVariable String email, @RequestBody Map<String, Object> requestData) {
        try {
            String type = (String) requestData.get("type");
            String valeur = (String) requestData.get("valeur");
            
            List<Abonnement> abonnements = storage.loadAbonnements();
            Abonnement nouvelAbo = new Abonnement(email, type, valeur);
            abonnements.add(nouvelAbo);
            storage.saveAbonnements(abonnements);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Abonnement créé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'abonnement", e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("ERROR", "Erreur: " + e.getMessage(), 400, "/api/residents/" + email + "/abonnements"));
        }
    }
    
    @GetMapping("/{email}/abonnements")
    public ResponseEntity<?> consulterAbonnements(@PathVariable String email) {
        try {
            List<Abonnement> tousAbonnements = storage.loadAbonnements();
            List<Abonnement> mesAbonnements = tousAbonnements.stream()
                .filter(a -> email.equals(a.getResidentEmail()))
                .toList();
            
            List<Map<String, Object>> abonnementsJson = new ArrayList<>();
            for (Abonnement a : mesAbonnements) {
                Map<String, Object> abo = new HashMap<>();
                abo.put("type", a.getType());
                abo.put("valeur", a.getValeur());
                abonnementsJson.add(abo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("abonnements", abonnementsJson);
            response.put("total", abonnementsJson.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la consultation des abonnements", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERROR", "Erreur: " + e.getMessage(), 500, "/api/residents/" + email + "/abonnements"));
        }
    }
    
    @PutMapping("/{email}/notifications/marquer-lu")
    public ResponseEntity<?> marquerNotificationsLues(@PathVariable String email) {
        try {
            List<Notification> toutesNotifs = storage.loadNotifications();
            toutesNotifs.stream()
                .filter(n -> email.equals(n.getResidentEmail()) && !n.isLu())
                .forEach(n -> n.setLu(true));
            storage.saveNotifications(toutesNotifs);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notifications marquées comme lues");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors du marquage des notifications", e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("ERROR", "Erreur: " + e.getMessage(), 400, "/api/residents/" + email + "/notifications/marquer-lu"));
        }
    }
    
    @PutMapping("/{email}/preferences")
    public ResponseEntity<?> modifierPreferences(@PathVariable String email, @RequestBody Map<String, Object> preferences) {
        try {
            storage.savePreferencesNotification(email, preferences);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Préférences mises à jour avec succès");
            response.put("preferences", preferences);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la modification des préférences", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERROR", "Erreur: " + e.getMessage(), 500, "/api/residents/" + email + "/preferences"));
        }
    }
}

