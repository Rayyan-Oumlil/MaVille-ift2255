package ca.udem.maville.api.service;

import ca.udem.maville.api.controller.NotificationController;
import ca.udem.maville.entity.NotificationEntity;
import ca.udem.maville.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service unifié pour envoyer des notifications via WebSocket ET SSE
 * Permet de choisir le canal de communication selon les besoins
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final NotificationWebSocketService webSocketService;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    
    // ObjectMapper est automatiquement fourni par Spring Boot
    
    // Injection optionnelle du contrôleur SSE (pour éviter dépendance circulaire)
    private NotificationController sseController;

    @Autowired
    public NotificationService(
            NotificationWebSocketService webSocketService,
            NotificationRepository notificationRepository,
            ObjectMapper objectMapper) {
        this.webSocketService = webSocketService;
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Injecte le contrôleur SSE (appelé après création du contrôleur)
     * Méthode publique pour permettre l'injection manuelle et éviter dépendance circulaire
     */
    public void setSseController(NotificationController sseController) {
        this.sseController = sseController;
    }

    /**
     * Envoie une notification à un utilisateur via WebSocket ET/OU SSE
     * 
     * @param userIdentifier Email (résident), NEQ (prestataire), ou "stpm"
     * @param notification La notification à envoyer
     * @param useWebSocket Si true, envoie via WebSocket
     * @param useSSE Si true, envoie via SSE
     */
    public void sendNotificationToUser(
            @NonNull String userIdentifier,
            @NonNull NotificationEntity notification,
            boolean useWebSocket,
            boolean useSSE) {
        
        if (useWebSocket) {
            webSocketService.sendNotificationToUser(userIdentifier, notification);
        }
        
        if (useSSE && sseController != null) {
            sseController.sendNotificationViaSSE(userIdentifier, notification);
        }
    }

    /**
     * Envoie une notification à un utilisateur via les deux canaux (par défaut)
     */
    public void sendNotificationToUser(
            @NonNull String userIdentifier,
            @NonNull NotificationEntity notification) {
        sendNotificationToUser(userIdentifier, notification, true, true);
    }

    /**
     * Broadcast une notification à tous les utilisateurs
     */
    public void broadcastNotification(
            @NonNull NotificationEntity notification,
            boolean useWebSocket,
            boolean useSSE) {
        
        if (useWebSocket) {
            webSocketService.broadcastNotification(notification);
        }
        
        if (useSSE && sseController != null) {
            sseController.broadcastNotificationViaSSE(notification);
        }
    }

    /**
     * Broadcast via les deux canaux (par défaut)
     */
    public void broadcastNotification(@NonNull NotificationEntity notification) {
        broadcastNotification(notification, true, true);
    }

    /**
     * Envoie une notification à tous les résidents
     */
    public void sendToResidents(
            @NonNull NotificationEntity notification,
            boolean useWebSocket,
            boolean useSSE) {
        
        if (useWebSocket) {
            webSocketService.sendToResidents(notification);
        }
        
        if (useSSE && sseController != null) {
            sseController.sendToGroupViaSSE("residents", notification);
        }
    }

    /**
     * Envoie une notification à tous les prestataires
     */
    public void sendToPrestataires(
            @NonNull NotificationEntity notification,
            boolean useWebSocket,
            boolean useSSE) {
        
        if (useWebSocket) {
            webSocketService.sendToPrestataires(notification);
        }
        
        if (useSSE && sseController != null) {
            sseController.sendToGroupViaSSE("prestataires", notification);
        }
    }

    /**
     * Envoie une notification au STPM
     */
    public void sendToStpm(
            @NonNull NotificationEntity notification,
            boolean useWebSocket,
            boolean useSSE) {
        
        if (useWebSocket) {
            webSocketService.sendToStpm(notification);
        }
        
        if (useSSE && sseController != null) {
            sseController.sendNotificationViaSSE("stpm", notification);
        }
    }

    /**
     * Récupère les notifications non lues pour un utilisateur
     */
    public List<NotificationEntity> getUnreadNotifications(@NonNull String userIdentifier) {
        // Déterminer le type d'utilisateur et récupérer les notifications
        if ("stpm".equalsIgnoreCase(userIdentifier)) {
            return notificationRepository.findStpmNotifications()
                    .stream()
                    .filter(n -> !n.isLu())
                    .toList();
        } else {
            // Essayer comme email résident
            List<NotificationEntity> residentNotifications = 
                    notificationRepository.findByResidentEmail(userIdentifier)
                            .stream()
                            .filter(n -> !n.isLu())
                            .toList();
            
            if (!residentNotifications.isEmpty()) {
                return residentNotifications;
            }
            
            // Essayer comme NEQ prestataire
            return notificationRepository.findByPrestataireNeq(userIdentifier)
                    .stream()
                    .filter(n -> !n.isLu())
                    .toList();
        }
    }

    /**
     * Formate une notification en JSON pour SSE
     */
    public String formatNotificationAsJson(@NonNull NotificationEntity notification) {
        try {
            NotificationWebSocketService.NotificationMessage message = 
                    new NotificationWebSocketService.NotificationMessage();
            message.setType("notification");
            message.setPayload(new NotificationWebSocketService.NotificationPayload(
                    String.valueOf(notification.getId()),
                    notification.getMessage(),
                    notification.getTypeChangement() != null ? notification.getTypeChangement() : "GENERAL",
                    notification.getDateCreation().toString(),
                    notification.getProjetId() != null ? notification.getProjetId().intValue() : null
            ));
            message.setTimestamp(java.time.Instant.now().toString());
            
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            logger.error("Erreur lors du formatage JSON de la notification", e);
            return "{\"type\":\"notification\",\"error\":\"format_error\"}";
        }
    }
}

