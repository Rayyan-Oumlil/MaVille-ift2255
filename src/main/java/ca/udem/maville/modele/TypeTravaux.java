package ca.udem.maville.modele;

/*
    Les 10 types de travaux définis dans l'énoncé.
    Utilisés pour catégoriser les problèmes.
 */
public enum TypeTravaux {
    TRAVAUX_ROUTIERS("Travaux routiers"),
    TRAVAUX_GAZ_ELECTRICITE("Travaux de gaz ou électricité"),
    CONSTRUCTION_RENOVATION("Construction ou rénovation"),
    ENTRETIEN_PAYSAGER("Entretien paysager"),
    TRAVAUX_TRANSPORTS_COMMUN("Travaux liés aux transports en commun"),
    TRAVAUX_SIGNALISATION_ECLAIRAGE("Travaux de signalisation et éclairage"),
    TRAVAUX_SOUTERRAINS("Travaux souterrains"),
    TRAVAUX_RESIDENTIEL("Travaux résidentiel"),
    ENTRETIEN_URBAIN("Entretien urbain"),
    ENTRETIEN_RESEAUX_TELECOM("Entretien des réseaux de télécommunication");

    private final String description;

    TypeTravaux(String description) {
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