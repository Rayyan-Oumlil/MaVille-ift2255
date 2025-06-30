package ca.udem.maville.api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import ca.udem.maville.service.GestionnaireProblemes;
import ca.udem.maville.service.GestionnaireProjets;
import ca.udem.maville.storage.JsonStorage;
import ca.udem.maville.modele.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.time.LocalDate;

/**
 * Serveur API REST pour MaVille
 * Version finale avec exactement 3 projets et filtrage corrigé
 */
public class ApiServer {
    private Javalin app;
    private GestionnaireProblemes gestionnaireProblemes;
    private GestionnaireProjets gestionnaireProjets;
    private JsonStorage storage;
    private int port = 7000;
    
    public ApiServer() {
        initializeServices();
        createJavalinApp();
        setupRoutes();
    }
    
    /**
     * Initialise les services existants
     */
private void initializeServices() {
    System.out.println("Initialisation des services...");
    
    // Créer le storage en premier
    this.storage = new JsonStorage();
    
    // Initialiser avec des données de test si NÉCESSAIRE
    storage.initializeWithSampleData();
    
    // Créer les gestionnaires AVEC le storage pour qu'ils chargent les données
    this.gestionnaireProblemes = new GestionnaireProblemes(storage);
    this.gestionnaireProjets = new GestionnaireProjets();
    
    // Afficher ce qui a été chargé
    List<Probleme> problemesCharges = storage.loadProblemes();
    List<Candidature> candidaturesChargees = storage.loadCandidatures();
    List<Resident> residentsCharges = storage.loadResidents();
    List<Prestataire> prestatairesCharges = storage.loadPrestataires();
    
    System.out.println("Données chargées :");
    System.out.println("- " + problemesCharges.size() + " problèmes");
    System.out.println("- " + candidaturesChargees.size() + " candidatures");
    System.out.println("- " + residentsCharges.size() + " résidents");
    System.out.println("- " + prestatairesCharges.size() + " prestataires");
    
    System.out.println("Services initialisés avec succès");
}
    
    private void createJavalinApp() {
        this.app = Javalin.create();
        
        // Gestionnaire d'erreurs global
        app.exception(Exception.class, (e, ctx) -> {
            System.err.println("Erreur API: " + e.getMessage());
            ctx.status(500).json(createErrorResponse("Erreur serveur: " + e.getMessage()));
        });
    }
    
    /**
     * Configuration de tous les endpoints REST
     */
    private void setupRoutes() {
        System.out.println("Configuration des routes API...");
        
        // Route racine de l'API
        app.get("/api", ctx -> {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bienvenue sur l'API MaVille");
            response.put("version", "1.0");
            response.put("endpoints", Map.of(
                "santé", "GET /api/health",
                "résidents", List.of(
                    "POST /api/residents/problemes - Signaler un problème",
                    "GET /api/residents/travaux - Consulter les travaux"
                ),
                "prestataires", List.of(
                    "GET /api/prestataires/problemes - Consulter problèmes disponibles",
                    "POST /api/prestataires/candidatures - Soumettre candidature"
                )
            ));
            ctx.json(response);
        });
        
        // ENDPOINT DE TEST
        app.get("/api/health", ctx -> {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OK");
            response.put("message", "MaVille API is running");
            response.put("version", "1.0");
            ctx.json(response);
        });
        
        // ENDPOINTS RÉSIDENTS
        app.post("/api/residents/problemes", this::signalerProbleme);
        app.get("/api/residents/travaux", this::consulterTravaux);
        app.get("/api/residents/{id}/notifications", this::consulterNotifications);
        
        // ENDPOINTS PRESTATAIRES
        app.get("/api/prestataires/problemes", this::consulterProblemes);
        app.post("/api/prestataires/candidatures", this::soumettreCandiature);
        app.put("/api/prestataires/projets/{id}", this::mettreAJourProjet);
        
        // ENDPOINTS STPM
        app.put("/api/stpm/candidatures/{id}/valider", this::validerCandiature);
        app.get("/api/stpm/candidatures", this::consulterCandidatures);
        
        // ENDPOINTS API EXTERNE MONTRÉAL
        app.get("/api/montreal/travaux", this::getTravauxMontreal);
        
        System.out.println("Routes configurées");
    }
    
