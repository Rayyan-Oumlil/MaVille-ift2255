package ca.udem.maville.api.service;

import ca.udem.maville.entity.NotificationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service pour envoyer des notifications via WebSocket
 */
@Service
public class NotificationWebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketService.class);
    
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Envoie une notification à un utilisateur spécifique
     * @param userIdentifier Email (résident) ou NEQ (prestataire) ou "stpm"
     * @param notification La notification à envoyer
     */
    public void sendNotificationToUser(@NonNull String userIdentifier, @NonNull NotificationEntity notification) {
        try {
            String destination = "/topic/notifications/" + userIdentifier;
            messagingTemplate.convertAndSend(destination, createNotificationMessage(notification));
            logger.debug("Notification envoyée via WebSocket à {}: {}", userIdentifier, notification.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de notification WebSocket à {}", userIdentifier, e);
        }
    }

    /**
     * Envoie une notification à tous les utilisateurs (broadcast)
     * @param notification La notification à envoyer
     */
    public void broadcastNotification(@NonNull NotificationEntity notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications", createNotificationMessage(notification));
            logger.debug("Notification broadcast envoyée via WebSocket: {}", notification.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors du broadcast de notification WebSocket", e);
        }
    }

    /**
     * Envoie une notification à tous les résidents
     */
    public void sendToResidents(@NonNull NotificationEntity notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications/residents", createNotificationMessage(notification));
            logger.debug("Notification envoyée aux résidents via WebSocket: {}", notification.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de notification aux résidents", e);
        }
    }

    /**
     * Envoie une notification à tous les prestataires
     */
    public void sendToPrestataires(@NonNull NotificationEntity notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications/prestataires", createNotificationMessage(notification));
            logger.debug("Notification envoyée aux prestataires via WebSocket: {}", notification.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de notification aux prestataires", e);
        }
    }

    /**
     * Envoie une notification aux agents STPM
     */
    public void sendToStpm(@NonNull NotificationEntity notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications/stpm", createNotificationMessage(notification));
            logger.debug("Notification envoyée au STPM via WebSocket: {}", notification.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de notification au STPM", e);
        }
    }

    /**
     * Crée un message de notification au format JSON pour le frontend
     */
    @NonNull
    private NotificationMessage createNotificationMessage(@NonNull NotificationEntity notification) {
        NotificationMessage message = new NotificationMessage();
        message.setType("notification");
        message.setPayload(new NotificationPayload(
            String.valueOf(notification.getId()),
            notification.getMessage(),
            notification.getTypeChangement() != null ? notification.getTypeChangement() : "GENERAL",
            notification.getDateCreation().toString(),
            notification.getProjetId() != null ? notification.getProjetId().intValue() : null
        ));
        message.setTimestamp(java.time.Instant.now().toString());
        return message;
    }

    /**
     * Classe interne pour le format de message WebSocket
     */
    public static class NotificationMessage {
        private String type;
        private NotificationPayload payload;
        private String timestamp;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public NotificationPayload getPayload() {
            return payload;
        }

        public void setPayload(NotificationPayload payload) {
            this.payload = payload;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * Classe interne pour le payload de notification
     */
    public static class NotificationPayload {
        private String id;
        private String message;
        private String type;
        private String date;
        private Integer projetId;

        public NotificationPayload(String id, String message, String type, String date, Integer projetId) {
            this.id = id;
            this.message = message;
            this.type = type;
            this.date = date;
            this.projetId = projetId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Integer getProjetId() {
            return projetId;
        }

        public void setProjetId(Integer projetId) {
            this.projetId = projetId;
        }
    }
}
