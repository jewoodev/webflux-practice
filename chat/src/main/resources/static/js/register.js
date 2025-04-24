document.addEventListener('DOMContentLoaded', () => {
    auth = new Auth();
    
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password })
                });

                if (response.ok) {
                    window.location.href = '/login';
                } else {
                    const data = await response.json();
                    auth.showError(data.message || 'Registration failed');
                }
            } catch (error) {
                auth.showError('An error occurred during registration');
            }
        });
    }
});