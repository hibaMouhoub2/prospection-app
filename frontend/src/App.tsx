import React, { useState, useEffect } from 'react';
import NouvelleProspection from './pages/agent/NouvelleProspection';
import MesProspections from './pages/agent/MesProspections';
import DetailsProspection from './pages/agent/DetailsProspection';
import AgentLayout from './components/agent/AgentLayout';
import { LogIn, Eye, EyeOff, AlertCircle, CheckCircle, User, Lock, UserPlus, Settings, LogOut } from 'lucide-react';

// Import du composant QuestionManagement
import QuestionManagement from './components/QuestionManagement';

// Types
interface Region {
    id: number;
    nom: string;
    code: string;
}

interface Supervision {
    id: number;
    nom: string;
    code: string;
}

interface Branche {
    id: number;
    nom: string;
    code: string;
}

interface LoginForm {
    email: string;
    motDePasse: string;
}

interface RegisterForm {
    nom: string;
    prenom: string;
    email: string;
    motDePasse: string;
    role: string;
}

interface ApiResponse {
    success: boolean;
    message: string;
    accessToken?: string;
    refreshToken?: string;
    utilisateur?: {
        id: number;
        nom: string;
        prenom: string;
        email: string;
        role: string;
        roleDisplayName: string;
    };
    expiresIn?: number;
}

interface User {
    id: number;
    nom: string;
    prenom: string;
    email: string;
    role: string;
    roleDisplayName: string;
}

// Service d'authentification
class AuthService {
    private static ACCESS_TOKEN_KEY = 'access_token';    // NOUVEAU
    private static REFRESH_TOKEN_KEY = 'refresh_token';  // NOUVEAU
    private static USER_KEY = 'auth_user';

    static saveTokens(accessToken: string, refreshToken: string): void {
        localStorage.setItem(this.ACCESS_TOKEN_KEY, accessToken);
        localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
    }

    static getAccessToken(): string | null {
        return localStorage.getItem(this.ACCESS_TOKEN_KEY);
    }
    static getRefreshToken(): string | null {
        return localStorage.getItem(this.REFRESH_TOKEN_KEY);
    }

    static saveUser(user: User): void {
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }

    static getUser(): User | null {
        const user = localStorage.getItem(this.USER_KEY);
        return user ? JSON.parse(user) : null;
    }

    static async logout(): Promise<void> {
        const accessToken = this.getAccessToken();
        const refreshToken = this.getRefreshToken();

        try {
            await fetch('http://localhost:8090/api/auth/logout', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ accessToken, refreshToken })
            });
        } catch (error) {
            console.log('Erreur logout:', error);
        }

        localStorage.removeItem(this.ACCESS_TOKEN_KEY);
        localStorage.removeItem(this.REFRESH_TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    }


    static isAuthenticated(): boolean {
        return !!this.getAccessToken();
    }
}

