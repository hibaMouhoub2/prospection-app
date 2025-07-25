package com.prospection.prospectionbackend.controllers;

import com.prospection.prospectionbackend.entities.Question;
import com.prospection.prospectionbackend.entities.QuestionOption;
import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.enums.QuestionType;
import com.prospection.prospectionbackend.services.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * 1. CRÉER UNE QUESTION (SIEGE uniquement)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> creerQuestion(@Valid @RequestBody CreerQuestionRequest request) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            // Validation des données
            questionService.validerCreationQuestionnaire(request.getQuestion(), request.getType(), request.getOptions());

            Question question = questionService.createQuestion(
                    request.getQuestion(),
                    request.getDescription(),
                    request.getType(),
                    request.getObligatoire(),
                    request.getOptions(),
                    utilisateur
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Question créée avec succès");
            response.put("question", mapQuestionToResponse(question));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 2. RÉCUPÉRER TOUTES LES QUESTIONS (pour admin SIEGE)
     */
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAllQuestions() {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            // Vérifier que c'est le SIEGE
            if (!utilisateur.getRole().name().equals("SIEGE")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accès réservé au siège"));
            }

            List<Question> questions = questionService.getAllQuestions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("questions", questions.stream()
                    .map(this::mapQuestionToResponse)
                    .toList());
            response.put("total", questions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 3. RÉCUPÉRER LES QUESTIONS ACTIVES (pour le formulaire de prospection)
     */
    @GetMapping("/formulaire")
    public ResponseEntity<Map<String, Object>> getQuestionsFormulaire() {
        try {
            List<Question> questions = questionService.getQuestionsActives();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("questions", questions.stream()
                    .map(this::mapQuestionToResponse)
                    .toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 4. APERÇU DU FORMULAIRE (pour prévisualisation)
     */
    @GetMapping("/apercu")
    public ResponseEntity<Map<String, Object>> getApercuFormulaire() {
        try {
            List<Question> questions = questionService.getApercuFormulaire();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("questions", questions.stream()
                    .map(this::mapQuestionToResponse)
                    .toList());
            response.put("message", "Aperçu du formulaire tel que vu par les agents");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 5. RÉORGANISER LES QUESTIONS (DRAG & DROP)
     */
    @PutMapping("/reorder")
    public ResponseEntity<Map<String, Object>> reorganiserQuestions(@RequestBody ReorganiserRequest request) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            questionService.reorgnaiserQuestion(request.getOrdreIds(), utilisateur);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Questions réorganisées avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 6. DÉSACTIVER UNE QUESTION
     */
    @PutMapping("/{id}/desactiver")
    public ResponseEntity<Map<String, Object>> desactiverQuestion(@PathVariable Long id) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            questionService.desactiverQuestion(id, utilisateur);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Question désactivée avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 7. ACTIVER UNE QUESTION
     */
    @PutMapping("/{id}/activer")
    public ResponseEntity<Map<String, Object>> activerQuestion(@PathVariable Long id) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            questionService.activerQuestion(id, utilisateur);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Question activée avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 8. STATISTIQUES (SIEGE uniquement)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            if (!utilisateur.getRole().name().equals("SIEGE")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accès réservé au siège"));
            }

            Map<String, Object> stats = questionService.getStatistiques();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistiques", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 9. RÉCUPÉRER UNE QUESTION PAR ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getQuestionById(@PathVariable Long id) {
        try {
            Optional<Question> questionOpt = questionService.getQuestionById(id);

            if (questionOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("question", mapQuestionToResponse(questionOpt.get()));

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Question non trouvée"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 10. RÉCUPÉRER LES TYPES DE QUESTIONS DISPONIBLES
     */
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getTypesQuestions() {
        try {
            Map<String, Object> types = new HashMap<>();

            for (QuestionType type : QuestionType.values()) {
                Map<String, Object> typeInfo = new HashMap<>();
                typeInfo.put("name", type.name());
                typeInfo.put("displayName", type.getDisplayName());
                typeInfo.put("description", type.getDescription());
                typeInfo.put("requiresOptions", type.requiresOptions());
                typeInfo.put("validationPattern", type.getValidationPattern());
                typeInfo.put("validationMessage", type.getValidationMessage());

                types.put(type.name(), typeInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("types", types);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * MÉTHODES UTILITAIRES
     */
    private Utilisateur getUtilisateurAuthentifie() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Utilisateur)) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        return (Utilisateur) authentication.getPrincipal();
    }

    private Map<String, Object> mapQuestionToResponse(Question question) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", question.getId());
        map.put("question", question.getQuestion());
        map.put("description", question.getDescription());
        map.put("type", question.getType().name());
        map.put("typeDisplayName", question.getType().getDisplayName());
        map.put("ordre", question.getOrdre());
        map.put("actif", question.getActif());
        map.put("obligatoire", question.getObligatoire());
        map.put("dateCreation", question.getDateCreation());

        // Ajouter les options si c'est un type à choix
        if (question.hasOptions()) {
            List<Map<String, Object>> options = question.getOptions().stream()
                    .map(option -> {
                        Map<String, Object> optionMap = new HashMap<>();
                        optionMap.put("id", option.getId());
                        optionMap.put("valeur", option.getValeur());
                        optionMap.put("ordre", option.getOrdreOption());
                        return optionMap;
                    })
                    .toList();
            map.put("options", options);
        }

        return map;
    }

    /**
     * DTOs POUR LES REQUÊTES
     */
    public static class CreerQuestionRequest {
        private String question;
        private String description;
        private QuestionType type;
        private Boolean obligatoire;
        private List<String> options;

        // Getters et setters
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public QuestionType getType() { return type; }
        public void setType(QuestionType type) { this.type = type; }

        public Boolean getObligatoire() { return obligatoire; }
        public void setObligatoire(Boolean obligatoire) { this.obligatoire = obligatoire; }

        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
    }

    public static class ReorganiserRequest {
        private List<Long> ordreIds;

        public List<Long> getOrdreIds() { return ordreIds; }
        public void setOrdreIds(List<Long> ordreIds) { this.ordreIds = ordreIds; }
    }
}