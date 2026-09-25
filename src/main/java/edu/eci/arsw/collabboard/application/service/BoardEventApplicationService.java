package edu.eci.arsw.collabboard.application.service;

import edu.eci.arsw.collabboard.application.event.BoardEvent;
import edu.eci.arsw.collabboard.application.exception.BoardNotFoundException;
import edu.eci.arsw.collabboard.application.port.out.BoardRepository;
import edu.eci.arsw.collabboard.domain.model.Board;
import org.springframework.stereotype.Service;
import edu.eci.arsw.collabboard.domain.model.BoardElement;
import edu.eci.arsw.collabboard.domain.model.ElementType;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates and applies a BoardEvent against the authoritative Board state.
 * BoardWebSocketController only broadcasts to /topic/boards/{boardId} when
 * apply(...) returns normally; any exception here means the event is
 * rejected and never reaches other clients.
 */
@Service
public class BoardEventApplicationService {

    private final BoardRepository repository;

    public BoardEventApplicationService(BoardRepository repository) {
        this.repository = repository;
    }

    public Board apply(String boardId, BoardEvent event) {
        Board board = repository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        return switch (event.type()) {
            case ELEMENT_CREATED -> applyElementCreated(board, event);
            case ELEMENT_MOVED -> applyElementMoved(board, event);
            case ELEMENT_UPDATED -> applyElementUpdated(board, event);
            case ELEMENT_DELETED -> applyElementDeleted(board, event);
            case CONNECTOR_CREATED -> applyConnectorCreated(board, event);
        };
    }

    // TODO LAB-06 (Mabel - feature/element-created): construir el BoardElement
    // (RECTANGLE o TEXT) a partir de event.payload(), agregarlo a los elementos
    // del Board y guardar con repository.save(...). Devolver el Board actualizado.
    private Board applyElementCreated(Board board, BoardEvent event) {
        String id = (String) event.payload().get("id");
        String typeStr = (String) event.payload().get("type");
        double x = ((Number) event.payload().get("x")).doubleValue();
        double y = ((Number) event.payload().get("y")).doubleValue();
        double width = ((Number) event.payload().get("width")).doubleValue();
        double height = ((Number) event.payload().get("height")).doubleValue();
        String text = event.payload().get("text") != null ? (String) event.payload().get("text") : "";

        BoardElement element = new BoardElement(id, ElementType.valueOf(typeStr), x, y, width, height, text);

        List<BoardElement> updated = new ArrayList<>(board.elements());
        updated.add(element);

        return repository.save(new Board(board.id(), board.name(), updated));
    }

    // TODO LAB-06 (Nicolas - feature/element-moved): ubicar el elemento por id
    // en board.elements(), validar que exista, actualizar x/y y guardar.
    private Board applyElementMoved(Board board, BoardEvent event) {
        String elementId = (String) event.payload().get("elementId");
        double x = ((Number) event.payload().get("x")).doubleValue();
        double y = ((Number) event.payload().get("y")).doubleValue();

        boolean exists = board.elements().stream().anyMatch(e -> e.id().equals(elementId));
        if (!exists) throw new IllegalArgumentException("Element not found: " + elementId);

        var updatedElements = board.elements().stream()
                .map(e -> e.id().equals(elementId)
                        ? new edu.eci.arsw.collabboard.domain.model.BoardElement(
                                e.id(), e.type(), x, y, e.width(), e.height(), e.text(), e.sourceId(), e.targetId())
                        : e)
                .toList();

        Board updated = new Board(board.id(), board.name(), updatedElements);
        return repository.save(updated);
    }

    // ELEMENT_UPDATED no esta en el alcance funcional obligatorio de este lab
    // (ver seccion 2 del enunciado). Se deja el case para que el switch sea
    // exhaustivo sobre BoardEventType; no hace falta implementarlo.
    private Board applyElementUpdated(Board board, BoardEvent event) {
        throw new UnsupportedOperationException("ELEMENT_UPDATED no esta en el alcance de este lab");
    }

    // TODO LAB-06 (Vera - feature/connector-deleted): eliminar el elemento por
    // id y, en cascada, cualquier CONNECTOR cuyo sourceId/targetId lo referencie.
    private Board applyElementDeleted(Board board, BoardEvent event) {
        String elementId = (String) event.payload().get("elementId");

        if (elementId == null || elementId.isBlank()) {
            throw new IllegalArgumentException("elementId is required");
        }

        boolean exists = board.elements().stream()
                .anyMatch(element -> element.id().equals(elementId));

        if (!exists) {
            throw new IllegalArgumentException("Element not found: " + elementId);
        }

        var updatedElements = board.elements().stream()
                .filter(element -> !element.id().equals(elementId))
                .filter(element -> !elementId.equals(element.sourceId()))
                .filter(element -> !elementId.equals(element.targetId()))
                .toList();

        Board updated = new Board(board.id(), board.name(), updatedElements);

        return repository.save(updated);
    }

    // TODO LAB-06 (Vera - feature/connector-deleted): validar que sourceId y
    // targetId existan en el Board y sean distintos, y agregar el CONNECTOR.
    private Board applyConnectorCreated(Board board, BoardEvent event) {
        String connectorId = (String) event.payload().get("id");
        String sourceId = (String) event.payload().get("sourceId");
        String targetId = (String) event.payload().get("targetId");

        if (connectorId == null || connectorId.isBlank()) {
            throw new IllegalArgumentException("Connector id is required");
        }

        if (sourceId == null || targetId == null) {
            throw new IllegalArgumentException("sourceId and targetId are required");
        }

        if (sourceId.equals(targetId)) {
            throw new IllegalArgumentException("sourceId and targetId must be different");
        }

        boolean sourceExists = board.elements().stream()
                .anyMatch(element -> element.id().equals(sourceId));

        boolean targetExists = board.elements().stream()
                .anyMatch(element -> element.id().equals(targetId));

        if (!sourceExists || !targetExists) {
            throw new IllegalArgumentException("Connector endpoints must exist");
        }

        BoardElement connector = new BoardElement(
                connectorId,
                ElementType.CONNECTOR,
                0,
                0,
                0,
                0,
                "",
                sourceId,
                targetId
        );

        var updatedElements = new ArrayList<>(board.elements());
        updatedElements.add(connector);

        Board updated = new Board(board.id(), board.name(), updatedElements);

        return repository.save(updated);
    }
}
