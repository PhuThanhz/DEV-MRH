-- Normalize legacy evaluation sub-criteria weights.
-- Scoring uses the parent criterion weight and divides it equally across sub-criteria,
-- so sub-criteria must not carry their own weight.

UPDATE template_criteria
SET weight = 0
WHERE id > 0
  AND parent_id IS NOT NULL
  AND weight <> 0;
