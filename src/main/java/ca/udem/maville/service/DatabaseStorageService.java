package ca.udem.maville.service;

import ca.udem.maville.entity.*;
import ca.udem.maville.modele.*;
import ca.udem.maville.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

/**
 * Service de stockage utilisant PostgreSQL via Spring Data JPA
 * Remplace progressivement JsonStorage pour activer les transactions
 */
@Service
@Transactional
public class DatabaseStorageService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseStorageService.class);
    
    @Autowired
    private ResidentRepository residentRepository;
    
    @Autowired
    private PrestataireRepository prestataireRepository;
    
    @Autowired
    private ProblemeRepository problemeRepository;
    
    @Autowired
    private CandidatureRepository candidatureRepository;
    
    @Autowired
    private ProjetRepository projetRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private AbonnementRepository abonnementRepository;
    
    @Autowired
    private PreferenceRepository preferenceRepository;
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // ========== RESIDENTS ==========
    
    public ResidentEntity findOrCreateResident(String email, String prenom, String nom, 
                                               String telephone, String adresse) {
        Optional<ResidentEntity> existing = residentRepository.findByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        ResidentEntity resident = new ResidentEntity(prenom, nom, email, telephone, adresse);
        return residentRepository.save(resident);
    }
    
    public Optional<ResidentEntity> findResidentByEmail(String email) {
        return residentRepository.findByEmail(email);
    }
    
    // ========== PRESTATAIRES ==========
    
    public PrestataireEntity findOrCreatePrestataire(String numeroEntreprise, String nomEntreprise,
                                                     String contactNom, String telephone, String email) {
        Optional<PrestataireEntity> existing = prestataireRepository.findByNumeroEntreprise(numeroEntreprise);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        PrestataireEntity prestataire = new PrestataireEntity(numeroEntreprise, nomEntreprise, 
                                                              contactNom, telephone, email);
        return prestataireRepository.save(prestataire);
    }
    
    public Optional<PrestataireEntity> findPrestataireByNeq(String neq) {
        return prestataireRepository.findByNumeroEntreprise(neq);
    }
    
    // ========== AUTHENTIFICATION ==========
    
    /**
     * Vérifie si un mot de passe correspond au hash stocké
     */
    public boolean checkPassword(String rawPassword, String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
    
    /**
     * Hash un mot de passe avec BCrypt
     */
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    // ========== PROBLEMES ==========
    
    @Transactional
    @CacheEvict(value = "problemes", allEntries = true)
    public ProblemeEntity createProbleme(String lieu, TypeTravaux typeProbleme, String description,
                                         ResidentEntity declarant, Priorite priorite) {
        ProblemeEntity probleme = new ProblemeEntity(lieu, typeProbleme, description, declarant, priorite);
        return problemeRepository.save(probleme);
    }
    
    @Cacheable(value = "problemes", key = "'all'")
    public List<ProblemeEntity> findAllProblemes() {
        return problemeRepository.findAll();
    }
    
    @Cacheable(value = "problemes", key = "'nonResolus'")
    public List<ProblemeEntity> findNonResolus() {
        return problemeRepository.findByResoluFalse();
    }
    
    // Cache avec clé composite incluant les filtres et pagination
    @Cacheable(value = "problemes", key = "#quartier + '_' + (#type != null ? #type.name() : 'null') + '_' + #page + '_' + #size")
    public Page<ProblemeEntity> findNonResolusWithFilters(String quartier, TypeTravaux type, 
                                                          int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return problemeRepository.findNonResolusWithFilters(quartier, type, pageable);
    }
    
    public Optional<ProblemeEntity> findProblemeById(@org.springframework.lang.NonNull Long id) {
        return problemeRepository.findById(id);
    }
    
    @Transactional
    public ProblemeEntity updateProbleme(@org.springframework.lang.NonNull ProblemeEntity probleme) {
        return problemeRepository.save(probleme);
    }
    
    // ========== CANDIDATURES ==========
    
    @Transactional
    public CandidatureEntity createCandidature(PrestataireEntity prestataire, 
                                               List<ProblemeEntity> problemes,
                                               String descriptionProjet, Double coutEstime,
                                               LocalDate dateDebutPrevue, LocalDate dateFinPrevue) {
        CandidatureEntity candidature = new CandidatureEntity(prestataire, problemes, descriptionProjet,
                                                             coutEstime, dateDebutPrevue, dateFinPrevue);
        return candidatureRepository.save(candidature);
    }
    
    public List<CandidatureEntity> findAllCandidatures() {
        return candidatureRepository.findAll();
    }
    
    public Page<CandidatureEntity> findAllCandidatures(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return candidatureRepository.findAll(pageable);
    }
    
    public Optional<CandidatureEntity> findCandidatureById(@org.springframework.lang.NonNull Long id) {
        return candidatureRepository.findById(id);
    }
    
    @Transactional
    public CandidatureEntity updateCandidature(@org.springframework.lang.NonNull CandidatureEntity candidature) {
        return candidatureRepository.save(candidature);
    }
    
    // ========== PROJETS ==========
    
    @Transactional
    @CacheEvict(value = {"projets", "candidatures"}, allEntries = true)
    public ProjetEntity createProjet(CandidatureEntity candidature, List<ProblemeEntity> problemes,
                                     PrestataireEntity prestataire) {
        ProjetEntity projet = new ProjetEntity(candidature, problemes, prestataire);
        return projetRepository.save(projet);
    }
    
    @Cacheable(value = "projets", key = "'all'")
    public List<ProjetEntity> findAllProjets() {
        return projetRepository.findAll();
    }
    
    @Cacheable(value = "projets", key = "#neq")
    public List<ProjetEntity> findProjetsByPrestataire(String neq) {
        return projetRepository.findByPrestataireNeq(neq);
    }
    
    public Optional<ProjetEntity> findProjetById(@org.springframework.lang.NonNull Long id) {
        return projetRepository.findById(id);
    }
    
    @Transactional
    public ProjetEntity updateProjet(ProjetEntity projet) {
        projet.setDerniereMiseAJour(LocalDateTime.now());
        return projetRepository.save(projet);
    }
    
    // ========== NOTIFICATIONS ==========
    
    @Transactional
    public NotificationEntity createNotification(String message, String typeChangement,
                                                 String residentEmail, Long projetId, 
                                                 String typeDestinataire) {
        NotificationEntity notification = new NotificationEntity(message, typeChangement, 
                                                               residentEmail, projetId, typeDestinataire);
        return notificationRepository.save(notification);
    }
    
    @Transactional
    public NotificationEntity creerNotificationStmp(String message, String typeChangement,
                                                     int projetOuProblemeId, String quartier) {
        NotificationEntity notification = new NotificationEntity();
        notification.setMessage(message);
        notification.setTypeChangement(typeChangement);
        notification.setTypeDestinataire("STPM");
        notification.setDestinataire("STPM");
        notification.setProjetId((long) projetOuProblemeId);
        notification.setDateCreation(java.time.LocalDateTime.now());
        notification.setLu(false);
        return notificationRepository.save(notification);
    }
    
    @Transactional
    public NotificationEntity creerNotificationResident(String residentEmail, String message,
                                                        String typeChangement, int projetId, String quartier) {
        NotificationEntity notification = new NotificationEntity();
        notification.setMessage(message);
        notification.setTypeChangement(typeChangement);
        notification.setResidentEmail(residentEmail);
        notification.setTypeDestinataire("RESIDENT");
        notification.setDestinataire(residentEmail);
        notification.setProjetId((long) projetId);
        notification.setDateCreation(java.time.LocalDateTime.now());
        notification.setLu(false);
        return notificationRepository.save(notification);
    }
    
    @Transactional
    public NotificationEntity creerNotificationPrestataire(String prestataireNeq, String message,
                                                            String typeChangement, int problemeId,
                                                            String quartier, String priorite) {
        NotificationEntity notification = new NotificationEntity();
        notification.setMessage(message);
        notification.setTypeChangement(typeChangement);
        notification.setTypeDestinataire("PRESTATAIRE");
        notification.setDestinataire(prestataireNeq);
        notification.setProjetId((long) problemeId);
        notification.setDateCreation(java.time.LocalDateTime.now());
        notification.setLu(false);
        return notificationRepository.save(notification);
    }
    
    public List<NotificationEntity> findNotificationsByResident(String email) {
        return notificationRepository.findByResidentEmail(email);
    }
    
    public List<NotificationEntity> findStpmNotifications() {
        return notificationRepository.findStpmNotifications();
    }
    
    public List<NotificationEntity> findPrestataireNotifications(String neq) {
        return notificationRepository.findPrestataireNotifications(neq);
    }
    
    @Transactional
    public void markNotificationsAsRead(List<Long> notificationIds) {
        notificationIds.forEach(id -> {
            if (id != null) {
            notificationRepository.findById(id).ifPresent(notif -> {
                notif.setLu(true);
                notificationRepository.save(notif);
            });
            }
        });
    }
    
    // ========== ABONNEMENTS ==========
    
    @Transactional
    public AbonnementEntity createAbonnement(String residentEmail, String type, String valeur) {
        // Vérifier si l'abonnement existe déjà
        List<AbonnementEntity> existing = abonnementRepository.findByResidentEmailAndTypeAndValeur(
            residentEmail, type, valeur);
        
        if (!existing.isEmpty()) {
            return existing.get(0); // Retourner l'existant
        }
        
        AbonnementEntity abonnement = new AbonnementEntity(residentEmail, type, valeur);
        return abonnementRepository.save(abonnement);
    }
    
    public List<AbonnementEntity> findAbonnementsByResident(String email) {
        return abonnementRepository.findByResidentEmail(email);
    }
    
    public List<AbonnementEntity> findAbonnementsByTypeAndValeur(String type, String valeur) {
        return abonnementRepository.findByTypeAndValeur(type, valeur);
    }
    
    // ========== PRÉFÉRENCES ==========
    
    /**
     * Récupère les préférences d'un résident par son email
     */
    @NonNull
    public PreferenceEntity findOrCreatePreferencesByEmail(@NonNull String email) {
        Optional<PreferenceEntity> existing = preferenceRepository.findByEmail(email);
        if (existing.isPresent()) {
            return Objects.requireNonNull(existing.get());
        }
        
        // Créer des préférences par défaut
        PreferenceEntity preferences = new PreferenceEntity(email);
        preferences.setNotificationsEmail(true);
        preferences.setNotificationsQuartier(true);
        preferences.setNotificationsType(new ArrayList<String>());
        PreferenceEntity saved = preferenceRepository.save(preferences);
        return Objects.requireNonNull(saved);
    }
    
    /**
     * Récupère les préférences d'un prestataire par son NEQ
     */
    @NonNull
    public PreferenceEntity findOrCreatePreferencesByNeq(@NonNull String neq) {
        Optional<PreferenceEntity> existing = preferenceRepository.findByNeq(neq);
        if (existing.isPresent()) {
            @SuppressWarnings("null")
            PreferenceEntity pref = existing.get();
            return Objects.requireNonNull(pref);
        }
        
        // Créer des préférences par défaut
        PreferenceEntity preferences = new PreferenceEntity();
        preferences.setNeq(neq);
        preferences.setNotificationsEmail(true);
        preferences.setNotificationsQuartier(true);
        preferences.setNotificationsType(new ArrayList<String>());
        PreferenceEntity saved = preferenceRepository.save(preferences);
        return Objects.requireNonNull(saved);
    }
    
    /**
     * Sauvegarde ou met à jour les préférences d'un utilisateur
     */
    @NonNull
    public PreferenceEntity savePreferences(String email, String neq, 
                                           Boolean notificationsEmail, 
                                           Boolean notificationsQuartier,
                                           List<String> notificationsType) {
        PreferenceEntity preferences;
        
        if (email != null && !email.isEmpty()) {
            preferences = findOrCreatePreferencesByEmail(email);
        } else if (neq != null && !neq.isEmpty()) {
            preferences = findOrCreatePreferencesByNeq(neq);
        } else {
            throw new IllegalArgumentException("Email ou NEQ doit être fourni");
        }
        
        if (notificationsEmail != null) {
            preferences.setNotificationsEmail(notificationsEmail);
        }
        if (notificationsQuartier != null) {
            preferences.setNotificationsQuartier(notificationsQuartier);
        }
        if (notificationsType != null) {
            preferences.setNotificationsType(notificationsType);
        }
        
        PreferenceEntity saved = preferenceRepository.save(preferences);
        return Objects.requireNonNull(saved);
    }
    
    // ========== INITIALISATION DES DONNÉES ==========
    
    /**
     * Initialise la base de données avec des données de test si elle est vide
     */
    public void initializeWithSampleData() {
        // Vérifier si la base contient déjà des données
        if (!residentRepository.findAll().isEmpty()) {
            logger.debug("Base de données déjà initialisée, skip de l'initialisation");
            return;
        }
        
        logger.info("Initialisation de la base de données avec des données de test...");
        
        // Créer quelques résidents de test avec mots de passe
        ResidentEntity resident1 = findOrCreateResident("marie@test.com", "Marie", "Dupont", "514-123-4567", "123 Rue Saint-Denis, Plateau-Mont-Royal");
        if (resident1.getPasswordHash() == null) {
            resident1.setPasswordHash(hashPassword("password123"));
            residentRepository.save(resident1);
        }
        
        ResidentEntity resident2 = findOrCreateResident("jean@test.com", "Jean", "Martin", "514-234-5678", "456 Avenue du Parc, Villeray");
        if (resident2.getPasswordHash() == null) {
            resident2.setPasswordHash(hashPassword("password123"));
            residentRepository.save(resident2);
        }
        
        // Créer quelques prestataires de test avec mots de passe
        PrestataireEntity prestataire1 = findOrCreatePrestataire("ABC123", "Entreprise ABC", "Contact ABC", "514-345-6789", "contact@abc.com");
        if (prestataire1.getPasswordHash() == null) {
            prestataire1.setPasswordHash(hashPassword("password123"));
            prestataireRepository.save(prestataire1);
        }
        
        PrestataireEntity prestataire2 = findOrCreatePrestataire("XYZ789", "Entreprise XYZ", "Contact XYZ", "514-456-7890", "contact@xyz.com");
        if (prestataire2.getPasswordHash() == null) {
            prestataire2.setPasswordHash(hashPassword("password123"));
            prestataireRepository.save(prestataire2);
        }
        
        // Créer quelques problèmes de test
        createProbleme(
            "123 Rue Saint-Denis, Plateau-Mont-Royal",
            TypeTravaux.ENTRETIEN_URBAIN,
            "Trou dans la chaussée",
            resident1,
            Priorite.MOYENNE
        );
        
        createProbleme(
            "456 Avenue du Parc, Villeray",
            TypeTravaux.TRAVAUX_ROUTIERS,
            "Signalisation manquante",
            resident2,
            Priorite.ELEVEE
        );
        
        logger.info("Données de test initialisées : {} résidents, {} prestataires, {} problèmes",
            residentRepository.count(), prestataireRepository.count(), problemeRepository.count());
    }
}

