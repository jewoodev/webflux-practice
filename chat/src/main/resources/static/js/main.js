// main.js - 애플리케이션 초기화
document.addEventListener('DOMContentLoaded', () => {
    // 설정 초기화
    ChatConfig.user.init();

    // 메시지 핸들러 초기화
    const messageHandler = new ChatMessageHandler(ChatConfig);

    // WebSocket 초기화
    const webSocket = new ChatWebSocket(ChatConfig, messageHandler);
    webSocket.connect();

    // UI 초기화
    new ChatUI(ChatConfig, webSocket);
});
