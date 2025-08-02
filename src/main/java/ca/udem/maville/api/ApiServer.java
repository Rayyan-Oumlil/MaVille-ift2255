package ca.udem.maville.api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import ca.udem.maville.service.GestionnaireProblemes;
import ca.udem.maville.service.GestionnaireProjets;
import ca.udem.maville.storage.JsonStorage;
import ca.udem.maville.modele.*;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    
    // Initialiser avec des données de test SI NÉCESSAIRE
    storage.initializeWithSampleData();
    
    // IMPORTANT : Synchroniser les compteurs d'ID
    List<Probleme> problemesExistants = storage.loadProblemes();
    if (!problemesExistants.isEmpty()) {
        Probleme.synchroniserCompteurId(problemesExistants);
        System.out.println("Compteur ID problèmes synchronisé");
    }
    
    List<Candidature> candidaturesExistantes = storage.loadCandidatures();
    if (!candidaturesExistantes.isEmpty()) {
        Candidature.synchroniserCompteurId(candidaturesExistantes);
        System.out.println("Compteur ID candidatures synchronisé");
    }
    
    // Créer les gestionnaires AVEC le storage pour qu'ils chargent les données
    this.gestionnaireProblemes = new GestionnaireProblemes(storage);
    this.gestionnaireProjets = new GestionnaireProjets();
    
    // Afficher ce qui a été chargé
    List<Candidature> candidaturesChargees = storage.loadCandidatures();
    List<Resident> residentsCharges = storage.loadResidents();
    List<Prestataire> prestatairesCharges = storage.loadPrestataires();
    
    System.out.println("Données chargées :");
    System.out.println("- " + problemesExistants.size() + " problèmes");
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
        app.post("/api/residents/{email}/abonnements", this::creerAbonnement);
        app.get("/api/residents/{email}/abonnements", this::consulterAbonnements);
        app.put("/api/residents/{email}/preferences", this::modifierPreferencesResident);
        
        // ENDPOINTS PRESTATAIRES
        app.get("/api/prestataires/problemes", this::consulterProblemes);
        app.post("/api/prestataires/candidatures", this::soumettreCandiature);
        app.put("/api/prestataires/projets/{id}", this::mettreAJourProjet);
        app.get("/api/prestataires/{neq}/projets", this::consulterProjetsDuPrestataire);  
        
        // ENDPOINTS STPM
        app.put("/api/stpm/candidatures/{id}/valider", this::validerCandiature);
        app.get("/api/stpm/candidatures", this::consulterCandidatures);
        app.put("/api/stpm/problemes/{id}/priorite", this::modifierPrioriteProbleme);
        
        // ENDPOINTS API EXTERNE MONTRÉAL
        app.get("/api/montreal/travaux", this::getTravauxMontreal);
        
        app.get("/api/prestataires/{neq}/preferences", this::consulterPreferencesPrestataire);
        app.post("/api/prestataires/{neq}/abonnements", this::creerAbonnementPrestataire);
        app.get("/api/prestataires/{neq}/notifications", this::consulterNotificationsPrestataire);
        app.put("/api/residents/{email}/notifications/marquer-lu", this::marquerNotificationsLues);
app.put("/api/prestataires/{neq}/notifications/marquer-lu", this::marquerNotificationsLuesPrestataire);

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
        
        // Extraire le quartier du lieu
        String quartier = extraireQuartier(lieu);
        
        // Créer un résident temporaire pour la démo
        Resident declarant = new Resident("Demo", "User", "514-000-0000", residentEmail, lieu);
        
        // Créer le problème avec le gestionnaire
        Probleme nouveauProbleme = gestionnaireProblemes.signalerProbleme(
            lieu, 
            TypeTravaux.ENTRETIEN_URBAIN,
            description, 
            declarant
        );
        
        // Sauvegarder tous les problèmes
        List<Probleme> tousLesProblemes = gestionnaireProblemes.listerProblemes();
        storage.saveProblemes(tousLesProblemes);
        
        // NOUVEAU : Créer automatiquement un abonnement au quartier
        List<Abonnement> abonnements = storage.loadAbonnements();
        Abonnement nouvelAbo = new Abonnement(residentEmail, "QUARTIER", quartier);
        
        // Vérifier qu'il n'existe pas déjà
        boolean existe = abonnements.stream()
            .anyMatch(a -> a.getResidentEmail().equals(residentEmail) && 
                          a.getType().equals("QUARTIER") && 
                          a.getValeur().equals(quartier));
        
        if (!existe) {
            abonnements.add(nouvelAbo);
            storage.saveAbonnements(abonnements);
            System.out.println("Abonnement automatique créé pour le quartier : " + quartier);
        }
        
        // Réponse de succès
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Problème #" + nouveauProbleme.getId() + " signalé avec succès");
        response.put("problemeId", nouveauProbleme.getId());
        response.put("quartierAbonnement", quartier);
        
        // NOUVEAU : Notifier le STPM du nouveau problème
storage.creerNotificationStmp(
    "Nouveau problème #" + nouveauProbleme.getId() + " signalé dans " + quartier + 
    " (" + nouveauProbleme.getTypeProbleme().getDescription() + ")",
    "NOUVEAU_PROBLEME", 
    nouveauProbleme.getId(), 
    quartier
);
// Notifier le STPM du nouveau problème
storage.creerNotificationStmp(
    "Nouveau problème #" + nouveauProbleme.getId() + " signalé dans " + quartier + 
    " (" + nouveauProbleme.getTypeProbleme().getDescription() + ")",
    "NOUVEAU_PROBLEME", 
    nouveauProbleme.getId(), 
    quartier
);
System.out.println(" Notification STPM créée pour le nouveau problème #" + nouveauProbleme.getId());
        ctx.json(response);
        
    } catch (Exception e) {
        ctx.status(400).json(createErrorResponse("Erreur: " + e.getMessage()));
    }
}

    


