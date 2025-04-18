// chatConfig.js - 설정 관련
const ChatConfig = {
    // 사용자 정보 관리
    user: {
        username: null,
        roomNum: null,
        lang: null,

        init() {
            this.username = prompt("아이디를 입력하세요.");
            this.roomNum = prompt("채팅방 번호를 입력하세요.");
            this.lang = prompt("사용하는 언어를 입력하세요.");
            document.querySelector("#username").innerHTML = this.username;
        }
    },

    // API 엔드포인트
    endpoints: {
        ws: `ws://${window.location.host}/ws-connect`,
        chatHistory: (roomNum) => `/chat/${roomNum}`
    }
};