// Composant de Login
function LoginForm({ onSuccess, onSwitchToRegister }: { onSuccess: (user: User) => void; onSwitchToRegister: () => void }) {
    const [formData, setFormData] = useState<LoginForm>({ email: '', motDePasse: '' });
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string>('');

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (error) setError('');
    };

    const handleSubmit = async () => {
        setLoading(true);
        setError('');

        try {
            const response = await fetch('http://localhost:8090/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });

            const data: ApiResponse = await response.json();

            if (data.success && data.accessToken && data.refreshToken && data.utilisateur) {
                AuthService.saveTokens(data.accessToken, data.refreshToken);
                AuthService.saveUser(data.utilisateur);
                onSuccess(data.utilisateur);
            } else {
                setError(data.message);
            }
        } catch (error) {
            console.error('Erreur serveur :', error);
        } finally {
            setLoading(false);
        }
    };

    const isFormValid = formData.email && formData.motDePasse;

    return (
        <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
            <div className="text-center mb-8">
                <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-100 rounded-full mb-4">
                    <LogIn className="w-8 h-8 text-blue-600" />
                </div>
                <h1 className="text-2xl font-bold text-gray-900">Connexion</h1>
                <p className="text-gray-600 mt-2">Application de prospection</p>
            </div>

            {error && (
                <div className="mb-6 p-4 rounded-lg flex items-center space-x-3 bg-red-50 border border-red-200">
                    <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0" />
                    <span className="text-sm text-red-700">{error}</span>
                </div>
            )}

            <div className="space-y-6">
                <div>
                    <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                        Email
                    </label>
                    <div className="relative">
                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                            <User className="h-5 w-5 text-gray-400" />
                        </div>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleInputChange}
                            className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                            placeholder="votre@email.com"
                        />
                    </div>
                </div>

                <div>
                    <label htmlFor="motDePasse" className="block text-sm font-medium text-gray-700 mb-2">
                        Mot de passe
                    </label>
                    <div className="relative">
                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                            <Lock className="h-5 w-5 text-gray-400" />
                        </div>
                        <input
                            type={showPassword ? "text" : "password"}
                            id="motDePasse"
                            name="motDePasse"
                            value={formData.motDePasse}
                            onChange={handleInputChange}
                            className="w-full pl-10 pr-10 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                            placeholder="Votre mot de passe"
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute inset-y-0 right-0 pr-3 flex items-center"
                        >
                            {showPassword ? (
                                <EyeOff className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                            ) : (
                                <Eye className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                            )}
                        </button>
                    </div>
                </div>

                <button
                    onClick={handleSubmit}
                    disabled={!isFormValid || loading}
                    className={`w-full py-3 px-4 rounded-md font-medium transition-all duration-200 flex items-center justify-center space-x-2 ${
                        isFormValid && !loading
                            ? 'bg-blue-600 hover:bg-blue-700 text-white shadow-md hover:shadow-lg'
                            : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    }`}
                >
                    {loading ? (
                        <>
                            <div className="w-5 h-5 border-2 border-gray-300 border-t-white rounded-full animate-spin"></div>
                            <span>Connexion...</span>
                        </>
                    ) : (
                        <>
                            <LogIn className="w-5 h-5" />
                            <span>Se connecter</span>
                        </>
                    )}
                </button>
            </div>

            <div className="mt-6 text-center">
                <p className="text-sm text-gray-600">
                    Pas encore de compte ?{' '}
                    <button
                        onClick={onSwitchToRegister}
                        className="text-blue-600 hover:text-blue-700 font-medium"
                    >
                        Créer un compte
                    </button>
                </p>
            </div>
        </div>
    );
}

