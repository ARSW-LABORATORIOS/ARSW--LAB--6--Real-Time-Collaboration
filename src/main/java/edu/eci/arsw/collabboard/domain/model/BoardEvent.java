package edu.eci.arsw.collabboard.domain.model;

public record BoardEvent(
        BoardEventType type,
        String boardId,
        BoardElement element
) {}
