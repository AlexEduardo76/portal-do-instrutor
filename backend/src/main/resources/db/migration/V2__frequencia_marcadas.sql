ALTER TABLE frequencias ADD COLUMN marcadas integer;

UPDATE frequencias SET marcadas = CASE WHEN presente THEN 4 ELSE 0 END;