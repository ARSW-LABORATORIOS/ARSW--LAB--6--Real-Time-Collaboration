package edu.eci.arsw.collabboard.application.service;

import edu.eci.arsw.collabboard.application.exception.BoardNotFoundException;
import edu.eci.arsw.collabboard.application.port.out.BoardRepository;
import edu.eci.arsw.collabboard.domain.model.Board;
import edu.eci.arsw.collabboard.domain.model.BoardElement;
import edu.eci.arsw.collabboard.domain.model.ElementType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BoardApplicationService {

    private final BoardRepository repository;

    public BoardApplicationService(BoardRepository repository) {
        this.repository = repository;
    }

    public Board createBoard(String name) {
        Board board = new Board(UUID.randomUUID().toString(), name, List.of());
        return repository.save(board);
    }

    public Board getBoard(String boardId) {
        return repository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
    }

    public Board replaceBoard(String boardId, String name, List<BoardElement> elements) {
        if (!repository.existsById(boardId)) {
            throw new BoardNotFoundException(boardId);
        }

        List<BoardElement> safeElements = elements == null ? List.of() : elements;

        for (BoardElement element : safeElements) {
            if (element.type() == ElementType.CONNECTOR) {
                boolean sourceExists = safeElements.stream()
                        .anyMatch(e -> e.id().equals(element.sourceId()));

                boolean targetExists = safeElements.stream()
                        .anyMatch(e -> e.id().equals(element.targetId()));

                if (!sourceExists || !targetExists) {
                    throw new IllegalArgumentException(
                            "Connector sourceId and targetId must reference existing elements"
                    );
                }
            }
        }

        Board updated = new Board(boardId, name, safeElements);
        return repository.save(updated);
    }
}
