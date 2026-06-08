-- ============================================================
--  LoanManager — Script SQL v2 (IDs corregidos a BIGINT)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS loanmanager_db;

CREATE DATABASE loanmanager_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE loanmanager_db;

-- ── rol ──────────────────────────────────────────────────────
CREATE TABLE rol (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50)     NOT NULL,
    descripcion VARCHAR(255)    NULL,
    activo      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_rol PRIMARY KEY (id),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── usuario ───────────────────────────────────────────────────
CREATE TABLE usuario (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(100)    NOT NULL,
    apellido        VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    telefono        VARCHAR(20)     NULL,
    foto_url        VARCHAR(500)    NULL,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    ultimo_acceso   TIMESTAMP       NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_email UNIQUE (email),
    CONSTRAINT chk_usuario_email CHECK (email LIKE '%@%.%')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── cliente ───────────────────────────────────────────────────
CREATE TABLE cliente (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    cedula           VARCHAR(20)     NOT NULL,
    nombre           VARCHAR(100)    NOT NULL,
    apellido         VARCHAR(100)    NOT NULL,
    email            VARCHAR(150)    NULL,
    telefono         VARCHAR(20)     NOT NULL,
    direccion        TEXT            NULL,
    ciudad           VARCHAR(100)    NULL,
    fecha_nacimiento DATE            NULL,
    ingreso_mensual  DECIMAL(12,2)   NULL,
    activo           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_cliente PRIMARY KEY (id),
    CONSTRAINT uq_cliente_cedula UNIQUE (cedula),
    CONSTRAINT chk_cliente_ingreso CHECK (ingreso_mensual IS NULL OR ingreso_mensual >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── usuario_rol ───────────────────────────────────────────────
CREATE TABLE usuario_rol (
    usuario_id   BIGINT    NOT NULL,
    rol_id       BIGINT    NOT NULL,
    asignado_en  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asignado_por BIGINT    NULL,
    CONSTRAINT pk_usuario_rol PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_ur_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ur_rol     FOREIGN KEY (rol_id)     REFERENCES rol(id)     ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── prestamo ──────────────────────────────────────────────────
CREATE TABLE prestamo (
    id                   BIGINT          NOT NULL AUTO_INCREMENT,
    cliente_id           BIGINT          NOT NULL,
    asesor_id            BIGINT          NOT NULL,
    monto                DECIMAL(12,2)   NOT NULL,
    tasa_interes         DECIMAL(5,4)    NOT NULL,
    plazo_meses          TINYINT UNSIGNED NOT NULL,
    sistema_amortizacion ENUM('FRANCES','ALEMAN','AMERICANO') NOT NULL DEFAULT 'FRANCES',
    estado               ENUM('SOLICITADO','EN_REVISION','APROBADO','RECHAZADO',
                              'DESEMBOLSADO','AL_DIA','EN_MORA','CANCELADO','CASTIGADO')
                         NOT NULL DEFAULT 'SOLICITADO',
    fecha_solicitud      DATE            NOT NULL DEFAULT (CURRENT_DATE),
    fecha_aprobacion     DATE            NULL,
    fecha_desembolso     DATE            NULL,
    fecha_cancelacion    DATE            NULL,
    total_a_pagar        DECIMAL(14,2)   NULL,
    total_interes        DECIMAL(14,2)   NULL,
    observaciones        TEXT            NULL,
    created_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_prestamo PRIMARY KEY (id),
    CONSTRAINT fk_prestamo_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_prestamo_asesor  FOREIGN KEY (asesor_id)  REFERENCES usuario(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_prestamo_monto  CHECK (monto > 0),
    CONSTRAINT chk_prestamo_tasa   CHECK (tasa_interes > 0 AND tasa_interes < 1),
    CONSTRAINT chk_prestamo_plazo  CHECK (plazo_meses > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── cuota ─────────────────────────────────────────────────────
CREATE TABLE cuota (
    id                BIGINT           NOT NULL AUTO_INCREMENT,
    prestamo_id       BIGINT           NOT NULL,
    numero_cuota      TINYINT UNSIGNED NOT NULL,
    capital           DECIMAL(12,2)    NOT NULL,
    interes           DECIMAL(12,2)    NOT NULL,
    valor_total       DECIMAL(12,2)    NOT NULL,
    fecha_vencimiento DATE             NOT NULL,
    saldo_capital     DECIMAL(14,2)    NOT NULL,
    estado            ENUM('PENDIENTE','PAGADA','PAGADA_PARCIAL','EN_MORA','CONDONADA')
                      NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT pk_cuota PRIMARY KEY (id),
    CONSTRAINT uq_cuota_prestamo_numero UNIQUE (prestamo_id, numero_cuota),
    CONSTRAINT fk_cuota_prestamo FOREIGN KEY (prestamo_id) REFERENCES prestamo(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_cuota_capital CHECK (capital > 0),
    CONSTRAINT chk_cuota_interes CHECK (interes >= 0),
    CONSTRAINT chk_cuota_total   CHECK (valor_total > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── pago ──────────────────────────────────────────────────────
CREATE TABLE pago (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    cuota_id        BIGINT          NOT NULL,
    registrado_por  BIGINT          NOT NULL,
    monto_pagado    DECIMAL(12,2)   NOT NULL,
    interes_mora    DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    dias_mora       SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    monto_total     DECIMAL(12,2)   NOT NULL,
    fecha_pago      DATE            NOT NULL DEFAULT (CURRENT_DATE),
    referencia      VARCHAR(100)    NULL,
    medio_pago      ENUM('EFECTIVO','TRANSFERENCIA','CONSIGNACION','CHEQUE','TARJETA','PSE','OTRO')
                    NOT NULL DEFAULT 'EFECTIVO',
    observaciones   TEXT            NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_pago PRIMARY KEY (id),
    CONSTRAINT fk_pago_cuota   FOREIGN KEY (cuota_id)       REFERENCES cuota(id)   ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_pago_usuario FOREIGN KEY (registrado_por) REFERENCES usuario(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_pago_monto     CHECK (monto_pagado > 0),
    CONSTRAINT chk_pago_mora      CHECK (interes_mora >= 0),
    CONSTRAINT chk_pago_dias_mora CHECK (dias_mora >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Indices ───────────────────────────────────────────────────
CREATE INDEX idx_usuario_email       ON usuario(email);
CREATE INDEX idx_usuario_activo      ON usuario(activo);
CREATE INDEX idx_cliente_cedula      ON cliente(cedula);
CREATE INDEX idx_cliente_nombre      ON cliente(nombre, apellido);
CREATE INDEX idx_cliente_email       ON cliente(email);
CREATE INDEX idx_prestamo_cliente    ON prestamo(cliente_id);
CREATE INDEX idx_prestamo_asesor     ON prestamo(asesor_id);
CREATE INDEX idx_prestamo_estado     ON prestamo(estado);
CREATE INDEX idx_prestamo_fecha      ON prestamo(fecha_solicitud);
CREATE INDEX idx_prestamo_estado_fecha ON prestamo(estado, fecha_desembolso);
CREATE INDEX idx_cuota_prestamo      ON cuota(prestamo_id);
CREATE INDEX idx_cuota_vencimiento   ON cuota(fecha_vencimiento);
CREATE INDEX idx_cuota_estado        ON cuota(estado);
CREATE INDEX idx_cuota_mora_check    ON cuota(estado, fecha_vencimiento);
CREATE INDEX idx_pago_cuota          ON pago(cuota_id);
CREATE INDEX idx_pago_fecha          ON pago(fecha_pago);
CREATE INDEX idx_pago_registrador    ON pago(registrado_por);

-- ── Vista ─────────────────────────────────────────────────────
CREATE OR REPLACE VIEW v_cartera_activa AS
SELECT
    p.id                                        AS prestamo_id,
    CONCAT(c.nombre, ' ', c.apellido)           AS cliente,
    c.cedula,
    p.monto                                     AS monto_prestado,
    p.tasa_interes,
    p.plazo_meses,
    p.estado,
    p.fecha_desembolso,
    COUNT(cu.id)                                AS total_cuotas,
    SUM(CASE WHEN cu.estado = 'PAGADA' THEN 1 ELSE 0 END) AS cuotas_pagadas,
    SUM(CASE WHEN cu.estado = 'EN_MORA' THEN 1 ELSE 0 END) AS cuotas_en_mora,
    SUM(CASE WHEN cu.estado IN ('PENDIENTE','PAGADA_PARCIAL','EN_MORA')
             THEN cu.valor_total ELSE 0 END)    AS saldo_pendiente
FROM prestamo p
JOIN cliente c  ON p.cliente_id = c.id
JOIN cuota cu   ON cu.prestamo_id = p.id
WHERE p.estado NOT IN ('RECHAZADO','CANCELADO')
GROUP BY p.id, c.nombre, c.apellido, c.cedula,
         p.monto, p.tasa_interes, p.plazo_meses, p.estado, p.fecha_desembolso;

-- ── Seeders ───────────────────────────────────────────────────
INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMIN',   'Administrador con acceso total al sistema'),
    ('ASESOR',  'Asesor de credito que gestiona prestamos'),
    ('CLIENTE', 'Cliente con acceso limitado a sus datos');

INSERT INTO usuario (nombre, apellido, email, password_hash, telefono) VALUES
    ('Carlos',  'Mendoza',  'admin@loanmanager.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lkDm', '3001234567'),
    ('Daniela', 'Rios',     'daniela.rios@loanmanager.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lkDm', '3109876543'),
    ('Andres',  'Castillo', 'andres.castillo@loanmanager.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lkDm', '3154567890');

INSERT INTO usuario_rol (usuario_id, rol_id) VALUES
    (1, 1), (1, 2), (2, 2), (3, 2);

INSERT INTO cliente (cedula, nombre, apellido, email, telefono, direccion, ciudad, fecha_nacimiento, ingreso_mensual) VALUES
    ('1098765432', 'Juan',  'Garcia',   'juan.garcia@email.com',   '3201112233', 'Cra 5 #12-34', 'Neiva',    '1985-03-15', 3500000.00),
    ('52341678',   'Maria', 'Lopez',    'maria.lopez@email.com',   '3112223344', 'Cl 8 #22-11',  'Bogota',   '1990-07-22', 5200000.00),
    ('1067890123', 'Pedro', 'Ramirez',  'pedro.ramirez@email.com', '3003334455', 'Av 3 #45-67',  'Medellin', '1978-11-08', 4100000.00),
    ('41234567',   'Ana',   'Martinez', 'ana.martinez@email.com',  '3184445566', 'Cra 15 #3-22', 'Cali',     '1995-01-30', 2800000.00),
    ('1099001234', 'Luis',  'Hernandez', NULL,                     '3005556677', 'Cl 20 #8-90',  'Neiva',    '1982-06-18', 6000000.00);

INSERT INTO prestamo (cliente_id, asesor_id, monto, tasa_interes, plazo_meses,
                      sistema_amortizacion, estado, fecha_solicitud, fecha_aprobacion,
                      fecha_desembolso, total_a_pagar, total_interes) VALUES
    (1, 2, 5000000.00,  0.0245, 12, 'FRANCES', 'AL_DIA',    '2025-01-05', '2025-01-08', '2025-01-10', 5782500.00,  782500.00),
    (2, 2, 10000000.00, 0.0220, 24, 'FRANCES', 'EN_MORA',   '2024-09-01', '2024-09-05', '2024-09-10', 12860000.00, 2860000.00),
    (3, 3, 3000000.00,  0.0260, 6,  'FRANCES', 'SOLICITADO','2025-06-01', NULL,          NULL,          NULL,        NULL),
    (4, 2, 2000000.00,  0.0245, 6,  'FRANCES', 'CANCELADO', '2024-06-01', '2024-06-03', '2024-06-05', 2154000.00,  154000.00),
    (5, 1, 15000000.00, 0.0200, 36, 'FRANCES', 'AL_DIA',    '2025-02-01', '2025-02-03', '2025-02-05', 19620000.00, 4620000.00);

INSERT INTO cuota (prestamo_id, numero_cuota, capital, interes, valor_total, fecha_vencimiento, saldo_capital, estado) VALUES
    (1,  1, 390209.00, 122500.00, 512709.00, '2025-02-10', 4609791.00, 'PAGADA'),
    (1,  2, 399764.00, 112945.00, 512709.00, '2025-03-10', 4210027.00, 'PAGADA'),
    (1,  3, 409546.00, 103163.00, 512709.00, '2025-04-10', 3800481.00, 'PAGADA'),
    (1,  4, 419563.00,  93146.00, 512709.00, '2025-05-10', 3380918.00, 'PAGADA'),
    (1,  5, 429824.00,  82885.00, 512709.00, '2025-06-10', 2951094.00, 'PAGADA'),
    (1,  6, 440338.00,  72371.00, 512709.00, '2025-07-10', 2510756.00, 'PENDIENTE'),
    (1,  7, 451111.00,  61598.00, 512709.00, '2025-08-10', 2059645.00, 'PENDIENTE'),
    (1,  8, 462151.00,  50558.00, 512709.00, '2025-09-10', 1597494.00, 'PENDIENTE'),
    (1,  9, 473469.00,  39240.00, 512709.00, '2025-10-10', 1124025.00, 'PENDIENTE'),
    (1, 10, 485073.00,  27636.00, 512709.00, '2025-11-10',  638952.00, 'PENDIENTE'),
    (1, 11, 496975.00,  15734.00, 512709.00, '2025-12-10',  141977.00, 'PENDIENTE'),
    (1, 12, 141977.00,   3480.00, 145457.00, '2026-01-10',       0.00, 'PENDIENTE');

INSERT INTO pago (cuota_id, registrado_por, monto_pagado, interes_mora, dias_mora, monto_total, fecha_pago, medio_pago, referencia) VALUES
    (1, 2, 512709.00, 0.00, 0, 512709.00, '2025-02-09', 'TRANSFERENCIA', 'TRF-001-2025'),
    (2, 2, 512709.00, 0.00, 0, 512709.00, '2025-03-08', 'TRANSFERENCIA', 'TRF-002-2025'),
    (3, 2, 512709.00, 0.00, 0, 512709.00, '2025-04-10', 'EFECTIVO',      'EFT-001-2025'),
    (4, 2, 512709.00, 0.00, 0, 512709.00, '2025-05-09', 'TRANSFERENCIA', 'TRF-003-2025'),
    (5, 2, 512709.00, 0.00, 0, 512709.00, '2025-06-10', 'CONSIGNACION',  'CON-001-2025');

SET FOREIGN_KEY_CHECKS = 1;

-- Verificacion final
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'loanmanager_db' AND COLUMN_NAME = 'id'
ORDER BY TABLE_NAME;

SELECT 'Roles:'    AS info; SELECT * FROM rol;
SELECT 'Usuarios:' AS info; SELECT id, nombre, email FROM usuario;
SELECT 'Clientes:' AS info; SELECT id, cedula, nombre FROM cliente;
