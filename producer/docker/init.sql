-- Tabla de líneas autorizadas (Requisito 3)
CREATE TABLE IF NOT EXISTS origin_lines (
    id           CHAR(36)    NOT NULL,
    origin       VARCHAR(20) NOT NULL,
    destination  VARCHAR(20),
    message_type VARCHAR(20),
    content      TEXT,
    PRIMARY KEY (id),
    UNIQUE KEY uq_origin (origin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Cinco líneas de origen predefinidas
INSERT IGNORE INTO origin_lines (id, origin) VALUES
    (UUID(), '+573001234567'),
    (UUID(), '+573009876543'),
    (UUID(), '+573001112233'),
    (UUID(), '+573004445566'),
    (UUID(), '+573007778899');
