package ca.udem.maville.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité JPA pour les notifications
 */
@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "type_changement")
    private String typeChangement;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(nullable = false)
    private boolean lu = false;
    
    @Column(name = "resident_email")
    private String residentEmail;
    
    @Column(name = "projet_id")
    private Long projetId;
    
    @Column(name = "type_destinataire")
    private String typeDestinataire; // "RESIDENT", "PRESTATAIRE", "STPM"
    
    @Column(name = "destinataire")
    private String destinataire; // Email ou NEQ selon le type
    
    public NotificationEntity() {}
    
    public NotificationEntity(String message, String typeChangement, String residentEmail, 
                              Long projetId, String typeDestinataire) {
        this.message = message;
        this.typeChangement = typeChangement;
        this.residentEmail = residentEmail;
        this.projetId = projetId;
        this.typeDestinataire = typeDestinataire;
        this.dateCreation = LocalDateTime.now();
        this.lu = false;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getTypeChangement() { return typeChangement; }
    public void setTypeChangement(String typeChangement) { this.typeChangement = typeChangement; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
    
    public String getResidentEmail() { return residentEmail; }
    public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }
    
    public Long getProjetId() { return projetId; }
    public void setProjetId(Long projetId) { this.projetId = projetId; }
    
    public String getTypeDestinataire() { return typeDestinataire; }
    public void setTypeDestinataire(String typeDestinataire) { this.typeDestinataire = typeDestinataire; }
    
    public String getDestinataire() { return destinataire; }
    public void setDestinataire(String destinataire) { this.destinataire = destinataire; }
    
    // Méthodes utilitaires
    public boolean estPourStmp() {
        return "STPM".equals(typeDestinataire);
    }
    
    public boolean estPourPrestataire() {
        return "PRESTATAIRE".equals(typeDestinataire);
    }
}

