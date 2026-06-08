-- ============================================================
--  LoanManager — Script SQL Completo
--  Base de datos: MySQL 8.0
--  Codificación: UTF-8 (utf8mb4)
--  Motor: InnoDB (soporte FK y transacciones)
--  Autor: [Tu nombre]
--  Versión: 1.0.0
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- SECCIÓN 1: CONFIGURACIÓN DEL ENTORNO
-- ─────────────────────────────────────────────────────────────

-- SET FOREIGN_KEY_CHECKS=0 desactiva temporalmente la verificación
-- de FK. Útil para poder hacer DROP/CREATE sin importar el orden.
SET FOREIGN_KEY_CHECKS = 0;

-- Elimina la BD si existe (útil al resetear en desarrollo).
-- ¡NUNCA usar en producción sin backup previo!
DROP DATABASE IF EXISTS loanmanager_db;

-- utf8mb4 es el charset correcto en MySQL 8 para soportar
-- emojis, caracteres especiales y Unicode completo.
-- utf8 antiguo solo soportaba 3 bytes (incompleto).
CREATE DATABASE loanmanager_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE loanmanager_db;


-- ─────────────────────────────────────────────────────────────
-- SECCIÓN 2: TABLAS BASE (sin dependencias externas)
-- ─────────────────────────────────────────────────────────────

-- ┌─────────────────────────────────────┐
-- │  TABLA: rol                         │
-- │  Roles del sistema: ADMIN, ASESOR,  │
-- │  CLIENTE                            │
-- └─────────────────────────────────────┘
CREATE TABLE rol (
    id          INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50)     NOT NULL,
    descripcion VARCHAR(255)    NULL,
    activo      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_rol PRIMARY KEY (id),

    -- UNIQUE garantiza que no haya dos roles con el mismo nombre.
    -- Nombrar los constraints ayuda a identificar errores claramente.
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Roles de acceso al sistema';


