package ca.udem.maville.entity;

import jakarta.persistence.*;

/**
 * Entité JPA pour les abonnements
 */
@Entity
@Table(name = "abonnements")
public class AbonnementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "resident_email", nullable = false)
    private String residentEmail;
    
    @Column(nullable = false)
    private String type; // "QUARTIER", "TYPE_TRAVAUX", etc.
    
    @Column(nullable = false)
    private String valeur; // Valeur de l'abonnement (nom du quartier, type de travaux, etc.)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private ResidentEntity resident;
    
    public AbonnementEntity() {}
    
    public AbonnementEntity(String residentEmail, String type, String valeur) {
        this.residentEmail = residentEmail;
        this.type = type;
        this.valeur = valeur;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getResidentEmail() { return residentEmail; }
    public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }
    
    public ResidentEntity getResident() { return resident; }
    public void setResident(ResidentEntity resident) { this.resident = resident; }
}

