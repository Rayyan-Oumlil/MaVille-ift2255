package ca.udem.maville.service;

import ca.udem.maville.modele.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/*
    Service qui gère les candidatures des prestataires et les projets de travaux.
    Sépare la logique des candidatures (soumises) et des projets (approuvés).
 */
public class GestionnaireProjets {
    // Stockage en mémoire pour le prototype
    private List<Candidature> candidatures;
    private List<Projet> projets;

    public GestionnaireProjets() {
        this.candidatures = new ArrayList<>();
        this.projets = new ArrayList<>();
    }

    /*
        Crée une nouvelle candidature soumise par un prestataire.
        La candidature est automatiquement en statut SOUMISE.
     */
    public Candidature soumettreCandidat(Prestataire prestataire, List<Integer> problemesVises, 
                                        String description, double cout, 
                                        java.time.LocalDate dateDebut, java.time.LocalDate dateFin) {
        Candidature candidature = new Candidature(prestataire, problemesVises, description, cout, dateDebut, dateFin);
        candidatures.add(candidature);
        return candidature;
    }

    /*
        Modifie une candidature existante.
        Seulement possible si la candidature n'a pas encore été traitée .
     */
    public boolean modifierCandidature(int candidatureId, String nouvelleDescription, 
                                     double nouveauCout, java.time.LocalDate nouvelleDateDebut, 
                                     java.time.LocalDate nouvelleDateFin) {
        Candidature candidature = trouverCandidatureParId(candidatureId);
        if (candidature != null && candidature.peutEtreModifiee()) {
            candidature.setDescriptionProjet(nouvelleDescription);
            candidature.setCoutEstime(nouveauCout);
            candidature.setDateDebutPrevue(nouvelleDateDebut);
            candidature.setDateFinPrevue(nouvelleDateFin);
            return true;
        }
        return false;
    }

    /*
        Annule une candidature.
        Seulement possible si elle n'a pas encore été approuvée.
     */
    public boolean annulerCandidature(int candidatureId) {
        Candidature candidature = trouverCandidatureParId(candidatureId);
        if (candidature != null && candidature.peutEtreAnnulee()) {
            candidature.setStatut(StatutCandidature.ANNULEE);
            return true;
        }
        return false;
    }

    /*
        Retourne toutes les candidatures.
        Utilisé pour rechercher les candidatures d'un prestataire spécifique.
     */
    public List<Candidature> listerCandidatures() {
        return new ArrayList<>(candidatures);
    }

    /*
        Retourne les projets en cours ou approuvés.
        Utilisé pour afficher les travaux actifs dans les arrondissements.
     */
    public List<Projet> listerProjetsActifs() {
        return projets.stream()
                .filter(p -> p.getStatut() == StatutProjet.EN_COURS || p.getStatut() == StatutProjet.APPROUVE)
                .collect(Collectors.toList());
    }

    /*
        Trouve une candidature par son ID.
        Utilisé pour les modifications et annulations.
     */
    public Candidature trouverCandidatureParId(int id) {
        return candidatures.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }
}