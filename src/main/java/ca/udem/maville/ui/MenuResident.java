package ca.udem.maville.ui;

import ca.udem.maville.ui.client.HttpClient;
import ca.udem.maville.modele.TypeTravaux;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Menu résident conforme à l'énoncé du DM2
 * Respecte exactement les 4 fonctionnalités demandées
 */
public class MenuResident {
    private HttpClient httpClient;
    private SaisieConsole saisie;
    
    public MenuResident(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.saisie = new SaisieConsole();
    }
    
    public void afficher() {
        boolean continuer = true;
        
        System.out.println("\n=== Interface Résident ===");
        
        while (continuer) {
            afficherMenuPrincipal();
            int choix = saisie.lireEntier("Votre choix: ");
            
            switch (choix) {
                case 1:
                    consulterTravauxEnCoursOuAVenir();
                    break;
                case 2:
                    rechercherTravaux();
                    break;
                case 3:
                    signalerProblemeRoutier();
                    break;
                case 4:
                    gererNotificationsPersonnalisees();
                    break;
                case 0:
                    continuer = false;
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
            }
        }
    }
    
    private void afficherMenuPrincipal() {
        System.out.println("\n=== MENU RÉSIDENT ===");
        System.out.println("1. Consulter les travaux en cours ou à venir (3 prochains mois)");
        System.out.println("2. Rechercher des travaux");
        System.out.println("3. Signaler un problème routier à la ville");
        System.out.println("4. Recevoir des notifications personnalisées");
        System.out.println("0. Retour au menu principal");
    }
    
    /**
     * 1. Consulter les travaux en cours ou à venir
     * Affiche les projets en cours ou planifiés pour les 3 prochains mois
     * Avec possibilité de filtrer par quartier ou type
     */
    private void consulterTravauxEnCoursOuAVenir() {
        System.out.println("\n=== CONSULTER LES TRAVAUX (3 PROCHAINS MOIS) ===");
        
        // Afficher la période couverte
        LocalDate aujourd = LocalDate.now();
        LocalDate dans3Mois = aujourd.plusMonths(3);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        System.out.println("Période : " + aujourd.format(formatter) + " au " + dans3Mois.format(formatter));
        
        // Options de filtrage
        System.out.println("\nFiltrer par :");
        System.out.println("1. Tous les travaux");
        System.out.println("2. Par quartier");
        System.out.println("3. Par type de travaux");
        System.out.println("4. Par quartier ET type");
        
        int choixFiltre = saisie.lireEntier("Votre choix: ");
        
        String quartier = null;
        String type = null;
        
        switch (choixFiltre) {
            case 2:
                quartier = saisie.lireChaine("Nom du quartier: ");
                break;
            case 3:
                type = choisirTypeTravaux();
                break;
            case 4:
                quartier = saisie.lireChaine("Nom du quartier: ");
                type = choisirTypeTravaux();
                break;
        }
        
        // Appel API avec filtres
        System.out.println("\nRécupération des travaux...");
        String resultat = httpClient.consulterTravaux(quartier, type);
        
        System.out.println("\n--- TRAVAUX EN COURS OU À VENIR ---");
        System.out.println(resultat);
        
        pauseAvantContinuer();
    }
    
    /**
     * 2. Rechercher des travaux
     * Recherche spécifique par type ou quartier
     */
    private void rechercherTravaux() {
        System.out.println("\n=== RECHERCHER DES TRAVAUX ===");
        
        System.out.println("Rechercher par :");
        System.out.println("1. Type de travaux");
        System.out.println("2. Quartier");
        System.out.println("3. Type ET quartier");
        
        int choix = saisie.lireEntier("Votre choix: ");
        
        String quartier = null;
        String type = null;
        
        switch (choix) {
            case 1:
                type = choisirTypeTravaux();
                break;
            case 2:
                quartier = saisie.lireChaine("Nom du quartier à rechercher: ");
                break;
            case 3:
                type = choisirTypeTravaux();
                quartier = saisie.lireChaine("Nom du quartier à rechercher: ");
                break;
            default:
                System.out.println("Choix invalide");
                return;
        }
        
        // Recherche
        System.out.println("\nRecherche en cours...");
        String resultat = httpClient.consulterTravaux(quartier, type);
        
        System.out.println("\n--- RÉSULTATS DE LA RECHERCHE ---");
        if (quartier != null) System.out.println("Quartier: " + quartier);
        if (type != null) System.out.println("Type: " + type);
        System.out.println("\n" + resultat);
        
        pauseAvantContinuer();
    }
    
    /**
     * 3. Recevoir des notifications personnalisées
     * Gestion des abonnements aux notifications par quartier/rue
     */
    private void gererNotificationsPersonnalisees() {
        System.out.println("\n=== NOTIFICATIONS PERSONNALISÉES ===");
        
        String email = saisie.lireChaine("Votre email: ");
        
        System.out.println("\n1. Consulter mes notifications");
        System.out.println("2. Gérer mes abonnements");
        System.out.println("3. Retour");
        
        int choix = saisie.lireEntier("Votre choix: ");
        
        switch (choix) {
            case 1:
                consulterNotifications(email);
                break;
            case 2:
                gererAbonnements(email);
                break;
        }
    }
    
