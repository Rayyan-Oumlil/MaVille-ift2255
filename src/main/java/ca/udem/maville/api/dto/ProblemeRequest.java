package ca.udem.maville.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la création d'un problème
 */
public class ProblemeRequest {
    @NotBlank(message = "Le lieu est requis")
    @Size(min = 3, message = "Le lieu doit contenir au moins 3 caractères")
    private String lieu;
    
    @NotBlank(message = "La description est requise")
    @Size(min = 10, message = "La description doit contenir au moins 10 caractères")
    private String description;
    
    @Email(message = "L'email du résident n'est pas valide")
    private String residentId;
    
    public ProblemeRequest() {}
    
    public ProblemeRequest(String lieu, String description, String residentId) {
        this.lieu = lieu;
        this.description = description;
        this.residentId = residentId;
    }
    
    // Getters et Setters
    public String getLieu() {
        return lieu;
    }
    
    public void setLieu(String lieu) {
        this.lieu = lieu;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getResidentId() {
        return residentId;
    }
    
    public void setResidentId(String residentId) {
        this.residentId = residentId;
    }
}

