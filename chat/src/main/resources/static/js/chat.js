// 로그인을 대신하는 임시 방편
let username = prompt("아이디를 입력하세요.");
let roomNum = prompt("채팅방 번호를 입력하세요.");
let lang = prompt("사용하는 언어를 입력하세요.");

document.querySelector("#username").innerHTML = username;

// 웹소켓 연결 상태 관리 변수
let ws = null;
let reconnectInterval = null;
let lastMsgTimestamp = Date.now();
let pingInterval = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 10;
const RECONNECT_DELAY = 3000; // 3초 후 재연결
const PING_INTERVAL = 20000; // 20초마다 PING 전송

// 상태 표시 UI 요소 추가
const statusElement = document.createElement('div');
statusElement.id = 'connection-status';
statusElement.style.position = 'fixed';
statusElement.style.top = '10px';
statusElement.style.right = '10px';
statusElement.style.padding = '5px 10px';
statusElement.style.borderRadius = '5px';
statusElement.style.fontWeight = 'bold';
statusElement.style.zIndex = '1000';
document.body.appendChild(statusElement);

function updateConnectionStatus(status) {
    const statusEl = document.getElementById('connection-status');
    if (status === 'connected') {
        statusEl.textContent = '연결됨';
        statusEl.style.backgroundColor = '#4CAF50';
        statusEl.style.color = 'white';
    } else if (status === 'connecting') {
        statusEl.textContent = '연결 중...';
        statusEl.style.backgroundColor = '#FFC107';
        statusEl.style.color = 'black';
    } else if (status === 'disconnected') {
        statusEl.textContent = '연결 끊김';
        statusEl.style.backgroundColor = '#F44336';
        statusEl.style.color = 'white';
    }
}

// WebSocket 연결 함수
function connectWebSocket() {
    if (ws && ws.readyState === WebSocket.OPEN) {
        console.log('이미 WebSocket이 연결되어 있습니다.');
        return;
    }

    updateConnectionStatus('connecting');
    
    // HTTP인 경우 WebSocket도 ws://, HTTPS인 경우 WebSocket도 wss:// 사용
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    ws = new WebSocket(`${protocol}//${window.location.host}/ws/chat/${roomNum}`);

    ws.onopen = () => {
        console.log('WebSocket 연결 성공!');
        updateConnectionStatus('connected');
        reconnectAttempts = 0; // 재연결 시도 횟수 초기화
        
        // 연결 후 이전 채팅 내역 가져오기
        fetchChatHistory(roomNum);
        
        // 연결 유지를 위한 ping 전송 시작
        startPingInterval();
        
        // 재연결 인터벌 제거
        if (reconnectInterval) {
            clearInterval(reconnectInterval);
            reconnectInterval = null;
        }
    };

    ws.onmessage = (event) => {
        lastMsgTimestamp = Date.now(); // 메시지 수신 시간 갱신
        
        try {
            // ping 메시지는 무시
            if (event.data === 'ping' || event.data === '{"type":"PING"}') {
                console.log('Ping 메시지 수신');
                return;
            }
            
            const message = JSON.parse(event.data);
            console.log('메시지 수신:', message);
            
            // 연결 확인 메시지는 별도 처리
            if (message.type === 'CONNECTED') {
                console.log('서버와 연결 확인:', message);
                return;
            }
            
            // 메시지 처리 로직 추가
            initMessage(message);
        } catch (error) {
            console.error('메시지 처리 중 오류 발생:', error, event.data);
        }
    };

    ws.onclose = (event) => {
        console.log('WebSocket 연결 종료:', event.code, event.reason);
        updateConnectionStatus('disconnected');
        
        // ping 인터벌 종료
        if (pingInterval) {
            clearInterval(pingInterval);
            pingInterval = null;
        }
        
        // 의도적인 종료가 아니라면 재연결 시도
        if (event.code !== 1000) {
            attemptReconnect();
        }
    };

    ws.onerror = (error) => {
        console.error('WebSocket 오류:', error);
        updateConnectionStatus('disconnected');
    };
}

// 재연결 시도 함수
function attemptReconnect() {
    if (reconnectInterval) return; // 이미 재연결 시도 중이면 중복 방지
    
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
        console.error('최대 재연결 시도 횟수 초과. 수동 새로고침이 필요합니다.');
        alert('서버와의 연결이 끊겼습니다. 페이지를 새로고침해 주세요.');
        return;
    }
    
    reconnectAttempts++;
    console.log(`${reconnectAttempts}번째 재연결 시도... (${RECONNECT_DELAY/1000}초 후)`);
    
    reconnectInterval = setTimeout(() => {
        console.log('재연결 중...');
        connectWebSocket();
        reconnectInterval = null;
    }, RECONNECT_DELAY);
}

// Ping 메시지 전송 시작
function startPingInterval() {
    if (pingInterval) {
        clearInterval(pingInterval);
    }
    
    pingInterval = setInterval(() => {
        if (ws && ws.readyState === WebSocket.OPEN) {
            // 마지막 메시지 수신 후 30초 이상 지났으면 ping 전송
            const elapsed = Date.now() - lastMsgTimestamp;
            if (elapsed > 30000) {
                console.log('Ping 메시지 전송 (keepalive)');
                ws.send(JSON.stringify({ type: 'PING' }));
            }
        } else if (ws && ws.readyState === WebSocket.CLOSED) {
            // 소켓이 닫혔다면 재연결 시도
            attemptReconnect();
        }
    }, PING_INTERVAL);
}

