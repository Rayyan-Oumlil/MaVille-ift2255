package ca.udem.maville.storage;

import ca.udem.maville.modele.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stockage JSON simple pour MaVille
 * Garde vos données entre les sessions
 */
public class JsonStorage {
    private static final String DATA_DIR = "data";
    private static final String PROBLEMES_FILE = DATA_DIR + "/problemes.json";
    private static final String PROJETS_FILE = DATA_DIR + "/projets.json";
    private static final String RESIDENTS_FILE = DATA_DIR + "/residents.json";
    private static final String PRESTATAIRES_FILE = DATA_DIR + "/prestataires.json";
    private static final String CANDIDATURES_FILE = DATA_DIR + "/candidatures.json";
    
    private static final String ABONNEMENTS_PRESTATAIRES_FILE = DATA_DIR + "/abonnements_prestataires.json";
    private static final String PREFERENCES_NOTIFICATIONS_FILE = DATA_DIR + "/preferences_notifications.json";


    private final ObjectMapper mapper;
    
    public JsonStorage() {
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        createDataDirectory();
    }
    
    private void createDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    // === PROBLÈMES ===
    public void saveProblemes(List<Probleme> problemes) {
        try {
            mapper.writeValue(new File(PROBLEMES_FILE), problemes);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde problèmes: " + e.getMessage());
        }
    }
    
