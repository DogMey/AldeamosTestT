# Cuestionario Técnico

---

## 1. Requerimientos no funcionales considerados y faltantes

**Implementados:**

- **Seguridad**: Se implemento JWT para la protección del endpoint de envio de mensajes, se creo uno aparte (login) para generar el bearer. El sistema de archivos esta en solo lectura y se tiene la limitacion para no crear nuevos privilegios al nivel del docker-compose.
- **Resiliencia**: Las colas de RabbitMQ se configuraron para que los mensajes no se pierdan si el servicio se reinicia. Además, se configuró que si un contenedor falla, se intente levantar de nuevo automáticamente. También se definieron verificaciones de salud para que los servicios no arranquen hasta que las bases de datos y RabbitMQ estén listos.
- **Trazabilidad**: El campo `processingTime` mide el tiempo de extremo a extremo desde que el producer recibe la petición HTTP hasta que el consumer persiste en MongoDB, usando el header AMQP `x-timestamp-reception`. Así podemos medir el tiempo en cola de la petición también.
- **Auditabilidad**: los mensajes que superan el límite de negocio se guardan con su campo `error` en lugar de descartarse.
- **Validación en frontera**: E.164 para teléfonos (Es un protocolo que nos define como se estructura un teléfono), URL válida para multimedia (Revisando la estructura), `content` obligatorio en todos los tipos.

**Que faltaron en la especificación original:**

- **Observabilidad**: no se especificó exposición de métricas ni integración con sistemas de monitoreo.
- **Rate limiting**: no se definió un límite de peticiones por IP o por token en el producer. Lo unico que implementamos fue la regla de negocio.
- **Cifrado en tránsito**: la especificación no mencionó la necesidad de cifrar las peticiones.
- **Autenticación del consumer**: el endpoint `GET /api/v1/messages` del consumer queda sin protección JWT, ya que la especificación no lo contempló. Esto sería un punto a mejorar.
- **Paginación**: el endpoint de consulta devuelve todos los mensajes del destinatario sin límite. Lo que en un futuro puede generar problemas de rendimiento.

---

## 2. Patrones de diseño implementados

| Patrón | Dónde |
|---|---|
| **Repository** | `MessageRepository`, `OriginLineRepository` — acceso a datos desacoplado de la lógica |
| **Service Layer / Facade** | `MessageService` + `MessageServiceImpl` — los controladores no conocen la lógica de negocio |
| **DTO** | `MessageRequestDto`, `AuthRequestDto`, `AuthResponseDto` — separación entre API y modelo interno, se implementaron para la transferencia de datos |
| **Builder** | `MessageDocument` con `@Builder` de Lombok — construcción clara de documentos con campos opcionales como el error en el message|
| **Factory** | `SimpleRabbitListenerContainerFactory` — creación configurable del contenedor de listeners |
| **Configuration Properties** | `RabbitMQProperties`, `RabbitMQTopologyProperties`, `JwtProperties` — externalización de configuración tipada a través del .env |
| **Chain of Responsibility** | Filtros de Spring Security (`JwtAuthenticationFilter` → `UsernamePasswordAuthenticationFilter`) |
| **Observer / Event-driven** | `@RabbitListener` — el consumer reacciona a eventos publicados por el producer sin acoplamiento directo |

---

## 3. Recomendaciones para reducir la carga operativa

- **Spring Boot Actuator**: Para que las plataformas de orquestación (Kubernetes, ECS) gestionen la salud del servicio.
- **Centralizar logs**: enviar logs estructurados (JSON) a un stack ELK o Grafana Loki.
- **CI/CD**: automatizar build, test y despliegue con GitHub Actions o GitLab CI.
- **Gestión de secretos**: usar HashiCorp Vault o AWS Secrets Manager en lugar de variables de entorno planas en `.env`.
- **Alertas sobre la cola**: configurar una alerta cuando `producer.queue` supere N mensajes acumulados.

---

## 4. Consideraciones para ejecutar con el mínimo de recursos en producción

- **Imagen JRE (no JDK)**: Es mas liviana.
- **Build multi-etapa**: el Dockerfile separa la compilación del runtime; la imagen final no contiene Maven, fuentes ni dependencias de compilación.
- **`-Djava.security.egd=file:/dev/./urandom`**: Evita bloqueos del JVM al generar tokens y reduce el tiempo de arranque en contenedores.
- **Índice MongoDB**: `@Indexed` sobre el campo `destination` en `MessageDocument`.
- **Sistema de archivos read-only**: Previene escrituras accidentales en disco del contenedor (Esto ayuda tambien a la seguridad), sin costo en rendimiento.
- **`tmpfs` en `/tmp`**: Los archivos temporales del JVM van a RAM, no a disco.

---

## 5. Aspectos para una fase evolutiva de refactorización

- **Dead Letter Queue (DLQ)**: Implementar una cola de mensajes fallidos para mensajes que el consumer no pueda procesar con mecanismo de reintento o alerta.
- **Circuit breaker**: Agregar Resilience4j en el producer para manejar degradación de RabbitMQ sin que el servicio entero falle.
- **Paginación en el consumer**: el endpoint `GET /api/v1/messages` devuelve todos los registros; en producción necesitaría `Pageable`.
- **Protección del consumer API**: agregar JWT o API key al endpoint de consulta del consumer.
- **Separar la validación de origen**: actualmente es una consulta síncrona a MySQL en el path crítico; podría cachear las líneas autorizadas con Redis para reducir latencia.

