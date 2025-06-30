package ca.udem.maville.ui.client;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Client HTTP pour l'architecture REST MaVille
 * Version finale avec formatage amélioré
 */
public class HttpClient {
    // URL de base de l'API qui tourne sur localhost:7000
    private static final String BASE_URL = "http://localhost:7000/api";
    
    // Client OkHttp pour faire les requetes HTTP
    private final OkHttpClient client;
    
    // ObjectMapper Jackson pour convertir entre Java et JSON
    private final ObjectMapper objectMapper;
    
    // Constructeur - initialise le client HTTP
    public HttpClient() {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        // Support pour les dates LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    // ================================================================
    // MÉTHODES POUR LES MENUS RÉSIDENTS
    // ================================================================
    
    /**
     * Signaler un problème via REST
     */
    public String signalerProbleme(String lieu, String description, String residentId) {
        try {
            // Préparation des données à envoyer en JSON
            Map<String, String> data = new HashMap<>();
            data.put("lieu", lieu);
            data.put("description", description);
            data.put("residentId", residentId);
            
            // Envoi d'une requête POST à l'API
            String response = post("/residents/problemes", data);
            return "Problème signalé avec succès via REST API";
        } catch (Exception e) {
            return "Erreur lors du signalement: " + e.getMessage();
        }
    }
    
    /**
     * Consulter les travaux via REST avec filtres optionnels
     */
    public String consulterTravaux(String quartier, String typeTravaux) {
        try {
            // Construction de l'URL avec les paramètres de filtre
            String url = "/residents/travaux";
            boolean hasParam = false;
            
            // Ajout des filtres seulement s'ils sont fournis
            if (quartier != null && !quartier.trim().isEmpty()) {
                url += "?quartier=" + quartier;
                hasParam = true;
            }
            
            if (typeTravaux != null && !typeTravaux.trim().isEmpty()) {
                url += (hasParam ? "&" : "?") + "type=" + typeTravaux;
            }
            
            // Requête GET et formatage de la réponse
            String response = get(url);
            return formatJsonResponse(response);
        } catch (Exception e) {
            return "Erreur consultation travaux: " + e.getMessage();
        }
    }
    
    /**
     * Consulter les notifications du résident
     */
    public String consulterNotifications(String residentId) {
        try {
            String response = get("/residents/" + residentId + "/notifications");
            return formatJsonResponse(response);
        } catch (Exception e) {
            return "Erreur notifications: " + e.getMessage();
        }
    }
    
    // ================================================================
    // MÉTHODES POUR LES MENUS PRESTATAIRES
    // ================================================================
    
    /**
     * Consulter les problèmes disponibles pour soumissionner
     */
    public String consulterProblemes(String quartier, String typeTravaux) {
        try {
            // Même logique que consulterTravaux mais pour les problèmes
            String url = "/prestataires/problemes";
            boolean hasParam = false;
            
            if (quartier != null && !quartier.trim().isEmpty()) {
                url += "?quartier=" + quartier;
                hasParam = true;
            }
            
            if (typeTravaux != null && !typeTravaux.trim().isEmpty()) {
                url += (hasParam ? "&" : "?") + "type=" + typeTravaux;
            }
            
            String response = get(url);
            return formatJsonResponse(response);
        } catch (Exception e) {
            return "Erreur consultation problèmes: " + e.getMessage();
        }
    }
    
    /**
     * Soumettre une candidature pour un projet
     */
    public String soumettreCandiature(String prestataireId, String titre, 
                                     String description, String typeTravaux,
                                     String dateDebut, String dateFin, double cout) {
        try {
            // Regroupement de toutes les données de la candidature
            Map<String, Object> data = new HashMap<>();
            data.put("prestataireId", prestataireId);
            data.put("titre", titre);
            data.put("description", description);
            data.put("typeTravaux", typeTravaux);
            data.put("dateDebut", dateDebut);
            data.put("dateFin", dateFin);
            data.put("cout", cout);
            
            String response = post("/prestataires/candidatures", data);
            return "Candidature soumise avec succès";
        } catch (Exception e) {
            return "Erreur soumission candidature: " + e.getMessage();
        }
    }
    
    /**
     * Mettre à jour un des projets en cours
     */
    public String mettreAJourProjet(String projetId, String nouveauStatut, 
                                   String nouvelleDescription, String nouvelleDateFin) {
        try {
            // Envoi seulement des champs à modifier (les autres restent null)
            Map<String, Object> data = new HashMap<>();
            if (nouveauStatut != null) data.put("statut", nouveauStatut);
            if (nouvelleDescription != null) data.put("description", nouvelleDescription);
            if (nouvelleDateFin != null) data.put("dateFin", nouvelleDateFin);
            
            String response = put("/prestataires/projets/" + projetId, data);
            return "Projet mis à jour avec succès";
        } catch (Exception e) {
            return "Erreur mise à jour projet: " + e.getMessage();
        }
    }
    
    // ================================================================
    // MÉTHODES POUR L'INTERFACE STPM
    // ================================================================
    
    /**
     * Valider ou refuser une candidature (pour les agents STPM)
     */
    public String validerCandiature(String candidatureId, boolean accepter) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("accepter", accepter);
            
            String response = put("/stpm/candidatures/" + candidatureId + "/valider", data);
            String action = accepter ? "acceptée" : "refusée";
            return "Candidature " + action + " avec succès";
        } catch (Exception e) {
            return "Erreur validation candidature: " + e.getMessage();
        }
    }
    
    // ================================================================
    // INTÉGRATION API MONTRÉAL
    // ================================================================
    
    /**
     * Récupérer les travaux officiels de la Ville de Montréal
     */
    public String consulterTravauxMontreal() {
        try {
            String response = get("/montreal/travaux");
            return formatJsonResponse(response);
        } catch (Exception e) {
            return "Erreur API Montréal: " + e.getMessage();
        }
    }
    
    // ================================================================
    // MÉTHODES HTTP INTERNES
    // ================================================================
    
    /**
     * Méthode GET publique (pour MenuStpm)
     */
    public String get(String endpoint) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + endpoint)
            .build();
            
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur HTTP " + response.code());
            }
            return response.body().string();
        }
    }
    
    /**
     * Méthode PUT publique (pour MenuStpm)
     */
    public String put(String endpoint, Object data) throws IOException {
        String json = objectMapper.writeValueAsString(data);
        
        RequestBody body = RequestBody.create(
            json, MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
            .url(BASE_URL + endpoint)
            .put(body)
            .build();
            
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur HTTP " + response.code());
            }
            return response.body().string();
        }
    }
    
    /**
     * Faire une requête POST à l'API avec des données JSON
     */
    private String post(String endpoint, Object data) throws IOException {
        // Conversion des données Java en JSON
        String json = objectMapper.writeValueAsString(data);
        
        RequestBody body = RequestBody.create(
            json, MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
            .url(BASE_URL + endpoint)
            .post(body)
            .build();
            
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur HTTP " + response.code() + ": " + response.message());
            }
            return response.body().string();
        }
    }
    
    // ================================================================
    // MÉTHODES UTILITAIRES
    // ================================================================
    
    /**
     * Formate le JSON reçu pour l'affichage dans la console
     * Version améliorée pour mieux gérer les travaux
     */
    private String formatJsonResponse(String jsonResponse) {
    if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
        return "Aucune donnée trouvée";
    }
    
    try {
        // Parser le JSON pour un meilleur formatage
        Map<String, Object> data = objectMapper.readValue(jsonResponse, Map.class);
        
        // Cas spécial pour les travaux
        if (data.containsKey("travaux")) {
            return formatTravaux(data);
        }
        
        // Cas spécial pour les problèmes
        if (data.containsKey("problemes")) {
            return formatProblemes(data);
        }
        
        // Cas spécial pour les notifications
        if (data.containsKey("notifications")) {
            return formatNotifications(data);
        }
        
        // CAS SPÉCIAL POUR LES CANDIDATURES
        if (data.containsKey("candidatures")) {
            return formatCandidatures(data);
        }
        
        // Formatage générique pour les autres cas
        return formatGeneric(jsonResponse);
        
    } catch (Exception e) {
        // Si le parsing échoue, utiliser l'ancien formatage
        return jsonResponse.replace("{", "\n  ")
                          .replace("}", "")
                          .replace(",", "\n  ")
                          .replace("\"", "");
    }
}
    
    // Méthode spéciale pour formater les travaux
    private String formatTravaux(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        
        List<Map<String, Object>> travaux = (List<Map<String, Object>>) data.get("travaux");
        Integer total = (Integer) data.get("total");
        
        sb.append("Nombre de travaux trouvés : ").append(total).append("\n");
        
        if (data.containsKey("periode")) {
            sb.append("Période : ").append(data.get("periode")).append("\n");
        }
        
        if (travaux != null && !travaux.isEmpty()) {
            sb.append("\n=== LISTE DES TRAVAUX ===\n");
            
            for (int i = 0; i < travaux.size(); i++) {
                Map<String, Object> travail = travaux.get(i);
                sb.append("\n").append(i + 1).append(". ");
                sb.append(travail.get("titre")).append("\n");
                sb.append("   - Localisation : ").append(travail.get("localisation")).append("\n");
                sb.append("   - Quartier : ").append(travail.get("quartier")).append("\n");
                sb.append("   - Type : ").append(travail.get("type")).append("\n");
                sb.append("   - Statut : ").append(travail.get("statut")).append("\n");
                sb.append("   - Prestataire : ").append(travail.get("prestataire")).append("\n");
                sb.append("   - Dates : du ").append(travail.get("dateDebut"))
                  .append(" au ").append(travail.get("dateFin")).append("\n");
                sb.append("   - Coût : ").append(travail.get("cout")).append("$\n");
            }
        } else {
            sb.append("\nAucun travaux trouvé pour ces critères.\n");
        }
        
        return sb.toString();
    }
    