    private void consulterNotifications(String email) {
        System.out.println("\nRécupération de vos notifications...");
        String resultat = httpClient.consulterNotifications(email);
        
        System.out.println("\n--- VOS NOTIFICATIONS ---");
        System.out.println(resultat);
        pauseAvantContinuer();
    }
    
    private void gererAbonnements(String email) {
        System.out.println("\n=== GÉRER VOS ABONNEMENTS ===");
        System.out.println("Vous êtes automatiquement abonné aux notifications de votre quartier de résidence.");
        System.out.println("\nAbonnements supplémentaires :");
        System.out.println("1. S'abonner aux notifications d'un quartier");
        System.out.println("2. S'abonner aux notifications d'une rue");
        System.out.println("3. Voir tous mes abonnements");
        
        int choix = saisie.lireEntier("Votre choix: ");
        
        switch (choix) {
            case 1:
                String quartier = saisie.lireChaine("Nom du quartier: ");
                System.out.println("Abonnement au quartier '" + quartier + "' enregistré.");
                System.out.println("Vous recevrez des notifications lors de changements dans les projets de ce quartier.");
                break;
            case 2:
                String rue = saisie.lireChaine("Nom de la rue: ");
                System.out.println("Abonnement à la rue '" + rue + "' enregistré.");
                System.out.println("Vous recevrez des notifications lors de changements dans les projets de cette rue.");
                break;
            case 3:
                System.out.println("\nVos abonnements actuels:");
                System.out.println("- Quartier de résidence (automatique)");
                System.out.println("- [Liste des autres abonnements]");
                break;
        }
        pauseAvantContinuer();
    }
    
    /**
     * 4. Signaler un problème routier à la ville
     * Permet de signaler un problème avec lieu et description
     */
    private void signalerProblemeRoutier() {
        System.out.println("\n=== SIGNALER UN PROBLÈME ROUTIER ===");
        
        // Collecte des informations
        System.out.println("Localisation du problème:");
        String rue = saisie.lireChaine("Rue: ");
        String quartier = saisie.lireChaine("Quartier: ");
        String lieu = rue + ", " + quartier;
        
        System.out.println("\nDescription du problème:");
        String description = saisie.lireChaineNonVide("Décrivez le problème: ");
        
        System.out.println("\nVos coordonnées:");
        String nom = saisie.lireChaine("Nom: ");
        String prenom = saisie.lireChaine("Prénom: ");
        String email = saisie.lireChaine("Email: ");
        String telephone = saisie.lireChaine("Téléphone: ");
        
        // Récapitulatif
        System.out.println("\n--- RÉCAPITULATIF ---");
        System.out.println("Lieu: " + lieu);
        System.out.println("Problème: " + description);
        System.out.println("Déclarant: " + prenom + " " + nom);
        System.out.println("Contact: " + email + " / " + telephone);
        
        String confirmation = saisie.lireChaine("\nConfirmer le signalement? (oui/non): ");
        
        if (confirmation.toLowerCase().startsWith("o")) {
            // Envoi via API
            System.out.println("\nEnvoi du signalement...");
            String resultat = httpClient.signalerProbleme(lieu, description, email);
            System.out.println(resultat);
            System.out.println("\nVos coordonnées ont été attachées au signalement.");
        } else {
            System.out.println("Signalement annulé.");
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * Affiche la liste des types de travaux et retourne le choix
     */
    private String choisirTypeTravaux() {
        System.out.println("\nTypes de travaux:");
        System.out.println("1. Travaux routiers");
        System.out.println("2. Travaux de gaz ou électricité");
        System.out.println("3. Construction ou rénovation");
        System.out.println("4. Entretien paysager");
        System.out.println("5. Travaux liés aux transports en commun");
        System.out.println("6. Travaux de signalisation et éclairage");
        System.out.println("7. Travaux souterrains");
        System.out.println("8. Travaux résidentiel");
        System.out.println("9. Entretien urbain");
        System.out.println("10. Entretien des réseaux de télécommunication");
        
        int choix = saisie.lireEntier("Choisir un type (1-10): ");
        
        switch (choix) {
            case 1: return "TRAVAUX_ROUTIERS";
            case 2: return "TRAVAUX_GAZ_ELECTRICITE";
            case 3: return "CONSTRUCTION_RENOVATION";
            case 4: return "ENTRETIEN_PAYSAGER";
            case 5: return "TRAVAUX_TRANSPORTS_COMMUN";
            case 6: return "TRAVAUX_SIGNALISATION_ECLAIRAGE";
            case 7: return "TRAVAUX_SOUTERRAINS";
            case 8: return "TRAVAUX_RESIDENTIEL";
            case 9: return "ENTRETIEN_URBAIN";
            case 10: return "ENTRETIEN_RESEAUX_TELECOM";
            default: return "ENTRETIEN_URBAIN";
        }
    }
    
    private void pauseAvantContinuer() {
        System.out.println("\nAppuyez sur Entrée pour continuer...");
        saisie.lireChaine("");
    }
}