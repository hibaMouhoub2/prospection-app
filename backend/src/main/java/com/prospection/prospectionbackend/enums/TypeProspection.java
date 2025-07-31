package com.prospection.prospectionbackend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
public enum TypeProspection {
    CAMPAGNE_PROSPECTION("Campagne de prospection", "Le prospect sera assigné au chef de branche pour l'assignation aux agents"),
    PLANNING_AGENT("Planning agent", "Le prospect sera directement assigné à l'agent pour la relance"),
    EVENEMENT_CULTUREL("Evénement culturel", "Le prospect sera assigné à la direction pour l'assignation");

    private final String displayName;
    private final String description;

    public String getAssignationInitiale() {
        switch (this) {
            case CAMPAGNE_PROSPECTION:
                return "CHEF_BRANCHE";
            case PLANNING_AGENT:
                return "AGENT";
            case EVENEMENT_CULTUREL:
                return "CHEF_ANIMATION_REGIONAL";
            default:
                return "AGENT";
        }
    }

    public String getWorkflow() {
        switch (this) {
            case CAMPAGNE_PROSPECTION:
                return "Agent → Chef Branche → Agent";
            case PLANNING_AGENT:
                return "Agent → Direct";
            case EVENEMENT_CULTUREL:
                return "Région → Branche → Agent";
            default:
                return "Standard";
        }
    }
}
