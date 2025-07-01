package ca.udem.maville;

import ca.udem.maville.api.ApiServer;          // Serveur REST API
import ca.udem.maville.ui.MenuPrincipal;       // Menu principal console (UI)
import ca.udem.maville.ui.client.HttpClient;   // Client HTTP REST

/**
 * Démarrage de l'application MaVille en mode REST
 * Lance le serveur API et propose le menu principal (console)
 */
public class Main {
    private static ApiServer apiServer;
    private static HttpClient httpClient;
    private static String[] args; // pour détecter les options comme --test

    public static void main(String[] arguments) {
        args = arguments;
        System.out.println("=== MaVille - Architecture REST ===\n");

        try {
            // 1. Démarrer le serveur API dans un thread séparé
            demarrerServeur();

            // 2. Attendre que le serveur soit prêt (30 secondes max)
            if (!attendreServeurPret()) {
                System.err.println(" Impossible de démarrer le serveur API");
                System.exit(1);
            }

            // 3. Créer un seul client HTTP partagé par toute l'application
            httpClient = new HttpClient();

            // 4. Vérifier que le client peut se connecter
            if (!httpClient.testerConnexion()) {
                System.err.println(" Impossible de se connecter à l'API");
                System.exit(1);
            }

            System.out.println(" Serveur API opérationnel sur http://localhost:7000/api\n");

            // 5. Lancer le menu principal, en passant l'instance du HttpClient unique
            lancerMenuPrincipal();

        } catch (Exception e) {
            System.err.println(" Erreur : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 6. Arrêter proprement le client et le serveur API
            arreterServeur();
        }
    }

    /**
     * Démarre le serveur API dans un thread séparé
     */
    private static void demarrerServeur() {
        System.out.println("1. Démarrage du serveur API...");
        Thread serverThread = new Thread(() -> {
            try {
                apiServer = new ApiServer();
                apiServer.start();
            } catch (Exception e) {
                System.err.println(" Erreur démarrage serveur: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    /**
     * Attend jusqu'à 30 secondes que le serveur REST soit prêt à accepter des connexions
     */
    private static boolean attendreServeurPret() {
        System.out.print("2. Attente du serveur");

        // Attendre jusqu'à 30 secondes que le serveur démarre
        for (int i = 0; i < 30; i++) {
            try {
                Thread.sleep(1000);
                System.out.print(".");

                // Essayer de se connecter à partir de 3 secondes
                if (i > 2) {
                    HttpClient testClient = new HttpClient();
                    if (testClient.testerConnexion()) {
                        System.out.println(" OK");
                        testClient.fermer();
                        return true;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        System.out.println(" TIMEOUT");
        return false;
    }

    /**
     * Lance le menu principal (console)
     */
    private static void lancerMenuPrincipal() {
        System.out.println("3. Lancement de l'interface utilisateur\n");

        // Mode test pour vérifier tous les endpoints REST
        if (args.length > 0 && args[0].equals("--test")) {
            System.out.println("Mode test activé - Test de tous les endpoints:");
            httpClient.testerTousLesEndpoints();
            System.out.println();
        }

        // Lancer le menu principal en passant le même httpClient 
        MenuPrincipal menuPrincipal = new MenuPrincipal(httpClient);
        menuPrincipal.demarrer();
    }

    /**
     * Arrête le serveur et ferme le client HTTP
     */
    private static void arreterServeur() {
        System.out.println("\n=== Arrêt du système ===");

        if (httpClient != null) {
            System.out.println("- Fermeture du client HTTP...");
            httpClient.fermer();
        }

        if (apiServer != null && apiServer.isRunning()) {
            System.out.println("- Arrêt du serveur API...");
            apiServer.stop();
        }

        System.out.println(" Système arrêté proprement");
        System.out.println("=== Merci d'avoir utilisé MaVille ===");
    }
}
