package ca.udem.maville.modele;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/*
    Représente une candidature soumise par un prestataire pour traiter un ou plusieurs problèmes.
    Contient toutes les infos du formulaire : entreprise, projet, coût, dates.
 */
public class Candidature {
    // Compteur pour générer des IDs uniques automatiquement
    private static final AtomicInteger compteurId = new AtomicInteger(1);
    
    private int id;
    private Prestataire prestataire;
    private List<Integer> problemesVises; // IDs des problèmes que le prestataire veut traiter
    private String descriptionProjet;
    private double coutEstime;
    private LocalDate dateDebutPrevue;
    private LocalDate dateFinPrevue;
    private LocalDateTime dateDepot;
    private StatutCandidature statut;
    private String commentaireRejet; // Si la candidature est rejetée

    public Candidature() {
        this.problemesVises = new ArrayList<>();
    }

    /*
        Constructeur - crée une nouvelle candidature avec statut SOUMISE par défaut
     */
    public Candidature(Prestataire prestataire, List<Integer> problemesVises, 
                      String descriptionProjet, double coutEstime, 
                      LocalDate dateDebutPrevue, LocalDate dateFinPrevue) {
        this.id = compteurId.getAndIncrement(); // ID auto-généré
        this.prestataire = prestataire;
        this.problemesVises = new ArrayList<>(problemesVises); // Copie pour éviter les modifications externes
        this.descriptionProjet = descriptionProjet;
        this.coutEstime = coutEstime;
        this.dateDebutPrevue = dateDebutPrevue;
        this.dateFinPrevue = dateFinPrevue;
        this.dateDepot = LocalDateTime.now(); // Date actuelle
        this.statut = StatutCandidature.SOUMISE; // Statut initial
    }
    /**
 * Synchronise le compteur d'ID avec l'ID maximum existant
 * Appelé après le chargement depuis JSON pour éviter les doublons d'ID
 */
public static void synchroniserCompteurId(List<Candidature> candidaturesExistantes) {
    if (candidaturesExistantes != null && !candidaturesExistantes.isEmpty()) {
        int maxId = candidaturesExistantes.stream()
            .mapToInt(Candidature::getId)
            .max()
            .orElse(0);
        compteurId.set(maxId + 1);
    }
}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public Prestataire getPrestataire() { return prestataire; }
    public void setPrestataire(Prestataire prestataire) { this.prestataire = prestataire; }
    
    // Retourne une copie pour protéger la liste interne
    public List<Integer> getProblemesVises() { 
        return problemesVises != null ? new ArrayList<>(problemesVises) : new ArrayList<>(); 
    }
    public void setProblemesVises(List<Integer> problemesVises) { 
        this.problemesVises = problemesVises != null ? new ArrayList<>(problemesVises) : new ArrayList<>(); 
    }
    
    public String getDescriptionProjet() { return descriptionProjet; }
    public void setDescriptionProjet(String descriptionProjet) { 
        this.descriptionProjet = descriptionProjet; 
    }
    
    public double getCoutEstime() { return coutEstime; }
    public void setCoutEstime(double coutEstime) { this.coutEstime = coutEstime; }
    
    public LocalDate getDateDebutPrevue() { return dateDebutPrevue; }
    public void setDateDebutPrevue(LocalDate dateDebutPrevue) { 
        this.dateDebutPrevue = dateDebutPrevue; 
    }
    
    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { 
        this.dateFinPrevue = dateFinPrevue; 
    }
    
    public LocalDateTime getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDateTime dateDepot) { this.dateDepot = dateDepot; }
    
    public StatutCandidature getStatut() { return statut; }
    public void setStatut(StatutCandidature statut) { this.statut = statut; }
    
    public String getCommentaireRejet() { return commentaireRejet; }
    public void setCommentaireRejet(String commentaireRejet) { 
        this.commentaireRejet = commentaireRejet; 
    }

    /*
        Vérifie si la candidature peut être modifiée
        Seulement possible si elle est encore en statut SOUMISE
     */
    public boolean peutEtreModifiee() {
        return statut == StatutCandidature.SOUMISE;
    }

    /*
        Vérifie si la candidature peut être annulée
        Seulement possible si elle n'a pas encore été approuvée
     */
    public boolean peutEtreAnnulee() {
        return statut == StatutCandidature.SOUMISE;
    }

    // Affichage simple pour les listes
    @Override
    public String toString() {
        return "Candidature #" + id + " par " + 
               (prestataire != null ? prestataire.getNomEntreprise() : "N/A") + 
               " - " + coutEstime + "$ (" + 
               (statut != null ? statut : "N/A") + ")";
    }
}