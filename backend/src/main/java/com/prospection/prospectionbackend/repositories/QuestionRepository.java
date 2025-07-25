package com.prospection.prospectionbackend.repositories;

import com.prospection.prospectionbackend.entities.Question;
import com.prospection.prospectionbackend.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {


    @Query("SELECT q FROM Question q WHERE q.actif = true ORDER BY q.ordre ASC")
    List<Question> findAllActiveOrderByOrdre();


    @Query("SELECT q FROM Question q ORDER BY q.ordre ASC")
    List<Question> findAllOrderByOrdre();


    List<Question> findByTypeAndActifTrueOrderByOrdre(QuestionType type);


    Optional<Question> findByIdAndActifTrue(Long id);


    long countByActifTrue();


    @Query("SELECT COALESCE(MAX(q.ordre), 0) FROM Question q")
    Integer findMaxOrdre();


    @Query("SELECT q FROM Question q WHERE q.ordre BETWEEN :startOrdre AND :endOrdre ORDER BY q.ordre ASC")
    List<Question> findByOrdreBetween(@Param("startOrdre") Integer startOrdre, @Param("endOrdre") Integer endOrdre);


    @Modifying
    @Query("UPDATE Question q SET q.ordre = :newOrdre WHERE q.id = :questionId")
    void updateOrdre(@Param("questionId") Long questionId, @Param("newOrdre") Integer newOrdre);


    @Modifying
    @Query("UPDATE Question q SET q.ordre = q.ordre + 1 WHERE q.ordre >= :fromOrdre")
    void incrementOrdreFrom(@Param("fromOrdre") Integer fromOrdre);

    @Modifying
    @Query("UPDATE Question q SET q.ordre = q.ordre - 1 WHERE q.ordre > :fromOrdre")
    void decrementOrdreFrom(@Param("fromOrdre") Integer fromOrdre);


    boolean existsByOrdre(Integer ordre);


    List<Question> findByCreateurIdOrderByDateCreationDesc(Long createurId);


    @Query("SELECT q.type, COUNT(q) FROM Question q WHERE q.actif = true GROUP BY q.type")
    List<Object[]> countActiveQuestionsByType();
}