# Architecture Evidence — Lab 06

## 1. ArchiMate Application View

![ArchiMate Application View](archimate.drawio.png)

> Actualizar el diagrama para incluir los nuevos componentes del Lab 6:
> - **Web Client** — `app.js` orquesta REST y STOMP
> - **REST Interface** — `BoardRestController` → `BoardApplicationService` → `BoardRepository`
> - **WebSocket/STOMP Interface** — `BoardWebSocketController` → `BoardEventApplicationService` → `BoardRepository`
> - **Application Services** — `BoardApplicationService` (CRUD) y `BoardEventApplicationService` (eventos en tiempo real)
> - **Repository** — `InMemoryBoardRepository` implementa `BoardRepository`

## 2. Diagrama de clases — Lab 6

![Class Diagram Lab 5](classdiagramLAB5.png)

> Clases relevantes agregadas en el Lab 6:
>
> **`BoardEvent`** (record)
> - `eventId: String`
> - `boardId: String`
> - `type: BoardEventType`
> - `actorId: String`
> - `occurredAt: Instant`
> - `payload: Map<String, Object>`
>
> **`BoardEventType`** (enum)
> - `ELEMENT_CREATED`, `ELEMENT_MOVED`, `ELEMENT_UPDATED`, `ELEMENT_DELETED`, `CONNECTOR_CREATED`
>
> **`BoardEventApplicationService`**
> - `apply(boardId, event): Board`
> - `applyElementCreated(board, event): Board`
> - `applyElementMoved(board, event): Board`
> - `applyElementDeleted(board, event): Board`
> - `applyConnectorCreated(board, event): Board`
> - depende de `BoardRepository`
>
> **`BoardWebSocketController`**
> - `handle(boardId, event): void`
> - depende de `BoardEventApplicationService` y `SimpMessagingTemplate`
>
> **`WebSocketConfig`**
> - configura endpoint `/ws`, prefijo `/app`, broker `/topic`

## Quality rule

Los diagramas deben describir el código que se entrega. Sin cajas decorativas ni clases de framework.
