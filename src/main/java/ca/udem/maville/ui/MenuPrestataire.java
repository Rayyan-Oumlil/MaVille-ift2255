package ca.udem.maville.ui;

import ca.udem.maville.modele.*;
import ca.udem.maville.service.*;
import java.util.List;
import java.util.ArrayList;

/*
    Menu spécialisé pour les prestataires de services (entreprises)
    Permet de consulter les problèmes, soumettre des candidatures et gérer ses projets
 */
public class MenuPrestataire {
    // Outils d'interface utilisateur
    private SaisieConsole saisie;
    private AffichageConsole affichage;
    
    // Services métier
    private GestionnaireProblemes gestionnaireProblemes;
    private GestionnaireProjets gestionnaireProjets;

    /*
        Constructeur - initialise le menu avec les services nécessaires
     */
    public MenuPrestataire(GestionnaireProblemes gestionnaireProblemes, GestionnaireProjets gestionnaireProjets) {
        this.saisie = new SaisieConsole();
        this.affichage = new AffichageConsole();
        this.gestionnaireProblemes = gestionnaireProblemes;
        this.gestionnaireProjets = gestionnaireProjets;
    }

    /*
        Affiche le menu principal du prestataire et gère ses choix
     */
    public void afficher() {
        boolean continuer = true;
        
        while (continuer) {
            affichage.afficherTitre("Menu Prestataire");
            
            // Options disponibles pour un prestataire
            String[] options = {
                "Consulter les problèmes signalés",
                "Soumettre une candidature pour un projet",
                "Gérer mes candidatures"
            };
            
            affichage.afficherMenu(options);
            int choix = saisie.lireEntier("Votre choix: ");
            
            switch (choix) {
                case 1:
                    consulterProblemes();
                    break;
                case 2:
                    soumettreCandidate();
                    break;
                case 3:
                    gererCandidatures();
                    break;
                case 0:
                    continuer = false; // Retour au menu principal
                    break;
                default:
                    affichage.afficherErreur("Choix invalide");
            }
            
            if (continuer) {
                saisie.attendreEntree();
            }
        }
    }

    /*
        Simule la consultation libre des problèmes au bureau du STPM
     */
    private void consulterProblemes() {
        affichage.afficherSousTitre("Consultation des problèmes signalés");
        
        // Simulation de la visite au bureau STPM
        affichage.afficherMessage("=== FICHES PROBLÈMES EN ATTENTE ===");
        affichage.afficherMessage("(Consultation libre - Bureau STPM)");
        affichage.afficherMessage("");
        
        // Récupère tous les problèmes en attente
        List<Probleme> problemesDisponibles = gestionnaireProblemes.listerProblemesNonResolus();
        
        if (problemesDisponibles.isEmpty()) {
            affichage.afficherMessage("Aucun problème en attente actuellement.");
            affichage.afficherMessage("Revenez consulter plus tard.");
        } else {
            affichage.afficherMessage("Problèmes en attente que vous pouvez consulter :");
            affichage.afficherListeProblemes(problemesDisponibles);
            
            // Guide l'utilisateur vers l'étape suivante
            affichage.afficherMessage("");
            affichage.afficherMessage("  Si un problème vous intéresse :");
            affichage.afficherMessage("  --> Utilisez l'option 2 pour soumettre une candidature");
            affichage.afficherMessage("  --> Vous remplirez alors le formulaire avec vos informations");
        }
    }

