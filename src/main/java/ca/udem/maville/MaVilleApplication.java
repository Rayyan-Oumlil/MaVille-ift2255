package ca.udem.maville;

import ca.udem.maville.service.DatabaseStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Application principale Spring Boot pour MaVille
 * Utilise H2 en mémoire par défaut (peut être changé pour PostgreSQL)
 */
@SpringBootApplication
public class MaVilleApplication {
    private static final Logger logger = LoggerFactory.getLogger(MaVilleApplication.class);
    
    private final DatabaseStorageService dbStorage;
    
    public MaVilleApplication(DatabaseStorageService dbStorage) {
        this.dbStorage = dbStorage;
    }
    
    public static void main(String[] args) {
        SpringApplication.run(MaVilleApplication.class, args);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        logger.info("Initialisation des données...");
        
        try {
        // Initialiser avec des données de test si la base est vide
        dbStorage.initializeWithSampleData();
            logger.info("Données initialisées avec succès");
        } catch (Exception e) {
            logger.warn("Impossible d'initialiser les données (PostgreSQL non disponible?): {}", e.getMessage());
        }
        
        logger.info("Application MaVille démarrée avec succès");
    }
}

