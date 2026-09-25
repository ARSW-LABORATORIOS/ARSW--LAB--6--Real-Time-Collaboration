# Contrato de Eventos — Lab 6 Real-Time Collaboration

Los eventos viajan por STOMP del cliente al servidor en `/app/boards/{boardId}/events`
y el servidor los retransmite a todos los suscritos en `/topic/boards/{boardId}`.

## Envelope común

```json
{
  "eventId":    "uuid — el servidor lo genera si viene vacío",
  "boardId":    "uuid del board",
  "type":       "ELEMENT_CREATED | ELEMENT_MOVED | CONNECTOR_CREATED | ELEMENT_DELETED",
  "actorId":    "actor-xxxxxxxx — id aleatorio generado por sesión de navegador",
  "occurredAt": "ISO-8601",
  "payload":    {}
}
```

> Cada cliente ignora los eventos cuyo `actorId` coincida con el suyo para no aplicar su propia acción dos veces.

---

## ELEMENT_CREATED

```json
{
  "id":     "el-xxxxxxxx",
  "type":   "RECTANGLE | TEXT",
  "x":      60,
  "y":      60,
  "width":  140,
  "height": 80,
  "text":   ""
}
```

Validación: `id`, `type`, `x`, `y`, `width`, `height` son obligatorios. `type` debe ser `RECTANGLE` o `TEXT`.

---

## ELEMENT_MOVED

```json
{
  "elementId": "el-xxxxxxxx",
  "x": 120,
  "y": 200
}
```

Validación: `elementId` debe existir en el board. Se publica una sola vez al soltar el drag, no en cada pixel.

---

## CONNECTOR_CREATED

```json
{
  "id":       "el-xxxxxxxx",
  "sourceId": "el-aaaaaaaa",
  "targetId": "el-bbbbbbbb"
}
```

Validación: `sourceId` y `targetId` deben existir en el board y ser distintos entre sí.

---

## ELEMENT_DELETED

```json
{
  "elementId": "el-xxxxxxxx"
}
```

Validación: `elementId` debe existir en el board. Al eliminar un elemento se eliminan en cascada todos los conectores que lo referencien como `sourceId` o `targetId`.