    /*
        Gère la soumission d'une nouvelle candidature
        Processus complet : sélection problèmes + remplissage formulaire + confirmation
     */
    private void soumettreCandidate() {
        affichage.afficherSousTitre("Soumettre une candidature pour un projet");
        
        // Afficher les problèmes disponibles pour candidature
        List<Probleme> problemesDisponibles = gestionnaireProblemes.listerProblemesNonResolus();
        if (problemesDisponibles.isEmpty()) {
            affichage.afficherMessage("Aucun problème disponible pour candidature actuellement.");
            return;
        }
        
        affichage.afficherMessage("Problèmes disponibles pour candidature :");
        affichage.afficherListeProblemes(problemesDisponibles);
        affichage.afficherMessage("");
        
        // Sélection des problème(s) d'intérêt
        List<Integer> problemesVises = saisie.lireListeEntiers("IDs des problèmes qui vous intéressent");
        if (problemesVises.isEmpty()) {
            affichage.afficherErreur("Aucun problème sélectionné.");
            return;
        }
        
        // Validation que les problèmes existent et sont disponibles
        List<Probleme> problemesSelectionnes = new ArrayList<>();
        for (Integer id : problemesVises) {
            Probleme p = gestionnaireProblemes.trouverProblemeParId(id);
            if (p != null && !p.isResolu()) {
                problemesSelectionnes.add(p);
            } else {
                affichage.afficherErreur("Problème #" + id + " introuvable ou déjà résolu. Ignoré.");
            }
        }
        
        if (problemesSelectionnes.isEmpty()) {
            affichage.afficherErreur("Aucun problème valide sélectionné.");
            return;
        }
        
        // Remplissage du formulaire de candidature
        affichage.afficherSousTitre("Formulaire de candidature");
        affichage.afficherMessage("Veuillez remplir vos informations d'entreprise :");
        String numeroEntreprise = saisie.lireChaineNonVide("Numéro d'entreprise (NE) : ");
        String nomEntreprise = saisie.lireChaineNonVide("Nom de l'entreprise : ");
        String contactNom = saisie.lireChaineNonVide("Nom du contact : ");
        String telephone = saisie.lireChaineNonVide("Téléphone : ");
        String email = saisie.lireChaineNonVide("Email : ");
        
        // Crée l'objet Prestataire pour cette candidature
        Prestataire prestataire = new Prestataire(numeroEntreprise, nomEntreprise, contactNom, telephone, email);
        String descriptionProjet = saisie.lireChaineNonVide("Description de votre projet de travaux : ");
        double coutEstime = saisie.lireDouble("Coût estimé du projet ($) : ");
        java.time.LocalDate dateDebut = saisie.lireDate("Date de début prévue");
        java.time.LocalDate dateFin = saisie.lireDate("Date de fin prévue");
        
        // Validation logique des dates
        if (dateDebut.isAfter(dateFin)) {
            affichage.afficherErreur("La date de début doit être antérieure à la date de fin.");
            return;
        }
        
        // Récapitulatif du formulaire complet
        affichage.afficherSousTitre("Récapitulatif du formulaire de candidature");
        affichage.afficherMessage("=== INFORMATIONS ENTREPRISE ===");
        affichage.afficherMessage("Numéro d'entreprise (NE) : " + numeroEntreprise);
        affichage.afficherMessage("Nom de l'entreprise : " + nomEntreprise);
        affichage.afficherMessage("Contact : " + contactNom);
        affichage.afficherMessage("Téléphone : " + telephone);
        affichage.afficherMessage("Email : " + email);
        affichage.afficherMessage("");
        affichage.afficherMessage("=== PROJET PROPOSÉ ===");
        affichage.afficherMessage("Problèmes ciblés : " + problemesVises);
        affichage.afficherMessage("Description du projet : " + descriptionProjet);
        affichage.afficherMessage("Coût estimé : " + coutEstime + " $");
        
        // Formatage des dates pour l'affichage
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        affichage.afficherMessage("Date de début prévue : " + dateDebut.format(dateFormatter));
        affichage.afficherMessage("Date de fin prévue : " + dateFin.format(dateFormatter));
        
        // Confirmation et soumission
        if (saisie.confirmer("Confirmer la soumission de ce formulaire de candidature ?")) {
            // Enregistre la candidature via le gestionnaire
            Candidature candidature = gestionnaireProjets.soumettreCandidat(
                prestataire, problemesVises, descriptionProjet, coutEstime, dateDebut, dateFin);
            
            // Confirmation avec informations utiles
            affichage.afficherSucces("Formulaire de candidature soumis avec succès !");
            affichage.afficherMessage("Numéro de candidature : " + candidature.getId());
            affichage.afficherMessage("Statut : " + candidature.getStatut().getDescription());
            affichage.afficherMessage("");
            
            // Explique la suite du processus
            affichage.afficherMessage("Votre candidature sera examinée par les agents du STPM.");
            affichage.afficherMessage("Si votre projet respecte le budget municipal, vous serez notifié");
            affichage.afficherMessage("de l'approbation et le projet sera officialisé.");
        } else {
            affichage.afficherMessage("Soumission annulée.");
        }
    }

