# Análisis de la Solución

## Contexto

Sistema de mensajería asíncrona para envío de mensajes entre líneas telefónicas. El sistema debe validar origen, publicar el mensaje y persistirlo; con una restricción de negocio de máximo 3 mensajes por destinatario en 24 horas.

---

## Herramientas seleccionadas

| Herramienta | Rol | Por qué |
|---|---|---|
| Spring Boot 4.0.5 | Framework base | Ecosistema maduro, autoconfiguración, integración nativa con AMQP y MongoDB |
| MySQL 8 | Persistencia Producer | Datos relacionales: líneas autorizadas con validación de origen |
| MongoDB 7 | Persistencia Consumer | Documentos flexibles, sin esquema fijo, ideal para mensajes con campo `error` opcional |
| RabbitMQ 3 | Broker de mensajería | Desacoplamiento entre servicios, garantía de entrega, soporte de headers AMQP |
| JWT (JJWT 0.12.6) | Autenticación | Stateless, sin sesiones en servidor |
| Docker Compose | Orquestación local | Levanta los 5 servicios con un solo comando, healthchecks garantizan el orden de arranque |

---

## Decisiones de diseño

**DTOs como `record`**  
Inmutabilidad por defecto, menos código. Un DTO no necesita setters.

**Interfaces de servicio + `Impl`**  
Patrón estándar en Spring. Permite cambiar implementaciones sin tocar los controladores.

**`@ConfigurationProperties` para topología RabbitMQ**  
Las propiedades de exchange/queue/routing-key son configurables por entorno vía `.env`. Spring solo autoconfigura la conexión, no la topología(por eso se necesita una clase propia).

**Un único `docker-compose.yml` en la raíz**  
Monorepo: un solo archivo orquesta todos los servicios. Cada microservicio tiene su propio `Dockerfile`.

**Campo `error` en `MessageDocument`**  
En lugar de rechazar mensajes que violan la regla de negocio, se persisten con el campo `error` poblado. Permite auditoría.

**Header `x-timestamp-reception`**  
El producer inyecta el timestamp en el momento en que recibe la petición HTTP. El consumer lo lee para calcular el `processingTime` real end-to-end.

---

## Validaciones implementadas (Producer)

- **E.164** para `origin` y `destination` (`^\+[1-9]\d{6,14}$`)
- **URL válida** (`http/https`) para `content` cuando `messageType` es `IMAGEN`, `VIDEO` o `DOCUMENTO`
- **Origen autorizado**: se verifica en MySQL antes de publicar
- **JWT obligatorio** en todos los endpoints excepto `/auth/login`
