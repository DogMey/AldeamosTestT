-- Tabla de líneas autorizadas
CREATE TABLE IF NOT EXISTS origin_lines (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    phone_number VARCHAR(20)  NOT NULL,
    description  VARCHAR(255),
    active       TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_phone_number (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Cinco líneas de origen predefinidas (Requisito 3)
INSERT IGNORE INTO origin_lines (phone_number, description, active) VALUES
    ('+573001234567', 'Línea principal',    1),
    ('+573009876543', 'Línea secundaria',   1),
    ('+573001112233', 'Línea corporativa',  1),
    ('+573004445566', 'Línea regional',     1),
    ('+573007778899', 'Línea de reserva',   1);
