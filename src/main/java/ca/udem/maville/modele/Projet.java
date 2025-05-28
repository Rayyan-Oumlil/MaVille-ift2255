package ca.udem.maville.modele;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


/*
    Représente un projet de travaux créé à partir d'une candidature approuvée.
    Combine les infos de la candidature avec les détails des problèmes à traiter.
 */

public class Projet {
    private static final AtomicInteger compteurId = new AtomicInteger(1);
    
    private int id;
    private List<Integer> problemesVises; // IDs des problèmes
    private String localisation;
    private StatutProjet statut;
    private Priorite priorite;
    private TypeTravaux typeTravail;
    private LocalDate dateDebutPrevue;
    private LocalDate dateFinPrevue;
    private LocalDate dateDebutReelle;   // Quand les travaux ont vraiment commencé
    private LocalDate dateFinReelle;     // Quand les travaux se sont terminés
    private Prestataire prestataire;
    private String descriptionProjet;
    private double cout;
    private LocalDateTime dateCreation;
    private LocalDateTime derniereMiseAJour;
    private int nombreRapports;

/*
        Constructeur - crée un projet à partir d'une candidature approuvée
        Récupère automatiquement les infos des problèmes (localisation, type, priorité )
*/

    public Projet(Candidature candidature, List<Probleme> problemes) {
        this.id = compteurId.getAndIncrement();
        // Copier les infos de la candidature
        this.problemesVises = new ArrayList<>(candidature.getProblemesVises());
        this.prestataire = candidature.getPrestataire();
        this.descriptionProjet = candidature.getDescriptionProjet();
        this.cout = candidature.getCoutEstime();
        this.dateDebutPrevue = candidature.getDateDebutPrevue();
        this.dateFinPrevue = candidature.getDateFinPrevue();
        this.statut = StatutProjet.APPROUVE;
        this.dateCreation = LocalDateTime.now();
        this.derniereMiseAJour = LocalDateTime.now();
        
        // Dériver les autres propriétés des problèmes
        if (!problemes.isEmpty()) {
            this.localisation = problemes.get(0).getLieu();
            this.typeTravail = problemes.get(0).getTypeProbleme();
            this.priorite = problemes.stream()   // Prendre la priorité la plus élevée des problèmes
                    .map(Probleme::getPriorite)
                    .max(Enum::compareTo)
                    .orElse(Priorite.MOYENNE);
        }
        this.nombreRapports = problemes.size();
    }

    // Getters et Setters
    public int getId() { return id; }
    
    public List<Integer> getProblemesVises() { return new ArrayList<>(problemesVises); }
    
    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }
    
    public StatutProjet getStatut() { return statut; }
    public void setStatut(StatutProjet statut) { 
        this.statut = statut; 
        this.derniereMiseAJour = LocalDateTime.now();
    }
    
    public Priorite getPriorite() { return priorite; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }
    
    public TypeTravaux getTypeTravail() { return typeTravail; }
    public void setTypeTravail(TypeTravaux typeTravail) { this.typeTravail = typeTravail; }
    
    public LocalDate getDateDebutPrevue() { return dateDebutPrevue; }
    public void setDateDebutPrevue(LocalDate dateDebutPrevue) { 
        this.dateDebutPrevue = dateDebutPrevue; 
        this.derniereMiseAJour = LocalDateTime.now();
    }
    
    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { 
        this.dateFinPrevue = dateFinPrevue; 
        this.derniereMiseAJour = LocalDateTime.now();
    }
    
    public LocalDate getDateDebutReelle() { return dateDebutReelle; }
    public void setDateDebutReelle(LocalDate dateDebutReelle) { 
        this.dateDebutReelle = dateDebutReelle; 
        this.derniereMiseAJour = LocalDateTime.now();
    }
    
    public LocalDate getDateFinReelle() { return dateFinReelle; }
    public void setDateFinReelle(LocalDate dateFinReelle) { 
        this.dateFinReelle = dateFinReelle; 
        this.derniereMiseAJour = LocalDateTime.now();
    }
    
    public Prestataire getPrestataire() { return prestataire; }
    public void setPrestataire(Prestataire prestataire) { this.prestataire = prestataire; }
    
    public String getDescriptionProjet() { return descriptionProjet; }
    public void setDescriptionProjet(String descriptionProjet) { this.descriptionProjet = descriptionProjet; }
    
    public double getCout() { return cout; }
    public void setCout(double cout) { this.cout = cout; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    
    public LocalDateTime getDerniereMiseAJour() { return derniereMiseAJour; }
    
    public int getNombreRapports() { return nombreRapports; }
    public void setNombreRapports(int nombreRapports) { this.nombreRapports = nombreRapports; }

    /*
        Démarre le projet - passe de APROUVE à EN COURS
     */
    public void demarrer() {
        if (statut == StatutProjet.APPROUVE) {
            this.statut = StatutProjet.EN_COURS;
            this.dateDebutReelle = LocalDate.now();
            this.derniereMiseAJour = LocalDateTime.now();
        }
    }

    /*
        Suspend temporairement le projet
     */
    public void suspendre() {
        if (statut == StatutProjet.EN_COURS) {
            this.statut = StatutProjet.SUSPENDU;
            this.derniereMiseAJour = LocalDateTime.now();
        }
    }
/*
        Reprend un projet suspendu
*/
    public void reprendre() {
        if (statut == StatutProjet.SUSPENDU) {
            this.statut = StatutProjet.EN_COURS;
            this.derniereMiseAJour = LocalDateTime.now();
        }
    }

/*
        Termine le projet
*/
    public void terminer() {
        if (statut == StatutProjet.EN_COURS) {
            this.statut = StatutProjet.TERMINE;
            this.dateFinReelle = LocalDate.now();
            this.derniereMiseAJour = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "Projet #" + id + " - " + typeTravail + " à " + localisation + 
               " (" + statut + ") - " + prestataire.getNomEntreprise();
    }
}