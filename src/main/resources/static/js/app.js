import * as api from './api/board-api-client.js';
import * as boardState from './state/board-state.js';
import { renderBoard, attachInteractionHandlers } from './ui/board-view.js';
import * as realtime from './api/board-realtime-client.js';

const state = boardState.createInitialState();
let pendingConnectorSourceId = null;

const svgEl = document.getElementById('board-svg');
const statusEl = document.getElementById('status-bar');
const hintEl = document.getElementById('hint-bar');
const boardIdEl = document.getElementById('board-id-display');
const retryBtn = document.getElementById('retry-btn');
const boardNameInput = document.getElementById('board-name-input');
const boardIdInput = document.getElementById('board-id-input');
const textContentInput = document.getElementById('text-content-input');

function rerender() {
    renderBoard(svgEl, state.board, state.selectedElementId);
    renderStatus();
}

function showHint(message) {
    hintEl.textContent = message;
}

function renderStatus() {
    statusEl.textContent = state.remote.status === 'idle' ? '' : state.remote.message;
    statusEl.className = 'status status-' + state.remote.status;
    retryBtn.disabled = state.remote.status !== 'error';
    boardIdEl.textContent = state.board ? `Board: ${state.board.id}` : 'Sin board cargado';
}

async function runRemoteOperation(operationName, action) {
    boardState.setRemoteStatus(state, 'loading', 'Cargando...', operationName);
    rerender();
    try {
        const result = await action();
        boardState.setRemoteStatus(state, 'success', 'Operación exitosa', operationName);
        return result;
    } catch (error) {
        const message = error instanceof api.ApiClientError
            ? `${error.code ?? 'ERROR'}: ${error.message}`
            : 'Error inesperado de red';
        boardState.setRemoteStatus(state, 'error', message, operationName);
        return null;
    } finally {
        rerender();
    }
}

async function handleNewBoard() {
    const name = boardNameInput.value.trim();
    if (!name) {
        showHint('Escribe un nombre para el nuevo board');
        return;
    }
    const board = await runRemoteOperation('createBoard', () => api.createBoard(name));
    if (board) {
        boardState.setBoard(state, board);
        realtime.connect(board.id, handleRealtimeEvent);
        showHint('');
        boardNameInput.value = '';
    }
    rerender();
}

async function handleLoadBoard() {
    const boardId = boardIdInput.value.trim();
    if (!boardId) {
        showHint('Escribe el boardId a cargar');
        return;
    }
    const board = await runRemoteOperation('loadBoard', () => api.loadBoard(boardId));
    if (board) {
        boardState.setBoard(state, board);
        realtime.connect(board.id, handleRealtimeEvent);
        showHint('');
    }
    rerender();
}

async function handleSaveBoard() {
    if (!state.board) return;
    const saved = await runRemoteOperation('saveBoard', () => api.saveBoard(state.board.id, state.board));
    if (saved) boardState.setBoard(state, saved);
    rerender();
}

function handleRetry() {
    const lastOperation = state.remote.lastOperation;
    if (lastOperation === 'saveBoard') handleSaveBoard();
    else if (lastOperation === 'loadBoard') handleLoadBoard();
    else if (lastOperation === 'createBoard') handleNewBoard();
}

function requireBoard() {
    if (!state.board) {
        showHint('Primero crea o carga un board');
        return false;
    }
    return true;
}

function handleAddRectangle() {
    if (!requireBoard()) return;
    boardState.addElement(state, { type: 'RECTANGLE', x: 60, y: 60, width: 140, height: 80 });
    showHint('');
    rerender();
}

function handleAddText() {
    if (!requireBoard()) return;
    const text = textContentInput.value;
    boardState.addElement(state, { type: 'TEXT', x: 60, y: 60, width: 140, height: 30, text });
    textContentInput.value = '';
    showHint('');
    rerender();
}

function handleConnect() {
    if (!requireBoard()) return;
    if (!state.selectedElementId) {
        showHint('Selecciona primero el elemento origen');
        return;
    }
    pendingConnectorSourceId = state.selectedElementId;
    showHint('Modo conectar: haz click en el elemento destino');
}

function handleDeleteSelected() {
    if (!state.selectedElementId) return;
    boardState.deleteElement(state, state.selectedElementId);
    rerender();
}

function handleSelect(elementId) {
    if (pendingConnectorSourceId && pendingConnectorSourceId !== elementId) {
        try {
            boardState.addConnector(state, pendingConnectorSourceId, elementId);
            showHint('');
        } catch (error) {
            showHint(error.message);
        }
        pendingConnectorSourceId = null;
    }
    boardState.selectElement(state, elementId);
    rerender();
}

function handleRealtimeEvent(event) {
    if (!state.board || event.boardId !== state.board.id) return;
    if (event.type === 'ELEMENT_MOVED') {
        const el = boardState.findElement(state, event.element.id);
        if (!el) return;
        boardState.moveElement(state, event.element.id, event.element.x, event.element.y);
        rerender();
    }
}

function handleMove(elementId, x, y) {
    boardState.moveElement(state, elementId, x, y);
    rerender();
}

function handleMoveEnd(elementId, x, y) {
    if (!state.board) return;
    realtime.publish(state.board.id, 'move', {
        type: 'ELEMENT_MOVED',
        boardId: state.board.id,
        element: { id: elementId, x, y },
    });
}

attachInteractionHandlers(svgEl, {
    getElement: (id) => boardState.findElement(state, id),
    selectedElementId: () => state.selectedElementId,
    onSelect: handleSelect,
    onMove: handleMove,
    onMoveEnd: handleMoveEnd,
    onDeleteRequest: handleDeleteSelected,
});

document.getElementById('new-board-btn').addEventListener('click', handleNewBoard);
document.getElementById('load-board-btn').addEventListener('click', handleLoadBoard);
document.getElementById('add-rectangle-btn').addEventListener('click', handleAddRectangle);
document.getElementById('add-text-btn').addEventListener('click', handleAddText);
document.getElementById('connect-btn').addEventListener('click', handleConnect);
document.getElementById('delete-btn').addEventListener('click', handleDeleteSelected);
document.getElementById('save-btn').addEventListener('click', handleSaveBoard);
retryBtn.addEventListener('click', handleRetry);

rerender();
