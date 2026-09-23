const SVG_NS = 'http://www.w3.org/2000/svg';

export function renderBoard(svgEl, board, selectedElementId) {
    svgEl.innerHTML = '';
    if (!board) return;

    const connectors = board.elements.filter((el) => el.type === 'CONNECTOR');
    const shapes = board.elements.filter((el) => el.type !== 'CONNECTOR');

    for (const connector of connectors) {
        renderConnector(svgEl, connector, board.elements);
    }
    for (const element of shapes) {
        renderShape(svgEl, element, element.id === selectedElementId);
    }
}

function renderShape(svgEl, element, isSelected) {
    if (element.type === 'RECTANGLE') {
        renderRectangle(svgEl, element, isSelected);
    } else if (element.type === 'TEXT') {
        renderText(svgEl, element, isSelected);
    }
}

function renderRectangle(svgEl, element, isSelected) {
    const rect = document.createElementNS(SVG_NS, 'rect');
    rect.setAttribute('x', element.x);
    rect.setAttribute('y', element.y);
    rect.setAttribute('width', element.width);
    rect.setAttribute('height', element.height);
    rect.setAttribute('class', 'board-element rectangle' + (isSelected ? ' selected' : ''));
    rect.dataset.elementId = element.id;
    svgEl.appendChild(rect);
}

function renderText(svgEl, element, isSelected) {
    const width = element.width || 120;
    const height = element.height || 30;

    const group = document.createElementNS(SVG_NS, 'g');
    group.setAttribute('class', 'board-element text' + (isSelected ? ' selected' : ''));
    group.dataset.elementId = element.id;

    const background = document.createElementNS(SVG_NS, 'rect');
    background.setAttribute('x', element.x);
    background.setAttribute('y', element.y);
    background.setAttribute('width', width);
    background.setAttribute('height', height);
    background.setAttribute('class', 'text-background');
    group.appendChild(background);

    const label = document.createElementNS(SVG_NS, 'text');
    label.setAttribute('x', element.x + 8);
    label.setAttribute('y', element.y + height / 2 + 5);
    label.setAttribute('class', 'text-content');
    label.textContent = element.text;
    group.appendChild(label);

    svgEl.appendChild(group);
}

function elementCenter(element) {
    return {
        x: element.x + (element.width || 0) / 2,
        y: element.y + (element.height || 0) / 2,
    };
}

function renderConnector(svgEl, connector, allElements) {
    const source = allElements.find((el) => el.id === connector.sourceId);
    const target = allElements.find((el) => el.id === connector.targetId);
    if (!source || !target) return;

    const from = elementCenter(source);
    const to = elementCenter(target);

    const line = document.createElementNS(SVG_NS, 'line');
    line.setAttribute('x1', from.x);
    line.setAttribute('y1', from.y);
    line.setAttribute('x2', to.x);
    line.setAttribute('y2', to.y);
    line.setAttribute('class', 'connector');
    svgEl.appendChild(line);
}

function toSvgPoint(svgEl, event) {
    const rect = svgEl.getBoundingClientRect();
    return { x: event.clientX - rect.left, y: event.clientY - rect.top };
}

// handlers: { getElement(id), onSelect(id), onMove(id, x, y), onMoveEnd(id, x, y), onDeleteRequest(id) }
export function attachInteractionHandlers(svgEl, handlers) {
    let drag = null;

    svgEl.setAttribute('tabindex', '0');

    svgEl.addEventListener('mousedown', (event) => {
        const target = event.target.closest('[data-element-id]');
        if (!target) return;

        const elementId = target.dataset.elementId;
        handlers.onSelect(elementId);
        svgEl.focus();

        const element = handlers.getElement(elementId);
        if (!element) return;

        const startPoint = toSvgPoint(svgEl, event);
        drag = {
            elementId,
            startPointX: startPoint.x,
            startPointY: startPoint.y,
            originX: element.x,
            originY: element.y,
        };
    });

    svgEl.addEventListener('mousemove', (event) => {
        if (!drag) return;
        const point = toSvgPoint(svgEl, event);
        const newX = drag.originX + (point.x - drag.startPointX);
        const newY = drag.originY + (point.y - drag.startPointY);
        handlers.onMove(drag.elementId, newX, newY);
    });

    window.addEventListener('mouseup', (event) => {
        if (drag) {
            const point = toSvgPoint(svgEl, event);
            const finalX = drag.originX + (point.x - drag.startPointX);
            const finalY = drag.originY + (point.y - drag.startPointY);
            if (handlers.onMoveEnd) handlers.onMoveEnd(drag.elementId, finalX, finalY);
        }
        drag = null;
    });

    svgEl.addEventListener('keydown', (event) => {
        if (event.key !== 'Delete' && event.key !== 'Backspace') return;
        if (!handlers.selectedElementId()) return;
        handlers.onDeleteRequest(handlers.selectedElementId());
    });
}
