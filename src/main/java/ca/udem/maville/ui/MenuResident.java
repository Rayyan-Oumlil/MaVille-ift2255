package ca.udem.maville.ui;

import ca.udem.maville.modele.*;
import ca.udem.maville.service.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/*
    Menu spécialisé pour les résidents de Montréal
    Permet de signaler des problèmes, consulter ses signalements et voir les travaux en cours
 */
public class MenuResident {
    // Outils d'interface utilisateur
    private SaisieConsole saisie;
    private AffichageConsole affichage;
    
    // Services métier
    private GestionnaireProblemes gestionnaireProblemes;
    private GestionnaireProjets gestionnaireProjets;

    /*
        Constructeur - initialise le menu avec les services nécessaires
     */
    public MenuResident(GestionnaireProblemes gestionnaireProblemes, GestionnaireProjets gestionnaireProjets) {
        this.saisie = new SaisieConsole();
        this.affichage = new AffichageConsole();
        this.gestionnaireProblemes = gestionnaireProblemes;
        this.gestionnaireProjets = gestionnaireProjets;
    }

    /*
        Affiche le menu principal du résident et gère ses choix
        Boucle jusqu'à ce que l'utilisateur choisisse de retourner au menu principal
     */
    public void afficher() {
        boolean continuer = true;
        
        while (continuer) {
            affichage.afficherTitre("Menu Résident");
            
            // Options disponibles pour un résident
            String[] options = {
                "Signaler un problème",
                "Consulter mes signalements",
                "Consulter les travaux en cours"
            };
            
            affichage.afficherMenu(options);
            int choix = saisie.lireEntier("Votre choix: ");
            
            // Traite le choix de l'utilisateur
            switch (choix) {
                case 1:
                    signalerProbleme();
                    break;
                case 2:
                    consulterMesSignalements();
                    break;
                case 3:
                    consulterTravauxEnCours();
                    break;
                case 0:
                    continuer = false; // Sort de la boucle = retour menu principal
                    break;
                default:
                    affichage.afficherErreur("Choix invalide");
            }
            
            // Pause avant de réafficher le menu (sauf si on quitte)
            if (continuer) {
                saisie.attendreEntree();
            }
        }
    }

