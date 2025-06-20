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

/**
 * Serveur API REST pour MaVille
 * Utilise Javalin et vos gestionnaires existants
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
     * Initialise vos services existants
     */
    private void initializeServices() {
        System.out.println("Initialisation des services...");
        
        this.storage = new JsonStorage();
        this.gestionnaireProblemes = new GestionnaireProblemes();
        this.gestionnaireProjets = new GestionnaireProjets();
        
        // Initialiser avec des données de test si nécessaire
        storage.initializeWithSampleData();
        
        System.out.println("Services initialisés");
    }
    
private void createJavalinApp() {
    this.app = Javalin.create(); // Configuration minimale
    
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
        
        // ================================================================
        // ENDPOINT DE TEST
        // ================================================================
        app.get("/api/health", ctx -> {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OK");
            response.put("message", "MaVille API is running");
            response.put("version", "1.0");
            response.put("endpoints", List.of(
                "GET /api/health",
                "GET /api/residents/travaux",
                "POST /api/residents/problemes",
                "GET /api/prestataires/problemes",
                "POST /api/prestataires/candidatures"
            ));
            ctx.json(response);
        });
        
        // ================================================================
        // ENDPOINTS RÉSIDENTS
        // ================================================================
        
        // Signaler un problème
        app.post("/api/residents/problemes", this::signalerProbleme);
        
        // Consulter travaux avec filtres
        app.get("/api/residents/travaux", this::consulterTravaux);
        
        // Consulter notifications d'un résident
        app.get("/api/residents/{id}/notifications", this::consulterNotifications);
        
        // Gérer abonnements notifications
        app.post("/api/residents/{id}/notifications/abonnements", this::ajouterAbonnement);
        app.get("/api/residents/{id}/notifications/abonnements", this::getAbonnements);
        
        // ================================================================
        // ENDPOINTS PRESTATAIRES
        // ================================================================
        
        // Consulter problèmes disponibles
        app.get("/api/prestataires/problemes", this::consulterProblemes);
        
        // Soumettre une candidature
        app.post("/api/prestataires/candidatures", this::soumettreCandiature);
        
        // Mettre à jour un projet
        app.put("/api/prestataires/projets/{id}", this::mettreAJourProjet);
        
        // Consulter projets d'un prestataire
        app.get("/api/prestataires/{id}/projets", this::consulterProjetsPrestataire);
        
        // ================================================================
        // ENDPOINTS STPM
        // ================================================================
        
        // Valider une candidature
        app.put("/api/stpm/candidatures/{id}/valider", this::validerCandiature);
        
        // Consulter toutes les candidatures en attente
        app.get("/api/stpm/candidatures", this::consulterCandidatures);
        
        // Gérer priorités des problèmes
        app.put("/api/stpm/problemes/{id}/priorite", this::definirPriorite);
        
        // ================================================================
        // ENDPOINTS API EXTERNE MONTRÉAL
        // ================================================================
        
        // Récupérer travaux officiels de Montréal
        app.get("/api/montreal/travaux", this::getTravauxMontreal);
        
        System.out.println("Routes configurées");
    }
    
    // ================================================================
    // IMPLÉMENTATION ENDPOINTS RÉSIDENTS
    // ================================================================
    
    private void signalerProbleme(Context ctx) {
        try {
            // Pour l'instant, réponse simulée
            // TODO: Intégrer avec votre GestionnaireProblemes
            
            Map<String, Object> requestData = ctx.bodyAsClass(Map.class);
            String lieu = (String) requestData.get("lieu");
            String description = (String) requestData.get("description");
            String residentId = (String) requestData.get("residentId");
            
            System.out.println("Nouveau problème signalé:");
            System.out.println("- Lieu: " + lieu);
            System.out.println("- Description: " + description);
            System.out.println("- Résident: " + residentId);
            
            // TODO: Créer un Probleme et appeler gestionnaireProblemes.ajouterProbleme()
            
            ctx.json(createSuccessResponse("Problème signalé avec succès"));
            
        } catch (Exception e) {
            ctx.status(400).json(createErrorResponse("Données invalides: " + e.getMessage()));
        }
    }
    
    private void consulterTravaux(Context ctx) {
        try {
            String quartier = ctx.queryParam("quartier");
            String type = ctx.queryParam("type");
            
            System.out.println("Consultation travaux - Quartier: " + quartier + ", Type: " + type);
            
            // TODO: Utiliser gestionnaireProjets.consulterProjets()
            
            List<Map<String, Object>> travaux = new ArrayList<>();
            travaux.add(createTravauxExample("Réfection rue Saint-Denis", "Rosemont", "ROUTIER"));
            travaux.add(createTravauxExample("Installation feux", "Centre-ville", "SIGNALISATION"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("travaux", travaux);
            response.put("total", travaux.size());
            response.put("filtres", Map.of("quartier", quartier, "type", type));
            
            ctx.json(response);
            
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Erreur consultation travaux"));
        }
    }
    
    private void consulterNotifications(Context ctx) {
        try {
            String residentId = ctx.pathParam("id");
            
            System.out.println("Consultation notifications pour résident: " + residentId);
            
            List<Map<String, Object>> notifications = new ArrayList<>();
            notifications.add(createNotificationExample("Nouveau projet dans votre quartier", false));
            notifications.add(createNotificationExample("Travaux terminés rue Sainte-Catherine", true));
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("non_lues", notifications.stream().mapToLong(n -> (Boolean)n.get("lu") ? 0 : 1).sum());
            
            ctx.json(response);
            
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Erreur consultation notifications"));
        }
    }
    
    private void ajouterAbonnement(Context ctx) {
        try {
            String residentId = ctx.pathParam("id");
            Map<String, Object> abonnement = ctx.bodyAsClass(Map.class);
            
            System.out.println("Nouvel abonnement pour résident: " + residentId);
            
            ctx.json(createSuccessResponse("Abonnement créé"));
            
        } catch (Exception e) {
            ctx.status(400).json(createErrorResponse("Données abonnement invalides"));
        }
    }
    
    private void getAbonnements(Context ctx) {
        try {
            String residentId = ctx.pathParam("id");
            
            List<Map<String, Object>> abonnements = new ArrayList<>();
            abonnements.add(Map.of("quartier", "Rosemont", "type", "ROUTIER", "actif", true));
            
            ctx.json(Map.of("abonnements", abonnements));
            
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Erreur consultation abonnements"));
        }
    }
    
    // ================================================================
    // IMPLÉMENTATION ENDPOINTS PRESTATAIRES
    // ================================================================
    
    private void consulterProblemes(Context ctx) {
        try {
            String quartier = ctx.queryParam("quartier");
            String type = ctx.queryParam("type");
            
            System.out.println("Consultation problèmes - Quartier: " + quartier + ", Type: " + type);
            
            // TODO: Utiliser gestionnaireProblemes.consulterProblemes()
            
            List<Map<String, Object>> problemes = new ArrayList<>();
            problemes.add(createProblemeExample("Nid de poule", "Rue Saint-Denis", "ROUTIER"));
            problemes.add(createProblemeExample("Feu défectueux", "Boulevard René-Lévesque", "SIGNALISATION"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("problemes", problemes);
            response.put("total", problemes.size());
            
            ctx.json(response);
            
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Erreur consultation problèmes"));
        }
    }
    
    private void soumettreCandiature(Context ctx) {
        try {
            Map<String, Object> candidature = ctx.bodyAsClass(Map.class);
            
            System.out.println("Nouvelle candidature:");
            System.out.println("- Prestataire: " + candidature.get("prestataireId"));
            System.out.println("- Titre: " + candidature.get("titre"));
            
            // TODO: Créer Candidature et appeler gestionnaireProjets.ajouterCandiature()
            
            ctx.json(createSuccessResponse("Candidature soumise avec succès"));
            
        } catch (Exception e) {
            ctx.status(400).json(createErrorResponse("Données candidature invalides"));
        }
    }
    
    private void mettreAJourProjet(Context ctx) {
        try {
            String projetId = ctx.pathParam("id");
            Map<String, Object> updates = ctx.bodyAsClass(Map.class);
            
            System.out.println("Mise à jour projet " + projetId + ": " + updates);
            
            // TODO: Utiliser gestionnaireProjets.mettreAJourProjet()
            
            ctx.json(createSuccessResponse("Projet mis à jour"));
            
        } catch (Exception e) {
            ctx.status(400).json(createErrorResponse("Erreur mise à jour projet"));
        }
    }
    
    private void consulterProjetsPrestataire(Context ctx) {
        try {
            String prestataireId = ctx.pathParam("id");
            
            List<Map<String, Object>> projets = new ArrayList<>();
            projets.add(createProjetExample("Réparation rue Saint-Denis", "EN_COURS"));
            
            ctx.json(Map.of("projets", projets));
            
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Erreur consultation projets"));
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
            
            System.out.println("Validation candidature " + candidatureId + ": " + (accepter ? "ACCEPTÉE" : "REFUSÉE"));
            
            // TODO: Utiliser gestionnaireProjets.validerCandiature()
            
            String message = accepter ? "Candidature acceptée" : "Candidature refusée";
            ctx.json(createSuccessResponse(message));
            
        } catch (Exception e) {
            ctx.status(400).json(createErrorResponse("Erreur validation candidature"));
        }
    }
    
    private void consulterCandidatures(Context ctx) {
        try {
            List<Map<String, Object>> candidatures = new ArrayList<>();
            candidatures.add(createCandidatureExample("Réparation nids de poule", "EN_ATTENTE"));
            
            ctx.json(Map.of("candidatures", candidatures));
            
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Erreur consultation candidatures"));
        }
    }
    
    private void definirPriorite(Context ctx) {
        try {
            String problemeId = ctx.pathParam("id");
            Map<String, Object> prioriteData = ctx.bodyAsClass(Map.class);
            
            System.out.println("Définition priorité problème " + problemeId);
            
            ctx.json(createSuccessResponse("Priorité définie"));
            
        } catch (Exception e) {
            ctx.status(400).json(createErrorResponse("Erreur définition priorité"));
        }
    }
    
    // ================================================================
    // IMPLÉMENTATION API MONTRÉAL
    // ================================================================
    
    private void getTravauxMontreal(Context ctx) {
        try {
            System.out.println("Récupération travaux officiels Montréal");
            
            // TODO: Intégrer avec MontrealApiService
            
            List<Map<String, Object>> travaux = new ArrayList<>();
            travaux.add(createTravauxMontrealExample("Métro ligne verte", "Transport"));
            travaux.add(createTravauxMontrealExample("Pont Jacques-Cartier", "Infrastructure"));
            
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
    
    private Map<String, Object> createTravauxExample(String titre, String quartier, String type) {
        Map<String, Object> travaux = new HashMap<>();
        travaux.put("id", "TRV-" + System.currentTimeMillis());
        travaux.put("titre", titre);
        travaux.put("quartier", quartier);
        travaux.put("type", type);
        travaux.put("statut", "EN_COURS");
        return travaux;
    }
    
    private Map<String, Object> createProblemeExample(String description, String lieu, String type) {
        Map<String, Object> probleme = new HashMap<>();
        probleme.put("id", "PB-" + System.currentTimeMillis());
        probleme.put("description", description);
        probleme.put("lieu", lieu);
        probleme.put("type", type);
        probleme.put("priorite", "MOYENNE");
        return probleme;
    }
    
    private Map<String, Object> createNotificationExample(String message, boolean lu) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", "NOT-" + System.currentTimeMillis());
        notification.put("message", message);
        notification.put("lu", lu);
        notification.put("date", "2024-06-19");
        return notification;
    }
    
    private Map<String, Object> createProjetExample(String titre, String statut) {
        Map<String, Object> projet = new HashMap<>();
        projet.put("id", "PRJ-" + System.currentTimeMillis());
        projet.put("titre", titre);
        projet.put("statut", statut);
        return projet;
    }
    
    private Map<String, Object> createCandidatureExample(String titre, String statut) {
        Map<String, Object> candidature = new HashMap<>();
        candidature.put("id", "CAN-" + System.currentTimeMillis());
        candidature.put("titre", titre);
        candidature.put("statut", statut);
        return candidature;
    }
    
    private Map<String, Object> createTravauxMontrealExample(String titre, String categorie) {
        Map<String, Object> travaux = new HashMap<>();
        travaux.put("id", "MTL-" + System.currentTimeMillis());
        travaux.put("titre", titre);
        travaux.put("categorie", categorie);
        travaux.put("source", "Ville de Montréal");
        return travaux;
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