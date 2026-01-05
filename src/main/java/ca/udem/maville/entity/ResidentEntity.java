package ca.udem.maville.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA pour les résidents
 */
@Entity
@Table(name = "residents", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class ResidentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String prenom;
    
    @Column(nullable = false)
    private String nom;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    private String telephone;
    
    private String adresse;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    @OneToMany(mappedBy = "declarant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemeEntity> problemes = new ArrayList<>();
    
    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AbonnementEntity> abonnements = new ArrayList<>();
    
    public ResidentEntity() {}
    
    public ResidentEntity(String prenom, String nom, String email, String telephone, String adresse) {
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public List<ProblemeEntity> getProblemes() { return problemes; }
    public void setProblemes(List<ProblemeEntity> problemes) { this.problemes = problemes; }
    
    public List<AbonnementEntity> getAbonnements() { return abonnements; }
    public void setAbonnements(List<AbonnementEntity> abonnements) { this.abonnements = abonnements; }
    
    public String getNomComplet() {
        return prenom + " " + nom;
    }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}

