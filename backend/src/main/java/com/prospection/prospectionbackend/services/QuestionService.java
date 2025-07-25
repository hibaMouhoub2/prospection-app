package com.prospection.prospectionbackend.services;


import com.prospection.prospectionbackend.entities.Question;
import com.prospection.prospectionbackend.entities.QuestionOption;
import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.enums.QuestionType;
import com.prospection.prospectionbackend.enums.Role;
import com.prospection.prospectionbackend.repositories.QuestionOptionRepository;
import com.prospection.prospectionbackend.repositories.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;

import java.util.*;

@Service
@Transactional
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    public Question createQuestion(String questionText, String description, QuestionType type,
                                   Boolean obligatoire, List<String> Options, Utilisateur createur) {
        if (createur.getRole() != Role.SIEGE) {
            throw new AccessDeniedException("Vous ne pouvez pas créer des questions");
        }
        if (questionText == null || questionText.trim().isEmpty()) {
            throw new IllegalArgumentException("le texte de la question est obligatoire");
        }
        if (type.requiresOptions()) {
            if (Options == null || Options.size() < 2) {
                throw new IllegalArgumentException("les options nécessitent au moins 2 options");
            }
            long distinct = Options.stream().distinct().count();
            if (distinct != Options.size()) {
                throw new IllegalArgumentException("Les options ne peuvent pas contenir des doublons");
            }
        }
        Question question = new Question();
        question.setQuestion(questionText);
        question.setDescription(description);
        question.setType(type);
        question.setObligatoire(obligatoire != null ? obligatoire : false);
        question.setOrdre(getNextOrdre());
        question.setActif(true);
        question.setCreateurId(createur.getId());

        Question savedQuestion = questionRepository.save(question);
        if (type.requiresOptions() && Options != null) {
            for (int i = 0; i < Options.size(); i++) {
                String optionValue = Options.get(i).trim();
                if (!optionValue.isEmpty()) {
                    QuestionOption option = new QuestionOption();
                    option.setValeur(optionValue);
                    option.setOrdreOption(i + 1);
                    option.setQuestion(savedQuestion);
                    questionOptionRepository.save(option);
                }
            }
        }
        System.out.println("✅ Question créée : ID=" + savedQuestion.getId() +
                ", Type=" + type + ", Ordre=" + savedQuestion.getOrdre());
        return savedQuestion;
    }

    private Integer getNextOrdre() {
        Integer maxOrdre = questionRepository.findMaxOrdre();
        return maxOrdre + 1;
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAllOrderByOrdre();
    }

    public List<Question> getQuestionsActives() {
        return questionRepository.findAllActiveOrderByOrdre();
    }

    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    public Optional<Question> getQuestionActiveById(Long id) {
        return questionRepository.findByIdAndActifTrue(id);
    }

    public void desactiverQuestion(Long questionId, Utilisateur createur) {
        if(createur.getRole() != Role.SIEGE) {
            throw new AccessDeniedException("Vous ne pouvez pas désactiver une question");
        }
        Question question = questionRepository.findById(questionId).orElseThrow(()-> new IllegalArgumentException("Question non trouvée" ));
        if (!question.getActif())
        {
            throw new IllegalArgumentException("La question est dèjà désactivée");
        }
        question.setActif(false);
        questionRepository.save(question);
        reCalculerOrdresApresDesactivation(question.getOrdre());
        System.out.println("Question désactivée");
    }

    public void activerQuestion(Long questionId, Utilisateur createur) {
        if(createur.getRole() != Role.SIEGE) {
            throw new AccessDeniedException("Vous ne pouvez pas activer une question");
        }
        Question question =questionRepository.findById(questionId).orElseThrow(()-> new IllegalArgumentException("Question non trouvée"));
        if(question.getActif())
        {
            throw new IllegalArgumentException("Cette question est géjà active");
        }
        question.setActif(true);
        question.setOrdre(getNextOrdre());
        questionRepository.save(question);
        System.out.println("Question réactivée");
    }

    private void reCalculerOrdresApresDesactivation(Integer ordreDesactive) {
        questionRepository.decrementOrdreFrom(ordreDesactive);
    }

    public void reorgnaiserQuestion(List<Long> nouvelOrdreIds, Utilisateur createur) {
        if(createur.getRole() != Role.SIEGE) {
            throw new AccessDeniedException("Vous ne pouvez pas reorganiser une question");
        }
        if(nouvelOrdreIds == null || nouvelOrdreIds.isEmpty()) {
            throw new IllegalArgumentException("La liste des questions ne peut pas être vide");
        }
        List<Question> questionList = questionRepository.findAllById(nouvelOrdreIds);
        if(questionList.size() != nouvelOrdreIds.size()) {
            throw new IllegalArgumentException("Une ou plusieurs questions ne sont pas présentes");
        }
        for(int i =0 ; i<nouvelOrdreIds.size(); i++)
        {
            Long questionId = nouvelOrdreIds.get(i);
            int nouvelOrdre= i+ 1;
            questionRepository.updateOrdre(questionId, nouvelOrdre);
        }
        System.out.println("La rorganisation est terminée");
    }

    public Map<String, Object> getStatistiques()
    {
        Map<String, Object> statistiques = new HashMap<String, Object>();
        statistiques.put("totalQuestions", questionRepository.count());
        statistiques.put("questionsActives", questionRepository.countByActifTrue());
        List<Object[]> repartitionTypes = questionRepository.countActiveQuestionsByType();
        Map<String, Long> typesStats = new HashMap<>();
        for (Object[] row : repartitionTypes) {
            QuestionType type = (QuestionType) row[0];
            Long count = (Long) row[1];
            typesStats.put(type.getDisplayName(), count);
        }
        statistiques.put("repartitionTypes", typesStats);
        return statistiques;
    }
    public List<Question> getApercuFormulaire() {
        return getQuestionsActives();
    }

    public boolean isQuestionDupliquee(String questionText) {
        List<Question> questions = questionRepository.findAll();
        return questions.stream()
                .anyMatch(q -> q.getQuestion().trim().equalsIgnoreCase(questionText.trim()));
    }

    public void validerCreationQuestionnaire(String questionText, QuestionType questionType, List<String> options) {
        if(isQuestionDupliquee(questionText)) {
            throw new IllegalArgumentException("Cette question existe déjà");
        }
        switch (questionType){
            case CHOICE:
            case MULTIPLE_CHOICE:
                if(options == null || options.size()<2) {
                    throw new IllegalArgumentException("Les questions à choix nécessitent au moins 2 options");
                }
                for (String option : options) {
                    if(option== null || option.trim().isEmpty()) {
                        throw new IllegalArgumentException("Toutes les options doivent avoir du contenu");
                    }
                }
                break;
            case TEXT:
            case NUMBER:
            case PHONE:
                if (options != null && !options.isEmpty()) {
                    throw new IllegalArgumentException("Ce type de question ne doit pas avoir d'options");
                }
                break;

        }
    }

    public List<QuestionOption> getOptionsParQuestion(Long questionId) {
        return questionOptionRepository.findByQuestionIdOrderByOrdreOption(questionId);
    }
    public boolean peutEtreDesactivee(Long questionId) {
        return true;
    }


}
