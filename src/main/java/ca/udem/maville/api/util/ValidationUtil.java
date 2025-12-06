package ca.udem.maville.api.util;

import java.util.regex.Pattern;

/**
 * Utilitaires de validation pour les données d'entrée
 */
public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern NEQ_PATTERN = Pattern.compile("^\\d{10}$");
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9]{3}[-. ]?[0-9]{3}[-. ]?[0-9]{4}$"
    );
    
    /**
     * Valide un email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Valide un NEQ (Numéro d'entreprise du Québec)
     */
    public static boolean isValidNEQ(String neq) {
        if (neq == null || neq.trim().isEmpty()) {
            return false;
        }
        return NEQ_PATTERN.matcher(neq.trim()).matches();
    }
    
    /**
     * Valide un numéro de téléphone
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Valide qu'une chaîne n'est pas vide
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Valide qu'une chaîne a une longueur minimale
     */
    public static boolean hasMinLength(String value, int minLength) {
        return value != null && value.trim().length() >= minLength;
    }
    
    /**
     * Valide qu'une chaîne a une longueur maximale
     */
    public static boolean hasMaxLength(String value, int maxLength) {
        return value == null || value.length() <= maxLength;
    }
    
    /**
     * Valide qu'un entier est dans une plage
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * Nettoie une chaîne (trim et échappement basique)
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim()
                   .replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#x27;");
    }
}

