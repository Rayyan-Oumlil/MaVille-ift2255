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
 * Global exception handler for the API
 * Centralizes error handling with English messages
 * and stack traces only in development mode
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    private final Environment environment;
    
    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }
    
    /**
     * Check if we're in development mode
     */
    private boolean isDevMode() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            // No active profile = development by default
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
     * Translate validation error messages to French
     */
    private String translateValidationMessage(String defaultMessage) {
        if (defaultMessage == null) {
            return "Data validation error";
        }
        
        // Common translations
        Map<String, String> translations = new HashMap<>();
        translations.put("must not be null", "must not be empty");
        translations.put("must not be empty", "must not be empty");
        translations.put("must not be blank", "must not be empty");
        translations.put("size must be between", "size must be between");
        translations.put("must be a valid email", "must be a valid email");
        translations.put("must match", "must match the format");
        
        String message = defaultMessage.toLowerCase();
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            if (message.contains(entry.getKey())) {
                return defaultMessage.replace(entry.getKey(), entry.getValue());
            }
        }
        
        return defaultMessage;
    }
    
    /**
     * Handle validation errors (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("Validation error: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        final String[] firstErrorMessage = {null};
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String translatedMessage = translateValidationMessage(error.getDefaultMessage());
            details.put(error.getField(), translatedMessage);
            if (firstErrorMessage[0] == null) {
                firstErrorMessage[0] = translatedMessage;
            }
        });
        
        String message = firstErrorMessage[0] != null ? firstErrorMessage[0] : "Data validation error";
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            message,
            400,
            request.getDescription(false).replace("uri=", "")
        );
        errorResponse.setDetails(details);
        
        // Add stack trace only in dev
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTrace(ex));
        }
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle custom validation exceptions
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        logger.warn("Validation error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            400,
            request.getDescription(false).replace("uri=", "")
        );
        
        // Add stack trace only in dev
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTrace(ex));
        }
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());
        
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = "The requested resource was not found";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "NOT_FOUND",
            message,
            404,
            request.getDescription(false).replace("uri=", "")
        );
        
        // Add stack trace only in dev
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTrace(ex));
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handle external API errors
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(
            ExternalApiException ex, WebRequest request) {
        logger.error("External API error: {}", ex.getMessage(), ex);
        
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = "Error communicating with external API";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "EXTERNAL_API_ERROR",
            message,
            502,
            request.getDescription(false).replace("uri=", "")
        );
        
        // Add stack trace only in dev
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTrace(ex));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }
    
    /**
     * Handle all other unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        logger.error("Unhandled error: {}", ex.getMessage(), ex);
        
        String message = isDevMode() 
            ? "An internal error occurred: " + ex.getMessage()
            : "An internal error occurred. Please try again later.";
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            message,
            500,
            request.getDescription(false).replace("uri=", "")
        );
        
        // Add stack trace only in dev
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTrace(ex));
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Extract stack trace as string
     */
    private String getStackTrace(Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}

