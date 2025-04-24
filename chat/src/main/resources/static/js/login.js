document.addEventListener('DOMContentLoaded', () => {
    const auth = new Auth();
    
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            
            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password })
                });
                
                if (response.ok) {
                    const data = await response.json();
                    // 토큰 저장
                    auth.setToken(data.token);
                    // 토큰을 헤더에 포함시켜 채팅 페이지로 이동
                    navigateWithToken('/chat', auth.TOKEN_KEY);
                } else {
                    auth.showError('Login failed');
                }
            } catch (error) {
                console.error('Error:', error);
                auth.showError('Login failed');
            }
        });
    }
});

// 토큰을 헤더에 포함시켜 페이지 이동
function navigateWithToken(url, token_key) {
    const token = localStorage.getItem(token_key);
    if (!token) {
        window.location.href = '/login';
        return;
    }

    fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .catch(error => {
        console.error('Navigation error:', error);
        window.location.href = '/login';
    });
}