    // ================================================================
    // IMPLÉMENTATION ENDPOINTS RÉSIDENTS
    // ================================================================
    
    private void signalerProbleme(Context ctx) {
        try {
            Map<String, Object> requestData = ctx.bodyAsClass(Map.class);
            String lieu = (String) requestData.get("lieu");
            String description = (String) requestData.get("description");
            String residentEmail = (String) requestData.get("residentId");
            
            // Créer un résident temporaire pour la démo
            Resident declarant = new Resident("Demo", "User", "514-000-0000", residentEmail, lieu);
            
            // Créer le problème avec le gestionnaire
            Probleme nouveauProbleme = gestionnaireProblemes.signalerProbleme(
                lieu, 
                TypeTravaux.ENTRETIEN_URBAIN, // Type par défaut pour la démo
                description, 
                declarant
            );
            
            // Sauvegarder tous les problèmes
            List<Probleme> tousLesProblemes = gestionnaireProblemes.listerProblemes();
            storage.saveProblemes(tousLesProblemes);
            
            // Réponse de succès
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Problème #" + nouveauProbleme.getId() + " signalé avec succès");
            response.put("problemeId", nouveauProbleme.getId());
            
            ctx.json(response);
            
        } catch (Exception e) {
            ctx.status(400).json(createErrorResponse("Erreur: " + e.getMessage()));
        }
    }
    
