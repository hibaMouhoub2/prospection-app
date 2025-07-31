package com.prospection.prospectionbackend.repositories;

import com.prospection.prospectionbackend.entities.Prospection;
import com.prospection.prospectionbackend.enums.StatutProspection;
import com.prospection.prospectionbackend.enums.TypeProspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProspectionRepository extends JpaRepository<Prospection, Long> {
    List<Prospection> findByCreateurIdOrderByDateCreationDesc(Long createurId);
    List<Prospection> findByAgentAssigneIdOrderByDateCreationDesc(Long agentId);
    @Query("SELECT p FROM Prospection p WHERE p.createur.id = :agentId OR p.agentAssigne.id = :agentId ORDER BY p.dateCreation DESC")
    List<Prospection> findByAgentIdOrderByDateCreationDesc(@Param("agentId") Long agentId);
    List<Prospection> findByBrancheIdOrderByDateCreationDesc(Long brancheId);
    List<Prospection> findBySupervisionIdOrderByDateCreationDesc(Long supervisionId);
    List<Prospection> findByRegionIdOrderByDateCreationDesc(Long regionId);
    List<Prospection> findByStatutOrderByDateCreationDesc(StatutProspection statut);
    List<Prospection> findByTypeProspectionOrderByDateCreationDesc(TypeProspection typeProspection);
    @Query("SELECT p FROM Prospection p WHERE (p.createur.id = :agentId OR p.agentAssigne.id = :agentId) AND p.typeProspection = :type ORDER BY p.dateCreation DESC")
    List<Prospection> findByAgentIdAndTypeProspectionOrderByDateCreationDesc(@Param("agentId") Long agentId, @Param("type") TypeProspection type);
    @Query("SELECT p FROM Prospection p WHERE " +
            "(:agentId IS NULL OR p.createur.id = :agentId OR p.agentAssigne.id = :agentId) AND " +
            "(:statut IS NULL OR p.statut = :statut) AND " +
            "(:type IS NULL OR p.typeProspection = :type) AND " +
            "(:brancheId IS NULL OR p.branche.id = :brancheId) AND " +
            "(:supervisionId IS NULL OR p.supervision.id = :supervisionId) AND " +
            "(:regionId IS NULL OR p.region.id = :regionId) AND " +
            "(:dateDebut IS NULL OR p.dateCreation >= :dateDebut) AND " +
            "(:dateFin IS NULL OR p.dateCreation <= :dateFin) " +
            "ORDER BY p.dateCreation DESC")
    Page<Prospection> findWithFilters(
            @Param("agentId") Long agentId,
            @Param("statut") StatutProspection statut,
            @Param("type") TypeProspection type,
            @Param("brancheId") Long brancheId,
            @Param("supervisionId") Long supervisionId,
            @Param("regionId") Long regionId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin,
            Pageable pageable
    );
    @Query("SELECT p FROM Prospection p WHERE " +
            "LOWER(p.commentaire) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY p.dateCreation DESC")
    List<Prospection> findBySearchTerm(@Param("searchTerm") String searchTerm);


    @Query("SELECT p.statut, COUNT(p) FROM Prospection p GROUP BY p.statut")
    List<Object[]> countByStatut();


    @Query("SELECT p.statut, COUNT(p) FROM Prospection p WHERE p.createur.id = :agentId OR p.agentAssigne.id = :agentId GROUP BY p.statut")
    List<Object[]> countByAgentIdAndStatut(@Param("agentId") Long agentId);

    @Query("SELECT p.typeProspection, COUNT(p) FROM Prospection p GROUP BY p.typeProspection")
    List<Object[]> countByTypeProspection();


    @Query("SELECT p.statut, COUNT(p) FROM Prospection p WHERE p.branche.id = :brancheId GROUP BY p.statut")
    List<Object[]> countByBrancheIdAndStatut(@Param("brancheId") Long brancheId);


    @Query("SELECT p FROM Prospection p WHERE p.statut = 'EN_COURS' AND " +
            "(p.dateDerniereRelance IS NULL OR p.dateDerniereRelance < :dateLimit) " +
            "ORDER BY p.dateCreation ASC")
    List<Prospection> findProspectionsARelancer(@Param("dateLimit") LocalDateTime dateLimit);


    @Query("SELECT p FROM Prospection p WHERE p.statut = 'CONVERTI' AND p.dateConversion BETWEEN :debut AND :fin ORDER BY p.dateConversion DESC")
    List<Prospection> findConvertiesBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);


    List<Prospection> findByAgentAssigneIsNullOrderByDateCreationDesc();


    @Query("SELECT p FROM Prospection p WHERE p.statut IN ('NOUVEAU', 'ASSIGNE', 'EN_COURS') ORDER BY p.dateCreation DESC")
    List<Prospection> findProspectionsActives();


    @Query("SELECT COUNT(p) FROM Prospection p WHERE p.createur.id = :agentId AND p.dateCreation >= :startOfDay AND p.dateCreation < :endOfDay")
    long countByCreateurIdAndToday(@Param("agentId") Long agentId , @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    Optional<Prospection> findTopByCreateurIdOrderByDateCreationDesc(Long createurId);


}