    /*
        Permet à un résident de signaler un nouveau problème
        Collecte toutes les informations requises selon le cahier des charges
     */
    private void signalerProbleme() {
        affichage.afficherSousTitre("Signaler un nouveau problème");
        
        
        affichage.afficherMessage("Veuillez fournir les informations suivantes :");
        affichage.afficherMessage("");
        
        // LIEU - avec choix d'arrondissement
        String lieuPrecis = saisie.lireChaineNonVide("Lieu précis du problème (rue, intersection, etc.) : ");
        
        // Liste des vrais arrondissements de Montréal
        affichage.afficherMessage("\nDans quel arrondissement se situe ce problème ?");
        String[] arrondissements = {
            "Ahuntsic-Cartierville",
            "Anjou", 
            "Côte-des-Neiges–Notre-Dame-de-Grâce",
            "Lachine",
            "LaSalle",
            "Le Plateau-Mont-Royal",
            "Le Sud-Ouest",
            "L'Île-Bizard–Sainte-Geneviève",
            "Mercier–Hochelaga-Maisonneuve",
            "Montréal-Nord",
            "Outremont",
            "Pierrefonds-Roxboro",
            "Rivière-des-Prairies–Pointe-aux-Trembles",
            "Rosemont–La Petite-Patrie",
            "Saint-Laurent",
            "Saint-Léonard",
            "Verdun",
            "Ville-Marie",
            "Villeray–Saint-Michel–Parc-Extension"
        };
        
        // Affiche les arrondissements numérotés
        for (int i = 0; i < arrondissements.length; i++) {
            System.out.println((i + 1) + ". " + arrondissements[i]);
        }
        
        int choixArrondissement = saisie.lireEntier("Choisissez l'arrondissement (1-" + arrondissements.length + ") : ");
        
        // Validation du choix d'arrondissement
        String arrondissement;
        if (choixArrondissement >= 1 && choixArrondissement <= arrondissements.length) {
            arrondissement = arrondissements[choixArrondissement - 1];
        } else {
            affichage.afficherErreur("Choix invalide.");
            return; // Sort de la méthode, annule le signalement
        }
        
        // Combine lieu précis + arrondissement pour le lieu complet
        String lieuComplet = lieuPrecis + ", " + arrondissement;
        
        //  TYPE DE PROBLÈME
        TypeTravaux typeProbleme = saisie.choisirTypeTravaux();
        
        // COORDONNÉES DU RÉSIDENT (collectées au moment du signalement)
        affichage.afficherSousTitre("Vos coordonnées (pour le suivi du dossier)");
        String nom = saisie.lireChaineNonVide("Nom de famille : ");
        String prenom = saisie.lireChaineNonVide("Prénom : ");
        String telephone = saisie.lireChaineNonVide("Numéro de téléphone : ");  
        String email = saisie.lireChaineNonVide("Adresse courriel : ");
        String adresse = saisie.lireChaineNonVide("Votre adresse : ");
        
        // Crée l'objet Resident avec les coordonnées
        Resident resident = new Resident(nom, prenom, telephone, email, adresse);
        
        // DESCRIPTION DU PROBLÈME
        String description = saisie.lireChaineNonVide("Brève description du problème : ");
        
        // RÉCAPITULATIF - permet au résident de vérifier avant de confirmer
        affichage.afficherSousTitre("Récapitulatif de votre signalement");
        affichage.afficherMessage("Lieu complet : " + lieuComplet);
        affichage.afficherMessage("Arrondissement : " + arrondissement);
        affichage.afficherMessage("Type de problème : " + typeProbleme.getDescription());
        affichage.afficherMessage("Description : " + description);
        affichage.afficherMessage("Déclarant : " + resident.getNomComplet());
        affichage.afficherMessage("Téléphone : " + resident.getTelephone());
        affichage.afficherMessage("Email : " + resident.getEmail());
        affichage.afficherMessage("Adresse : " + resident.getAdresse());
        
        // Demande confirmation avant d'enregistrer
        if (saisie.confirmer("Confirmer le signalement ?")) {
            // Enregistre le problème via le gestionnaire
            Probleme probleme = gestionnaireProblemes.signalerProbleme(lieuComplet, typeProbleme, description, resident);
            
            // Confirmation de l'enregistrement avec infos utiles
            affichage.afficherSucces("Problème signalé avec succès !");
            affichage.afficherMessage("Numéro de référence : " + probleme.getId());
            
            // Affichage lisible de la date/heure
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
            affichage.afficherMessage("Date de signalement : " + probleme.getDateSignalement().format(formatter));
            
            affichage.afficherMessage("");
            // Messages informatifs sur la suite du processus
            affichage.afficherMessage("Un agent du STPM examinera votre signalement et lui assignera une priorité.");
            affichage.afficherMessage("Le problème est maintenant référencé dans l'arrondissement " + arrondissement + ".");
            affichage.afficherMessage("Conservez le numéro de référence pour le suivi de votre dossier.");
            affichage.afficherMessage("Vous serez contacté si des informations supplémentaires sont nécessaires.");
        } else {
            affichage.afficherMessage("Signalement annulé.");
        }
    }

