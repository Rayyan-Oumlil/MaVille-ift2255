package ca.udem.maville;

import ca.udem.maville.service.DatabaseStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Main Spring Boot application for MaVille
 * Uses PostgreSQL as the default database
 */
@SpringBootApplication
public class MaVilleApplication {
    private static final Logger logger = LoggerFactory.getLogger(MaVilleApplication.class);

    /**
     * Optional in slice tests (e.g. @WebMvcTest) where service beans are intentionally not loaded.
     * In the real application runtime, {@link DatabaseStorageService} is present.
     */
    @Autowired(required = false)
    private DatabaseStorageService dbStorage;
    
    public static void main(String[] args) {
        SpringApplication.run(MaVilleApplication.class, args);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        logger.info("Initializing data...");
        
        try {
            if (dbStorage == null) {
                logger.debug("DatabaseStorageService not available (slice test context?): skipping initialization");
                return;
            }

            // Initialize with sample data if database is empty
            dbStorage.initializeWithSampleData();
            logger.info("Data initialized successfully");
        } catch (Exception e) {
            logger.warn("Unable to initialize data (PostgreSQL not available?): {}", e.getMessage());
        }
        
        logger.info("MaVille application started successfully");
    }
}