---

## 6. Estrategia de despliegue propuesta

**Entorno actual (desarrollo/demo):** Docker Compose con un único `docker-compose.yml` en la raíz del monorepo. Simple y sin dependencias externas.

Sin embargo, si queremos escalar el proyecto si necesitamos cambiar la manera en que desplagamos de la siguiente manera:

**Entorno productivo:** Kubernetes.

- **Rolling deployment**: Actualizar las réplicas de forma gradual — en ningún momento el servicio queda completamente caído.
- **Readiness/Liveness probes**: Kubernetes espera que el pod esté listo (via `/actuator/health`) antes de enrutar tráfico.
- **Horizontal Pod Autoscaler (HPA)**: Escalar el consumer automáticamente según la profundidad de `producer.queue`. Esto pues la complegidad del proceso es baja y lo mejor es aumentar el número de nodos que su capacidad.
- **Separación de configmaps y secrets**: las variables de entorno van a ConfigMaps (config no sensible) y Secrets (JWT secret, contraseñas de base de datos).

---

## 7. Mecanismos de seguridad implementados

| Mecanismo | Descripción |
|---|---|
| **JWT (HMAC-SHA256)** | Todos los endpoints del producer excepto `/api/v1/auth/login` requieren `Authorization: Bearer <token>`. El secret debe tener mínimo 32 caracteres. |
| **Usuario no-root** | Los contenedores corren como `appuser` (UID sin privilegios), creado explícitamente en el Dockerfile. |
| **`no-new-privileges`** | El proceso del contenedor no puede escalar privilegios. |
| **Sistema de archivos read-only** | El contenedor no puede escribir en disco salvo en `/tmp` (montado como `tmpfs`). |
| **Validaciones de entrada** | E.164 para teléfonos, URL http/https para multimedia, `content` obligatorio — rechazando en el producer antes de llegar a RabbitMQ. |
| **Origen autorizado** | Solo líneas registradas en MySQL pueden publicar mensajes. |
| **Imagen JRE mínima** | Reduce también la posibilidad de ataques al no incluir herramientas de compilación en la imagen de producción. |

---

## 8. Métricas para monitorear rendimiento y estabilidad

**RabbitMQ:**
- Cuántos mensajes hay acumulados en la cola.
- Cuántos mensajes entran vs. cuántos se procesan por segundo.

**Aplicación:**
- Memoria que está usando el servidor.
- Cuánto tarda en responder cada endpoint y cuántos errores está devolviendo.

**MongoDB:**
- Cuánto tarda en ejecutarse la consulta de mensajes por destinatario.

**Ya implementado en la solución:**
- El campo `processingTime` en cada mensaje guardado en MongoDB permite ver directamente cuánto tardó el sistema de punta a punta. Si ese valor empieza a crecer, algo en el proceso se está atrasando.

---

## 9. Escalabilidad

- **Consumer horizontal**: múltiples instancias del consumer pueden escuchar `producer.queue` simultáneamente. RabbitMQ distribuye los mensajes entre los consumidores activos, sin configuración adicional.
- **Producer horizontal**: stateless — cualquier número de réplicas puede atender peticiones HTTP. Un balanceador de carga distribuye el tráfico.
- **RabbitMQ en cluster**: para alta disponibilidad, RabbitMQ soporta clustering con quorum queues que replican mensajes entre nodos.
- **Índice `destination`**: ya implementado — garantiza que el conteo de mensajes en 24h y la consulta por destinatario no degraden con el volumen de datos.

---

## 10. Estrategias de pruebas utilizadas

**Pruebas unitarias (implementadas):**
- `MessageServiceImplTest`: valida los tres casos de negocio del consumer — mensaje bajo el límite se guarda sin error, mensaje que supera el límite se guarda con error, ausencia de timestamp resulta en `processingTime = 0`. Se usa Mockito para aislar el repositorio.

**Pruebas de integración manuales (E2E con Postman):**
- Flujo completo: login → obtención de JWT → envío de mensaje → consulta en MongoDB.
- Validación del límite de 3 mensajes en 24h.
- Verificación de acumulación en cola (consumer detenido) y drenado al reiniciarlo.
- Casos de error: tipo de mensaje inválido, origen no autorizado, content vacío.

**Pruebas pendientes:**
- Tests de integración con `@SpringBootTest` y Testcontainers (MongoDB, RabbitMQ, MySQL reales en contenedor).
- Tests de contrato entre producer y consumer para detectar cambios de esquema en el mensaje AMQP.

---

## 11. Facilidad de mantenimiento y extensibilidad

- **Interfaz + Impl**: los controladores dependen de la interfaz `MessageService`, no de la implementación. Cambiar la lógica de negocio no requiere tocar el controlador ni los tests que mockearon la interfaz.
- **`@ConfigurationProperties`**: agregar o cambiar parámetros de configuración es un cambio en `.env` y en la clase de propiedades, sin tocar la lógica.
- **Records para DTOs**: inmutables por definición — no se pueden modificar accidentalmente en el flujo.
- **GlobalExceptionHandler centralizado**: agregar manejo de un nuevo tipo de excepción es añadir un método `@ExceptionHandler`.
- **Versionamiento de API (`/api/v1/`)**: permite introducir `/api/v2/` con cambios de contrato sin romper clientes existentes.
- **Documentación técnica**: `analisis.md`, `design.md` y `postmortem.md` mantienen el contexto de las decisiones de diseño.