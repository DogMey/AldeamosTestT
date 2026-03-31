# Análisis de la Solución

## Contexto

Sistema de mensajería asíncrona para envío de mensajes entre líneas telefónicas. El sistema debe:

- Validar origen
- Publicar el mensaje y persistirlo
- Con una restricción de negocio de máximo 3 mensajes por destinatario en 24 horas.

---

## Herramientas seleccionadas

| Herramienta | Rol | 
|---|---|---|
| Spring Boot 4.0.5 | Framework base |
| MySQL 8 | Persistencia Producer |
| MongoDB 7 | Persistencia Consumer |
| RabbitMQ 3 | Broker de mensajería |
| JWT (JJWT 0.12.6) | Autenticación |
| Docker Compose | Orquestación local |

---

## Decisiones de diseño

**DTOs como `record`**  
Facil implementación y simplificación de codigo.

**Interfaces de servicio + `Impl`**  
Permite cambiar implementaciones sin tocar los controladores. Seguimos buenas practicas en SpringBoot.

**`@ConfigurationProperties` para topología RabbitMQ**  
Spring solo autoconfigura la conexión, no la topología(por eso se necesita una clase propia).

**Un único `docker-compose.yml` en la raíz**  
Monorepo: un solo archivo orquesta todos los servicios. Cada microservicio tiene su propio `Dockerfile`.

**Header `x-timestamp-reception`**  
El producer inyecta el timestamp en el momento en que recibe la petición HTTP. El consumer lo lee para calcular el `processingTime`.

---

## Supuestos y restricciones

Puntos que el problema no definía explícitamente o que se tuvieron en cuenta:

- Se tomo `content` es **opcional para TEXTO** y **obligatorio para multimedia** (IMAGEN, VIDEO, DOCUMENTO).
- Al superar el límite de 3 mensajes, **el mensaje se persiste con `error`** se guarda también para tener auditoria del error.
- La ventana de 24h se definio como **deslizante** (últimas 24h desde ahora), no un reset a medianoche.
- Las líneas autorizadas son **fijas en base de datos**, sin endpoint para gestionarlas.
- El `processingTime` mide desde que el producer recibe la petición HTTP hasta que el consumer la persiste.

---

## Riesgos identificados

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Consumer caído al llegar mensajes | Mensajes sin procesar | RabbitMQ guarda los mensajes en cola hasta que el consumer vuelva a estar disponible |
| MySQL tarda en iniciar | Producer falla al arrancar | Docker no levanta el producer hasta que MySQL responde correctamente al healthcheck |
| JWT secret débil | Tokens forjables | El secret debe tener mínimo 32 caracteres; si es corto, la librería rechaza el arranque |
| Ventana de 24h mal calculada | Regla de negocio incorrecta | Se calcula como "últimas 24 horas exactas" usando el reloj del servidor, sin depender de la zona horaria |

---

## Validaciones implementadas (Producer)

- **E.164** para `origin` y `destination` (`^\+[1-9]\d{6,14}$`)
- **URL válida** (`http/https`) para `content` cuando `messageType` es `IMAGEN`, `VIDEO` o `DOCUMENTO`
- **Origen autorizado**: se verifica en MySQL antes de publicar
- **JWT obligatorio** en todos los endpoints excepto `/auth/login`
