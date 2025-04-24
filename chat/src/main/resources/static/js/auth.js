// JWT token management
class Auth {
    TOKEN_KEY;
    constructor() {
        this.TOKEN_KEY = 'jwt_token';
    }

    setToken(token) {
        try {
            localStorage.setItem(this.TOKEN_KEY, token);
            const savedToken = localStorage.getItem(this.TOKEN_KEY);
            if (savedToken !== token) {
                throw new Error('Token not saved correctly');
            }
            return true;
        } catch (error) {
            console.error('Error saving token:', error);
            return false;
        }
    }

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    removeToken() {
        localStorage.removeItem(this.TOKEN_KEY);
    }

    setUsername(username) {
        localStorage.setItem('username', username);
    }

    getUsername() {
        return localStorage.getItem('username');
    }

    showError(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        errorDiv.textContent = message;
        document.querySelector('.auth-box').insertBefore(errorDiv, document.querySelector('form'));
        errorDiv.style.display = 'block';
        setTimeout(() => errorDiv.remove(), 3000);
    }

    getAuthHeaders() {
        const token = this.getToken();
        return {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        };
    }

    // 로그아웃 처리
    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        window.location.href = '/login';
    }
}