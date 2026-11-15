-- =====================================================================
-- USUARIOS DE DEMOSTRACIÓN (uno por rol), con ids libres 9001-9003.
-- Ejecutar en pgAdmin: Query Tool sobre proyecto_renta_inmuebles > F5
--
-- La contraseña de los tres es 1234. Se guarda cifrada igual que tus
-- usuarios: 'sha256_' + huella SHA-256 de la contraseña.
-- No modifica ni borra ningún dato existente.
-- =====================================================================

INSERT INTO usuario (id, nombre, telefono, email, contrasena) VALUES
 (9001, 'Laura Gómez (demo cliente)',     '3104567890', 'cliente@rentadias.com',     'sha256_' || encode(sha256('1234'::bytea), 'hex')),
 (9002, 'Carlos Ruiz (demo propietario)', '3157894561', 'propietario@rentadias.com', 'sha256_' || encode(sha256('1234'::bytea), 'hex')),
 (9003, 'Administrador (demo)',           '6011234567', 'admin@rentadias.com',       'sha256_' || encode(sha256('1234'::bytea), 'hex'))
ON CONFLICT DO NOTHING;

-- Cliente: vive en la primera ciudad registrada
INSERT INTO cliente (id, calificacion, id_ciudad_residencia)
SELECT 9001, 5, MIN(id) FROM ciudad
ON CONFLICT DO NOTHING;

INSERT INTO propietario (id, tipo_persona, calificacion) VALUES
 (9002, 'Natural', 5)
ON CONFLICT DO NOTHING;

INSERT INTO administrador (id, cargo, nivel_acceso) VALUES
 (9003, 'Coordinador de plataforma', 1)
ON CONFLICT DO NOTHING;

-- Comprobación: deben aparecer los 3 usuarios con su rol
SELECT u.id, u.email,
       (c.id IS NOT NULL) AS es_cliente,
       (p.id IS NOT NULL) AS es_propietario,
       (a.id IS NOT NULL) AS es_admin
FROM usuario u
LEFT JOIN cliente c       ON c.id = u.id
LEFT JOIN propietario p   ON p.id = u.id
LEFT JOIN administrador a ON a.id = u.id
WHERE u.id BETWEEN 9001 AND 9003;
