-- Migration to fix evaluation template section and criteria weight units
-- Convert integer percentage values to decimals (e.g. 50.0 -> 0.5)

UPDATE template_sections
SET weight = weight / 100.0
WHERE id > 0 AND weight > 1.0;

UPDATE template_criteria
SET weight = weight / 100.0
WHERE id > 0 AND weight > 1.0;
