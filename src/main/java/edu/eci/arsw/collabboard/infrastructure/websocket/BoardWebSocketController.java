package edu.eci.arsw.collabboard.infrastructure.websocket;

import edu.eci.arsw.collabboard.application.service.BoardApplicationService;
import edu.eci.arsw.collabboard.domain.model.BoardEvent;
import edu.eci.arsw.collabboard.domain.model.BoardEventType;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BoardWebSocketController {

    private final BoardApplicationService service;
    private final SimpMessagingTemplate broker;

    public BoardWebSocketController(BoardApplicationService service, SimpMessagingTemplate broker) {
        this.service = service;
        this.broker = broker;
    }

    @MessageMapping("/boards/{boardId}/move")
    public void handleElementMoved(@DestinationVariable String boardId, BoardEvent event) {
        var board = service.getBoard(boardId);
        boolean exists = board.elements().stream()
                .anyMatch(e -> e.id().equals(event.element().id()));
        if (!exists) return;

        var moved = service.moveElement(boardId, event.element().id(), event.element().x(), event.element().y());
        broker.convertAndSend("/topic/boards/" + boardId, new BoardEvent(BoardEventType.ELEMENT_MOVED, boardId, moved));
    }
}
