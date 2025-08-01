// Types
export interface Question {
    id: number;
    question: string;
    description?: string;
    type: 'TEXT' | 'NUMBER' | 'EMAIL' | 'PHONE' | 'CHOICE' | 'MULTIPLE_CHOICE' | 'DATE' | 'TEXTAREA';
    typeDisplayName: string;
    ordre: number;
    obligatoire: boolean;
    options?: Array<{
        id: number;
        valeur: string;
        ordre: number;
    }>;
}

export interface TypeProspection {
    value: string;
    label: string;
    description: string;
}

export interface Formulaire {
    questions: Question[];
    typesProspection: TypeProspection[];
}

export interface Prospection {
    id: number;
    dateCreation: string;
    typeProspection: string;
    typeProspectionDisplay: string;
    statut: string;
    statutDisplay: string;
    statutCssClass: string;
    nomProspect?: string;
    prenomProspect?: string;
    telephoneProspect?: string;
    emailProspect?: string;
    commentaire?: string;
    createur: {
        id: number;
        nom: string;
        prenom: string;
    };
    agentAssigne?: {
        id: number;
        nom: string;
        prenom: string;
    };
    branche?: {
        id: number;
        nom: string;
    };
}

export interface Reponse {
    id: number;
    questionId: number;
    questionTexte: string;
    valeur: string;
    valeurFormatee: string;
    dateCreation: string;
}

export interface ProspectionDetails {
    prospection: Prospection;
    reponses: Reponse[];
    reponsesMap: Record<number, string>;
}

export interface Statistiques {
    repartitionStatuts: Record<string, number>;
    totalProspections: number;
    prospectionsAujourdhui: number;
}

export interface CreerProspectionRequest {
    typeProspection: string;
    reponses: Record<number, string>;
    commentaire?: string;
    telephoneProspect?: string;
}

export interface ApiResponse<T> {
    success: boolean;
    message?: string;
    data?: T;
    type?: string;
}

// Configuration de base
const API_BASE_URL = 'http://localhost:8090/api';

class ProspectionService {
    private getAuthHeaders(): Record<string, string> {
        const token = localStorage.getItem('access_token');
        return {
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        };
    }

    private async handleResponse<T>(response: Response): Promise<ApiResponse<T>> {
        const data = await response.json();

        if (response.ok) {
            return {
                success: true,
                data: data.success ? data : data,
                message: data.message
            };
        } else {
            return {
                success: false,
                message: data.message || 'Une erreur est survenue',
                type: data.type
            };
        }
    }



