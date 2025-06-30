package ca.udem.maville.storage;

import ca.udem.maville.modele.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.time.LocalDate;

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
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement candidatures: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    // === MÉTHODES UTILITAIRES ===
    public void initializeWithSampleData() {
        // Initialise avec des données de test si les fichiers n'existent pas
        if (loadResidents().isEmpty()) {
            List<Resident> sampleResidents = createSampleResidents();
            saveResidents(sampleResidents);
            System.out.println( sampleResidents.size() + " résidents créés");
        }
        
        if (loadPrestataires().isEmpty()) {
            List<Prestataire> samplePrestataires = createSamplePrestataires();
            savePrestataires(samplePrestataires);
            System.out.println( samplePrestataires.size() + " prestataires créés");
        }
        
        if (loadProblemes().isEmpty()) {
            List<Probleme> sampleProblemes = createSampleProblemes();
            saveProblemes(sampleProblemes);
            System.out.println( sampleProblemes.size() + " problèmes créés");
        }
        
        if (loadCandidatures().isEmpty()) {
            List<Candidature> sampleCandidatures = createSampleCandidatures();
            saveCandidatures(sampleCandidatures);
            System.out.println( sampleCandidatures.size() + " candidatures créées");
        }
    }
    
    private List<Resident> createSampleResidents() {
        List<Resident> residents = new ArrayList<>();
        
        // 4 résidents 
        residents.add(new Resident("Tremblay", "Marie", "514-555-0001", 
            "marie.tremblay@email.com", "123 Rue Saint-Denis, Montréal"));
        
        residents.add(new Resident("Gagnon", "Pierre", "514-555-0002", 
            "pierre.gagnon@email.com", "456 Avenue du Parc, Montréal"));
        
        residents.add(new Resident("Roy", "Sophie", "514-555-0003", 
            "sophie.roy@email.com", "789 Boulevard René-Lévesque, Montréal"));
        
        residents.add(new Resident("Bouchard", "Jean", "514-555-0004", 
            "jean.bouchard@email.com", "321 Rue Sainte-Catherine, Montréal"));
        
        return residents;
    }
    
    private List<Prestataire> createSamplePrestataires() {
        List<Prestataire> prestataires = new ArrayList<>();
        
        // 4 prestataires 
        prestataires.add(new Prestataire("NEQ1234567890", "Construction ABC Inc.", 
            "Robert Dubois", "514-666-0001", "contact@constructionabc.com"));
        
        prestataires.add(new Prestataire("NEQ0987654321", "Pavage Pro Ltée", 
            "Martine Lavoie", "514-666-0002", "info@pavagepro.com"));
        
        prestataires.add(new Prestataire("NEQ1122334455", "Électricité Montréal", 
            "Claude Martin", "514-666-0003", "service@elecmtl.com"));
        
        prestataires.add(new Prestataire("NEQ5544332211", "Paysagement Vert", 
            "Lucie Bergeron", "514-666-0004", "contact@paysvert.com"));
        
        return prestataires;
    }
    
private List<Probleme> createSampleProblemes() {
    List<Probleme> problemes = new ArrayList<>();
    
    List<Resident> residents = loadResidents();
    if (residents.isEmpty()) {
        residents = createSampleResidents();
        saveResidents(residents);
    }
    
    // 4 problèmes avec quartier
    
    // Problème 1 : ROSEMONT
    Probleme p1 = new Probleme(
        "Boulevard Rosemont coin 10e Avenue, Rosemont",
        TypeTravaux.TRAVAUX_ROUTIERS,
        "Gros nid de poule dangereux pour les cyclistes près de l'école",
        residents.get(0)
    );
    p1.setPriorite(Priorite.ELEVEE);
    problemes.add(p1);
    
    // Problème 2 : PLATEAU
    Probleme p2 = new Probleme(
        "Parc Lafontaine entrée Sherbrooke, Plateau Mont-Royal",
        TypeTravaux.ENTRETIEN_PAYSAGER,
        "Branches d'arbres tombées après la tempête bloquant l'allée principale",
        residents.get(1)
    );
    p2.setPriorite(Priorite.MOYENNE);
    problemes.add(p2);
    
    // Problème 3 : VILLE-MARIE
    Probleme p3 = new Probleme(
        "Coin René-Lévesque et Saint-Laurent, Ville-Marie",
        TypeTravaux.TRAVAUX_SIGNALISATION_ECLAIRAGE,
        "Feu de circulation défectueux - reste toujours rouge direction ouest",
        residents.get(2)
    );
    p3.setPriorite(Priorite.ELEVEE);
    problemes.add(p3);
    
    // Problème 4 : HOCHELAGA
    Probleme p4 = new Probleme(
        "Rue Ontario Est près du métro Pie-IX, Hochelaga-Maisonneuve",
        TypeTravaux.TRAVAUX_SOUTERRAINS,
        "Fuite d'eau importante créant une flaque sur le trottoir",
        residents.get(3)
    );
    p4.setPriorite(Priorite.MOYENNE);
    problemes.add(p4);
    
    return problemes;
}
    
    private List<Candidature> createSampleCandidatures() {
    List<Candidature> candidatures = new ArrayList<>();
    
    List<Prestataire> prestataires = loadPrestataires();
    if (prestataires.isEmpty()) {
        prestataires = createSamplePrestataires();
        savePrestataires(prestataires);
    }
        
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
    
    return candidatures;
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
}