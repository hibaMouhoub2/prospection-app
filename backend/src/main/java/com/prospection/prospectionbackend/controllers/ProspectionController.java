package com.prospection.prospectionbackend.controllers;

import com.prospection.prospectionbackend.entities.Prospection;
import com.prospection.prospectionbackend.entities.Question;
import com.prospection.prospectionbackend.entities.Reponse;
import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.enums.TypeProspection;
import com.prospection.prospectionbackend.repositories.UtilisateurRepository;
import com.prospection.prospectionbackend.services.ProspectionService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Transactional
@RequestMapping("/prospections")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class ProspectionController {

    @Autowired
    private ProspectionService prospectionService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/formulaire")
    public ResponseEntity<Map<String, Object>> getFormulaireVide() {
        try {
            ProspectionService.FormulaireProspection formulaire = prospectionService.getFormulaireVide();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("questions", formulaire.getQuestions().stream()
                    .map(this::mapQuestionToResponse)
                    .toList());
            response.put("typesProspection", getTypesProspectionDisponibles());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Erreur lors du chargement du formulaire: " + e.getMessage()));
        }
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> creerProspection(@Valid @RequestBody CreerProspectionRequest request) {
        try {
            System.out.println("=== DÉBUT DEBUG CRÉATION PROSPECTION ===");
            System.out.println("Type prospection: " + request.getTypeProspection());
            System.out.println("Reponses reçues: " + request.getReponses());
            System.out.println("Taille des réponses: " + (request.getReponses() != null ? request.getReponses().size() : "null"));

            if (request.getReponses() != null) {
                for (Map.Entry<Long, String> entry : request.getReponses().entrySet()) {
                    System.out.println("Question ID: " + entry.getKey() + " (Type: " + entry.getKey().getClass().getSimpleName() + "), Valeur: " + entry.getValue());
                }
            }
            Utilisateur utilisateur = getUtilisateurAuthentifie();


            if (request.getTypeProspection() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Le type de prospection est obligatoire"));
            }

            if (request.getReponses() == null || request.getReponses().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Au moins une réponse est requise"));
            }

            if (request.getTelephoneProspect() != null) {

                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Un prospect avec ce numéro de téléphone existe déjà");
                response.put("type", "DUPLICATE_WARNING");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Créer la prospection
            Prospection prospection = prospectionService.creerProspection(
                    request.getTypeProspection(),
                    request.getReponses(),
                    request.getCommentaire(),
                    utilisateur
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Prospection créée avec succès");
            response.put("prospection", mapProspectionToResponse(prospection));
            response.put("id", prospection.getId());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            System.out.println("ERREUR dans creerProspection: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ERREUR dans creerProspection: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Erreur lors de la création: " + e.getMessage()));
        }
    }


    @GetMapping("/mes-prospections")
    public ResponseEntity<Map<String, Object>> getMesProspections() {
        System.out.println("=== DÉBUT getMesProspections ===");

        try {
            System.out.println("Étape 1: Vérification de l'authentification...");

            // Debug authentification
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication object: " + auth);
            System.out.println("Is authenticated: " + (auth != null ? auth.isAuthenticated() : "auth is null"));

            if (auth != null) {
                System.out.println("Principal type: " + auth.getPrincipal().getClass().getName());
                System.out.println("Principal value: " + auth.getPrincipal());
            }

            System.out.println("Étape 2: Récupération utilisateur...");
            Utilisateur utilisateur = getUtilisateurAuthentifie();
            System.out.println("Utilisateur récupéré: " + utilisateur.getEmail() + " (ID: " + utilisateur.getId() + ")");
            System.out.println("Rôle utilisateur: " + utilisateur.getRole());

            System.out.println("Étape 3: Appel service getProspectionsAgent...");
            List<Prospection> prospections = prospectionService.getProspectionsAgent(utilisateur);
            System.out.println("Nombre de prospections récupérées: " + prospections.size());

            System.out.println("Étape 4: Mapping des prospections...");
            List<Map<String, Object>> prospectionsMapped = new ArrayList<>();

            for (int i = 0; i < prospections.size(); i++) {
                try {
                    Prospection p = prospections.get(i);
                    System.out.println("Mapping prospection " + (i+1) + "/" + prospections.size() + " - ID: " + p.getId());
                    Map<String, Object> mapped = mapProspectionToResponse(p);
                    prospectionsMapped.add(mapped);
                } catch (Exception e) {
                    System.out.println("ERREUR lors du mapping de la prospection " + (i+1) + ": " + e.getMessage());
                    e.printStackTrace();
                    throw e; // Re-lancer l'exception pour arrêter le processus
                }
            }

            System.out.println("Étape 5: Création de la réponse...");
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prospections", prospectionsMapped);
            response.put("total", prospections.size());

            System.out.println("=== getMesProspections TERMINÉ AVEC SUCCÈS ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("=== ERREUR DANS getMesProspections ===");
            System.out.println("Type d'erreur: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            System.out.println("Stack trace:");
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Erreur: " + e.getMessage()));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProspection(@PathVariable Long id) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();
            ProspectionService.ProspectionWithReponses prospectionData =
                    prospectionService.getProspectionAvecReponses(id, utilisateur);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prospection", mapProspectionToResponse(prospectionData.getProspection()));
            response.put("reponses", prospectionData.getReponses().stream()
                    .map(this::mapReponseToResponse)
                    .toList());
            response.put("reponsesMap", prospectionData.getReponsesMap());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }


    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        System.out.println("=== DÉBUT getStatistiques ===");

        try {
            System.out.println("Étape 1: Récupération utilisateur...");
            Utilisateur utilisateur = getUtilisateurAuthentifie();
            System.out.println("Utilisateur: " + utilisateur.getEmail() + " (ID: " + utilisateur.getId() + ")");

            System.out.println("Étape 2: Appel service getStatistiquesAgent...");
            Map<String, Object> statistiques = prospectionService.getStatistiquesAgent(utilisateur.getId());
            System.out.println("Statistiques récupérées: " + statistiques);

            System.out.println("Étape 3: Création réponse...");
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistiques", statistiques);

            System.out.println("=== getStatistiques TERMINÉ AVEC SUCCÈS ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("=== ERREUR DANS getStatistiques ===");
            System.out.println("Type d'erreur: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            System.out.println("Stack trace:");
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Erreur: " + e.getMessage()));
        }
    }


    @GetMapping("/verifier-doublon")
    public ResponseEntity<Map<String, Object>> verifierDoublon(@RequestParam String telephone) {
        try {
//            boolean existe = prospectionService.prospectExisteDeja(telephone);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("Un prospect avec ce téléphone existe déjà", "message");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }




    private Utilisateur getUtilisateurAuthentifie() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("=== DEBUG AUTHENTIFICATION ===");
        System.out.println("Authentication: " + authentication);

        if (authentication != null) {
            System.out.println("Authentication class: " + authentication.getClass().getName());
            System.out.println("Principal: " + authentication.getPrincipal());
            System.out.println("Principal class: " + authentication.getPrincipal().getClass().getName());
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Pas d'authentification valide");
            throw new RuntimeException("Aucune authentification trouvée dans le contexte");

        }

        Object principal = authentication.getPrincipal();

        // CAS 1: Principal est déjà un objet Utilisateur
        if (principal instanceof Utilisateur) {
            System.out.println("✅ Principal est un Utilisateur: " + ((Utilisateur) principal).getEmail());
            return (Utilisateur) principal;
        }

        // CAS 2: Principal est une String (email) - fallback
        if (principal instanceof String) {
            String email = (String) principal;
            System.out.println("⚠️ Principal est une String: " + email);

            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmailAndActifTrue(email);
            if (utilisateurOpt.isPresent()) {
                System.out.println("✅ Utilisateur trouvé par email: " + email);
                return utilisateurOpt.get();
            } else {
                throw new RuntimeException("Utilisateur non trouvé pour l'email: " + email);
            }
        }

        // CAS 3: Principal est UserDetails
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            System.out.println("⚠️ Principal est UserDetails: " + email);

            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmailAndActifTrue(email);
            if (utilisateurOpt.isPresent()) {
                System.out.println("✅ Utilisateur trouvé par UserDetails: " + email);
                return utilisateurOpt.get();
            } else {
                throw new RuntimeException("Utilisateur non trouvé pour l'email: " + email);
            }
        }

        throw new RuntimeException("Type de principal non supporté: " +
                (principal != null ? principal.getClass().getName() : "null"));
    }


    private Map<String, Object> mapProspectionToResponse(Prospection prospection) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", prospection.getId());
        map.put("dateCreation", prospection.getDateCreation());
        map.put("typeProspection", prospection.getTypeProspection().name());
        map.put("typeProspectionDisplay", prospection.getTypeProspection().getDisplayName());
        map.put("statut", prospection.getStatut().name());
        map.put("statutDisplay", prospection.getStatut().getDisplayName());
        map.put("statutCssClass", prospection.getStatut().getCssClass());


        map.put("nomProspect", null);
        map.put("prenomProspect", null);
        map.put("telephoneProspect", null);
        map.put("emailProspect", null);
        map.put("commentaire", prospection.getCommentaire());

        // Informations créateur
        if (prospection.getCreateur() != null) {
            Map<String, Object> createur = new HashMap<>();
            createur.put("id", prospection.getCreateur().getId());
            createur.put("nom", prospection.getCreateur().getNom());
            createur.put("prenom", prospection.getCreateur().getPrenom());
            map.put("createur", createur);
        }

        // Agent assigné
        if (prospection.getAgentAssigne() != null) {
            Map<String, Object> agent = new HashMap<>();
            agent.put("id", prospection.getAgentAssigne().getId());
            agent.put("nom", prospection.getAgentAssigne().getNom());
            agent.put("prenom", prospection.getAgentAssigne().getPrenom());
            map.put("agentAssigne", agent);
        }

        // Hiérarchie
        if (prospection.getBranche() != null) {
            Map<String, Object> brancheMap = new HashMap<>();
            brancheMap.put("id", prospection.getBranche().getId());
            brancheMap.put("nom", prospection.getBranche().getNom());
            map.put("branche", brancheMap);
        }

        return map;
    }

    private Map<String, Object> mapQuestionToResponse(Question question) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", question.getId());
        map.put("question", question.getQuestion());
        map.put("description", question.getDescription());
        map.put("type", question.getType().name());
        map.put("typeDisplayName", question.getType().getDisplayName());
        map.put("ordre", question.getOrdre());
        map.put("obligatoire", question.getObligatoire());

        // Ajouter les options si nécessaire
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

    private Map<String, Object> mapReponseToResponse(Reponse reponse) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", reponse.getId());
        map.put("questionId", reponse.getQuestion().getId());
        map.put("questionTexte", reponse.getQuestion().getQuestion());
        map.put("valeur", reponse.getValeur());
        map.put("valeurFormatee", reponse.getValeurFormatee());
        map.put("dateCreation", reponse.getDateCreation());
        return map;
    }

    private List<Map<String, Object>> getTypesProspectionDisponibles() {
        List<Map<String, Object>> types = new ArrayList<>();

        Map<String, Object> planningAgent = new HashMap<>();
        planningAgent.put("value", TypeProspection.PLANNING_AGENT.name());
        planningAgent.put("label", TypeProspection.PLANNING_AGENT.getDisplayName());
        planningAgent.put("description", TypeProspection.PLANNING_AGENT.getDescription());
        types.add(planningAgent);

        Map<String, Object> campagneProspection = new HashMap<>();
        campagneProspection.put("value", TypeProspection.CAMPAGNE_PROSPECTION.name());
        campagneProspection.put("label", TypeProspection.CAMPAGNE_PROSPECTION.getDisplayName());
        campagneProspection.put("description", TypeProspection.CAMPAGNE_PROSPECTION.getDescription());
        types.add(campagneProspection);

        Map<String, Object> evenementCulturel = new HashMap<>();
        evenementCulturel.put("value", TypeProspection.EVENEMENT_CULTUREL.name());
        evenementCulturel.put("label", TypeProspection.EVENEMENT_CULTUREL.getDisplayName());
        evenementCulturel.put("description", TypeProspection.EVENEMENT_CULTUREL.getDescription());
        types.add(evenementCulturel);

        return types;
    }

    // ===============================
    // DTO POUR LES REQUÊTES
    // ===============================

    public static class CreerProspectionRequest {
        private TypeProspection typeProspection;
        private Map<Long, String> reponses;
        private String commentaire;
        private String telephoneProspect; // Pour vérification doublons

        // Getters et setters
        public TypeProspection getTypeProspection() { return typeProspection; }
        public void setTypeProspection(TypeProspection typeProspection) { this.typeProspection = typeProspection; }

        public Map<Long, String> getReponses() { return reponses; }
        public void setReponses(Map<Long, String> reponses) { this.reponses = reponses; }

        public String getCommentaire() { return commentaire; }
        public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

        public String getTelephoneProspect() { return telephoneProspect; }
        public void setTelephoneProspect(String telephoneProspect) { this.telephoneProspect = telephoneProspect; }
    }
}