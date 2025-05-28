package ca.udem.maville.modele;

/*
   Les 3 niveaux de priorité pour les problèmes signalés.
   Attribués par les agents STPM selon l'urgence.
 */
public enum Priorite {
    FAIBLE("Faible"),
    MOYENNE("Moyenne"),    // Priorité par défaut
    ELEVEE("Élevée");

    private final String description;

    Priorite(String description) {
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