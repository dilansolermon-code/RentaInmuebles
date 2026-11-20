-- =====================================================================
-- ACTUALIZACIÓN: permite el estado 'retirado' en inmueble.estado_aprobacion
-- (CU-05 y CU-06: el inmueble retirado se conserva y se muestra como "Retirado").
--
-- Ejecutar UNA vez en pgAdmin: Query Tool sobre proyecto_renta_inmuebles > F5
-- No borra ni modifica ningún dato: solo cambia la regla CHECK de la columna.
-- =====================================================================

-- 1. Quita la regla anterior (solo aceptaba pendiente, aprobada y rechazado)
ALTER TABLE inmueble DROP CONSTRAINT IF EXISTS inmueble_estado_aprobacion_check;

-- 2. Crea la regla nueva con el cuarto valor: retirado
ALTER TABLE inmueble ADD CONSTRAINT inmueble_estado_aprobacion_check
    CHECK (estado_aprobacion IN ('pendiente', 'aprobada', 'rechazado', 'retirado'));

-- 3. Comprobación: debe mostrar la regla con los cuatro valores
SELECT conname, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conname = 'inmueble_estado_aprobacion_check';
