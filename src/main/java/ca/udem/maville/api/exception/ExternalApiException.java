package ca.udem.maville.api.exception;

/**
 * Exception pour les erreurs d'API externe
 */
public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message) {
        super(message);
    }
    
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

