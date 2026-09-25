// Placeholder de Juan (CONNECTOR/backend). Mantener los nombres de función si se reescribe.

export function createInitialState() {
    return {
        board: null,
        selectedElementId: null,
        remote: { status: 'idle', message: '', lastOperation: null },
    };
}

function generateElementId() {
    return 'el-' + Math.random().toString(36).slice(2, 10);
}

export function setBoard(state, board) {
    state.board = board;
    state.selectedElementId = null;
}

export function findElement(state, elementId) {
    if (!state.board) return null;
    return state.board.elements.find((el) => el.id === elementId) || null;
}

export function addElement(state, partial) {
    const element = {
        id: partial.id ?? generateElementId(),
        type: partial.type,
        x: partial.x ?? 40,
        y: partial.y ?? 40,
        width: partial.width ?? 120,
        height: partial.height ?? 60,
        text: partial.text ?? '',
        sourceId: partial.sourceId ?? null,
        targetId: partial.targetId ?? null,
    };
    state.board.elements.push(element);
    return element;
}

export function addConnector(state, sourceId, targetId, id = null) {
    if (!sourceId || !targetId || sourceId === targetId) {
        throw new Error('CONNECTOR requiere dos elementos distintos');
    }

    return addElement(state, {
        id,
        type: 'CONNECTOR',
        sourceId,
        targetId
    });
}

export function selectElement(state, elementId) {
    state.selectedElementId = elementId;
}

export function moveElement(state, elementId, x, y) {
    const element = findElement(state, elementId);
    if (element) {
        element.x = x;
        element.y = y;
    }
}

export function deleteElement(state, elementId) {
    if (!state.board) return;

    state.board.elements = state.board.elements.filter(
        (el) =>
            el.id !== elementId &&
            el.sourceId !== elementId &&
            el.targetId !== elementId
    );

    if (state.selectedElementId === elementId) {
        state.selectedElementId = null;
    }
}

export function setRemoteStatus(state, status, message, lastOperation) {
    state.remote = {
        status,
        message: message ?? '',
        lastOperation: lastOperation ?? state.remote.lastOperation,
    };
}
