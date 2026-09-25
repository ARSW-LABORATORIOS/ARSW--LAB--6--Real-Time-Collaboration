package edu.eci.arsw.collabboard.application.service;

import edu.eci.arsw.collabboard.application.event.BoardEvent;
import edu.eci.arsw.collabboard.application.event.BoardEventType;
import edu.eci.arsw.collabboard.domain.model.Board;
import edu.eci.arsw.collabboard.domain.model.BoardElement;
import edu.eci.arsw.collabboard.domain.model.ElementType;
import edu.eci.arsw.collabboard.infrastructure.persistence.InMemoryBoardRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BoardEventApplicationServiceTest {

    @Test
    void shouldApplyConnectorCreatedEvent() {
        InMemoryBoardRepository repository = new InMemoryBoardRepository();
        BoardEventApplicationService service =
                new BoardEventApplicationService(repository);

        BoardElement source = new BoardElement(
                "e1", ElementType.RECTANGLE, 10, 10, 100, 50, ""
        );

        BoardElement target = new BoardElement(
                "e2", ElementType.RECTANGLE, 200, 10, 100, 50, ""
        );

        repository.save(
                new Board("board-1", "Board", List.of(source, target))
        );

        BoardEvent event = new BoardEvent(
                "event-1",
                "board-1",
                BoardEventType.CONNECTOR_CREATED,
                "actor-1",
                Instant.now(),
                Map.of(
                        "id", "c1",
                        "sourceId", "e1",
                        "targetId", "e2"
                )
        );

        Board updated = service.apply("board-1", event);

        assertEquals(3, updated.elements().size());

        BoardElement connector = updated.elements().stream()
                .filter(element -> element.id().equals("c1"))
                .findFirst()
                .orElseThrow();

        assertEquals(ElementType.CONNECTOR, connector.type());
        assertEquals("e1", connector.sourceId());
        assertEquals("e2", connector.targetId());
    }

    @Test
    void shouldDeleteElementAndDependentConnectors() {
        InMemoryBoardRepository repository = new InMemoryBoardRepository();
        BoardEventApplicationService service =
                new BoardEventApplicationService(repository);

        BoardElement source = new BoardElement(
                "e1", ElementType.RECTANGLE, 10, 10, 100, 50, ""
        );

        BoardElement target = new BoardElement(
                "e2", ElementType.RECTANGLE, 200, 10, 100, 50, ""
        );

        BoardElement connector = new BoardElement(
                "c1",
                ElementType.CONNECTOR,
                0,
                0,
                0,
                0,
                "",
                "e1",
                "e2"
        );

        repository.save(
                new Board(
                        "board-1",
                        "Board",
                        List.of(source, target, connector)
                )
        );

        BoardEvent event = new BoardEvent(
                "event-2",
                "board-1",
                BoardEventType.ELEMENT_DELETED,
                "actor-1",
                Instant.now(),
                Map.of("elementId", "e1")
        );

        Board updated = service.apply("board-1", event);

        assertEquals(1, updated.elements().size());
        assertEquals("e2", updated.elements().getFirst().id());

        assertTrue(
                updated.elements().stream()
                        .noneMatch(element -> element.id().equals("c1"))
        );
    }

    @Test
    void shouldRejectEventWithoutBoardId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BoardEvent(
                        "event-3",
                        "",
                        BoardEventType.CONNECTOR_CREATED,
                        "actor-1",
                        Instant.now(),
                        Map.of()
                )
        );
    }
}