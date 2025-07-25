package com.prospection.prospectionbackend.repositories;

import com.prospection.prospectionbackend.entities.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {


    List<QuestionOption> findByQuestionIdOrderByOrdreOption(Long questionId);


    long countByQuestionId(Long questionId);


    @Query("SELECT COALESCE(MAX(qo.ordreOption), 0) FROM QuestionOption qo WHERE qo.question.id = :questionId")
    Integer findMaxOrdreOptionByQuestionId(@Param("questionId") Long questionId);


    @Modifying
    @Query("DELETE FROM QuestionOption qo WHERE qo.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);


    @Modifying
    @Query("UPDATE QuestionOption qo SET qo.ordreOption = :newOrdre WHERE qo.id = :optionId")
    void updateOrdreOption(@Param("optionId") Long optionId, @Param("newOrdre") Integer newOrdre);


    @Modifying
    @Query("UPDATE QuestionOption qo SET qo.ordreOption = qo.ordreOption + 1 WHERE qo.question.id = :questionId AND qo.ordreOption >= :fromOrdre")
    void incrementOrdreOptionFrom(@Param("questionId") Long questionId, @Param("fromOrdre") Integer fromOrdre);


    @Modifying
    @Query("UPDATE QuestionOption qo SET qo.ordreOption = qo.ordreOption - 1 WHERE qo.question.id = :questionId AND qo.ordreOption > :fromOrdre")
    void decrementOrdreOptionFrom(@Param("questionId") Long questionId, @Param("fromOrdre") Integer fromOrdre);


    boolean existsByQuestionIdAndOrdreOption(Long questionId, Integer ordreOption);


    List<QuestionOption> findByQuestionIdAndValeur(Long questionId, String valeur);


    boolean existsByQuestionIdAndValeur(Long questionId, String valeur);
}