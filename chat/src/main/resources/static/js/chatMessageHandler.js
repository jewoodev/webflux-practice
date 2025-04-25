// chatMessageHandler.js - 메시지 처리 관련
class ChatMessageHandler {
    constructor(config) {
        this.config = config;
    }

    async fetchChatHistory(roomNum) {
        const res = await fetch(this.config.endpoints.chatHistory(roomNum), {
            headers: {
                'Authorization': `Bearer ${auth.getToken()}`
            }
        });
        const messages = await res.json();
        console.log("messages are ", message);
        messages.forEach(message => this.handleMessage(message));
    }

    handleMessage(chat) {
        if (chat.type === 'ENTER' || chat.type === 'LEAVE') {
            this.renderSystemMessage(chat);
        } else if (chat.sender === this.config.user.username) {
            this.renderMyMessage(chat);
        } else {
            this.renderOtherMessage(chat);
        }
    }

    formatTime(createdAt) {
        if (!createdAt) return new Date().toLocaleString();
        const md = createdAt.substring(5, 10);
        const tm = createdAt.substring(11, 16);
        return `${tm}  |  ${md}`;
    }

    renderSystemMessage(chat) {
        const template = `
            <div class="system_msg_content">
                <p>${chat.message}</p>
                <span class="time_date">${this.formatTime(chat.createdAt)}</span>
            </div>
        `;
        this.appendMessage('system_msg', template);
    }

    renderMyMessage(chat) {
        const template = `
            <div class="sent_msg">
                <p>${chat.message}</p>
                <span class="time_date">${this.formatTime(chat.createdAt)} / ${chat.sender}</span>
            </div>
        `;
        this.appendMessage('outgoing_msg', template);
    }

    renderOtherMessage(chat) {
        const template = `
            <div class="received_withd_msg">
                <p>${chat.message}</p>
                <span class="time_date">${this.formatTime(chat.createdAt)} / ${chat.sender}</span>
            </div>
        `;
        this.appendMessage('received_msg', template);
    }

    appendMessage(className, template) {
        const chatBox = document.querySelector("#chat-box");
        const messageDiv = document.createElement("div");
        messageDiv.className = className;
        messageDiv.innerHTML = template;
        chatBox.append(messageDiv);
        document.documentElement.scrollTop = document.body.scrollHeight;
    }
}