// Méthode pour formater les problèmes avec quartier visible
private String formatProblemes(Map<String, Object> data) {
    StringBuilder sb = new StringBuilder();
    
    List<Map<String, Object>> problemes = (List<Map<String, Object>>) data.get("problemes");
    Integer total = (Integer) data.get("total");
    
    sb.append("Nombre de problèmes trouvés : ").append(total).append("\n");
    
    // Afficher les filtres appliqués si présents
    if (data.containsKey("filtres_appliques")) {
        Map<String, String> filtres = (Map<String, String>) data.get("filtres_appliques");
        sb.append("Filtres appliqués : ");
        filtres.forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
        sb.append("\n");
    }
    
    if (problemes != null && !problemes.isEmpty()) {
        sb.append("\n=== LISTE DES PROBLÈMES DISPONIBLES ===\n");
        
        for (int i = 0; i < problemes.size(); i++) {
            Map<String, Object> probleme = problemes.get(i);
            String lieu = (String) probleme.get("lieu");
            
            // Extraire le quartier du lieu
            String quartier = "Non spécifié";
            if (lieu != null && lieu.contains(",")) {
                String[] parts = lieu.split(",");
                if (parts.length > 1) {
                    quartier = parts[parts.length - 1].trim();
                }
            }
            
            sb.append("\n").append(i + 1).append(". ");
            sb.append("Problème #").append(probleme.get("id"));
            sb.append(" [").append(quartier.toUpperCase()).append("]\n");
            sb.append("   - Type : ").append(probleme.get("type")).append("\n");
            sb.append("   - Lieu : ").append(lieu).append("\n");
            sb.append("   - Description : ").append(probleme.get("description")).append("\n");
            sb.append("   - Priorité : ").append(probleme.get("priorite")).append("\n");
            sb.append("   - Signalé par : ").append(probleme.get("declarant")).append("\n");
            sb.append("   - Date : ").append(probleme.get("date")).append("\n");
        }
        
        // Résumé par quartier
        sb.append("\n=== RÉSUMÉ PAR QUARTIER ===\n");
        Map<String, Integer> compteurQuartiers = new HashMap<>();
        
        for (Map<String, Object> probleme : problemes) {
            String lieu = (String) probleme.get("lieu");
            String quartier = "Non spécifié";
            if (lieu != null && lieu.contains(",")) {
                String[] parts = lieu.split(",");
                if (parts.length > 1) {
                    quartier = parts[parts.length - 1].trim();
                }
            }
            compteurQuartiers.put(quartier, compteurQuartiers.getOrDefault(quartier, 0) + 1);
        }
        
        compteurQuartiers.forEach((quartier, count) -> 
            sb.append("- ").append(quartier).append(" : ").append(count).append(" problème(s)\n")
        );
        
    } else {
        sb.append("\nAucun problème disponible pour ces critères.\n");
    }
    
    return sb.toString();
}
    
    // Méthode pour formater les notifications
    private String formatNotifications(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        
        List<Map<String, Object>> notifications = (List<Map<String, Object>>) data.get("notifications");
        Integer nonLues = (Integer) data.get("non_lues");
        
        sb.append("Notifications non lues : ").append(nonLues).append("\n");
        
        if (notifications != null && !notifications.isEmpty()) {
            sb.append("\n=== VOS NOTIFICATIONS ===\n");
            
            for (Map<String, Object> notif : notifications) {
                String status = (Boolean) notif.get("lu") ? "[LUE]" : "[NON LUE]";
                sb.append("\n").append(status).append(" ");
                sb.append(notif.get("message")).append("\n");
                sb.append("   Date : ").append(notif.get("date")).append("\n");
            }
        }
        
        return sb.toString();
    }
    
