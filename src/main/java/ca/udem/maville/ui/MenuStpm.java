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
 * Version améliorée avec affichage des listes et sélection par ID
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
                    validerCandidatures();
                    break;
                case "2":
                    affecterPriorites();
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
        System.out.println("1. Valider/Refuser des candidatures");
        System.out.println("2. Affecter des priorités aux problèmes");
        System.out.println("0. Retour menu principal");
        System.out.print("Votre choix : ");
    }
    
    /**
     * Nouvelle méthode pour valider les candidatures avec affichage de liste
     */
    private void validerCandidatures() {
        System.out.println("\n=== VALIDATION DES CANDIDATURES ===");
        
        try {
            // 1. Récupérer et afficher toutes les candidatures
            String response = httpClient.get("/stpm/candidatures");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(response, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidatures = (List<Map<String, Object>>) data.get("candidatures");
            Integer total = (Integer) data.get("total");
            
            if (candidatures == null || candidatures.isEmpty()) {
                System.out.println(" Aucune candidature en attente de validation.");
                pauseAvantContinuer();
                return;
            }
            
            // 2. Afficher la liste des candidatures
            System.out.println(" " + total + " candidature(s) en attente de validation :\n");
            
            for (Map<String, Object> candidature : candidatures) {
                System.out.println("════════════════════════════════════════");
                System.out.println("ID: #" + candidature.get("id"));
                System.out.println("Prestataire: " + candidature.get("prestataire"));
                System.out.println("Description: " + candidature.get("description"));
                System.out.println("Coût: " + candidature.get("cout") + "$");
                System.out.println("Période: du " + candidature.get("dateDebut") + " au " + candidature.get("dateFin"));
                
                // Afficher les problèmes visés
                if (candidature.containsKey("detailsProblemes")) {
                    @SuppressWarnings("unchecked")
                    List<String> details = (List<String>) candidature.get("detailsProblemes");
                    System.out.println("Problèmes visés:");
                    for (String detail : details) {
                        System.out.println("  • " + detail);
                    }
                }
                System.out.println("════════════════════════════════════════");
            }
            
            // 3. Traitement en boucle
            boolean continuerValidation = true;
            
            while (continuerValidation) {
                System.out.println("\n─────────────────────────────────────");
                System.out.println("Actions disponibles:");
                System.out.println("- Entrez l'ID d'une candidature pour la traiter");
                System.out.println("- Tapez 'Q' pour quitter");
                System.out.print("\nVotre choix: ");
                
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("Q")) {
                    continuerValidation = false;
                    continue;
                }
                
                // Vérifier si c'est un ID valide
                try {
                    int candidatureId = Integer.parseInt(input);
                    
                    // Vérifier que l'ID existe dans la liste
                    boolean idExiste = candidatures.stream()
                        .anyMatch(c -> c.get("id").toString().equals(input));
                    
                    if (!idExiste) {
                        System.out.println(" ID invalide. Veuillez réessayer.");
                        continue;
                    }
                    
                    // 4. Traiter la candidature sélectionnée
                    System.out.println("\nTraitement de la candidature #" + candidatureId);
                    System.out.println("1 ACCEPTER la candidature");
                    System.out.println("2.  REFUSER la candidature");
                    System.out.println("3.   Annuler");
                    System.out.print("Votre décision: ");
                    
                    String decision = scanner.nextLine();
                    
                    switch (decision) {
                        case "1":
                            // Accepter
                            String resultAccept = httpClient.validerCandiature(String.valueOf(candidatureId), true);
                            System.out.println(" " + resultAccept);
                            System.out.println("Un projet a été créé automatiquement.");
                            
                            // Retirer de la liste locale pour ne plus l'afficher
                            candidatures.removeIf(c -> c.get("id").toString().equals(input));
                            break;
                            
                        case "2":
                            // Refuser
                            System.out.print("Motif du refus (optionnel): ");
                            String motif = scanner.nextLine();
                            
                            String resultRefuse = httpClient.validerCandiature(String.valueOf(candidatureId), false);
                            System.out.println("\n " + resultRefuse);
                            if (!motif.trim().isEmpty()) {
                                System.out.println("Motif enregistré: " + motif);
                            }
                            
                            // Retirer de la liste locale
                            candidatures.removeIf(c -> c.get("id").toString().equals(input));
                            break;
                            
                        case "3":
                            System.out.println("Opération annulée.");
                            break;
                            
                        default:
                            System.out.println("Choix invalide.");
                    }
                    
                    // Si toutes les candidatures ont été traitées
                    if (candidatures.isEmpty()) {
                        System.out.println(" Toutes les candidatures ont été traitées!");
                        continuerValidation = false;
                    }
                    
                } catch (NumberFormatException e) {
                    System.out.println(" Veuillez entrer un ID valide ou 'Q' pour quitter.");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * Nouvelle méthode pour affecter les priorités avec affichage de liste
     */
    private void affecterPriorites() {
        System.out.println("\n=== AFFECTATION DES PRIORITÉS ===");
        
        boolean continuerPriorites = true;
        
        while (continuerPriorites) {
            try {
                // 1. Récupérer et afficher tous les problèmes ACTUELS
                System.out.println(" Chargement des problèmes...");
                String response = httpClient.consulterProblemes(null, null);
                
                // Afficher la liste des problèmes
                System.out.println(response);
                
                // Parser les IDs disponibles depuis la réponse
                java.util.Set<String> idsDisponibles = new java.util.HashSet<>();
                String[] lines = response.split("\n");
                for (String line : lines) {
                    if (line.matches("\\d+\\. Problème #\\d+.*")) {
                        String id = line.substring(line.indexOf("#") + 1);
                        if (id.contains(" ")) {
                            id = id.substring(0, id.indexOf(" "));
                        }
                        idsDisponibles.add(id);
                    }
                }
                
                if (idsDisponibles.isEmpty()) {
                    System.out.println(" Aucun problème à traiter.");
                    pauseAvantContinuer();
                    return;
                }
                
                // 2. Menu d'actions
                System.out.println("\n─────────────────────────────────────");
                System.out.println("Actions disponibles:");
                System.out.println("- Entrez l'ID d'un problème pour modifier sa priorité");
                System.out.println("  (IDs disponibles : " + String.join(", ", idsDisponibles) + ")");
                System.out.println("- Tapez 'Q' pour quitter");
                System.out.print("\nVotre choix: ");
                
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("Q")) {
                    continuerPriorites = false;
                    continue;
                }
                
                // Vérifier que l'ID existe
                if (!idsDisponibles.contains(input)) {
                    System.out.println(" ID invalide. Veuillez choisir parmi : " + String.join(", ", idsDisponibles));
                    continue;
                }
                
                try {
                    int problemeId = Integer.parseInt(input);
                    
                    // 3. Extraire la priorité actuelle
                    String prioriteActuelle = "INCONNUE";
                    for (String line : lines) {
                        if (line.contains("Problème #" + problemeId + " ")) {
                            // Chercher la priorité dans les lignes suivantes
                            for (int i = 0; i < lines.length; i++) {
                                if (lines[i].contains("Problème #" + problemeId)) {
                                    for (int j = i; j < Math.min(i + 10, lines.length); j++) {
                                        if (lines[j].contains("- Priorité :")) {
                                            prioriteActuelle = lines[j].substring(lines[j].indexOf(":") + 1).trim();
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    
                    // 4. Afficher le menu des priorités
                    System.out.println("\nModification de la priorité du problème #" + problemeId);
                    System.out.println("Priorité actuelle : " + prioriteActuelle);
                    System.out.println("\nNouvelles priorités disponibles:");
                    System.out.println("1.  FAIBLE");
                    System.out.println("2.  MOYENNE");
                    System.out.println("3.  ÉLEVÉE");
                    System.out.println("4.   Annuler");
                    System.out.print("Nouvelle priorité: ");
                    
                    String choixPriorite = scanner.nextLine();
                    String priorite = "";
                    
                    switch (choixPriorite) {
                        case "1":
                            priorite = "FAIBLE";
                            break;
                        case "2":
                            priorite = "MOYENNE";
                            break;
                        case "3":
                            priorite = "ELEVEE";
                            break;
                        case "4":
                            System.out.println("Opération annulée.");
                            continue;
                        default:
                            System.out.println("Choix invalide.");
                            continue;
                    }
                    
                    // 5. Envoyer la mise à jour
                    Map<String, Object> data = new HashMap<>();
                    data.put("priorite", priorite);
                    
                    httpClient.put("/stpm/problemes/" + problemeId + "/priorite", data);
                    
                    String not = priorite.equals("FAIBLE") ? "FB" : 
                                  priorite.equals("MOYENNE") ? "MY" : "EL";
                    System.out.println(" Priorité mise à jour : " + not + " " + priorite);
                    
                    // 6. IMPORTANT : Recharger immédiatement pour voir les changements
                    System.out.println(" Rechargement des données...");
                    Thread.sleep(500); // Petite pause pour laisser le temps au serveur
                    
                    String nouveauResponse = httpClient.consulterProblemes(null, null);
                    
                    // Extraire et afficher le problème modifié
                    System.out.println(" NOUVEL ÉTAT DU PROBLÈME #" + problemeId);
                    System.out.println("════════════════════════════════════");
                    String[] newLines = nouveauResponse.split("\n");
                    boolean found = false;
                    for (int i = 0; i < newLines.length; i++) {
                        if (newLines[i].contains("Problème #" + problemeId + " ")) {
                            // Afficher cette ligne et les 6 suivantes
                            for (int j = i; j < Math.min(i + 7, newLines.length); j++) {
                                if (newLines[j].trim().isEmpty()) break;
                                if (j > i && newLines[j].matches("\\d+\\..*")) break;
                                System.out.println(newLines[j]);
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println("Problème non trouvé dans la liste mise à jour");
                    }
                    System.out.println("════════════════════════════════════");
                    
                    // Proposer de continuer
                    System.out.print("\nModifier un autre problème? (O/N): ");
                    String continuer = scanner.nextLine();
                    if (!continuer.toLowerCase().startsWith("o")) {
                        continuerPriorites = false;
                    }
                    
                } catch (NumberFormatException e) {
                    System.out.println(" Veuillez entrer un ID valide ou 'Q' pour quitter.");
                } catch (Exception e) {
                    System.err.println(" Erreur : " + e.getMessage());
                }
            
            } catch (Exception e) {
                System.err.println("Erreur : " + e.getMessage());
                pauseAvantContinuer();
                return;
            }
        }
        
        pauseAvantContinuer();
    }
    
    /**
     * Pause avant de continuer
     */
    private void pauseAvantContinuer() {
        System.out.println("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }
}