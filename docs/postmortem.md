# Observabilidad y PostMortem

## Logs registrados

Todos los logs usan SLF4J.

| Servicio | Nivel | Evento |
|---|---|---|
| Producer | `INFO` | Mensaje publicado a RabbitMQ |
| Producer | `WARN` | Origen no autorizado |
| Producer | `ERROR` | Fallo al conectar con RabbitMQ (AmqpException) |
| Consumer | `INFO` | Mensaje recibido del listener |
| Consumer | `INFO` | Mensaje guardado en MongoDB + conteo 24h |
| Consumer | `WARN` | Límite de 3 mensajes excedido para un destino |
| Consumer | `WARN` | Header `x-timestamp-reception` no parseable |
| Consumer | `ERROR` | Error inesperado al procesar mensaje |

---

## Métricas clave a monitorear

**RabbitMQ** (Management UI en `:15672`):
- `messages_ready` en `producer.queue` → si sube y no baja, el consumer está caído
- `messages_unacknowledged` → mensajes en procesamiento; si crece, hay lentitud en consumer
- `publish_rate` y `deliver_rate` → throughput del sistema

**MongoDB** (colección `messages`):
- Documentos con `error != null` → indica destinatarios saturados o errores de negocio
- `processingTime` promedio → latencia end-to-end real del sistema
- Distribución por `messageType` → qué tipo de mensaje se usa más

**Aplicación**:
- Tiempo de respuesta del endpoint `POST /messages/send` (Producer)
- Tasa de errores 4xx (validación) vs 5xx (infra)

---

## Puntos de falla y mitigación

| Falla | Síntoma | Mitigación |
|---|---|---|
| MySQL caído | Producer retorna 503 | `restart: on-failure` + healthcheck en Compose |
| RabbitMQ caído | Producer retorna 503, mensajes perdidos | `restart: on-failure`; considerar persistencia de mensajes en RabbitMQ (`durable: true`) |
| Consumer caído | Mensajes se acumulan en la queue | `restart: on-failure`; RabbitMQ los retiene hasta que el consumer vuelva |
| MongoDB caído | Consumer no puede persistir | Los mensajes quedan en la queue sin ack; se reprocesarán al volver |
| JWT secret débil | Tokens forjables | Secret mínimo 32 caracteres; rotar periódicamente vía variable de entorno |

---

## Consultas útiles para diagnóstico

```javascript
// MongoDB: mensajes con error en las últimas 24h
db.messages.find({ error: { $ne: null }, createdDate: { $gt: new Date(Date.now() - 86400000) } })

// MongoDB: destinos que han alcanzado el límite hoy
db.messages.aggregate([
  { $match: { error: { $ne: null } } },
  { $group: { _id: "$destination", count: { $sum: 1 } } }
])

// MongoDB: processingTime promedio
db.messages.aggregate([
  { $group: { _id: null, avgMs: { $avg: "$processingTime" } } }
])
```
