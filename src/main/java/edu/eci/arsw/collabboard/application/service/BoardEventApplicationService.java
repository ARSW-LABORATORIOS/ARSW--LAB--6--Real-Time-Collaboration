package edu.eci.arsw.collabboard.application.service;

import edu.eci.arsw.collabboard.application.event.BoardEvent;
import edu.eci.arsw.collabboard.application.exception.BoardNotFoundException;
import edu.eci.arsw.collabboard.application.port.out.BoardRepository;
import edu.eci.arsw.collabboard.domain.model.Board;
import org.springframework.stereotype.Service;

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
        throw new UnsupportedOperationException("TODO LAB-06: ELEMENT_CREATED");
    }

    // TODO LAB-06 (Nicolas - feature/element-moved): ubicar el elemento por id
    // en board.elements(), validar que exista, actualizar x/y y guardar.
    private Board applyElementMoved(Board board, BoardEvent event) {
        throw new UnsupportedOperationException("TODO LAB-06: ELEMENT_MOVED");
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
        throw new UnsupportedOperationException("TODO LAB-06: ELEMENT_DELETED");
    }

    // TODO LAB-06 (Vera - feature/connector-deleted): validar que sourceId y
    // targetId existan en el Board y sean distintos, y agregar el CONNECTOR.
    private Board applyConnectorCreated(Board board, BoardEvent event) {
        throw new UnsupportedOperationException("TODO LAB-06: CONNECTOR_CREATED");
    }
}
