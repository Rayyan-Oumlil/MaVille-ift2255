package ca.udem.maville.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket pour les notifications en temps réel
 * 
 * NOTE: Les WebSockets maintiennent des connexions longues qui peuvent empêcher
 * Cloud Run de scale-to-zero. Pour réduire les coûts, considérer:
 * - Utiliser Server-Sent Events (SSE) à la place
 * - Utiliser polling HTTP périodique
 * - Accepter qu'une instance reste active pour les WebSockets temps réel
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
        // Heartbeat configuré pour maintenir la connexion active
        config.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10000, 10000}); // Heartbeat toutes les 10s
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Endpoint WebSocket accessible depuis le frontend
        // Le frontend se connecte à ws://localhost:7000/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // En production, spécifier les origines autorisées
                .withSockJS() // Support SockJS pour compatibilité navigateurs
                .setHeartbeatTime(25000) // Timeout de 25s pour détecter les connexions mortes
                .setDisconnectDelay(5000); // Délai avant de nettoyer les connexions fermées
    }

    /**
     * TaskScheduler requis pour le mécanisme de heartbeat du broker WebSocket.
     * Sans ce bean, Spring lève une exception lors du démarrage si des valeurs
     * de heartbeat sont configurées.
     */
    @Bean
    public TaskScheduler messageBrokerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(20);
        return scheduler;
    }
}
