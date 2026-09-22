// board-realtime-client.js — base STOMP para colaboración en tiempo real
// Mabel (MB) define el contrato; NP lo usa para ELEMENT_MOVED.

let stompClient = null;

export function connect(boardId, onEvent) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/boards/${boardId}`, (message) => {
            const event = JSON.parse(message.body);
            onEvent(event);
        });
    });
}

export function publish(boardId, destination, payload) {
    if (!stompClient || !stompClient.connected) return;
    stompClient.send(`/app/boards/${boardId}/${destination}`, {}, JSON.stringify(payload));
}

export function disconnect() {
    if (stompClient) stompClient.disconnect();
}
