package ca.udem.maville.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Swagger/OpenAPI pour la documentation de l'API
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI maVilleAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MaVille API")
                .description("API REST pour la gestion des travaux publics de Montréal. " +
                    "Permet aux résidents de signaler des problèmes, aux prestataires de soumettre " +
                    "des candidatures, et aux agents STPM de gérer les projets.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Équipe MaVille")
                    .email("support@maville.ca"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")));
    }
}

