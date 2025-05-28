package ca.udem.maville.ui;

import ca.udem.maville.service.*;

/*
    Menu principal de l'application MaVille
    Point d'entrée qui permet de choisir entre profil Résident ou Prestataire
 */
public class MenuPrincipal {
    // Outils pour saisie et affichage
    private SaisieConsole saisie;
    private AffichageConsole affichage;
    
    // Services métier pour gérer les données
    private GestionnaireProblemes gestionnaireProblemes;
    private GestionnaireProjets gestionnaireProjets;

    /*
      Constructeur - reçoit les gestionnaires nécessaires au fonctionnement
     */
    public MenuPrincipal(GestionnaireProblemes gestionnaireProblemes, GestionnaireProjets gestionnaireProjets) {
        this.saisie = new SaisieConsole();
        this.affichage = new AffichageConsole();
        this.gestionnaireProblemes = gestionnaireProblemes;
        this.gestionnaireProjets = gestionnaireProjets;
    }

    /*
        Méthode principale qui démarre l'application
        Boucle infinie jusqu'à ce que l'utilisateur choisisse de quitter
     */
    public void demarrer() {
        affichage.afficherTitre("Bienvenue dans MaVille");
        
        while (true) {
            // Demande à l'utilisateur son type de profil
            TypeUtilisateur typeUtilisateur = choisirTypeUtilisateur();
            
            // Si null, c'est qu'il veut quitter
            if (typeUtilisateur == null) {
                break; // Sort de la boucle = quitter l'application
            }

            // Lance le bon menu selon le type d'utilisateur
            switch (typeUtilisateur) {
                case RESIDENT:
                    // Crée et affiche le menu résident
                    MenuResident menuResident = new MenuResident(gestionnaireProblemes, gestionnaireProjets);
                    menuResident.afficher();
                    break;
                case PRESTATAIRE:
                    // Crée et affiche le menu prestataire
                    MenuPrestataire menuPrestataire = new MenuPrestataire(gestionnaireProblemes, gestionnaireProjets);
                    menuPrestataire.afficher();
                    break;
            }
        }
        
        // Message de sortie
        affichage.afficherMessage("Au revoir!");
    }

    /*
        Affiche le menu de sélection du type d'utilisateur et lit le choix
     */
    private TypeUtilisateur choisirTypeUtilisateur() {
        affichage.afficherSousTitre("Choisissez votre profil");
        
        // Options disponibles
        String[] options = {
            "Résident",
            "Prestataire de services",
            "Quitter"
        };
        
        affichage.afficherMenu(options);
        int choix = saisie.lireEntier("Votre choix: ");
        
        // Traite le choix de l'utilisateur
        switch (choix) {
            case 1: return TypeUtilisateur.RESIDENT;
            case 2: return TypeUtilisateur.PRESTATAIRE;
            case 3: return null; // null = quitter
            default:
                // Choix invalide, on recommence
                affichage.afficherErreur("Choix invalide");
                return choisirTypeUtilisateur(); 
        }
    }

    /*
        Énumération des types d'utilisateurs possibles
        Simplifie la gestion des profils dans l'application
     */
    private enum TypeUtilisateur {
        RESIDENT,      
        PRESTATAIRE    
    }
}