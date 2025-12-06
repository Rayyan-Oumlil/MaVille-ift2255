package ca.udem.maville.api.controller;

import ca.udem.maville.entity.ResidentEntity;
import ca.udem.maville.entity.PrestataireEntity;
import ca.udem.maville.service.DatabaseStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur REST pour l'authentification
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Endpoints pour la connexion des utilisateurs")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final DatabaseStorageService dbStorage;
    
    public AuthController(DatabaseStorageService dbStorage) {
        this.dbStorage = dbStorage;
    }
    
    @PostMapping("/login")
    @Operation(summary = "Connexion", 
               description = "Permet à un utilisateur de se connecter avec son email (résident) ou NEQ (prestataire)")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String identifier = credentials.get("identifier"); // email ou NEQ
        String password = credentials.get("password");
        
        if (identifier == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Identifiant et mot de passe requis"
            ));
        }
        
        // Vérifier si c'est un email (résident) ou NEQ (prestataire)
        if (identifier.contains("@")) {
            // Résident
            Optional<ResidentEntity> residentOpt = dbStorage.findResidentByEmail(identifier);
            if (residentOpt.isPresent()) {
                ResidentEntity resident = residentOpt.get();
                if (resident.getPasswordHash() != null && 
                    dbStorage.checkPassword(password, resident.getPasswordHash())) {
                    
                    logger.info("Connexion réussie pour résident: {}", identifier);
                    
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", resident.getId());
                    userInfo.put("email", resident.getEmail());
                    userInfo.put("nom", resident.getNomComplet());
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "type", "RESIDENT",
                        "user", userInfo
                    ));
                }
            }
        } else {
            // Prestataire
            Optional<PrestataireEntity> prestataireOpt = dbStorage.findPrestataireByNeq(identifier);
            if (prestataireOpt.isPresent()) {
                PrestataireEntity prestataire = prestataireOpt.get();
                if (prestataire.getPasswordHash() != null && 
                    dbStorage.checkPassword(password, prestataire.getPasswordHash())) {
                    
                    logger.info("Connexion réussie pour prestataire: {}", identifier);
                    
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", prestataire.getId());
                    userInfo.put("neq", prestataire.getNumeroEntreprise());
                    userInfo.put("nom", prestataire.getNomEntreprise());
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "type", "PRESTATAIRE",
                        "user", userInfo
                    ));
                }
            }
        }
        
        logger.warn("Tentative de connexion échouée pour: {}", identifier);
        return ResponseEntity.status(401).body(Map.of(
            "success", false,
            "message", "Identifiants invalides"
        ));
    }
}
