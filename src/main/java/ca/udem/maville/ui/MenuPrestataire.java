package ca.udem.maville.ui;

import ca.udem.maville.ui.client.HttpClient;

/**
 * Menu prestataire adapté pour l'architecture REST
 * Utilise HttpClient au lieu d'appels directs aux gestionnaires
 */
public class MenuPrestataire {
    // Remplacement des gestionnaires par le client HTTP
    private HttpClient httpClient;
    private SaisieConsole saisie; // AJOUT DE L'ATTRIBUT MANQUANT
    
    // Constructeur adapté pour recevoir HttpClient
    public MenuPrestataire(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.saisie = new SaisieConsole(); // INITIALISATION
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
                    mettreAJourProjet();
                    break;
                case 5:
                    consulterMesProjets();
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
        System.out.println("4. Mettre à jour un projet");
        System.out.println("5. Consulter mes projets");
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
        
        String dateDebut = saisie.lireChaine("Date de début (YYYY-MM-DD) : ");
        String dateFin = saisie.lireChaine("Date de fin (YYYY-MM-DD) : ");
        
        // Gestion du coût avec validation
        double cout = 0;
        try {
            cout = saisie.lireDouble("Coût estimé ($) : ");
        } catch (Exception e) {
            System.out.println("Coût invalide, défini à 0");
            cout = 0;
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
            System.out.println("Soumission de la candidature via API REST...");
            
            // APPEL REST pour soumettre la candidature
            String resultat = httpClient.soumettreCandiature(
                prestataireId, titre, description, typeTravaux, 
                dateDebut, dateFin, cout
            );
            
            // Affichage du résultat
            System.out.println(resultat);
            System.out.println("Votre candidature sera évaluée par le STPM.");
        } else {
            System.out.println("Candidature annulée.");
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * Mettre à jour un projet en cours via API REST
     */
    private void mettreAJourProjet() {
        System.out.println("\n=== METTRE A JOUR UN PROJET ===");
        
        String projetId = saisie.lireChaine("ID du projet à modifier : ");
        
        System.out.println("\nQue voulez-vous modifier? (laissez vide pour ignorer)");
        
        // Collecte des modifications
        String nouveauStatut = saisie.lireChaine("Nouveau statut (EN_COURS/SUSPENDU/TERMINE) : ");
        String nouvelleDescription = saisie.lireChaine("Nouvelle description : ");
        String nouvelleDateFin = saisie.lireChaine("Nouvelle date de fin (YYYY-MM-DD) : ");
        
        // Nettoyage des champs vides
        if (nouveauStatut.trim().isEmpty()) nouveauStatut = null;
        if (nouvelleDescription.trim().isEmpty()) nouvelleDescription = null;
        if (nouvelleDateFin.trim().isEmpty()) nouvelleDateFin = null;
        
        // Vérification qu'au moins un champ est modifié
        if (nouveauStatut == null && nouvelleDescription == null && nouvelleDateFin == null) {
            System.out.println("Aucune modification spécifiée.");
            pauseAvantContinuer();
            return;
        }
        
        // Récapitulatif des modifications
        System.out.println("\n=== MODIFICATIONS À APPLIQUER ===");
        if (nouveauStatut != null) System.out.println("Statut : " + nouveauStatut);
        if (nouvelleDescription != null) System.out.println("Description : " + nouvelleDescription);
        if (nouvelleDateFin != null) System.out.println("Date fin : " + nouvelleDateFin);
        
        System.out.println("\nMise à jour via API REST...");
        
        // APPEL REST pour mettre à jour le projet
        String resultat = httpClient.mettreAJourProjet(
            projetId, nouveauStatut, nouvelleDescription, nouvelleDateFin
        );
        
        // Affichage du résultat
        System.out.println(resultat);
        
        pauseAvantContinuer();
    }
    
    /**
     * Consulter les projets du prestataire
     */
    private void consulterMesProjets() {
        System.out.println("\n=== MES PROJETS ===");
        
        String prestataireId = saisie.lireChaine("Votre ID prestataire (NEQ) : ");
        
        System.out.println("Fonctionnalités disponibles :");
        System.out.println("- Projets en cours");
        System.out.println("- Candidatures en attente");
        System.out.println("- Historique des projets terminés");
        System.out.println("- Statistiques de performance");
        
        System.out.println("\nNote : Cette fonctionnalité sera implémentée");
        System.out.println("avec un endpoint REST /prestataires/" + prestataireId + "/projets");
        
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
    
    /**
     * Démonstration des avantages REST pour les prestataires
     */
    public void demonstrationRestPrestataire() {
        System.out.println("\n=== AVANTAGES REST POUR PRESTATAIRES ===");
        System.out.println("Nouvelles capacités:");
        System.out.println(" Recherche avancée de problèmes par quartier/type");
        System.out.println(" Soumission de candidatures en temps réel");
        System.out.println(" Mise à jour instantanée des projets");
        System.out.println(" Suivi en temps réel des candidatures");
        System.out.println(" Notifications automatiques des validations");
        System.out.println(" API standardisée pour intégrations futures");
        
        pauseAvantContinuer();
    }
}