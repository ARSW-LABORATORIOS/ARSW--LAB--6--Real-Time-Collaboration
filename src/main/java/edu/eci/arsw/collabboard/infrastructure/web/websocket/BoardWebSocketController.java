package edu.eci.arsw.collabboard.infrastructure.web.websocket;

import edu.eci.arsw.collabboard.application.event.BoardEvent;
import edu.eci.arsw.collabboard.application.service.BoardEventApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Client SEND /app/boards/{boardId}/events
 *   -> validate + apply (BoardEventApplicationService)
 *   -> Server BROADCAST /topic/boards/{boardId}   (only if accepted)
 *
 * No domain/protocol mixing here: this class only orchestrates, it never
 * touches Board/BoardElement directly.
 */
@Controller
public class BoardWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(BoardWebSocketController.class);

    private final BoardEventApplicationService eventService;
    private final SimpMessagingTemplate messagingTemplate;

    public BoardWebSocketController(BoardEventApplicationService eventService,
                                     SimpMessagingTemplate messagingTemplate) {
        this.eventService = eventService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/boards/{boardId}/events")
    public void handle(@DestinationVariable String boardId, BoardEvent event) {
        if (!boardId.equals(event.boardId())) {
            log.warn("Evento rechazado: boardId de la ruta ({}) no coincide con el del evento ({})",
                    boardId, event.boardId());
            return;
        }

        try {
            eventService.apply(boardId, event);
        } catch (RuntimeException ex) {
            log.warn("Evento rechazado para board {}: {}", boardId, ex.getMessage());
            return;
        }

        messagingTemplate.convertAndSend("/topic/boards/" + boardId, event);
    }
}
