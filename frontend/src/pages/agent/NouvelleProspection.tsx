import React, { useState, useEffect } from 'react';
import { Save, AlertCircle, CheckCircle, Phone, Mail, User, Calendar, MessageCircle } from 'lucide-react';

interface Question {
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

interface TypeProspection {
    value: string;
    label: string;
    description: string;
}

interface Formulaire {
    questions: Question[];
    typesProspection: TypeProspection[];
}

const NouvelleProspection: React.FC = () => {
    const [formulaire, setFormulaire] = useState<Formulaire | null>(null);
    const [loading, setLoading] = useState(true);
    const [reponses, setReponses] = useState<Record<number, string>>({});
    const [typeProspection, setTypeProspection] = useState<string>('');
    const [commentaire, setCommentaire] = useState('');
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [submitting, setSubmitting] = useState(false);
    const [success, setSuccess] = useState(false);

    // Charger le formulaire au montage du composant
    useEffect(() => {
        chargerFormulaire();
    }, []);

    const chargerFormulaire = async () => {
        try {
            const token = localStorage.getItem('auth_token');
            const response = await fetch('http://localhost:8090/api/prospections/formulaire', {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    setFormulaire({
                        questions: data.questions,
                        typesProspection: data.typesProspection
                    });
                }
            } else {
                console.error('Erreur lors du chargement du formulaire');
            }
        } catch (error) {
            console.error('Erreur:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleReponseChange = (questionId: number, valeur: string) => {
        setReponses(prev => ({
            ...prev,
            [questionId]: valeur
        }));

        // Supprimer l'erreur si elle existe
        if (errors[`question_${questionId}`]) {
            setErrors(prev => {
                const newErrors = { ...prev };
                delete newErrors[`question_${questionId}`];
                return newErrors;
            });
        }
    };

    const validerFormulaire = (): boolean => {
        const newErrors: Record<string, string> = {};

        // Vérifier le type de prospection
        if (!typeProspection) {
            newErrors.typeProspection = 'Le type de prospection est obligatoire';
        }

        // Vérifier les questions obligatoires
        formulaire?.questions.forEach(question => {
            if (question.obligatoire) {
                const valeur = reponses[question.id];
                if (!valeur || valeur.trim() === '') {
                    newErrors[`question_${question.id}`] = 'Cette question est obligatoire';
                } else {
                    // Validation par type
                    if (question.type === 'EMAIL' && !isValidEmail(valeur)) {
                        newErrors[`question_${question.id}`] = 'Format email invalide';
                    } else if (question.type === 'PHONE' && !isValidPhone(valeur)) {
                        newErrors[`question_${question.id}`] = 'Le numéro doit commencer par 06 ou 07 et contenir 10 chiffres';
                    } else if (question.type === 'NUMBER' && !isValidNumber(valeur)) {
                        newErrors[`question_${question.id}`] = 'Veuillez saisir un nombre valide';
                    }
                }
            }
        });

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const isValidEmail = (email: string): boolean => {
        return /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email);
    };

    const isValidPhone = (phone: string): boolean => {
        return /^(06|07)\d{8}$/.test(phone);
    };

    const isValidNumber = (value: string): boolean => {
        return /^\d+$/.test(value);
    };

    const soumettre = async () => {
        if (!validerFormulaire()) {
            return;
        }

        setSubmitting(true);

        try {
            const token = localStorage.getItem('auth_token');
            const response = await fetch('http://localhost:8090/api/prospections', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    typeProspection,
                    reponses,
                    commentaire: commentaire.trim() || null
                })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                setSuccess(true);
                // Réinitialiser le formulaire
                setReponses({});
                setTypeProspection('');
                setCommentaire('');
                setTimeout(() => setSuccess(false), 5000);
            } else {
                if (response.status === 409 && data.type === 'DUPLICATE_WARNING') {
                    // Gestion des doublons
                    const confirmer = window.confirm(
                        `${data.message}\n\nVoulez-vous continuer quand même ?`
                    );
                    if (confirmer) {
                        // Retry sans vérification de doublon
                        // Cette logique peut être implémentée côté backend
                    }
                } else {
                    setErrors({ general: data.message || 'Erreur lors de l\'enregistrement' });
                }
            }
        } catch (error) {
            setErrors({ general: 'Erreur de connexion' });
            console.log(error);
        } finally {
            setSubmitting(false);
        }
    };

    const renderQuestion = (question: Question) => {
        const valeur = reponses[question.id] || '';
        const hasError = errors[`question_${question.id}`];

        const inputClass = `w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
            hasError ? 'border-red-500' : 'border-gray-300'
        }`;

        switch (question.type) {
            case 'TEXT':
                return (
                    <input
                        type="text"
                        value={valeur}
                        onChange={(e) => handleReponseChange(question.id, e.target.value)}
                        className={inputClass}
                        placeholder="Votre réponse..."
                    />
                );

            case 'TEXTAREA':
                return (
                    <textarea
                        value={valeur}
                        onChange={(e) => handleReponseChange(question.id, e.target.value)}
                        className={inputClass}
                        rows={3}
                        placeholder="Votre réponse..."
                    />
                );

            case 'NUMBER':
                return (
                    <input
                        type="number"
                        value={valeur}
                        onChange={(e) => handleReponseChange(question.id, e.target.value)}
                        className={inputClass}
                        placeholder="Nombre..."
                    />
                );

            case 'EMAIL':
                return (
                    <div className="relative">
                        <Mail className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                        <input
                            type="email"
                            value={valeur}
                            onChange={(e) => handleReponseChange(question.id, e.target.value)}
                            className={`${inputClass} pl-10`}
                            placeholder="exemple@email.com"
                        />
                    </div>
                );

            case 'PHONE':
                return (
                    <div className="relative">
                        <Phone className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                        <input
                            type="tel"
                            value={valeur}
                            onChange={(e) => handleReponseChange(question.id, e.target.value)}
                            className={`${inputClass} pl-10`}
                            placeholder="0612345678"
                            maxLength={10}
                        />
                    </div>
                );

            case 'DATE':
                return (
                    <div className="relative">
                        <Calendar className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                        <input
                            type="date"
                            value={valeur}
                            onChange={(e) => handleReponseChange(question.id, e.target.value)}
                            className={`${inputClass} pl-10`}
                        />
                    </div>
                );

            case 'CHOICE':
                return (
                    <div className="space-y-2">
                        {question.options?.map((option) => (
                            <label key={option.id} className="flex items-center space-x-2">
                                <input
                                    type="radio"
                                    name={`question_${question.id}`}
                                    value={option.valeur}
                                    checked={valeur === option.valeur}
                                    onChange={(e) => handleReponseChange(question.id, e.target.value)}
                                    className="text-blue-600 focus:ring-blue-500"
                                />
                                <span>{option.valeur}</span>
                            </label>
                        ))}
                    </div>
                );

            case 'MULTIPLE_CHOICE':
                return (
                    <div className="space-y-2">
                        {question.options?.map((option) => {
                            const selections = valeur ? valeur.split(',') : [];
                            const isChecked = selections.includes(option.valeur);

                            return (
                                <label key={option.id} className="flex items-center space-x-2">
                                    <input
                                        type="checkbox"
                                        checked={isChecked}
                                        onChange={(e) => {
                                            let newSelections;
                                            if (e.target.checked) {
                                                newSelections = [...selections, option.valeur];
                                            } else {
                                                newSelections = selections.filter(s => s !== option.valeur);
                                            }
                                            handleReponseChange(question.id, newSelections.join(','));
                                        }}
                                        className="text-blue-600 focus:ring-blue-500"
                                    />
                                    <span>{option.valeur}</span>
                                </label>
                            );
                        })}
                    </div>
                );

            default:
                return null;
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-64">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                <span className="ml-2">Chargement du formulaire...</span>
            </div>
        );
    }

    if (!formulaire) {
        return (
            <div className="text-center py-8 text-red-600">
                <AlertCircle className="w-12 h-12 mx-auto mb-4" />
                <p>Erreur lors du chargement du formulaire</p>
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto p-6">
            <div className="bg-white rounded-lg shadow-lg">
                <div className="px-6 py-4 border-b border-gray-200">
                    <h1 className="text-2xl font-bold text-gray-900 flex items-center">
                        <User className="w-6 h-6 mr-2" />
                        Nouvelle Prospection
                    </h1>
                    <p className="text-gray-600 mt-1">Remplissez le formulaire pour enregistrer un nouveau prospect</p>
                </div>

                <form className="p-6 space-y-6">
                    {/* Messages de succès/erreur */}
                    {success && (
                        <div className="bg-green-50 border border-green-200 rounded-md p-4 flex items-center">
                            <CheckCircle className="w-5 h-5 text-green-600 mr-2" />
                            <span className="text-green-800">Prospection enregistrée avec succès !</span>
                        </div>
                    )}

                    {errors.general && (
                        <div className="bg-red-50 border border-red-200 rounded-md p-4 flex items-center">
                            <AlertCircle className="w-5 h-5 text-red-600 mr-2" />
                            <span className="text-red-800">{errors.general}</span>
                        </div>
                    )}

                    {/* Type de prospection */}
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700">
                            Type de prospection <span className="text-red-500">*</span>
                        </label>
                        <select
                            value={typeProspection}
                            onChange={(e) => setTypeProspection(e.target.value)}
                            className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                                errors.typeProspection ? 'border-red-500' : 'border-gray-300'
                            }`}
                        >
                            <option value="">Sélectionnez un type</option>
                            {formulaire.typesProspection.map((type) => (
                                <option key={type.value} value={type.value}>
                                    {type.label}
                                </option>
                            ))}
                        </select>
                        {errors.typeProspection && (
                            <p className="text-red-600 text-sm">{errors.typeProspection}</p>
                        )}
                        {typeProspection && (
                            <p className="text-gray-600 text-sm">
                                {formulaire.typesProspection.find(t => t.value === typeProspection)?.description}
                            </p>
                        )}
                    </div>

                    {/* Questions du formulaire */}
                    <div className="space-y-6">
                        <h3 className="text-lg font-medium text-gray-900 border-b pb-2">
                            Informations du prospect
                        </h3>

                        {formulaire.questions.map((question) => (
                            <div key={question.id} className="space-y-2">
                                <label className="block text-sm font-medium text-gray-700">
                                    {question.question}
                                    {question.obligatoire && <span className="text-red-500 ml-1">*</span>}
                                </label>

                                {question.description && (
                                    <p className="text-xs text-gray-500">{question.description}</p>
                                )}

                                {renderQuestion(question)}

                                {errors[`question_${question.id}`] && (
                                    <p className="text-red-600 text-sm">{errors[`question_${question.id}`]}</p>
                                )}
                            </div>
                        ))}
                    </div>

                    {/* Commentaire optionnel */}
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700 flex items-center">
                            <MessageCircle className="w-4 h-4 mr-1" />
                            Commentaire (optionnel)
                        </label>
                        <textarea
                            value={commentaire}
                            onChange={(e) => setCommentaire(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            rows={3}
                            placeholder="Notes ou observations sur ce prospect..."
                            maxLength={1000}
                        />
                        <p className="text-xs text-gray-500">{commentaire.length}/1000 caractères</p>
                    </div>

                    {/* Boutons d'action */}
                    <div className="flex items-center justify-between pt-6 border-t border-gray-200">
                        <button
                            type="button"
                            onClick={() => {
                                setReponses({});
                                setTypeProspection('');
                                setCommentaire('');
                                setErrors({});
                            }}
                            className="px-4 py-2 text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200 transition-colors"
                        >
                            Réinitialiser
                        </button>

                        <button
                            type="button"
                            onClick={soumettre}
                            disabled={submitting}
                            className="flex items-center px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 transition-colors"
                        >
                            {submitting ? (
                                <>
                                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                                    Enregistrement...
                                </>
                            ) : (
                                <>
                                    <Save className="w-4 h-4 mr-2" />
                                    Enregistrer la prospection
                                </>
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default NouvelleProspection;