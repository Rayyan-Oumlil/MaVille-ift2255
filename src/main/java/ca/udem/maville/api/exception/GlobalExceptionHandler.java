package ca.udem.maville.api.exception;

import ca.udem.maville.api.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global d'exceptions pour l'API
 * Centralise la gestion de toutes les erreurs avec messages en français
 * et stack traces uniquement en mode développement
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    private final Environment environment;
    
    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }
    
    /**
     * Vérifie si on est en mode développement
     */
    private boolean isDevMode() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            // Pas de profil actif = développement par défaut
            return true;
        }
        for (String profile : activeProfiles) {
            if (profile.equals("dev") || profile.equals("development")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Traduit les messages d'erreur de validation en français
     */
    private String translateValidationMessage(String defaultMessage) {
        if (defaultMessage == null) {
            return "Erreur de validation des données";
        }
        
        // Traductions courantes
        Map<String, String> translations = new HashMap<>();
        translations.put("must not be null", "ne doit pas être vide");
        translations.put("must not be empty", "ne doit pas être vide");
        translations.put("must not be blank", "ne doit pas être vide");
        translations.put("size must be between", "la taille doit être entre");
        translations.put("must be a valid email", "doit être un email valide");
        translations.put("must match", "doit correspondre au format");
        
        String message = defaultMessage.toLowerCase();
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            if (message.contains(entry.getKey())) {
                return defaultMessage.replace(entry.getKey(), entry.getValue());
            }
        }
        
        return defaultMessage;
    }
    
    /**
     * Gère les erreurs de validation (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("Erreur de validation: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        final String[] firstErrorMessage = {null};
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String translatedMessage = translateValidationMessage(error.getDefaultMessage());
            details.put(error.getField(), translatedMessage);
            if (firstErrorMessage[0] == null) {
                firstErrorMessage[0] = translatedMessage;
            }
        });
        
        String message = firstErrorMessage[0] != null ? firstErrorMessage[0] : "Erreur de validation des données";
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            message,
            400,
            request.getDescription(false).replace("uri=", "")
        );
        errorResponse.setDetails(details);
        
        // Ajouter stack trace seulement en dev
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTrace(ex));
        }
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Gère les exceptions de validation personnalisées
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        logger.warn("Erreur de validation: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            400,
            request.getDescription(false).replace("uri=", "")
        );
        
        // Ajouter stack trace seulement en dev
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTrace(ex));
        }
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Gère les ressources non trouvées
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Ressource non trouvée: {}", ex.getMessage());
        
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = "La ressource demandée n'a pas été trouvée";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "NOT_FOUND",
            message,
            404,
            request.getDescription(false).replace("uri=", "")
        );
        
        // Ajouter stack trace seulement en dev
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTrace(ex));
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Gère les erreurs d'API externe
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(
            ExternalApiException ex, WebRequest request) {
        logger.error("Erreur API externe: {}", ex.getMessage(), ex);
        
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = "Erreur lors de la communication avec l'API externe";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "EXTERNAL_API_ERROR",
            message,
            502,
            request.getDescription(false).replace("uri=", "")
        );
        
        // Ajouter stack trace seulement en dev
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTrace(ex));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }
    
    /**
     * Gère toutes les autres exceptions non gérées
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        logger.error("Erreur non gérée: {}", ex.getMessage(), ex);
        
        String message = isDevMode() 
            ? "Une erreur interne est survenue: " + ex.getMessage()
            : "Une erreur interne est survenue. Veuillez réessayer plus tard.";
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            message,
            500,
            request.getDescription(false).replace("uri=", "")
        );
        
        // Ajouter stack trace seulement en dev
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTrace(ex));
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Extrait la stack trace sous forme de chaîne
     */
    private String getStackTrace(Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}

