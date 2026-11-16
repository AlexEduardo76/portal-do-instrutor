
ALTER TABLE frequencias
ADD COLUMN IF NOT EXISTS marcadas INTEGER;

UPDATE frequencias
SET marcadas = CASE
    WHEN presente THEN 4
    ELSE 0
END
WHERE marcadas IS NULL;
