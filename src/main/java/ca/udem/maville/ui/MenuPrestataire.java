package ca.udem.maville.ui;

import ca.udem.maville.ui.client.HttpClient;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * Menu prestataire avec login persistant
 */
public class MenuPrestataire {
    private HttpClient httpClient;
    private SaisieConsole saisie;
    private String neqConnecte; // NEQ du prestataire connecté
    private String nomEntreprise; // Nom de l'entreprise connectée
    
    public MenuPrestataire(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.saisie = new SaisieConsole();
        this.neqConnecte = null;
        this.nomEntreprise = null;
    }
    
    public void afficher() {
        // LOGIN UNE SEULE FOIS
        if (neqConnecte == null) {
            effectuerLogin();
        }
        
        boolean continuer = true;
        
        System.out.println("\n=== Interface Prestataire - Architecture REST ===");
        System.out.println("Connecté : " + nomEntreprise + " (NEQ: " + neqConnecte + ")");
        
        while (continuer) {
            afficherOptions();
            int choix = saisie.lireEntier("Votre choix: ");
            
            switch (choix) {
                case 1:
                    consulterProblemes();
                    break;
                case 2:
                    rechercherProblemes();
                    break;
                case 3:
                    soumettreCandiature();
                    break;
                case 4:
                    mettreAJourMesProjets();
                    break;
                case 5:
                    voirMesNotifications();
                    break;
                case 6:
                    modifierPreferencesNotification();
                    break;
                case 0:
                    continuer = false;
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
            }
        }
    }
    
    /**
     * Effectue le login du prestataire
     */
    private void effectuerLogin() {
        System.out.println("\n=== CONNEXION PRESTATAIRE ===");
        System.out.println("Prestataires disponibles :");
        System.out.println("1. NEQ1234567890 - Construction ABC Inc.");
        System.out.println("2. NEQ0987654321 - Pavage Pro Ltée");
        System.out.println("3. NEQ1122334455 - Électricité Montréal");
        System.out.println("4. NEQ5544332211 - Paysagement Vert");
        System.out.println("5. NEQ9988776655 - TechnoVert Solutions");
        System.out.println("6. Autre NEQ");
        
        int choix = saisie.lireEntier("Choisir un prestataire (1-6) : ");
        
        switch (choix) {
            case 1:
                neqConnecte = "NEQ1234567890";
                nomEntreprise = "Construction ABC Inc.";
                break;
            case 2:
                neqConnecte = "NEQ0987654321";
                nomEntreprise = "Pavage Pro Ltée";
                break;
            case 3:
                neqConnecte = "NEQ1122334455";
                nomEntreprise = "Électricité Montréal";
                break;
            case 4:
                neqConnecte = "NEQ5544332211";
                nomEntreprise = "Paysagement Vert";
                break;
            case 5:
                neqConnecte = "NEQ9988776655";
                nomEntreprise = "TechnoVert Solutions";
                break;
            case 6:
                neqConnecte = saisie.lireChaineNonVide("Votre NEQ : ");
                nomEntreprise = saisie.lireChaineNonVide("Nom de votre entreprise : ");
                break;
            default:
                neqConnecte = "NEQ1234567890";
                nomEntreprise = "Construction ABC Inc.";
        }
        
        System.out.println(" Connecté en tant que : " + nomEntreprise + " (" + neqConnecte + ")");
    }
    
    private void afficherOptions() {
        // Compter les notifications non lues
        int notificationsNonLues = compterNotificationsNonLues(neqConnecte);
        
        System.out.println("\n=== MENU PRESTATAIRE ===");
        System.out.println("Connecté : " + nomEntreprise + " (" + neqConnecte + ")");
        if (notificationsNonLues > 0) {
            System.out.println(" " + notificationsNonLues + " notification(s) non lue(s)");
        }
        System.out.println("1. Consulter tous les problèmes disponibles");
        System.out.println("2. Rechercher problèmes (avec filtres)");
        System.out.println("3. Soumettre une candidature");
        System.out.println("4. Mettre à jour mes projets");
        System.out.println("5. Voir mes notifications (" + notificationsNonLues + " non lues)");
        System.out.println("6. Modifier mes préférences de notification");
        System.out.println("0. Retour menu principal");
    }

