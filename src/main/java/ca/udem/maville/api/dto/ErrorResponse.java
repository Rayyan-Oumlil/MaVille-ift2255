package ca.udem.maville.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Réponse d'erreur standardisée pour l'API
 */
public class ErrorResponse {
    private boolean success;
    private String error;
    private String message;
    private int statusCode;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, Object> details;
    private String stackTrace; // Stack trace (uniquement en mode développement)
    
    public ErrorResponse() {
        this.success = false;
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String error, int statusCode, String path) {
        this();
        this.error = error;
        this.statusCode = statusCode;
        this.path = path;
    }
    
    public ErrorResponse(String error, String message, int statusCode, String path) {
        this(error, statusCode, path);
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
    
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
    
    public String getStackTrace() {
        return stackTrace;
    }
    
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
}

