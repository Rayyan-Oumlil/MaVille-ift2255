package ca.udem.maville.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket pour les notifications en temps réel
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Préfixe pour les destinations où les clients peuvent envoyer des messages
        config.setApplicationDestinationPrefixes("/app");
        
        // Préfixe pour les destinations où le serveur envoie des messages aux clients
        // Les clients s'abonnent à /topic/notifications pour recevoir les notifications
        config.enableSimpleBroker("/topic");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Endpoint WebSocket accessible depuis le frontend
        // Le frontend se connecte à ws://localhost:7000/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // En production, spécifier les origines autorisées
                .withSockJS(); // Support SockJS pour compatibilité navigateurs
    }
}
