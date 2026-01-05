package ca.udem.maville.modele;

import java.time.LocalDateTime;

/**
 * Notification améliorée conforme au DM3
 * Supporte les notifications pour résidents, prestataires et STPM
 */
public class Notification {
    private String destinataire; // Email résident, NEQ prestataire, ou "STPM"
    private String typeDestinataire; // "RESIDENT", "PRESTATAIRE", "STPM"
    private String message;
    private LocalDateTime dateCreation;
    private boolean lu;
    private String typeChangement; // NOUVEAU_PROJET, STATUT_CHANGE, PRIORITE_CHANGE, etc.
    private int projetId;
    private int problemeId; // Nouveau: pour les notifications de problèmes
    private String quartier;
    private String priorite; // Nouveau: pour les notifications de priorité
    
    public Notification() {
    }
    
    // Constructeur pour notifications résidents (existant)
    public Notification(String residentEmail, String message, String typeChangement, 
                       int projetId, String quartier) {
        this.destinataire = residentEmail;
        this.typeDestinataire = "RESIDENT";
        this.message = message;
        this.dateCreation = LocalDateTime.now();
        this.lu = false;
        this.typeChangement = typeChangement;
        this.projetId = projetId;
        this.quartier = quartier;
    }
    
    // Nouveau constructeur pour notifications prestataires
    public Notification(String prestataireNeq, String message, String typeChangement, 
                       int problemeId, String quartier, String priorite, boolean isPrestataire) {
        this.destinataire = prestataireNeq;
        this.typeDestinataire = "PRESTATAIRE";
        this.message = message;
        this.dateCreation = LocalDateTime.now();
        this.lu = false;
        this.typeChangement = typeChangement;
        this.problemeId = problemeId;
        this.quartier = quartier;
        this.priorite = priorite;
    }
    
    // Nouveau constructeur pour notifications STPM
    public static Notification pourStpm(String message, String typeChangement, 
                                       int projetOuProblemeId, String quartier) {
        Notification notif = new Notification();
        notif.destinataire = "STPM";
        notif.typeDestinataire = "STPM";
        notif.message = message;
        notif.dateCreation = LocalDateTime.now();
        notif.lu = false;
        notif.typeChangement = typeChangement;
        
        if (typeChangement.contains("PROJET")) {
            notif.projetId = projetOuProblemeId;
        } else {
            notif.problemeId = projetOuProblemeId;
        }
        notif.quartier = quartier;
        return notif;
    }
    
    public String getResidentEmail() { 
    // Pour compatibilité avec l'ancien format
    if ("RESIDENT".equals(typeDestinataire)) {
        return destinataire;
    }
    return null;
}
    public void setResidentEmail(String residentEmail) { 
        this.destinataire = residentEmail;
        this.typeDestinataire = "RESIDENT";
    }
    
    public String getDestinataire() { return destinataire; }
    public void setDestinataire(String destinataire) { this.destinataire = destinataire; }
    
    public String getTypeDestinataire() { return typeDestinataire; }
    public void setTypeDestinataire(String typeDestinataire) { this.typeDestinataire = typeDestinataire; }
    
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
    
    public int getProblemeId() { return problemeId; }
    public void setProblemeId(int problemeId) { this.problemeId = problemeId; }
    
    public String getQuartier() { return quartier; }
    public void setQuartier(String quartier) { this.quartier = quartier; }
    
    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }
    
    // Méthodes utilitaires
    public boolean estPourResident() {
        return "RESIDENT".equals(typeDestinataire);
    }
    
    public boolean estPourPrestataire() {
        return "PRESTATAIRE".equals(typeDestinataire);
    }
    
    public boolean estPourStmp() {
        return "STPM".equals(typeDestinataire);
    }
    /**
 * Méthode appelée après la désérialisation JSON pour corriger les données
 */
@com.fasterxml.jackson.annotation.JsonSetter("destinataire")
public void setDestinataireFromJson(String destinataire) {
    this.destinataire = destinataire;
    
    // Si typeDestinataire n'est pas défini et destinataire contient NEQ
    if (this.typeDestinataire == null && destinataire != null && destinataire.startsWith("NEQ")) {
        this.typeDestinataire = "PRESTATAIRE";
    }
    
    // Si typeDestinataire n'est pas défini et destinataire contient @
    if (this.typeDestinataire == null && destinataire != null && destinataire.contains("@")) {
        this.typeDestinataire = "RESIDENT";
    }
    
    // Si c'est STPM
    if (this.typeDestinataire == null && "STPM".equals(destinataire)) {
        this.typeDestinataire = "STPM";
    }
}
}