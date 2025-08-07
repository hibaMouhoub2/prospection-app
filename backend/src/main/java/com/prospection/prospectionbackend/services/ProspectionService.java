package com.prospection.prospectionbackend.services;

import com.prospection.prospectionbackend.entities.*;
import com.prospection.prospectionbackend.enums.Role;
import com.prospection.prospectionbackend.enums.StatutProspection;
import com.prospection.prospectionbackend.enums.TypeProspection;
import com.prospection.prospectionbackend.repositories.ProspectionRepository;
import com.prospection.prospectionbackend.repositories.QuestionRepository;
import com.prospection.prospectionbackend.repositories.ReponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProspectionService {

    @Autowired
    private ProspectionRepository prospectionRepository;

    @Autowired
    private ReponseRepository reponseRepository;

    @Autowired
    private QuestionRepository questionRepository;


    public Prospection creerProspection(
            TypeProspection typeProspection,
            Map<Long, String> reponses,
            String commentaire,
            Utilisateur createur) {


        if (!peutCreerProspection(createur)) {
            throw new AccessDeniedException("Vous n'avez pas le droit de créer une prospection");
        }


        validerDonneesProspection(typeProspection, reponses);


        Prospection prospection = new Prospection();
        prospection.setTypeProspection(typeProspection);
        prospection.setStatut(StatutProspection.NOUVEAU);
        prospection.setCreateur(createur);
        prospection.setCommentaire(commentaire);


        assignerHierarchie(prospection, createur);


        assignerSelonType(prospection, typeProspection);


        prospection = prospectionRepository.save(prospection);


        List<Reponse> reponsesEntities = creerReponses(prospection, reponses);
        reponseRepository.saveAll(reponsesEntities);


        prospection.extraireInfosProspect();
        prospectionRepository.save(prospection);

        return prospection;
    }


    private List<Reponse> creerReponses(Prospection prospection, Map<Long, String> reponses) {
        List<Reponse> reponsesEntities = new ArrayList<>();

        for (Map.Entry<Long, String> entry : reponses.entrySet()) {
            Long questionId = entry.getKey();
            String valeur = entry.getValue();


            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question non trouvée: " + questionId));

            // Créer la réponse si elle n'est pas vide ou si la question est obligatoire
            if (valeur != null && (!valeur.trim().isEmpty() || question.getObligatoire())) {
                Reponse reponse = new Reponse(question, prospection, valeur.trim());

                // Valider la réponse
                if (!reponse.estValide()) {
                    throw new IllegalArgumentException("Réponse invalide pour la question: " + question.getQuestion());
                }

                reponsesEntities.add(reponse);
            }
        }

        return reponsesEntities;
    }


    private void validerDonneesProspection(TypeProspection typeProspection, Map<Long, String> reponses) {
        if (typeProspection == null) {
            throw new IllegalArgumentException("Le type de prospection est obligatoire");
        }

        if (reponses == null || reponses.isEmpty()) {
            throw new IllegalArgumentException("Au moins une réponse est requise");
        }

        // Vérifier que toutes les questions obligatoires ont une réponse
        List<Question> questionsObligatoires = questionRepository.findAllActiveOrderByOrdre()
                .stream()
                .filter(Question::getObligatoire)
                .toList();

        for (Question question : questionsObligatoires) {
            String valeur = reponses.get(question.getId());
            if (valeur == null || valeur.trim().isEmpty()) {
                throw new IllegalArgumentException("La question '" + question.getQuestion() + "' est obligatoire");
            }
        }

        // Validation spécifique par type de question
        for (Map.Entry<Long, String> entry : reponses.entrySet()) {
            Long questionId = entry.getKey();
            String valeur = entry.getValue();

            if (valeur != null && !valeur.trim().isEmpty()) {
                Question question = questionRepository.findById(questionId)
                        .orElseThrow(() -> new IllegalArgumentException("Question non trouvée: " + questionId));

                if (!validerReponseSelonType(question, valeur)) {
                    throw new IllegalArgumentException("Format invalide pour la question: " + question.getQuestion());
                }
            }
        }
    }


    private boolean validerReponseSelonType(Question question, String valeur) {
        switch (question.getType()) {
            case PHONE:
                return valeur.matches("^(06|07)\\d{8}$");

            case NUMBER:
                try {
                    Integer.parseInt(valeur);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }

            case CHOICE:
                return question.getOptions().stream()
                        .anyMatch(option -> option.getValeur().equals(valeur));
            case MULTIPLE_CHOICE:
                String[] selections = valeur.split(",");
                for (String selection : selections) {
                    boolean found = question.getOptions().stream()
                            .anyMatch(option -> option.getValeur().equals(selection.trim()));
                    if (!found) return false;
                }
                return true;
            default:
                return true;
        }
    }


    private void assignerHierarchie(Prospection prospection, Utilisateur createur) {
        prospection.setBranche(createur.getBranche());
        prospection.setSupervision(createur.getSupervision());
        prospection.setRegion(createur.getRegion());
    }


    private void assignerSelonType(Prospection prospection, TypeProspection typeProspection) {
        switch (typeProspection) {
            case PLANNING_AGENT:
                // Directement assigné au créateur
                prospection.setAgentAssigne(prospection.getCreateur());
                prospection.setStatut(StatutProspection.ASSIGNE);
                break;

            case CAMPAGNE_PROSPECTION:
                // Sera assigné par le chef de branche
                prospection.setAgentAssigne(null);
                prospection.setStatut(StatutProspection.NOUVEAU);
                break;

            case EVENEMENT_CULTUREL:
                // Sera géré au niveau régional
                prospection.setAgentAssigne(null);
                prospection.setStatut(StatutProspection.NOUVEAU);
                break;
        }
    }

    private boolean peutCreerProspection(Utilisateur utilisateur) {
        // Seuls les agents peuvent créer des prospections
        return utilisateur.getRole() == Role.AGENT;
    }

    public ProspectionWithReponses getProspectionAvecReponses(Long prospectionId, Utilisateur utilisateur) {
        Prospection prospection = prospectionRepository.findById(prospectionId)
                .orElseThrow(() -> new IllegalArgumentException("Prospection non trouvée"));

        // Vérifier les droits d'accès
        if (!peutVoirProspection(prospection, utilisateur)) {
            throw new AccessDeniedException("Vous n'avez pas le droit de voir cette prospection");
        }

        List<Reponse> reponses = reponseRepository.findByProspectionIdWithQuestion(prospectionId);

        return new ProspectionWithReponses(prospection, reponses);
    }


    public List<Prospection> getProspectionsAgent(Utilisateur agent) {
        if (agent.getRole() != Role.AGENT) {
            throw new AccessDeniedException("Seuls les agents peuvent consulter leurs prospections");
        }


        List<Prospection> prospectionsAssignees = prospectionRepository.findByAgentIdWithHierarchie(agent.getId());
        List<Prospection> prospectionsCrees = prospectionRepository.findByCreatorIdWithHierarchie(agent.getId());


        Set<Prospection> prospectionsUniques = new HashSet<>();
        prospectionsUniques.addAll(prospectionsAssignees);
        prospectionsUniques.addAll(prospectionsCrees);

        return new ArrayList<>(prospectionsUniques).stream()
                .sorted((p1, p2) -> p2.getDateCreation().compareTo(p1.getDateCreation()))
                .collect(Collectors.toList());
    }


    public FormulaireProspection getFormulaireVide() {
        List<Question> questions = questionRepository.findAllActiveOrderByOrdre();
        return new FormulaireProspection(questions);
    }


    private boolean peutVoirProspection(Prospection prospection, Utilisateur utilisateur) {
        switch (utilisateur.getRole()) {
            case AGENT:
                // L'agent ne peut voir que ses propres prospections
                return prospection.getCreateur().getId().equals(utilisateur.getId()) ||
                        (prospection.getAgentAssigne() != null &&
                                prospection.getAgentAssigne().getId().equals(utilisateur.getId()));

            case CHEF_BRANCHE:
                // Le chef de branche voit toutes les prospections de sa branche
                return prospection.getBranche().getId().equals(utilisateur.getBranche().getId());

            case SUPERVISEUR:
                // Le superviseur voit toutes les prospections de sa supervision
                return prospection.getSupervision().getId().equals(utilisateur.getSupervision().getId());

            case CHEF_ANIMATION_REGIONAL:
                // Le chef régional voit toutes les prospections de sa région
                return prospection.getRegion().getId().equals(utilisateur.getRegion().getId());

            case SIEGE:
                // Le siège voit tout
                return true;

            default:
                return false;
        }
    }


    public static class ProspectionWithReponses {
        private final Prospection prospection;
        private final List<Reponse> reponses;
        private final Map<Long, String> reponsesMap;

        public ProspectionWithReponses(Prospection prospection, List<Reponse> reponses) {
            this.prospection = prospection;
            this.reponses = reponses;
            this.reponsesMap = new HashMap<>();

            // Créer une map pour accès facile aux réponses
            for (Reponse reponse : reponses) {
                reponsesMap.put(reponse.getQuestion().getId(), reponse.getValeur());
            }
        }

        public Prospection getProspection() { return prospection; }
        public List<Reponse> getReponses() { return reponses; }
        public Map<Long, String> getReponsesMap() { return reponsesMap; }

        public String getReponse(Long questionId) {
            return reponsesMap.get(questionId);
        }
    }


    public static class FormulaireProspection {
        private final List<Question> questions;

        public FormulaireProspection(List<Question> questions) {
            this.questions = questions;
        }

        public List<Question> getQuestions() { return questions; }
    }




    public long getNombreProspectionsAujourdhui(Long agentId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return prospectionRepository.countByCreateurIdAndToday(agentId, startOfDay, endOfDay);
    }


    public Map<String, Object> getStatistiquesAgent(Long agentId) {
        Map<String, Object> stats = new HashMap<>();

        List<Object[]> statutsCount = prospectionRepository.countByAgentIdAndStatut(agentId);
        Map<String, Long> repartitionStatuts = new HashMap<>();

        for (Object[] row : statutsCount) {
            StatutProspection statut = (StatutProspection) row[0];
            Long count = (Long) row[1];
            repartitionStatuts.put(statut.getDisplayName(), count);
        }

        stats.put("repartitionStatuts", repartitionStatuts);
        stats.put("totalProspections", repartitionStatuts.values().stream().mapToLong(Long::longValue).sum());
        stats.put("prospectionsAujourdhui", getNombreProspectionsAujourdhui(agentId));

        return stats;
    }
}