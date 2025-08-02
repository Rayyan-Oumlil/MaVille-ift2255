package ca.udem.maville.modele;

import java.util.List;
import java.util.ArrayList;

/**
 * Préférences de notification pour un résident
 * Conforme au DM3: résidents peuvent modifier leurs préférences
 */
public class PreferencesNotification {
    private String residentEmail;
    private List<String> quartiers; // Quartiers d'intérêt
    private List<String> rues; // Rues d'intérêt
    private List<String> typesTravaux; // Types de travaux d'intérêt
    private String frequence; // "IMMEDIATE", "QUOTIDIEN", "HEBDOMADAIRE"
    private boolean actives; // Notifications activées ou non
    
    public PreferencesNotification() {
        this.quartiers = new ArrayList<>();
        this.rues = new ArrayList<>();
        this.typesTravaux = new ArrayList<>();
        this.frequence = "IMMEDIATE";
        this.actives = true;
    }
    
    public PreferencesNotification(String residentEmail) {
        this();
        this.residentEmail = residentEmail;
    }
    
    // Getters et Setters
    public String getResidentEmail() { return residentEmail; }
    public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }
    
    public List<String> getQuartiers() { return new ArrayList<>(quartiers); }
    public void setQuartiers(List<String> quartiers) { 
        this.quartiers = quartiers != null ? new ArrayList<>(quartiers) : new ArrayList<>(); 
    }
    
    public List<String> getRues() { return new ArrayList<>(rues); }
    public void setRues(List<String> rues) { 
        this.rues = rues != null ? new ArrayList<>(rues) : new ArrayList<>(); 
    }
    
    public List<String> getTypesTravaux() { return new ArrayList<>(typesTravaux); }
    public void setTypesTravaux(List<String> typesTravaux) { 
        this.typesTravaux = typesTravaux != null ? new ArrayList<>(typesTravaux) : new ArrayList<>(); 
    }
    
    public String getFrequence() { return frequence; }
    public void setFrequence(String frequence) { this.frequence = frequence; }
    
    public boolean isActives() { return actives; }
    public void setActives(boolean actives) { this.actives = actives; }
    
    // Méthodes utilitaires
    public void ajouterQuartier(String quartier) {
        if (quartier != null && !quartiers.contains(quartier)) {
            quartiers.add(quartier);
        }
    }
    
    public void retirerQuartier(String quartier) {
        quartiers.remove(quartier);
    }
    
    public void ajouterRue(String rue) {
        if (rue != null && !rues.contains(rue)) {
            rues.add(rue);
        }
    }
    
    public void retirerRue(String rue) {
        rues.remove(rue);
    }
    
    public boolean estAbonneAuQuartier(String quartier) {
        return quartiers.contains(quartier);
    }
    
    public boolean estAbonneALaRue(String rue) {
        return rues.contains(rue);
    }
}