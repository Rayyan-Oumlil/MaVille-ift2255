package ca.udem.maville.repository;

import ca.udem.maville.entity.ProblemeEntity;
import ca.udem.maville.modele.Priorite;
import ca.udem.maville.modele.TypeTravaux;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour les problèmes
 * Optimisé avec @EntityGraph pour éviter les N+1 queries
 */
@Repository
public interface ProblemeRepository extends JpaRepository<ProblemeEntity, Long> {
    List<ProblemeEntity> findByResoluFalse();
    
    @EntityGraph(attributePaths = {"declarant"})
    Page<ProblemeEntity> findByResoluFalse(Pageable pageable);
    
    @EntityGraph(attributePaths = {"declarant"})
    @Query(value = "SELECT p FROM ProblemeEntity p WHERE p.resolu = false AND " +
           "(:type IS NULL OR p.typeProbleme = :type)")
    Page<ProblemeEntity> findNonResolusWithFilters(
        @Param("type") TypeTravaux type,
        Pageable pageable
    );
    
    @EntityGraph(attributePaths = {"declarant"})
    List<ProblemeEntity> findByPriorite(Priorite priorite);
    
    @EntityGraph(attributePaths = {"declarant"})
    List<ProblemeEntity> findByTypeProbleme(TypeTravaux typeProbleme);
    
    @EntityGraph(attributePaths = {"declarant"})
    @Query("SELECT p FROM ProblemeEntity p WHERE p.declarant.email = :email")
    List<ProblemeEntity> findByDeclarantEmail(@Param("email") String email);
}

