package ca.udem.maville.entity;

import ca.udem.maville.modele.Priorite;
import ca.udem.maville.modele.StatutProjet;
import ca.udem.maville.modele.TypeTravaux;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA pour les projets
 */
@Entity
@Table(name = "projets")
public class ProjetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidature_id")
    private CandidatureEntity candidature;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "projet_problemes",
        joinColumns = @JoinColumn(name = "projet_id"),
        inverseJoinColumns = @JoinColumn(name = "probleme_id")
    )
    private List<ProblemeEntity> problemes = new ArrayList<>();
    
    @Column(nullable = false)
    private String localisation;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutProjet statut;
    
    @Enumerated(EnumType.STRING)
    private Priorite priorite;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_travail")
    private TypeTravaux typeTravail;
    
    @Column(name = "date_debut_prevue")
    private LocalDate dateDebutPrevue;
    
    @Column(name = "date_fin_prevue")
    private LocalDate dateFinPrevue;
    
    @Column(name = "date_debut_reelle")
    private LocalDate dateDebutReelle;
    
    @Column(name = "date_fin_reelle")
    private LocalDate dateFinReelle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestataire_id", nullable = false)
    private PrestataireEntity prestataire;
    
    @Column(name = "description_projet", columnDefinition = "TEXT")
    private String descriptionProjet;
    
    private Double cout;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "derniere_mise_a_jour")
    private LocalDateTime derniereMiseAJour;
    
    @Column(name = "nombre_rapports")
    private Integer nombreRapports = 0;
    
    public ProjetEntity() {}
    
    public ProjetEntity(CandidatureEntity candidature, List<ProblemeEntity> problemes,
                       PrestataireEntity prestataire) {
        this.candidature = candidature;
        this.problemes = new ArrayList<>(problemes);
        this.prestataire = prestataire;
        this.descriptionProjet = candidature.getDescriptionProjet();
        this.cout = candidature.getCoutEstime();
        this.dateDebutPrevue = candidature.getDateDebutPrevue();
        this.dateFinPrevue = candidature.getDateFinPrevue();
        this.dateCreation = LocalDateTime.now();
        
        // Déterminer localisation et type depuis les problèmes
        if (!problemes.isEmpty()) {
            ProblemeEntity premierProbleme = problemes.get(0);
            this.localisation = premierProbleme.getLieu();
            this.typeTravail = premierProbleme.getTypeProbleme();
            this.priorite = premierProbleme.getPriorite();
        }
        
        this.statut = StatutProjet.EN_COURS;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public CandidatureEntity getCandidature() { return candidature; }
    public void setCandidature(CandidatureEntity candidature) { this.candidature = candidature; }
    
    public List<ProblemeEntity> getProblemes() { return problemes; }
    public void setProblemes(List<ProblemeEntity> problemes) { this.problemes = problemes; }
    
    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }
    
    public StatutProjet getStatut() { return statut; }
    public void setStatut(StatutProjet statut) { this.statut = statut; }
    
    public Priorite getPriorite() { return priorite; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }
    
    public TypeTravaux getTypeTravail() { return typeTravail; }
    public void setTypeTravail(TypeTravaux typeTravail) { this.typeTravail = typeTravail; }
    
    public LocalDate getDateDebutPrevue() { return dateDebutPrevue; }
    public void setDateDebutPrevue(LocalDate dateDebutPrevue) { this.dateDebutPrevue = dateDebutPrevue; }
    
    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }
    
    public LocalDate getDateDebutReelle() { return dateDebutReelle; }
    public void setDateDebutReelle(LocalDate dateDebutReelle) { this.dateDebutReelle = dateDebutReelle; }
    
    public LocalDate getDateFinReelle() { return dateFinReelle; }
    public void setDateFinReelle(LocalDate dateFinReelle) { this.dateFinReelle = dateFinReelle; }
    
    public PrestataireEntity getPrestataire() { return prestataire; }
    public void setPrestataire(PrestataireEntity prestataire) { this.prestataire = prestataire; }
    
    public String getDescriptionProjet() { return descriptionProjet; }
    public void setDescriptionProjet(String descriptionProjet) { this.descriptionProjet = descriptionProjet; }
    
    public Double getCout() { return cout; }
    public void setCout(Double cout) { this.cout = cout; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDerniereMiseAJour() { return derniereMiseAJour; }
    public void setDerniereMiseAJour(LocalDateTime derniereMiseAJour) { this.derniereMiseAJour = derniereMiseAJour; }
    
    public Integer getNombreRapports() { return nombreRapports; }
    public void setNombreRapports(Integer nombreRapports) { this.nombreRapports = nombreRapports; }
}

