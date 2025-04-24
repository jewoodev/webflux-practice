// chatWebSocket.js - WebSocket 관련
class ChatWebSocket {
    constructor(config, messageHandler) {
        this.config = config;
        this.messageHandler = messageHandler;
        this.ws = null;
    }

    connect(token) {
        this.ws = new WebSocket(this.config.endpoints.ws(token));
        this.initializeEventHandlers();
    }

    initializeEventHandlers() {
        this.ws.onopen = () => {
            console.log('WebSocket 연결 성공');
            this.sendMessage({
                type: 'ENTER',
                sender: this.config.user.username,
                roomNum: this.config.user.roomNum,
                msg: `${this.config.user.username}님이 입장하셨습니다.`,
                lang: this.config.user.lang
            });
            this.messageHandler.fetchChatHistory(this.config.user.roomNum);
        };

        this.ws.onmessage = (event) => {
            const chat = JSON.parse(event.data);
            console.log("수신된 메세지:", chat);
            this.messageHandler.handleMessage(chat);
        };

        this.ws.onerror = (error) => {
            console.error('WebSocket 에러:', error);
            this.handleError();
        };

        this.ws.onclose = () => {
            console.log('WebSocket 연결 종료');
        };
    }

    sendMessage(chat) {
        if (this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(chat));
        } else {
            console.error('WebSocket is not connected');
        }
    }

    handleError() {
        this.sendMessage({
            type: 'LEAVE',
            sender: this.config.user.username,
            roomNum: this.config.user.roomNum,
            msg: `${this.config.user.username}님이 퇴장하셨습니다.`,
            lang: this.config.user.lang
        });
    }
}
