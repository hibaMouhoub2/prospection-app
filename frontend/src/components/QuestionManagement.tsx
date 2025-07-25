import  { useState, useEffect } from 'react';
import {
    Plus,
    GripVertical,
    Eye,
    EyeOff,
    Trash2,
    CheckCircle,
    AlertCircle,
    Phone,
    Hash,
    Type,
    List,
    CheckSquare,
    BarChart3
} from 'lucide-react';

// Types
interface QuestionOption {
    id?: number;
    valeur: string;
    ordre: number;
}

interface Question {
    id: number;
    question: string;
    description?: string;
    type: string;
    typeDisplayName: string;
    ordre: number;
    actif: boolean;
    obligatoire: boolean;
    dateCreation: string;
    options?: QuestionOption[];
}

interface QuestionType {
    name: string;
    displayName: string;
    description: string;
    requiresOptions: boolean;
    validationPattern?: string;
    validationMessage?: string;
}

interface CreateQuestionForm {
    question: string;
    description: string;
    type: string;
    obligatoire: boolean;
    options: string[];
}

// Service API
class QuestionAPI {
    private static BASE_URL = '/api/questions';

    private static getAuthHeaders() {
        const token = localStorage.getItem('auth_token');
        return {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        };
    }

    static async getAllQuestions(): Promise<Question[]> {
        const response = await fetch(`${this.BASE_URL}/admin`, {
            headers: this.getAuthHeaders()
        });
        const data = await response.json();
        return data.success ? data.questions : [];
    }

    static async getQuestionTypes(): Promise<Record<string, QuestionType>> {
        const response = await fetch(`${this.BASE_URL}/types`, {
            headers: this.getAuthHeaders()
        });
        const data = await response.json();
        return data.success ? data.types : {};
    }

    static async createQuestion(questionData: CreateQuestionForm) {
        const response = await fetch(this.BASE_URL, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(questionData)
        });
        return response.json();
    }

    static async reorderQuestions(ordreIds: number[]) {
        const response = await fetch(`${this.BASE_URL}/reorder`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify({ ordreIds })
        });
        return response.json();
    }

    static async toggleQuestionStatus(id: number, activate: boolean) {
        const endpoint = activate ? 'activer' : 'desactiver';
        const response = await fetch(`${this.BASE_URL}/${id}/${endpoint}`, {
            method: 'PUT',
            headers: this.getAuthHeaders()
        });
        return response.json();
    }

    static async getFormPreview(): Promise<Question[]> {
        const response = await fetch(`${this.BASE_URL}/apercu`, {
            headers: this.getAuthHeaders()
        });
        const data = await response.json();
        return data.success ? data.questions : [];
    }

    static async getStats() {
        const response = await fetch(`${this.BASE_URL}/stats`, {
            headers: this.getAuthHeaders()
        });
        const data = await response.json();
        return data.success ? data.statistiques : {};
    }
}

