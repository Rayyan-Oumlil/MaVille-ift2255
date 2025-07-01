package ca.udem.maville.ui;

import ca.udem.maville.ui.client.HttpClient;

/**
 * Menu prestataire adapté pour l'architecture REST
 * Utilise HttpClient au lieu d'appels directs aux gestionnaires
 */
public class MenuPrestataire {
    // Remplacement des gestionnaires par le client HTTP
    private HttpClient httpClient;
    private SaisieConsole saisie;
    
    // Constructeur adapté pour recevoir HttpClient
    public MenuPrestataire(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.saisie = new SaisieConsole();
    }
    
    public void afficher() {
        boolean continuer = true;
        
        System.out.println("\n=== Interface Prestataire - Architecture REST ===");
        
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
                case 0:
                    continuer = false;
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
            }
        }
    }
    
    private void afficherOptions() {
        System.out.println("\n=== MENU PRESTATAIRE ===");
        System.out.println("1. Consulter tous les problèmes disponibles");
        System.out.println("2. Rechercher problèmes (avec filtres)");
        System.out.println("3. Soumettre une candidature");
        System.out.println("4. Mettre à jour mes projets");
        System.out.println("0. Retour menu principal");
    }
    
    /**
     * Consulter tous les problèmes disponibles via API REST
     */
    private void consulterProblemes() {
        System.out.println("\n=== TOUS LES PROBLEMES DISPONIBLES ===");
        
        System.out.println("Récupération des problèmes via API REST...");
        
        // APPEL REST pour récupérer tous les problèmes
        String resultat = httpClient.consulterProblemes(null, null);
        
        // Affichage des résultats
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
        
        // Affichage des critères de recherche
        System.out.println("\n=== RECHERCHE EN COURS ===");
        if (quartier != null && !quartier.trim().isEmpty()) {
            System.out.println("Quartier : " + quartier);
        }
        if (typeTravaux != null && !typeTravaux.trim().isEmpty()) {
            System.out.println("Type de travaux : " + typeTravaux);
        }
        
        System.out.println("\nRécupération des problèmes...");
        
        // APPEL REST avec filtres de recherche
        String resultat = httpClient.consulterProblemes(quartier, typeTravaux);
        
        // Affichage des résultats filtrés
        System.out.println("\n" + resultat);
        
        // Si aucun résultat
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
        
        // Collecte des informations de candidature
        String prestataireId = saisie.lireChaine("Votre ID prestataire (NEQ) : ");
        String titre = saisie.lireChaine("Titre du projet : ");
        String description = saisie.lireChaine("Description du projet : ");
        
        System.out.println("\nType de travaux :");
        String typeTravaux = choisirTypeTravaux();
        
        // AMÉLIORATION : Validation des dates avec exemples clairs
        String dateDebut = "";
        String dateFin = "";
        boolean datesValides = false;
        
        while (!datesValides) {
            System.out.println(" IMPORTANT : Les dates doivent être au format AAAA-MM-JJ");
            System.out.println("Exemple : 2025-07-15 pour le 15 juillet 2025");
            
            dateDebut = saisie.lireChaine("Date de début (AAAA-MM-JJ) : ");
            
            // Vérifier le format de la date de début
            if (!dateDebut.matches("\\d{4}-\\d{2}-\\d{2}")) {
                System.out.println(" Format incorrect ! Utilisez AAAA-MM-JJ (ex: 2025-07-15)");
                continue;
            }
            
            dateFin = saisie.lireChaine("Date de fin (AAAA-MM-JJ) : ");
            
            // Vérifier le format de la date de fin
            if (!dateFin.matches("\\d{4}-\\d{2}-\\d{2}")) {
                System.out.println(" Format incorrect ! Utilisez AAAA-MM-JJ (ex: 2025-07-20)");
                continue;
            }
            
            // Vérifier que la date de fin est après la date de début
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
        
        // Gestion du coût avec validation
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
        
        // Récapitulatif de la candidature
        System.out.println("\n=== RÉCAPITULATIF CANDIDATURE ===");
        System.out.println("Prestataire : " + prestataireId);
        System.out.println("Titre : " + titre);
        System.out.println("Type : " + typeTravaux);
        System.out.println("Période : " + dateDebut + " à " + dateFin);
        System.out.println("Coût : " + cout + "$");
        
        // Confirmation avant envoi
        String confirmation = saisie.lireChaine("\nConfirmer la soumission? (oui/non) : ");
        
        if (confirmation.toLowerCase().startsWith("o")) {
            System.out.println("\nSoumission de la candidature via API REST...");
            
            // APPEL REST pour soumettre la candidature
            String resultat = httpClient.soumettreCandiature(
                prestataireId, titre, description, typeTravaux, 
                dateDebut, dateFin, cout
            );
            
            // Affichage du résultat
            System.out.println("\n=== RÉSULTAT ===");
            System.out.println(resultat);
            
            // Message de confirmation
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
     * Nouvelle méthode pour mettre à jour les projets du prestataire
     */
   // Dans MenuPrestataire.java, remplacez toute la méthode mettreAJourMesProjets :

// Dans MenuPrestataire.java, remplacez toute la méthode mettreAJourMesProjets :

private void mettreAJourMesProjets() {
    System.out.println("\n=== METTRE À JOUR MES PROJETS ===");
    
    // Demander le NEQ du prestataire
    String prestataireId = saisie.lireChaine("Votre numéro d'entreprise (NEQ) : ");
    
    System.out.println("\nRecherche de vos projets en cours...");
    
    // Appeler le nouvel endpoint pour récupérer les projets du prestataire
    String resultats = httpClient.consulterProjetsDuPrestataire(prestataireId);
    
    // Afficher les résultats
    System.out.println(resultats);
    
    // Vérifier s'il y a des projets
    if (resultats.contains("Aucun projet trouvé")) {
        System.out.println(" Conseil : Soumettez d'abord des candidatures (option 3)");
        System.out.println("   et attendez qu'elles soient acceptées par le STPM.");
        pauseAvantContinuer();
        return;
    }
    
    // Parser les projets depuis les résultats pour obtenir les IDs valides
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
    
    // Sélection du projet à modifier
    System.out.println("\n─────────────────────────────────────");
    System.out.println("Actions disponibles :");
    System.out.println("- Entrez l'ID du projet à modifier (IDs disponibles : " + String.join(", ", projectIds) + ")");
    System.out.println("- Tapez 'Q' pour quitter");
    System.out.print("\nVotre choix : ");
    
    String choix = saisie.lireChaine("");
    
    if (choix.equalsIgnoreCase("Q")) {
        return;
    }
    
    // Vérifier que l'ID choisi est valide
    if (!projectIds.contains(choix)) {
        System.out.println(" ID invalide. Ce projet ne vous appartient pas ou n'existe pas.");
        pauseAvantContinuer();
        return;
    }
    
    // RECHARGER les données du projet sélectionné pour avoir l'état actuel
    System.out.println("\n Récupération de l'état actuel du projet #" + choix + "...");
    String etatActuel = httpClient.consulterProjetsDuPrestataire(prestataireId);
    
    // Extraire les infos actuelles du projet sélectionné
    String statutActuel = "INCONNU";
    String descriptionActuelle = "";
    String dateFinActuelle = "";
    
    boolean projetTrouve = false;
    String[] lignes = etatActuel.split("\n");
    for (int i = 0; i < lignes.length; i++) {
        if (lignes[i].contains("PROJET #" + choix)) {
            projetTrouve = true;
            // Chercher les infos dans les lignes suivantes
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
            // Tout modifier
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
        
        // APPEL REST pour mettre à jour le projet AVEC LE NEQ
        String resultat = httpClient.mettreAJourProjet(
            choix, nouveauStatut, nouvelleDescription, nouvelleDateFin, prestataireId
        );
        
        System.out.println(resultat);
        
        if (resultat.contains("succès")) {
            System.out.println(" Projet mis à jour avec succès !");
            
            // Afficher immédiatement le nouvel état
            System.out.println("\n Récupération du nouvel état...");
            String nouvelEtat = httpClient.consulterProjetsDuPrestataire(prestataireId);
            
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
                        break; // Arrêter à la fin du projet
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
     * Choisir un type de travaux avec descriptions
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
        
        int choix = saisie.lireEntier("\nChoisir un type (1-10) : ");
        
        if (choix >= 1 && choix <= types.length) {
            System.out.println("Type sélectionné : " + descriptions[choix - 1]);
            return types[choix - 1];
        } else {
            System.out.println("Choix invalide, type par défaut sélectionné");
            return "ENTRETIEN_URBAIN";
        }
    }
    
    /**
     * Pause avant de continuer - améliore l'expérience utilisateur
     */
    private void pauseAvantContinuer() {
        System.out.println("\nAppuyez sur Entrée pour continuer...");
        saisie.lireChaine("");
    }
}