package ca.udem.maville.ui;

import ca.udem.maville.ui.client.HttpClient;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;

/**
 * Menu STPM pour les agents du Service des Travaux Publics
 * Permet de gérer les priorités et valider les candidatures
 */
public class MenuStpm {
    private HttpClient httpClient;
    private Scanner scanner;
    private ObjectMapper objectMapper;
    
    public MenuStpm(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.scanner = new Scanner(System.in);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    public void afficher() {
        boolean continuer = true;
        
        System.out.println("\n=== Interface Agent STPM ===");
        System.out.println("Bienvenue dans le système de gestion STPM");
        
        while (continuer) {
            afficherOptions();
            String choix = scanner.nextLine();
            
            switch (choix) {
                case "1":
                    consulterCandidatures();
                    break;
                case "2":
                    validerCandiature();
                    break;
                case "3":
                    affecterPriorite();
                    break;
                case "4":
                    consulterProblemes();
                    break;
                case "0":
                    continuer = false;
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
            }
        }
    }
    
    private void afficherOptions() {
        System.out.println("\n=== MENU AGENT STPM ===");
        System.out.println("1. Consulter candidatures en attente");
        System.out.println("2. Valider/Refuser une candidature");
        System.out.println("3. Affecter priorité à un problème");
        System.out.println("4. Consulter tous les problèmes");
        System.out.println("0. Retour menu principal");
        System.out.print("Votre choix : ");
    }
    
    /**
     * Consulter toutes les candidatures en attente de validation
     */
    private void consulterCandidatures() {
        System.out.println("\n=== CANDIDATURES EN ATTENTE ===");
        
        try {
            // Appel API pour récupérer les candidatures
            String response = httpClient.get("/stpm/candidatures");
            
            // Parser le JSON pour un meilleur affichage
            Map<String, Object> data = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> candidatures = (List<Map<String, Object>>) data.get("candidatures");
            Integer total = (Integer) data.get("total");
            
            System.out.println("\nNombre de candidatures en attente : " + total);
            
            if (candidatures != null && !candidatures.isEmpty()) {
                System.out.println("\n=== LISTE DES CANDIDATURES ===");
                
                for (int i = 0; i < candidatures.size(); i++) {
                    Map<String, Object> candidature = candidatures.get(i);
                    
                    System.out.println("\n" + (i + 1) + ". CANDIDATURE #" + candidature.get("id"));
                    System.out.println("   - Prestataire : " + candidature.get("prestataire"));
                    System.out.println("   - Description : " + candidature.get("description"));
                    System.out.println("   - Coût estimé : " + candidature.get("cout") + "$");
                    System.out.println("   - Date de dépôt : " + formatDate(candidature.get("dateDepot")));
                    System.out.println("   - Début prévu : " + candidature.get("dateDebut"));
                    System.out.println("   - Fin prévue : " + candidature.get("dateFin"));
                    
                    // Afficher les détails des problèmes visés
                    if (candidature.containsKey("detailsProblemes")) {
                        List<String> details = (List<String>) candidature.get("detailsProblemes");
                        System.out.println("   - Problèmes visés :");
                        for (String detail : details) {
                            System.out.println("     • " + detail);
                        }
                    }
                }
                
                System.out.println("\nUtilisez l'option 2 pour valider ou refuser une candidature.");
            } else {
                System.out.println("\nAucune candidature en attente de validation.");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la consultation : " + e.getMessage());
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * Valider ou refuser une candidature
     */
    private void validerCandiature() {
        System.out.println("\n=== VALIDATION DE CANDIDATURE ===");
        
        System.out.print("ID de la candidature à traiter : ");
        String candidatureId = scanner.nextLine();
        
        System.out.println("\nQue voulez-vous faire ?");
        System.out.println("1. Accepter la candidature");
        System.out.println("2. Refuser la candidature");
        System.out.print("Votre choix : ");
        String choix = scanner.nextLine();
        
        boolean accepter = choix.equals("1");
        String action = accepter ? "ACCEPTÉE" : "REFUSÉE";
        
        System.out.println("\nConfirmation : Candidature #" + candidatureId + " sera " + action);
        System.out.print("Confirmer ? (oui/non) : ");
        String confirmation = scanner.nextLine();
        
        if (confirmation.toLowerCase().startsWith("o")) {
            try {
                // Appel API pour valider/refuser
                String response = httpClient.validerCandiature(candidatureId, accepter);
                System.out.println("\n" + response);
                
                if (accepter) {
                    System.out.println("Un projet a été créé automatiquement.");
                }
                
            } catch (Exception e) {
                System.err.println("Erreur : " + e.getMessage());
            }
        } else {
            System.out.println("Opération annulée.");
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * Affecter une priorité à un problème
     */
    private void affecterPriorite() {
        System.out.println("\n=== AFFECTER PRIORITÉ À UN PROBLÈME ===");
        
        System.out.print("ID du problème : ");
        String problemeId = scanner.nextLine();
        
        System.out.println("\nNiveaux de priorité disponibles :");
        System.out.println("1. FAIBLE");
        System.out.println("2. MOYENNE");
        System.out.println("3. ELEVEE");
        System.out.print("Choisir priorité (1-3) : ");
        
        String choix = scanner.nextLine();
        String priorite = "";
        
        switch (choix) {
            case "1": priorite = "FAIBLE"; break;
            case "2": priorite = "MOYENNE"; break;
            case "3": priorite = "ELEVEE"; break;
            default:
                System.out.println("Choix invalide.");
                pauseAvantContinuer();
                return;
        }
        
        try {
            // Créer les données à envoyer
            Map<String, Object> data = new HashMap<>();
            data.put("priorite", priorite);
            
            // Appel API pour définir la priorité
            String response = httpClient.put("/stpm/problemes/" + problemeId + "/priorite", data);
            
            System.out.println("\n Priorité mise à jour : " + priorite);
            
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * Consulter tous les problèmes avec leurs priorités
     */
    private void consulterProblemes() {
        System.out.println("\n=== TOUS LES PROBLÈMES SIGNALÉS ===");
        
        try {
            // Utilise le même endpoint que les prestataires
            String response = httpClient.consulterProblemes(null, null);
            System.out.println(response);
            
            System.out.println("\nUtilisez l'option 3 pour modifier la priorité d'un problème.");
            
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * Helper pour formater les dates
     */
    private String formatDate(Object dateObj) {
        if (dateObj == null) return "Non spécifiée";
        
        String dateStr = dateObj.toString();
        // Si c'est un format ISO avec T, simplifier
        if (dateStr.contains("T")) {
            return dateStr.substring(0, 10); // Garde juste YYYY-MM-DD
        }
        return dateStr;
    }
    
    /**
     * Pause avant de continuer
     */
    private void pauseAvantContinuer() {
        System.out.println("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }
}