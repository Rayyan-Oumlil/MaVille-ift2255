package ca.udem.maville.modele;

/*
   Représente une entreprise prestataire de services.
   Contient uniquement les informations de base de l'entreprise.
 */
public class Prestataire {
    // Infos de base de l'entreprise
    private String numeroEntreprise; // Numéro d'entreprise (NE) 
    private String nomEntreprise;
    private String contactNom;
    private String telephone;
    private String email;

    // Constructeur par défaut NÉCESSAIRE pour Jackson
    public Prestataire() {
        // Constructeur vide pour la désérialisation JSON
    }

    /*
       Constructeur - crée un prestataire avec les infos de base
     */
    public Prestataire(String numeroEntreprise, String nomEntreprise, String contactNom, 
                      String telephone, String email) {
        this.numeroEntreprise = numeroEntreprise;
        this.nomEntreprise = nomEntreprise;
        this.contactNom = contactNom;
        this.telephone = telephone;
        this.email = email;
    }

    // Getters et Setters 
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

    @Override
    public String toString() {
        return nomEntreprise + " (" + numeroEntreprise + ")";
    }

    /*
       Deux prestataires sont égaux s'ils ont le même numéro d'entreprise
       Important pour les recherches et comparaisons
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Prestataire prestataire = (Prestataire) obj;
        return numeroEntreprise.equals(prestataire.numeroEntreprise);
    }

    @Override
    public int hashCode() {
        return numeroEntreprise.hashCode();
    }
}