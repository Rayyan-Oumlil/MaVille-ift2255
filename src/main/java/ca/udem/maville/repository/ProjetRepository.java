package ca.udem.maville.repository;

import ca.udem.maville.entity.ProjetEntity;
import ca.udem.maville.modele.StatutProjet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour les projets
 * Optimisé avec @EntityGraph pour éviter les N+1 queries
 */
@Repository
public interface ProjetRepository extends JpaRepository<ProjetEntity, Long> {
    @EntityGraph(attributePaths = {"prestataire", "problemes"})
    @Query("SELECT p FROM ProjetEntity p WHERE p.prestataire.numeroEntreprise = :neq")
    List<ProjetEntity> findByPrestataireNeq(@Param("neq") String neq);
    
    @EntityGraph(attributePaths = {"prestataire", "problemes"})
    List<ProjetEntity> findByStatut(StatutProjet statut);
    
    @EntityGraph(attributePaths = {"prestataire", "problemes"})
    @Query("SELECT DISTINCT p FROM ProjetEntity p JOIN p.problemes pr WHERE pr.id = :problemeId")
    List<ProjetEntity> findByProblemeId(@Param("problemeId") Long problemeId);
}

