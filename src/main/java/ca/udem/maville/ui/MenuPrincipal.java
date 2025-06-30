package ca.udem.maville.ui;

import ca.udem.maville.ui.client.HttpClient;
import java.util.Scanner;

public class MenuPrincipal {
    private final HttpClient httpClient;

    public MenuPrincipal(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void demarrer() {
        Scanner scanner = new Scanner(System.in);
        boolean continuer = true;
        
        while (continuer) {
            System.out.println("\n=== MENU PRINCIPAL MAVILLE ===");
            System.out.println("Choisissez votre profil :");
            System.out.println("1. Résident");
            System.out.println("2. Prestataire de services");
            System.out.println("3. Agent STPM");
            System.out.println("0. Quitter");
            System.out.print("Votre choix : ");
            
            String choix = scanner.nextLine();

            switch (choix) {
                case "1":
                    new MenuResident(httpClient).afficher();
                    break;
                case "2":
                    new MenuPrestataire(httpClient).afficher();
                    break;
                case "3":
                    new MenuStpm(httpClient).afficher();
                    break;
                case "0":
                    continuer = false;
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
            }
        }
        
        scanner.close();
        System.out.println("\n=== Au revoir! ===");
        System.out.println("Merci d'avoir utilisé MaVille.");
    }
}