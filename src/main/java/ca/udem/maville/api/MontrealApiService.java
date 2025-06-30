package ca.udem.maville.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Service pour intégrer l'API de données ouvertes de Montréal
 * Récupère les vrais travaux en cours depuis l'API officielle
 */
public class MontrealApiService {
    private static final String API_URL = "https://donnees.montreal.ca/api/3/action/datastore_search";
    private static final String RESOURCE_ID = "cc41b532-f12d-40fb-9f55-eb58c9a2b12b";
    
    private final OkHttpClient client;
    private final ObjectMapper mapper;
    
    public MontrealApiService() {
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();
    }
    
    /**
     * Récupère les travaux en cours depuis l'API de Montréal
     * @param limit Nombre maximum de résultats (par défaut 10)
     * @return Liste des travaux sous forme de Map
     */
    public List<Map<String, Object>> getTravauxEnCours(int limit) {
        List<Map<String, Object>> travaux = new ArrayList<>();
        
        try {
            // Construction de l'URL avec paramètres
            String url = API_URL + "?resource_id=" + RESOURCE_ID + "&limit=" + limit;
            
            Request request = new Request.Builder()
                .url(url)
                .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    Map<String, Object> data = mapper.readValue(jsonResponse, Map.class);
                    
                    // L'API retourne: { "success": true, "result": { "records": [...] } }
                    if (data.get("success").equals(true)) {
                        Map<String, Object> result = (Map<String, Object>) data.get("result");
                        List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
                        
                        // Transformer les données pour notre format
                        for (Map<String, Object> record : records) {
                            Map<String, Object> travail = new java.util.HashMap<>();
                            travail.put("id", record.get("id"));
                            travail.put("arrondissement", record.get("boroughid"));
                            travail.put("statut", record.get("currentstatus"));
                            travail.put("motif", record.get("reason_category"));
                            travail.put("organisation", record.get("organizationname"));
                            travail.put("categorie_soumissionnaire", record.get("submittercategory"));
                            
                            // Ajouter des infos supplémentaires si disponibles
                            if (record.containsKey("duration_start_date")) {
                                travail.put("date_debut", record.get("duration_start_date"));
                            }
                            if (record.containsKey("duration_end_date")) {
                                travail.put("date_fin", record.get("duration_end_date"));
                            }
                            
                            travaux.add(travail);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel à l'API Montréal: " + e.getMessage());
            // En cas d'erreur, retourner des données simulées
            travaux.add(createTravauxSimule());
        }
        
        return travaux;
    }
    
    /**
     * Crée un travail simulé en cas d'erreur API
     */
    private Map<String, Object> createTravauxSimule() {
        Map<String, Object> travail = new java.util.HashMap<>();
        travail.put("id", "SIMUL-001");
        travail.put("arrondissement", "Ville-Marie");
        travail.put("statut", "En cours");
        travail.put("motif", "Infrastructure");
        travail.put("organisation", "Ville de Montréal");
        travail.put("note", "Données simulées - API non disponible");
        return travail;
    }
}
