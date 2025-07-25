export interface Region {
    id: number;
    nom: string;
    code: string;
}

export interface Supervision {
    id: number;
    nom: string;
    code: string;
}

export interface Branche {
    id: number;
    nom: string;
    code: string;
}

export interface LoginForm {
    email: string;
    motDePasse: string;
}

export interface RegisterForm {
    nom: string;
    prenom: string;
    email: string;
    motDePasse: string;
    role: string;
}

export interface ApiResponse {
    success: boolean;
    message: string;
    token?: string;
    utilisateur?: User;
    expiresIn?: number;
}

export interface User {
    id: number;
    nom: string;
    prenom: string;
    email: string;
    role: string;
    roleDisplayName: string;
}