    /*
        Point d'entrée pour la gestion des candidatures existantes
        Permet de consulter, modifier ou annuler ses candidatures
     */
    private void gererCandidatures() {
        affichage.afficherSousTitre("Gestion de mes candidatures");
        
        // Identification du prestataire 
        affichage.afficherMessage("Pour retrouver vos candidatures, veuillez vous identifier :");
        String numeroEntreprise = saisie.lireChaineNonVide("Numéro d'entreprise (NE) : ");
        
        // Recherche des candidatures de ce prestataire
        List<Candidature> toutesLesCandidatures = gestionnaireProjets.listerCandidatures();
        List<Candidature> mesCandidatures = new ArrayList<>();
        
        // Filtre pour ne garder que les candidatures de cette entreprise
        for (Candidature candidature : toutesLesCandidatures) {
            if (candidature.getPrestataire().getNumeroEntreprise().equals(numeroEntreprise)) {
                mesCandidatures.add(candidature);
            }
        }
        
        if (mesCandidatures.isEmpty()) {
            affichage.afficherMessage("Aucune candidature trouvée pour le numéro d'entreprise : " + numeroEntreprise);
            return;
        }
        
        // Affichage des candidatures trouvées
        affichage.afficherMessage("Vos candidatures :");
        affichage.afficherListeCandidatures(mesCandidatures);
        
        // Menu des actions possibles
        String[] optionsGestion = {
            "Consulter le détail d'une candidature",
            "Modifier une candidature",
            "Annuler une candidature"
        };
        
        affichage.afficherMenu(optionsGestion);
        int choix = saisie.lireEntier("Que souhaitez-vous faire ? ");
        
        // Traite le choix de l'utilisateur
        switch (choix) {
            case 1:
                consulterDetailCandidature(mesCandidatures);
                break;
            case 2:
                modifierCandidature(mesCandidatures);
                break;
            case 3:
                annulerCandidature(mesCandidatures);
                break;
            case 0:
                return; // Retour au menu prestataire
            default:
                affichage.afficherErreur("Choix invalide");
        }
    }

    /*
        Affiche le détail complet d'une candidature spécifique
     */
    private void consulterDetailCandidature(List<Candidature> candidatures) {
        if (candidatures.isEmpty()) return;
        
        int id = saisie.lireEntier("ID de la candidature à consulter : ");
        
        // Recherche de la candidature par ID
        Candidature candidature = null;
        for (Candidature c : candidatures) {
            if (c.getId() == id) {
                candidature = c;
                break;
            }
        }
        
        if (candidature == null) {
            affichage.afficherErreur("Candidature introuvable.");
            return;
        }
        
        // Affiche tous les détails de la candidature
        affichage.afficherSousTitre("Détail de la candidature #" + candidature.getId());
        affichage.afficherCandidature(candidature);
    }