// Composant d'enregistrement
function RegisterForm({ onSuccess, onSwitchToLogin }: { onSuccess: () => void; onSwitchToLogin: () => void }) {
    const [formData, setFormData] = useState({
        nom: '',
        prenom: '',
        email: '',
        motDePasse: '',
        role: 'AGENT',
        regionId: '',
        supervisionId: '',
        brancheId: ''
    });

    const [regions, setRegions] = useState<Region[]>([]);
    const [supervisions, setSupervisions] = useState<Supervision[]>([]);
    const [branches, setBranches] = useState<Branche[]>([]);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState<{ type: 'success' | 'error' | null; text: string }>({
        type: null,
        text: ''
    });

    useEffect(() => {
        fetch('http://localhost:8090/api/structure/regions')
            .then(res => res.json())
            .then((data: Region[]) => setRegions(data))
            .catch(() => setMessage({ type: 'error', text: 'Erreur chargement régions' }));
    }, []);

    useEffect(() => {
        if (formData.regionId) {
            fetch(`http://localhost:8090/api/structure/supervisions?regionId=${formData.regionId}`)
                .then(res => res.json())
                .then((data: Supervision[]) => setSupervisions(data))
                .catch(() => setMessage({ type: 'error', text: 'Erreur chargement supervisions' }));
            setFormData(prev => ({ ...prev, supervisionId: '', brancheId: '' }));
            setBranches([]);
        } else {
            setSupervisions([]);
            setBranches([]);
        }
    }, [formData.regionId]);

    useEffect(() => {
        if (formData.supervisionId) {
            fetch(`http://localhost:8090/api/structure/branches?supervisionId=${formData.supervisionId}`)
                .then(res => res.json())
                .then((data: Branche[]) => setBranches(data))
                .catch(() => setMessage({ type: 'error', text: 'Erreur chargement branches' }));
            setFormData(prev => ({ ...prev, brancheId: '' }));
        } else {
            setBranches([]);
        }
    }, [formData.supervisionId]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (message.type) setMessage({ type: null, text: '' });
    };

    const handleSubmit = async () => {
        setLoading(true);
        setMessage({ type: null, text: '' });

        try {
            const response = await fetch('http://localhost:8090/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    nom: formData.nom,
                    prenom: formData.prenom,
                    email: formData.email,
                    motDePasse: formData.motDePasse,
                    role: formData.role,
                    regionId: formData.regionId ? parseInt(formData.regionId) : null,
                    supervisionId: formData.supervisionId ? parseInt(formData.supervisionId) : null,
                    brancheId: formData.brancheId ? parseInt(formData.brancheId) : null
                })
            });

            const data: ApiResponse = await response.json();

            if (data.success) {
                setMessage({ type: 'success', text: data.message });
                setFormData({
                    nom: '', prenom: '', email: '', motDePasse: '', role: 'AGENT',
                    regionId: '', supervisionId: '', brancheId: ''
                });
                setTimeout(() => onSuccess(), 1500);
            } else {
                setMessage({ type: 'error', text: data.message });
            }
        } catch {
            setMessage({ type: 'error', text: 'Erreur de connexion au serveur' });
        } finally {
            setLoading(false);
        }
    };

    const needsRegion = ['CHEF_ANIMATION_REGIONAL', 'SUPERVISEUR', 'AGENT', 'CHEF_BRANCHE'].includes(formData.role);
    const needsSupervision = ['SUPERVISEUR', 'AGENT', 'CHEF_BRANCHE'].includes(formData.role);
    const needsBranche = ['AGENT', 'CHEF_BRANCHE'].includes(formData.role);

    const isFormValid = formData.nom && formData.prenom && formData.email && formData.motDePasse &&
        (formData.role === 'SIEGE' ||
            (needsBranche && formData.brancheId) ||
            (needsSupervision && !needsBranche && formData.supervisionId) ||
            (needsRegion && !needsSupervision && formData.regionId));

    return (
        <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
            <div className="text-center mb-8">
                <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 rounded-full mb-4">
                    <UserPlus className="w-8 h-8 text-green-600" />
                </div>
                <h1 className="text-2xl font-bold text-gray-900">Créer un compte</h1>
                <p className="text-gray-600 mt-2">Nouvel utilisateur</p>
            </div>

            {message.type && (
                <div className={`mb-6 p-4 rounded-lg flex items-center space-x-3 ${
                    message.type === 'success'
                        ? 'bg-green-50 border border-green-200'
                        : 'bg-red-50 border border-red-200'
                }`}>
                    {message.type === 'success' ? (
                        <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0" />
                    ) : (
                        <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0" />
                    )}
                    <span className={`text-sm ${
                        message.type === 'success' ? 'text-green-700' : 'text-red-700'
                    }`}>
                        {message.text}
                    </span>
                </div>
            )}

            <div className="space-y-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Nom *</label>
                    <input
                        type="text"
                        name="nom"
                        placeholder="Nom"
                        value={formData.nom}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Prénom *</label>
                    <input
                        type="text"
                        name="prenom"
                        placeholder="Prénom"
                        value={formData.prenom}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Email *</label>
                    <input
                        type="email"
                        name="email"
                        placeholder="votre@email.com"
                        value={formData.email}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Mot de passe *</label>
                    <input
                        type="password"
                        name="motDePasse"
                        placeholder="Minimum 6 caractères"
                        value={formData.motDePasse}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Rôle *</label>
                    <select
                        name="role"
                        value={formData.role}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 bg-white"
                    >
                        <option value="AGENT">Agent</option>
                        <option value="CHEF_BRANCHE">Chef de Branche</option>
                        <option value="SUPERVISEUR">Superviseur</option>
                        <option value="CHEF_ANIMATION_REGIONAL">Chef Animation Régional</option>
                        <option value="SIEGE">Siège</option>
                    </select>
                </div>

                {needsRegion && (
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Région *</label>
                        <select
                            name="regionId"
                            value={formData.regionId}
                            onChange={handleInputChange}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 bg-white"
                        >
                            <option value="">Sélectionnez une région</option>
                            {regions.map(region => (
                                <option key={region.id} value={region.id.toString()}>
                                    {region.nom} ({region.code})
                                </option>
                            ))}
                        </select>
                    </div>
                )}

                {needsSupervision && (
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Supervision *</label>
                        <select
                            name="supervisionId"
                            value={formData.supervisionId}
                            onChange={handleInputChange}
                            disabled={!formData.regionId}
                            className={`w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 ${
                                !formData.regionId ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'
                            }`}
                        >
                            <option value="">
                                {!formData.regionId ? 'Sélectionnez d\'abord une région' : 'Sélectionnez une supervision'}
                            </option>
                            {supervisions.map(supervision => (
                                <option key={supervision.id} value={supervision.id.toString()}>
                                    {supervision.nom} ({supervision.code})
                                </option>
                            ))}
                        </select>
                    </div>
                )}

                {needsBranche && (
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Branche *</label>
                        <select
                            name="brancheId"
                            value={formData.brancheId}
                            onChange={handleInputChange}
                            disabled={!formData.supervisionId}
                            className={`w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 ${
                                !formData.supervisionId ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'
                            }`}
                        >
                            <option value="">
                                {!formData.supervisionId ? 'Sélectionnez d\'abord une supervision' : 'Sélectionnez une branche'}
                            </option>
                            {branches.map(branche => (
                                <option key={branche.id} value={branche.id.toString()}>
                                    {branche.nom} ({branche.code})
                                </option>
                            ))}
                        </select>
                    </div>
                )}

                <button
                    onClick={handleSubmit}
                    disabled={!isFormValid || loading}
                    className={`w-full py-3 px-4 rounded-md font-medium transition-all duration-200 flex items-center justify-center space-x-2 ${
                        isFormValid && !loading
                            ? 'bg-green-600 hover:bg-green-700 text-white shadow-md hover:shadow-lg'
                            : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    }`}
                >
                    {loading ? (
                        <>
                            <div className="w-5 h-5 border-2 border-gray-300 border-t-white rounded-full animate-spin"></div>
                            <span>Création...</span>
                        </>
                    ) : (
                        <>
                            <UserPlus className="w-5 h-5" />
                            <span>Créer le compte</span>
                        </>
                    )}
                </button>
            </div>

            <div className="mt-6 text-center">
                <p className="text-sm text-gray-600">
                    Déjà un compte ?{' '}
                    <button
                        onClick={onSwitchToLogin}
                        className="text-green-600 hover:text-green-700 font-medium"
                    >
                        Se connecter
                    </button>
                </p>
            </div>
        </div>
    );
}