// Méthode pour formater les candidatures
private String formatCandidatures(Map<String, Object> data) {
    StringBuilder sb = new StringBuilder();
    
    List<Map<String, Object>> candidatures = (List<Map<String, Object>>) data.get("candidatures");
    Integer total = (Integer) data.get("total");
    
    sb.append("Nombre de candidatures en attente : ").append(total).append("\n");
    
    if (candidatures != null && !candidatures.isEmpty()) {
        sb.append("\n=== LISTE DES CANDIDATURES ===\n");
        
        for (int i = 0; i < candidatures.size(); i++) {
            Map<String, Object> candidature = candidatures.get(i);
            
            sb.append("\n").append(i + 1).append(". ");
            sb.append("CANDIDATURE #").append(candidature.get("id")).append("\n");
            sb.append("   - Prestataire : ").append(candidature.get("prestataire")).append("\n");
            sb.append("   - Description : ").append(candidature.get("description")).append("\n");
            sb.append("   - Coût estimé : ").append(candidature.get("cout")).append("$\n");
            sb.append("   - Date de dépôt : ").append(formatDate(candidature.get("dateDepot"))).append("\n");
            
            // Si d'autres infos sont disponibles
            if (candidature.containsKey("problemesVises")) {
                sb.append("   - Problèmes visés : ").append(candidature.get("problemesVises")).append("\n");
            }
            if (candidature.containsKey("dateDebut")) {
                sb.append("   - Début prévu : ").append(candidature.get("dateDebut")).append("\n");
            }
            if (candidature.containsKey("dateFin")) {
                sb.append("   - Fin prévue : ").append(candidature.get("dateFin")).append("\n");
            }
        }
    } else {
        sb.append("\nAucune candidature en attente de validation.\n");
    }
    
    return sb.toString();
}

