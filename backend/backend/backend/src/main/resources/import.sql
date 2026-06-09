-- ============================================================
--  LoanManager — Datos iniciales (ejecutado por Hibernate)
--  Este archivo se ejecuta automáticamente cuando
--  ddl-auto=create después de crear las tablas
-- ============================================================

INSERT INTO rol (nombre, descripcion, activo) VALUES
    ('ADMIN',   'Administrador con acceso total', true),
    ('ASESOR',  'Asesor de credito', true),
    ('CLIENTE', 'Cliente del sistema', true);

INSERT INTO usuario (nombre, apellido, email, password_hash, telefono, activo) VALUES
    ('Carlos',  'Mendoza',  'admin@loanmanager.com',
     '$2a$10$slYQmyNdgTY18LlUD7Vd8.Ob7V3PGT9pTpSdPBgF7BHSZ1MEi.IGu', '3001234567', true),
    ('Daniela', 'Rios',     'daniela.rios@loanmanager.com',
     '$2a$10$slYQmyNdgTY18LlUD7Vd8.Ob7V3PGT9pTpSdPBgF7BHSZ1MEi.IGu', '3109876543', true),
    ('Andres',  'Castillo', 'andres.castillo@loanmanager.com',
     '$2a$10$slYQmyNdgTY18LlUD7Vd8.Ob7V3PGT9pTpSdPBgF7BHSZ1MEi.IGu', '3154567890', true);

INSERT INTO usuario_rol (usuario_id, rol_id) VALUES
    (1, 1), (1, 2), (2, 2), (3, 2);

INSERT INTO cliente (cedula, nombre, apellido, email, telefono, direccion, ciudad, fecha_nacimiento, ingreso_mensual, activo) VALUES
    ('1098765432', 'Juan',  'Garcia',   'juan.garcia@email.com',   '3201112233', 'Cra 5 #12-34', 'Neiva',    '1985-03-15', 3500000.00, true),
    ('52341678',   'Maria', 'Lopez',    'maria.lopez@email.com',   '3112223344', 'Cl 8 #22-11',  'Bogota',   '1990-07-22', 5200000.00, true),
    ('1067890123', 'Pedro', 'Ramirez',  'pedro.ramirez@email.com', '3003334455', 'Av 3 #45-67',  'Medellin', '1978-11-08', 4100000.00, true),
    ('41234567',   'Ana',   'Martinez', 'ana.martinez@email.com',  '3184445566', 'Cra 15 #3-22', 'Cali',     '1995-01-30', 2800000.00, true),
    ('1099001234', 'Luis',  'Hernandez', NULL,                     '3005556677', 'Cl 20 #8-90',  'Neiva',    '1982-06-18', 6000000.00, true);
