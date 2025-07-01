package ca.udem.maville.modele;

public class Abonnement {
    private String residentEmail;
    private String type; // QUARTIER ou RUE
    private String valeur; // nom du quartier ou de la rue
    
    public Abonnement() {
        // Constructeur vide pour JSON
    }
    
    public Abonnement(String residentEmail, String type, String valeur) {
        this.residentEmail = residentEmail;
        this.type = type;
        this.valeur = valeur;
    }
    
    // Getters et Setters
    public String getResidentEmail() { return residentEmail; }
    public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Abonnement that = (Abonnement) obj;
        return residentEmail.equals(that.residentEmail) && 
               type.equals(that.type) && 
               valeur.equals(that.valeur);
    }
}