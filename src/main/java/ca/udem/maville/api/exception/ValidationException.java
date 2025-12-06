package ca.udem.maville.api.exception;

/**
 * Exception pour les erreurs de validation
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

