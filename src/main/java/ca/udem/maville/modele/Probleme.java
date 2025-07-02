package ca.udem.maville.modele;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

/*
    Représente un problème signalé par un résident.
    Contient les 4 infos requises : lieu, type, déclarant, description.
 */
public class Probleme {
    // Compteur pour IDs auto-générés
    private static final AtomicInteger compteurId = new AtomicInteger(1);
    
    private int id;
    private String lieu;                 
    private TypeTravaux typeProbleme;
    private String description;
    private Resident declarant;         
    private LocalDateTime dateSignalement;
    private Priorite priorite;
    private boolean resolu;              

    // Constructeur par défaut NÉCESSAIRE pour Jackson
    public Probleme() {
        // Constructeur vide pour la désérialisation JSON
    }

    /*
        Constructeur - initialise avec priorité MOYENNE par défaut
     */
    public Probleme(String lieu, TypeTravaux typeProbleme, String description, Resident declarant) {
        this.id = compteurId.getAndIncrement();
        this.lieu = lieu;
        this.typeProbleme = typeProbleme;
        this.description = description;
        this.declarant = declarant;
        this.dateSignalement = LocalDateTime.now();
        this.priorite = Priorite.MOYENNE; // Priorité par défaut
        this.resolu = false;
    }

    // Getters et Setters 
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    
    public TypeTravaux getTypeProbleme() { return typeProbleme; }
    public void setTypeProbleme(TypeTravaux typeProbleme) { this.typeProbleme = typeProbleme; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public Resident getDeclarant() { return declarant; }
    public void setDeclarant(Resident declarant) { this.declarant = declarant; }
    
    public LocalDateTime getDateSignalement() { return dateSignalement; }
    public void setDateSignalement(LocalDateTime dateSignalement) { this.dateSignalement = dateSignalement; }
    
    public Priorite getPriorite() { return priorite; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }
    
    public boolean isResolu() { return resolu; }
    public void setResolu(boolean resolu) { this.resolu = resolu; }

    /**
     * Synchronise le compteur d'ID avec l'ID maximum existant
     * Appelé après le chargement depuis JSON pour éviter les doublons d'ID
     */
    public static void synchroniserCompteurId(List<Probleme> problemesExistants) {
        if (problemesExistants != null && !problemesExistants.isEmpty()) {
            int maxId = problemesExistants.stream()
                .mapToInt(Probleme::getId)
                .max()
                .orElse(0);
            compteurId.set(maxId + 1);
        }
    }

    @Override
    public String toString() {
        return "Problème #" + id + " - " + typeProbleme + " à " + lieu + " (Priorité: " + priorite + ")";
    }
}