private void consulterTravaux(Context ctx) {
    try {
        String quartier = ctx.queryParam("quartier");
        String type = ctx.queryParam("type");
        
        // 1. CHARGER LES PROJETS MAVILLE
        List<Projet> projetsMaVille = storage.loadProjets();
        
        // 2. CHARGER LES TRAVAUX OFFICIELS MONTRÉAL
        MontrealApiService montrealApi = new MontrealApiService();
        List<Map<String, Object>> travauxMontreal = montrealApi.getTravauxEnCours(50);
        
        // 3. COMBINER LES DEUX SOURCES
        List<Map<String, Object>> travaux = new ArrayList<>();
        
        // Ajouter les projets MaVille (modifiables) avec filtrage
        for (Projet p : projetsMaVille) {
            boolean inclure = true;
            
            // Filtrer par quartier si spécifié
            if (quartier != null && !quartier.trim().isEmpty()) {
                String quartierRecherche = quartier.trim().toLowerCase();
                String localisation = p.getLocalisation().toLowerCase();
                String quartierProjet = extraireQuartier(p.getLocalisation()).toLowerCase();
                
                if (!localisation.contains(quartierRecherche) && 
                    !quartierProjet.contains(quartierRecherche) &&
                    !quartierRecherche.contains(quartierProjet)) {
                    inclure = false;
                }
            }
            
            // Filtrer par type si spécifié
            if (inclure && type != null && !type.trim().isEmpty()) {
                String typeRecherche = type.trim().toUpperCase();
                String typeProjet = p.getTypeTravail().name();
                String descriptionProjet = p.getTypeTravail().getDescription();
                
                if (!typeProjet.equals(typeRecherche) && 
                    !typeProjet.contains(typeRecherche) &&
                    !descriptionProjet.toLowerCase().contains(typeRecherche.toLowerCase()) &&
                    !typeRecherche.contains(typeProjet)) {
                    inclure = false;
                }
            }
            
            if (inclure) {
                Map<String, Object> travail = new HashMap<>();
                travail.put("id", "MAVILLE-" + p.getId());
                travail.put("titre", p.getDescriptionProjet());
                travail.put("localisation", p.getLocalisation());
                travail.put("quartier", extraireQuartier(p.getLocalisation()));
                travail.put("type", p.getTypeTravail().getDescription());
                travail.put("statut", p.getStatut().getDescription());
                travail.put("priorite", p.getPriorite().getDescription());
                travail.put("prestataire", p.getPrestataire().getNomEntreprise());
                travail.put("dateDebut", p.getDateDebutPrevue().toString());
                travail.put("dateFin", p.getDateFinPrevue().toString());
                travail.put("cout", p.getCout());
                travail.put("source", "MaVille");
                travail.put("modifiable", true);
                travaux.add(travail);
            }
        }
        
        // Ajouter les travaux officiels Montréal (lecture seule) avec filtrage
        for (Map<String, Object> travailMtl : travauxMontreal) {
            boolean inclure = true;
            
            // Filtrer par quartier si spécifié
            if (quartier != null && !quartier.trim().isEmpty()) {
                String quartierRecherche = quartier.trim().toLowerCase();
                String arrondissement = (String) travailMtl.get("arrondissement");
                
                if (arrondissement == null || 
                    (!arrondissement.toLowerCase().contains(quartierRecherche) &&
                     !quartierRecherche.contains(arrondissement.toLowerCase()))) {
                    inclure = false;
                }
            }
            
            // Filtrer par type si spécifié
            if (inclure && type != null && !type.trim().isEmpty()) {
                String typeRecherche = type.trim().toLowerCase();
                String motif = (String) travailMtl.get("motif");
                
                if (motif == null || 
                    (!motif.toLowerCase().contains(typeRecherche) &&
                     !typeRecherche.contains(motif.toLowerCase()))) {
                    inclure = false;
                }
            }
            
            if (inclure) {
                Map<String, Object> travail = new HashMap<>();
                travail.put("id", "MTL-" + travailMtl.get("id"));
                travail.put("titre", "Travaux officiels - " + travailMtl.get("motif"));
                travail.put("localisation", travailMtl.get("arrondissement"));
                travail.put("quartier", travailMtl.get("arrondissement"));
                travail.put("type", travailMtl.get("motif"));
                travail.put("statut", travailMtl.get("statut"));
                travail.put("priorite", "N/A");
                travail.put("prestataire", travailMtl.get("organisation"));
                travail.put("dateDebut", "N/A");
                travail.put("dateFin", "N/A");
                travail.put("cout", "N/A");
                travail.put("source", "Ville de Montréal");
                travail.put("modifiable", false);
                travaux.add(travail);
            }
        }
        
        // 4. RÉPONSE COMBINÉE
        
        Map<String, Object> response = new HashMap<>();
        response.put("travaux", travaux);
        response.put("total", travaux.size());
        response.put("sources", Map.of(
            "maville", (int) travaux.stream().filter(t -> "MaVille".equals(t.get("source"))).count(),
            "montreal_officiel", (int) travaux.stream().filter(t -> "Ville de Montréal".equals(t.get("source"))).count()
        ));
        response.put("periode", "Projets MaVille + Travaux officiels Montréal");
        
        // Ajouter info sur les filtres appliqués
        if (quartier != null || type != null) {
            Map<String, String> filtres = new HashMap<>();
            if (quartier != null) filtres.put("quartier", quartier);
            if (type != null) filtres.put("type", type);
            response.put("filtres_appliques", filtres);
        }
        
        ctx.json(response);
        
    } catch (Exception e) {
        System.err.println("ERREUR dans consulterTravaux : " + e.getMessage());
        e.printStackTrace();
        ctx.status(500).json(createErrorResponse("Erreur: " + e.getMessage()));
    }
}
    
    private void consulterNotifications(Context ctx) {
    try {
        String residentEmail = ctx.pathParam("id");
        System.out.println("DEBUG - Recherche notifications pour : " + residentEmail);
        
        // Charger les vraies notifications
        List<Notification> toutesNotifs = storage.loadNotifications();
        System.out.println("DEBUG - Total notifications chargées : " + toutesNotifs.size());
        
        List<Notification> mesNotifs = toutesNotifs.stream()
            .filter(n -> {
                String email = n.getResidentEmail(); // Peut retourner null !
                return email != null && email.equals(residentEmail);
            })
            .sorted((n1, n2) -> {
                // Vérifier que les dates ne sont pas null
                if (n1.getDateCreation() == null || n2.getDateCreation() == null) {
                    return 0;
                }
                return n2.getDateCreation().compareTo(n1.getDateCreation());
            })
            .collect(Collectors.toList());
        
        System.out.println("DEBUG - Notifications trouvées : " + mesNotifs.size());
        
        // Convertir en format JSON avec vérifications
        List<Map<String, Object>> notifications = new ArrayList<>();
        for (Notification n : mesNotifs) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("message", n.getMessage() != null ? n.getMessage() : "Message manquant");
            notif.put("lu", n.isLu());
            
            // Vérification sécurisée pour la date
            if (n.getDateCreation() != null) {
                notif.put("date", n.getDateCreation().toLocalDate().toString());
            } else {
                notif.put("date", "Date inconnue");
            }
            
            notif.put("type", n.getTypeChangement() != null ? n.getTypeChangement() : "Type inconnu");
            notif.put("projetId", n.getProjetId());
            notifications.add(notif);
        }
        
        long nonLues = mesNotifs.stream()
            .filter(n -> !n.isLu())
            .count();
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("total", notifications.size());
        response.put("non_lues", nonLues);
        
        ctx.json(response);
        
    } catch (Exception e) {
        System.err.println("ERREUR dans consulterNotifications : " + e.getMessage());
        e.printStackTrace(); // Afficher la stack trace complète
        ctx.status(500).json(createErrorResponse("Erreur consultation notifications : " + e.getMessage()));
    }
}

    private void creerAbonnement(Context ctx) {
    try {
        String email = ctx.pathParam("email");
        Map<String, Object> requestData = ctx.bodyAsClass(Map.class);
        String type = (String) requestData.get("type"); // QUARTIER ou RUE
        String valeur = (String) requestData.get("valeur");
        
        List<Abonnement> abonnements = storage.loadAbonnements();
        Abonnement nouvelAbo = new Abonnement(email, type, valeur);
        
        // Vérifier qu'il n'existe pas déjà
        if (!abonnements.contains(nouvelAbo)) {
            abonnements.add(nouvelAbo);
            storage.saveAbonnements(abonnements);
            
            ctx.json(createSuccessResponse("Abonnement créé pour " + type + " : " + valeur));
        } else {
            ctx.json(createSuccessResponse("Vous êtes déjà abonné à " + type + " : " + valeur));
        }
        
    } catch (Exception e) {
        ctx.status(400).json(createErrorResponse("Erreur création abonnement"));
    }
}

