import React, { useState, useEffect } from 'react';
import { LogIn, Eye, EyeOff, AlertCircle, CheckCircle, User, Lock, UserPlus } from 'lucide-react';

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
    token?: string;
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

// Service d'authentification simple
class AuthService {
    private static TOKEN_KEY = 'auth_token';
    private static USER_KEY = 'auth_user';

    static saveToken(token: string): void {
        localStorage.setItem(this.TOKEN_KEY, token);
    }

    static getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    static saveUser(user: User): void {
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }

    static getUser(): User | null {
        const user = localStorage.getItem(this.USER_KEY);
        return user ? JSON.parse(user) : null;
    }

    static logout(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    }

    static isAuthenticated(): boolean {
        return !!this.getToken();
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
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });

            const data: ApiResponse = await response.json();

            if (data.success && data.token && data.utilisateur) {
                AuthService.saveToken(data.token);
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
            {/* Header */}
            <div className="text-center mb-8">
                <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-100 rounded-full mb-4">
                    <LogIn className="w-8 h-8 text-blue-600" />
                </div>
                <h1 className="text-2xl font-bold text-gray-900">Connexion</h1>
                <p className="text-gray-600 mt-2">Application de prospection</p>
            </div>

            {/* Message d'erreur */}
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
        fetch('/api/structure/regions')
            .then(res => res.json())
            .then((data: Region[]) => setRegions(data))
            .catch(() => setMessage({ type: 'error', text: 'Erreur chargement régions' }));
    }, []);

    // Charger supervisions quand région change
    useEffect(() => {
        if (formData.regionId) {
            fetch(`/api/structure/supervisions?regionId=${formData.regionId}`)
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

    // Charger branches quand supervision change
    useEffect(() => {
        if (formData.supervisionId) {
            fetch(`/api/structure/branches?supervisionId=${formData.supervisionId}`)
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
            const response = await fetch('/api/auth/register', {
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
            {/* Header */}
            <div className="text-center mb-8">
                <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 rounded-full mb-4">
                    <UserPlus className="w-8 h-8 text-green-600" />
                </div>
                <h1 className="text-2xl font-bold text-gray-900">Créer un compte</h1>
                <p className="text-gray-600 mt-2">Nouvel utilisateur</p>
            </div>

            {/* Message */}
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
                {/* Nom */}
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

                {/* Prénom */}
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
                        <option value="SUPERVISEUR"> Superviseur</option>
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

// Dashboard simple
function Dashboard({ user, onLogout }: { user: User; onLogout: () => void }) {
    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
            <div className="max-w-4xl mx-auto">
                <div className="bg-white rounded-lg shadow-lg p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-2xl font-bold text-gray-900">
                                Bienvenue, {user.prenom} {user.nom}
                            </h1>
                            <p className="text-gray-600">Rôle : {user.roleDisplayName}</p>
                            <p className="text-gray-600">Email : {user.email}</p>
                        </div>
                        <button
                            onClick={onLogout}
                            className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md"
                        >
                            Déconnexion
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

// App principal
function App() {
    const [currentView, setCurrentView] = useState<'login' | 'register'>('login');
    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {
        // Vérifier si déjà connecté
        const token = AuthService.getToken();
        const userData = AuthService.getUser();

        if (token && userData) {
            setUser(userData);
        }
    }, []);

    const handleLoginSuccess = (userData: User) => {
        setUser(userData);
    };

    const handleRegisterSuccess = () => {
        setCurrentView('login');
    };

    const handleLogout = () => {
        AuthService.logout();
        setUser(null);
        setCurrentView('login');
    };

    // Si connecté, afficher le dashboard
    if (user) {
        return <Dashboard user={user} onLogout={handleLogout} />;
    }

    // Sinon afficher login ou register
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