    private async fetchWithRetry(input: RequestInfo | URL, init?: RequestInit): Promise<Response> {
        let response = await fetch(input, init);

        if (response.status === 401 && !input.toString().includes('/auth/')) {
            try {
                const refreshToken = localStorage.getItem('refresh_token');
                if (refreshToken) {
                    const refreshResponse = await fetch(`${API_BASE_URL}/auth/refresh`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ refreshToken })
                    });

                    if (refreshResponse.ok) {
                        const refreshData = await refreshResponse.json();
                        localStorage.setItem('access_token', refreshData.token);

                        // Retry avec nouveau token
                        const newInit = {
                            ...init,
                            headers: {
                                ...init?.headers,
                                'Authorization': `Bearer ${refreshData.token}`
                            }
                        };
                        response = await fetch(input, newInit);
                    }
                }
            } catch (error) {
                console.log('Erreur refresh:', error);
            }
        }

        return response;
    }

    async getFormulaire(): Promise<ApiResponse<Formulaire>> {
        try {
            const response = await this.fetchWithRetry(`${API_BASE_URL}/prospections/formulaire`, {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            return await this.handleResponse<Formulaire>(response);
        } catch (error) {
            console.log(error);
            return {
                success: false,
                message: 'Erreur de connexion au serveur'
            };
        }
    }
        // Créer une nouvelle prospection
    async creerProspection(request: CreerProspectionRequest): Promise<ApiResponse<{ prospection: Prospection; id: number }>> {
        try {
            const response = await this.fetchWithRetry(`${API_BASE_URL}/prospections`, {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify(request)
            });

            return await this.handleResponse<{ prospection: Prospection; id: number }>(response);
        } catch (error) {
            console.log(error);
            return {
                success: false,
                message: 'Erreur de connexion au serveur'
            };
        }
    }

    // Récupérer les prospections de l'agent
    async getMesProspections(): Promise<ApiResponse<{ prospections: Prospection[]; total: number }>> {
        try {
            const response = await this.fetchWithRetry(`${API_BASE_URL}/prospections/mes-prospections`, {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            return await this.handleResponse<{ prospections: Prospection[]; total: number }>(response);
        } catch (error) {
            console.log(error);
            return {
                success: false,
                message: 'Erreur de connexion au serveur'
            };
        }
    }

    // Récupérer les détails d'une prospection
    async getProspectionDetails(id: number): Promise<ApiResponse<ProspectionDetails>> {
        try {
            const response = await this.fetchWithRetry(`${API_BASE_URL}/prospections/${id}`, {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            return await this.handleResponse<ProspectionDetails>(response);
        } catch (error) {
            console.log(error);
            return {
                success: false,
                message: 'Erreur de connexion au serveur'
            };
        }
    }

    // Récupérer les statistiques de l'agent
    async getStatistiques(): Promise<ApiResponse<{ statistiques: Statistiques }>> {
        try {
            const response = await this.fetchWithRetry(`${API_BASE_URL}/prospections/statistiques`, {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            return await this.handleResponse<{ statistiques: Statistiques }>(response);
        } catch (error) {
            console.log(error);
            return {
                success: false,
                message: 'Erreur de connexion au serveur'
            };
        }
    }

    // Vérifier si un prospect existe déjà
    async verifierDoublon(telephone: string): Promise<ApiResponse<{ existe: boolean; message: string }>> {
        try {
            const response = await this.fetchWithRetry(`${API_BASE_URL}/prospections/verifier-doublon?telephone=${encodeURIComponent(telephone)}`, {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            return await this.handleResponse<{ existe: boolean; message: string }>(response);
        } catch (error) {
            console.log(error);
            return {
                success: false,
                message: 'Erreur de connexion au serveur'
            };
        }
    }
}

// Export de l'instance du service
export const prospectionService = new ProspectionService();

// Utilitaires pour le formatage
export const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
};

export const formatDateShort = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
};

// Validation des champs
export const validateEmail = (email: string): boolean => {
    return /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email);
};

export const validatePhone = (phone: string): boolean => {
    return /^(06|07)\d{8}$/.test(phone);
};

export const validateNumber = (value: string): boolean => {
    return /^\d+$/.test(value);
};

// Formateurs spécifiques
export const formatPhone = (phone: string): string => {
    if (phone && phone.length === 10) {
        return phone.substring(0, 2) + ' ' +
            phone.substring(2, 4) + ' ' +
            phone.substring(4, 6) + ' ' +
            phone.substring(6, 8) + ' ' +
            phone.substring(8, 10);
    }
    return phone;
};

export const getStatutColor = (statut: string): string => {
    switch (statut) {
        case 'NOUVEAU':
            return 'bg-blue-100 text-blue-800';
        case 'ASSIGNE':
            return 'bg-yellow-100 text-yellow-800';
        case 'EN_COURS':
            return 'bg-orange-100 text-orange-800';
        case 'CONVERTI':
            return 'bg-green-100 text-green-800';
        case 'ABANDONNE':
            return 'bg-red-100 text-red-800';
        default:
            return 'bg-gray-100 text-gray-800';
    }
};

export const getTypeProspectionIcon = (type: string): string => {
    switch (type) {
        case 'PLANNING_AGENT':
            return '📅';
        case 'CAMPAGNE_PROSPECTION':
            return '📢';
        case 'EVENEMENT_CULTUREL':
            return '🎭';
        default:
            return '📝';
    }
};