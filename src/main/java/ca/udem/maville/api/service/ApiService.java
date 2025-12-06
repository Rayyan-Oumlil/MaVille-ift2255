package ca.udem.maville.api.service;

import org.springframework.stereotype.Service;

/**
 * Service partagé pour les méthodes utilitaires de l'API
 */
@Service
public class ApiService {
    
    /**
     * Extrait le quartier d'une localisation
     */
    public String extraireQuartier(String localisation) {
        if (localisation == null) return "Non spécifié";
        
        // Rechercher des mots clés de quartiers connus
        String[] quartiers = {"Rosemont", "Ville-Marie", "Plateau", "Centre-ville", 
                             "Outremont", "Verdun", "LaSalle", "Mercier",
                             "Hochelaga", "Villeray", "Ahuntsic", "CDN"};
        
        for (String q : quartiers) {
            if (localisation.toLowerCase().contains(q.toLowerCase())) {
                return q;
            }
        }
        
        // Si aucun quartier trouvé, essayer d'extraire après la dernière virgule
        if (localisation.contains(",")) {
            String[] parts = localisation.split(",");
            return parts[parts.length - 1].trim();
        }
        
        return "Centre-ville"; // Quartier par défaut
    }
}

