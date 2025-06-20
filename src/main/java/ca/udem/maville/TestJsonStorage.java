package ca.udem.maville;

import ca.udem.maville.storage.JsonStorage;
import ca.udem.maville.modele.*;
import java.util.List;

/**
 * Test rapide de JsonStorage
 * À supprimer après validation
 */
public class TestJsonStorage {
    public static void main(String[] args) {
        System.out.println("🧪 Test JsonStorage...");
        
        JsonStorage storage = new JsonStorage();
        
        // Test 1: Créer le dossier data/
        System.out.println("✅ JsonStorage créé");
        
        // Test 2: Charger des listes vides (première fois)
        List<Probleme> problemes = storage.loadProblemes();
        List<Resident> residents = storage.loadResidents();
        
        System.out.println("📁 Problèmes chargés: " + problemes.size());
        System.out.println("📁 Résidents chargés: " + residents.size());
        
        // Test 3: Sauvegarder des listes vides (pour voir si ça crash)
        try {
            storage.saveProblemes(problemes);
            storage.saveResidents(residents);
            System.out.println("💾 Sauvegarde OK");
        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde: " + e.getMessage());
        }
        
        // Test 4: Recharger pour voir si les fichiers sont créés
        List<Probleme> problemesReloaded = storage.loadProblemes();
        System.out.println("🔄 Rechargement OK: " + problemesReloaded.size() + " problèmes");
        
        // Test 5: Initialisation avec données de test
        try {
            storage.initializeWithSampleData();
            System.out.println("🎲 Données de test initialisées");
        } catch (Exception e) {
            System.err.println("❌ Erreur init données: " + e.getMessage());
        }
        
        System.out.println("✅ Test JsonStorage terminé !");
        System.out.println("📂 Vérifiez qu'un dossier 'data/' a été créé dans votre projet");
    }
}