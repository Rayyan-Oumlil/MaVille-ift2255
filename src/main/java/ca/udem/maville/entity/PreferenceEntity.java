package ca.udem.maville.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA pour les préférences de notification des utilisateurs
 */
@Entity
@Table(name = "preferences", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "numero_entreprise")
})
public class PreferenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String email; // Pour les résidents
    
    @Column(name = "numero_entreprise", unique = true)
    private String neq; // Pour les prestataires
    
    @Column(name = "notifications_email", nullable = false)
    private Boolean notificationsEmail = true;
    
    @Column(name = "notifications_quartier", nullable = false)
    private Boolean notificationsQuartier = true;
    
    @ElementCollection
    @CollectionTable(name = "preference_notification_types", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "type")
    private List<String> notificationsType = new ArrayList<>();
    
    @Column(name = "date_creation", nullable = false)
    private java.time.LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private java.time.LocalDateTime dateModification;
    
    public PreferenceEntity() {
        this.dateCreation = java.time.LocalDateTime.now();
    }
    
    public PreferenceEntity(String email) {
        this();
        this.email = email;
    }
    
    public PreferenceEntity(String email, String neq) {
        this();
        this.email = email;
        this.neq = neq;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getNeq() { return neq; }
    public void setNeq(String neq) { this.neq = neq; }
    
    public Boolean getNotificationsEmail() { return notificationsEmail; }
    public void setNotificationsEmail(Boolean notificationsEmail) { 
        this.notificationsEmail = notificationsEmail;
        this.dateModification = java.time.LocalDateTime.now();
    }
    
    public Boolean getNotificationsQuartier() { return notificationsQuartier; }
    public void setNotificationsQuartier(Boolean notificationsQuartier) { 
        this.notificationsQuartier = notificationsQuartier;
        this.dateModification = java.time.LocalDateTime.now();
    }
    
    public List<String> getNotificationsType() { return notificationsType; }
    public void setNotificationsType(List<String> notificationsType) { 
        this.notificationsType = notificationsType != null ? notificationsType : new ArrayList<>();
        this.dateModification = java.time.LocalDateTime.now();
    }
    
    public java.time.LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(java.time.LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public java.time.LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(java.time.LocalDateTime dateModification) { this.dateModification = dateModification; }
}
