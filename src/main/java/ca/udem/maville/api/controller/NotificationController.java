package ca.udem.maville.api.controller;

import ca.udem.maville.api.service.NotificationService;
import ca.udem.maville.entity.NotificationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contrôleur pour les notifications en temps réel
 * Supporte Server-Sent Events (SSE) pour permettre scale-to-zero sur Cloud Run
 * 
 * SSE permet scale-to-zero car les connexions sont HTTP standard (pas de connexions longues)
 * WebSocket reste disponible pour les cas nécessitant une latence minimale
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    private final NotificationService notificationService;
    
    // Stocke les emitters SSE actifs par utilisateur
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    
    // Timeout SSE (5 minutes - max Cloud Run)
    private static final long SSE_TIMEOUT = 300_000L;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
        // Enregistrer ce contrôleur dans le service pour éviter dépendance circulaire
        notificationService.setSseController(this);
    }

    /**
     * Endpoint SSE pour recevoir les notifications en temps réel
     * Permet scale-to-zero car c'est une connexion HTTP standard
     * 
     * @param userIdentifier Email (résident), NEQ (prestataire), ou "stpm"
     * @return SseEmitter pour le streaming
     */
    @GetMapping(value = "/stream/{userIdentifier}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@PathVariable String userIdentifier) {
        logger.info("Nouvelle connexion SSE pour utilisateur: {}", userIdentifier);
        
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        // Callbacks pour nettoyer lors de la déconnexion
        emitter.onCompletion(() -> {
            logger.debug("SSE emitter complété pour: {}", userIdentifier);
            sseEmitters.remove(userIdentifier);
        });
        
        emitter.onTimeout(() -> {
            logger.debug("SSE emitter timeout pour: {}", userIdentifier);
            sseEmitters.remove(userIdentifier);
        });
        
        emitter.onError((ex) -> {
            logger.error("Erreur SSE emitter pour: {}", userIdentifier, ex);
            sseEmitters.remove(userIdentifier);
        });
        
        // Enregistrer l'emitter
        sseEmitters.put(userIdentifier, emitter);
        
        // Envoyer un message de connexion
        try {
            String connectionMessage = "{\"status\":\"connected\",\"user\":\"" + 
                (userIdentifier != null ? userIdentifier : "unknown") + "\"}";
            emitter.send(SseEmitter.event()
                .name("connected")
                .data(connectionMessage));
        } catch (IOException e) {
            logger.error("Erreur lors de l'envoi du message de connexion SSE", e);
            sseEmitters.remove(userIdentifier);
        }
        
        return emitter;
    }

    /**
     * Endpoint pour obtenir les notifications non lues (polling fallback)
     */
    @GetMapping("/unread/{userIdentifier}")
    public ResponseEntity<List<NotificationEntity>> getUnreadNotifications(
            @PathVariable String userIdentifier) {
        if (userIdentifier == null || userIdentifier.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            List<NotificationEntity> notifications = notificationService.getUnreadNotifications(userIdentifier);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des notifications pour: {}", userIdentifier, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Méthode interne pour envoyer une notification via SSE
     * Appelée par NotificationService
     */
    public void sendNotificationViaSSE(String userIdentifier, NotificationEntity notification) {
        if (userIdentifier == null || notification == null) {
            return;
        }
        SseEmitter emitter = sseEmitters.get(userIdentifier);
        if (emitter != null) {
            try {
                String json = notificationService.formatNotificationAsJson(notification);
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(json != null ? json : "{}"));
                logger.debug("Notification envoyée via SSE à {}: {}", userIdentifier, notification.getMessage());
            } catch (IOException e) {
                logger.error("Erreur lors de l'envoi SSE à {}", userIdentifier, e);
                sseEmitters.remove(userIdentifier);
            }
        }
    }

    /**
     * Broadcast une notification à tous les utilisateurs connectés via SSE
     */
    public void broadcastNotificationViaSSE(NotificationEntity notification) {
        if (notification == null) {
            return;
        }
        String json = notificationService.formatNotificationAsJson(notification);
        String finalJson = json != null ? json : "{}";
        sseEmitters.forEach((userIdentifier, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(finalJson));
            } catch (IOException e) {
                logger.error("Erreur lors du broadcast SSE à {}", userIdentifier, e);
                sseEmitters.remove(userIdentifier);
            }
        });
    }

    /**
     * Envoie une notification à un groupe spécifique via SSE
     */
    public void sendToGroupViaSSE(String group, NotificationEntity notification) {
        if (group == null || notification == null) {
            return;
        }
        String json = notificationService.formatNotificationAsJson(notification);
        String finalJson = json != null ? json : "{}";
        String groupPrefix = group + "/";
        sseEmitters.entrySet().stream()
            .filter(entry -> entry.getKey() != null && entry.getKey().startsWith(groupPrefix))
            .forEach(entry -> {
                try {
                    entry.getValue().send(SseEmitter.event()
                        .name("notification")
                        .data(finalJson));
                } catch (IOException e) {
                    logger.error("Erreur lors de l'envoi SSE au groupe {} pour {}", group, entry.getKey(), e);
                    sseEmitters.remove(entry.getKey());
                }
            });
    }
}

