// chatConfig.js - 설정 관련
const ChatConfig = {
    // 사용자 정보 관리
    user: {
        username: null,
        roomNum: null,
        lang: null,

        init(username) {
            this.username = username;
            this.roomNum = prompt("채팅방 번호를 입력하세요.");
            this.lang = prompt("사용하는 언어를 입력하세요.");
            document.querySelector("#username").innerHTML = this.username;
        }
    },

    // API 엔드포인트
    endpoints: {
        ws: (token) => {
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            return `${protocol}//${window.location.host}/ws-connect?token=${token}`;
        },
        chatHistory: (roomNum) => `/chat/${roomNum}`
    }
};