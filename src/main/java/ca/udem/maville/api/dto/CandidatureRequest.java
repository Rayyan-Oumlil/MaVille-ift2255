package ca.udem.maville.api.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour la soumission d'une candidature
 */
public class CandidatureRequest {
    @NotBlank(message = "Le NEQ est requis")
    @Pattern(regexp = "^\\d{10}$", message = "Le NEQ doit contenir exactement 10 chiffres")
    private String prestataireId;
    
    @NotBlank(message = "La description du projet est requise")
    @Size(min = 10, message = "La description doit contenir au moins 10 caractères")
    private String description;
    
    @NotNull(message = "La date de début est requise")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDate dateDebut;
    
    @NotNull(message = "La date de fin est requise")
    private LocalDate dateFin;
    
    @Min(value = 0, message = "Le coût doit être positif")
    private Double cout;
    
    private List<Integer> problemesVises;
    
    public CandidatureRequest() {}
    
    public String getPrestataireId() {
        return prestataireId;
    }
    
    public void setPrestataireId(String prestataireId) {
        this.prestataireId = prestataireId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }
    
    public LocalDate getDateFin() {
        return dateFin;
    }
    
    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }
    
    public Double getCout() {
        return cout;
    }
    
    public void setCout(Double cout) {
        this.cout = cout;
    }
    
    public List<Integer> getProblemesVises() {
        return problemesVises;
    }
    
    public void setProblemesVises(List<Integer> problemesVises) {
        this.problemesVises = problemesVises;
    }
}