    /*
        Permet de modifier une candidature existante
        Seules les candidatures non encore traitées peuvent être modifiées
     */
    private void modifierCandidature(List<Candidature> candidatures) {
        //Filtrer les candidatures modifiables
        List<Candidature> candidaturesModifiables = new ArrayList<>();
        for (Candidature c : candidatures) {
            if (c.peutEtreModifiee()) { // Vérifie le statut (doit être SOUMISE)
                candidaturesModifiables.add(c);
            }
        }
        
        if (candidaturesModifiables.isEmpty()) {
            affichage.afficherMessage("Aucune candidature modifiable.");
            affichage.afficherMessage("Les candidatures ne peuvent être modifiées que tant qu'elles");
            affichage.afficherMessage("n'ont pas été traitées par un agent STPM.");
            return;
        }
        
        // Affichage des candidatures modifiables
        affichage.afficherMessage("Candidatures modifiables (statut: " + StatutCandidature.SOUMISE + ") :");
        affichage.afficherListeCandidatures(candidaturesModifiables);
        
        int id = saisie.lireEntier("ID de la candidature à modifier : ");
        
        // Validation de l'ID
        Candidature candidature = null;
        for (Candidature c : candidaturesModifiables) {
            if (c.getId() == id) {
                candidature = c;
                break;
            }
        }
        
        if (candidature == null) {
            affichage.afficherErreur("Candidature introuvable ou non modifiable.");
            return;
        }
        
        // Affichage des valeurs actuelles
        affichage.afficherSousTitre("Modification de la candidature #" + candidature.getId());
        affichage.afficherMessage("Valeurs actuelles :");
        affichage.afficherCandidature(candidature);
        affichage.afficherMessage("");
        
        // Saisie des nouvelles valeurs
        String nouvelleDescription = saisie.lireChaineNonVide("Nouvelle description du projet : ");
        double nouveauCout = saisie.lireDouble("Nouveau coût estimé ($) : ");
        java.time.LocalDate nouvelleDateDebut = saisie.lireDate("Nouvelle date de début prévue");
        java.time.LocalDate nouvelleDateFin = saisie.lireDate("Nouvelle date de fin prévue");
        
        // Validation des nouvelles dates
        if (nouvelleDateDebut.isAfter(nouvelleDateFin)) {
            affichage.afficherErreur("La date de début doit être antérieure à la date de fin.");
            return;
        }
        
        // Récapitulatif des modifications
        affichage.afficherSousTitre("Récapitulatif des modifications");
        affichage.afficherMessage("Nouvelle description : " + nouvelleDescription);
        affichage.afficherMessage("Nouveau coût : " + nouveauCout + " $");
        
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        affichage.afficherMessage("Nouvelle date début : " + nouvelleDateDebut.format(dateFormatter));
        affichage.afficherMessage("Nouvelle date fin : " + nouvelleDateFin.format(dateFormatter));
        
        // Confirmation et application
        if (saisie.confirmer("Confirmer les modifications ?")) {
            if (gestionnaireProjets.modifierCandidature(id, nouvelleDescription, nouveauCout, nouvelleDateDebut, nouvelleDateFin)) {
                affichage.afficherSucces("Candidature modifiée avec succès !");
            } else {
                affichage.afficherErreur("Impossible de modifier cette candidature.");
            }
        } else {
            affichage.afficherMessage("Modifications annulées.");
        }
    }

    /*
        Permet d'annuler une candidature existante
     */
    private void annulerCandidature(List<Candidature> candidatures) {
        // Filtrer les candidatures annulables
        List<Candidature> candidaturesAnnulables = new ArrayList<>();
        for (Candidature c : candidatures) {
            if (c.peutEtreAnnulee()) { // Vérifie qu'elle n'est pas encore approuvée
                candidaturesAnnulables.add(c);
            }
        }
        
        if (candidaturesAnnulables.isEmpty()) {
            affichage.afficherMessage("Aucune candidature annulable.");
            affichage.afficherMessage("Les candidatures ne peuvent être annulées que tant qu'elles");
            affichage.afficherMessage("n'ont pas été approuvées par un agent STPM.");
            return;
        }
        
        // Affichage des candidatures annulables
        affichage.afficherMessage("Candidatures annulables (statut: " + StatutCandidature.SOUMISE + ") :");
        affichage.afficherListeCandidatures(candidaturesAnnulables);
        
        int id = saisie.lireEntier("ID de la candidature à annuler : ");
        
        // Validation de l'ID
        Candidature candidature = null;
        for (Candidature c : candidaturesAnnulables) {
            if (c.getId() == id) {
                candidature = c;
                break;
            }
        }
        
        if (candidature == null) {
            affichage.afficherErreur("Candidature introuvable ou non annulable.");
            return;
        }
        
        // Affichage de la candidature à annuler
        affichage.afficherSousTitre("Annulation de la candidature #" + candidature.getId());
        affichage.afficherMessage("Candidature à annuler :");
        affichage.afficherCandidature(candidature);
        affichage.afficherMessage("");
        
        // Confirmation o/n
        if (saisie.confirmer("Êtes-vous sûr de vouloir annuler cette candidature ?")) {
            if (gestionnaireProjets.annulerCandidature(id)) {
                affichage.afficherSucces("Candidature annulée avec succès !");
                affichage.afficherMessage("La candidature a été marquée comme annulée.");
            } else {
                affichage.afficherErreur("Impossible d'annuler cette candidature.");
            }
        } else {
            affichage.afficherMessage("Annulation annulée.");
        }
    }
}