    /*
        Permet à un résident de consulter tous ses signalements passés
        Utilise l'email comme identifiant 
     */
    private void consulterMesSignalements() {
        affichage.afficherSousTitre("Consulter mes signalements");
        
        // Identification simple par email
        String email = saisie.lireChaineNonVide("Entrez votre adresse courriel : ");
        
        // Filtre tous les problèmes pour ne garder que ceux de cet email
        List<Probleme> mesProblemes = gestionnaireProblemes.listerProblemes().stream()
                .filter(p -> p.getDeclarant().getEmail().equalsIgnoreCase(email)) // Ignore la casse
                .collect(Collectors.toList());
        
        if (mesProblemes.isEmpty()) {
            affichage.afficherMessage("Aucun signalement trouvé pour cette adresse courriel.");
            affichage.afficherMessage("Vérifiez l'adresse saisie ou utilisez l'option 1 pour signaler un problème.");
        } else {
            affichage.afficherMessage("Signalements trouvés pour " + email + " :");
            affichage.afficherMessage("");
            
            // Affiche chaque problème avec son statut
            for (Probleme probleme : mesProblemes) {
                affichage.afficherSousTitre("Signalement #" + probleme.getId());
                affichage.afficherProbleme(probleme);
                
                // Informe sur le statut de traitement
                if (probleme.isResolu()) {
                    affichage.afficherSucces("-- Ce problème a été traité et résolu.");
                } else {
                    affichage.afficherMessage("-- Ce problème est en cours de traitement");
                    affichage.afficherMessage("   Priorité assignée : " + probleme.getPriorite().getDescription());
                }
                affichage.afficherMessage(""); // Ligne vide pour la lisibilité
            }
        }
    }

