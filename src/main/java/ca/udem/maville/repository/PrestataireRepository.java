package ca.udem.maville.repository;

import ca.udem.maville.entity.PrestataireEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour les prestataires
 */
@Repository
public interface PrestataireRepository extends JpaRepository<PrestataireEntity, Long> {
    Optional<PrestataireEntity> findByNumeroEntreprise(String numeroEntreprise);
    boolean existsByNumeroEntreprise(String numeroEntreprise);
}