    public List<Probleme> loadProblemes() {
        try {
            File file = new File(PROBLEMES_FILE);
            if (file.exists()) {
                Probleme[] array = mapper.readValue(file, Probleme[].class);
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement problèmes: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    // === PROJETS ===
    public void saveProjets(List<Projet> projets) {
        try {
            mapper.writeValue(new File(PROJETS_FILE), projets);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde projets: " + e.getMessage());
        }
    }
    
    public List<Projet> loadProjets() {
        try {
            File file = new File(PROJETS_FILE);
            if (file.exists()) {
                Projet[] array = mapper.readValue(file, Projet[].class);
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement projets: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    // === RÉSIDENTS ===
    public void saveResidents(List<Resident> residents) {
        try {
            mapper.writeValue(new File(RESIDENTS_FILE), residents);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde résidents: " + e.getMessage());
        }
    }
    
    public List<Resident> loadResidents() {
        try {
            File file = new File(RESIDENTS_FILE);
            if (file.exists()) {
                Resident[] array = mapper.readValue(file, Resident[].class);
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement résidents: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    // === PRESTATAIRES ===
    public void savePrestataires(List<Prestataire> prestataires) {
        try {
            mapper.writeValue(new File(PRESTATAIRES_FILE), prestataires);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde prestataires: " + e.getMessage());
        }
    }
    
    public List<Prestataire> loadPrestataires() {
        try {
            File file = new File(PRESTATAIRES_FILE);
            if (file.exists()) {
                Prestataire[] array = mapper.readValue(file, Prestataire[].class);
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement prestataires: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    // === CANDIDATURES ===
    public void saveCandidatures(List<Candidature> candidatures) {
        try {
            mapper.writeValue(new File(CANDIDATURES_FILE), candidatures);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde candidatures: " + e.getMessage());
        }
    }
    


public List<Candidature> loadCandidatures() {
    try {
        File file = new File(CANDIDATURES_FILE);
        if (file.exists()) {
            Candidature[] array = mapper.readValue(file, Candidature[].class);
            List<Candidature> candidatures = new ArrayList<>(Arrays.asList(array));
            
            // IMPORTANT : Synchroniser le compteur d'ID pour éviter les doublons
            Candidature.synchroniserCompteurId(candidatures);
            
            return candidatures;
        }
    } catch (IOException e) {
        System.err.println("Erreur chargement candidatures: " + e.getMessage());
    }
    return new ArrayList<>();
}
    
    // === MÉTHODES UTILITAIRES ===
public void initializeWithSampleData() {
    // Données existantes
    if (loadResidents().isEmpty()) {
        List<Resident> sampleResidents = createSampleResidents();
        saveResidents(sampleResidents);
        System.out.println(sampleResidents.size() + " résidents créés");
    }
    
    if (loadPrestataires().isEmpty()) {
        List<Prestataire> samplePrestataires = createSamplePrestataires();
        savePrestataires(samplePrestataires);
        System.out.println(samplePrestataires.size() + " prestataires créés");
    }
    
    if (loadProblemes().isEmpty()) {
        List<Probleme> sampleProblemes = createSampleProblemes();
        saveProblemes(sampleProblemes);
        System.out.println(sampleProblemes.size() + " problèmes créés");
    }
    
    if (loadCandidatures().isEmpty()) {
        List<Candidature> sampleCandidatures = createSampleCandidatures();
        saveCandidatures(sampleCandidatures);
        System.out.println(sampleCandidatures.size() + " candidatures créées");
    }
    
    // NOUVEAU : Initialiser les projets
    initializeWithSampleProjects();
    
    // NOUVEAU : Initialiser les abonnements et notifications
    initializeWithSampleAbonnements();
    initializeWithSampleAbonnementsPrestataires();
    initializeWithSampleNotifications();
    // FORCER la recréation des notifications
File notifFile = new File("data/notifications.json");
if (notifFile.exists()) {
    notifFile.delete();
    System.out.println("Ancien fichier notifications supprimé");
}

List<Notification> notifications = createSampleNotifications();
saveNotifications(notifications);
System.out.println("NOUVELLES notifications créées : " + notifications.size());
}
    
        private List<Resident> createSampleResidents() {
    List<Resident> residents = new ArrayList<>();
    
    // 5 résidents dont 2 dans le même quartier (Plateau)
    residents.add(new Resident("Tremblay", "Marie", "514-555-0001", 
        "marie.tremblay@email.com", "123 Rue Saint-Denis, Plateau"));
    
    residents.add(new Resident("Gagnon", "Pierre", "514-555-0002", 
        "pierre.gagnon@email.com", "456 Avenue du Parc, Plateau")); // 2e dans Plateau
    
    residents.add(new Resident("Roy", "Sophie", "514-555-0003", 
        "sophie.roy@email.com", "789 Boulevard René-Lévesque, Ville-Marie"));
    
    residents.add(new Resident("Bouchard", "Jean", "514-555-0004", 
        "jean.bouchard@email.com", "321 Rue Sainte-Catherine, Rosemont"));
    
    residents.add(new Resident("Lavoie", "Marc", "514-555-0005", 
        "marc.lavoie@email.com", "654 Rue Ontario, Hochelaga-Maisonneuve"));
    
    return residents;
}
    
    private List<Prestataire> createSamplePrestataires() {
    List<Prestataire> prestataires = new ArrayList<>();
    
    // 5 prestataires
    prestataires.add(new Prestataire("NEQ1234567890", "Construction ABC Inc.", 
        "Robert Dubois", "514-666-0001", "contact@constructionabc.com"));
    
    prestataires.add(new Prestataire("NEQ0987654321", "Pavage Pro Ltée", 
        "Martine Lavoie", "514-666-0002", "info@pavagepro.com"));
    
    prestataires.add(new Prestataire("NEQ1122334455", "Électricité Montréal", 
        "Claude Martin", "514-666-0003", "service@elecmtl.com"));
    
    prestataires.add(new Prestataire("NEQ5544332211", "Paysagement Vert", 
        "Lucie Bergeron", "514-666-0004", "contact@paysvert.com"));
    
    prestataires.add(new Prestataire("NEQ9988776655", "TechnoVert Solutions", 
        "Alex Dupuis", "514-666-0005", "alex@technoverte.com"));
    
    return prestataires;
}
    
private List<Probleme> createSampleProblemes() {
    List<Probleme> problemes = new ArrayList<>();
    
    List<Resident> residents = loadResidents();
    if (residents.isEmpty()) {
        residents = createSampleResidents();
        saveResidents(residents);
    }
    
    // 5 problèmes dont au moins 2 avec priorité affectée
    
    // Problème 1 : PRIORITÉ ÉLEVÉE
    Probleme p1 = new Probleme(
        "Boulevard Rosemont coin 10e Avenue, Rosemont",
        TypeTravaux.TRAVAUX_ROUTIERS,
        "Gros nid de poule dangereux pour les cyclistes près de l'école",
        residents.get(0)
    );
    p1.setPriorite(Priorite.ELEVEE); // PRIORITÉ AFFECTÉE
    problemes.add(p1);
    
    // Problème 2 : PRIORITÉ MOYENNE  
    Probleme p2 = new Probleme(
        "Parc Lafontaine entrée Sherbrooke, Plateau Mont-Royal",
        TypeTravaux.ENTRETIEN_PAYSAGER,
        "Branches d'arbres tombées après la tempête bloquant l'allée principale",
        residents.get(1)
    );
    p2.setPriorite(Priorite.MOYENNE); // PRIORITÉ AFFECTÉE
    problemes.add(p2);
    
    // Problème 3 : Pas encore de priorité
    Probleme p3 = new Probleme(
        "Coin René-Lévesque et Saint-Laurent, Ville-Marie",
        TypeTravaux.TRAVAUX_SIGNALISATION_ECLAIRAGE,
        "Feu de circulation défectueux - reste toujours rouge direction ouest",
        residents.get(2)
    );
    // Garde priorité par défaut (MOYENNE)
    problemes.add(p3);
    
    // Problème 4 : Pas encore de priorité
    Probleme p4 = new Probleme(
        "Rue Ontario Est près du métro Pie-IX, Hochelaga-Maisonneuve",
        TypeTravaux.TRAVAUX_SOUTERRAINS,
        "Fuite d'eau importante créant une flaque sur le trottoir",
        residents.get(3)
    );
    problemes.add(p4);
    
    // Problème 5 : Nouveau problème
    Probleme p5 = new Probleme(
        "Avenue du Parc près du Métro, Plateau Mont-Royal",
        TypeTravaux.ENTRETIEN_URBAIN,
        "Lampadaire défectueux et banc public cassé",
        residents.get(4)
    );
    problemes.add(p5);
    
    return problemes;
}
    
    private List<Candidature> createSampleCandidatures() {
    List<Candidature> candidatures = new ArrayList<>();
    
    List<Prestataire> prestataires = loadPrestataires();
    if (prestataires.isEmpty()) {
        prestataires = createSamplePrestataires();
        savePrestataires(prestataires);
    }

    // 5 candidatures
    
    // Candidature 1 : Pavage Pro pour le nid de poule
    Candidature c1 = new Candidature(
        prestataires.get(1), // Pavage Pro
        Arrays.asList(1), // Problème ID 1
        "Réparation urgente de la chaussée",
        15000.0,
        LocalDate.now().plusDays(7),
        LocalDate.now().plusDays(10)
    );
    candidatures.add(c1);
    
    // Candidature 2 : Électricité Montréal pour le feu défectueux
    Candidature c2 = new Candidature(
        prestataires.get(2), // Électricité Montréal
        Arrays.asList(3), // Problème ID 3
        "Réparation et modernisation du système",
        8500.0,
        LocalDate.now().plusDays(3),
        LocalDate.now().plusDays(4)
    );
    candidatures.add(c2);
    
    // Candidature 3 : Paysagement Vert pour l'entretien
    Candidature c3 = new Candidature(
        prestataires.get(3), // Paysagement Vert
        Arrays.asList(2), // Problème ID 2
        "Nettoyage et aménagement paysager",
        3200.0,
        LocalDate.now().plusDays(2),
        LocalDate.now().plusDays(3)
    );
    candidatures.add(c3);
    
    // Candidature 4 : TechnoVert pour travaux souterrains
    Candidature c4 = new Candidature(
        prestataires.get(4), // TechnoVert Solutions
        Arrays.asList(4), // Problème ID 4
        "Réparation urgente de la fuite d'eau",
        12000.0,
        LocalDate.now().plusDays(1),
        LocalDate.now().plusDays(6)
    );
    candidatures.add(c4);
    
    // Candidature 5 : Construction ABC pour entretien urbain
    Candidature c5 = new Candidature(
        prestataires.get(0), // Construction ABC Inc.
        Arrays.asList(5), // Problème ID 5
        "Remplacement du lampadaire et réparation du banc",
        4500.0,
        LocalDate.now().plusDays(5),
        LocalDate.now().plusDays(8)
    );
    candidatures.add(c5);
    
    return candidatures;
}
// NOUVEAU : Créer 5 projets avec au moins 1 en cours et 1 terminé
public void initializeWithSampleProjects() {
    if (loadProjets().isEmpty()) {
        List<Projet> projets = createSampleProjets();
        saveProjets(projets);
        System.out.println(projets.size() + " projets créés");
    }
}

private List<Projet> createSampleProjets() {
    List<Projet> projets = new ArrayList<>();
    
    List<Candidature> candidatures = loadCandidatures();
    List<Probleme> problemes = loadProblemes();
    List<Prestataire> prestataires = loadPrestataires();
    
    if (candidatures.isEmpty()) {
        candidatures = createSampleCandidatures();
        saveCandidatures(candidatures);
    }
    
    if (problemes.isEmpty()) {
        problemes = createSampleProblemes();
        saveProblemes(problemes);
    }
    
    if (prestataires.isEmpty()) {
        prestataires = createSamplePrestataires();
        savePrestataires(prestataires);
    }
    
    // CRÉER EXACTEMENT 5 PROJETS AVEC LES STATUTS REQUIS
    
    // Projet 1 : EN_COURS (OBLIGATOIRE selon spécifications)
    if (candidatures.size() > 0 && prestataires.size() > 0) {
        Candidature c1 = candidatures.get(0);
        List<Probleme> problemesP1 = new ArrayList<>();
        if (!problemes.isEmpty()) problemesP1.add(problemes.get(0));
        
        Projet p1 = new Projet(c1, problemesP1);
        p1.setStatut(StatutProjet.EN_COURS); // ✅ EN COURS
        p1.setDateDebutReelle(LocalDate.now().minusDays(5));
        p1.setLocalisation("Boulevard Rosemont, Rosemont");
        p1.setPrestataire(prestataires.get(1)); // Pavage Pro
        projets.add(p1);
    }
    
    // Projet 2 : TERMINÉ (OBLIGATOIRE selon spécifications)
    if (candidatures.size() > 1 && prestataires.size() > 1) {
        Candidature c2 = candidatures.get(1);
        List<Probleme> problemesP2 = new ArrayList<>();
        if (problemes.size() > 1) problemesP2.add(problemes.get(1));
        
        Projet p2 = new Projet(c2, problemesP2);
        p2.setStatut(StatutProjet.TERMINE); // ✅ TERMINÉ
        p2.setDateDebutReelle(LocalDate.now().minusDays(20));
        p2.setDateFinReelle(LocalDate.now().minusDays(2));
        p2.setLocalisation("Parc Lafontaine, Plateau Mont-Royal");
        p2.setPrestataire(prestataires.get(2)); // Électricité Montréal
        projets.add(p2);
    }
    
    // Projet 3 : APPROUVÉ (ACCEPTÉ selon spécifications)
    if (candidatures.size() > 2 && prestataires.size() > 2) {
        Candidature c3 = candidatures.get(2);
        List<Probleme> problemesP3 = new ArrayList<>();
        if (problemes.size() > 2) problemesP3.add(problemes.get(2));
        
        Projet p3 = new Projet(c3, problemesP3);
        p3.setStatut(StatutProjet.APPROUVE); // ✅ ACCEPTÉ/APPROUVÉ
        p3.setLocalisation("René-Lévesque et Saint-Laurent, Ville-Marie");
        p3.setPrestataire(prestataires.get(2)); // Électricité Montréal
        projets.add(p3);
    }
    
    // Projet 4 : SUSPENDU
    if (candidatures.size() > 3 && prestataires.size() > 3) {
        Candidature c4 = candidatures.get(3);
        List<Probleme> problemesP4 = new ArrayList<>();
        if (problemes.size() > 3) problemesP4.add(problemes.get(3));
        
        Projet p4 = new Projet(c4, problemesP4);
        p4.setStatut(StatutProjet.SUSPENDU);
        p4.setDateDebutReelle(LocalDate.now().minusDays(10));
        p4.setLocalisation("Rue Ontario Est, Hochelaga-Maisonneuve");
        p4.setPrestataire(prestataires.get(4)); // TechnoVert
        projets.add(p4);
    }
    
    // Projet 5 : EN_ATTENTE
    if (candidatures.size() > 4 && prestataires.size() > 4) {
        Candidature c5 = candidatures.get(4);
        List<Probleme> problemesP5 = new ArrayList<>();
        if (problemes.size() > 4) problemesP5.add(problemes.get(4));
        
        Projet p5 = new Projet(c5, problemesP5);
        p5.setStatut(StatutProjet.EN_ATTENTE);
        p5.setLocalisation("Avenue du Parc, Plateau Mont-Royal");
        p5.setPrestataire(prestataires.get(0)); // Construction ABC
        projets.add(p5);
    }
    
    System.out.println(" 5 projets créés :");
    System.out.println("- Projet #1 : EN_COURS");
    System.out.println("- Projet #2 : TERMINÉ");
    System.out.println("- Projet #3 : APPROUVÉ (accepté)");
    System.out.println("- Projet #4 : SUSPENDU");
    System.out.println("- Projet #5 : EN_ATTENTE");
    
    return projets;
}
    
    public void clearAllData() {
        File[] files = {
            new File(PROBLEMES_FILE),
            new File(PROJETS_FILE), 
            new File(RESIDENTS_FILE),
            new File(PRESTATAIRES_FILE),
            new File(CANDIDATURES_FILE)
        };
        
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }
    // Ajoutez ces méthodes dans JsonStorage.java :

// Constantes pour les nouveaux fichiers
private static final String NOTIFICATIONS_FILE = DATA_DIR + "/notifications.json";
private static final String ABONNEMENTS_FILE = DATA_DIR + "/abonnements.json";

// === NOTIFICATIONS ===
public void saveNotifications(List<Notification> notifications) {
    try {
        mapper.writeValue(new File(NOTIFICATIONS_FILE), notifications);
    } catch (IOException e) {
        System.err.println("Erreur sauvegarde notifications: " + e.getMessage());
    }
}
public List<Notification> loadNotifications() {
    try {
        File file = new File(NOTIFICATIONS_FILE);
        if (file.exists()) {
            // Lire le JSON comme texte brut
            String jsonContent = java.nio.file.Files.readString(file.toPath());
            
            // Parser manuellement pour corriger les données
            List<Notification> notifications = new ArrayList<>();
            
            // Utiliser Jackson pour parser le JSON de base
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(jsonContent);
            
            for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
                Notification notif = new Notification();
                
                // Récupérer les champs manuellement
                String destinataire = node.get("destinataire").asText();
                String typeDestinataire = node.get("typeDestinataire").asText();
                String message = node.get("message").asText();
                String typeChangement = node.get("typeChangement").asText();
                boolean lu = node.get("lu").asBoolean();
                
                // Définir les valeurs
                notif.setDestinataire(destinataire);
                notif.setTypeDestinataire(typeDestinataire);
                notif.setMessage(message);
                notif.setTypeChangement(typeChangement);
                notif.setLu(lu);
                
                // Gérer les champs optionnels
                if (node.has("quartier") && !node.get("quartier").isNull()) {
                    notif.setQuartier(node.get("quartier").asText());
                }
                if (node.has("priorite") && !node.get("priorite").isNull()) {
                    notif.setPriorite(node.get("priorite").asText());
                }
                if (node.has("projetId")) {
                    notif.setProjetId(node.get("projetId").asInt());
                }
                if (node.has("problemeId")) {
                    notif.setProblemeId(node.get("problemeId").asInt());
                }
                
                // Gérer la date (format array)
                if (node.has("dateCreation")) {
                    com.fasterxml.jackson.databind.JsonNode dateNode = node.get("dateCreation");
                    if (dateNode.isArray() && dateNode.size() >= 6) {
                        int year = dateNode.get(0).asInt();
                        int month = dateNode.get(1).asInt();
                        int day = dateNode.get(2).asInt();
                        int hour = dateNode.get(3).asInt();
                        int minute = dateNode.get(4).asInt();
                        int second = dateNode.get(5).asInt();
                        notif.setDateCreation(LocalDateTime.of(year, month, day, hour, minute, second));
                    }
                } else {
                    notif.setDateCreation(LocalDateTime.now());
                }
                
                // Pour compatibilité avec l'ancien format
                if ("RESIDENT".equals(typeDestinataire)) {
                    notif.setResidentEmail(destinataire);
                }
                
                notifications.add(notif);
                
            }
            
            return notifications;
        }
    } catch (Exception e) {
        System.err.println("Erreur chargement notifications: " + e.getMessage());
        e.printStackTrace();
    }
    return new ArrayList<>();
}

// === ABONNEMENTS ===
public void saveAbonnements(List<Abonnement> abonnements) {
    try {
        mapper.writeValue(new File(ABONNEMENTS_FILE), abonnements);
    } catch (IOException e) {
        System.err.println("Erreur sauvegarde abonnements: " + e.getMessage());
    }
}

public List<Abonnement> loadAbonnements() {
    try {
        File file = new File(ABONNEMENTS_FILE);
        if (file.exists()) {
            Abonnement[] array = mapper.readValue(file, Abonnement[].class);
            return new ArrayList<>(Arrays.asList(array));
        }
    } catch (IOException e) {
        System.err.println("Erreur chargement abonnements: " + e.getMessage());
    }
    return new ArrayList<>();
}

public void saveAbonnementsPrestataires(List<AbonnementPrestataire> abonnements) {
    try {
        mapper.writeValue(new File(ABONNEMENTS_PRESTATAIRES_FILE), abonnements);
    } catch (IOException e) {
        System.err.println("Erreur sauvegarde abonnements prestataires: " + e.getMessage());
    }
}

public List<AbonnementPrestataire> loadAbonnementsPrestataires() {
    try {
        File file = new File(ABONNEMENTS_PRESTATAIRES_FILE);
        if (file.exists()) {
            AbonnementPrestataire[] array = mapper.readValue(file, AbonnementPrestataire[].class);
            return new ArrayList<>(Arrays.asList(array));
        }
    } catch (IOException e) {
        System.err.println("Erreur chargement abonnements prestataires: " + e.getMessage());
    }
    return new ArrayList<>();
}

// === PRÉFÉRENCES NOTIFICATIONS ===
public void savePreferencesNotification(String email, Map<String, Object> preferences) {
    try {
        // Charger les préférences existantes
        Map<String, Map<String, Object>> toutesPreferences = loadToutesPreferencesNotification();
        
        // Mettre à jour pour cet email
        toutesPreferences.put(email, preferences);
        
        // Sauvegarder
        mapper.writeValue(new File(PREFERENCES_NOTIFICATIONS_FILE), toutesPreferences);
    } catch (IOException e) {
        System.err.println("Erreur sauvegarde préférences: " + e.getMessage());
    }
}

// Méthode pour créer notification prestataire
public void creerNotificationPrestataire(String prestataireNeq, String message, 
                                        String typeChangement, int problemeOuProjetId, 
                                        String quartier, String priorite) {
    List<Notification> notifications = loadNotifications();
    
    Notification nouvelle = new Notification();
    nouvelle.setDestinataire(prestataireNeq);
    nouvelle.setTypeDestinataire("PRESTATAIRE");
    nouvelle.setMessage(message);
    nouvelle.setTypeChangement(typeChangement);
    nouvelle.setQuartier(quartier);
    nouvelle.setPriorite(priorite);
    nouvelle.setDateCreation(LocalDateTime.now());
    nouvelle.setLu(false);
    
    // Définir le bon ID selon le type
    if (typeChangement.contains("PROBLEME") || typeChangement.contains("PRIORITE")) {
        nouvelle.setProblemeId(problemeOuProjetId);
    } else {
        nouvelle.setProjetId(problemeOuProjetId);
    }
    
    notifications.add(nouvelle);
    saveNotifications(notifications);
    
    System.out.println(" Notification prestataire créée pour " + prestataireNeq + " : " + message);
}

// Méthode pour créer notification STPM
public void creerNotificationStmp(String message, String typeChangement, 
                                 int projetOuProblemeId, String quartier) {
    List<Notification> notifications = loadNotifications();
    
    Notification nouvelle = new Notification();
    nouvelle.setDestinataire("STPM");
    nouvelle.setTypeDestinataire("STPM");
    nouvelle.setMessage(message);
    nouvelle.setTypeChangement(typeChangement);
    nouvelle.setQuartier(quartier);
    nouvelle.setDateCreation(LocalDateTime.now());
    nouvelle.setLu(false);
    
    // Définir le bon ID selon le type
    if (typeChangement.contains("PROBLEME")) {
        nouvelle.setProblemeId(projetOuProblemeId);
    } else {
        nouvelle.setProjetId(projetOuProblemeId);
    }
    
    notifications.add(nouvelle);
    saveNotifications(notifications);
    
    System.out.println(" Notification STPM créée : " + message);
}


private void initializeWithSampleAbonnements() {
    if (loadAbonnements().isEmpty()) {
        List<Abonnement> abonnements = createSampleAbonnements();
        saveAbonnements(abonnements);
        System.out.println(abonnements.size() + " abonnements résidents créés");
    }
}

private List<Abonnement> createSampleAbonnements() {
    List<Abonnement> abonnements = new ArrayList<>();
    
    // Résident 1 : Abonné au quartier Plateau
    abonnements.add(new Abonnement("marie.tremblay@email.com", "QUARTIER", "Plateau"));
    
    // Résident 2 : Abonné au quartier Plateau ET à une rue
    abonnements.add(new Abonnement("pierre.gagnon@email.com", "QUARTIER", "Plateau"));
    abonnements.add(new Abonnement("pierre.gagnon@email.com", "RUE", "Saint-Denis"));
    
    // Résident 3 : Abonné seulement à une rue
    abonnements.add(new Abonnement("sophie.roy@email.com", "RUE", "René-Lévesque"));
    
    // Résident 4 : Abonné au quartier Rosemont
    abonnements.add(new Abonnement("jean.bouchard@email.com", "QUARTIER", "Rosemont"));
    
    // Résident 5 : Abonné au quartier Hochelaga
    abonnements.add(new Abonnement("marc.lavoie@email.com", "QUARTIER", "Hochelaga-Maisonneuve"));
    
    return abonnements;
}

public Map<String, Object> loadPreferencesNotification(String email) {
    Map<String, Map<String, Object>> toutesPreferences = loadToutesPreferencesNotification();
    return toutesPreferences.getOrDefault(email, createDefaultPreferences());
}

private Map<String, Map<String, Object>> loadToutesPreferencesNotification() {
    try {
        File file = new File(PREFERENCES_NOTIFICATIONS_FILE);
        if (file.exists()) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> prefs = mapper.readValue(file, Map.class);
            return prefs;
        }
    } catch (IOException e) {
        System.err.println("Erreur chargement préférences: " + e.getMessage());
    }
    return new HashMap<>();
}

private void initializeWithSampleAbonnementsPrestataires() {
    if (loadAbonnementsPrestataires().isEmpty()) {
        List<AbonnementPrestataire> abonnements = createSampleAbonnementsPrestataires();
        saveAbonnementsPrestataires(abonnements);
        System.out.println(abonnements.size() + " abonnements prestataires créés");
    }
}

private List<AbonnementPrestataire> createSampleAbonnementsPrestataires() {
    List<AbonnementPrestataire> abonnements = new ArrayList<>();
    
    // Prestataire 1 : Construction ABC - Abonné aux travaux routiers
    abonnements.add(new AbonnementPrestataire("NEQ1234567890", "TYPE_TRAVAUX", "TRAVAUX_ROUTIERS"));
    
    // Prestataire 2 : Pavage Pro - Abonné au quartier Plateau ET aux travaux routiers
    abonnements.add(new AbonnementPrestataire("NEQ0987654321", "QUARTIER", "Plateau"));
    abonnements.add(new AbonnementPrestataire("NEQ0987654321", "TYPE_TRAVAUX", "TRAVAUX_ROUTIERS"));
    
    // Prestataire 3 : Électricité Montréal - Abonné aux travaux électriques
    abonnements.add(new AbonnementPrestataire("NEQ1122334455", "TYPE_TRAVAUX", "TRAVAUX_GAZ_ELECTRICITE"));
    
    // Prestataire 4 : Paysagement Vert - Abonné au quartier Rosemont
    abonnements.add(new AbonnementPrestataire("NEQ5544332211", "QUARTIER", "Rosemont"));
    
    // Prestataire 5 : TechnoVert - Abonné aux travaux souterrains
    abonnements.add(new AbonnementPrestataire("NEQ9988776655", "TYPE_TRAVAUX", "TRAVAUX_SOUTERRAINS"));
    
    return abonnements;
}

private void initializeWithSampleNotifications() {
    if (loadNotifications().isEmpty()) {
        List<Notification> notifications = createSampleNotifications();
        saveNotifications(notifications);
        System.out.println(notifications.size() + " notifications créées");
    }
}

private List<Notification> createSampleNotifications() {
    List<Notification> notifications = new ArrayList<>();
    
    // NOTIFICATIONS RÉSIDENTS (comme avant - elles marchent)
    Notification n1 = new Notification("marie.tremblay@email.com", 
        "Nouveau projet approuvé dans le Plateau : Réfection de la rue Saint-Denis", 
        "NOUVEAU_PROJET", 1, "Plateau");
    n1.setLu(true);
    n1.setDateCreation(LocalDateTime.now().minusDays(3));
    notifications.add(n1);
    
    notifications.add(new Notification("marie.tremblay@email.com", 
        "Le projet de réparation sur Saint-Denis est maintenant en cours", 
        "STATUT_CHANGE", 1, "Plateau"));
    
    notifications.add(new Notification("pierre.gagnon@email.com", 
        "Priorité élevée affectée au problème sur votre rue", 
        "PRIORITE_CHANGE", 2, "Plateau"));
    
    Notification n2 = new Notification("sophie.roy@email.com", 
        "Travaux terminés sur René-Lévesque", 
        "STATUT_CHANGE", 3, "Ville-Marie");
    n2.setLu(true);
    n2.setDateCreation(LocalDateTime.now().minusDays(5));
    notifications.add(n2);
    
    // NOTIFICATIONS PRESTATAIRES - CRÉATION CORRECTE BASÉE SUR L'ÉNONCÉ
        
// Construction ABC (NEQ1234567890) - Abonné aux travaux routiers
Notification np1 = new Notification();
np1.setDestinataire("NEQ1234567890");
np1.setTypeDestinataire("PRESTATAIRE");
np1.setMessage("Priorité ÉLEVÉE affectée au problème #1 (Travaux routiers à Boulevard Rosemont, Rosemont)");
np1.setTypeChangement("PRIORITE_AFFECTEE");
np1.setProblemeId(1);
np1.setQuartier("Rosemont");
np1.setPriorite("ELEVEE");
np1.setDateCreation(LocalDateTime.now().minusDays(1));
np1.setLu(false);
notifications.add(np1);

Notification np2 = new Notification();
np2.setDestinataire("NEQ1234567890");
np2.setTypeDestinataire("PRESTATAIRE");
np2.setMessage("Nouveau problème #5 signalé (Travaux routiers dans votre zone)");
np2.setTypeChangement("NOUVEAU_PROBLEME");
np2.setProblemeId(5);
np2.setQuartier("Plateau");
np2.setPriorite("MOYENNE");
np2.setDateCreation(LocalDateTime.now().minusHours(6));
np2.setLu(false);
notifications.add(np2);

// Pavage Pro (NEQ0987654321) - Abonné au Plateau ET aux travaux routiers
Notification np3 = new Notification();
np3.setDestinataire("NEQ0987654321");
np3.setTypeDestinataire("PRESTATAIRE");
np3.setMessage("Priorité MOYENNE affectée au problème #2 dans le Plateau (votre quartier d'abonnement)");
np3.setTypeChangement("PRIORITE_AFFECTEE");
np3.setProblemeId(2);
np3.setQuartier("Plateau");
np3.setPriorite("MOYENNE");
np3.setDateCreation(LocalDateTime.now().minusDays(2));
np3.setLu(false);
notifications.add(np3);

Notification np4 = new Notification();
np4.setDestinataire("NEQ0987654321");
np4.setTypeDestinataire("PRESTATAIRE");
np4.setMessage("Votre candidature #1 a été acceptée - Projet créé avec succès");
np4.setTypeChangement("CANDIDATURE_ACCEPTEE");
np4.setProblemeId(1);
np4.setQuartier("Rosemont");
np4.setDateCreation(LocalDateTime.now().minusDays(3));
np4.setLu(true);
notifications.add(np4);

// Électricité Montréal (NEQ1122334455) - Abonné aux travaux électriques
Notification np5 = new Notification();
np5.setDestinataire("NEQ1122334455");
np5.setTypeDestinataire("PRESTATAIRE");
np5.setMessage("Priorité ÉLEVÉE affectée au problème #3 (Travaux de signalisation - votre spécialité)");
np5.setTypeChangement("PRIORITE_AFFECTEE");
np5.setProblemeId(3);
np5.setQuartier("Ville-Marie");
np5.setPriorite("ELEVEE");
np5.setDateCreation(LocalDateTime.now().minusDays(1));
np5.setLu(false);
notifications.add(np5);

Notification np6 = new Notification();
np6.setDestinataire("NEQ1122334455");
np6.setTypeDestinataire("PRESTATAIRE");
np6.setMessage("Nouveau problème électrique signalé dans Ville-Marie");
np6.setTypeChangement("NOUVEAU_PROBLEME");
np6.setProblemeId(3);
np6.setQuartier("Ville-Marie");
np6.setPriorite("MOYENNE");
np6.setDateCreation(LocalDateTime.now().minusHours(8));
np6.setLu(false);
notifications.add(np6);

// Paysagement Vert (NEQ5544332211) - Abonné au quartier Rosemont
Notification np7 = new Notification();
np7.setDestinataire("NEQ5544332211");
np7.setTypeDestinataire("PRESTATAIRE");
np7.setMessage("Priorité ÉLEVÉE affectée dans Rosemont (votre quartier d'abonnement)");
np7.setTypeChangement("PRIORITE_AFFECTEE");
np7.setProblemeId(1);
np7.setQuartier("Rosemont");
np7.setPriorite("ELEVEE");
np7.setDateCreation(LocalDateTime.now().minusDays(1));
np7.setLu(false);
notifications.add(np7);

Notification np8 = new Notification();
np8.setDestinataire("NEQ5544332211");
np8.setTypeDestinataire("PRESTATAIRE");
np8.setMessage("Nouveau problème d'entretien paysager dans votre zone");
np8.setTypeChangement("NOUVEAU_PROBLEME");
np8.setProblemeId(2);
np8.setQuartier("Plateau");
np8.setPriorite("MOYENNE");
np8.setDateCreation(LocalDateTime.now().minusDays(2));
np8.setLu(true);
notifications.add(np8);

// TechnoVert (NEQ9988776655) - Abonné aux travaux souterrains
Notification np9 = new Notification();
np9.setDestinataire("NEQ9988776655");
np9.setTypeDestinataire("PRESTATAIRE");
np9.setMessage("Priorité ÉLEVÉE affectée au problème #4 (Travaux souterrains - votre spécialité)");
np9.setTypeChangement("PRIORITE_AFFECTEE");
np9.setProblemeId(4);
np9.setQuartier("Hochelaga");
np9.setPriorite("ELEVEE");
np9.setDateCreation(LocalDateTime.now().minusDays(1));
np9.setLu(false);
notifications.add(np9);

Notification np10 = new Notification();
np10.setDestinataire("NEQ9988776655");
np10.setTypeDestinataire("PRESTATAIRE");
np10.setMessage("Votre candidature #4 est en cours d'évaluation par le STPM");
np10.setTypeChangement("CANDIDATURE_EN_COURS");
np10.setProblemeId(4);
np10.setQuartier("Hochelaga");
np10.setDateCreation(LocalDateTime.now().minusDays(2));
np10.setLu(false);
notifications.add(np10);
    
    // NOTIFICATIONS STPM (comme avant)
    notifications.add(Notification.pourStpm(
        "Nouveau problème signalé #4 dans Hochelaga-Maisonneuve", 
        "NOUVEAU_PROBLEME", 4, "Hochelaga-Maisonneuve"));
    
    notifications.add(Notification.pourStpm(
        "Nouveau projet créé #5 par TechnoVert Solutions", 
        "NOUVEAU_PROJET", 5, "Centre-ville"));
    
    return notifications;
}

private Map<String, Object> createDefaultPreferences() {
    Map<String, Object> defautPrefs = new HashMap<>();
    defautPrefs.put("quartiers", new ArrayList<String>());
    defautPrefs.put("rues", new ArrayList<String>());
    defautPrefs.put("types_travaux", new ArrayList<String>());
    defautPrefs.put("frequence", "IMMEDIATE");
    defautPrefs.put("actives", true);
    return defautPrefs;
}

// Méthode corrigée - suppression de @Override et amélioration de la logique
public void creerNotification(String destinataire, String message, String typeChangement, 
                            int projetId, String quartier) {
    List<Notification> notifications = loadNotifications();
    
    // Déterminer le type de destinataire
    if (destinataire.equals("STPM")) {
        Notification nouvelle = Notification.pourStpm(message, typeChangement, projetId, quartier);
        notifications.add(nouvelle);
    } else if (destinataire.startsWith("NEQ")) {
        // C'est un prestataire
        Notification nouvelle = new Notification(destinataire, message, typeChangement, 
                                               projetId, quartier, null, true);
        notifications.add(nouvelle);
    } else {
        // C'est un résident (comportement existant)
        Notification nouvelle = new Notification(destinataire, message, typeChangement, projetId, quartier);
        notifications.add(nouvelle);
    }
    
    saveNotifications(notifications);
}
}