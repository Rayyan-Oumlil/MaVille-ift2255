package ca.udem.maville.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA pour les prestataires
 */
@Entity
@Table(name = "prestataires", uniqueConstraints = {
    @UniqueConstraint(columnNames = "numero_entreprise")
})
public class PrestataireEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_entreprise", nullable = false, unique = true)
    private String numeroEntreprise;
    
    @Column(name = "nom_entreprise", nullable = false)
    private String nomEntreprise;
    
    @Column(name = "contact_nom")
    private String contactNom;
    
    private String telephone;
    
    private String email;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CandidatureEntity> candidatures = new ArrayList<>();
    
    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjetEntity> projets = new ArrayList<>();
    
    public PrestataireEntity() {}
    
    public PrestataireEntity(String numeroEntreprise, String nomEntreprise, String contactNom, 
                            String telephone, String email) {
        this.numeroEntreprise = numeroEntreprise;
        this.nomEntreprise = nomEntreprise;
        this.contactNom = contactNom;
        this.telephone = telephone;
        this.email = email;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNumeroEntreprise() { return numeroEntreprise; }
    public void setNumeroEntreprise(String numeroEntreprise) { this.numeroEntreprise = numeroEntreprise; }
    
    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }
    
    public String getContactNom() { return contactNom; }
    public void setContactNom(String contactNom) { this.contactNom = contactNom; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public List<CandidatureEntity> getCandidatures() { return candidatures; }
    public void setCandidatures(List<CandidatureEntity> candidatures) { this.candidatures = candidatures; }
    
    public List<ProjetEntity> getProjets() { return projets; }
    public void setProjets(List<ProjetEntity> projets) { this.projets = projets; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}

