package ca.udem.maville.modele;

import java.time.LocalDateTime;

public class Notification {
    private String residentEmail;
    private String message;
    private LocalDateTime dateCreation;
    private boolean lu;
    private String typeChangement; // NOUVEAU_PROJET, STATUT_CHANGE, PRIORITE_CHANGE
    private int projetId;
    private String quartier;
    
    public Notification() {
        // Constructeur vide pour JSON
    }
    
    public Notification(String residentEmail, String message, String typeChangement, 
                       int projetId, String quartier) {
        this.residentEmail = residentEmail;
        this.message = message;
        this.dateCreation = LocalDateTime.now();
        this.lu = false;
        this.typeChangement = typeChangement;
        this.projetId = projetId;
        this.quartier = quartier;
    }
    
    // Getters et Setters
    public String getResidentEmail() { return residentEmail; }
    public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
    
    public String getTypeChangement() { return typeChangement; }
    public void setTypeChangement(String typeChangement) { this.typeChangement = typeChangement; }
    
    public int getProjetId() { return projetId; }
    public void setProjetId(int projetId) { this.projetId = projetId; }
    
    public String getQuartier() { return quartier; }
    public void setQuartier(String quartier) { this.quartier = quartier; }
}

