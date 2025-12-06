package ca.udem.maville.repository;

import ca.udem.maville.entity.ResidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour les résidents
 */
@Repository
public interface ResidentRepository extends JpaRepository<ResidentEntity, Long> {
    Optional<ResidentEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}

