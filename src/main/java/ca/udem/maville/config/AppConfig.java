package ca.udem.maville.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration centralisée de l'application
 * Charge les propriétés depuis application.properties
 */
public class AppConfig {
    private static AppConfig instance;
    private Properties properties;
    
    private AppConfig() {
        this.properties = new Properties();
        loadProperties();
    }
    
    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }
    
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                // Valeurs par défaut si le fichier n'existe pas
                setDefaults();
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement configuration: " + e.getMessage());
            setDefaults();
        }
    }
    
    private void setDefaults() {
        properties.setProperty("server.port", "7000");
        properties.setProperty("server.host", "localhost");
        properties.setProperty("pagination.default.page.size", "20");
        properties.setProperty("pagination.max.page.size", "100");
        properties.setProperty("storage.data.dir", "data");
    }
    
    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "7000"));
    }
    
    public String getServerHost() {
        return properties.getProperty("server.host", "localhost");
    }
    
    public int getDefaultPageSize() {
        return Integer.parseInt(properties.getProperty("pagination.default.page.size", "20"));
    }
    
    public int getMaxPageSize() {
        return Integer.parseInt(properties.getProperty("pagination.max.page.size", "100"));
    }
    
    public String getDataDir() {
        return properties.getProperty("storage.data.dir", "data");
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}

