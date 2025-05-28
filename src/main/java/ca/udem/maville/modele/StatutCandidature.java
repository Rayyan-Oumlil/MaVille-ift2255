package ca.udem.maville.modele;

/*
   États possibles d'une candidature soumise par un prestataire.
   SOUMISE -> APPROUVEE/REJETEE ou ANNULEE
 */
public enum StatutCandidature {
    SOUMISE("Soumise"),      // État initial
    APPROUVEE("Approuvée"),  // Validée par agent STPM
    REJETEE("Rejetée"),      // Refusée par agent STPM
    ANNULEE("Annulée");      // Annulée par le prestataire

    private final String description;

    StatutCandidature(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}