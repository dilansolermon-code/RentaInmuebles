-- =====================================================================
-- DATOS DE PRUEBA para la base proyecto_renta_inmuebles
-- Ejecutar en pgAdmin: Query Tool sobre la base > pegar todo > F5
-- Respeta tus reglas CHECK: calificación entera de 1 a 5, tipo_persona Natural/Juridica,
-- nivel_acceso de 1 a 3, precios y valores mayores que 0 y salida posterior a la entrada.
--
-- "ON CONFLICT DO NOTHING" = si ya existe un registro con ese id,
-- NO lo reemplaza. Así no se dañan los datos que ya tengas.
-- =====================================================================

-- País y ciudades
INSERT INTO pais (id, nombre) VALUES (1, 'Colombia') ON CONFLICT DO NOTHING;

INSERT INTO ciudad (id, nombre, id_pais) VALUES
 (1, 'Bogotá', 1),
 (2, 'Medellín', 1),
 (3, 'Cartagena', 1),
 (4, 'Santa Marta', 1)
ON CONFLICT DO NOTHING;

-- Bancos (catálogo)
INSERT INTO banco (id, nombre) VALUES
 (1, 'Bancolombia'),
 (2, 'Banco de Bogotá'),
 (3, 'Davivienda'),
 (4, 'BBVA Colombia'),
 (5, 'Banco Popular'),
 (6, 'Nequi')
ON CONFLICT DO NOTHING;

-- Usuarios (tabla padre). Contraseña de todos: 1234
INSERT INTO usuario (id, nombre, telefono, email, contrasena) VALUES
 (1, 'Laura Gómez',      '3104567890', 'laura.gomez@correo.com',     '1234'),
 (2, 'Carlos Ruiz',      '3157894561', 'carlos.ruiz@correo.com',     '1234'),
 (3, 'Andrea Martínez',  '3201112233', 'andrea.martinez@correo.com', '1234'),
 (4, 'Administrador',    '6011234567', 'admin@rentadias.com',        '1234'),
 (5, 'Julián Pérez',     '3009876543', 'julian.perez@correo.com',    '1234')
ON CONFLICT DO NOTHING;

-- Roles (tablas hijas: comparten el id con usuario)
INSERT INTO cliente (id, calificacion, id_ciudad_residencia) VALUES
 (1, 5, 1),
 (5, 4, 2)
ON CONFLICT DO NOTHING;

INSERT INTO propietario (id, tipo_persona, calificacion) VALUES
 (2, 'Natural', 5),
 (3, 'Juridica', 4)
ON CONFLICT DO NOTHING;

INSERT INTO administrador (id, cargo, nivel_acceso) VALUES
 (4, 'Coordinador de plataforma', 1)
ON CONFLICT DO NOTHING;

-- Inmuebles
INSERT INTO inmueble (id, direccion, area_mts, numero_habitaciones, capacidad, id_ciudad) VALUES
 (1, 'Calle 60 # 4-25, Chapinero Alto',      65,  2, 4,  1),
 (2, 'Carrera 37 # 8A-30, El Poblado',       38,  1, 2,  2),
 (3, 'Calle de la Sierpe # 29-50, Getsemaní', 140, 4, 8,  3),
 (4, 'Carrera 1 # 7-45, El Rodadero',        72,  2, 5,  4),
 (5, 'Carrera 7 # 119-40, Usaquén',          18,  1, 1,  1),
 (6, 'Vía Llanogrande km 3, Rionegro',       200, 5, 10, 2)
ON CONFLICT DO NOTHING;

-- Relación Asocia: qué propietario publica cada inmueble y a qué precio por noche
INSERT INTO propietario_inmueble (id_propietario, id_inmueble, precio_noche) VALUES
 (2, 1, 180000),
 (3, 2, 140000),
 (2, 3, 520000),
 (3, 4, 230000),
 (2, 5, 85000),
 (3, 6, 610000)
ON CONFLICT DO NOTHING;

-- Cuentas bancarias
INSERT INTO cuenta_cliente (id, numero_cta, tipo_cta, id_banco, id_cliente) VALUES
 (1, '45678912345', 'Ahorros', 3, 1)
ON CONFLICT DO NOTHING;

INSERT INTO cuenta_propietario (id, numero_cta, tipo_cta, id_banco, id_propietario) VALUES
 (1, '12345678901', 'Ahorros',   1, 2),
 (2, '98765432100', 'Corriente', 2, 3)
ON CONFLICT DO NOTHING;

-- Rentas de ejemplo de Laura (cliente 1)
-- estado: 'activo' = reserva pagada (vigente o ya finalizada), 'inactivo' = cancelada
INSERT INTO renta (id, fecha_entrada, fecha_salida, valor, estado, id_cliente, id_inmueble) VALUES
 (1, '2026-12-18', '2026-12-22', 2080000, 'activo',   1, 3),
 (2, '2026-09-05', '2026-09-08',  420000, 'activo',   1, 2),
 (3, '2026-07-12', '2026-07-14',  170000, 'inactivo', 1, 5)
ON CONFLICT DO NOTHING;
