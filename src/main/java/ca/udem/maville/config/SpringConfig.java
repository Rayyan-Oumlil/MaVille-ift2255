package ca.udem.maville.config;

import ca.udem.maville.service.GestionnaireProblemes;
import ca.udem.maville.service.GestionnaireProjets;
import ca.udem.maville.storage.JsonStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Spring pour MaVille
 */
@Configuration
public class SpringConfig implements WebMvcConfigurer {
    
    @Bean
    public JsonStorage jsonStorage() {
        return new JsonStorage();
    }
    
    @Bean
    public GestionnaireProblemes gestionnaireProblemes(JsonStorage storage) {
        return new GestionnaireProblemes(storage);
    }
    
    @Bean
    public GestionnaireProjets gestionnaireProjets() {
        return new GestionnaireProjets();
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public/", "classpath:/static/");
    }
}

