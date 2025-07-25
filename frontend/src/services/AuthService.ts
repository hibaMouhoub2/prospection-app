import  {type User} from '../types';

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

export default AuthService;
