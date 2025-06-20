package ca.udem.maville.storage;

import ca.udem.maville.modele.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.*;

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
    
    // === MÉTHODES UTILITAIRES ===
    public void initializeWithSampleData() {
        // Initialise avec des données de test si les fichiers n'existent pas
        if (loadProblemes().isEmpty()) {
            List<Probleme> sampleProblemes = createSampleProblemes();
            saveProblemes(sampleProblemes);
        }
        
        if (loadResidents().isEmpty()) {
            List<Resident> sampleResidents = createSampleResidents();
            saveResidents(sampleResidents);
        }
        
        if (loadPrestataires().isEmpty()) {
            List<Prestataire> samplePrestataires = createSamplePrestataires();
            savePrestataires(samplePrestataires);
        }
    }
    
    private List<Probleme> createSampleProblemes() {
        List<Probleme> problemes = new ArrayList<>();
        // Ajouter vos données de test ici selon vos constructeurs
        // Exemple basique (adaptez selon vos classes) :
        // problemes.add(new Probleme("Rue Saint-Denis", "Nid de poule", "resident1"));
        return problemes;
    }
    
    private List<Resident> createSampleResidents() {
        List<Resident> residents = new ArrayList<>();
        // Ajouter vos résidents de test
        return residents;
    }
    
    private List<Prestataire> createSamplePrestataires() {
        List<Prestataire> prestataires = new ArrayList<>();
        // Ajouter vos prestataires de test
        return prestataires;
    }
    
    public void clearAllData() {
        File[] files = {
            new File(PROBLEMES_FILE),
            new File(PROJETS_FILE), 
            new File(RESIDENTS_FILE),
            new File(PRESTATAIRES_FILE)
        };
        
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }
}