package ca.udem.maville.entity;

import ca.udem.maville.modele.Priorite;
import ca.udem.maville.modele.TypeTravaux;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA pour les problèmes
 */
@Entity
@Table(name = "problemes")
public class ProblemeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String lieu;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_probleme", nullable = false)
    private TypeTravaux typeProbleme;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declarant_id", nullable = false)
    private ResidentEntity declarant;
    
    @Column(name = "date_signalement", nullable = false)
    private LocalDateTime dateSignalement;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priorite priorite;
    
    @Column(nullable = false)
    private boolean resolu = false;
    
    @ManyToMany(mappedBy = "problemes")
    private List<CandidatureEntity> candidatures = new ArrayList<>();
    
    @ManyToMany(mappedBy = "problemes")
    private List<ProjetEntity> projets = new ArrayList<>();
    
    public ProblemeEntity() {}
    
    public ProblemeEntity(String lieu, TypeTravaux typeProbleme, String description, 
                         ResidentEntity declarant, Priorite priorite) {
        this.lieu = lieu;
        this.typeProbleme = typeProbleme;
        this.description = description;
        this.declarant = declarant;
        this.priorite = priorite;
        this.dateSignalement = LocalDateTime.now();
        this.resolu = false;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    
    public TypeTravaux getTypeProbleme() { return typeProbleme; }
    public void setTypeProbleme(TypeTravaux typeProbleme) { this.typeProbleme = typeProbleme; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ResidentEntity getDeclarant() { return declarant; }
    public void setDeclarant(ResidentEntity declarant) { this.declarant = declarant; }
    
    public LocalDateTime getDateSignalement() { return dateSignalement; }
    public void setDateSignalement(LocalDateTime dateSignalement) { this.dateSignalement = dateSignalement; }
    
    public Priorite getPriorite() { return priorite; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }
    
    public boolean isResolu() { return resolu; }
    public void setResolu(boolean resolu) { this.resolu = resolu; }
    
    public List<CandidatureEntity> getCandidatures() { return candidatures; }
    public void setCandidatures(List<CandidatureEntity> candidatures) { this.candidatures = candidatures; }
    
    public List<ProjetEntity> getProjets() { return projets; }
    public void setProjets(List<ProjetEntity> projets) { this.projets = projets; }
}

