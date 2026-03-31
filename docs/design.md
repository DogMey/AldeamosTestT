# Diseño del Sistema

## Arquitectura general

```mermaid
graph LR
    Client(["Cliente HTTP"])

    subgraph Producer [:8080]
        PC[MessageController]
        PS[MessageService]
        PA[AuthController]
        PDB[(MySQL)]
    end

    subgraph Broker
        RMQ[[RabbitMQ\nproducer.queue]]
    end

    subgraph Consumer [:8081]
        CL[MessageListener]
        CS[MessageService]
        CC[MessageController]
        CDB[(MongoDB)]
    end

    Client -->|POST /auth/login| PA
    Client -->|POST /messages/send + JWT| PC
    PC --> PS
    PS -->|existsByOrigin| PDB
    PS -->|publish + x-timestamp-reception| RMQ
    RMQ -->|@RabbitListener| CL
    CL --> CS
    CS -->|countByDestination 24h| CDB
    CS -->|save MessageDocument| CDB
    Client -->|GET /messages?destination=| CC
    CC --> CS
```

---

## Flujo de un mensaje

```mermaid
sequenceDiagram
    actor Client
    participant Producer
    participant MySQL
    participant RabbitMQ
    participant Consumer
    participant MongoDB

    Client->>Producer: POST /messages/send (JWT)
    Producer->>Producer: Validar campos (E.164, URL)
    Producer->>MySQL: existsByOrigin(origin)?
    alt origen no autorizado
        Producer-->>Client: 403 Forbidden
    else origen válido
        Producer->>RabbitMQ: publish(message, x-timestamp-reception)
        Producer-->>Client: 202 Accepted
        RabbitMQ->>Consumer: @RabbitListener
        Consumer->>MongoDB: count mensajes destino últimas 24h
        alt count >= 3
            Consumer->>MongoDB: save(message, error="límite excedido")
        else count < 3
            Consumer->>MongoDB: save(message)
        end
    end
```

---

## Estructura de paquetes

```mermaid
graph TD
    subgraph producer
        p_ctrl[controller]
        p_svc[service / impl]
        p_repo[repository]
        p_ent[entity]
        p_dto[dto]
        p_val[validation]
        p_cfg[config]
        p_exc[exception]
    end

    subgraph consumer
        c_list[listener]
        c_ctrl[controller]
        c_svc[service / impl]
        c_repo[repository]
        c_doc[document]
        c_dto[dto]
        c_cfg[config]
        c_exc[exception]
    end
```

---

## Modelo de datos

```mermaid
erDiagram
    ORIGIN_LINES {
        char(36) id PK
        varchar(20) origin UK
        varchar(20) destination
        varchar(20) message_type
        text content
    }

    MESSAGES {
        string id PK
        string origin
        string destination
        string messageType
        string content
        long processingTime
        instant createdDate
        string error
    }
```