    /*
        Affiche les travaux en cours dans un arrondissement choisi par le résident
        Permet aux citoyens de s'informer sur les perturbations à venir
     */
    private void consulterTravauxEnCours() {
        affichage.afficherSousTitre("Travaux en cours dans votre secteur");
        
        // Même liste d'arrondissements que pour le signalement
        affichage.afficherMessage("Arrondissements de Montréal :");
        String[] arrondissements = {
            "Ahuntsic-Cartierville",
            "Anjou", 
            "Côte-des-Neiges–Notre-Dame-de-Grâce",
            "Lachine",
            "LaSalle",
            "Le Plateau-Mont-Royal",
            "Le Sud-Ouest",
            "L'Île-Bizard–Sainte-Geneviève",
            "Mercier–Hochelaga-Maisonneuve",
            "Montréal-Nord",
            "Outremont",
            "Pierrefonds-Roxboro",
            "Rivière-des-Prairies–Pointe-aux-Trembles",
            "Rosemont–La Petite-Patrie",
            "Saint-Laurent",
            "Saint-Léonard",
            "Verdun",
            "Ville-Marie",
            "Villeray–Saint-Michel–Parc-Extension"
        };
        
        for (int i = 0; i < arrondissements.length; i++) {
            System.out.println((i + 1) + ". " + arrondissements[i]);
        }
        
        int choix = saisie.lireEntier("Choisissez votre arrondissement (1-" + arrondissements.length + ") : ");
        
        // Validation du choix
        String arrondissement;
        if (choix >= 1 && choix <= arrondissements.length) {
            arrondissement = arrondissements[choix - 1];
        } else {
            affichage.afficherErreur("Choix invalide.");
            return;
        }
        
        affichage.afficherMessage("Recherche des travaux en cours dans : " + arrondissement);
        affichage.afficherMessage("");
        
        // Récupère tous les projets actifs
        List<Projet> projetsEnCours = gestionnaireProjets.listerProjetsActifs();
        List<Projet> projetsDansArrondissement = new ArrayList<>();
        
        // Filtre les projets pour ne garder que ceux de l'arrondissement demandé
        String arrondissementLower = arrondissement.toLowerCase();
        for (Projet projet : projetsEnCours) {
            String localisationProjet = projet.getLocalisation().toLowerCase();
            
            // Vérifie si le projet est dans l'arrondissement
            if (localisationProjet.contains(arrondissementLower) || 
                correspondArrondissement(localisationProjet, arrondissementLower)) {
                projetsDansArrondissement.add(projet);
            }
        }
        
        affichage.afficherMessage("=== TRAVAUX EN COURS - " + arrondissement.toUpperCase() + " ===");
        
        if (projetsDansArrondissement.isEmpty()) {
            // Aucun projet trouvé
            affichage.afficherMessage("Aucun projet de travaux en cours trouvé dans cet arrondissement.");
            affichage.afficherMessage("");
            affichage.afficherMessage("Cela signifie qu'actuellement :");
            affichage.afficherMessage("--> Aucun projet n'a été approuvé pour cet arrondissement");
            affichage.afficherMessage("--> Ou les projets approuvés ne sont pas encore démarrés");
            affichage.afficherMessage("--> Ou les travaux sont terminés");
        } else {
            // Affiche tous les projets trouvés avec détails utiles pour les résidents
            affichage.afficherMessage(projetsDansArrondissement.size() + " projet(s) en cours dans cet arrondissement :");
            affichage.afficherMessage("");
            
            for (Projet projet : projetsDansArrondissement) {
                affichage.afficherMessage("--> " + projet.getTypeTravail().getDescription());
                affichage.afficherMessage("   Localisation : " + projet.getLocalisation());
                affichage.afficherMessage("   Statut : " + projet.getStatut().getDescription());
                affichage.afficherMessage("   Prestataire : " + projet.getPrestataire().getNomEntreprise());
                affichage.afficherMessage("   Priorité : " + projet.getPriorite().getDescription());
                
                // Dates importantes pour les résidents
                if (projet.getDateDebutReelle() != null) {
                    affichage.afficherMessage("   Débuté le : " + projet.getDateDebutReelle().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
                affichage.afficherMessage("   Fin prévue : " + projet.getDateFinPrevue().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                
                // Alerte si travaux suspendus
                if (projet.getStatut() == StatutProjet.SUSPENDU) {
                    affichage.afficherMessage("   !  TRAVAUX TEMPORAIREMENT SUSPENDUS");
                }
                
                affichage.afficherMessage("   Description : " + projet.getDescriptionProjet());
                affichage.afficherMessage("");
            }
        }
        
        // Messages informatifs
        affichage.afficherMessage("-- Cette information est basée sur les projets actuellement");
        affichage.afficherMessage("   approuvés et gérés par le système MaVille. -- ");
        affichage.afficherMessage("");
    }
    
    /*
        Méthode utilitaire pour améliorer la correspondance arrondissement/localisation
        Gère les cas où les noms ne correspondent pas exactement
     */
    private boolean correspondArrondissement(String localisation, String arrondissement) {
        // Correspondance directe 
        if (localisation.contains(arrondissement.toLowerCase())) {
            return true;
        }
        
        // Correspondances spéciales pour certains arrondissements
        
        // Le Plateau-Mont-Royal a plusieurs variations de noms
        if (arrondissement.contains("plateau") && 
            (localisation.contains("mont-royal") || localisation.contains("saint-laurent"))) {
            return true;
        }
        
        // Ville-Marie inclut le centre-ville et le Vieux-Montréal
        if (arrondissement.contains("ville-marie") && 
            (localisation.contains("centre-ville") || localisation.contains("vieux-montréal") ||
             localisation.contains("sainte-catherine") || localisation.contains("saint-denis"))) {
            return true;
        }
        
        // Rosemont-La Petite-Patrie
        if (arrondissement.contains("rosemont") && 
            (localisation.contains("beaubien") || localisation.contains("rosemont"))) {
            return true;
        }
        
        // Le Sud-Ouest inclut plusieurs quartiers
        if (arrondissement.contains("sud-ouest") && 
            (localisation.contains("verdun") || localisation.contains("pointe-saint-charles"))) {
            return true;
        }
        
        // Côte-des-Neiges–Notre-Dame-de-Grâce
        if (arrondissement.contains("côte-des-neiges") && 
            (localisation.contains("côte-des-neiges") || localisation.contains("notre-dame-de-grâce"))) {
            return true;
        }
        
        // Villeray–Saint-Michel–Parc-Extension
        if (arrondissement.contains("villeray") && 
            (localisation.contains("villeray") || localisation.contains("saint-michel") || 
             localisation.contains("parc-extension"))) {
            return true;
        }
        
        return false; // Aucune correspondance trouvée
    }
}