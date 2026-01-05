package ca.udem.maville.entity;

import ca.udem.maville.modele.StatutCandidature;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA pour les candidatures
 */
@Entity
@Table(name = "candidatures")
public class CandidatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestataire_id", nullable = false)
    private PrestataireEntity prestataire;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "candidature_problemes",
        joinColumns = @JoinColumn(name = "candidature_id"),
        inverseJoinColumns = @JoinColumn(name = "probleme_id")
    )
    private List<ProblemeEntity> problemes = new ArrayList<>();
    
    @Column(name = "description_projet", columnDefinition = "TEXT")
    private String descriptionProjet;
    
    @Column(name = "cout_estime")
    private Double coutEstime;
    
    @Column(name = "date_debut_prevue")
    private LocalDate dateDebutPrevue;
    
    @Column(name = "date_fin_prevue")
    private LocalDate dateFinPrevue;
    
    @Column(name = "date_depot", nullable = false)
    private LocalDateTime dateDepot;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCandidature statut = StatutCandidature.SOUMISE;
    
    @Column(name = "commentaire_rejet", columnDefinition = "TEXT")
    private String commentaireRejet;
    
    @OneToOne(mappedBy = "candidature", cascade = CascadeType.ALL)
    private ProjetEntity projet;
    
    public CandidatureEntity() {}
    
    public CandidatureEntity(PrestataireEntity prestataire, List<ProblemeEntity> problemes,
                            String descriptionProjet, Double coutEstime,
                            LocalDate dateDebutPrevue, LocalDate dateFinPrevue) {
        this.prestataire = prestataire;
        this.problemes = new ArrayList<>(problemes);
        this.descriptionProjet = descriptionProjet;
        this.coutEstime = coutEstime;
        this.dateDebutPrevue = dateDebutPrevue;
        this.dateFinPrevue = dateFinPrevue;
        this.dateDepot = LocalDateTime.now();
        this.statut = StatutCandidature.SOUMISE;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public PrestataireEntity getPrestataire() { return prestataire; }
    public void setPrestataire(PrestataireEntity prestataire) { this.prestataire = prestataire; }
    
    public List<ProblemeEntity> getProblemes() { return problemes; }
    public void setProblemes(List<ProblemeEntity> problemes) { this.problemes = problemes; }
    
    public String getDescriptionProjet() { return descriptionProjet; }
    public void setDescriptionProjet(String descriptionProjet) { this.descriptionProjet = descriptionProjet; }
    
    public Double getCoutEstime() { return coutEstime; }
    public void setCoutEstime(Double coutEstime) { this.coutEstime = coutEstime; }
    
    public LocalDate getDateDebutPrevue() { return dateDebutPrevue; }
    public void setDateDebutPrevue(LocalDate dateDebutPrevue) { this.dateDebutPrevue = dateDebutPrevue; }
    
    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }
    
    public LocalDateTime getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDateTime dateDepot) { this.dateDepot = dateDepot; }
    
    public StatutCandidature getStatut() { return statut; }
    public void setStatut(StatutCandidature statut) { this.statut = statut; }
    
    public String getCommentaireRejet() { return commentaireRejet; }
    public void setCommentaireRejet(String commentaireRejet) { this.commentaireRejet = commentaireRejet; }
    
    public ProjetEntity getProjet() { return projet; }
    public void setProjet(ProjetEntity projet) { this.projet = projet; }
}

