package ca.udem.maville.repository;

import ca.udem.maville.entity.AbonnementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour les abonnements
 */
@Repository
public interface AbonnementRepository extends JpaRepository<AbonnementEntity, Long> {
    @Query("SELECT a FROM AbonnementEntity a WHERE a.residentEmail = :email")
    List<AbonnementEntity> findByResidentEmail(@Param("email") String email);
    
    @Query("SELECT a FROM AbonnementEntity a WHERE a.residentEmail = :email AND a.type = :type AND a.valeur = :valeur")
    List<AbonnementEntity> findByResidentEmailAndTypeAndValeur(
        @Param("email") String email,
        @Param("type") String type,
        @Param("valeur") String valeur
    );
    
    @Query("SELECT a FROM AbonnementEntity a WHERE a.type = :type AND a.valeur = :valeur")
    List<AbonnementEntity> findByTypeAndValeur(@Param("type") String type, @Param("valeur") String valeur);
}