private void consulterAbonnements(Context ctx) {
    try {
        String email = ctx.pathParam("email");
        
        List<Abonnement> tousAbonnements = storage.loadAbonnements();
        List<Abonnement> mesAbonnements = tousAbonnements.stream()
            .filter(a -> a.getResidentEmail().equals(email))
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("abonnements", mesAbonnements);
        response.put("total", mesAbonnements.size());
        
        ctx.json(response);
        
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
        
        gestionnaireProblemes.synchroniserAvecStorage(storage);

        // TOUJOURS charger depuis le storage pour avoir les données à jour
        List<Probleme> problemes = storage.loadProblemes();
        
        System.out.println("DEBUG - Chargement des problèmes depuis le storage : " + problemes.size() + " problèmes");
        
        // Afficher les priorités actuelles pour debug
        for (Probleme p : problemes) {
            System.out.println("DEBUG - Problème #" + p.getId() + " : Priorité = " + p.getPriorite());
        }
        
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
        
        // Convertir en JSON - TOUJOURS utiliser les données actuelles
        List<Map<String, Object>> problemesJson = new ArrayList<>();
        for (Probleme p : problemes) {
            Map<String, Object> pJson = new HashMap<>();
            pJson.put("id", p.getId());
            pJson.put("lieu", p.getLieu());
            pJson.put("description", p.getDescription());
            pJson.put("type", p.getTypeProbleme().getDescription());
            pJson.put("priorite", p.getPriorite().getDescription()); // Priorité actuelle
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
            System.out.println("\n=== NOUVELLE CANDIDATURE REÇUE ===");
            
            // 1. Récupérer les données JSON
            String bodyJson = ctx.body();
            System.out.println("Body brut reçu : " + bodyJson);
            
            Map<String, Object> requestData = ctx.bodyAsClass(Map.class);
            
            // 2. Extraire toutes les données
            String prestataireId = (String) requestData.get("prestataireId");
            String titre = (String) requestData.get("titre");
            String description = (String) requestData.get("description");
            String typeTravaux = (String) requestData.get("typeTravaux");
            String dateDebut = (String) requestData.get("dateDebut");
            String dateFin = (String) requestData.get("dateFin");
            
            // Gérer le coût (peut être Double, Integer ou String)
            Double cout = 0.0;
            Object coutObj = requestData.get("cout");
            if (coutObj != null) {
                if (coutObj instanceof Number) {
                    cout = ((Number) coutObj).doubleValue();
                } else {
                    try {
                        cout = Double.parseDouble(coutObj.toString());
                    } catch (Exception e) {
                        cout = 10000.0;
                    }
                }
            }
            
            System.out.println("Données extraites :");
            System.out.println("- Prestataire ID : " + prestataireId);
            System.out.println("- Titre : " + titre);
            System.out.println("- Type : " + typeTravaux);
            System.out.println("- Dates : " + dateDebut + " à " + dateFin);
            System.out.println("- Coût : " + cout);
            
            // 3. Trouver ou créer le prestataire
            List<Prestataire> prestataires = storage.loadPrestataires();
            Prestataire prestataire = null;
            
            for (Prestataire p : prestataires) {
                if (p.getNumeroEntreprise().equals(prestataireId)) {
                    prestataire = p;
                    break;
                }
            }
            
            if (prestataire == null) {
                // Créer un nouveau prestataire
                prestataire = new Prestataire(
                    prestataireId,
                    "Nouvelle Entreprise " + prestataireId,
                    "Contact Principal",
                    "514-000-0000",
                    prestataireId.toLowerCase() + "@entreprise.com"
                );
                prestataires.add(prestataire);
                storage.savePrestataires(prestataires);
                System.out.println("Nouveau prestataire créé : " + prestataire.getNomEntreprise());
            }
            
            // 4. Trouver des problèmes à associer
            List<Probleme> problemes = storage.loadProblemes();
            List<Integer> problemesVises = new ArrayList<>();
            
            // Chercher un problème non résolu du bon type
            for (Probleme p : problemes) {
                if (!p.isResolu()) {
                    if (typeTravaux != null && p.getTypeProbleme().name().equals(typeTravaux)) {
                        problemesVises.add(p.getId());
                        System.out.println("Problème associé : #" + p.getId());
                        break;
                    }
                }
            }
            
            // Si aucun problème du bon type, prendre n'importe quel problème non résolu
            if (problemesVises.isEmpty()) {
                for (Probleme p : problemes) {
                    if (!p.isResolu()) {
                        problemesVises.add(p.getId());
                        System.out.println("Problème par défaut : #" + p.getId());
                        break;
                    }
                }
            }
            
            // Si toujours rien, utiliser l'ID 1
            if (problemesVises.isEmpty()) {
                problemesVises.add(1);
                System.out.println("Aucun problème disponible, utilisation de l'ID 1");
            }
            
            // 5. Créer la candidature
            LocalDate dateDebutParsed = LocalDate.parse(dateDebut);
            LocalDate dateFinParsed = LocalDate.parse(dateFin);
            
            Candidature nouvelleCandidature = new Candidature(
                prestataire,
                problemesVises,
                description != null && !description.trim().isEmpty() ? description : titre,
                cout,
                dateDebutParsed,
                dateFinParsed
            );
            
            System.out.println("Nouvelle candidature créée avec ID : " + nouvelleCandidature.getId());
            
            // 6. IMPORTANT : Charger, ajouter et sauvegarder
            List<Candidature> candidatures = storage.loadCandidatures();
            int ancienNombre = candidatures.size();
            
            candidatures.add(nouvelleCandidature);
            
            storage.saveCandidatures(candidatures);
            
            // 7. Vérifier la sauvegarde
            List<Candidature> verification = storage.loadCandidatures();
            System.out.println("Vérification : " + ancienNombre + " -> " + verification.size() + " candidatures");
            
            // 8. Envoyer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidature #" + nouvelleCandidature.getId() + " créée avec succès");
            response.put("candidatureId", nouvelleCandidature.getId());
            response.put("total", verification.size());
            
            ctx.json(response);
            System.out.println("=== FIN TRAITEMENT CANDIDATURE ===\n");
            
        } catch (Exception e) {
            System.err.println("ERREUR CRITIQUE dans soumettreCandiature :");
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            
            ctx.status(400).json(errorResponse);
        }
    }
    


private void mettreAJourProjet(Context ctx) {
    try {
        String projetId = ctx.pathParam("id");
        Map<String, Object> modifications = ctx.bodyAsClass(Map.class);
        
        System.out.println("\n=== MISE À JOUR PROJET #" + projetId + " ===");
        System.out.println("Modifications reçues : " + modifications);
        
        // IMPORTANT : Extraire le NEQ du prestataire depuis les modifications
        String neq = (String) modifications.get("neq");
        
        // 1. Charger tous les projets
        List<Projet> projets = storage.loadProjets();
        System.out.println("Nombre total de projets : " + projets.size());
        
        // 2. Trouver le projet à modifier EN VÉRIFIANT LE PRESTATAIRE
        Projet projetAModifier = null;
        for (Projet p : projets) {
            if (String.valueOf(p.getId()).equals(projetId)) {
                // Si NEQ fourni, vérifier qu'il correspond
                if (neq != null && p.getPrestataire() != null) {
                    if (!p.getPrestataire().getNumeroEntreprise().equals(neq)) {
                        System.out.println("Projet #" + p.getId() + " trouvé mais appartient à " + 
                                         p.getPrestataire().getNumeroEntreprise() + ", pas à " + neq);
                        continue;
                    }
                }
                projetAModifier = p;
                break;
            }
        }
        
        if (projetAModifier == null) {
            ctx.status(404).json(createErrorResponse("Projet #" + projetId + " introuvable ou n'appartient pas au prestataire"));
            return;
        }
        
        System.out.println("Projet trouvé : " + projetAModifier.getDescriptionProjet());
        System.out.println("Prestataire : " + projetAModifier.getPrestataire().getNomEntreprise() + 
                         " (NEQ: " + projetAModifier.getPrestataire().getNumeroEntreprise() + ")");
        
        // 3. Afficher l'état actuel RÉEL
        System.out.println("\nÉtat actuel du projet :");
        System.out.println("- ID : " + projetAModifier.getId());
        System.out.println("- Statut : " + projetAModifier.getStatut());
        System.out.println("- Description : " + projetAModifier.getDescriptionProjet());
        System.out.println("- Date fin : " + projetAModifier.getDateFinPrevue());
        System.out.println("- Localisation : " + projetAModifier.getLocalisation());
        
        // 4. Appliquer les modifications
        boolean modifie = false;
        
        // Modifier le statut si fourni
        if (modifications.containsKey("statut") && modifications.get("statut") != null) {
            String nouveauStatut = (String) modifications.get("statut");
            try {
                StatutProjet statut = StatutProjet.valueOf(nouveauStatut);
                projetAModifier.setStatut(statut);
                
                // Si le projet passe à EN_COURS, définir la date de début réelle
                if (statut == StatutProjet.EN_COURS && projetAModifier.getDateDebutReelle() == null) {
                    projetAModifier.setDateDebutReelle(LocalDate.now());
                }
                
                // Si le projet est TERMINE, définir la date de fin réelle
                if (statut == StatutProjet.TERMINE && projetAModifier.getDateFinReelle() == null) {
                    projetAModifier.setDateFinReelle(LocalDate.now());
                }
                
                System.out.println(" Statut modifié : " + statut);
                modifie = true;
            } catch (IllegalArgumentException e) {
                System.err.println(" Statut invalide : " + nouveauStatut);
            }
        }
        
        // Modifier la description si fournie
        if (modifications.containsKey("description") && modifications.get("description") != null) {
            String nouvelleDescription = (String) modifications.get("description");
            if (!nouvelleDescription.trim().isEmpty()) {
                projetAModifier.setDescriptionProjet(nouvelleDescription);
                System.out.println(" Description modifiée : " + nouvelleDescription);
                modifie = true;
            }
        }
        
        // Modifier la date de fin si fournie
        if (modifications.containsKey("dateFin") && modifications.get("dateFin") != null) {
            String nouvelleDateFin = (String) modifications.get("dateFin");
            try {
                LocalDate dateFin = LocalDate.parse(nouvelleDateFin);
                
                // Vérifier que la nouvelle date est cohérente
                if (dateFin.isBefore(projetAModifier.getDateDebutPrevue())) {
                    ctx.status(400).json(createErrorResponse("La date de fin ne peut pas être avant la date de début"));
                    return;
                }
                
                projetAModifier.setDateFinPrevue(dateFin);
                System.out.println(" Date de fin modifiée : " + dateFin);
                modifie = true;
            } catch (Exception e) {
                System.err.println(" Format de date invalide : " + nouvelleDateFin);
            }
        }
        
        // 5. Si au moins une modification a été appliquée
        if (modifie) {
            // Mettre à jour la date de dernière modification
            projetAModifier.setDerniereMiseAJour(LocalDateTime.now());
            
            // IMPORTANT : Sauvegarder TOUS les projets
            storage.saveProjets(projets);
            System.out.println("\n Projets sauvegardés dans le storage");

            // Envoyer des notifications selon le type de modification
                if (modifications.containsKey("statut")) {
    envoyerNotificationsChangementProjet(projetAModifier, "STATUT_CHANGE", 
                            projetAModifier.getStatut().toString());
}
                if (modifications.containsKey("dateFin")) {
    envoyerNotificationsChangementProjet(projetAModifier, "DATE_CHANGE", 
                                    "Nouvelle date de fin : " + projetAModifier.getDateFinPrevue());
}
            // Vérifier la sauvegarde
            List<Projet> verification = storage.loadProjets();
            System.out.println("Vérification : " + verification.size() + " projets dans le storage");
            
            // Afficher le nouvel état
            System.out.println("\nNouvel état du projet :");
            System.out.println("- ID : " + projetAModifier.getId());
            System.out.println("- Statut : " + projetAModifier.getStatut());
            System.out.println("- Description : " + projetAModifier.getDescriptionProjet());
            System.out.println("- Date fin : " + projetAModifier.getDateFinPrevue());
            System.out.println("- Dernière MAJ : " + projetAModifier.getDerniereMiseAJour());
            
            // Réponse de succès avec détails
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Projet #" + projetId + " mis à jour avec succès");
            response.put("projet", Map.of(
                "id", projetAModifier.getId(),
                "statut", projetAModifier.getStatut().toString(),
                "description", projetAModifier.getDescriptionProjet(),
                "dateFin", projetAModifier.getDateFinPrevue().toString(),
                "localisation", projetAModifier.getLocalisation(),
                "prestataire", projetAModifier.getPrestataire().getNumeroEntreprise(),
                "derniereMiseAJour", projetAModifier.getDerniereMiseAJour().toString()
            ));
            
            ctx.json(response);
        } else {
            ctx.status(400).json(createErrorResponse("Aucune modification valide fournie"));
        }
        
        System.out.println("=== FIN MISE À JOUR ===\n");
        
    } catch (Exception e) {
        System.err.println("ERREUR dans mettreAJourProjet : " + e.getMessage());
        e.printStackTrace();
        ctx.status(400).json(createErrorResponse("Erreur mise à jour projet: " + e.getMessage()));
    }
}
    
private void consulterProjetsDuPrestataire(Context ctx) {
    try {
        String neq = ctx.pathParam("neq");
        System.out.println("Recherche des projets pour le prestataire NEQ: " + neq);
        
        // Charger tous les projets
        List<Projet> tousProjets = storage.loadProjets();
        
        // Filtrer par prestataire
        List<Projet> projetsDuPrestataire = tousProjets.stream()
            .filter(p -> p.getPrestataire() != null && 
                        p.getPrestataire().getNumeroEntreprise().equals(neq))
            .collect(Collectors.toList());
        
        System.out.println("Projets trouvés pour " + neq + " : " + projetsDuPrestataire.size());
        
        // Convertir en JSON
        List<Map<String, Object>> projetsJson = new ArrayList<>();
        for (Projet p : projetsDuPrestataire) {
            Map<String, Object> pJson = new HashMap<>();
            pJson.put("id", p.getId());
            pJson.put("description", p.getDescriptionProjet());
            pJson.put("localisation", p.getLocalisation());
            pJson.put("type", p.getTypeTravail().getDescription());
            pJson.put("statut", p.getStatut().getDescription());
            pJson.put("dateDebut", p.getDateDebutPrevue().toString());
            pJson.put("dateFin", p.getDateFinPrevue().toString());
            pJson.put("cout", p.getCout());
            pJson.put("prestataire", p.getPrestataire().getNomEntreprise());
            pJson.put("neq", p.getPrestataire().getNumeroEntreprise());
            projetsJson.add(pJson);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("projets", projetsJson);
        response.put("total", projetsJson.size());
        response.put("prestataire", neq);
        
        ctx.json(response);
        
    } catch (Exception e) {
        ctx.status(500).json(createErrorResponse("Erreur consultation projets: " + e.getMessage()));
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
        
        System.out.println("\n=== VALIDATION CANDIDATURE #" + candidatureId + " ===");
        System.out.println("Décision : " + (accepter ? "ACCEPTER" : "REFUSER"));
        
        // 1. Charger toutes les candidatures
        List<Candidature> candidatures = storage.loadCandidatures();
        
        // 2. Trouver la candidature à valider
        Candidature candidatureAValider = null;
        for (Candidature c : candidatures) {
            if (String.valueOf(c.getId()).equals(candidatureId)) {
                candidatureAValider = c;
                break;
            }
        }
        
        if (candidatureAValider == null) {
            ctx.status(404).json(createErrorResponse("Candidature #" + candidatureId + " introuvable"));
            return;
        }
        
        // 3. Vérifier que la candidature est encore en attente
        if (candidatureAValider.getStatut() != StatutCandidature.SOUMISE) {
            ctx.status(400).json(createErrorResponse("Cette candidature a déjà été traitée"));
            return;
        }
        
        // 4. Mettre à jour le statut
        if (accepter) {
            candidatureAValider.setStatut(StatutCandidature.APPROUVEE);
            System.out.println("Candidature APPROUVÉE");
            
            // 5. CRÉER LE PROJET
            List<Probleme> problemes = storage.loadProblemes();
            List<Probleme> problemesVises = new ArrayList<>();
            
            // Trouver les problèmes correspondants
            for (Integer pId : candidatureAValider.getProblemesVises()) {
                for (Probleme p : problemes) {
                    if (p.getId() == pId) {
                        problemesVises.add(p);
                        // Marquer le problème comme résolu
                        p.setResolu(true);
                        break;
                    }
                }
            }
            
            // Créer le nouveau projet
            Projet nouveauProjet = new Projet(candidatureAValider, problemesVises);
            nouveauProjet.setStatut(StatutProjet.APPROUVE);
            
            // Envoyer des notifications pour le nouveau projet
            envoyerNotificationsChangementProjet(nouveauProjet, "NOUVEAU_PROJET", 
                                "Projet approuvé et démarré");

            // Si pas de problèmes trouvés, utiliser les infos de la candidature
            if (problemesVises.isEmpty()) {
                nouveauProjet.setTypeTravail(TypeTravaux.ENTRETIEN_URBAIN);
                nouveauProjet.setPriorite(Priorite.MOYENNE);
                nouveauProjet.setLocalisation("Montréal");
            }
            
            // Charger et mettre à jour la liste des projets
            List<Projet> projets = storage.loadProjets();
            projets.add(nouveauProjet);
            storage.saveProjets(projets);
            
            // Sauvegarder les problèmes mis à jour (maintenant résolus)
            storage.saveProblemes(problemes);
            
            System.out.println("Nouveau projet créé : #" + nouveauProjet.getId());
            System.out.println("- Prestataire : " + nouveauProjet.getPrestataire().getNomEntreprise());
            System.out.println("- Coût : " + nouveauProjet.getCout() + "$");
            
        } else {
            candidatureAValider.setStatut(StatutCandidature.REJETEE);
            
            // Optionnel : ajouter le motif de rejet
            if (validation.containsKey("motif")) {
                candidatureAValider.setCommentaireRejet((String) validation.get("motif"));
            }
            System.out.println("Candidature REJETÉE");
        }
        
        // 6. Sauvegarder les candidatures mises à jour
        storage.saveCandidatures(candidatures);
        System.out.println("Candidatures sauvegardées");
        
        // 7. Vérification
        List<Candidature> verif = storage.loadCandidatures();
        long nbEnAttente = verif.stream()
            .filter(c -> c.getStatut() == StatutCandidature.SOUMISE)
            .count();
        System.out.println("Candidatures restantes en attente : " + nbEnAttente);
        
        // 8. Réponse
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", accepter ? 
            "Candidature #" + candidatureId + " acceptée et projet créé" : 
            "Candidature #" + candidatureId + " refusée");
        
        if (accepter) {
            response.put("projetCree", true);
            List<Projet> projetsActuels = storage.loadProjets();
            response.put("nombreProjets", projetsActuels.size());
        }
        
        ctx.json(response);
        System.out.println("=== FIN VALIDATION ===\n");
        
    } catch (Exception e) {
        System.err.println("ERREUR dans validerCandiature : " + e.getMessage());
        e.printStackTrace();
        ctx.status(400).json(createErrorResponse("Erreur validation candidature: " + e.getMessage()));
    }
}

private void modifierPrioriteProbleme(Context ctx) {
    try {
        String problemeId = ctx.pathParam("id");
        Map<String, Object> requestData = ctx.bodyAsClass(Map.class);
        String nouvellePriorite = (String) requestData.get("priorite");
        
        System.out.println("\n=== MODIFICATION PRIORITÉ PROBLÈME #" + problemeId + " ===");
        System.out.println("Nouvelle priorité demandée : " + nouvellePriorite);
        
        // IMPORTANT : Synchroniser d'abord avec le storage
        gestionnaireProblemes.synchroniserAvecStorage(storage);
        
        // 1. Charger tous les problèmes DEPUIS LE STORAGE
        List<Probleme> problemes = storage.loadProblemes();
        System.out.println("Nombre de problèmes chargés : " + problemes.size());
        
        // 2. Trouver le problème à modifier
        Probleme problemeAModifier = null;
        for (Probleme p : problemes) {
            if (p.getId() == Integer.parseInt(problemeId)) {
                problemeAModifier = p;
                break;
            }
        }
        
        if (problemeAModifier == null) {
            ctx.status(404).json(createErrorResponse("Problème #" + problemeId + " introuvable"));
            return;
        }
        
        // 3. Afficher l'état actuel
        System.out.println("Problème trouvé : " + problemeAModifier.getDescription());
        System.out.println("Priorité actuelle : " + problemeAModifier.getPriorite());
        
        // 4. Modifier la priorité
        try {
            Priorite priorite = Priorite.valueOf(nouvellePriorite);
            Priorite anciennePriorite = problemeAModifier.getPriorite();
            problemeAModifier.setPriorite(priorite);
            
            // 5. IMPORTANT : Sauvegarder les changements
            storage.saveProblemes(problemes);
            System.out.println(" Problèmes sauvegardés dans le storage");
            
            // 6. Vérifier la sauvegarde
            List<Probleme> verification = storage.loadProblemes();
            Probleme pVerif = verification.stream()
                .filter(p -> p.getId() == Integer.parseInt(problemeId))
                .findFirst()
                .orElse(null);
            
            if (pVerif != null) {
                System.out.println("Vérification - Nouvelle priorité : " + pVerif.getPriorite());
            }
            
            System.out.println(" Priorité modifiée : " + anciennePriorite + " → " + priorite);
            
            // NOUVEAU : Notifier les prestataires de la priorité affectée
envoyerNotificationsPrioriteAffectee(problemeAModifier, priorite);
            // 7. Si le problème a un projet associé, mettre à jour sa priorité aussi
            List<Projet> projets = storage.loadProjets();
            boolean projetModifie = false;
            
            for (Projet projet : projets) {
                if (projet.getProblemesVises().contains(problemeAModifier.getId())) {
                    projet.setPriorite(priorite);
                    projetModifie = true;
                    System.out.println(" Priorité du projet #" + projet.getId() + " également mise à jour");
                }
            }
            
            if (projetModifie) {
                storage.saveProjets(projets);
            }
            
            // 8. Resynchroniser le gestionnaire
            gestionnaireProblemes.synchroniserAvecStorage(storage);
            
            // 9. Réponse de succès
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Priorité du problème #" + problemeId + " modifiée avec succès");
            response.put("anciennePriorite", anciennePriorite.toString());
            response.put("nouvellePriorite", priorite.toString());
            
            ctx.json(response);
            
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(createErrorResponse("Priorité invalide : " + nouvellePriorite));
        }
        
        System.out.println("=== FIN MODIFICATION PRIORITÉ ===\n");
        
    } catch (Exception e) {
        System.err.println("ERREUR dans modifierPrioriteProbleme : " + e.getMessage());
        e.printStackTrace();
        ctx.status(500).json(createErrorResponse("Erreur modification priorité: " + e.getMessage()));
    }
}

    
    private void consulterCandidatures(Context ctx) {
        try {
            // TOUJOURS charger les candidatures fraîches depuis le storage
            List<Candidature> candidatures = storage.loadCandidatures();
            
            System.out.println("DEBUG - Nombre total de candidatures chargées : " + candidatures.size());
            
            // Filtrer seulement les candidatures en attente
            List<Candidature> candidaturesEnAttente = candidatures.stream()
                .filter(c -> c.getStatut() == StatutCandidature.SOUMISE)
                .collect(Collectors.toList());
            
            System.out.println("DEBUG - Nombre de candidatures en attente : " + candidaturesEnAttente.size());
            
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
            System.err.println("ERREUR dans consulterCandidatures : " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(createErrorResponse("Erreur consultation candidatures: " + e.getMessage()));
        }
    }
    // ================================================================
    // Notifications
    // ================================================================
    private void consulterPreferencesPrestataire(Context ctx) {
    try {
        String neq = ctx.pathParam("neq");
        
        List<AbonnementPrestataire> abonnements = storage.loadAbonnementsPrestataires();
        List<AbonnementPrestataire> mesAbonnements = abonnements.stream()
            .filter(a -> a.getNeq().equals(neq))
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("abonnements", mesAbonnements);
        response.put("total", mesAbonnements.size());
        
        ctx.json(response);
        
    } catch (Exception e) {
        ctx.status(500).json(createErrorResponse("Erreur consultation préférences"));
    }
}

private void creerAbonnementPrestataire(Context ctx) {
    try {
        String neq = ctx.pathParam("neq");
        Map<String, Object> requestData = ctx.bodyAsClass(Map.class);
        String type = (String) requestData.get("type"); // "QUARTIER" ou "TYPE_TRAVAUX"
        String valeur = (String) requestData.get("valeur");
        
        List<AbonnementPrestataire> abonnements = storage.loadAbonnementsPrestataires();
        AbonnementPrestataire nouvelAbo = new AbonnementPrestataire(neq, type, valeur);
        
        if (!abonnements.contains(nouvelAbo)) {
            abonnements.add(nouvelAbo);
            storage.saveAbonnementsPrestataires(abonnements);
            
            ctx.json(createSuccessResponse("Abonnement créé pour " + type + " : " + valeur));
        } else {
            ctx.json(createSuccessResponse("Vous êtes déjà abonné à " + type + " : " + valeur));
        }
        
    } catch (Exception e) {
        ctx.status(400).json(createErrorResponse("Erreur création abonnement"));
    }
}

private void consulterNotificationsPrestataire(Context ctx) {
    try {
        String neq = ctx.pathParam("neq");
        
        // Charger toutes les notifications
        List<Notification> toutesNotifs = storage.loadNotifications();
        // Filtrage simple et direct
        List<Map<String, Object>> notifications = new ArrayList<>();
        int totalNotifs = 0;
        int nonLues = 0;
        
        for (Notification n : toutesNotifs) {
            // Vérifier si c'est pour ce prestataire
            if (neq.equals(n.getDestinataire()) && "PRESTATAIRE".equals(n.getTypeDestinataire())) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("message", n.getMessage());
                notif.put("lu", n.isLu());
                notif.put("type", n.getTypeChangement());
                notif.put("problemeId", n.getProblemeId());
                
                // Formater la date simplement
                if (n.getDateCreation() != null) {
                    notif.put("date", n.getDateCreation().toLocalDate().toString());
                } else {
                    notif.put("date", "2025-08-01");
                }
                
                notifications.add(notif);
                totalNotifs++;
                
                if (!n.isLu()) {
                    nonLues++;
                }
            }
        }
        
        // Trier par date (plus récent en premier) - simple
        notifications.sort((n1, n2) -> {
            String date1 = (String) n1.get("date");
            String date2 = (String) n2.get("date");
            return date2.compareTo(date1);
        });
        
        // Réponse finale
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("total", totalNotifs);
        response.put("non_lues", nonLues);
        
        ctx.json(response);
        
    } catch (Exception e) {
        System.err.println("ERREUR dans consulterNotificationsPrestataire : " + e.getMessage());
        e.printStackTrace();
        ctx.status(500).json(Map.of("error", e.getMessage()));
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
    
    // Méthode helper pour extraire le quartier d'une localisation
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
    private void envoyerNotificationsChangementProjet(Projet projet, String typeChangement, String details) {
    try {
        // Extraire le quartier du projet
        String quartierProjet = extraireQuartier(projet.getLocalisation());
        
        // Charger tous les abonnements
        List<Abonnement> abonnements = storage.loadAbonnements();
        
        // Trouver les résidents abonnés à ce quartier ou cette rue
        Set<String> residentsANotifier = new HashSet<>();
        
        for (Abonnement abo : abonnements) {
            boolean doitNotifier = false;
            
            // Abonné au quartier
            if (abo.getType().equals("QUARTIER") && 
                abo.getValeur().equalsIgnoreCase(quartierProjet)) {
                doitNotifier = true;
            }
            
            // Abonné à une rue qui est dans la localisation
            if (abo.getType().equals("RUE") && 
                projet.getLocalisation().toLowerCase().contains(abo.getValeur().toLowerCase())) {
                doitNotifier = true;
            }
            
            if (doitNotifier) {
                residentsANotifier.add(abo.getResidentEmail());
            }
        }
        
        
        // Créer les notifications
        String message = "";
        switch (typeChangement) {
            case "NOUVEAU_PROJET":
                message = "Nouveau projet dans votre quartier : " + projet.getDescriptionProjet();
                break;
            case "STATUT_CHANGE":
                message = "Le projet '" + projet.getDescriptionProjet() + 
                         "' a changé de statut : " + details;
                break;
            case "PRIORITE_CHANGE":
                message = "La priorité du projet '" + projet.getDescriptionProjet() + 
                         "' a été modifiée : " + details;
                break;
            case "DATE_CHANGE":
                message = "Les dates du projet '" + projet.getDescriptionProjet() + 
                         "' ont été modifiées";
                break;
        }
        
        // Envoyer les notifications
        for (String email : residentsANotifier) {
            storage.creerNotification(email, message, typeChangement, 
                                    projet.getId(), quartierProjet);
        }
        
        System.out.println("Notifications envoyées à " + residentsANotifier.size() + 
                          " résidents pour le projet #" + projet.getId());
        
    } catch (Exception e) {
        System.err.println("Erreur envoi notifications : " + e.getMessage());
    }
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
    /**
 * Marquer toutes les notifications d'un résident comme lues
 */
private void marquerNotificationsLues(Context ctx) {
    try {
        String email = ctx.pathParam("email");
        
        List<Notification> notifications = storage.loadNotifications();
        boolean modifie = false;
        
        for (Notification n : notifications) {
            if (email.equals(n.getResidentEmail()) && !n.isLu()) {
                n.setLu(true);
                modifie = true;
            }
        }
        
        if (modifie) {
            storage.saveNotifications(notifications);
        }
        
        ctx.json(createSuccessResponse("Notifications marquées comme lues"));
        
    } catch (Exception e) {
        ctx.status(500).json(createErrorResponse("Erreur : " + e.getMessage()));
    }
}

/**
 * Marquer toutes les notifications d'un prestataire comme lues
 */
private void marquerNotificationsLuesPrestataire(Context ctx) {
    try {
        String neq = ctx.pathParam("neq");
        
        List<Notification> notifications = storage.loadNotifications();
        boolean modifie = false;
        
        for (Notification n : notifications) {
            if (neq.equals(n.getDestinataire()) && 
                "PRESTATAIRE".equals(n.getTypeDestinataire()) && 
                !n.isLu()) {
                n.setLu(true);
                modifie = true;
            }
        }
        
        if (modifie) {
            storage.saveNotifications(notifications);
        }
        
        ctx.json(createSuccessResponse("Notifications marquées comme lues"));
        
    } catch (Exception e) {
        ctx.status(500).json(createErrorResponse("Erreur : " + e.getMessage()));
    }
}

/**
 * Envoie des notifications aux prestataires quand une priorité est affectée à un problème
 */
private void envoyerNotificationsPrioriteAffectee(Probleme probleme, Priorite nouvellePriorite) {
    try {
        String quartierProbleme = extraireQuartier(probleme.getLieu());
        
        List<AbonnementPrestataire> abonnements = storage.loadAbonnementsPrestataires();
        
        Set<String> prestatairesANotifier = new HashSet<>();
        
        for (AbonnementPrestataire abo : abonnements) {
            boolean doitNotifier = false;
            
            if (abo.getType().equals("QUARTIER") && 
                abo.getValeur().equalsIgnoreCase(quartierProbleme)) {
                doitNotifier = true;
            }
            
            if (abo.getType().equals("TYPE_TRAVAUX") && 
                abo.getValeur().equals(probleme.getTypeProbleme().name())) {
                doitNotifier = true;
            }
            
            if (doitNotifier) {
                prestatairesANotifier.add(abo.getNeq());
            }
        }
        
        String message = String.format("Priorité %s affectée au problème #%d (%s à %s)", 
                                      nouvellePriorite.getDescription(),
                                      probleme.getId(),
                                      probleme.getTypeProbleme().getDescription(),
                                      probleme.getLieu());
        
        for (String neq : prestatairesANotifier) {
            storage.creerNotificationPrestataire(neq, message, "PRIORITE_AFFECTEE", 
                                                probleme.getId(), quartierProbleme, 
                                                nouvellePriorite.toString());
        }
        
        System.out.println("Notifications priorité envoyées à " + prestatairesANotifier.size() + 
                          " prestataires pour le problème #" + probleme.getId());
        
    } catch (Exception e) {
        System.err.println("Erreur envoi notifications priorité : " + e.getMessage());
    }
}
/**
 * Modifier les préférences de notification d'un résident
 */
private void modifierPreferencesResident(Context ctx) {
    try {
        String email = ctx.pathParam("email");
        Map<String, Object> preferences = ctx.bodyAsClass(Map.class);
        
        // Sauvegarder les préférences (méthode simplifiée)
        storage.savePreferencesNotification(email, preferences);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Préférences mises à jour avec succès");
        response.put("preferences", preferences);
        
        ctx.json(response);
        
    } catch (Exception e) {
        ctx.status(500).json(createErrorResponse("Erreur préférences : " + e.getMessage()));
    }
}
}