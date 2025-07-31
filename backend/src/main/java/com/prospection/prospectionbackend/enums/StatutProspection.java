package com.prospection.prospectionbackend.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatutProspection {
    NOUVEAU("Nouveau", "Prospect créé, non encore traité", "bg-blue-100 text-blue-800"),
    ASSIGNE("Assigné", "Assigné à un agent", "bg-yellow-100 text-yellow-800"),
    EN_COURS("En cours", "Relance en cours", "bg-orange-100 text-orange-800"),
    CONVERTI("Converti", "Transformé en client", "bg-green-100 text-green-800"),
    ABANDONNE("Abandonné", "Prospect non abouti", "bg-red-100 text-red-800");

    private final String displayName;
    private final String description;
    private final String cssClass;

    public boolean peutTransitionnerVers(StatutProspection nouveauStatut) {
        switch (this) {
            case NOUVEAU:
                return nouveauStatut == ASSIGNE || nouveauStatut == ABANDONNE;
            case ASSIGNE:
                return nouveauStatut == EN_COURS || nouveauStatut == CONVERTI || nouveauStatut == ABANDONNE;
            case EN_COURS:
                return nouveauStatut == CONVERTI || nouveauStatut == ABANDONNE;
            case CONVERTI:
                return false; // Statut final, pas de transition possible
            case ABANDONNE:
                return nouveauStatut == EN_COURS; // Possibilité de relancer
            default:
                return false;
        }
    }

    public StatutProspection[] getTransitionsPossibles() {
        switch (this) {
            case NOUVEAU:
                return new StatutProspection[]{ASSIGNE, ABANDONNE};
            case ASSIGNE:
                return new StatutProspection[]{EN_COURS, CONVERTI, ABANDONNE};
            case EN_COURS:
                return new StatutProspection[]{CONVERTI, ABANDONNE};
            case CONVERTI:
                return new StatutProspection[]{}; // Aucune transition
            case ABANDONNE:
                return new StatutProspection[]{EN_COURS}; // Possibilité de relancer
            default:
                return new StatutProspection[]{};
        }
    }

    public boolean estFinal() {
        return this == CONVERTI;
    }

    public boolean estActif() {
        return this != CONVERTI && this != ABANDONNE;
    }
}
