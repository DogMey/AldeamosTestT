# AldeamosTestT — Mensajería Asíncrona con Spring Boot

Sistema de mensajería asíncrona compuesto por dos microservicios que se comunican a través de RabbitMQ.

## Arquitectura

```
Cliente HTTP
    │
    ▼
┌─────────────┐     RabbitMQ      ┌──────────────┐
│  Producer   │ ──────────────── ▶│   Consumer   │
│  :8080      │  producer.queue   │   :8081      │
│  MySQL      │                   │  MongoDB     │
└─────────────┘                   └──────────────┘
```

| Servicio  | Responsabilidad                                                |
|-----------|----------------------------------------------------------------|
| Producer  | Recibe peticiones REST, valida el origen en MySQL y publica en RabbitMQ |
| Consumer  | Consume la cola RabbitMQ y persiste los mensajes en MongoDB    |

---

## Requisitos previos

| Herramienta    | Versión mínima | Descarga                          |
|----------------|---------------|-----------------------------------|
| Docker Desktop | 24.x          | https://docs.docker.com/get-docker |
| Docker Compose | 2.x           | Incluido en Docker Desktop         |
| Git            | Cualquiera    | https://git-scm.com               |

> Java y Maven **no son necesarios** en tu máquina — el build ocurre dentro de Docker.

---

## Levantamiento rápido

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd AldeamosTestT
```

### 2. Configurar las variables de entorno

```bash
cp .env.example .env
```

Abre `.env` y ajusta al menos estas variables:

```env
DB_PASSWORD=una_contraseña_segura
JWT_SECRET=cadena_aleatoria_minimo_32_caracteres
AUTH_PASSWORD=contraseña_para_el_api
```

> Para generar un JWT secret seguro: `openssl rand -base64 32`

### 3. Levantar todos los servicios

```bash
docker compose up --build
```

La primera vez tardará varios minutos mientras descarga las imágenes base y compila los microservicios.

Para ejecutar en segundo plano:

```bash
docker compose up --build -d
```

### 4. Verificar que todo está corriendo

```bash
docker compose ps
```

Deberías ver los 5 servicios en estado `running` (healthy):

```
NAME        STATUS              PORTS
producer    running (healthy)   0.0.0.0:8080->8080/tcp
consumer    running (healthy)   0.0.0.0:8081->8081/tcp
mysql       running (healthy)   0.0.0.0:3307->3306/tcp
mongodb     running (healthy)   0.0.0.0:27017->27017/tcp
rabbitmq    running (healthy)   0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
```

---

## Acceso a los servicios

| Servicio             | URL                                  | Credenciales         |
|----------------------|--------------------------------------|----------------------|
| Producer API         | http://localhost:8080                | JWT (ver sección API) |
| Consumer API         | http://localhost:8081                | —                    |
| RabbitMQ Management  | http://localhost:15672               | guest / guest        |
| MySQL                | localhost:3307                       | root / `DB_PASSWORD` |
| MongoDB              | localhost:27017                      | Sin autenticación    |

---

## Uso de la API (Producer)

### Obtener token JWT

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "<AUTH_PASSWORD>"}'
```

Respuesta:
```json
{
  "accessToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

### Enviar un mensaje de texto

```bash
curl -X POST http://localhost:8080/messages/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "origin": "+573001234567",
    "destination": "+573009876543",
    "messageType": "TEXTO",
    "content": "Hola mundo"
  }'
```

### Enviar un mensaje multimedia (imagen, video, documento)

El campo `content` debe ser una URL `http/https` válida:

```bash
curl -X POST http://localhost:8080/messages/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "origin": "+573001234567",
    "destination": "+573009876543",
    "messageType": "IMAGEN",
    "content": "https://cdn.example.com/foto.jpg"
  }'
```

**Tipos de mensaje válidos:** `TEXTO` · `IMAGEN` · `VIDEO` · `DOCUMENTO`

**Líneas de origen autorizadas (predefinidas en MySQL):**
- `+573001234567`
- `+573009876543`
- `+573001112233`
- `+573004445566`
- `+573007778899`

---

## Comandos útiles

```bash
# Ver logs de todos los servicios
docker compose logs -f

# Ver logs de un servicio específico
docker compose logs -f producer
docker compose logs -f consumer

# Detener todos los servicios (preserva los volúmenes)
docker compose down

# Detener y eliminar volúmenes (base de datos limpia)
docker compose down -v

# Reconstruir solo un servicio
docker compose up --build producer

# Entrar al contenedor del producer
docker compose exec producer sh
```

---

## Estructura del proyecto

```
AldeamosTestT/
├── docker-compose.yml       ← Orquestación de todos los servicios
├── .env.example             ← Plantilla de variables de entorno
├── README.md
├── producer/                ← Microservicio Producer (puerto 8080)
│   ├── Dockerfile
│   ├── docker/
│   │   └── init.sql         ← Esquema y datos iniciales de MySQL
│   └── src/
└── consumer/                ← Microservicio Consumer (puerto 8081)
    ├── Dockerfile
    └── src/
```

---

## Solución de problemas comunes

**El producer no conecta con MySQL al inicio**
Los healthchecks garantizan el orden de arranque, pero si MySQL tarda, Docker reiniciará el producer automáticamente (`restart: on-failure`).

**Puerto 3306 ocupado en el host**
MySQL se expone en el puerto `3307` del host para evitar conflictos con una instalación local de MySQL.

**El JWT_SECRET es muy corto**
El secreto debe tener mínimo 32 caracteres. Si es más corto, el producer fallará al arrancar.
