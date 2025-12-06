package ca.udem.maville.api.exception;

/**
 * Exception pour les ressources non trouvées
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}