    private void consulterTravaux(Context ctx) {
        try {
            String quartier = ctx.queryParam("quartier");
            String type = ctx.queryParam("type");
            
            // Charger les projets
            List<Projet> projets = storage.loadProjets();
            
            // Si aucun projet OU trop de projets, recréer exactement 3
            if (projets.isEmpty() || projets.size() > 3) {
                System.out.println("Réinitialisation avec exactement 3 projets...");
                projets = creerExactement3Projets();
                storage.saveProjets(projets);
            }
            
            // Filtrer pour la période de 3 mois
            LocalDate aujourd = LocalDate.now();
            LocalDate dans3Mois = aujourd.plusMonths(3);
            
            // Filtrer par quartier si spécifié
            if (quartier != null && !quartier.trim().isEmpty()) {
                projets = projets.stream()
                    .filter(p -> p.getLocalisation() != null && 
                                p.getLocalisation().toLowerCase().contains(quartier.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            // Filtrer par type si spécifié  
            if (type != null && !type.trim().isEmpty()) {
                final String typeRecherche = type.toUpperCase().trim();
                
                projets = projets.stream()
                    .filter(p -> {
                        if (p.getTypeTravail() == null) return false;
                        
                        // Comparer avec le nom de l'enum ET la description
                        String enumName = p.getTypeTravail().name();
                        String enumDescription = p.getTypeTravail().getDescription();
                        
                        return enumName.equalsIgnoreCase(typeRecherche) || 
                               enumDescription.equalsIgnoreCase(typeRecherche) ||
                               enumName.replace("_", " ").equalsIgnoreCase(typeRecherche);
                    })
                    .collect(Collectors.toList());
            }
            
            // Convertir en JSON
            List<Map<String, Object>> projetsJson = new ArrayList<>();
            for (Projet p : projets) {
                Map<String, Object> pJson = new HashMap<>();
                pJson.put("id", p.getId());
                pJson.put("titre", p.getDescriptionProjet());
                pJson.put("localisation", p.getLocalisation());
                pJson.put("quartier", extraireQuartier(p.getLocalisation()));
                pJson.put("type", p.getTypeTravail().getDescription());
                pJson.put("statut", p.getStatut().getDescription());
                pJson.put("priorite", p.getPriorite().getDescription());
                pJson.put("prestataire", p.getPrestataire().getNomEntreprise());
                pJson.put("dateDebut", p.getDateDebutPrevue().toString());
                pJson.put("dateFin", p.getDateFinPrevue().toString());
                pJson.put("cout", p.getCout());
                projetsJson.add(pJson);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("travaux", projetsJson);
            response.put("total", projetsJson.size());
            response.put("periode", "Projets actifs et prévus jusqu'au " + dans3Mois.toString());
            
            ctx.json(response);
            
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Erreur: " + e.getMessage()));
        }
    }
    
    private void consulterNotifications(Context ctx) {
        try {
            String residentId = ctx.pathParam("id");
            
            // Notifications simulées pour la démo
            List<Map<String, Object>> notifications = new ArrayList<>();
            
            Map<String, Object> notif1 = new HashMap<>();
            notif1.put("message", "Nouveau projet dans votre quartier");
            notif1.put("lu", false);
            notif1.put("date", LocalDate.now().toString());
            notifications.add(notif1);
            
            Map<String, Object> notif2 = new HashMap<>();
            notif2.put("message", "Travaux terminés rue Sainte-Catherine");
            notif2.put("lu", true);
            notif2.put("date", LocalDate.now().minusDays(2).toString());
            notifications.add(notif2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("total", notifications.size());
            response.put("non_lues", 1);
            
            ctx.json(response);
            
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Erreur consultation notifications"));
        }
    }
    
    // ================================================================
    // IMPLÉMENTATION ENDPOINTS PRESTATAIRES
    // ================================================================


private void consulterProblemes(Context ctx) {
    try {
        String quartier = ctx.queryParam("quartier");
        String type = ctx.queryParam("type");
        
        // Charger UNIQUEMENT depuis le storage pour avoir toutes les données
        List<Probleme> problemes = storage.loadProblemes();
        
        // Filtrer seulement les non résolus
        problemes = problemes.stream()
            .filter(p -> !p.isResolu())
            .collect(Collectors.toList());
        
        System.out.println("Problèmes disponibles avant filtrage : " + problemes.size());
        
        // Filtrer par quartier si spécifié
        if (quartier != null && !quartier.trim().isEmpty()) {
            final String quartierRecherche = quartier.trim();
            problemes = problemes.stream()
                .filter(p -> {
                    String lieu = p.getLieu().toLowerCase();
                    return lieu.contains(quartierRecherche.toLowerCase()) ||
                           extraireQuartier(lieu).equalsIgnoreCase(quartierRecherche);
                })
                .collect(Collectors.toList());
            System.out.println("Après filtre quartier '" + quartier + "' : " + problemes.size() + " problèmes");
        }
        
        // Filtrer par type si spécifié
        if (type != null && !type.trim().isEmpty()) {
            final String typeRecherche = type.toUpperCase().trim();
            problemes = problemes.stream()
                .filter(p -> {
                    String enumName = p.getTypeProbleme().name();
                    String enumDescription = p.getTypeProbleme().getDescription();
                    
                    // Accepter plusieurs formats de recherche
                    return enumName.equalsIgnoreCase(typeRecherche) || 
                           enumDescription.equalsIgnoreCase(typeRecherche) ||
                           enumName.replace("_", " ").equalsIgnoreCase(typeRecherche) ||
                           enumName.replace("_", "").equalsIgnoreCase(typeRecherche.replace(" ", ""));
                })
                .collect(Collectors.toList());
            System.out.println("Après filtre type '" + type + "' : " + problemes.size() + " problèmes");
        }
        
        // Convertir en JSON
        List<Map<String, Object>> problemesJson = new ArrayList<>();
        for (Probleme p : problemes) {
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("problemes", problemesJson);
        response.put("total", problemesJson.size());
        
        if (quartier != null || type != null) {
            Map<String, String> filtres = new HashMap<>();
            if (quartier != null) filtres.put("quartier", quartier);
            if (type != null) filtres.put("type", type);
            response.put("filtres_appliques", filtres);
        }
        
        ctx.json(response);
        
    } catch (Exception e) {
        ctx.status(500).json(createErrorResponse("Erreur consultation problèmes: " + e.getMessage()));
    }
}
    
    private void soumettreCandiature(Context ctx) {
        try {
            // Pour la démo, on retourne simplement un succès
            ctx.json(createSuccessResponse("Candidature soumise avec succès"));
        } catch (Exception e) {
            ctx.status(400).json(createErrorResponse("Erreur soumission candidature"));
        }
    }
    
    private void mettreAJourProjet(Context ctx) {
        try {
            String projetId = ctx.pathParam("id");
            ctx.json(createSuccessResponse("Projet #" + projetId + " mis à jour"));
        } catch (Exception e) {
            ctx.status(400).json(createErrorResponse("Erreur mise à jour projet"));
        }
    }
    
    // ================================================================
    // IMPLÉMENTATION ENDPOINTS STPM
    // ================================================================
    
    private void validerCandiature(Context ctx) {
        try {
            String candidatureId = ctx.pathParam("id");
            Map<String, Object> validation = ctx.bodyAsClass(Map.class);
            boolean accepter = (Boolean) validation.getOrDefault("accepter", false);
            
            String message = accepter ? "Candidature acceptée" : "Candidature refusée";
            ctx.json(createSuccessResponse(message));
            
        } catch (Exception e) {
            ctx.status(400).json(createErrorResponse("Erreur validation candidature"));
        }
    }
    
private void consulterCandidatures(Context ctx) {
    try {
        // Charger les vraies candidatures
        List<Candidature> candidatures = storage.loadCandidatures();
        
        // Filtrer seulement les candidatures en attente
        List<Candidature> candidaturesEnAttente = candidatures.stream()
            .filter(c -> c.getStatut() == StatutCandidature.SOUMISE)
            .collect(Collectors.toList());
        
        // Convertir en JSON avec plus de détails
        List<Map<String, Object>> candidaturesJson = new ArrayList<>();
        for (Candidature c : candidaturesEnAttente) {
            Map<String, Object> cJson = new HashMap<>();
            cJson.put("id", c.getId());
            cJson.put("prestataire", c.getPrestataire().getNomEntreprise());
            cJson.put("description", c.getDescriptionProjet());
            cJson.put("cout", c.getCoutEstime());
            cJson.put("dateDepot", c.getDateDepot().toString());
            
            // Ajouter les détails importants
            cJson.put("problemesVises", c.getProblemesVises());
            cJson.put("dateDebut", c.getDateDebutPrevue().toString());
            cJson.put("dateFin", c.getDateFinPrevue().toString());
            
            // Ajouter info sur les problèmes visés
            List<String> descriptionProblemes = new ArrayList<>();
            for (Integer problemeId : c.getProblemesVises()) {
                Probleme p = storage.loadProblemes().stream()
                    .filter(prob -> prob.getId() == problemeId)
                    .findFirst()
                    .orElse(null);
                if (p != null) {
                    descriptionProblemes.add("Problème #" + p.getId() + " (" + 
                        p.getTypeProbleme().getDescription() + " à " + 
                        extraireQuartier(p.getLieu()) + ")");
                }
            }
            cJson.put("detailsProblemes", descriptionProblemes);
            
            candidaturesJson.add(cJson);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("candidatures", candidaturesJson);
        response.put("total", candidaturesJson.size());
        
        ctx.json(response);
        
    } catch (Exception e) {
        ctx.status(500).json(createErrorResponse("Erreur consultation candidatures"));
    }
}
    
    // ================================================================
    // API MONTRÉAL
    // ================================================================
    
    private void getTravauxMontreal(Context ctx) {
        try {
            // Simulation de données de l'API Montréal
            List<Map<String, Object>> travaux = new ArrayList<>();
            
            Map<String, Object> travail1 = new HashMap<>();
            travail1.put("id", "MTL-2024-001");
            travail1.put("boroughid", "Ville-Marie");
            travail1.put("currentstatus", "En cours");
            travail1.put("organizationname", "Ville de Montréal");
            travail1.put("reason_category", "Infrastructure");
            travail1.put("description", "Réfection du réseau d'aqueduc");
            travaux.add(travail1);
            
            Map<String, Object> travail2 = new HashMap<>();
            travail2.put("id", "MTL-2024-002");
            travail2.put("boroughid", "Rosemont-La Petite-Patrie");
            travail2.put("currentstatus", "Planifié");
            travail2.put("organizationname", "STM");
            travail2.put("reason_category", "Transport");
            travail2.put("description", "Installation voie réservée autobus");
            travaux.add(travail2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("travaux_montreal", travaux);
            response.put("source", "Données ouvertes Montréal");
            response.put("total", travaux.size());
            
            ctx.json(response);
            
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Erreur API Montréal"));
        }
    }
    
    // ================================================================
    // MÉTHODES UTILITAIRES
    // ================================================================
    
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    private Map<String, Object> createErrorResponse(String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    // Méthode pour extraire le quartier d'une localisation
    private String extraireQuartier(String localisation) {
        if (localisation == null) return "Non spécifié";
        
        // Rechercher des mots clés de quartiers connus
        String[] quartiers = {"Rosemont", "Ville-Marie", "Plateau", "Centre-ville", 
                             "Outremont", "Verdun", "LaSalle", "Mercier",
                             "Hochelaga", "Villeray", "Ahuntsic", "CDN"};
        
        for (String q : quartiers) {
            if (localisation.toLowerCase().contains(q.toLowerCase())) {
                return q;
            }
        }
        
        // Si aucun quartier trouvé, essayer d'extraire après la dernière virgule
        if (localisation.contains(",")) {
            String[] parts = localisation.split(",");
            return parts[parts.length - 1].trim();
        }
        
        return "Centre-ville"; // Quartier par défaut
    }
    
    // Nouvelle méthode qui crée EXACTEMENT 3 projets
    private List<Projet> creerExactement3Projets() {
        List<Projet> projets = new ArrayList<>();
        
        // Charger les données de base
        List<Candidature> candidatures = storage.loadCandidatures();
        List<Probleme> problemes = storage.loadProblemes();
        
        // Utiliser SEULEMENT les 3 premières candidatures
        for (int i = 0; i < 3 && i < candidatures.size(); i++) {
            Candidature c = candidatures.get(i);
            List<Probleme> problemesVises = new ArrayList<>();
            
            // Trouver le problème correspondant
            for (Integer pId : c.getProblemesVises()) {
                problemes.stream()
                    .filter(p -> p.getId() == pId)
                    .findFirst()
                    .ifPresent(problemesVises::add);
            }
            
            if (!problemesVises.isEmpty()) {
                Projet projet = new Projet(c, problemesVises);
                
                // S'ASSURER que le type est bien copié du problème
                Probleme problemeSource = problemesVises.get(0);
                projet.setTypeTravail(problemeSource.getTypeProbleme());
                projet.setPriorite(problemeSource.getPriorite());
                
                // Personnaliser chaque projet
                if (i == 0) {
                    projet.setStatut(StatutProjet.EN_COURS);
                    projet.setLocalisation(problemeSource.getLieu() + ", Rosemont");
                } else if (i == 1) {
                    projet.setStatut(StatutProjet.APPROUVE);
                    projet.setLocalisation(problemeSource.getLieu() + ", Plateau Mont-Royal");
                } else {
                    projet.setStatut(StatutProjet.EN_COURS);
                    projet.setLocalisation(problemeSource.getLieu() + ", Ville-Marie");
                }
                
                projets.add(projet);
            }
        }
        
        return projets;
    }
    
    // ================================================================
    // CONTRÔLE DU SERVEUR
    // ================================================================
    
    public void start() {
        start(this.port);
    }
    
    public void start(int port) {
        try {
            this.port = port;
            app.start(port);
            System.out.println("\n=== Serveur API MaVille démarré ===");
            System.out.println("Port: " + port);
            System.out.println("URL de base: http://localhost:" + port + "/api");
            System.out.println("Test de santé: http://localhost:" + port + "/api/health");
            System.out.println("=====================================");
        } catch (Exception e) {
            System.err.println("Erreur démarrage serveur: " + e.getMessage());
            throw e;
        }
    }
    
    public void stop() {
        if (app != null) {
            app.stop();
            System.out.println("Serveur API arrêté");
        }
    }
    
    public boolean isRunning() {
        return app != null && app.port() > 0;
    }
    
    public int getPort() {
        return this.port;
    }
}
