-- ============================================================
--  LoanManager — Script SQL Completo v4
--  Incluye: todas las tablas, columnas, índices y datos
--  Compatible con MySQL 8.0 y Hibernate (Spring Boot 3.x)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
DROP DATABASE IF EXISTS loanmanager_db;
CREATE DATABASE loanmanager_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE loanmanager_db;

-- ── Tabla: rol ────────────────────────────────────────────────
CREATE TABLE rol (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50)   NOT NULL,
    descripcion VARCHAR(255)  NULL,
    activo      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_rol PRIMARY KEY (id),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Tabla: usuario ────────────────────────────────────────────
CREATE TABLE usuario (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(100)  NOT NULL,
    apellido      VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL,
    password_hash VARCHAR(255)  NOT NULL,
    telefono      VARCHAR(20)   NULL,
    foto_url      VARCHAR(500)  NULL,
    activo        BOOLEAN       NOT NULL DEFAULT TRUE,
    ultimo_acceso TIMESTAMP     NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Tabla: usuario_rol ────────────────────────────────────────
CREATE TABLE usuario_rol (
    usuario_id   BIGINT    NOT NULL,
    rol_id       BIGINT    NOT NULL,
    asignado_en  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asignado_por BIGINT    NULL,
    CONSTRAINT pk_usuario_rol PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_ur_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_rol     FOREIGN KEY (rol_id)     REFERENCES rol(id)     ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Tabla: cliente ────────────────────────────────────────────
CREATE TABLE cliente (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    cedula           VARCHAR(20)    NOT NULL,
    nombre           VARCHAR(100)   NOT NULL,
    apellido         VARCHAR(100)   NOT NULL,
    email            VARCHAR(150)   NULL,
    telefono         VARCHAR(20)    NOT NULL,
    direccion        TEXT           NULL,
    ciudad           VARCHAR(100)   NULL,
    fecha_nacimiento DATE           NULL,
    ingreso_mensual  DECIMAL(12,2)  NULL,
    activo           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_cliente PRIMARY KEY (id),
    CONSTRAINT uq_cliente_cedula UNIQUE (cedula)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Tabla: prestamo ───────────────────────────────────────────
CREATE TABLE prestamo (
    id                   BIGINT           NOT NULL AUTO_INCREMENT,
    cliente_id           BIGINT           NOT NULL,
    asesor_id            BIGINT           NOT NULL,
    monto                DECIMAL(12,2)    NOT NULL,
    tasa_interes         DECIMAL(5,4)     NOT NULL,
    plazo_meses          TINYINT UNSIGNED NOT NULL,
    sistema_amortizacion ENUM('FRANCES','ALEMAN','AMERICANO') NOT NULL DEFAULT 'FRANCES',
    estado               ENUM('SOLICITADO','EN_REVISION','APROBADO','RECHAZADO','DESEMBOLSADO','AL_DIA','EN_MORA','CANCELADO','CASTIGADO') NOT NULL DEFAULT 'SOLICITADO',
    fecha_solicitud      DATE             NOT NULL DEFAULT (CURRENT_DATE),
    fecha_aprobacion     DATE             NULL,
    fecha_desembolso     DATE             NULL,
    fecha_cancelacion    DATE             NULL,
    total_a_pagar        DECIMAL(14,2)    NULL,
    total_interes        DECIMAL(14,2)    NULL,
    observaciones        TEXT             NULL,
    created_at           TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_prestamo PRIMARY KEY (id),
    CONSTRAINT fk_prestamo_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE RESTRICT,
    CONSTRAINT fk_prestamo_asesor  FOREIGN KEY (asesor_id)  REFERENCES usuario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Tabla: cuota ──────────────────────────────────────────────
CREATE TABLE cuota (
    id                BIGINT           NOT NULL AUTO_INCREMENT,
    prestamo_id       BIGINT           NOT NULL,
    numero_cuota      TINYINT UNSIGNED NOT NULL,
    capital           DECIMAL(12,2)    NOT NULL,
    interes           DECIMAL(12,2)    NOT NULL,
    valor_total       DECIMAL(12,2)    NOT NULL,
    fecha_vencimiento DATE             NOT NULL,
    saldo_capital     DECIMAL(14,2)    NOT NULL,
    estado            ENUM('PENDIENTE','PAGADA','PAGADA_PARCIAL','EN_MORA','CONDONADA') NOT NULL DEFAULT 'PENDIENTE',
    created_at        TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_cuota PRIMARY KEY (id),
    CONSTRAINT uq_cuota_numero UNIQUE (prestamo_id, numero_cuota),
    CONSTRAINT fk_cuota_prestamo FOREIGN KEY (prestamo_id) REFERENCES prestamo(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Tabla: pago ───────────────────────────────────────────────
CREATE TABLE pago (
    id             BIGINT            NOT NULL AUTO_INCREMENT,
    cuota_id       BIGINT            NOT NULL,
    registrado_por BIGINT            NOT NULL,
    monto_pagado   DECIMAL(12,2)     NOT NULL,
    interes_mora   DECIMAL(10,2)     NOT NULL DEFAULT 0.00,
    dias_mora      SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    monto_total    DECIMAL(12,2)     NOT NULL,
    fecha_pago     DATE              NOT NULL DEFAULT (CURRENT_DATE),
    referencia     VARCHAR(100)      NULL,
    medio_pago     ENUM('EFECTIVO','TRANSFERENCIA','CONSIGNACION','CHEQUE','TARJETA','PSE','OTRO') NOT NULL DEFAULT 'EFECTIVO',
    observaciones  TEXT              NULL,
    created_at     TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_pago PRIMARY KEY (id),
    CONSTRAINT fk_pago_cuota   FOREIGN KEY (cuota_id)       REFERENCES cuota(id)   ON DELETE RESTRICT,
    CONSTRAINT fk_pago_usuario FOREIGN KEY (registrado_por) REFERENCES usuario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Índices ───────────────────────────────────────────────────
CREATE INDEX idx_usuario_email    ON usuario(email);
CREATE INDEX idx_cliente_cedula   ON cliente(cedula);
CREATE INDEX idx_prestamo_cliente ON prestamo(cliente_id);
CREATE INDEX idx_prestamo_estado  ON prestamo(estado);
CREATE INDEX idx_cuota_prestamo   ON cuota(prestamo_id);
CREATE INDEX idx_cuota_estado     ON cuota(estado, fecha_vencimiento);
CREATE INDEX idx_pago_cuota       ON pago(cuota_id);

-- ── Datos de prueba ───────────────────────────────────────────

-- Roles
INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMIN',   'Administrador con acceso total al sistema'),
    ('ASESOR',  'Asesor de credito con acceso a clientes y prestamos'),
    ('CLIENTE', 'Cliente del sistema');

-- Usuarios (contraseña: Admin1234!)
INSERT INTO usuario (nombre, apellido, email, password_hash, telefono) VALUES
    ('Carlos',  'Mendoza',  'admin@loanmanager.com',
     '$2a$10$Bs28HUzJ/yl68u1U1evgxu/NMx1RmnW9Px0KI6kXnQYF5ExjOWvga', '3001234567'),
    ('Daniela', 'Rios',     'daniela.rios@loanmanager.com',
     '$2a$10$Bs28HUzJ/yl68u1U1evgxu/NMx1RmnW9Px0KI6kXnQYF5ExjOWvga', '3109876543'),
    ('Andres',  'Castillo', 'andres.castillo@loanmanager.com',
     '$2a$10$Bs28HUzJ/yl68u1U1evgxu/NMx1RmnW9Px0KI6kXnQYF5ExjOWvga', '3154567890');

-- Roles de usuarios
INSERT INTO usuario_rol (usuario_id, rol_id) VALUES
    (1, 1), -- Carlos: ADMIN
    (1, 2), -- Carlos: ASESOR
    (2, 2), -- Daniela: ASESOR
    (3, 2); -- Andres: ASESOR

-- Clientes
INSERT INTO cliente (cedula, nombre, apellido, email, telefono, direccion, ciudad, fecha_nacimiento, ingreso_mensual) VALUES
    ('1098765432', 'Juan',    'Garcia',    'juan.garcia@email.com',    '3201112233', 'Cra 5 #12-34',  'Neiva',    '1985-03-15', 3500000.00),
    ('52341678',   'Maria',   'Lopez',     'maria.lopez@email.com',    '3112223344', 'Cl 8 #22-11',   'Bogota',   '1990-07-22', 5200000.00),
    ('1067890123', 'Pedro',   'Ramirez',   'pedro.ramirez@email.com',  '3003334455', 'Av 3 #45-67',   'Medellin', '1978-11-08', 4100000.00),
    ('41234567',   'Ana',     'Martinez',  'ana.martinez@email.com',   '3184445566', 'Cra 15 #3-22',  'Cali',     '1995-01-30', 2800000.00),
    ('1099001234', 'Luis',    'Hernandez', NULL,                       '3005556677', 'Cl 20 #8-90',   'Neiva',    '1982-06-18', 6000000.00);

-- Préstamos de prueba
INSERT INTO prestamo (cliente_id, asesor_id, monto, tasa_interes, plazo_meses, sistema_amortizacion, estado, fecha_solicitud, fecha_aprobacion, fecha_desembolso, total_a_pagar, total_interes) VALUES
    (1, 1, 10000000.00, 0.0245, 24, 'FRANCES', 'AL_DIA',    DATE_SUB(CURDATE(), INTERVAL 6 MONTH),  DATE_SUB(CURDATE(), INTERVAL 6 MONTH),  DATE_SUB(CURDATE(), INTERVAL 5 MONTH),  13176000.00, 3176000.00),
    (2, 1, 5000000.00,  0.0245, 12, 'FRANCES', 'EN_MORA',   DATE_SUB(CURDATE(), INTERVAL 4 MONTH),  DATE_SUB(CURDATE(), INTERVAL 4 MONTH),  DATE_SUB(CURDATE(), INTERVAL 3 MONTH),  6270000.00,  1270000.00),
    (3, 2, 8000000.00,  0.0245, 36, 'FRANCES', 'SOLICITADO', CURDATE(), NULL, NULL, NULL, NULL),
    (4, 2, 3000000.00,  0.0200, 6,  'FRANCES', 'APROBADO',  DATE_SUB(CURDATE(), INTERVAL 1 MONTH),  DATE_SUB(CURDATE(), INTERVAL 1 MONTH),  NULL, NULL, NULL),
    (5, 1, 15000000.00, 0.0245, 48, 'FRANCES', 'CANCELADO', DATE_SUB(CURDATE(), INTERVAL 12 MONTH), DATE_SUB(CURDATE(), INTERVAL 12 MONTH), DATE_SUB(CURDATE(), INTERVAL 11 MONTH), 19500000.00, 4500000.00);

SET FOREIGN_KEY_CHECKS = 1;
