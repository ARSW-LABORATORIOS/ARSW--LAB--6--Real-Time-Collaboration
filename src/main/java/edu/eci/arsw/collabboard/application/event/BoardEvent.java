package edu.eci.arsw.collabboard.application.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Communication envelope between collaborators (docs/event-contract.md).
 *
 * This is NOT a domain entity: Board and BoardElement keep representing the
 * state of the problem. BoardEvent only represents "something happened and
 * needs to travel between participants".
 */
public record BoardEvent(
        String eventId,
        String boardId,
        BoardEventType type,
        String actorId,
        Instant occurredAt,
        Map<String, Object> payload
) {
    public BoardEvent {
        if (boardId == null || boardId.isBlank()) {
            throw new IllegalArgumentException("boardId is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
        eventId = (eventId == null || eventId.isBlank()) ? UUID.randomUUID().toString() : eventId;
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
