package ca.udem.maville.modele;

/**
 * Abonnement d'un prestataire aux notifications de problèmes
 * Conforme au DM3: prestataires peuvent s'abonner aux notifications 
 * de problèmes (quand priorité affectée) pour un quartier ou type donné
 */
public class AbonnementPrestataire {
    private String neq; // Numéro d'entreprise du prestataire
    private String type; // "QUARTIER" ou "TYPE_TRAVAUX"
    private String valeur; // nom du quartier ou type de travaux
    
    public AbonnementPrestataire() {
        // Constructeur vide pour JSON
    }
    
    public AbonnementPrestataire(String neq, String type, String valeur) {
        this.neq = neq;
        this.type = type;
        this.valeur = valeur;
    }
    
    // Getters et Setters
    public String getNeq() { return neq; }
    public void setNeq(String neq) { this.neq = neq; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbonnementPrestataire that = (AbonnementPrestataire) obj;
        return neq.equals(that.neq) && 
               type.equals(that.type) && 
               valeur.equals(that.valeur);
    }
    
    @Override
    public String toString() {
        return "AbonnementPrestataire{" +
                "neq='" + neq + '\'' +
                ", type='" + type + '\'' +
                ", valeur='" + valeur + '\'' +
                '}';
    }
}