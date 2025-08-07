import React, { useState, useEffect } from 'react';
import {
    Plus,
    List,
    BarChart3,
    LogOut,
    Menu,
    X,
    Home,
    Bell,
    Settings
} from 'lucide-react';

interface AgentLayoutProps {
    children: React.ReactNode;
    currentPage?: 'dashboard' | 'nouvelle' | 'liste' | 'statistiques';
}

interface UserInfo {
    nom: string;
    prenom: string;
    email: string;
    role: string;
    branche?: {
        nom: string;
    };
}

const AgentLayout: React.FC<AgentLayoutProps> = ({ children, currentPage = 'dashboard' }) => {
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
    const [notifications] = useState(0);

    useEffect(() => {
        // Charger les informations utilisateur depuis le localStorage ou API
        const token = localStorage.getItem('access_token');
        const user = localStorage.getItem('auth_user');

        // Utiliser le token pour valider la session
        if (token && user) {
            try {
                setUserInfo(JSON.parse(user));
            } catch (e) {
                console.error('Erreur parsing user info:', e);
            }
        }
    }, []);

    const navigation = [
        {
            name: 'Tableau de bord',
            href: '/agent/dashboard',
            icon: Home,
            current: currentPage === 'dashboard'
        },
        {
            name: 'Nouvelle prospection',
            href: '/agent/nouvelle-prospection',
            icon: Plus,
            current: currentPage === 'nouvelle'
        },
        {
            name: 'Mes prospections',
            href: '/agent/mes-prospections',
            icon: List,
            current: currentPage === 'liste'
        },
        {
            name: 'Statistiques',
            href: '/agent/statistiques',
            icon: BarChart3,
            current: currentPage === 'statistiques'
        }
    ];

    const handleLogout = () => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('auth_user');
        window.location.href = '/login';
    };

    const navigateTo = (href: string) => {
        window.location.href = href;
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Sidebar pour mobile */}
            <div className={`fixed inset-0 z-40 lg:hidden ${sidebarOpen ? 'block' : 'hidden'}`}>
                <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
                <div className="relative flex w-64 flex-col bg-white">
                    <div className="flex h-16 items-center justify-between px-4 border-b">
                        <h1 className="text-xl font-bold text-gray-900">Agent Portal</h1>
                        <button
                            onClick={() => setSidebarOpen(false)}
                            className="text-gray-400 hover:text-gray-600"
                        >
                            <X className="h-6 w-6" />
                        </button>
                    </div>

                    <nav className="flex-1 space-y-1 px-2 py-4">
                        {navigation.map((item) => (
                            <button
                                key={item.name}
                                onClick={() => navigateTo(item.href)}
                                className={`group flex w-full items-center rounded-md px-2 py-2 text-sm font-medium ${
                                    item.current
                                        ? 'bg-blue-100 text-blue-900'
                                        : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                                }`}
                            >
                                <item.icon className="mr-3 h-5 w-5" />
                                {item.name}
                            </button>
                        ))}
                    </nav>
                </div>
            </div>

            {/* Sidebar pour desktop */}
            <div className="hidden lg:fixed lg:inset-y-0 lg:flex lg:w-64 lg:flex-col">
                <div className="flex flex-col flex-grow bg-white border-r border-gray-200">
                    <div className="flex h-16 items-center px-4 border-b">
                        <h1 className="text-xl font-bold text-gray-900">Agent Portal</h1>
                    </div>

                    <nav className="flex-1 space-y-1 px-2 py-4">
                        {navigation.map((item) => (
                            <button
                                key={item.name}
                                onClick={() => navigateTo(item.href)}
                                className={`group flex w-full items-center rounded-md px-2 py-2 text-sm font-medium transition-colors ${
                                    item.current
                                        ? 'bg-blue-100 text-blue-900'
                                        : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                                }`}
                            >
                                <item.icon className="mr-3 h-5 w-5" />
                                {item.name}
                            </button>
                        ))}
                    </nav>

                    {/* Profil utilisateur */}
                    <div className="flex-shrink-0 border-t border-gray-200 p-4">
                        {userInfo && (
                            <div className="group block">
                                <div className="flex items-center">
                                    <div className="flex-shrink-0">
                                        <div className="h-10 w-10 rounded-full bg-blue-500 flex items-center justify-center">
                      <span className="text-white font-medium">
                        {userInfo.prenom[0]}{userInfo.nom[0]}
                      </span>
                                        </div>
                                    </div>
                                    <div className="ml-3 flex-1 min-w-0">
                                        <p className="text-sm font-medium text-gray-900 truncate">
                                            {userInfo.prenom} {userInfo.nom}
                                        </p>
                                        <p className="text-xs text-gray-500 truncate">
                                            {userInfo.branche?.nom || 'Agent'}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        )}

                        <button
                            onClick={handleLogout}
                            className="mt-3 group flex w-full items-center rounded-md px-2 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                        >
                            <LogOut className="mr-3 h-5 w-5" />
                            Se déconnecter
                        </button>
                    </div>
                </div>
            </div>

            {/* Contenu principal */}
            <div className="lg:pl-64">
                {/* Header mobile */}
                <div className="sticky top-0 z-10 bg-white shadow-sm border-b lg:hidden">
                    <div className="flex h-16 items-center justify-between px-4">
                        <button
                            onClick={() => setSidebarOpen(true)}
                            className="text-gray-500 hover:text-gray-600"
                        >
                            <Menu className="h-6 w-6" />
                        </button>

                        <h1 className="text-lg font-semibold text-gray-900">
                            {navigation.find(item => item.current)?.name || 'Agent Portal'}
                        </h1>

                        <div className="flex items-center space-x-2">
                            {/* Notifications */}
                            <button className="relative text-gray-400 hover:text-gray-600">
                                <Bell className="h-6 w-6" />
                                {notifications > 0 && (
                                    <span className="absolute -top-1 -right-1 h-4 w-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                    {notifications}
                  </span>
                                )}
                            </button>

                            {/* Profil mobile */}
                            {userInfo && (
                                <div className="h-8 w-8 rounded-full bg-blue-500 flex items-center justify-center">
                  <span className="text-white text-sm font-medium">
                    {userInfo.prenom[0]}{userInfo.nom[0]}
                  </span>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Header desktop */}
                <div className="hidden lg:block sticky top-0 z-10 bg-white shadow-sm border-b">
                    <div className="flex h-16 items-center justify-between px-6">
                        <h1 className="text-2xl font-bold text-gray-900">
                            {navigation.find(item => item.current)?.name || 'Tableau de bord'}
                        </h1>

                        <div className="flex items-center space-x-4">
                            {/* Notifications */}
                            <button className="relative text-gray-400 hover:text-gray-600">
                                <Bell className="h-6 w-6" />
                                {notifications > 0 && (
                                    <span className="absolute -top-1 -right-1 h-5 w-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                    {notifications}
                  </span>
                                )}
                            </button>

                            {/* Settings */}
                            <button className="text-gray-400 hover:text-gray-600">
                                <Settings className="h-6 w-6" />
                            </button>

                            {/* Profil */}
                            {userInfo && (
                                <div className="flex items-center space-x-3">
                                    <div className="text-right">
                                        <p className="text-sm font-medium text-gray-900">
                                            {userInfo.prenom} {userInfo.nom}
                                        </p>
                                        <p className="text-xs text-gray-500">
                                            {userInfo.branche?.nom || 'Agent'}
                                        </p>
                                    </div>
                                    <div className="h-10 w-10 rounded-full bg-blue-500 flex items-center justify-center">
                    <span className="text-white font-medium">
                      {userInfo.prenom[0]}{userInfo.nom[0]}
                    </span>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Contenu de la page */}
                <main className="flex-1">
                    <div className="py-6">
                        {children}
                    </div>
                </main>

                {/* Footer */}
                <footer className="bg-white border-t border-gray-200 py-4">
                    <div className="px-6 text-center text-sm text-gray-500">
                        © 2025 Application de Prospection. Tous droits réservés.
                    </div>
                </footer>
            </div>

            {/* Indicateur en ligne (optionnel) */}
            <div className="fixed bottom-4 right-4 z-50">
                <div className="bg-green-500 text-white px-3 py-1 rounded-full text-xs font-medium shadow-lg">
                    ● En ligne
                </div>
            </div>
        </div>
    );
};

export default AgentLayout;