// 페이지 가시성 변경 감지 (브라우저 탭 전환 등)
document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
        // 페이지가 다시 보이면 연결 확인
        if (!ws || ws.readyState !== WebSocket.OPEN) {
            console.log('페이지 포커스 복귀, 연결 확인');
            connectWebSocket();
        }
    }
});

// 네트워크 상태 변경 감지
window.addEventListener('online', () => {
    console.log('네트워크 연결 복구');
    if (!ws || ws.readyState !== WebSocket.OPEN) {
        connectWebSocket();
    }
});

window.addEventListener('offline', () => {
    console.log('네트워크 연결 끊김');
    updateConnectionStatus('disconnected');
});

async function fetchChatHistory(roomNum) {
    try {
        updateConnectionStatus('connected');
        const res = await fetch(`/chat/${roomNum}`);
        const messages = await res.json();

        console.log(`채팅방 ${roomNum}의 이전 메시지 ${messages.length}개를 가져왔습니다.`);
        messages.forEach(msg => {
            initMessage(msg);
        });
    } catch (error) {
        console.error('채팅 이력 가져오기 실패:', error);
        updateConnectionStatus('disconnected');
    }
}

function initMessage(chat) {
    // 시스템 메시지나 연결 관련 메시지는 다르게 처리
    if (chat.type === 'SYSTEM') {
        initSystemMessage(chat);
        return;
    }
    
    if (chat.sender === username) {
        initMyMessage(chat);
    } else {
        initYourMessage(chat);
    }
}

// 시스템 메시지 표시
function initSystemMessage(message) {
    let chatBox = document.querySelector("#chat-box");
    
    let systemBox = document.createElement("div");
    systemBox.className = "system_msg";
    systemBox.innerHTML = `<div class="system_msg_content">${message.msg}</div>`;
    
    chatBox.append(systemBox);
    scrollToBottom();
}

// 파란 박스 만들기
function getSendMsgBox(msg) {
    let convertTime = '';

    if (msg.createdAt) {
        const md = msg.createdAt.substring(5, 10);
        const tm = msg.createdAt.substring(11, 16);
        convertTime = tm + "  |  " + md;
    } else {
        convertTime = new Date().toLocaleTimeString() + " | " + new Date().toLocaleDateString();
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

    scrollToBottom();
}

// 회색 박스 만들기
function getReceiveMsgBox(msg) {
    let convertTime = '';

    if (msg.createdAt) {
        const md = msg.createdAt.substring(5, 10);
        const tm = msg.createdAt.substring(11, 16);
        convertTime = tm + "  |  " + md;
    } else {
        convertTime = new Date().toLocaleTimeString() + " | " + new Date().toLocaleDateString();
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

    scrollToBottom();
}

// 스크롤을 항상 아래로 유지
function scrollToBottom() {
    document.documentElement.scrollTop = document.body.scrollHeight;
}

// 채팅 메세지를 전송
function addMessage() {
    const msgInput = document.querySelector("#chat-outgoing-msg");
    const text = msgInput.value.trim();

    if (!text) return; // 빈 메시지는 전송하지 않음

    // 연결 상태 확인
    if (!ws || ws.readyState !== WebSocket.OPEN) {
        console.log('메시지 전송 전 연결 시도');
        connectWebSocket();
        alert('서버와 연결 중입니다. 잠시 후 다시 시도해 주세요.');
        return;
    }

    const chat = {
        sender: username,
        roomNum: parseInt(roomNum), // 숫자로 변환
        msg: text,
        lang: lang
    };

    try {
        ws.send(JSON.stringify(chat));
        console.log('메시지 전송:', chat);
        
        // 전송 후 lastMsgTimestamp 갱신
        lastMsgTimestamp = Date.now();
    } catch (error) {
        console.error('메시지 전송 실패:', error);
        alert('메시지 전송에 실패했습니다. 연결 상태를 확인해주세요.');
        updateConnectionStatus('disconnected');
        attemptReconnect();
    }

    msgInput.value = "";
    msgInput.focus();
}

// 버튼 클릭 시 메세지 전송
document.querySelector("#chat-send").addEventListener("click", () => {
    addMessage();
});

// 엔터 타건 시 메세지 전송
document.querySelector("#chat-outgoing-msg").addEventListener("keydown", (event) => {
    if (event.keyCode === 13) {
        event.preventDefault(); // 폼 전송 방지
        addMessage();
    }
});

// 페이지 로드 시 자동으로 input에 포커스 및 웹소켓 연결
window.onload = () => {
    document.querySelector("#chat-outgoing-msg").focus();
    connectWebSocket(); // 페이지 로드 시 WebSocket 연결
};

// 윈도우 종료 시 웹소켓 연결 종료
window.addEventListener('beforeunload', () => {
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.close(1000, "사용자 페이지 종료");
    }
});

// CSS 스타일 추가
const style = document.createElement('style');
style.textContent = `
.system_msg {
    text-align: center;
    margin: 10px 0;
}
.system_msg_content {
    display: inline-block;
    background-color: #f1f1f1;
    padding: 5px 15px;
    border-radius: 15px;
    color: #666;
    font-size: 0.9em;
}
#connection-status {
    transition: all 0.3s ease;
    box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}
`;
document.head.appendChild(style);