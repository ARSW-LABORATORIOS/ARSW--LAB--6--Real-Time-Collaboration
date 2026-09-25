# ADR-003 — REST y WebSocket/STOMP coexisten

## Estado
Aceptado

## Contexto

En el Lab 6 necesitamos colaboración en tiempo real. Ya teníamos lo del REST del Lab 5
y ahora agregamos WebSocket/STOMP. La pregunta era: ¿reemplazamos REST o los dejamos convivir?

## Decisión

Los dejamos convivir, pero cada uno hace una cosa distinta:

- **REST** — para aquello que necesita una respuesta directa, crear un board, cargarlo y guardar el snapshot. Si algo falla, se sabe de inmediato.
- **WebSocket/STOMP** — para los eventos de colaboración en tiempo real, crear, mover, conectar y eliminar elementos. El servidor valida el evento y lo propaga a todos los clientes conectados al mismo board.

El servidor nunca retransmite a ciegas — todo evento pasa por `BoardEventApplicationService` antes del broadcast. Si la validación falla, no hay propagación.

## Por qué no usar solo WebSocket

Reimplementar request-response sobre STOMP (para crear/cargar/guardar) añade complejidad innecesaria. REST ya resuelve eso bien y el Lab 5 funciona. No tiene sentido romper lo que funciona.

## Consecuencias

- El API REST del Lab 5 no cambió los tests existentes siguen pasando.
- La carga inicial siempre es por REST, garantizando un estado consistente antes de suscribirse al topic STOMP.
- Si llega a fallar el WebSocket, igual se puede crear y cargar un board.
- `board-api-client.js` solo sabe de REST y `board-realtime-client.js` solo sabe de STOMP, ninguno conoce al otro, `app.js` los orquesta.
