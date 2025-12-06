package ca.udem.maville.repository;

import ca.udem.maville.entity.CandidatureEntity;
import ca.udem.maville.modele.StatutCandidature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour les candidatures
 * Optimisé avec @EntityGraph pour éviter les N+1 queries
 */
@Repository
public interface CandidatureRepository extends JpaRepository<CandidatureEntity, Long> {
    @Override
    @EntityGraph(attributePaths = {"prestataire", "problemes"})
    @NonNull
    Page<CandidatureEntity> findAll(@NonNull Pageable pageable);
    
    @EntityGraph(attributePaths = {"prestataire", "problemes"})
    List<CandidatureEntity> findByStatut(StatutCandidature statut);
    
    @EntityGraph(attributePaths = {"prestataire", "problemes"})
    @Query("SELECT c FROM CandidatureEntity c WHERE c.prestataire.numeroEntreprise = :neq")
    List<CandidatureEntity> findByPrestataireNeq(@Param("neq") String neq);
    
    @EntityGraph(attributePaths = {"prestataire", "problemes"})
    @Query("SELECT c FROM CandidatureEntity c WHERE c.statut = :statut")
    Page<CandidatureEntity> findByStatut(@Param("statut") StatutCandidature statut, Pageable pageable);
}

