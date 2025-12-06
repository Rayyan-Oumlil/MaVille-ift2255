package ca.udem.maville.ui;

import ca.udem.maville.ui.client.HttpClient;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * Menu résident complet avec login persistant
 */
public class MenuResident {
    private HttpClient httpClient;
    private SaisieConsole saisie;
    private String emailConnecte; // Email du résident connecté
    
    public MenuResident(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.saisie = new SaisieConsole();
        this.emailConnecte = null;
    }
    
    public void afficher() {
        // LOGIN UNE SEULE FOIS
        if (emailConnecte == null) {
            effectuerLogin();
        }
        
        boolean continuer = true;
        
        System.out.println("\n=== Interface Résident ===");
        System.out.println("Connecté en tant que : " + emailConnecte);
        
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
                case 5:
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
     * Effectue le login du résident
     */
    private void effectuerLogin() {
        System.out.println("\n=== CONNEXION RÉSIDENT ===");
        System.out.println("Résidents disponibles :");
        System.out.println("1. marie.tremblay@email.com (Plateau)");
        System.out.println("2. pierre.gagnon@email.com (Plateau)");
        System.out.println("3. sophie.roy@email.com (Ville-Marie)");
        System.out.println("4. jean.bouchard@email.com (Rosemont)");
        System.out.println("5. marc.lavoie@email.com (Hochelaga)");
        System.out.println("6. Autre email");
        
        int choix = saisie.lireEntier("Choisir un résident (1-6) : ");
        
        switch (choix) {
            case 1:
                emailConnecte = "marie.tremblay@email.com";
                break;
            case 2:
                emailConnecte = "pierre.gagnon@email.com";
                break;
            case 3:
                emailConnecte = "sophie.roy@email.com";
                break;
            case 4:
                emailConnecte = "jean.bouchard@email.com";
                break;
            case 5:
                emailConnecte = "marc.lavoie@email.com";
                break;
            case 6:
                emailConnecte = saisie.lireChaineNonVide("Votre email : ");
                break;
            default:
                emailConnecte = "marie.tremblay@email.com";
        }
        
        System.out.println(" Connecté en tant que : " + emailConnecte);
    }
    
    private void afficherMenuPrincipal() {
        // Compter les notifications non lues
        int notificationsNonLues = compterNotificationsNonLues(emailConnecte);
        
        System.out.println("\n=== MENU RÉSIDENT ===");
        System.out.println("Connecté : " + emailConnecte);
        if (notificationsNonLues > 0) {
            System.out.println(" " + notificationsNonLues + " notification(s) non lue(s)");
        }
        System.out.println("1. Consulter les travaux en cours ou à venir (3 prochains mois)");
        System.out.println("2. Rechercher des travaux");
        System.out.println("3. Signaler un problème routier à la ville");
        System.out.println("4. Voir mes notifications personnalisées (" + notificationsNonLues + " non lues)");
        System.out.println("5. Modifier mes préférences de notification");
        System.out.println("0. Retour au menu principal");
    }

   private int compterNotificationsNonLues(String email) {
    try {
        String response = httpClient.consulterNotifications(email);
        
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
        System.err.println("Erreur comptage notifications résident : " + e.getMessage());
        return 0;
    }
}
    
    /**
     * 1. Consulter les travaux en cours ou à venir
     */
    private void consulterTravauxEnCoursOuAVenir() {
        System.out.println("\n=== CONSULTER LES TRAVAUX (3 PROCHAINS MOIS) ===");
        
        LocalDate aujourd = LocalDate.now();
        LocalDate dans3Mois = aujourd.plusMonths(3);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        System.out.println("Période : " + aujourd.format(formatter) + " au " + dans3Mois.format(formatter));
        
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
        
        System.out.println("\nRécupération des travaux...");
        String resultat = httpClient.consulterTravaux(quartier, type);
        
        System.out.println("\n--- TRAVAUX EN COURS OU À VENIR ---");
        System.out.println(resultat);
        
        pauseAvantContinuer();
    }
    
    /**
     * 2. Rechercher des travaux
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
        
        System.out.println("\nRecherche en cours...");
        String resultat = httpClient.consulterTravaux(quartier, type);
        
        System.out.println("\n--- RÉSULTATS DE LA RECHERCHE ---");
        if (quartier != null) System.out.println("Quartier: " + quartier);
        if (type != null) System.out.println("Type: " + type);
        System.out.println("\n" + resultat);
        
        pauseAvantContinuer();
    }
    
    /**
     * 3. Signaler un problème routier à la ville
     */
    private void signalerProblemeRoutier() {
        System.out.println("\n=== SIGNALER UN PROBLÈME ROUTIER ===");
        
        System.out.println("Localisation du problème:");
        String rue = saisie.lireChaine("Rue: ");
        String quartier = saisie.lireChaine("Quartier: ");
        String lieu = rue + ", " + quartier;
        
        System.out.println("\nDescription du problème:");
        String description = saisie.lireChaineNonVide("Décrivez le problème: ");
        
        System.out.println("\n--- RÉCAPITULATIF ---");
        System.out.println("Lieu: " + lieu);
        System.out.println("Problème: " + description);
        System.out.println("Déclarant: " + emailConnecte);
        
        String confirmation = saisie.lireChaine("\nConfirmer le signalement? (oui/non): ");
        
        if (confirmation.toLowerCase().startsWith("o")) {
            System.out.println("\nEnvoi du signalement...");
            String resultat = httpClient.signalerProbleme(lieu, description, emailConnecte);
            System.out.println(resultat);
            System.out.println("\n Signalement envoyé avec votre email : " + emailConnecte);
            System.out.println(" Vous avez été automatiquement abonné aux notifications du quartier " + quartier);
        } else {
            System.out.println("Signalement annulé.");
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * 4. Voir mes notifications personnalisées
     */
    private void gererNotificationsPersonnalisees() {
        System.out.println("\n=== NOTIFICATIONS PERSONNALISÉES ===");
        
        System.out.println("\n1. Consulter mes notifications");
        System.out.println("2. Gérer mes abonnements");
        System.out.println("3. Retour");
        
        int choix = saisie.lireEntier("Votre choix : ");
        
        switch (choix) {
            case 1:
                consulterNotifications(emailConnecte);
                break;
            case 2:
                gererAbonnements(emailConnecte);
                break;
        }
    }
    
    private void consulterNotifications(String email) {
        System.out.println("\n RÉCUPÉRATION DE VOS NOTIFICATIONS...");
        
        String resultat = httpClient.consulterNotifications(email);
        
        System.out.println("\n--- VOS NOTIFICATIONS ---");
        System.out.println(resultat);
        
        System.out.println("\nOptions :");
        System.out.println("1. Marquer toutes comme lues");
        System.out.println("2. Retour");
        
        int choix = saisie.lireEntier("Votre choix : ");
if (choix == 1) {
    String reponse = httpClient.marquerNotificationsLues(email);
    System.out.println(" " + reponse);
}
        
        pauseAvantContinuer();
    }
    
    private void gererAbonnements(String email) {
        System.out.println("\n=== GÉRER VOS ABONNEMENTS ===");
        
        System.out.println("\n Vos abonnements actuels :");
        String abonnementsActuels = httpClient.consulterAbonnements(email);
        System.out.println(abonnementsActuels);
        
        System.out.println("\n--- NOUVEL ABONNEMENT ---");
        System.out.println("À quoi voulez-vous vous abonner ?");
        System.out.println("1. Un quartier");
        System.out.println("2. Une rue");
        System.out.println("3. Retour");
        
        int choix = saisie.lireEntier("Votre choix : ");
        
        switch (choix) {
            case 1:
                System.out.println("\nQuartiers disponibles : Rosemont, Plateau, Ville-Marie, Hochelaga, etc.");
                String quartier = saisie.lireChaineNonVide("Nom du quartier : ");
                
                String resultatQuartier = httpClient.creerAbonnement(email, "QUARTIER", quartier);
                System.out.println("\n" + resultatQuartier);
                System.out.println(" Vous recevrez des notifications pour tous les projets dans " + quartier);
                break;
                
            case 2:
                System.out.println("\nExemples : Saint-Denis, Sainte-Catherine, Ontario, etc.");
                String rue = saisie.lireChaineNonVide("Nom de la rue : ");
                
                String resultatRue = httpClient.creerAbonnement(email, "RUE", rue);
                System.out.println("\n" + resultatRue);
                System.out.println(" Vous recevrez des notifications pour tous les projets sur " + rue);
                break;
                
            case 3:
                return;
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * 5. Modifier préférences de notification
     */
    private void modifierPreferencesNotification() {
        System.out.println("\n=== MODIFIER MES PRÉFÉRENCES DE NOTIFICATION ===");
        
        System.out.println("Résident connecté : " + emailConnecte);
        
        System.out.println("\nConfigurez vos préférences de notification :");
        
        // Fréquence des notifications
        System.out.println("\n1. Fréquence des notifications :");
        System.out.println("1. Immédiate (dès qu'un changement survient)");
        System.out.println("2. Quotidienne (résumé quotidien)");
        System.out.println("3. Hebdomadaire (résumé hebdomadaire)");
        
        int choixFrequence = saisie.lireEntier("Votre choix : ");
        String frequence = "";
        switch (choixFrequence) {
            case 1: frequence = "IMMEDIATE"; break;
            case 2: frequence = "QUOTIDIEN"; break;
            case 3: frequence = "HEBDOMADAIRE"; break;
            default: frequence = "IMMEDIATE";
        }
        
        // Types de travaux d'intérêt
        System.out.println("\n2. Types de travaux qui vous intéressent :");
        System.out.println("Entrez les numéros séparés par des virgules (ex: 1,3,5)");
        System.out.println("1. Travaux routiers");
        System.out.println("2. Travaux de gaz ou électricité");
        System.out.println("3. Construction ou rénovation");
        System.out.println("4. Entretien paysager");
        System.out.println("5. Travaux de transport en commun");
        System.out.println("6. Travaux de signalisation et éclairage");
        System.out.println("7. Travaux souterrains");
        System.out.println("8. Travaux résidentiel");
        System.out.println("9. Entretien urbain");
        System.out.println("10. Entretien des réseaux de télécommunication");
        
        String choixTypes = saisie.lireChaine("Vos choix (ex: 1,3,5) : ");
        
        // Activer/désactiver les notifications
        System.out.println("\n3. Statut des notifications :");
        boolean actives = saisie.confirmer("Voulez-vous activer les notifications ?");
        
        // Préparer les données
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("frequence", frequence);
        preferences.put("types_travaux", choixTypes);
        preferences.put("actives", actives);
        
        // Envoyer via HTTP
        try {
            httpClient.modifierPreferences(emailConnecte, preferences);
            System.out.println("\n Préférences mises à jour avec succès !");
            System.out.println("- Fréquence : " + frequence);
            System.out.println("- Types d'intérêt : " + choixTypes);
            System.out.println("- Notifications : " + (actives ? "Activées" : "Désactivées"));
        } catch (Exception e) {
            System.out.println(" Erreur lors de la mise à jour des préférences : " + e.getMessage());
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
        System.out.println("11. Autre (voir types disponibles)");
        
        int choix = saisie.lireEntier("Choisir un type (1-11): ");
        
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
            case 11: 
                return choisirAutreType();
            default: return "ENTRETIEN_URBAIN";
        }
    }

    /**
     * Récupère et affiche les types "autres" depuis l'API
     */
    private String choisirAutreType() {
        System.out.println("\n=== RÉCUPÉRATION DES AUTRES TYPES DISPONIBLES ===");
        
        try {
            String response = httpClient.consulterTravaux(null, null);
            
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
                System.out.println("Aucun autre type trouvé. Utilisation du type par défaut.");
                return "ENTRETIEN_URBAIN";
            }
            
            System.out.println("\nAutres types de travaux disponibles :");
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