    private int compterNotificationsNonLues(String neq) {
    try {
        String response = httpClient.consulterNotificationsPrestataire(neq);
        
        // Parser la réponse formatée pour extraire le nombre
        if (response.contains("Notifications non lues :")) {
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.contains("Notifications non lues :")) {
                    String nombre = line.substring(line.indexOf(":") + 1).trim();
                    return Integer.parseInt(nombre);
                }
            }
        }
        
        // Si le format est différent, essayer avec JSON
        ObjectMapper mapper = new ObjectMapper();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = mapper.readValue(response, Map.class);
            Object nonLues = data.get("non_lues");
            if (nonLues instanceof Integer) {
                return (Integer) nonLues;
            }
        } catch (Exception e) {
            // Ignorer l'erreur JSON et continuer
        }
        
        return 0;
    } catch (Exception e) {
        System.err.println("Erreur comptage notifications prestataire : " + e.getMessage());
        return 0;
    }
}
    
    /**
     * Consulter tous les problèmes disponibles via API REST
     */
    private void consulterProblemes() {
        System.out.println("\n=== TOUS LES PROBLEMES DISPONIBLES ===");
        
        System.out.println("Récupération des problèmes via API REST...");
        
        String resultat = httpClient.consulterProblemes(null, null);
        System.out.println(resultat);
        
        System.out.println("\nCes problèmes sont disponibles pour soumission de candidatures.");
        pauseAvantContinuer();
    }
    
    /**
     * Rechercher des problèmes avec filtres via API REST
     */
    private void rechercherProblemes() {
        System.out.println("\n=== RECHERCHER DES PROBLEMES ===");
        
        System.out.println("Filtres de recherche disponibles :");
        System.out.println("1. Par quartier uniquement");
        System.out.println("2. Par type de travaux uniquement");
        System.out.println("3. Par quartier ET type de travaux");
        System.out.println("0. Annuler");
        
        int choixFiltre = saisie.lireEntier("Votre choix : ");
        
        String quartier = null;
        String typeTravaux = null;
        
        switch (choixFiltre) {
            case 1:
                System.out.println("\nQuartiers disponibles : Centre-ville, Rosemont, Plateau, Ville-Marie, etc.");
                quartier = saisie.lireChaine("Entrez le nom du quartier : ");
                break;
                
            case 2:
                typeTravaux = choisirTypeTravaux();
                break;
                
            case 3:
                System.out.println("\nQuartiers disponibles : Centre-ville, Rosemont, Plateau, Ville-Marie, etc.");
                quartier = saisie.lireChaine("Entrez le nom du quartier : ");
                typeTravaux = choisirTypeTravaux();
                break;
                
            case 0:
                return;
                
            default:
                System.out.println("Choix invalide");
                return;
        }
        
        System.out.println("\n=== RECHERCHE EN COURS ===");
        if (quartier != null && !quartier.trim().isEmpty()) {
            System.out.println("Quartier : " + quartier);
        }
        if (typeTravaux != null && !typeTravaux.trim().isEmpty()) {
            System.out.println("Type de travaux : " + typeTravaux);
        }
        
        System.out.println("\nRécupération des problèmes...");
        String resultat = httpClient.consulterProblemes(quartier, typeTravaux);
        System.out.println("\n" + resultat);
        
        if (resultat.contains("Nombre de problèmes trouvés : 0")) {
            System.out.println("\nAucun problème ne correspond à vos critères de recherche.");
            System.out.println("Essayez avec d'autres filtres.");
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * Soumettre une candidature pour un projet via API REST
     */
    private void soumettreCandiature() {
        System.out.println("\n=== SOUMETTRE UNE CANDIDATURE ===");
        
        // Utiliser le NEQ connecté automatiquement
        String titre = saisie.lireChaine("Titre du projet : ");
        String description = saisie.lireChaine("Description du projet : ");
        
        System.out.println("\nType de travaux :");
        String typeTravaux = choisirTypeTravaux();
        
        // Validation des dates
        String dateDebut = "";
        String dateFin = "";
        boolean datesValides = false;
        
        while (!datesValides) {
            System.out.println(" IMPORTANT : Les dates doivent être au format AAAA-MM-JJ");
            System.out.println("Exemple : 2025-07-15 pour le 15 juillet 2025");
            
            dateDebut = saisie.lireChaine("Date de début (AAAA-MM-JJ) : ");
            
            if (!dateDebut.matches("\\d{4}-\\d{2}-\\d{2}")) {
                System.out.println(" Format incorrect ! Utilisez AAAA-MM-JJ (ex: 2025-07-15)");
                continue;
            }
            
            dateFin = saisie.lireChaine("Date de fin (AAAA-MM-JJ) : ");
            
            if (!dateFin.matches("\\d{4}-\\d{2}-\\d{2}")) {
                System.out.println(" Format incorrect ! Utilisez AAAA-MM-JJ (ex: 2025-07-20)");
                continue;
            }
            
            try {
                java.time.LocalDate debut = java.time.LocalDate.parse(dateDebut);
                java.time.LocalDate fin = java.time.LocalDate.parse(dateFin);
                
                if (fin.isBefore(debut)) {
                    System.out.println(" La date de fin doit être après la date de début !");
                    continue;
                }
                
                datesValides = true;
                System.out.println(" Dates valides !");
                
            } catch (Exception e) {
                System.out.println(" Dates invalides : " + e.getMessage());
            }
        }
        
        // Gestion du coût
        double cout = 0;
        boolean coutValide = false;
        
        while (!coutValide) {
            try {
                cout = saisie.lireDouble("Coût estimé ($) : ");
                if (cout <= 0) {
                    System.out.println(" Le coût doit être supérieur à 0");
                    continue;
                }
                coutValide = true;
            } catch (Exception e) {
                System.out.println(" Montant invalide, veuillez entrer un nombre");
            }
        }
        
        // Récapitulatif
        System.out.println("\n=== RÉCAPITULATIF CANDIDATURE ===");
        System.out.println("Prestataire : " + nomEntreprise + " (" + neqConnecte + ")");
        System.out.println("Titre : " + titre);
        System.out.println("Type : " + typeTravaux);
        System.out.println("Période : " + dateDebut + " à " + dateFin);
        System.out.println("Coût : " + cout + "$");
        
        String confirmation = saisie.lireChaine("\nConfirmer la soumission? (oui/non) : ");
        
        if (confirmation.toLowerCase().startsWith("o")) {
            System.out.println("\nSoumission de la candidature via API REST...");
            
            String resultat = httpClient.soumettreCandiature(
                neqConnecte, titre, description, typeTravaux, 
                dateDebut, dateFin, cout
            );
            
            System.out.println("\n=== RÉSULTAT ===");
            System.out.println(resultat);
            
            if (resultat.contains("succès")) {
                System.out.println(" Votre candidature a été enregistrée avec succès !");
                System.out.println("Elle sera évaluée par le STPM dans les plus brefs délais.");
            }
        } else {
            System.out.println("Candidature annulée.");
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * Mettre à jour les projets du prestataire
     */
    private void mettreAJourMesProjets() {
        System.out.println("\n=== METTRE À JOUR MES PROJETS ===");
        
        System.out.println("Recherche de vos projets en cours...");
        
        // Utiliser le NEQ connecté automatiquement
        String resultats = httpClient.consulterProjetsDuPrestataire(neqConnecte);
        System.out.println(resultats);
        
        if (resultats.contains("Aucun projet trouvé")) {
            System.out.println(" Conseil : Soumettez d'abord des candidatures (option 3)");
            System.out.println("   et attendez qu'elles soient acceptées par le STPM.");
            pauseAvantContinuer();
            return;
        }
        
        // Parser les projets pour obtenir les IDs valides
        java.util.List<String> projectIds = new java.util.ArrayList<>();
        String[] lines = resultats.split("\n");
        for (String line : lines) {
            if (line.contains("PROJET #")) {
                String id = line.substring(line.indexOf("#") + 1).trim();
                if (id.matches("\\d+")) {
                    projectIds.add(id);
                }
            }
        }
        
        if (projectIds.isEmpty()) {
            System.out.println("Aucun projet trouvé pour modification.");
            pauseAvantContinuer();
            return;
        }
        
        // Sélection du projet
        System.out.println("\n─────────────────────────────────────");
        System.out.println("Actions disponibles :");
        System.out.println("- Entrez l'ID du projet à modifier (IDs disponibles : " + String.join(", ", projectIds) + ")");
        System.out.println("- Tapez 'Q' pour quitter");
        System.out.print("\nVotre choix : ");
        
        String choix = saisie.lireChaine("");
        
        if (choix.equalsIgnoreCase("Q")) {
            return;
        }
        
        if (!projectIds.contains(choix)) {
            System.out.println(" ID invalide. Ce projet ne vous appartient pas ou n'existe pas.");
            pauseAvantContinuer();
            return;
        }
        
        // Recharger les données du projet sélectionné
        System.out.println("\n Récupération de l'état actuel du projet #" + choix + "...");
        String etatActuel = httpClient.consulterProjetsDuPrestataire(neqConnecte);
        
        // Extraire les infos actuelles du projet sélectionné
        String statutActuel = "INCONNU";
        String descriptionActuelle = "";
        String dateFinActuelle = "";
        
        boolean projetTrouve = false;
        String[] lignes = etatActuel.split("\n");
        for (int i = 0; i < lignes.length; i++) {
            if (lignes[i].contains("PROJET #" + choix)) {
                projetTrouve = true;
                for (int j = i + 1; j < lignes.length && j < i + 10; j++) {
                    if (lignes[j].contains("- Description :")) {
                        descriptionActuelle = lignes[j].substring(lignes[j].indexOf(":") + 1).trim();
                    } else if (lignes[j].contains("- Statut :")) {
                        statutActuel = lignes[j].substring(lignes[j].indexOf(":") + 1).trim();
                    } else if (lignes[j].contains(" au ")) {
                        String dates = lignes[j];
                        dateFinActuelle = dates.substring(dates.lastIndexOf(" ") + 1).trim();
                    }
                }
                break;
            }
        }
        
        if (!projetTrouve) {
            System.out.println(" Erreur lors de la récupération du projet.");
            pauseAvantContinuer();
            return;
        }
        
        // Afficher l'état actuel
        System.out.println("\n ÉTAT ACTUEL DU PROJET #" + choix);
        System.out.println("════════════════════════════════════");
        System.out.println("Statut actuel : " + statutActuel);
        System.out.println("Description : " + descriptionActuelle);
        System.out.println("Date fin prévue : " + dateFinActuelle);
        System.out.println("════════════════════════════════════");
        
        // Modification du projet sélectionné
        System.out.println("\n=== MODIFICATION DU PROJET #" + choix + " ===");
        System.out.println("\nQue voulez-vous modifier ?");
        System.out.println("1. Statut du projet");
        System.out.println("2. Description");
        System.out.println("3. Date de fin prévue");
        System.out.println("4. Tout modifier");
        System.out.println("0. Annuler");
        
        int typeModif = saisie.lireEntier("Votre choix : ");
        
        String nouveauStatut = null;
        String nouvelleDescription = null;
        String nouvelleDateFin = null;
        
        switch (typeModif) {
            case 1:
                System.out.println("\nStatuts disponibles :");
                System.out.println("1. EN_COURS");
                System.out.println("2. SUSPENDU");
                System.out.println("3. TERMINE");
                System.out.println("Statut actuel : " + statutActuel);
                int statut = saisie.lireEntier("Nouveau statut : ");
                switch (statut) {
                    case 1: nouveauStatut = "EN_COURS"; break;
                    case 2: nouveauStatut = "SUSPENDU"; break;
                    case 3: nouveauStatut = "TERMINE"; break;
                }
                break;
                
            case 2:
                System.out.println("Description actuelle : " + descriptionActuelle);
                nouvelleDescription = saisie.lireChaine("Nouvelle description : ");
                break;
                
            case 3:
                System.out.println("Date fin actuelle : " + dateFinActuelle);
                nouvelleDateFin = saisie.lireChaine("Nouvelle date de fin (AAAA-MM-JJ) : ");
                while (!nouvelleDateFin.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    System.out.println(" Format incorrect !");
                    nouvelleDateFin = saisie.lireChaine("Nouvelle date de fin (AAAA-MM-JJ) : ");
                }
                break;
                
            case 4:
                System.out.println("\nStatuts : 1=EN_COURS, 2=SUSPENDU, 3=TERMINE");
                System.out.println("Statut actuel : " + statutActuel);
                int st = saisie.lireEntier("Nouveau statut : ");
                switch (st) {
                    case 1: nouveauStatut = "EN_COURS"; break;
                    case 2: nouveauStatut = "SUSPENDU"; break;
                    case 3: nouveauStatut = "TERMINE"; break;
                }
                
                System.out.println("Description actuelle : " + descriptionActuelle);
                nouvelleDescription = saisie.lireChaine("Nouvelle description : ");
                
                System.out.println("Date fin actuelle : " + dateFinActuelle);
                nouvelleDateFin = saisie.lireChaine("Nouvelle date de fin (AAAA-MM-JJ) : ");
                while (!nouvelleDateFin.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    System.out.println(" Format incorrect !");
                    nouvelleDateFin = saisie.lireChaine("Nouvelle date de fin (AAAA-MM-JJ) : ");
                }
                break;
                
            case 0:
                System.out.println("Modification annulée.");
                pauseAvantContinuer();
                return;
        }
        
        // Récapitulatif des modifications
        System.out.println("\n=== RÉCAPITULATIF DES MODIFICATIONS ===");
        if (nouveauStatut != null) System.out.println("Nouveau statut : " + nouveauStatut);
        if (nouvelleDescription != null && !nouvelleDescription.trim().isEmpty()) 
            System.out.println("Nouvelle description : " + nouvelleDescription);
        if (nouvelleDateFin != null) System.out.println("Nouvelle date fin : " + nouvelleDateFin);
        
        String confirm = saisie.lireChaine("\nConfirmer les modifications ? (oui/non) : ");
        
        if (confirm.toLowerCase().startsWith("o")) {
            System.out.println("\nMise à jour en cours...");
            
            String resultat = httpClient.mettreAJourProjet(
                choix, nouveauStatut, nouvelleDescription, nouvelleDateFin, neqConnecte
            );
            
            System.out.println(resultat);
            
            if (resultat.contains("succès")) {
                System.out.println(" Projet mis à jour avec succès !");
                
                // Afficher immédiatement le nouvel état
                System.out.println("\n Récupération du nouvel état...");
                String nouvelEtat = httpClient.consulterProjetsDuPrestataire(neqConnecte);
                
                // Extraire et afficher uniquement le projet modifié
                String[] lignesNouvelEtat = nouvelEtat.split("\n");
                boolean affichageCommence = false;
                System.out.println("\n NOUVEL ÉTAT DU PROJET #" + choix);
                System.out.println("════════════════════════════════════");
                for (String ligne : lignesNouvelEtat) {
                    if (ligne.contains("PROJET #" + choix)) {
                        affichageCommence = true;
                    }
                    if (affichageCommence && (ligne.trim().isEmpty() || ligne.contains("PROJET #"))) {
                        if (!ligne.contains("PROJET #" + choix)) {
                            break;
                        }
                    }
                    if (affichageCommence) {
                        System.out.println(ligne);
                    }
                }
                System.out.println("════════════════════════════════════");
            }
        } else {
            System.out.println("Modifications annulées.");
        }
        
        pauseAvantContinuer();
    }

    /**
     * Voir les notifications du prestataire
     */
    private void voirMesNotifications() {
        System.out.println("\n=== MES NOTIFICATIONS ===");
        
        System.out.println("\n Récupération de vos notifications...");
        
        try {
            String resultat = httpClient.consulterNotificationsPrestataire(neqConnecte);
            
            System.out.println("\n--- VOS NOTIFICATIONS ---");
            System.out.println(resultat);
            
            System.out.println("\nOptions :");
            System.out.println("1. Marquer toutes comme lues");
            System.out.println("2. Retour");
            
            int choix = saisie.lireEntier("Votre choix : ");
            if (choix == 1) {
    String reponse = httpClient.marquerNotificationsLues(neqConnecte);
    System.out.println(" " + reponse);
}
            
        } catch (Exception e) {
            System.out.println(" Erreur lors de la récupération des notifications : " + e.getMessage());
        }
        
        pauseAvantContinuer();
    }

    /**
     * Modifier les préférences de notification
     */
    private void modifierPreferencesNotification() {
        System.out.println("\n=== MODIFIER MES PRÉFÉRENCES DE NOTIFICATION ===");
        
        System.out.println("Prestataire connecté : " + nomEntreprise + " (" + neqConnecte + ")");
        
        System.out.println("\n Vos abonnements actuels :");
        try {
            String abonnementsActuels = httpClient.consulterAbonnementsPrestataire(neqConnecte);
            System.out.println(abonnementsActuels);
        } catch (Exception e) {
            System.out.println("Aucun abonnement trouvé.");
        }
        
        System.out.println("\n--- NOUVEL ABONNEMENT ---");
        System.out.println("À quoi voulez-vous vous abonner ?");
        System.out.println("1. Un quartier (notifications quand priorité affectée)");
        System.out.println("2. Un type de travaux");
        System.out.println("3. Retour");
        
        int choix = saisie.lireEntier("Votre choix : ");
        
        switch (choix) {
            case 1:
                System.out.println("\nQuartiers disponibles : Rosemont, Plateau, Ville-Marie, Hochelaga, etc.");
                String quartier = saisie.lireChaineNonVide("Nom du quartier : ");
                
                try {
                    String resultat = httpClient.creerAbonnementPrestataire(neqConnecte, "QUARTIER", quartier);
                    System.out.println("\n" + resultat);
                    System.out.println(" Vous recevrez des notifications quand une priorité sera affectée aux problèmes dans " + quartier);
                } catch (Exception e) {
                    System.out.println(" Erreur : " + e.getMessage());
                }
                break;
                
            case 2:
                String typeTravaux = choisirTypeTravaux();
                
                try {
                    String resultat = httpClient.creerAbonnementPrestataire(neqConnecte, "TYPE_TRAVAUX", typeTravaux);
                    System.out.println("\n " + resultat);
                    System.out.println(" Vous recevrez des notifications pour les problèmes de type " + typeTravaux);
                } catch (Exception e) {
                    System.out.println(" Erreur : " + e.getMessage());
                }
                break;
                
            case 3:
                return;
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * Choisir un type de travaux avec descriptions + option "Autre"
     */
    private String choisirTypeTravaux() {
        System.out.println("\n=== TYPES DE TRAVAUX ===");
        String[] types = {
            "TRAVAUX_ROUTIERS",
            "TRAVAUX_GAZ_ELECTRICITE", 
            "CONSTRUCTION_RENOVATION",
            "ENTRETIEN_PAYSAGER",
            "TRAVAUX_TRANSPORTS_COMMUN",
            "TRAVAUX_SIGNALISATION_ECLAIRAGE",
            "TRAVAUX_SOUTERRAINS",
            "TRAVAUX_RESIDENTIEL",
            "ENTRETIEN_URBAIN",
            "ENTRETIEN_RESEAUX_TELECOM"
        };
        
        String[] descriptions = {
            "Travaux routiers (nids de poule, pavage, etc.)",
            "Travaux de gaz ou électricité",
            "Construction ou rénovation",
            "Entretien paysager (parcs, arbres, etc.)",
            "Travaux liés aux transports en commun",
            "Travaux de signalisation et éclairage (feux, panneaux)",
            "Travaux souterrains (égouts, aqueduc)",
            "Travaux résidentiel",
            "Entretien urbain général",
            "Entretien des réseaux de télécommunication"
        };
        
        for (int i = 0; i < types.length; i++) {
            System.out.println((i + 1) + ". " + descriptions[i]);
        }
        System.out.println("11. Autre (voir types disponibles)");
        
        int choix = saisie.lireEntier("\nChoisir un type (1-11) : ");
        
        if (choix >= 1 && choix <= types.length) {
            System.out.println("Type sélectionné : " + descriptions[choix - 1]);
            return types[choix - 1];
        } else if (choix == 11) {
            return choisirAutreTypePrestataire();
        } else {
            System.out.println("Choix invalide, type par défaut sélectionné");
            return "ENTRETIEN_URBAIN";
        }
    }
    
    /**
     * Récupère et affiche les types "autres" depuis l'API pour prestataires
     */
    private String choisirAutreTypePrestataire() {
        System.out.println("\n=== RÉCUPÉRATION DES AUTRES TYPES DISPONIBLES ===");
        
        try {
            String response = httpClient.consulterProblemes(null, null);
            
            java.util.Set<String> typesStandards = java.util.Set.of(
                "Travaux routiers", "Travaux de gaz ou électricité", "Construction ou rénovation",
                "Entretien paysager", "Travaux liés aux transports en commun", 
                "Travaux de signalisation et éclairage", "Travaux souterrains",
                "Travaux résidentiel", "Entretien urbain", "Entretien des réseaux de télécommunication"
            );
            
            java.util.Set<String> autresTypes = new java.util.TreeSet<>();
            
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.contains("- Type :")) {
                    String type = line.substring(line.indexOf(":") + 1).trim();
                    if (!typesStandards.contains(type) && !type.equals("N/A")) {
                        autresTypes.add(type);
                    }
                }
            }
            
            if (autresTypes.isEmpty()) {
                System.out.println("Aucun autre type trouvé dans les problèmes disponibles.");
                return "ENTRETIEN_URBAIN";
            }
            
            System.out.println("\nAutres types de problèmes disponibles :");
            java.util.List<String> typesList = new java.util.ArrayList<>(autresTypes);
            
            for (int i = 0; i < typesList.size(); i++) {
                System.out.println((i + 1) + ". " + typesList.get(i));
            }
            System.out.println("0. Retour aux types standards");
            
            int choix = saisie.lireEntier("Choisir un type (0-" + typesList.size() + "): ");
            
            if (choix == 0) {
                return choisirTypeTravaux();
            } else if (choix >= 1 && choix <= typesList.size()) {
                String typeChoisi = typesList.get(choix - 1);
                System.out.println("Type sélectionné : " + typeChoisi);
                return typeChoisi;
            } else {
                System.out.println("Choix invalide, type par défaut utilisé.");
                return "ENTRETIEN_URBAIN";
            }
            
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des types : " + e.getMessage());
            System.out.println("Vous pouvez taper le nom exact :");
            return saisie.lireChaineNonVide("Type de travaux : ");
        }
    }
    
    private void pauseAvantContinuer() {
        System.out.println("\nAppuyez sur Entrée pour continuer...");
        saisie.lireChaine("");
    }
}