package ca.udem.maville.modele;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
    Représente un résident qui signale des problèmes.
    Contient les coordonnées nécessaires pour le suivi.
 */
public class Resident {
    private String nom;
    private String prenom;
    private String telephone;
    private String email;    // Utilisé comme identifiant pour retrouver ses signalements
    private String adresse;

    // Constructeur par défaut NÉCESSAIRE pour Jackson
    public Resident() {
        // Constructeur vide pour la désérialisation JSON
    }

    public Resident(String nom, String prenom, String telephone, String email, String adresse) {
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.email = email;
        this.adresse = adresse;
    }

    // Getters et Setters 
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    /*
        Méthode utilitaire pour avoir le nom complet
        @JsonIgnore pour que Jackson ne tente pas de la sérialiser/désérialiser
     */
    @JsonIgnore
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return getNomComplet() + " (" + email + ")";
    }

    /*
        Deux résidents sont égaux s'ils ont le même email
        Utilisé pour retrouver les signalements d'un résident
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Resident resident = (Resident) obj;
        return email != null && email.equals(resident.email);
    }

    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }
}