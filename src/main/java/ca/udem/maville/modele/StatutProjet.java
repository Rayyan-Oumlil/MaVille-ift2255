package ca.udem.maville.modele;

/*
   États d'avancement d'un projet de travaux.
   APPROUVE -> EN COURS -> TERMINE
 */
public enum StatutProjet {
    EN_ATTENTE("En attente"),
    APPROUVE("Approuvé"),    // Candidature acceptée, projet créé
    EN_COURS("En cours"),    // Travaux démarrés
    SUSPENDU("Suspendu"),    // Travaux temporairement arrêtés
    TERMINE("Terminé"),      // Travaux finis
    ANNULE("Annulé");        // Travaux annulé

    private final String description;

    StatutProjet(String description) {
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