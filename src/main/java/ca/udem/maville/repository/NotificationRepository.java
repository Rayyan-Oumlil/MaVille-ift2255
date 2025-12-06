package ca.udem.maville.repository;

import ca.udem.maville.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour les notifications
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    @Query("SELECT n FROM NotificationEntity n WHERE n.residentEmail = :email ORDER BY n.dateCreation DESC")
    List<NotificationEntity> findByResidentEmail(@Param("email") String email);
    
    @Query("SELECT n FROM NotificationEntity n WHERE n.typeDestinataire = 'STPM' ORDER BY n.dateCreation DESC")
    List<NotificationEntity> findStpmNotifications();
    
    @Query("SELECT n FROM NotificationEntity n WHERE n.typeDestinataire = 'PRESTATAIRE' AND " +
           "(n.destinataire = :neq OR n.destinataire IS NULL) ORDER BY n.dateCreation DESC")
    List<NotificationEntity> findPrestataireNotifications(@Param("neq") String neq);
    
    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.residentEmail = :email AND n.lu = false")
    long countUnreadByResidentEmail(@Param("email") String email);
}

