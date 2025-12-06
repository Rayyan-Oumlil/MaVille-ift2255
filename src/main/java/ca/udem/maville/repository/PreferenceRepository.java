package ca.udem.maville.repository;

import ca.udem.maville.entity.PreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour les préférences utilisateur
 */
@Repository
public interface PreferenceRepository extends JpaRepository<PreferenceEntity, Long> {
    Optional<PreferenceEntity> findByEmail(String email);
    Optional<PreferenceEntity> findByNeq(String neq);
}
