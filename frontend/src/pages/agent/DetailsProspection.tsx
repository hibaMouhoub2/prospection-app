import React, { useState, useEffect } from 'react';
import { ArrowLeft, User, Phone, Mail, Calendar, MessageCircle, Building, Tag, Clock } from 'lucide-react';

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

interface Reponse {
    id: number;
    questionId: number;
    questionTexte: string;
    valeur: string;
    valeurFormatee: string;
    dateCreation: string;
}

interface ProspectionDetails {
    prospection: Prospection;
    reponses: Reponse[];
    reponsesMap: Record<number, string>;
}

interface DetailsProspectionProps {
    prospectionId: number;
}

const DetailsProspection: React.FC<DetailsProspectionProps> = ({ prospectionId }) => {
    const [details, setDetails] = useState<ProspectionDetails | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        chargerDetails();
    }, [prospectionId]);

    const chargerDetails = async () => {
        setLoading(true);
        setError(null);

        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`http://localhost:8090/prospections/${prospectionId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    setDetails(data);
                } else {
                    setError(data.message || 'Erreur lors du chargement');
                }
            } else if (response.status === 404) {
                setError('Prospection non trouvée');
            } else if (response.status === 403) {
                setError('Vous n\'avez pas le droit de voir cette prospection');
            } else {
                setError('Erreur lors du chargement');
            }
        } catch (err) {
            setError('Erreur de connexion');
            console.log(err);
        } finally {
            setLoading(false);
        }
    };

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
        const baseClasses = "px-3 py-1 rounded-full text-sm font-medium";
        return `${baseClasses} ${prospection.statutCssClass}`;
    };

    const retourListe = () => {
        window.history.back();
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-64">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                <span className="ml-2">Chargement des détails...</span>
            </div>
        );
    }

    if (error) {
        return (
            <div className="max-w-4xl mx-auto p-6">
                <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
                    <div className="text-red-600 text-lg font-medium mb-2">Erreur</div>
                    <div className="text-red-800">{error}</div>
                    <button
                        onClick={retourListe}
                        className="mt-4 flex items-center mx-auto px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors"
                    >
                        <ArrowLeft className="w-4 h-4 mr-2" />
                        Retour à la liste
                    </button>
                </div>
            </div>
        );
    }

    if (!details) {
        return null;
    }

    const { prospection, reponses } = details;

    return (
        <div className="max-w-6xl mx-auto p-6">
            {/* En-tête */}
            <div className="mb-6">
                <button
                    onClick={retourListe}
                    className="flex items-center text-gray-600 hover:text-gray-900 mb-4 transition-colors"
                >
                    <ArrowLeft className="w-4 h-4 mr-2" />
                    Retour à mes prospections
                </button>

                <div className="bg-white rounded-lg shadow-lg p-6 border">
                    <div className="flex items-center justify-between mb-4">
                        <h1 className="text-2xl font-bold text-gray-900 flex items-center">
                            <User className="w-6 h-6 mr-2" />
                            Prospection #{prospection.id}
                        </h1>
                        <span className={getStatutBadge(prospection)}>
              {prospection.statutDisplay}
            </span>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        {/* Informations du prospect */}
                        <div className="space-y-3">
                            <h3 className="text-lg font-medium text-gray-900 border-b pb-2">
                                Informations du prospect
                            </h3>

                            <div className="space-y-2">
                                {prospection.nomProspect && prospection.prenomProspect ? (
                                    <div className="flex items-center">
                                        <User className="w-4 h-4 mr-2 text-gray-400" />
                                        <span className="font-medium">
                      {prospection.prenomProspect} {prospection.nomProspect}
                    </span>
                                    </div>
                                ) : (
                                    <div className="flex items-center text-gray-500">
                                        <User className="w-4 h-4 mr-2" />
                                        <span>Nom non renseigné</span>
                                    </div>
                                )}

                                {prospection.telephoneProspect ? (
                                    <div className="flex items-center">
                                        <Phone className="w-4 h-4 mr-2 text-gray-400" />
                                        <a href={`tel:${prospection.telephoneProspect}`} className="text-blue-600 hover:text-blue-800">
                                            {prospection.telephoneProspect}
                                        </a>
                                    </div>
                                ) : (
                                    <div className="flex items-center text-gray-500">
                                        <Phone className="w-4 h-4 mr-2" />
                                        <span>Téléphone non renseigné</span>
                                    </div>
                                )}

                                {prospection.emailProspect ? (
                                    <div className="flex items-center">
                                        <Mail className="w-4 h-4 mr-2 text-gray-400" />
                                        <a href={`mailto:${prospection.emailProspect}`} className="text-blue-600 hover:text-blue-800">
                                            {prospection.emailProspect}
                                        </a>
                                    </div>
                                ) : (
                                    <div className="flex items-center text-gray-500">
                                        <Mail className="w-4 h-4 mr-2" />
                                        <span>Email non renseigné</span>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Informations de la prospection */}
                        <div className="space-y-3">
                            <h3 className="text-lg font-medium text-gray-900 border-b pb-2">
                                Détails de la prospection
                            </h3>

                            <div className="space-y-2">
                                <div className="flex items-center">
                                    <Tag className="w-4 h-4 mr-2 text-gray-400" />
                                    <span className="font-medium">Type:</span>
                                    <span className="ml-2">{prospection.typeProspectionDisplay}</span>
                                </div>

                                <div className="flex items-center">
                                    <Calendar className="w-4 h-4 mr-2 text-gray-400" />
                                    <span className="font-medium">Créée le:</span>
                                    <span className="ml-2">{formaterDate(prospection.dateCreation)}</span>
                                </div>

                                <div className="flex items-center">
                                    <User className="w-4 h-4 mr-2 text-gray-400" />
                                    <span className="font-medium">Créée par:</span>
                                    <span className="ml-2">
                    {prospection.createur.prenom} {prospection.createur.nom}
                  </span>
                                </div>

                                {prospection.agentAssigne && (
                                    <div className="flex items-center">
                                        <User className="w-4 h-4 mr-2 text-gray-400" />
                                        <span className="font-medium">Assignée à:</span>
                                        <span className="ml-2">
                      {prospection.agentAssigne.prenom} {prospection.agentAssigne.nom}
                    </span>
                                    </div>
                                )}

                                {prospection.branche && (
                                    <div className="flex items-center">
                                        <Building className="w-4 h-4 mr-2 text-gray-400" />
                                        <span className="font-medium">Branche:</span>
                                        <span className="ml-2">{prospection.branche.nom}</span>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Commentaire */}
                        <div className="space-y-3">
                            <h3 className="text-lg font-medium text-gray-900 border-b pb-2">
                                Commentaire
                            </h3>

                            {prospection.commentaire ? (
                                <div className="bg-gray-50 rounded-lg p-3 border">
                                    <div className="flex items-start">
                                        <MessageCircle className="w-4 h-4 mr-2 text-gray-400 mt-0.5" />
                                        <p className="text-sm text-gray-700 whitespace-pre-wrap">
                                            {prospection.commentaire}
                                        </p>
                                    </div>
                                </div>
                            ) : (
                                <div className="text-gray-500 text-sm italic">
                                    Aucun commentaire ajouté
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Réponses au questionnaire */}
            <div className="bg-white rounded-lg shadow-lg border">
                <div className="px-6 py-4 border-b border-gray-200">
                    <h2 className="text-xl font-bold text-gray-900 flex items-center">
                        <MessageCircle className="w-5 h-5 mr-2" />
                        Réponses au questionnaire
                    </h2>
                </div>

                <div className="p-6">
                    {reponses.length === 0 ? (
                        <div className="text-center py-8 text-gray-500">
                            <MessageCircle className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                            <p>Aucune réponse enregistrée</p>
                        </div>
                    ) : (
                        <div className="space-y-6">
                            {reponses.map((reponse, index) => (
                                <div key={reponse.id} className="border-b border-gray-200 pb-4 last:border-b-0">
                                    <div className="flex items-start justify-between mb-2">
                                        <h4 className="font-medium text-gray-900 flex items-center">
                      <span className="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-1 rounded-full mr-2">
                        Q{index + 1}
                      </span>
                                            {reponse.questionTexte}
                                        </h4>
                                        <div className="flex items-center text-xs text-gray-500">
                                            <Clock className="w-3 h-3 mr-1" />
                                            {formaterDate(reponse.dateCreation)}
                                        </div>
                                    </div>

                                    <div className="ml-8">
                                        <div className="bg-gray-50 rounded-lg p-3 border">
                                            <p className="text-gray-800 whitespace-pre-wrap">
                                                {reponse.valeurFormatee || reponse.valeur || 'Aucune réponse'}
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {/* Actions */}
            <div className="mt-6 bg-white rounded-lg shadow border p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Actions disponibles</h3>

                <div className="flex flex-wrap gap-3">
                    {prospection.telephoneProspect && (
                        <a
                            href={`tel:${prospection.telephoneProspect}`}
                            className="flex items-center px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors"
                        >
                            <Phone className="w-4 h-4 mr-2" />
                            Appeler
                        </a>
                    )}

                    {prospection.emailProspect && (
                        <a
                            href={`mailto:${prospection.emailProspect}`}
                            className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                        >
                            <Mail className="w-4 h-4 mr-2" />
                            Envoyer un email
                        </a>
                    )}

                    <button
                        onClick={() => window.print()}
                        className="flex items-center px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors"
                    >
                        <Calendar className="w-4 h-4 mr-2" />
                        Imprimer
                    </button>
                </div>

                <div className="mt-4 text-sm text-gray-500">
                    Dernière modification: {formaterDate(prospection.dateCreation)}
                </div>
            </div>
        </div>
    );
};

export default DetailsProspection;