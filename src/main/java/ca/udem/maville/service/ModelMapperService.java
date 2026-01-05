package ca.udem.maville.service;

import ca.udem.maville.entity.*;
import ca.udem.maville.modele.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour convertir entre les modèles (modele.*) et les entités JPA (entity.*)
 */
@Service
public class ModelMapperService {
    
    
    public ResidentEntity toEntity(Resident resident) {
        if (resident == null) return null;
        ResidentEntity entity = new ResidentEntity();
        entity.setPrenom(resident.getPrenom());
        entity.setNom(resident.getNom());
        entity.setEmail(resident.getEmail());
        entity.setTelephone(resident.getTelephone());
        entity.setAdresse(resident.getAdresse());
        return entity;
    }
    
    public Resident toModel(ResidentEntity entity) {
        if (entity == null) return null;
        return new Resident(entity.getNom(), entity.getPrenom(), 
                          entity.getTelephone(), entity.getEmail(), entity.getAdresse());
    }
    
    
    public PrestataireEntity toEntity(Prestataire prestataire) {
        if (prestataire == null) return null;
        PrestataireEntity entity = new PrestataireEntity();
        entity.setNumeroEntreprise(prestataire.getNumeroEntreprise());
        entity.setNomEntreprise(prestataire.getNomEntreprise());
        entity.setContactNom(prestataire.getContactNom());
        entity.setTelephone(prestataire.getTelephone());
        entity.setEmail(prestataire.getEmail());
        return entity;
    }
    
    public Prestataire toModel(PrestataireEntity entity) {
        if (entity == null) return null;
        return new Prestataire(entity.getNumeroEntreprise(), entity.getNomEntreprise(),
                              entity.getContactNom(), entity.getTelephone(), entity.getEmail());
    }
    
    
    public ProblemeEntity toEntity(Probleme probleme, ResidentEntity declarant) {
        if (probleme == null) return null;
        ProblemeEntity entity = new ProblemeEntity();
        entity.setId(probleme.getId() > 0 ? Long.valueOf(probleme.getId()) : null);
        entity.setLieu(probleme.getLieu());
        entity.setTypeProbleme(probleme.getTypeProbleme());
        entity.setDescription(probleme.getDescription());
        entity.setDeclarant(declarant);
        entity.setDateSignalement(probleme.getDateSignalement());
        entity.setPriorite(probleme.getPriorite());
        entity.setResolu(probleme.isResolu());
        return entity;
    }
    
    public Probleme toModel(ProblemeEntity entity) {
        if (entity == null) return null;
        Probleme probleme = new Probleme(entity.getLieu(), entity.getTypeProbleme(),
                                         entity.getDescription(), toModel(entity.getDeclarant()));
        probleme.setId(entity.getId() != null ? entity.getId().intValue() : 0);
        probleme.setDateSignalement(entity.getDateSignalement());
        probleme.setPriorite(entity.getPriorite());
        probleme.setResolu(entity.isResolu());
        return probleme;
    }
    
    
    public CandidatureEntity toEntity(Candidature candidature, PrestataireEntity prestataire,
                                     List<ProblemeEntity> problemes) {
        if (candidature == null) return null;
        CandidatureEntity entity = new CandidatureEntity();
        entity.setId(candidature.getId() > 0 ? Long.valueOf(candidature.getId()) : null);
        entity.setPrestataire(prestataire);
        entity.setProblemes(problemes);
        entity.setDescriptionProjet(candidature.getDescriptionProjet());
        entity.setCoutEstime(candidature.getCoutEstime());
        entity.setDateDebutPrevue(candidature.getDateDebutPrevue());
        entity.setDateFinPrevue(candidature.getDateFinPrevue());
        entity.setDateDepot(candidature.getDateDepot());
        entity.setStatut(candidature.getStatut());
        entity.setCommentaireRejet(candidature.getCommentaireRejet());
        return entity;
    }
    
