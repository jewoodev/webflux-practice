// 로그인을 대신하는 임시 방편
let username = prompt("아이디를 입력하세요.");
let roomNum = prompt("채팅방 번호를 입력하세요.");
let lang = prompt("사용하는 언어를 입력하세요.")

document.querySelector("#username").innerHTML = username;

// WebSocket 연결
const ws = new WebSocket(`ws://${window.location.host}/ws-connect`);

// WebSocket 이벤트 핸들러
ws.onopen = () => {
    console.log('WebSocket 연결 성공');
    // 연결 성공 후 채팅 기록 불러오기
    fetchChatHistory(roomNum);
};

ws.onmessage = (event) => {
    const chat = JSON.parse(event.data);
    console.log("수신된 메세지:", chat);
    initMessage(chat);
};

ws.onerror = (error) => {
    console.error('WebSocket 에러:', error);
};

ws.onclose = () => {
    console.log('WebSocket 연결 종료');
};

async function fetchChatHistory(roomNum) {
    const res = await fetch(`/chat/${roomNum}`);
    const messages = await res.json();

    messages.forEach(msg => {
        initMessage(msg);
    });
}

function initMessage(chat) {
    if (chat.sender === username) {
        initMyMessage(chat);
    } else {
        initYourMessage(chat)
    }
}

// 파란 박스 만들기
function getSendMsgBox(msg) {
    let convertTime = "";

    if (msg.createdAt) {
        const md = msg.createdAt.substring(5, 10);
        const tm = msg.createdAt.substring(11, 16);
        convertTime = tm + "  |  " + md;
    } else {
        console.log("createdAt 파싱 실패");
        convertTime = new Date().toLocaleString(); // 현재 시간을 대신 표시
    }


    return `<div class="sent_msg">
              <p>${msg.msg}</p>
              <span class="time_date"> ${convertTime} / ${msg.sender} </span>
            </div>`;
}

// 파란박스 초기화
function initMyMessage(historyMsg) {
    let chatBox = document.querySelector("#chat-box");

    let sendBox = document.createElement("div");
    sendBox.className = "outgoing_msg";

    sendBox.innerHTML = getSendMsgBox(historyMsg);
    chatBox.append(sendBox);

    document.documentElement.scrollTop = document.body.scrollHeight;
}

// 회색 박스 만들기
function getReceiveMsgBox(msg) {
    let convertTime = "";

    if (msg.createdAt) {
        const md = msg.createdAt.substring(5, 10);
        const tm = msg.createdAt.substring(11, 16);
        convertTime = tm + "  |  " + md;
    } else {
        console.log("createdAt 파싱 실패");
        convertTime = new Date().toLocaleString(); // 현재 시간을 대신 표시
    }


    return `<div class="received_withd_msg">
              <p>${msg.msg}</p>
              <span class="time_date"> ${convertTime} / ${msg.sender} </span>
            </div>`;
}

// 회색 박스 초기화
function initYourMessage(historyMsg) {
    let chatBox = document.querySelector("#chat-box");

    let receivedBox = document.createElement("div");
    receivedBox.className = "received_msg";

    receivedBox.innerHTML = getReceiveMsgBox(historyMsg);
    chatBox.append(receivedBox);

    document.documentElement.scrollTop = document.body.scrollHeight;
}

// 채팅 메세지를 전송
function addMessage() {
    const msgInput = document.querySelector("#chat-outgoing-msg");

    const chat = {
        sender: username,
        roomNum: roomNum,
        msg: msgInput.value,
        lang: lang
    };

    // WebSocket을 통해 메시지 전송
    ws.send(JSON.stringify(chat));
    msgInput.value = "";
}

// 버튼 클릭 시 메세지 전송
document.querySelector("#chat-send").addEventListener("click", () => {
    addMessage();
});

// 엔터 타건 시 메세지 전송
document.querySelector("#chat-outgoing-msg").addEventListener("keydown", (event) => {
    if (event.keyCode === 13) {
        addMessage();
    }
});