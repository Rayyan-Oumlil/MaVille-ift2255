package ca.udem.maville.ui;

import ca.udem.maville.modele.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

/*
    Classe responsable de la saisie des données utilisateur via la console
    Gère toutes les entrées clavier avec validation
 */
public class SaisieConsole {
    private Scanner scanner;
    // Format de date utilisé dans toute l'application
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /*
        Constructeur - initialise le scanner pour lire les entrées clavier
     */
    public SaisieConsole() {
        this.scanner = new Scanner(System.in);
    }

    /*
        Lit un nombre entier avec validation
        Redemande tant que l'utilisateur n'entre pas un nombre valide
     */
    public int lireEntier(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                // Si ce n'est pas un nombre, on redemande
                System.out.println("Veuillez entrer un nombre valide.");
            }
        }
    }

    /*
        Lit un nombre décimal avec validation
     */
    public double lireDouble(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre décimal valide.");
            }
        }
    }

    /*
        Lit une chaîne de caractères simple (peut être vide)
     */
    public String lireChaine(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    /*
        Lit une chaîne non vide - redemande tant que l'utilisateur n'entre rien
     */
    public String lireChaineNonVide(String message) {
        String resultat;
        do {
            resultat = lireChaine(message);
            if (resultat.trim().isEmpty()) {
                System.out.println("Ce champ ne peut pas être vide.");
            }
        } while (resultat.trim().isEmpty());
        return resultat.trim(); // Enlève les espaces en début/fin
    }

    /*
        Lit une date avec validation du format dd/MM/yyyy
     */
    public LocalDate lireDate(String message) {
        while (true) {
            String dateStr = lireChaine(message + " (format: jj/mm/aaaa): ");
            try {
                return LocalDate.parse(dateStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                // Si le format est incorrect, on redemande
                System.out.println("Format de date invalide. Utilisez jj/mm/aaaa");
            }
        }
    }

    /*
        Affiche la liste des types de travaux et fait choisir à l'utilisateur
     */
    public TypeTravaux choisirTypeTravaux() {
        System.out.println("\nTypes de travaux disponibles:");
        TypeTravaux[] types = TypeTravaux.values(); // Récupère tous les types de l'enum
        
        // Affiche chaque type avec un numéro
        for (int i = 0; i < types.length; i++) {
            System.out.println((i + 1) + ". " + types[i].getDescription());
        }
        
        // Lit le choix avec validation
        int choix = lireEntier("Choisissez un type (1-" + types.length + "): ");
        while (choix < 1 || choix > types.length) {
            choix = lireEntier("Choix invalide. Choisissez entre 1 et " + types.length + ": ");
        }
        
        return types[choix - 1]; // -1 car les tableaux commencent à 0
    }

    /*
        Fait choisir une priorité parmi les priorités disponibles
     */
    public Priorite choisirPriorite() {
        System.out.println("\nPriorités disponibles:");
        Priorite[] priorites = Priorite.values();
        
        for (int i = 0; i < priorites.length; i++) {
            System.out.println((i + 1) + ". " + priorites[i].getDescription());
        }
        
        int choix = lireEntier("Choisissez une priorité (1-" + priorites.length + "): ");
        while (choix < 1 || choix > priorites.length) {
            choix = lireEntier("Choix invalide. Choisissez entre 1 et " + priorites.length + ": ");
        }
        
        return priorites[choix - 1];
    }

    /*
        Fait choisir un statut de projet
     */
    public StatutProjet choisirStatutProjet() {
        System.out.println("\nStatuts disponibles:");
        // Seulement ces 3 statuts sont proposés 
        StatutProjet[] statuts = {StatutProjet.EN_COURS, StatutProjet.SUSPENDU, StatutProjet.TERMINE};
        
        for (int i = 0; i < statuts.length; i++) {
            System.out.println((i + 1) + ". " + statuts[i].getDescription());
        }
        
        int choix = lireEntier("Choisissez un statut (1-" + statuts.length + "): ");
        while (choix < 1 || choix > statuts.length) {
            choix = lireEntier("Choix invalide. Choisissez entre 1 et " + statuts.length + ": ");
        }
        
        return statuts[choix - 1];
    }

    /*
        Lit une liste d'IDs séparés par des virgules
     */
    public List<Integer> lireListeEntiers(String message) {
        System.out.println(message);
        System.out.println("Entrez les IDs séparés par des virgules (ex: 1,2,3):");
        String input = scanner.nextLine();
        List<Integer> ids = new ArrayList<>();
        
        if (!input.trim().isEmpty()) {
            String[] parts = input.split(","); // Sépare par les virgules
            for (String part : parts) {
                try {
                    ids.add(Integer.parseInt(part.trim())); // Enlève les espaces et convertit
                } catch (NumberFormatException e) {
                    // Si un ID n'est pas valide, on l'ignore mais on prévient
                    System.out.println("ID invalide ignoré: " + part.trim());
                }
            }
        }
        
        return ids;
    }

    /*
        Demande une confirmation oui/non
     */
    public boolean confirmer(String message) {
        System.out.print(message + " (o/n): ");
        String reponse = scanner.nextLine().toLowerCase();
        return reponse.equals("o") || reponse.equals("oui");
    }

    /*
        Attend que l'utilisateur appuie sur Entrée avant de continuer
        Utile pour faire des pauses dans l'affichage
     */
    public void attendreEntree() {
        System.out.print("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }
}