function Dashboard({ user, onLogout }: { user: User; onLogout: () => void }) {
    const [currentView, setCurrentView] = useState<'overview' | 'questions' | 'agent'>('overview');
    const [agentView, setAgentView] = useState<'dashboard' | 'nouvelle' | 'liste' | 'statistiques'>('dashboard');
    const [selectedProspectionId] = useState<number | null>(null);

    // Variable définie correctement
    const showQuestionManagement = user.role === 'SIEGE';

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="bg-white shadow-sm border-b">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center space-x-8">
                            <h1 className="text-xl font-semibold text-gray-900">
                                Application Prospection
                            </h1>

                            <nav className="hidden md:flex space-x-4">
                                <button
                                    onClick={() => setCurrentView('overview')}
                                    className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                        currentView === 'overview'
                                            ? 'bg-blue-100 text-blue-700'
                                            : 'text-gray-600 hover:text-gray-900'
                                    }`}
                                >
                                    Vue d'ensemble
                                </button>

                                {user.role === 'AGENT' && (
                                    <button
                                        onClick={() => setCurrentView('agent')}
                                        className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                            currentView === 'agent'
                                                ? 'bg-blue-100 text-blue-700'
                                                : 'text-gray-600 hover:text-gray-900'
                                        }`}
                                    >
                                        <User className="w-4 h-4 inline mr-1" />
                                        Prospection
                                    </button>
                                )}

                                {showQuestionManagement && (
                                    <button
                                        onClick={() => setCurrentView('questions')}
                                        className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                            currentView === 'questions'
                                                ? 'bg-blue-100 text-blue-700'
                                                : 'text-gray-600 hover:text-gray-900'
                                        }`}
                                    >
                                        <Settings className="w-4 h-4 inline mr-1" />
                                        Gestion Questions
                                    </button>
                                )}
                            </nav>
                        </div>

                        <div className="flex items-center space-x-4">
                            <div className="text-right">
                                <p className="text-sm font-medium text-gray-900">
                                    {user.prenom} {user.nom}
                                </p>
                                <p className="text-xs text-gray-500">{user.roleDisplayName}</p>
                            </div>
                            <button
                                onClick={onLogout}
                                className="flex items-center px-3 py-2 text-sm text-red-600 hover:text-red-700 hover:bg-red-50 rounded-md transition-colors"
                            >
                                <LogOut className="w-4 h-4 mr-1" />
                                Déconnexion
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div className="flex-1">
                {/* Vue d'ensemble */}
                {currentView === 'overview' && (
                    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                        <div className="bg-white rounded-lg shadow-sm p-6">
                            <h2 className="text-2xl font-bold text-gray-900 mb-4">
                                Bienvenue, {user.prenom} !
                            </h2>

                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                <div className="bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg p-6 text-white">
                                    <div className="flex items-center">
                                        <User className="w-8 h-8 mr-3" />
                                        <div>
                                            <h3 className="font-semibold">Profil</h3>
                                            <p className="text-blue-100 text-sm">{user.email}</p>
                                            <p className="text-blue-100 text-sm">{user.roleDisplayName}</p>
                                        </div>
                                    </div>
                                </div>

                                {user.role === 'SIEGE' && (
                                    <div className="bg-gradient-to-r from-green-500 to-green-600 rounded-lg p-6 text-white">
                                        <div className="flex items-center">
                                            <Settings className="w-8 h-8 mr-3" />
                                            <div>
                                                <h3 className="font-semibold">Administration</h3>
                                                <p className="text-green-100 text-sm">Gestion des questions</p>
                                                <button
                                                    onClick={() => setCurrentView('questions')}
                                                    className="mt-2 text-sm bg-white/20 hover:bg-white/30 px-3 py-1 rounded transition-colors"
                                                >
                                                    Accéder
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                )}

                                {user.role === 'AGENT' && (
                                    <div className="bg-gradient-to-r from-purple-500 to-purple-600 rounded-lg p-6 text-white">
                                        <div className="flex items-center">
                                            <User className="w-8 h-8 mr-3" />
                                            <div>
                                                <h3 className="font-semibold">Agent</h3>
                                                <p className="text-purple-100 text-sm">Gestion des prospections</p>
                                                <div className="mt-2 space-x-2">
                                                    <button
                                                        onClick={() => {
                                                            setCurrentView('agent');
                                                            setAgentView('nouvelle');
                                                        }}
                                                        className="text-sm bg-white/20 hover:bg-white/30 px-3 py-1 rounded transition-colors"
                                                    >
                                                        Nouvelle
                                                    </button>
                                                    <button
                                                        onClick={() => {
                                                            setCurrentView('agent');
                                                            setAgentView('liste');
                                                        }}
                                                        className="text-sm bg-white/20 hover:bg-white/30 px-3 py-1 rounded transition-colors"
                                                    >
                                                        Mes prospects
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                )}

                                <div className="bg-gradient-to-r from-orange-500 to-orange-600 rounded-lg p-6 text-white">
                                    <div className="flex items-center">
                                        <CheckCircle className="w-8 h-8 mr-3" />
                                        <div>
                                            <h3 className="font-semibold">Activité</h3>
                                            <p className="text-orange-100 text-sm">Dernière connexion</p>
                                            <p className="text-orange-100 text-xs">Aujourd'hui</p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div className="mt-8 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                                {user.role === 'SIEGE' && (
                                    <div>
                                        <h4 className="font-medium text-blue-900 mb-2">Fonctionnalités SIEGE</h4>
                                        <ul className="text-sm text-blue-700 space-y-1">
                                            <li>• Créer et gérer les questions du formulaire de prospection</li>
                                            <li>• Réorganiser l'ordre des questions (drag & drop)</li>
                                            <li>• Prévisualiser le formulaire tel que vu par les agents</li>
                                            <li>• Activer/désactiver des questions</li>
                                        </ul>
                                    </div>
                                )}

                                {user.role === 'AGENT' && (
                                    <div>
                                        <h4 className="font-medium text-blue-900 mb-2">Fonctionnalités AGENT</h4>
                                        <ul className="text-sm text-blue-700 space-y-1">
                                            <li>• Créer de nouvelles prospections</li>
                                            <li>• Consulter et gérer vos prospects</li>
                                            <li>• Suivre les relances et conversions</li>
                                            <li>• Accéder à vos statistiques personnelles</li>
                                        </ul>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                )}

                {/* Interface Agent */}
                {currentView === 'agent' && user.role === 'AGENT' && (
                    <AgentLayout currentPage={agentView}>
                        {agentView === 'nouvelle' && <NouvelleProspection />}
                        {agentView === 'liste' && <MesProspections />}
                        {agentView === 'statistiques' && selectedProspectionId && (
                            <DetailsProspection prospectionId={selectedProspectionId} />
                        )}
                        {agentView === 'dashboard' && (
                            <div className="p-6">
                                <h2 className="text-2xl font-bold mb-4">Tableau de bord Agent</h2>
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    <button
                                        onClick={() => setAgentView('nouvelle')}
                                        className="p-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                                    >
                                        <div className="flex items-center justify-center">
                                            <User className="w-6 h-6 mr-2" />
                                            Nouvelle Prospection
                                        </div>
                                    </button>
                                    <button
                                        onClick={() => setAgentView('liste')}
                                        className="p-4 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                                    >
                                        <div className="flex items-center justify-center">
                                            <Settings className="w-6 h-6 mr-2" />
                                            Mes Prospections
                                        </div>
                                    </button>
                                    <button
                                        onClick={() => setAgentView('statistiques')}
                                        className="p-4 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                                    >
                                        <div className="flex items-center justify-center">
                                            <Settings className="w-6 h-6 mr-2" />
                                            Statistiques
                                        </div>
                                    </button>
                                </div>

                                <div className="mt-6 bg-white rounded-lg shadow p-4">
                                    <h3 className="text-lg font-medium text-gray-900 mb-2">Accès rapide</h3>
                                    <div className="text-sm text-gray-600">
                                        <p>• Utilisez le menu de navigation pour accéder aux différentes fonctionnalités</p>
                                        <p>• Créez une nouvelle prospection ou consultez vos prospects existants</p>
                                    </div>
                                </div>
                            </div>
                        )}
                    </AgentLayout>
                )}

                {/* Gestion des questions pour SIEGE */}
                {currentView === 'questions' && showQuestionManagement && (
                    <QuestionManagement />
                )}
            </div>
        </div>
    );
}
// App principal
function App() {
    const [currentView, setCurrentView] = useState<'login' | 'register'>('login');
    const [user, setUser] = useState<User | null>(null);



    useEffect(() => {
        const token = AuthService.getAccessToken();
        const userData = AuthService.getUser();

        if (token && userData) {
            setUser(userData);
        }
    }, []);


    useEffect(() => {
        // L'intercepteur est déjà créé dans la fonction, pas besoin de le stocker
        console.log('App initialisé');
    }, []);

    const handleLoginSuccess = (userData: User) => {
        setUser(userData);
    };

    const handleRegisterSuccess = () => {
        setCurrentView('login');
    };

    const handleLogout = async () => {
        await AuthService.logout();
        setUser(null);
        setCurrentView('login');
    };

    if (user) {
        return <Dashboard user={user} onLogout={handleLogout} />;
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
            {currentView === 'login' ? (
                <LoginForm
                    onSuccess={handleLoginSuccess}
                    onSwitchToRegister={() => setCurrentView('register')}
                />
            ) : (
                <RegisterForm
                    onSuccess={handleRegisterSuccess}
                    onSwitchToLogin={() => setCurrentView('login')}
                />
            )}
        </div>
    );
}

export default App;