package ca.udem.maville.service;

import ca.udem.maville.modele.*;
import ca.udem.maville.storage.JsonStorage;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/*
    Service qui gère tous les problèmes signalés par les résidents.
    Version corrigée qui charge les données depuis le storage.
 */
public class GestionnaireProblemes {
    // Liste de tous les problèmes signalés
    private List<Probleme> problemes;
    private JsonStorage storage;

    // Constructeur par défaut pour compatibilité
    public GestionnaireProblemes() {
        this.problemes = new ArrayList<>();
    }
    
    // Nouveau constructeur avec storage
    public GestionnaireProblemes(JsonStorage storage) {
        this.storage = storage;
        this.problemes = new ArrayList<>();
        chargerProblemes();
    }
    
    /*
        Charge les problèmes existants depuis le storage
     */
    private void chargerProblemes() {
        if (storage != null) {
            List<Probleme> problemesExistants = storage.loadProblemes();
            if (problemesExistants != null && !problemesExistants.isEmpty()) {
                this.problemes.addAll(problemesExistants);
                System.out.println("GestionnaireProblemes: " + problemes.size() + " problèmes chargés");
            }
        }
    }

    /*
        Crée un nouveau problème signalé par un résident.
        Priorité automatiquement fixée à MOYENNE (simplification pour le prototype).
     */
    public Probleme signalerProbleme(String lieu, TypeTravaux type, String description, Resident declarant) {
        Probleme probleme = new Probleme(lieu, type, description, declarant);
        // Attribution automatique de priorité MOYENNE
        probleme.setPriorite(Priorite.MOYENNE);
        problemes.add(probleme);
        
        // Sauvegarder si storage disponible
        if (storage != null) {
            storage.saveProblemes(problemes);
        }
        
        return probleme;
    }

    /*
        Retourne tous les problèmes signalés.
        Retourne une copie pour éviter les modifications externes.
     */
    public List<Probleme> listerProblemes() {
        return new ArrayList<>(problemes);
    }

    /*
        Retourne seulement les problèmes pas encore résolus.
        Utilisé pour afficher les problèmes disponibles aux prestataires.
     */
    public List<Probleme> listerProblemesNonResolus() {
        return problemes.stream()
                .filter(p -> !p.isResolu())
                .collect(Collectors.toList());
    }

    /*
        Trouve un problème par son ID.
        Retourne null si pas trouvé.
     */
    public Probleme trouverProblemeParId(int id) {
        return problemes.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /*
        Trouve plusieurs problèmes à partir d'une liste d'IDs.
        Filtre automatiquement les IDs qui n'existent pas.
        Utilisé pour créer des projets à partir des candidatures.
     */
    @SuppressWarnings("null")
    public List<Probleme> trouverProblemesParIds(List<Integer> ids) {
        return ids.stream()
                .map(this::trouverProblemeParId)  // Cherche chaque ID
                .filter(p -> p != null)          // Enlève les nulls
                .collect(Collectors.toList());
    }
    
    /*
        Méthode pour synchroniser avec le storage
        Utile pour recharger les données
     */
    public void synchroniserAvecStorage(JsonStorage storage) {
        this.storage = storage;
        this.problemes.clear();
        chargerProblemes();
    }
}