package com.prospection.prospectionbackend.repositories;

import com.prospection.prospectionbackend.entities.Reponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReponseRepository extends JpaRepository<Reponse, Long> {


    List<Reponse> findByProspectionIdOrderByQuestionOrdre(Long prospectionId);


    List<Reponse> findByQuestionIdOrderByDateCreation(Long questionId);


    Optional<Reponse> findByQuestionIdAndProspectionId(Long questionId, Long prospectionId);


    @Query("SELECT r FROM Reponse r WHERE LOWER(r.valeur) LIKE LOWER(CONCAT('%', :valeur, '%'))")
    List<Reponse> findByValeurContainingIgnoreCase(@Param("valeur") String valeur);


    List<Reponse> findByValeur(String valeur);

    @Query("SELECT r FROM Reponse r WHERE r.valeur IS NULL OR r.valeur = '' OR TRIM(r.valeur) = ''")
    List<Reponse> findReponsesVides();


    @Query("SELECT r FROM Reponse r WHERE r.valeur IS NOT NULL AND r.valeur != '' AND TRIM(r.valeur) != ''")
    List<Reponse> findReponsesNonVides();

    @Query("SELECT r FROM Reponse r JOIN FETCH r.question q WHERE r.prospection.id = :prospectionId ORDER BY q.ordre")
    List<Reponse> findByProspectionIdWithQuestion(@Param("prospectionId") Long prospectionId);


    @Query("SELECT r FROM Reponse r JOIN FETCH r.prospection p WHERE r.question.id = :questionId ORDER BY p.dateCreation DESC")
    List<Reponse> findByQuestionIdWithProspection(@Param("questionId") Long questionId);



    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'CHOICE'")
    List<Reponse> findReponsesChoixUnique();


    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'MULTIPLE_CHOICE'")
    List<Reponse> findReponsesChoixMultiple();


    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'TEXT'")
    List<Reponse> findReponsesTexte();


    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'NUMBER'")
    List<Reponse> findReponsesNombre();


    @Query("SELECT COUNT(r) FROM Reponse r")
    long countTotalReponses();


    @Query("SELECT r.question.id, COUNT(r) FROM Reponse r GROUP BY r.question.id")
    List<Object[]> countReponsesByQuestion();


    @Query("SELECT r.prospection.id, COUNT(r) FROM Reponse r GROUP BY r.prospection.id")
    List<Object[]> countReponsesByProspection();


    long countByQuestionId(Long questionId);


    @Query("SELECT r.valeur, COUNT(r) FROM Reponse r WHERE r.question.id = :questionId AND r.valeur IS NOT NULL GROUP BY r.valeur ORDER BY COUNT(r) DESC")
    List<Object[]> getStatistiquesChoixUnique(@Param("questionId") Long questionId);

//    /**
//     * Statistiques des valeurs pour questions numériques
//     */
//    @Query("SELECT MIN(CAST(r.valeur AS double)), MAX(CAST(r.valeur AS double)), AVG(CAST(r.valeur AS double)) FROM Reponse r WHERE r.question.id = :questionId AND r.valeur IS NOT NULL AND r.valeur REGEXP '^[0-9]+$'")
//    List<Object[]> getStatistiquesNumeriques(@Param("questionId") Long questionId);


    @Query("SELECT q.question, r.valeur, COUNT(r) FROM Reponse r JOIN r.question q WHERE q.type IN ('CHOICE', 'MULTIPLE_CHOICE') GROUP BY q.question, r.valeur ORDER BY q.ordre, COUNT(r) DESC")
    List<Object[]> getDonneesGraphiques();


    @Query("SELECT r.valeur, COUNT(r) FROM Reponse r WHERE r.question.id = :questionId AND r.valeur IS NOT NULL GROUP BY r.valeur ORDER BY COUNT(r) DESC")
    List<Object[]> getDonneesGraphiqueParQuestion(@Param("questionId") Long questionId);


    @Query("SELECT r FROM Reponse r WHERE r.dateCreation BETWEEN :debut AND :fin ORDER BY r.dateCreation DESC")
    List<Reponse> findByDateCreationBetween(@Param("debut") java.time.LocalDateTime debut, @Param("fin") java.time.LocalDateTime fin);


    @Query("SELECT COUNT(r) > 0 FROM Reponse r WHERE r.question.id = :questionId AND r.prospection.id = :prospectionId AND r.valeur IS NOT NULL AND TRIM(r.valeur) != ''")
    boolean existsValidReponseForQuestionAndProspection(@Param("questionId") Long questionId, @Param("prospectionId") Long prospectionId);


    @Query("SELECT DISTINCT p.id FROM Prospection p, Question q WHERE q.obligatoire = true AND NOT EXISTS (SELECT r FROM Reponse r WHERE r.prospection.id = p.id AND r.question.id = q.id AND r.valeur IS NOT NULL AND TRIM(r.valeur) != '')")
    List<Long> findProspectionsWithMissingRequiredAnswers();


    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'PHONE' AND r.valeur = :telephone")
    List<Reponse> findByTelephone(@Param("telephone") String telephone);


    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'EMAIL' AND LOWER(r.valeur) = LOWER(:email)")
    List<Reponse> findByEmail(@Param("email") String email);


    void deleteByProspectionId(Long prospectionId);


    void deleteByQuestionId(Long questionId);
}