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
    @Transactional
    public void initializeWithSampleData() {
        // Vérifier si la base contient déjà des données
        if (!residentRepository.findAll().isEmpty()) {
            logger.debug("Database already initialized, skipping initialization");
            return;
        }
        
        logger.info("Initializing database with realistic sample data...");
        
        // ========== CREATE RESIDENTS ==========
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
        
        ResidentEntity resident3 = findOrCreateResident("sophie.tremblay@gmail.com", "Sophie", "Tremblay", "514-345-6789", "789 Boulevard Saint-Laurent, Rosemont");
        if (resident3.getPasswordHash() == null) {
            resident3.setPasswordHash(hashPassword("password123"));
            residentRepository.save(resident3);
        }
        
        ResidentEntity resident4 = findOrCreateResident("luc.gagnon@outlook.com", "Luc", "Gagnon", "514-456-7890", "321 Rue Sherbrooke, Ville-Marie");
        if (resident4.getPasswordHash() == null) {
            resident4.setPasswordHash(hashPassword("password123"));
            residentRepository.save(resident4);
        }
        
        ResidentEntity resident5 = findOrCreateResident("amelie.roy@yahoo.ca", "Amélie", "Roy", "514-567-8901", "654 Avenue Mont-Royal, Le Plateau");
        if (resident5.getPasswordHash() == null) {
            resident5.setPasswordHash(hashPassword("password123"));
            residentRepository.save(resident5);
        }
        
        // ========== CREATE SERVICE PROVIDERS ==========
        PrestataireEntity prestataire1 = findOrCreatePrestataire("ABC123", "Construction ABC Inc.", "Pierre Lavoie", "514-345-6789", "contact@abc.com");
        if (prestataire1.getPasswordHash() == null) {
            prestataire1.setPasswordHash(hashPassword("password123"));
            prestataireRepository.save(prestataire1);
        }
        
        PrestataireEntity prestataire2 = findOrCreatePrestataire("XYZ789", "Pavage XYZ Ltée", "Isabelle Côté", "514-456-7890", "contact@xyz.com");
        if (prestataire2.getPasswordHash() == null) {
            prestataire2.setPasswordHash(hashPassword("password123"));
            prestataireRepository.save(prestataire2);
        }
        
        PrestataireEntity prestataire3 = findOrCreatePrestataire("MTL456", "Électricité Montréal Pro", "Marc Dubois", "514-678-9012", "info@mtlpro.ca");
        if (prestataire3.getPasswordHash() == null) {
            prestataire3.setPasswordHash(hashPassword("password123"));
            prestataireRepository.save(prestataire3);
        }
        
        PrestataireEntity prestataire4 = findOrCreatePrestataire("QC2024", "Plomberie Québec Expert", "Nathalie Bergeron", "514-789-0123", "service@qcexpert.com");
        if (prestataire4.getPasswordHash() == null) {
            prestataire4.setPasswordHash(hashPassword("password123"));
            prestataireRepository.save(prestataire4);
        }
        
        // ========== CREATE DIVERSE PROBLEMS ==========
        
        // High priority problems
        ProblemeEntity prob1 = createProbleme(
            "123 Rue Saint-Denis, Plateau-Mont-Royal",
            TypeTravaux.ENTRETIEN_URBAIN,
            "Large pothole causing vehicle damage. Urgent repair needed.",
            resident1,
            Priorite.ELEVEE
        );
        
        ProblemeEntity prob2 = createProbleme(
            "456 Avenue du Parc, Villeray",
            TypeTravaux.TRAVAUX_ROUTIERS,
            "Missing stop sign at busy intersection. Safety hazard.",
            resident2,
            Priorite.ELEVEE
        );
        
        ProblemeEntity prob3 = createProbleme(
            "789 Boulevard Saint-Laurent, Rosemont",
            TypeTravaux.TRAVAUX_GAZ_ELECTRICITE,
            "Gas leak detected near residential building. Immediate attention required.",
            resident3,
            Priorite.ELEVEE
        );
        
        // Medium priority problems
        ProblemeEntity prob4 = createProbleme(
            "321 Rue Sherbrooke, Ville-Marie",
            TypeTravaux.TRAVAUX_SIGNALISATION_ECLAIRAGE,
            "Street lights not working for 3 blocks. Poor visibility at night.",
            resident4,
            Priorite.MOYENNE
        );
        
        ProblemeEntity prob5 = createProbleme(
            "654 Avenue Mont-Royal, Le Plateau",
            TypeTravaux.ENTRETIEN_PAYSAGER,
            "Overgrown trees blocking sidewalk and street signs.",
            resident5,
            Priorite.MOYENNE
        );
        
        ProblemeEntity prob6 = createProbleme(
            "987 Rue Ontario, Hochelaga",
            TypeTravaux.TRAVAUX_SOUTERRAINS,
            "Sewer backup causing flooding in basement. Water damage ongoing.",
            resident1,
            Priorite.MOYENNE
        );
        
        ProblemeEntity prob7 = createProbleme(
            "234 Avenue Papineau, Centre-Sud",
            TypeTravaux.TRAVAUX_TRANSPORTS_COMMUN,
            "Bus shelter damaged and glass shattered. Unsafe for commuters.",
            resident2,
            Priorite.MOYENNE
        );
        
        // Low priority problems
        ProblemeEntity prob8 = createProbleme(
            "567 Rue Sainte-Catherine, Downtown",
            TypeTravaux.ENTRETIEN_URBAIN,
            "Graffiti on public wall. Aesthetic improvement needed.",
            resident3,
            Priorite.FAIBLE
        );
        
        ProblemeEntity prob9 = createProbleme(
            "890 Boulevard René-Lévesque, Griffintown",
            TypeTravaux.CONSTRUCTION_RENOVATION,
            "Sidewalk uneven but passable. Minor repair recommended.",
            resident4,
            Priorite.FAIBLE
        );
        
        ProblemeEntity prob10 = createProbleme(
            "111 Rue Beaubien, Rosemont",
            TypeTravaux.ENTRETIEN_RESEAUX_TELECOM,
            "Loose telecom cables hanging low. Not urgent but should be secured.",
            resident5,
            Priorite.FAIBLE
        );
        
        // ========== CREATE APPLICATIONS ==========
        
        // Application for problem 1 (approved)
        CandidatureEntity cand1 = new CandidatureEntity();
        cand1.setPrestataire(prestataire1);
        cand1.setProblemes(List.of(prob1));
        cand1.setDescriptionProjet("Complete pothole repair with asphalt resurfacing. Includes traffic management.");
        cand1.setCoutEstime(3500.0);
        cand1.setDateDepot(LocalDateTime.now().minusDays(3));
        cand1.setDateDebutPrevue(LocalDate.now().plusDays(5));
        cand1.setDateFinPrevue(LocalDate.now().plusDays(7));
        cand1.setStatut(StatutCandidature.APPROUVEE);
        candidatureRepository.save(cand1);
        
        // Application for problem 2 (submitted)
        CandidatureEntity cand2 = new CandidatureEntity();
        cand2.setPrestataire(prestataire2);
        cand2.setProblemes(List.of(prob2));
        cand2.setDescriptionProjet("Install new stop sign with reinforced post. Includes signage and road marking.");
        cand2.setCoutEstime(1200.0);
        cand2.setDateDepot(LocalDateTime.now().minusDays(2));
        cand2.setDateDebutPrevue(LocalDate.now().plusDays(3));
        cand2.setDateFinPrevue(LocalDate.now().plusDays(4));
        cand2.setStatut(StatutCandidature.SOUMISE);
        candidatureRepository.save(cand2);
        
        // Application for problem 3 (approved)
        CandidatureEntity cand3 = new CandidatureEntity();
        cand3.setPrestataire(prestataire3);
        cand3.setProblemes(List.of(prob3));
        cand3.setDescriptionProjet("Emergency gas leak repair. 24/7 service with safety inspection.");
        cand3.setCoutEstime(5000.0);
        cand3.setDateDepot(LocalDateTime.now().minusDays(2));
        cand3.setDateDebutPrevue(LocalDate.now().plusDays(1));
        cand3.setDateFinPrevue(LocalDate.now().plusDays(2));
        cand3.setStatut(StatutCandidature.APPROUVEE);
        candidatureRepository.save(cand3);
        
        // Application for problem 4 (submitted)
        CandidatureEntity cand4 = new CandidatureEntity();
        cand4.setPrestataire(prestataire3);
        cand4.setProblemes(List.of(prob4));
        cand4.setDescriptionProjet("Replace 15 LED street lights. Energy-efficient upgrade with 5-year warranty.");
        cand4.setCoutEstime(8500.0);
        cand4.setDateDepot(LocalDateTime.now().minusDays(1));
        cand4.setDateDebutPrevue(LocalDate.now().plusDays(10));
        cand4.setDateFinPrevue(LocalDate.now().plusDays(15));
        cand4.setStatut(StatutCandidature.SOUMISE);
        candidatureRepository.save(cand4);
        
        // ========== CREATE PROJECTS ==========
        
        // Project 1: In progress (from approved application)
        ProjetEntity projet1 = new ProjetEntity();
        projet1.setPrestataire(prestataire1);
        projet1.setCandidature(cand1);
        projet1.setProblemes(List.of(prob1));
        projet1.setDescriptionProjet("Pothole repair on Rue Saint-Denis");
        projet1.setLocalisation("123 Rue Saint-Denis, Plateau-Mont-Royal");
        projet1.setTypeTravail(TypeTravaux.ENTRETIEN_URBAIN);
        projet1.setPriorite(Priorite.ELEVEE);
        projet1.setCout(3500.0);
        projet1.setDateCreation(LocalDateTime.now().minusDays(2));
        projet1.setDateDebutPrevue(LocalDate.now().plusDays(5));
        projet1.setDateFinPrevue(LocalDate.now().plusDays(7));
        projet1.setDateDebutReelle(LocalDate.now().plusDays(5));
        projet1.setStatut(StatutProjet.EN_COURS);
        projet1.setNombreRapports(0);
        projetRepository.save(projet1);
        
        // Project 2: Approved, not started yet
        ProjetEntity projet2 = new ProjetEntity();
        projet2.setPrestataire(prestataire3);
        projet2.setCandidature(cand3);
        projet2.setProblemes(List.of(prob3));
        projet2.setDescriptionProjet("Emergency gas leak repair");
        projet2.setLocalisation("789 Boulevard Saint-Laurent, Rosemont");
        projet2.setTypeTravail(TypeTravaux.TRAVAUX_GAZ_ELECTRICITE);
        projet2.setPriorite(Priorite.ELEVEE);
        projet2.setCout(5000.0);
        projet2.setDateCreation(LocalDateTime.now().minusDays(1));
        projet2.setDateDebutPrevue(LocalDate.now().plusDays(1));
        projet2.setDateFinPrevue(LocalDate.now().plusDays(2));
        projet2.setStatut(StatutProjet.APPROUVE);
        projet2.setNombreRapports(0);
        projetRepository.save(projet2);
        
        logger.info("Realistic sample data initialized: {} residents, {} service providers, {} problems, {} applications, {} projects",
            residentRepository.count(), prestataireRepository.count(), problemeRepository.count(), 
            candidatureRepository.count(), projetRepository.count());
    }
}

