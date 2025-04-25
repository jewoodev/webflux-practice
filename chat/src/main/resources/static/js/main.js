// main.js - 애플리케이션 초기화
document.addEventListener('DOMContentLoaded', () => {
    let token;
    let username;

    auth = new Auth();
    token = auth.getToken();
    console.log("token = ", token);
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    auth.setToken(token);
    username = auth.getUsername();

    // 설정 초기화
    ChatConfig.user.init(username);

    // 메시지 핸들러 초기화
    const messageHandler = new ChatMessageHandler(ChatConfig);

    // WebSocket 초기화
    const webSocket = new ChatWebSocket(ChatConfig, messageHandler);
    webSocket.connect(token);

    // UI 초기화
    new ChatUI(ChatConfig, webSocket);
});
