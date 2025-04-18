// 로그인을 대신하는 임시 방편
let username = prompt("아이디를 입력하세요.");
let roomNum = prompt("채팅방 번호를 입력하세요.");
let lang = prompt("사용하는 언어를 입력하세요.")

document.querySelector("#username").innerHTML = username;

const socket = new SockJS("/ws-connect");
const stompClient = Stomp.over(socket);

// 연결
stompClient.connect({}, function () {
    // 1. 먼저 이전 채팅 기록을 1회만 불러오기
    fetchChatHistory(roomNum).then(() => {
        // 2. 채팅방 실시간 메세지 구독 시작
        stompClient.subscribe(`/subscribe/chat.${roomNum}`, function (messageOutput) {
            const chat = JSON.parse(messageOutput.body);

            console.log(" 수신된 메세지: ", chat);
            initMessage(chat);
        });
    })
});

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

    const md = msg.createdAt.substring(5, 10);
    const tm = msg.createdAt.substring(11, 16);
    const convertTime = tm + "  |  " + md;

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

    const md = msg.createdAt.substring(5, 10);
    const tm = msg.createdAt.substring(11, 16);
    const convertTime = tm + "  |  " + md;

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
async function addMessage() {
    const msgInput = document.querySelector("#chat-outgoing-msg");

    const chat = {
        sender: username,
        roomNum: roomNum,
        msg: msgInput.value,
        lang: lang
    };

    stompClient.send(`/publish/chat.${roomNum}`, {}, JSON.stringify(chat));

    msgInput.value = "";
}

// 버튼 클릭 시 메세지 전송
document.querySelector("#chat-send").addEventListener("click", () => {
    addMessage();
})

// 엔터 타건 시 메세지 전송
document.querySelector("#chat-outgoing-msg").addEventListener("keydown", (event) => {
    if (event.keyCode === 13) {
        addMessage();
    }
})


// // SSE 연결하기
// const eventSource = new EventSource(`http://localhost:8080/chat/roomNum/${roomNum}`);
// eventSource.onmessage = (event) => {
//     const data = JSON.parse(event.data);
//     if (data.sender === username) {
//         initMyMessage(data);
//     } else {
//         initYourMessage(data);
//     }
// }
//
// // 파란 박스 만들기
// function getSendMsgBox(msg) {
//
//     const md = msg.createdAt.substring(5, 10);
//     const tm = msg.createdAt.substring(11, 16);
//     const convertTime = tm + "  |  " + md;
//
//     return `<div class="sent_msg">
//               <p>${msg.msg}</p>
//               <span class="time_date"> ${convertTime} / ${msg.sender} </span>
//             </div>`;
// }
//
// // 파란박스 초기화
// function initMyMessage(historyMsg) {
//     let chatBox = document.querySelector("#chat-box");
//
//     let sendBox = document.createElement("div");
//     sendBox.className = "outgoing_msg";
//
//     sendBox.innerHTML = getSendMsgBox(historyMsg);
//     chatBox.append(sendBox);
//
//     document.documentElement.scrollTop = document.body.scrollHeight;
// }
//
// // 회색 박스 만들기
// function getReceiveMsgBox(msg) {
//
//     const md = msg.createdAt.substring(5, 10);
//     const tm = msg.createdAt.substring(11, 16);
//     const convertTime = tm + "  |  " + md;
//
//     return `<div class="received_withd_msg">
//               <p>${msg.msg}</p>
//               <span class="time_date"> ${convertTime} / ${msg.sender} </span>
//             </div>`;
// }
//
// // 회색 박스 초기화
// function initYourMessage(historyMsg) {
//     let chatBox = document.querySelector("#chat-box");
//
//     let receivedBox = document.createElement("div");
//     receivedBox.className = "received_msg";
//
//     receivedBox.innerHTML = getReceiveMsgBox(historyMsg);
//     chatBox.append(receivedBox);
//
//     document.documentElement.scrollTop = document.body.scrollHeight;
// }
//
// // AJAX 채팅 메세지를 전송
// async function addMessage() {
//     let msgInput = document.querySelector("#chat-outgoing-msg");
//
//     let chat = {
//         sender: username,
//         roomNum: roomNum,
//         msg: msgInput.value
//     };
//
//     await fetch("http://localhost:8080/chat", {
//         method: "post",
//         body: JSON.stringify(chat),
//         headers: {
//             "Content-Type": "application/json; charset=utf-8"
//         }
//     });
//
//     msgInput.value = "";
// }
//
// // 버튼 클릭 시 메세지 전송
// document.querySelector("#chat-send").addEventListener("click", () => {
//     addMessage();
// });
//
// // 엔터 타건 시 메세지 전송
// document.querySelector("#chat-outgoing-msg").addEventListener("keydown", (event) => {
//     if (event.keyCode === 13) {
//         addMessage();
//     }
// });