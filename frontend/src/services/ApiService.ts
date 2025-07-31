const API_BASE_URL = 'http://localhost:8090/api';

export class ApiService {
    private getAuthHeaders(): Record<string, string> {
        const token = localStorage.getItem('auth_token');
        return {
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        };
    }

    async get<T>(endpoint: string): Promise<T> {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            headers: this.getAuthHeaders()
        });
        return response.json();
    }

    async post<T>(endpoint: string, data: Date): Promise<T> {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(data)
        });
        return response.json();
    }
}

export const apiService = new ApiService();