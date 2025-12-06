package ca.udem.maville.api.controller;

import ca.udem.maville.api.dto.PaginatedResponse;
import ca.udem.maville.api.dto.ProblemeRequest;
import ca.udem.maville.api.service.ApiService;
import ca.udem.maville.api.service.NotificationWebSocketService;
import ca.udem.maville.api.util.ValidationUtil;
import ca.udem.maville.api.MontrealApiService;
import ca.udem.maville.modele.*;
import ca.udem.maville.service.DatabaseStorageService;
import ca.udem.maville.service.ModelMapperService;
import ca.udem.maville.entity.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur REST pour les résidents
 */
@RestController
@RequestMapping("/api/residents")
@Tag(name = "Résidents", description = "Endpoints pour les résidents : signaler des problèmes, consulter les travaux et notifications")
public class ResidentController {
    private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);
    
    private final DatabaseStorageService dbStorage;
    private final ModelMapperService mapper;
    private final ApiService apiService;
    private final MontrealApiService montrealApiService;
    private final NotificationWebSocketService webSocketService;
    
    public ResidentController(DatabaseStorageService dbStorage,
                             ModelMapperService mapper,
                             ApiService apiService,
                             MontrealApiService montrealApiService,
                             NotificationWebSocketService webSocketService) {
        this.dbStorage = dbStorage;
        this.mapper = mapper;
        this.apiService = apiService;
        this.montrealApiService = montrealApiService;
        this.webSocketService = webSocketService;
    }
    
    @PostMapping("/problemes")
    @Operation(summary = "Signaler un problème", 
               description = "Permet à un résident de signaler un problème routier ou urbain")
    @Transactional
    public ResponseEntity<?> signalerProbleme(@Valid @RequestBody ProblemeRequest request) {
        // Validation automatique via @Valid - gérée par GlobalExceptionHandler
        // Sanitization
        String lieu = ValidationUtil.sanitize(request.getLieu());
        String description = ValidationUtil.sanitize(request.getDescription());
        String residentEmail = request.getResidentId();
        
        // Extraire le quartier
        String quartier = apiService.extraireQuartier(lieu);
        
        // Trouver ou créer résident
        ResidentEntity declarant = dbStorage.findOrCreateResident(
            residentEmail, "Demo", "User", "514-000-0000", lieu
        );
        
        // Créer le problème
        ProblemeEntity nouveauProbleme = dbStorage.createProbleme(
            lieu, TypeTravaux.ENTRETIEN_URBAIN, description, declarant, Priorite.MOYENNE
        );
        
        // Créer abonnement automatique
        dbStorage.createAbonnement(residentEmail, "QUARTIER", quartier);
        logger.debug("Abonnement automatique créé pour le quartier : {}", quartier);
        
        // Notifier STPM
        NotificationEntity notificationStpm = dbStorage.creerNotificationStmp(
            "Nouveau problème #" + nouveauProbleme.getId() + " signalé dans " + quartier + 
            " (" + nouveauProbleme.getTypeProbleme().getDescription() + ")",
            "NOUVEAU_PROBLEME", nouveauProbleme.getId().intValue(), quartier
        );
        logger.info("Notification STPM créée pour le nouveau problème #{}", nouveauProbleme.getId());
        
        // Envoyer notification via WebSocket
        if (notificationStpm != null) {
            webSocketService.sendToStpm(notificationStpm);
        }
        
        // Log structuré avec contexte MDC
        org.slf4j.MDC.put("problemeId", String.valueOf(nouveauProbleme.getId()));
        org.slf4j.MDC.put("quartier", quartier);
        org.slf4j.MDC.put("typeProbleme", nouveauProbleme.getTypeProbleme().name());
        org.slf4j.MDC.put("residentEmail", residentEmail);
        logger.info("Problème signalé avec succès");
        org.slf4j.MDC.clear();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Problème #" + nouveauProbleme.getId() + " signalé avec succès");
        response.put("problemeId", nouveauProbleme.getId());
        response.put("quartierAbonnement", quartier);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/travaux")
    @Operation(summary = "Consulter les travaux", 
               description = "Retourne la liste paginée des travaux en cours (MaVille + API Montréal) avec filtres optionnels")
    public ResponseEntity<?> consulterTravaux(
            @Parameter(description = "Filtrer par quartier") 
            @RequestParam(required = false) String quartier,
            @Parameter(description = "Filtrer par type de travaux") 
            @RequestParam(required = false) String type,
            @Parameter(description = "Numéro de page (0-indexé)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "10") 
            @RequestParam(defaultValue = "10") int size) {
        List<Map<String, Object>> tousTravaux = new ArrayList<>();
        
        // 1. Récupérer les projets MaVille
        List<ProjetEntity> projetsMaVille = dbStorage.findAllProjets();
        for (ProjetEntity projetEntity : projetsMaVille) {
            Projet projet = mapper.toModel(projetEntity);
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
                    projet.getStatut().getDescription() : "En attente");
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
        List<NotificationEntity> mesNotifs = dbStorage.findNotificationsByResident(email);
        
        List<Map<String, Object>> notifications = new ArrayList<>();
        for (NotificationEntity n : mesNotifs) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("id", n.getId() != null ? n.getId().toString() : null);
            notif.put("message", n.getMessage() != null ? n.getMessage() : "Message manquant");
            notif.put("lu", n.isLu());
            notif.put("date", n.getDateCreation() != null ? n.getDateCreation().toString() : "Date inconnue");
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
    }
    
    @PostMapping("/{email}/abonnements")
    @Transactional
    public ResponseEntity<?> creerAbonnement(@PathVariable String email, @RequestBody Map<String, Object> requestData) {
        String type = (String) requestData.get("type");
        String valeur = (String) requestData.get("valeur");
        
        dbStorage.createAbonnement(email, type, valeur);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Abonnement créé avec succès");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{email}/abonnements")
    public ResponseEntity<?> consulterAbonnements(@PathVariable String email) {
        List<AbonnementEntity> mesAbonnements = dbStorage.findAbonnementsByResident(email);
        
        List<Map<String, Object>> abonnementsJson = new ArrayList<>();
        for (AbonnementEntity a : mesAbonnements) {
            Map<String, Object> abo = new HashMap<>();
            abo.put("type", a.getType());
            abo.put("valeur", a.getValeur());
            abonnementsJson.add(abo);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("abonnements", abonnementsJson);
        response.put("total", abonnementsJson.size());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{email}/notifications/marquer-lu")
    @Transactional
    public ResponseEntity<?> marquerNotificationsLues(@PathVariable String email) {
        List<NotificationEntity> mesNotifs = dbStorage.findNotificationsByResident(email);
        List<Long> notificationIds = mesNotifs.stream()
            .filter(n -> !n.isLu())
            .map(NotificationEntity::getId)
            .toList();
        
        dbStorage.markNotificationsAsRead(notificationIds);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notifications marquées comme lues");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{email}/preferences")
    @Operation(summary = "Récupérer les préférences d'un résident")
    public ResponseEntity<?> getPreferences(@PathVariable @org.springframework.lang.NonNull String email) {
        try {
            ca.udem.maville.entity.PreferenceEntity prefs = dbStorage.findOrCreatePreferencesByEmail(email);
            
            Map<String, Object> response = new HashMap<>();
            Boolean notificationsEmail = prefs.getNotificationsEmail();
            Boolean notificationsQuartier = prefs.getNotificationsQuartier();
            List<String> notificationsType = prefs.getNotificationsType();
            response.put("success", true);
            response.put("preferences", Map.of(
                "notificationsEmail", notificationsEmail != null ? notificationsEmail : true,
                "notificationsQuartier", notificationsQuartier != null ? notificationsQuartier : true,
                "notificationsType", notificationsType != null ? notificationsType : new ArrayList<>()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des préférences pour {}", email, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la récupération des préférences");
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PutMapping("/{email}/preferences")
    @Transactional
    @Operation(summary = "Modifier les préférences d'un résident")
    public ResponseEntity<?> modifierPreferences(@PathVariable String email, @RequestBody Map<String, Object> preferences) {
        try {
            Boolean notificationsEmail = preferences.get("notificationsEmail") != null 
                ? (Boolean) preferences.get("notificationsEmail") : null;
            Boolean notificationsQuartier = preferences.get("notificationsQuartier") != null 
                ? (Boolean) preferences.get("notificationsQuartier") : null;
            @SuppressWarnings("unchecked")
            List<String> notificationsType = preferences.get("notificationsType") != null 
                ? (List<String>) preferences.get("notificationsType") : null;
            
            ca.udem.maville.entity.PreferenceEntity savedPrefs = dbStorage.savePreferences(
                email, null, notificationsEmail, notificationsQuartier, notificationsType
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Préférences mises à jour avec succès");
            response.put("preferences", Map.of(
                "notificationsEmail", savedPrefs.getNotificationsEmail(),
                "notificationsQuartier", savedPrefs.getNotificationsQuartier(),
                "notificationsType", savedPrefs.getNotificationsType()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des préférences pour {}", email, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la mise à jour des préférences: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