// Composant pour créer une question
function CreateQuestionForm({ onQuestionCreated, questionTypes }: {
    onQuestionCreated: () => void;
    questionTypes: Record<string, QuestionType>;
}) {
    const [form, setForm] = useState<CreateQuestionForm>({
        question: '',
        description: '',
        type: 'TEXT',
        obligatoire: false,
        options: ['', '']
    });
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);

    const currentType = questionTypes[form.type];
    const needsOptions = currentType?.requiresOptions || false;

    const handleInputChange = (field: keyof CreateQuestionForm, value: string | boolean | string[]) => {
        setForm(prev => ({ ...prev, [field]: value }));
        if (message) setMessage(null);
    };

    const handleOptionChange = (index: number, value: string) => {
        const newOptions = [...form.options];
        newOptions[index] = value;
        setForm(prev => ({ ...prev, options: newOptions }));
    };

    const addOption = () => {
        setForm(prev => ({ ...prev, options: [...prev.options, ''] }));
    };

    const removeOption = (index: number) => {
        if (form.options.length > 2) {
            const newOptions = form.options.filter((_, i) => i !== index);
            setForm(prev => ({ ...prev, options: newOptions }));
        }
    };

    const handleSubmit = async () => {
        setLoading(true);
        setMessage(null);

        try {
            // Validation côté client
            if (!form.question.trim()) {
                throw new Error('La question est obligatoire');
            }

            if (needsOptions) {
                const validOptions = form.options.filter(opt => opt.trim());
                if (validOptions.length < 2) {
                    throw new Error('Au moins 2 options sont requises');
                }
            }

            const submitData = {
                ...form,
                options: needsOptions ? form.options.filter(opt => opt.trim()) : []
            };

            const result = await QuestionAPI.createQuestion(submitData);

            if (result.success) {
                setMessage({ type: 'success', text: result.message });
                setForm({
                    question: '',
                    description: '',
                    type: 'TEXT',
                    obligatoire: false,
                    options: ['', '']
                });
                onQuestionCreated();
            } else {
                setMessage({ type: 'error', text: result.message });
            }
        } catch (error: unknown) {
            const errorMessage = error instanceof Error ? error.message : 'Une erreur est survenue';
            setMessage({ type: 'error', text: errorMessage });
        } finally {
            setLoading(false);
        }
    };



    return (
        <div className="bg-white rounded-lg shadow-md p-6">
            <h3 className="text-lg font-semibold mb-4 flex items-center">
                <Plus className="w-5 h-5 mr-2" />
                Créer une nouvelle question
            </h3>

            {message && (
                <div className={`mb-4 p-3 rounded-lg flex items-center ${
                    message.type === 'success'
                        ? 'bg-green-50 text-green-700 border border-green-200'
                        : 'bg-red-50 text-red-700 border border-red-200'
                }`}>
                    {message.type === 'success' ?
                        <CheckCircle className="w-4 h-4 mr-2" /> :
                        <AlertCircle className="w-4 h-4 mr-2" />
                    }
                    {message.text}
                </div>
            )}

            <div className="space-y-4">
                {/* Question */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Question *
                    </label>
                    <input
                        type="text"
                        value={form.question}
                        onChange={(e) => handleInputChange('question', e.target.value)}
                        placeholder="Tapez votre question ici..."
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>

                {/* Description */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Description (optionnelle)
                    </label>
                    <textarea
                        value={form.description}
                        onChange={(e) => handleInputChange('description', e.target.value)}
                        placeholder="Ajoutez une description pour aider les utilisateurs..."
                        rows={2}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>

                {/* Type */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Type de question *
                    </label>
                    <select
                        value={form.type}
                        onChange={(e) => handleInputChange('type', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                        {Object.entries(questionTypes).map(([key, type]) => (
                            <option key={key} value={key}>
                                {type.displayName} - {type.description}
                            </option>
                        ))}
                    </select>
                </div>

                {/* Options (si nécessaire) */}
                {needsOptions && (
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Options de réponse *
                        </label>
                        <div className="space-y-2">
                            {form.options.map((option, index) => (
                                <div key={index} className="flex items-center space-x-2">
                                    <input
                                        type="text"
                                        value={option}
                                        onChange={(e) => handleOptionChange(index, e.target.value)}
                                        placeholder={`Option ${index + 1}`}
                                        className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    />
                                    {form.options.length > 2 && (
                                        <button
                                            onClick={() => removeOption(index)}
                                            className="text-red-500 hover:text-red-700"
                                        >
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    )}
                                </div>
                            ))}
                            <button
                                onClick={addOption}
                                className="text-blue-600 hover:text-blue-700 text-sm flex items-center"
                            >
                                <Plus className="w-4 h-4 mr-1" />
                                Ajouter une option
                            </button>
                        </div>
                    </div>
                )}

                {/* Obligatoire */}
                <div className="flex items-center">
                    <input
                        type="checkbox"
                        id="obligatoire"
                        checked={form.obligatoire}
                        onChange={(e) => handleInputChange('obligatoire', e.target.checked)}
                        className="h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                    />
                    <label htmlFor="obligatoire" className="ml-2 text-sm text-gray-700">
                        Question obligatoire
                    </label>
                </div>

                {/* Bouton Submit */}
                <button
                    onClick={handleSubmit}
                    disabled={loading || !form.question.trim()}
                    className={`w-full py-2 px-4 rounded-md font-medium transition-colors ${
                        loading || !form.question.trim()
                            ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                            : 'bg-blue-600 hover:bg-blue-700 text-white'
                    }`}
                >
                    {loading ? (
                        <div className="flex items-center justify-center">
                            <div className="w-4 h-4 border-2 border-gray-300 border-t-white rounded-full animate-spin mr-2"></div>
                            Création...
                        </div>
                    ) : (
                        'Créer la question'
                    )}
                </button>
            </div>
        </div>
    );
}

// Composant principal de gestion des questions
function QuestionManagement() {
    const [questions, setQuestions] = useState<Question[]>([]);
    const [questionTypes, setQuestionTypes] = useState<Record<string, QuestionType>>({});
    const [previewQuestions, setPreviewQuestions] = useState<Question[]>([]);
    const [showPreview, setShowPreview] = useState(true);
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState<Record<string, number>>({});

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        setLoading(true);
        try {
            const [questionsData, typesData, previewData, statsData] = await Promise.all([
                QuestionAPI.getAllQuestions(),
                QuestionAPI.getQuestionTypes(),
                QuestionAPI.getFormPreview(),
                QuestionAPI.getStats()
            ]);

            setQuestions(questionsData);
            setQuestionTypes(typesData);
            setPreviewQuestions(previewData);
            setStats(statsData);
        } catch (error) {
            console.error('Erreur lors du chargement:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleQuestionCreated = () => {
        loadData(); // Recharger toutes les données
    };

    const handleToggleStatus = async (id: number, currentStatus: boolean) => {
        try {
            const result = await QuestionAPI.toggleQuestionStatus(id, !currentStatus);
            if (result.success) {
                loadData();
            }
        } catch (error) {
            console.error('Erreur lors du changement de statut:', error);
        }
    };

    const getTypeIcon = (type: string) => {
        switch (type) {
            case 'TEXT': return <Type className="w-4 h-4 text-blue-500" />;
            case 'NUMBER': return <Hash className="w-4 h-4 text-green-500" />;
            case 'PHONE': return <Phone className="w-4 h-4 text-purple-500" />;
            case 'CHOICE': return <List className="w-4 h-4 text-orange-500" />;
            case 'MULTIPLE_CHOICE': return <CheckSquare className="w-4 h-4 text-red-500" />;
            default: return <Type className="w-4 h-4 text-gray-500" />;
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                    <div className="w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                    <p className="text-gray-600">Chargement des questions...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-6">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">Gestion des Questions</h1>
                    <p className="text-gray-600">Créez et organisez les questions du formulaire de prospection</p>

                    {/* Statistiques */}
                    {stats && (
                        <div className="mt-4 grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div className="bg-white rounded-lg p-4 shadow-sm">
                                <div className="flex items-center">
                                    <BarChart3 className="w-5 h-5 text-blue-500 mr-2" />
                                    <div>
                                        <p className="text-sm text-gray-600">Total Questions</p>
                                        <p className="text-2xl font-bold text-gray-900">{stats.totalQuestions || 0}</p>
                                    </div>
                                </div>
                            </div>
                            <div className="bg-white rounded-lg p-4 shadow-sm">
                                <div className="flex items-center">
                                    <CheckCircle className="w-5 h-5 text-green-500 mr-2" />
                                    <div>
                                        <p className="text-sm text-gray-600">Questions Actives</p>
                                        <p className="text-2xl font-bold text-gray-900">{stats.questionsActives || 0}</p>
                                    </div>
                                </div>
                            </div>
                            <div className="bg-white rounded-lg p-4 shadow-sm">
                                <div className="flex items-center">
                                    <EyeOff className="w-5 h-5 text-gray-500 mr-2" />
                                    <div>
                                        <p className="text-sm text-gray-600">Questions Inactives</p>
                                        <p className="text-2xl font-bold text-gray-900">
                                            {(stats.totalQuestions || 0) - (stats.questionsActives || 0)}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {/* Colonne gauche : Gestion des questions */}
                    <div className="space-y-6">
                        {/* Formulaire de création */}
                        <CreateQuestionForm
                            onQuestionCreated={handleQuestionCreated}
                            questionTypes={questionTypes}
                        />

                        {/* Liste des questions */}
                        <div className="bg-white rounded-lg shadow-md p-6">
                            <div className="flex items-center justify-between mb-4">
                                <h3 className="text-lg font-semibold">Questions créées ({questions.length})</h3>
                                <button
                                    onClick={() => setShowPreview(!showPreview)}
                                    className="text-blue-600 hover:text-blue-700 flex items-center text-sm"
                                >
                                    {showPreview ? <EyeOff className="w-4 h-4 mr-1" /> : <Eye className="w-4 h-4 mr-1" />}
                                    {showPreview ? 'Masquer aperçu' : 'Voir aperçu'}
                                </button>
                            </div>

                            {questions.length === 0 ? (
                                <div className="text-center py-8 text-gray-500">
                                    <Type className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                                    <p>Aucune question créée pour le moment</p>
                                    <p className="text-sm">Commencez par créer votre première question</p>
                                </div>
                            ) : (
                                <div className="space-y-3">
                                    {questions.map((question) => (
                                        <div
                                            key={question.id}
                                            className={`border rounded-lg p-4 transition-colors ${
                                                question.actif
                                                    ? 'border-gray-200 bg-white'
                                                    : 'border-gray-100 bg-gray-50'
                                            }`}
                                        >
                                            <div className="flex items-start justify-between">
                                                <div className="flex-1">
                                                    <div className="flex items-center space-x-2 mb-2">
                                                        <GripVertical className="w-4 h-4 text-gray-400 cursor-move" />
                                                        {getTypeIcon(question.type)}
                                                        <span className="text-xs text-gray-500 bg-gray-100 px-2 py-1 rounded">
                              {question.typeDisplayName}
                            </span>
                                                        {question.obligatoire && (
                                                            <span className="text-xs text-red-600 bg-red-100 px-2 py-1 rounded">
                                Obligatoire
                              </span>
                                                        )}
                                                        <span className="text-xs text-gray-500">#{question.ordre}</span>
                                                    </div>

                                                    <h4 className={`font-medium mb-1 ${
                                                        question.actif ? 'text-gray-900' : 'text-gray-500'
                                                    }`}>
                                                        {question.question}
                                                    </h4>

                                                    {question.description && (
                                                        <p className="text-sm text-gray-600 mb-2">{question.description}</p>
                                                    )}

                                                    {question.options && question.options.length > 0 && (
                                                        <div className="mt-2">
                                                            <p className="text-xs text-gray-500 mb-1">Options :</p>
                                                            <div className="flex flex-wrap gap-1">
                                                                {question.options.map((option, idx) => (
                                                                    <span
                                                                        key={idx}
                                                                        className="text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded"
                                                                    >
                                    {option.valeur}
                                  </span>
                                                                ))}
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>

                                                <div className="ml-4 flex items-center space-x-2">
                                                    <button
                                                        onClick={() => handleToggleStatus(question.id, question.actif)}
                                                        className={`p-2 rounded-lg transition-colors ${
                                                            question.actif
                                                                ? 'text-green-600 hover:bg-green-50'
                                                                : 'text-gray-400 hover:bg-gray-100'
                                                        }`}
                                                        title={question.actif ? 'Désactiver' : 'Activer'}
                                                    >
                                                        {question.actif ? <Eye className="w-4 h-4" /> : <EyeOff className="w-4 h-4" />}
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Colonne droite : Aperçu du formulaire */}
                    {showPreview && (
                        <div className="bg-white rounded-lg shadow-md p-6">
                            <h3 className="text-lg font-semibold mb-4 flex items-center">
                                <Eye className="w-5 h-5 mr-2" />
                                Aperçu du formulaire
                                <span className="ml-2 text-sm text-gray-500">
                  (Tel que vu par les agents)
                </span>
                            </h3>

                            {previewQuestions.length === 0 ? (
                                <div className="text-center py-8 text-gray-500">
                                    <AlertCircle className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                                    <p>Aucune question active</p>
                                    <p className="text-sm">Créez et activez des questions pour les voir ici</p>
                                </div>
                            ) : (
                                <div className="space-y-6">
                                    <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
                                        <p className="text-sm text-blue-700">
                                            <strong>Prévisualisation :</strong> Voici comment les agents verront le formulaire de prospection
                                        </p>
                                    </div>

                                    <form className="space-y-6">
                                        {previewQuestions.map((question) => (
                                            <div key={question.id} className="space-y-2">
                                                <label className="block text-sm font-medium text-gray-700">
                                                    {question.question}
                                                    {question.obligatoire && <span className="text-red-500 ml-1">*</span>}
                                                </label>

                                                {question.description && (
                                                    <p className="text-xs text-gray-500 mb-2">{question.description}</p>
                                                )}

                                                {/* Rendu selon le type */}
                                                {question.type === 'TEXT' && (
                                                    <input
                                                        type="text"
                                                        disabled
                                                        placeholder="Réponse texte..."
                                                        className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
                                                    />
                                                )}

                                                {question.type === 'NUMBER' && (
                                                    <input
                                                        type="number"
                                                        disabled
                                                        placeholder="Nombre entier..."
                                                        className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
                                                    />
                                                )}

                                                {question.type === 'PHONE' && (
                                                    <input
                                                        type="tel"
                                                        disabled
                                                        placeholder="06XXXXXXXX"
                                                        className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
                                                    />
                                                )}

                                                {question.type === 'CHOICE' && question.options && (
                                                    <div className="space-y-2">
                                                        {question.options.map((option, idx) => (
                                                            <label key={idx} className="flex items-center">
                                                                <input
                                                                    type="radio"
                                                                    name={`question_${question.id}`}
                                                                    disabled
                                                                    className="h-4 w-4 text-blue-600 border-gray-300"
                                                                />
                                                                <span className="ml-2 text-sm text-gray-700">{option.valeur}</span>
                                                            </label>
                                                        ))}
                                                    </div>
                                                )}

                                                {question.type === 'MULTIPLE_CHOICE' && question.options && (
                                                    <div className="space-y-2">
                                                        {question.options.map((option, idx) => (
                                                            <label key={idx} className="flex items-center">
                                                                <input
                                                                    type="checkbox"
                                                                    disabled
                                                                    className="h-4 w-4 text-blue-600 border-gray-300 rounded"
                                                                />
                                                                <span className="ml-2 text-sm text-gray-700">{option.valeur}</span>
                                                            </label>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        ))}

                                        <div className="pt-4 border-t border-gray-200">
                                            <button
                                                type="button"
                                                disabled
                                                className="w-full bg-blue-600 text-white py-2 px-4 rounded-md opacity-50 cursor-not-allowed"
                                            >
                                                Enregistrer la prospection
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default QuestionManagement;