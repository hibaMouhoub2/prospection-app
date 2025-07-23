package com.prospection.prospectionbackend.repositories;

import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByEmailAndActifTrue(String email);
    Boolean existsByEmail(String email);
    List<Utilisateur> findByActifTrue();
    List<Utilisateur> findByRole(Role role);
    @Query("SELECT u FROM Utilisateur u WHERE u.region.id = :regionId AND u.actif = true")
    List<Utilisateur> findByRegionIdAndActifTrue(@Param("regionId") Long regionId);
    @Query("SELECT u FROM Utilisateur u WHERE u.supervision.id = :supervisionId AND u.actif= true")
    List<Utilisateur> findBySupervisionIdAndActifTrue(@Param("supervisionId") Long supervisionId);
    @Query ("SELECT u FROM Utilisateur u WHERE u.branche.id =:brancheid AND u.actif=true")
    List<Utilisateur> findByBrancheIdAndActifTrue(@Param("brancheId") Long brancheId);
    @Query("SELECT u FROM Utilisateur u WHERE u.actif = true AND " +
            "(u.region.id = :regionId OR u.supervision.region.id = :regionId OR u.branche.supervision.region.id = :regionId)")
    List<Utilisateur> findUtilisateursAccessiblesByRegion(@Param("regionId") Long regionId);
    @Query("SELECT u.role, COUNT(u) FROM Utilisateur u WHERE u.actif = true GROUP BY u.role")
    List<Object[]> countUtilisateursByRole();
}
