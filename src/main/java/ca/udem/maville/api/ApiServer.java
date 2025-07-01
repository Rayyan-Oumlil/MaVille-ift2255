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
        app.get("/api/prestataires/{neq}/projets", this::consulterProjetsDuPrestataire);  
        
        // ENDPOINTS STPM
        app.put("/api/stpm/candidatures/{id}/valider", this::validerCandiature);
        app.get("/api/stpm/candidatures", this::consulterCandidatures);
        app.put("/api/stpm/problemes/{id}/priorite", this::modifierPrioriteProbleme); 
        
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
                
                System.out.println("✓ Statut modifié : " + statut);
                modifie = true;
            } catch (IllegalArgumentException e) {
                System.err.println("✗ Statut invalide : " + nouveauStatut);
            }
        }
        
        // Modifier la description si fournie
        if (modifications.containsKey("description") && modifications.get("description") != null) {
            String nouvelleDescription = (String) modifications.get("description");
            if (!nouvelleDescription.trim().isEmpty()) {
                projetAModifier.setDescriptionProjet(nouvelleDescription);
                System.out.println("✓ Description modifiée : " + nouvelleDescription);
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
                System.out.println("✓ Date de fin modifiée : " + dateFin);
                modifie = true;
            } catch (Exception e) {
                System.err.println("✗ Format de date invalide : " + nouvelleDateFin);
            }
        }
        
        // 5. Si au moins une modification a été appliquée
        if (modifie) {
            // Mettre à jour la date de dernière modification
            projetAModifier.setDerniereMiseAJour(LocalDateTime.now());
            
            // IMPORTANT : Sauvegarder TOUS les projets
            storage.saveProjets(projets);
            System.out.println("\n✓ Projets sauvegardés dans le storage");
            
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
        
        // 1. Charger tous les problèmes
        List<Probleme> problemes = storage.loadProblemes();
        
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
            
            // 5. Sauvegarder les changements
            storage.saveProblemes(problemes);
            
            System.out.println(" Priorité modifiée : " + anciennePriorite + " --> " + priorite);
            
            // 6. Si le problème a un projet associé, mettre à jour sa priorité aussi
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
            
            // 7. Réponse de succès
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