-- ┌─────────────────────────────────────┐
-- │  TABLA: usuario                     │
-- │  Personas que operan el sistema     │
-- └─────────────────────────────────────┘
CREATE TABLE usuario (
    id              INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(100)    NOT NULL,
    apellido        VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,  -- Bcrypt hash, nunca texto plano
    telefono        VARCHAR(20)     NULL,
    foto_url        VARCHAR(500)    NULL,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    ultimo_acceso   TIMESTAMP       NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_email UNIQUE (email),

    -- CHECK es una restricción de MySQL 8+.
    -- Garantiza formato mínimo de email a nivel de BD.
    CONSTRAINT chk_usuario_email CHECK (email LIKE '%@%.%')

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Usuarios operadores del sistema (asesores, administradores)';


-- ┌─────────────────────────────────────┐
-- │  TABLA: cliente                     │
-- │  Personas que solicitan préstamos   │
-- └─────────────────────────────────────┘
CREATE TABLE cliente (
    id              INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    cedula          VARCHAR(20)     NOT NULL,
    nombre          VARCHAR(100)    NOT NULL,
    apellido        VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NULL,
    telefono        VARCHAR(20)     NOT NULL,
    direccion       TEXT            NULL,
    ciudad          VARCHAR(100)    NULL,
    fecha_nacimiento DATE           NULL,
    -- DECIMAL(12,2) = hasta 9,999,999,999.99
    -- Nunca usar FLOAT para dinero: tiene errores de precisión
    ingreso_mensual DECIMAL(12,2)   NULL,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_cliente PRIMARY KEY (id),
    CONSTRAINT uq_cliente_cedula UNIQUE (cedula),
    CONSTRAINT chk_cliente_ingreso CHECK (ingreso_mensual IS NULL OR ingreso_mensual >= 0)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Clientes solicitantes de préstamos';


-- ─────────────────────────────────────────────────────────────
-- SECCIÓN 3: TABLAS DEPENDIENTES (con FK)
-- ─────────────────────────────────────────────────────────────

-- ┌─────────────────────────────────────┐
-- │  TABLA: usuario_rol                 │
-- │  Relación N:M entre usuario y rol   │
-- └─────────────────────────────────────┘
CREATE TABLE usuario_rol (
    usuario_id  INT UNSIGNED    NOT NULL,
    rol_id      INT UNSIGNED    NOT NULL,
    asignado_en TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asignado_por INT UNSIGNED   NULL,  -- Quién asignó el rol (auditoría)

    -- Clave primaria compuesta: evita que el mismo usuario
    -- tenga el mismo rol dos veces.
    CONSTRAINT pk_usuario_rol PRIMARY KEY (usuario_id, rol_id),

    CONSTRAINT fk_ur_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON DELETE CASCADE   -- Si se borra el usuario, se borran sus roles
        ON UPDATE CASCADE,

    CONSTRAINT fk_ur_rol
        FOREIGN KEY (rol_id)
        REFERENCES rol(id)
        ON DELETE RESTRICT  -- No puedes borrar un rol si tiene usuarios asignados
        ON UPDATE CASCADE

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Asignación de roles a usuarios (N:M)';


-- ┌─────────────────────────────────────┐
-- │  TABLA: prestamo                    │
-- │  Contrato de crédito principal      │
-- └─────────────────────────────────────┘
CREATE TABLE prestamo (
    id                  INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    cliente_id          INT UNSIGNED    NOT NULL,
    asesor_id           INT UNSIGNED    NOT NULL,

    -- Monto y condiciones financieras
    monto               DECIMAL(12,2)   NOT NULL,
    tasa_interes        DECIMAL(5,4)    NOT NULL,  -- Ej: 0.0245 = 2.45% mensual
    plazo_meses         TINYINT UNSIGNED NOT NULL, -- Máx 255 meses (~21 años)

    -- ENUM garantiza solo valores válidos a nivel de BD.
    -- Alternativa: tabla catalogo_sistema_amortizacion.
    -- Para este proyecto ENUM es suficiente y más simple.
    sistema_amortizacion ENUM('FRANCES','ALEMAN','AMERICANO')
                        NOT NULL DEFAULT 'FRANCES',

    -- Estados del ciclo de vida del préstamo
    estado              ENUM('SOLICITADO','EN_REVISION','APROBADO',
                             'RECHAZADO','DESEMBOLSADO','AL_DIA',
                             'EN_MORA','CANCELADO','CASTIGADO')
                        NOT NULL DEFAULT 'SOLICITADO',

    -- Fechas del ciclo de vida
    fecha_solicitud     DATE            NOT NULL DEFAULT (CURRENT_DATE),
    fecha_aprobacion    DATE            NULL,
    fecha_desembolso    DATE            NULL,
    fecha_cancelacion   DATE            NULL,

    -- Totales calculados al momento del desembolso
    -- Se guardan para evitar recalcular siempre
    total_a_pagar       DECIMAL(14,2)   NULL,
    total_interes       DECIMAL(14,2)   NULL,

    observaciones       TEXT            NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_prestamo PRIMARY KEY (id),

    CONSTRAINT fk_prestamo_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES cliente(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_prestamo_asesor
        FOREIGN KEY (asesor_id)
        REFERENCES usuario(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    -- Reglas de negocio a nivel de BD
    CONSTRAINT chk_prestamo_monto   CHECK (monto > 0),
    CONSTRAINT chk_prestamo_tasa    CHECK (tasa_interes > 0 AND tasa_interes < 1),
    CONSTRAINT chk_prestamo_plazo   CHECK (plazo_meses > 0)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Contratos de préstamo';


-- ┌─────────────────────────────────────┐
-- │  TABLA: cuota                       │
-- │  Plan de pagos generado por préstamo│
-- └─────────────────────────────────────┘
CREATE TABLE cuota (
    id              INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    prestamo_id     INT UNSIGNED    NOT NULL,
    numero_cuota    TINYINT UNSIGNED NOT NULL,  -- 1, 2, 3... hasta plazo_meses

    -- Descomposición de la cuota (clave para reportes financieros)
    capital         DECIMAL(12,2)   NOT NULL,   -- Abono a capital
    interes         DECIMAL(12,2)   NOT NULL,   -- Interés del período
    valor_total     DECIMAL(12,2)   NOT NULL,   -- capital + interes (+ seguro si aplica)

    fecha_vencimiento DATE          NOT NULL,
    saldo_capital   DECIMAL(14,2)   NOT NULL,   -- Saldo después de esta cuota

    estado          ENUM('PENDIENTE','PAGADA','PAGADA_PARCIAL',
                         'EN_MORA','CONDONADA')
                    NOT NULL DEFAULT 'PENDIENTE',

    CONSTRAINT pk_cuota PRIMARY KEY (id),

    -- Un préstamo no puede tener dos cuotas con el mismo número
    CONSTRAINT uq_cuota_prestamo_numero UNIQUE (prestamo_id, numero_cuota),

    CONSTRAINT fk_cuota_prestamo
        FOREIGN KEY (prestamo_id)
        REFERENCES prestamo(id)
        ON DELETE CASCADE   -- Si se borra el préstamo, se borran sus cuotas
        ON UPDATE CASCADE,

    CONSTRAINT chk_cuota_capital  CHECK (capital > 0),
    CONSTRAINT chk_cuota_interes  CHECK (interes >= 0),
    CONSTRAINT chk_cuota_total    CHECK (valor_total > 0)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Plan de pagos (cuotas) de cada préstamo';


-- ┌─────────────────────────────────────┐
-- │  TABLA: pago                        │
-- │  Registro de pagos reales recibidos │
-- └─────────────────────────────────────┘
CREATE TABLE pago (
    id              INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    cuota_id        INT UNSIGNED    NOT NULL,
    registrado_por  INT UNSIGNED    NOT NULL,

    -- Descomposición del pago (tu elección del reto)
    monto_pagado    DECIMAL(12,2)   NOT NULL,
    interes_mora    DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    dias_mora       SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    monto_total     DECIMAL(12,2)   NOT NULL,  -- monto_pagado + interes_mora

    fecha_pago      DATE            NOT NULL DEFAULT (CURRENT_DATE),
    referencia      VARCHAR(100)    NULL,      -- # comprobante, transacción, etc.

    medio_pago      ENUM('EFECTIVO','TRANSFERENCIA','CONSIGNACION',
                         'CHEQUE','TARJETA','PSE','OTRO')
                    NOT NULL DEFAULT 'EFECTIVO',

    observaciones   TEXT            NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_pago PRIMARY KEY (id),

    CONSTRAINT fk_pago_cuota
        FOREIGN KEY (cuota_id)
        REFERENCES cuota(id)
        ON DELETE RESTRICT  -- No borrar cuotas que ya tienen pagos
        ON UPDATE CASCADE,

    CONSTRAINT fk_pago_usuario
        FOREIGN KEY (registrado_por)
        REFERENCES usuario(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_pago_monto       CHECK (monto_pagado > 0),
    CONSTRAINT chk_pago_mora        CHECK (interes_mora >= 0),
    CONSTRAINT chk_pago_dias_mora   CHECK (dias_mora >= 0)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Pagos reales recibidos sobre cuotas';


-- ─────────────────────────────────────────────────────────────
-- SECCIÓN 4: ÍNDICES
-- Un índice es como el índice de un libro: permite encontrar
-- filas rápido sin leer toda la tabla (full table scan).
-- Regla: indexar columnas usadas en WHERE, JOIN y ORDER BY.
-- ─────────────────────────────────────────────────────────────

-- Índices en usuario
-- Buscamos usuarios por email al hacer login
CREATE INDEX idx_usuario_email     ON usuario(email);
CREATE INDEX idx_usuario_activo    ON usuario(activo);

-- Índices en cliente
-- Buscamos clientes por cédula y por nombre
CREATE INDEX idx_cliente_cedula    ON cliente(cedula);
CREATE INDEX idx_cliente_nombre    ON cliente(nombre, apellido);
CREATE INDEX idx_cliente_email     ON cliente(email);

-- Índices en prestamo
-- Consultas frecuentes: por cliente, por asesor, por estado, por fecha
CREATE INDEX idx_prestamo_cliente  ON prestamo(cliente_id);
CREATE INDEX idx_prestamo_asesor   ON prestamo(asesor_id);
CREATE INDEX idx_prestamo_estado   ON prestamo(estado);
CREATE INDEX idx_prestamo_fecha    ON prestamo(fecha_solicitud);
-- Índice compuesto: muy útil para reportes de cartera por estado y fecha
CREATE INDEX idx_prestamo_estado_fecha ON prestamo(estado, fecha_desembolso);

-- Índices en cuota
-- Consultas frecuentes: cuotas vencidas, cuotas por estado
CREATE INDEX idx_cuota_prestamo    ON cuota(prestamo_id);
CREATE INDEX idx_cuota_vencimiento ON cuota(fecha_vencimiento);
CREATE INDEX idx_cuota_estado      ON cuota(estado);
-- Índice compuesto para detectar mora: cuotas PENDIENTES vencidas
CREATE INDEX idx_cuota_mora_check  ON cuota(estado, fecha_vencimiento);

-- Índices en pago
CREATE INDEX idx_pago_cuota        ON pago(cuota_id);
CREATE INDEX idx_pago_fecha        ON pago(fecha_pago);
CREATE INDEX idx_pago_registrador  ON pago(registrado_por);


-- ─────────────────────────────────────────────────────────────
-- SECCIÓN 5: VISTA ÚTIL — Resumen de cartera
-- Una vista es una consulta guardada. Simplifica el código
-- del backend y centraliza la lógica de negocio compleja.
-- ─────────────────────────────────────────────────────────────

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
    SUM(CASE WHEN cu.estado = 'PAGADA'
             THEN 1 ELSE 0 END)                 AS cuotas_pagadas,
    SUM(CASE WHEN cu.estado = 'EN_MORA'
             THEN 1 ELSE 0 END)                 AS cuotas_en_mora,
    SUM(CASE WHEN cu.estado IN ('PENDIENTE','PAGADA_PARCIAL','EN_MORA')
             THEN cu.valor_total ELSE 0 END)    AS saldo_pendiente
FROM prestamo p
JOIN cliente c   ON p.cliente_id = c.id
JOIN cuota cu    ON cu.prestamo_id = p.id
WHERE p.estado NOT IN ('RECHAZADO','CANCELADO')
GROUP BY p.id, c.nombre, c.apellido, c.cedula,
         p.monto, p.tasa_interes, p.plazo_meses,
         p.estado, p.fecha_desembolso;


-- ─────────────────────────────────────────────────────────────
-- SECCIÓN 6: SEEDERS — Datos de prueba
-- Datos realistas para probar el sistema sin inventarlos.
-- ─────────────────────────────────────────────────────────────

-- Roles del sistema
INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMIN',    'Administrador con acceso total al sistema'),
    ('ASESOR',   'Asesor de crédito que gestiona préstamos'),
    ('CLIENTE',  'Cliente con acceso limitado a sus propios datos');

-- Usuarios del sistema
-- NOTA: password_hash corresponde a la contraseña "Admin1234!"
-- cifrada con BCrypt (rounds=10). Nunca guardes texto plano.
INSERT INTO usuario (nombre, apellido, email, password_hash, telefono) VALUES
    ('Carlos',   'Mendoza',    'admin@loanmanager.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lkDm',
     '3001234567'),
    ('Daniela',  'Ríos',       'daniela.rios@loanmanager.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lkDm',
     '3109876543'),
    ('Andrés',   'Castillo',   'andres.castillo@loanmanager.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lkDm',
     '3154567890');

-- Asignación de roles
INSERT INTO usuario_rol (usuario_id, rol_id) VALUES
    (1, 1),  -- Carlos  → ADMIN
    (1, 2),  -- Carlos  → ASESOR (también puede gestionar préstamos)
    (2, 2),  -- Daniela → ASESOR
    (3, 2);  -- Andrés  → ASESOR

-- Clientes
INSERT INTO cliente (cedula, nombre, apellido, email, telefono,
                     direccion, ciudad, fecha_nacimiento, ingreso_mensual) VALUES
    ('1098765432', 'Juan',     'García',   'juan.garcia@email.com',
     '3201112233', 'Cra 5 #12-34', 'Neiva',       '1985-03-15', 3500000.00),
    ('52341678',   'María',    'López',    'maria.lopez@email.com',
     '3112223344', 'Cl 8 #22-11', 'Bogotá',       '1990-07-22', 5200000.00),
    ('1067890123', 'Pedro',    'Ramírez',  'pedro.ramirez@email.com',
     '3003334455', 'Av 3 #45-67', 'Medellín',     '1978-11-08', 4100000.00),
    ('41234567',   'Ana',      'Martínez', 'ana.martinez@email.com',
     '3184445566', 'Cra 15 #3-22','Cali',          '1995-01-30', 2800000.00),
    ('1099001234', 'Luis',     'Hernández',NULL,
     '3005556677', 'Cl 20 #8-90', 'Neiva',         '1982-06-18', 6000000.00);

-- Préstamos
-- Sistema Francés: cuota fija, el capital sube y el interés baja cada mes.
-- Es el más común en Colombia.
INSERT INTO prestamo (cliente_id, asesor_id, monto, tasa_interes, plazo_meses,
                      sistema_amortizacion, estado,
                      fecha_solicitud, fecha_aprobacion, fecha_desembolso,
                      total_a_pagar, total_interes) VALUES
    -- Préstamo 1: Juan García — Al día
    (1, 2, 5000000.00, 0.0245, 12, 'FRANCES', 'AL_DIA',
     '2025-01-05', '2025-01-08', '2025-01-10',
     5782500.00, 782500.00),

    -- Préstamo 2: María López — En mora
    (2, 2, 10000000.00, 0.0220, 24, 'FRANCES', 'EN_MORA',
     '2024-09-01', '2024-09-05', '2024-09-10',
     12860000.00, 2860000.00),

    -- Préstamo 3: Pedro Ramírez — Solicitado (pendiente de aprobación)
    (3, 3, 3000000.00, 0.0260, 6,  'FRANCES', 'SOLICITADO',
     '2025-06-01', NULL, NULL, NULL, NULL),

    -- Préstamo 4: Ana Martínez — Cancelado
    (4, 2, 2000000.00, 0.0245, 6, 'FRANCES', 'CANCELADO',
     '2024-06-01', '2024-06-03', '2024-06-05',
     2154000.00, 154000.00),

    -- Préstamo 5: Luis Hernández — Al día
    (5, 1, 15000000.00, 0.0200, 36, 'FRANCES', 'AL_DIA',
     '2025-02-01', '2025-02-03', '2025-02-05',
     19620000.00, 4620000.00);

-- Cuotas del Préstamo 1 (Juan García — 12 cuotas, Sistema Francés)
-- Cuota fija mensual = $512,709 aprox.
-- Capital sube, interés baja cada mes (propiedad del sistema francés).
INSERT INTO cuota (prestamo_id, numero_cuota, capital, interes,
                   valor_total, fecha_vencimiento, saldo_capital, estado) VALUES
    (1, 1,  390209.00, 122500.00, 512709.00, '2025-02-10', 4609791.00, 'PAGADA'),
    (1, 2,  399764.00, 112945.00, 512709.00, '2025-03-10', 4210027.00, 'PAGADA'),
    (1, 3,  409546.00, 103163.00, 512709.00, '2025-04-10', 3800481.00, 'PAGADA'),
    (1, 4,  419563.00,  93146.00, 512709.00, '2025-05-10', 3380918.00, 'PAGADA'),
    (1, 5,  429824.00,  82885.00, 512709.00, '2025-06-10', 2951094.00, 'PAGADA'),
    (1, 6,  440338.00,  72371.00, 512709.00, '2025-07-10', 2510756.00, 'PENDIENTE'),
    (1, 7,  451111.00,  61598.00, 512709.00, '2025-08-10', 2059645.00, 'PENDIENTE'),
    (1, 8,  462151.00,  50558.00, 512709.00, '2025-09-10', 1597494.00, 'PENDIENTE'),
    (1, 9,  473469.00,  39240.00, 512709.00, '2025-10-10', 1124025.00, 'PENDIENTE'),
    (1,10,  485073.00,  27636.00, 512709.00, '2025-11-10',  638952.00, 'PENDIENTE'),
    (1,11,  496975.00,  15734.00, 512709.00, '2025-12-10',  141977.00, 'PENDIENTE'),
    (1,12,  141977.00,   3480.00, 145457.00, '2026-01-10',        0.00, 'PENDIENTE');

-- Pagos de las cuotas pagadas del Préstamo 1
INSERT INTO pago (cuota_id, registrado_por, monto_pagado, interes_mora,
                  dias_mora, monto_total, fecha_pago, medio_pago, referencia) VALUES
    (1, 2, 512709.00, 0.00, 0, 512709.00, '2025-02-09', 'TRANSFERENCIA', 'TRF-001-2025'),
    (2, 2, 512709.00, 0.00, 0, 512709.00, '2025-03-08', 'TRANSFERENCIA', 'TRF-002-2025'),
    (3, 2, 512709.00, 0.00, 0, 512709.00, '2025-04-10', 'EFECTIVO',      'EFT-001-2025'),
    (4, 2, 512709.00, 0.00, 0, 512709.00, '2025-05-09', 'TRANSFERENCIA', 'TRF-003-2025'),
    (5, 2, 512709.00, 0.00, 0, 512709.00, '2025-06-10', 'CONSIGNACION',  'CON-001-2025');

-- Activar nuevamente la verificación de FK
SET FOREIGN_KEY_CHECKS = 1;

-- ─────────────────────────────────────────────────────────────
-- SECCIÓN 7: CONSULTAS DE VERIFICACIÓN
-- Úsalas para comprobar que los datos quedaron bien.
-- ─────────────────────────────────────────────────────────────

-- Verificar estructura
-- SHOW TABLES;
-- DESCRIBE prestamo;

-- Ver cartera activa (usando la vista creada)
-- SELECT * FROM v_cartera_activa;

-- Préstamos por estado
-- SELECT estado, COUNT(*) as total, SUM(monto) as cartera
-- FROM prestamo GROUP BY estado;

-- Cuotas pendientes de la próxima semana
-- SELECT c.numero_cuota, c.valor_total, c.fecha_vencimiento,
--        CONCAT(cl.nombre,' ',cl.apellido) as cliente
-- FROM cuota c
-- JOIN prestamo p ON c.prestamo_id = p.id
-- JOIN cliente cl ON p.cliente_id = cl.id
-- WHERE c.estado = 'PENDIENTE'
--   AND c.fecha_vencimiento BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY)
-- ORDER BY c.fecha_vencimiento;

-- ============================================================
-- FIN DEL SCRIPT
-- ============================================================
