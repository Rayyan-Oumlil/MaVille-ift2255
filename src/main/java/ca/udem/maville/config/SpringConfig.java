package ca.udem.maville.config;

import ca.udem.maville.api.MontrealApiService;
import ca.udem.maville.service.GestionnaireProjets;
import ca.udem.maville.service.ModelMapperService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Spring pour MaVille
 * Utilise maintenant PostgreSQL via DatabaseStorageService
 */
@Configuration
@EnableCaching
public class SpringConfig implements WebMvcConfigurer {
    
    @Bean
    public ModelMapperService modelMapperService() {
        return new ModelMapperService();
    }
    
    @Bean
    public GestionnaireProjets gestionnaireProjets() {
        return new GestionnaireProjets();
    }
    
    @Bean
    public MontrealApiService montrealApiService() {
        return new MontrealApiService();
    }
    
    @Bean
    public CacheManager cacheManager() {
        // Cache pour : API externe, listes de problèmes, listes de projets
        return new ConcurrentMapCacheManager(
            "travauxMontreal",      // API externe Montréal
            "problemes",            // Liste des problèmes (cache 5 minutes)
            "projets",              // Liste des projets (cache 5 minutes)
            "candidatures",         // Liste des candidatures (cache 5 minutes)
            "residents",            // Données résidents (cache 10 minutes)
            "prestataires"          // Données prestataires (cache 10 minutes)
        );
    }
    
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
    
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public/", "classpath:/static/");
    }
}

