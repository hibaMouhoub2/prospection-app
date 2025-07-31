import React, { useState, useEffect } from 'react';
import { Eye, User, Phone, Mail, Calendar, Filter, Search, RefreshCw } from 'lucide-react';

interface Prospection {
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

interface Statistiques {
    repartitionStatuts: Record<string, number>;
    totalProspections: number;
    prospectionsAujourdhui: number;
}

const MesProspections: React.FC = () => {
    const [prospections, setProspections] = useState<Prospection[]>([]);
    const [statistiques, setStatistiques] = useState<Statistiques | null>(null);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterStatut, setFilterStatut] = useState('');
    const [filterType, setFilterType] = useState('');

    useEffect(() => {
        chargerDonnees();
    }, []);

    const chargerDonnees = async () => {
        setLoading(true);
        try {
            await Promise.all([
                chargerProspections(),
                chargerStatistiques()
            ]);
        } catch (error) {
            console.error('Erreur lors du chargement:', error);
        } finally {
            setLoading(false);
        }
    };

    const chargerProspections = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch('http://localhost:8090/prospections/mes-prospections', {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    setProspections(data.prospections);
                }
            }
        } catch (error) {
            console.error('Erreur lors du chargement des prospections:', error);
        }
    };

    const chargerStatistiques = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch('http://localhost:8090/prospections/statistiques', {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    setStatistiques(data.statistiques);
                }
            }
        } catch (error) {
            console.error('Erreur lors du chargement des statistiques:', error);
        }
    };

    const prospectionsFiltrees = prospections.filter(prospection => {
        const matchSearch = !searchTerm ||
            prospection.nomProspect?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            prospection.prenomProspect?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            prospection.telephoneProspect?.includes(searchTerm) ||
            prospection.emailProspect?.toLowerCase().includes(searchTerm.toLowerCase());

        const matchStatut = !filterStatut || prospection.statut === filterStatut;
        const matchType = !filterType || prospection.typeProspection === filterType;

        return matchSearch && matchStatut && matchType;
    });

    const formaterDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getStatutBadge = (prospection: Prospection) => {
        const baseClasses = "px-2 py-1 rounded-full text-xs font-medium";
        return `${baseClasses} ${prospection.statutCssClass}`;
    };

    const voirDetails = (prospectionId: number) => {
        // Rediriger vers la page de détails
        window.location.href = `/prospections/${prospectionId}`;
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-64">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                <span className="ml-2">Chargement de vos prospections...</span>
            </div>
        );
    }

    return (
        <div className="max-w-7xl mx-auto p-6">
            {/* En-tête avec statistiques */}
            <div className="mb-6">
                <div className="flex items-center justify-between mb-4">
                    <h1 className="text-2xl font-bold text-gray-900 flex items-center">
                        <User className="w-6 h-6 mr-2" />
                        Mes Prospections
                    </h1>
                    <button
                        onClick={chargerDonnees}
                        className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                    >
                        <RefreshCw className="w-4 h-4 mr-2" />
                        Actualiser
                    </button>
                </div>

                {/* Statistiques */}
                {statistiques && (
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                        <div className="bg-white p-4 rounded-lg shadow border">
                            <div className="text-2xl font-bold text-blue-600">{statistiques.totalProspections}</div>
                            <div className="text-sm text-gray-600">Total prospections</div>
                        </div>
                        <div className="bg-white p-4 rounded-lg shadow border">
                            <div className="text-2xl font-bold text-green-600">{statistiques.prospectionsAujourdhui}</div>
                            <div className="text-sm text-gray-600">Aujourd'hui</div>
                        </div>
                        <div className="bg-white p-4 rounded-lg shadow border">
                            <div className="text-2xl font-bold text-orange-600">
                                {statistiques.repartitionStatuts['Converti'] || 0}
                            </div>
                            <div className="text-sm text-gray-600">Converties</div>
                        </div>
                        <div className="bg-white p-4 rounded-lg shadow border">
                            <div className="text-2xl font-bold text-purple-600">
                                {statistiques.repartitionStatuts['En cours'] || 0}
                            </div>
                            <div className="text-sm text-gray-600">En cours</div>
                        </div>
                    </div>
                )}
            </div>

            {/* Filtres et recherche */}
            <div className="bg-white p-4 rounded-lg shadow mb-6 border">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                    <div className="relative">
                        <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Rechercher par nom, téléphone..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>

                    <select
                        value={filterStatut}
                        onChange={(e) => setFilterStatut(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                        <option value="">Tous les statuts</option>
                        <option value="NOUVEAU">Nouveau</option>
                        <option value="ASSIGNE">Assigné</option>
                        <option value="EN_COURS">En cours</option>
                        <option value="CONVERTI">Converti</option>
                        <option value="ABANDONNE">Abandonné</option>
                    </select>

                    <select
                        value={filterType}
                        onChange={(e) => setFilterType(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                        <option value="">Tous les types</option>
                        <option value="PLANNING_AGENT">Planning agent</option>
                        <option value="CAMPAGNE_PROSPECTION">Campagne de prospection</option>
                        <option value="EVENEMENT_CULTUREL">Événement culturel</option>
                    </select>

                    <div className="flex items-center text-sm text-gray-600">
                        <Filter className="w-4 h-4 mr-1" />
                        {prospectionsFiltrees.length} sur {prospections.length} prospections
                    </div>
                </div>
            </div>

            {/* Liste des prospections */}
            <div className="bg-white rounded-lg shadow border">
                {prospectionsFiltrees.length === 0 ? (
                    <div className="text-center py-12">
                        <User className="w-12 h-12 mx-auto text-gray-300 mb-4" />
                        <p className="text-gray-500">
                            {prospections.length === 0
                                ? "Vous n'avez pas encore de prospections"
                                : "Aucune prospection ne correspond aux filtres"}
                        </p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50 border-b">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Prospect
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Contact
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Type
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Statut
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Date création
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Actions
                                </th>
                            </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                            {prospectionsFiltrees.map((prospection) => (
                                <tr key={prospection.id} className="hover:bg-gray-50">
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="flex items-center">
                                            <div className="flex-shrink-0 h-10 w-10">
                                                <div className="h-10 w-10 rounded-full bg-gray-300 flex items-center justify-center">
                                                    <User className="h-5 w-5 text-gray-600" />
                                                </div>
                                            </div>
                                            <div className="ml-4">
                                                <div className="text-sm font-medium text-gray-900">
                                                    {prospection.nomProspect && prospection.prenomProspect
                                                        ? `${prospection.prenomProspect} ${prospection.nomProspect}`
                                                        : 'Nom non renseigné'
                                                    }
                                                </div>
                                                <div className="text-sm text-gray-500">
                                                    ID: {prospection.id}
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="space-y-1">
                                            {prospection.telephoneProspect && (
                                                <div className="flex items-center text-sm text-gray-900">
                                                    <Phone className="h-4 w-4 mr-2 text-gray-400" />
                                                    {prospection.telephoneProspect}
                                                </div>
                                            )}
                                            {prospection.emailProspect && (
                                                <div className="flex items-center text-sm text-gray-900">
                                                    <Mail className="h-4 w-4 mr-2 text-gray-400" />
                                                    {prospection.emailProspect}
                                                </div>
                                            )}
                                            {!prospection.telephoneProspect && !prospection.emailProspect && (
                                                <span className="text-sm text-gray-500">Non renseigné</span>
                                            )}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="text-sm text-gray-900">{prospection.typeProspectionDisplay}</div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={getStatutBadge(prospection)}>
                        {prospection.statutDisplay}
                      </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="flex items-center text-sm text-gray-900">
                                            <Calendar className="h-4 w-4 mr-2 text-gray-400" />
                                            {formaterDate(prospection.dateCreation)}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                        <button
                                            onClick={() => voirDetails(prospection.id)}
                                            className="flex items-center text-blue-600 hover:text-blue-900 transition-colors"
                                        >
                                            <Eye className="h-4 w-4 mr-1" />
                                            Voir détails
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Résumé en bas de page */}
            {prospectionsFiltrees.length > 0 && (
                <div className="mt-6 bg-gray-50 rounded-lg p-4 border">
                    <div className="text-sm text-gray-600 text-center">
                        Affichage de {prospectionsFiltrees.length} prospection{prospectionsFiltrees.length > 1 ? 's' : ''}
                        {prospectionsFiltrees.length !== prospections.length && ` sur ${prospections.length} au total`}
                    </div>
                </div>
            )}
        </div>
    );
};

export default MesProspections;