package ca.udem.maville.ui;

import ca.udem.maville.modele.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*
    Classe responsable de l'affichage des informations à la console
    Gère tous les messages, titres, et données qui s'affichent à l'écran
 */
public class AffichageConsole {
    // Formatters pour afficher les dates de façon uniforme
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /*
        Affiche un titre principal avec des bordures décoratives
     */
    public void afficherTitre(String titre) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  " + titre.toUpperCase());
        System.out.println("=".repeat(50));
    }

    /*
        Affiche un sous-titre avec des tirets
     */
    public void afficherSousTitre(String sousTitre) {
        System.out.println("\n--- " + sousTitre + " ---");
    }

    /*
        Affiche un message simple
     */
    public void afficherMessage(String message) {
        System.out.println(message);
    }

    /*
        Affiche un message d'erreur avec le préfixe "ERREUR:"
     */
    public void afficherErreur(String erreur) {
        System.out.println("ERREUR: " + erreur);
    }

    /*
        Affiche un message de succès avec le préfixe "SUCCÈS:"
     */
    public void afficherSucces(String message) {
        System.out.println("SUCCÈS: " + message);
    }

    /*
        Affiche toutes les informations détaillées d'un problème
     */
    public void afficherProbleme(Probleme probleme) {
        System.out.println("ID: " + probleme.getId());
        System.out.println("Type: " + probleme.getTypeProbleme().getDescription());
        System.out.println("Lieu: " + probleme.getLieu());
        System.out.println("Description: " + probleme.getDescription());
        System.out.println("Déclarant: " + probleme.getDeclarant().getNomComplet());
        System.out.println("Date: " + probleme.getDateSignalement().format(DATETIME_FORMATTER));
        System.out.println("Priorité: " + probleme.getPriorite().getDescription());
        System.out.println("Résolu: " + (probleme.isResolu() ? "Oui" : "Non"));
    }

    /*
        Affiche une liste de problèmes numérotés
        Gère le cas où la liste est vide
     */
    public void afficherListeProblemes(List<Probleme> problemes) {
        if (problemes.isEmpty()) {
            System.out.println("Aucun problème trouvé.");
            return;
        }

        System.out.println("\n" + problemes.size() + " problème(s) trouvé(s):");
        // Affiche chaque problème avec un numéro
        for (int i = 0; i < problemes.size(); i++) {
            System.out.println("\n" + (i + 1) + ". " + problemes.get(i));
        }
    }

    /*
        Affiche toutes les informations d'une candidature
     */
    public void afficherCandidature(Candidature candidature) {
        System.out.println("ID: " + candidature.getId());
        System.out.println("Prestataire: " + candidature.getPrestataire().getNomEntreprise());
        System.out.println("Problèmes visés: " + candidature.getProblemesVises());
        System.out.println("Description: " + candidature.getDescriptionProjet());
        System.out.println("Coût estimé: " + candidature.getCoutEstime() + "$");
        System.out.println("Date début prévue: " + candidature.getDateDebutPrevue().format(DATE_FORMATTER));
        System.out.println("Date fin prévue: " + candidature.getDateFinPrevue().format(DATE_FORMATTER));
        System.out.println("Date dépôt: " + candidature.getDateDepot().format(DATETIME_FORMATTER));
        System.out.println("Statut: " + candidature.getStatut().getDescription());
        // Affiche le commentaire seulement s'il existe
        if (candidature.getCommentaireRejet() != null) {
            System.out.println("Commentaire: " + candidature.getCommentaireRejet());
        }
    }

    /*
        Affiche une liste de candidatures numérotées
     */
    public void afficherListeCandidatures(List<Candidature> candidatures) {
        if (candidatures.isEmpty()) {
            System.out.println("Aucune candidature trouvée.");
            return;
        }

        System.out.println("\n" + candidatures.size() + " candidature(s) trouvée(s):");
        for (int i = 0; i < candidatures.size(); i++) {
            System.out.println("\n" + (i + 1) + ". " + candidatures.get(i));
        }
    }

    /*
        Affiche toutes les informations détaillées d'un projet
     */
    public void afficherProjet(Projet projet) {
        System.out.println("ID: " + projet.getId());
        System.out.println("Localisation: " + projet.getLocalisation());
        System.out.println("Type: " + projet.getTypeTravail().getDescription());
        System.out.println("Statut: " + projet.getStatut().getDescription());
        System.out.println("Priorité: " + projet.getPriorite().getDescription());
        System.out.println("Prestataire: " + projet.getPrestataire().getNomEntreprise());
        System.out.println("Description: " + projet.getDescriptionProjet());
        System.out.println("Coût: " + projet.getCout() + "$");
        System.out.println("Nombre de rapports: " + projet.getNombreRapports());
        System.out.println("Date début prévue: " + projet.getDateDebutPrevue().format(DATE_FORMATTER));
        System.out.println("Date fin prévue: " + projet.getDateFinPrevue().format(DATE_FORMATTER));
        
        // Affiche les dates réelles seulement si elles existent
        if (projet.getDateDebutReelle() != null) {
            System.out.println("Date début réelle: " + projet.getDateDebutReelle().format(DATE_FORMATTER));
        }
        if (projet.getDateFinReelle() != null) {
            System.out.println("Date fin réelle: " + projet.getDateFinReelle().format(DATE_FORMATTER));
        }
        
        System.out.println("Dernière mise à jour: " + projet.getDerniereMiseAJour().format(DATETIME_FORMATTER));
    }

    /*
        Affiche une liste de projets numérotés
     */
    public void afficherListeProjets(List<Projet> projets) {
        if (projets.isEmpty()) {
            System.out.println("Aucun projet trouvé.");
            return;
        }

        System.out.println("\n" + projets.size() + " projet(s) trouvé(s):");
        for (int i = 0; i < projets.size(); i++) {
            System.out.println("\n" + (i + 1) + ". " + projets.get(i));
        }
    }

    /*
        Affiche un menu avec options numérotées + option "0. Retour"
     */
    public void afficherMenu(String[] options) {
        System.out.println();
        // Affiche chaque option avec un numéro (commence à 1)
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        // Ajoute toujours l'option retour
        System.out.println("0. Retour");
    }

    /*
        Simule l'effacement de l'écran en affichant plusieurs lignes vides
     */
    public void effacerEcran() {
        // Simulation avec 50 lignes vides
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
}