// Méthode helper pour formater les dates
private String formatDate(Object dateObj) {
    if (dateObj == null) return "Non spécifiée";
    
    String dateStr = dateObj.toString();
    // Si c'est un format ISO avec T, simplifier
    if (dateStr.contains("T")) {
        String[] parts = dateStr.split("T");
        String date = parts[0];
        String time = parts[1].split("\\.")[0]; // Enlever les millisecondes
        return date + " à " + time;
    }
    return dateStr;
}
    
    // Formatage générique
    private String formatGeneric(String jsonResponse) {
        return jsonResponse.replace("{", "\n  ")
                          .replace("}", "")
                          .replace(",", "\n  ")
                          .replace("\"", "");
    }
    
    /**
     * Gérer les abonnements aux notifications d'un résident
     */
    public String gererAbonnements(String residentId, String quartier, String rue) {
        try {
            Map<String, String> data = new HashMap<>();
            if (quartier != null) data.put("quartier", quartier);
            if (rue != null) data.put("rue", rue);
            
            String response = post("/residents/" + residentId + "/notifications/abonnements", data);
            return "Abonnement créé avec succès";
        } catch (Exception e) {
            return "Erreur abonnement: " + e.getMessage();
        }
    }
    
    /**
     * Tester si l'API est accessible
     */
    public boolean testerConnexion() {
        try {
            String response = get("/health");
            return response.contains("OK");
        } catch (Exception e) {
            System.err.println("Test connexion échoué: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Fermer proprement le client HTTP
     */
    public void fermer() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
    
    /**
     * Méthode de test pour vérifier que tous les endpoints marchent
     */
    public void testerTousLesEndpoints() {
        System.out.println("=== Test de l'architecture REST ===");
        
        // Test de base: l'API répond-elle?
        System.out.println("1. Test santé API: " + (testerConnexion() ? "OK" : "ECHEC"));
        
        // Test consultation travaux
        try {
            String travaux = consulterTravaux(null, null);
            System.out.println("2. Test consultation travaux: OK");
        } catch (Exception e) {
            System.out.println("2. Test consultation travaux: ECHEC");
        }
        
        // Test consultation problèmes
        try {
            String problemes = consulterProblemes(null, null);
            System.out.println("3. Test consultation problèmes: OK");
        } catch (Exception e) {
            System.out.println("3. Test consultation problèmes: ECHEC");
        }
        
        // Test API Montréal
        try {
            String montreal = consulterTravauxMontreal();
            System.out.println("4. Test API Montréal: OK");
        } catch (Exception e) {
            System.out.println("4. Test API Montréal: ECHEC");
        }
        
        System.out.println("=== Fin des tests de l'architecture ===");
    }
}