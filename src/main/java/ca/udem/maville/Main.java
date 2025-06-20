package ca.udem.maville;

import ca.udem.maville.api.ApiServer;

/**
 * Point d'entrée principal - MaVille avec architecture REST
 * Version progressive : commence simple, évolue vers complet
 */
public class Main {
    private static ApiServer apiServer;
    
    public static void main(String[] args) {
        System.out.println("=== MaVille - Architecture REST ===");
        System.out.println("Démarrage du système...\n");
        
        // Pour l'instant, juste le serveur API
        demarrerServeurSeul();
        
        // TODO: Ajouter le client CLI quand HttpClient sera prêt
        // demarrerArchitectureComplete();
    }
    
    /**
     * Version actuelle : serveur seul pour tests
     */
    private static void demarrerServeurSeul() {
        System.out.println("BACKEND: Démarrage du serveur API REST...");
        
        try {
            apiServer = new ApiServer();
            apiServer.start(); // Lance sur port 7000
            
            System.out.println("\n=== Serveur API prêt ! ===");
            System.out.println("Testez dans votre navigateur :");
            System.out.println("- http://localhost:7000/api/health");
            System.out.println("- http://localhost:7000/api/residents/travaux");
            System.out.println("- http://localhost:7000/api/prestataires/problemes");
            System.out.println("\nAppuyez sur Ctrl+C pour arrêter le serveur");
            
            // Garder le serveur en vie
            Thread.currentThread().join();
            
        } catch (InterruptedException e) {
            System.out.println("\nArrêt demandé...");
        } catch (Exception e) {
            System.err.println("Erreur démarrage serveur: " + e.getMessage());
        } finally {
            if (apiServer != null) {
                apiServer.stop();
                System.out.println("Serveur arrêté proprement");
            }
        }
    }
    
    /**
     * Version future : architecture complète avec client CLI
     * À utiliser quand HttpClient sera créé
     */
    private static void demarrerArchitectureComplete() {
        System.out.println("=== Architecture REST complète ===");
        
        // Thread serveur
        Thread serverThread = new Thread(() -> {
            System.out.println("BACKEND: Démarrage serveur...");
            apiServer = new ApiServer();
            apiServer.start();
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        // Attendre démarrage serveur
        waitForServer();
        
        // TODO: Lancer client CLI
        System.out.println("FRONTEND: Client CLI à implémenter");
        
        // Nettoyage
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (apiServer != null) {
                apiServer.stop();
            }
        }));
    }
    
    /**
     * Attendre que le serveur soit prêt
     */
    private static void waitForServer() {
        System.out.print("Attente serveur");
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(500);
                System.out.print(".");
            } catch (InterruptedException e) {
                break;
            }
        }
        System.out.println(" OK");
    }
    
    /**
     * Test rapide des endpoints
     */
    public static void testerEndpoints() {
        System.out.println("=== Test des endpoints ===");
        System.out.println("Lancez ces URLs dans votre navigateur :");
        System.out.println("GET  http://localhost:7000/api/health");
        System.out.println("GET  http://localhost:7000/api/residents/travaux");
        System.out.println("GET  http://localhost:7000/api/prestataires/problemes");
        System.out.println("GET  http://localhost:7000/api/montreal/travaux");
    }
}