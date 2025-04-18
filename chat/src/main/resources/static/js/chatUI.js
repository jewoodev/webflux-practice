// chatUI.js - UI 이벤트 처리 관련
class ChatUI {
    constructor(config, webSocket) {
        this.config = config;
        this.webSocket = webSocket;
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        document.querySelector("#chat-send").addEventListener("click", () => {
            this.sendMessage();
        });

        document.querySelector("#chat-outgoing-msg").addEventListener("keydown", (event) => {
            if (event.keyCode === 13) {
                this.sendMessage();
            }
        });
    }

    sendMessage() {
        const msgInput = document.querySelector("#chat-outgoing-msg");
        const chat = {
            type: 'TALK',
            sender: this.config.user.username,
            roomNum: this.config.user.roomNum,
            msg: msgInput.value,
            lang: this.config.user.lang
        };

        this.webSocket.sendMessage(chat);
        msgInput.value = "";
    }
}
