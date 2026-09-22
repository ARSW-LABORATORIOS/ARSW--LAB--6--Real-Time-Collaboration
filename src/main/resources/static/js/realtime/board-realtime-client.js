/**
 * BoardRealtimeClient — unico punto de acceso STOMP/WebSocket del cliente.
 * Ningun otro modulo debe conocer topics/destinos ni tocar el objeto stomp
 * directamente. app.js orquesta llamando estas funciones; BoardView no debe
 * saber que esto existe.
 *
 * Requiere SockJS y Stomp cargados como globales (ver script tags en
 * index.html) antes de que este modulo se ejecute.
 *
 * TODO LAB-06: cada integrante llena el publish de su(s) evento(s) y agrega
 * el manejo correspondiente del lado de quien recibe (eso vive en
 * board-state.js, no aqui).
 */

let stompClient = null;
let currentBoardId = null;

const actorId = 'actor-' + Math.random().toString(36).slice(2, 10);

export function connect(boardId, onEvent) {
    return new Promise((resolve, reject) => {
        currentBoardId = boardId;

        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect(
            {},
            () => {
                stompClient.subscribe(`/topic/boards/${boardId}`, (message) => {
                    const event = JSON.parse(message.body);
                    onEvent(event);
                });
                resolve();
            },
            (error) => reject(error)
        );
    });
}

export function disconnect() {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect();
    }
    stompClient = null;
    currentBoardId = null;
}

function publish(type, payload) {
    if (!stompClient || !stompClient.connected || !currentBoardId) {
        return;
    }
    const event = {
        boardId: currentBoardId,
        type,
        actorId,
        occurredAt: new Date().toISOString(),
        payload,
    };
    stompClient.send(`/app/boards/${currentBoardId}/events`, {}, JSON.stringify(event));
}

// TODO LAB-06 (Mabel - feature/element-created): payload minimo para que el
// receptor reconstruya el elemento (id, type, x, y, width, height, text).
export function publishElementCreated(element) {
    publish('ELEMENT_CREATED', element);
}

// TODO LAB-06 (Nicolas - feature/element-moved): publicar al soltar el drag,
// no en cada pixel.
export function publishElementMoved(elementId, x, y) {
    publish('ELEMENT_MOVED', { elementId, x, y });
}

// TODO LAB-06 (Vera - feature/connector-deleted)
export function publishConnectorCreated(connector) {
    publish('CONNECTOR_CREATED', connector);
}

// TODO LAB-06 (Vera - feature/connector-deleted)
export function publishElementDeleted(elementId) {
    publish('ELEMENT_DELETED', { elementId });
}

export { actorId };