    public Candidature toModel(CandidatureEntity entity) {
        if (entity == null) return null;
        List<Integer> problemesIds = entity.getProblemes().stream()
            .map(p -> p.getId().intValue())
            .collect(Collectors.toList());
        
        Candidature candidature = new Candidature(
            toModel(entity.getPrestataire()),
            problemesIds,
            entity.getDescriptionProjet(),
            entity.getCoutEstime(),
            entity.getDateDebutPrevue(),
            entity.getDateFinPrevue()
        );
        candidature.setId(entity.getId() != null ? entity.getId().intValue() : 0);
        candidature.setDateDepot(entity.getDateDepot());
        candidature.setStatut(entity.getStatut());
        candidature.setCommentaireRejet(entity.getCommentaireRejet());
        return candidature;
    }
    
    
    public ProjetEntity toEntity(Projet projet, CandidatureEntity candidature,
                                List<ProblemeEntity> problemes, PrestataireEntity prestataire) {
        if (projet == null) return null;
        ProjetEntity entity = new ProjetEntity(candidature, problemes, prestataire);
        entity.setId(projet.getId() > 0 ? Long.valueOf(projet.getId()) : null);
        entity.setLocalisation(projet.getLocalisation());
        entity.setStatut(projet.getStatut());
        entity.setPriorite(projet.getPriorite());
        entity.setTypeTravail(projet.getTypeTravail());
        entity.setDateDebutPrevue(projet.getDateDebutPrevue());
        entity.setDateFinPrevue(projet.getDateFinPrevue());
        entity.setDateDebutReelle(projet.getDateDebutReelle());
        entity.setDateFinReelle(projet.getDateFinReelle());
        entity.setDescriptionProjet(projet.getDescriptionProjet());
        entity.setCout(projet.getCout());
        entity.setDateCreation(projet.getDateCreation());
        entity.setDerniereMiseAJour(projet.getDerniereMiseAJour());
        entity.setNombreRapports(projet.getNombreRapports());
        return entity;
    }
    
    public Projet toModel(ProjetEntity entity) {
        if (entity == null) return null;
        List<Integer> problemesIds = entity.getProblemes().stream()
            .map(p -> p.getId().intValue())
            .collect(Collectors.toList());
        
        Projet projet = new Projet();
        projet.setId(entity.getId() != null ? entity.getId().intValue() : 0);
        projet.setProblemesVises(problemesIds);
        projet.setLocalisation(entity.getLocalisation());
        projet.setStatut(entity.getStatut());
        projet.setPriorite(entity.getPriorite());
        projet.setTypeTravail(entity.getTypeTravail());
        projet.setDateDebutPrevue(entity.getDateDebutPrevue());
        projet.setDateFinPrevue(entity.getDateFinPrevue());
        projet.setDateDebutReelle(entity.getDateDebutReelle());
        projet.setDateFinReelle(entity.getDateFinReelle());
        projet.setPrestataire(toModel(entity.getPrestataire()));
        projet.setDescriptionProjet(entity.getDescriptionProjet());
        projet.setCout(entity.getCout());
        projet.setDateCreation(entity.getDateCreation());
        projet.setDerniereMiseAJour(entity.getDerniereMiseAJour());
        projet.setNombreRapports(entity.getNombreRapports());
        return projet;
    }
    
    
    public NotificationEntity toEntity(Notification notification) {
        if (notification == null) return null;
        NotificationEntity entity = new NotificationEntity();
        entity.setMessage(notification.getMessage());
        entity.setTypeChangement(notification.getTypeChangement());
        entity.setDateCreation(notification.getDateCreation());
        entity.setLu(notification.isLu());
        entity.setResidentEmail(notification.getResidentEmail());
        entity.setProjetId(notification.getProjetId() != 0 ? Long.valueOf(notification.getProjetId()) : null);
        entity.setTypeDestinataire(notification.getTypeDestinataire());
        entity.setDestinataire(notification.getDestinataire());
        return entity;
    }
    
    public Notification toModel(NotificationEntity entity) {
        if (entity == null) return null;
        Notification notification = new Notification();
        notification.setMessage(entity.getMessage());
        notification.setTypeChangement(entity.getTypeChangement());
        notification.setDateCreation(entity.getDateCreation());
        notification.setLu(entity.isLu());
        notification.setResidentEmail(entity.getResidentEmail());
        notification.setProjetId(entity.getProjetId() != null ? entity.getProjetId().intValue() : 0);
        notification.setTypeDestinataire(entity.getTypeDestinataire());
        notification.setDestinataire(entity.getDestinataire());
        return notification;
    }
    
    
    public AbonnementEntity toEntity(Abonnement abonnement) {
        if (abonnement == null) return null;
        AbonnementEntity entity = new AbonnementEntity();
        entity.setResidentEmail(abonnement.getResidentEmail());
        entity.setType(abonnement.getType());
        entity.setValeur(abonnement.getValeur());
        return entity;
    }
    
    public Abonnement toModel(AbonnementEntity entity) {
        if (entity == null) return null;
        return new Abonnement(entity.getResidentEmail(), entity.getType(), entity.getValeur());
    }
}

