package com.prospection.prospectionbackend.repositories;

import com.prospection.prospectionbackend.entities.Reponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelanceRepository extends JpaRepository<Reponse, Long> {

    // ===============================
    // RECHERCHES DE BASE
    // ===============================

    /**
     * Trouve toutes les réponses d'une prospection
     */
    List<Reponse> findByProspectionIdOrderByQuestionOrdre(Long prospectionId);

    /**
     * Trouve toutes les réponses à une question spécifique
     */
    List<Reponse> findByQuestionIdOrderByDateCreation(Long questionId);

    /**
     * Trouve une réponse spécifique pour une question et une prospection
     */
    Optional<Reponse> findByQuestionIdAndProspectionId(Long questionId, Long prospectionId);

    // ===============================
    // RECHERCHES PAR VALEUR
    // ===============================

    /**
     * Trouve les réponses contenant une valeur spécifique
     */
    @Query("SELECT r FROM Reponse r WHERE LOWER(r.valeur) LIKE LOWER(CONCAT('%', :valeur, '%'))")
    List<Reponse> findByValeurContainingIgnoreCase(@Param("valeur") String valeur);

    /**
     * Trouve les réponses avec une valeur exacte
     */
    List<Reponse> findByValeur(String valeur);

    /**
     * Trouve les réponses vides ou nulles
     */
    @Query("SELECT r FROM Reponse r WHERE r.valeur IS NULL OR r.valeur = '' OR TRIM(r.valeur) = ''")
    List<Reponse> findReponsesVides();

    /**
     * Trouve les réponses non vides
     */
    @Query("SELECT r FROM Reponse r WHERE r.valeur IS NOT NULL AND r.valeur != '' AND TRIM(r.valeur) != ''")
    List<Reponse> findReponsesNonVides();

    // ===============================
    // RECHERCHES AVEC JOINTURES
    // ===============================

    /**
     * Trouve les réponses d'une prospection avec les détails des questions
     */
    @Query("SELECT r FROM Reponse r JOIN FETCH r.question q WHERE r.prospection.id = :prospectionId ORDER BY q.ordre")
    List<Reponse> findByProspectionIdWithQuestion(@Param("prospectionId") Long prospectionId);

    /**
     * Trouve les réponses à une question avec les détails de la prospection
     */
    @Query("SELECT r FROM Reponse r JOIN FETCH r.prospection p WHERE r.question.id = :questionId ORDER BY p.dateCreation DESC")
    List<Reponse> findByQuestionIdWithProspection(@Param("questionId") Long questionId);

    // ===============================
    // RECHERCHES PAR TYPE DE QUESTION
    // ===============================

    /**
     * Trouve les réponses aux questions de type CHOICE
     */
    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'CHOICE'")
    List<Reponse> findReponsesChoixUnique();

    /**
     * Trouve les réponses aux questions de type MULTIPLE_CHOICE
     */
    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'MULTIPLE_CHOICE'")
    List<Reponse> findReponsesChoixMultiple();

    /**
     * Trouve les réponses aux questions de type TEXT
     */
    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'TEXT'")
    List<Reponse> findReponsesTexte();

    /**
     * Trouve les réponses aux questions de type NUMBER
     */
    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'NUMBER'")
    List<Reponse> findReponsesNombre();

    // ===============================
    // STATISTIQUES GÉNÉRALES
    // ===============================

    /**
     * Compte le nombre total de réponses
     */
    @Query("SELECT COUNT(r) FROM Reponse r")
    long countTotalReponses();

    /**
     * Compte les réponses par question
     */
    @Query("SELECT r.question.id, COUNT(r) FROM Reponse r GROUP BY r.question.id")
    List<Object[]> countReponsesByQuestion();

    /**
     * Compte les réponses par prospection
     */
    @Query("SELECT r.prospection.id, COUNT(r) FROM Reponse r GROUP BY r.prospection.id")
    List<Object[]> countReponsesByProspection();

    // ===============================
    // STATISTIQUES PAR QUESTION SPÉCIFIQUE
    // ===============================

    /**
     * Compte les réponses pour une question spécifique
     */
    long countByQuestionId(Long questionId);

    /**
     * Statistiques des valeurs pour une question à choix unique
     */
    @Query("SELECT r.valeur, COUNT(r) FROM Reponse r WHERE r.question.id = :questionId AND r.valeur IS NOT NULL GROUP BY r.valeur ORDER BY COUNT(r) DESC")
    List<Object[]> getStatistiquesChoixUnique(@Param("questionId") Long questionId);

    /**
     * Statistiques des valeurs pour questions numériques (version simplifiée)
     */
    @Query("SELECT COUNT(r) FROM Reponse r WHERE r.question.id = :questionId AND r.valeur IS NOT NULL")
    long countReponsesNumeriques(@Param("questionId") Long questionId);

    // ===============================
    // RECHERCHES POUR ANALYTICS
    // ===============================

    /**
     * Trouve les réponses pour les graphiques (questions à choix)
     */
    @Query("SELECT q.question, r.valeur, COUNT(r) FROM Reponse r JOIN r.question q WHERE q.type IN ('CHOICE', 'MULTIPLE_CHOICE') GROUP BY q.question, r.valeur ORDER BY q.ordre, COUNT(r) DESC")
    List<Object[]> getDonneesGraphiques();

    /**
     * Trouve les réponses d'une question spécifique pour graphique
     */
    @Query("SELECT r.valeur, COUNT(r) FROM Reponse r WHERE r.question.id = :questionId AND r.valeur IS NOT NULL GROUP BY r.valeur ORDER BY COUNT(r) DESC")
    List<Object[]> getDonneesGraphiqueParQuestion(@Param("questionId") Long questionId);

    // ===============================
    // RECHERCHES PAR PÉRIODE
    // ===============================

    /**
     * Trouve les réponses créées dans une période
     */
    @Query("SELECT r FROM Reponse r WHERE r.dateCreation BETWEEN :debut AND :fin ORDER BY r.dateCreation DESC")
    List<Reponse> findByDateCreationBetween(@Param("debut") java.time.LocalDateTime debut, @Param("fin") java.time.LocalDateTime fin);

    // ===============================
    // VALIDATION ET COHÉRENCE
    // ===============================

    /**
     * Vérifie l'existence d'une réponse pour une question obligatoire
     */
    @Query("SELECT COUNT(r) > 0 FROM Reponse r WHERE r.question.id = :questionId AND r.prospection.id = :prospectionId AND r.valeur IS NOT NULL AND TRIM(r.valeur) != ''")
    boolean existsValidReponseForQuestionAndProspection(@Param("questionId") Long questionId, @Param("prospectionId") Long prospectionId);

    /**
     * Trouve les prospections avec des réponses manquantes pour questions obligatoires
     */
    @Query("SELECT DISTINCT p.id FROM Prospection p, Question q WHERE q.obligatoire = true AND NOT EXISTS (SELECT r FROM Reponse r WHERE r.prospection.id = p.id AND r.question.id = q.id AND r.valeur IS NOT NULL AND TRIM(r.valeur) != '')")
    List<Long> findProspectionsWithMissingRequiredAnswers();

    // ===============================
    // RECHERCHES SPÉCIALISÉES
    // ===============================

    /**
     * Trouve les réponses téléphone pour vérification de doublons
     */
    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'PHONE' AND r.valeur = :telephone")
    List<Reponse> findByTelephone(@Param("telephone") String telephone);

    /**
     * Trouve les réponses email pour vérification de doublons
     */
    @Query("SELECT r FROM Reponse r JOIN r.question q WHERE q.type = 'EMAIL' AND LOWER(r.valeur) = LOWER(:email)")
    List<Reponse> findByEmail(@Param("email") String email);

    /**
     * Supprime toutes les réponses d'une prospection
     */
    void deleteByProspectionId(Long prospectionId);

    /**
     * Supprime toutes les réponses à une question (quand question supprimée)
     */
    void deleteByQuestionId(Long questionId);
}