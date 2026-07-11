IF COL_LENGTH('dbo.MOVIES', 'end_date') IS NULL
BEGIN
    ALTER TABLE dbo.MOVIES ADD end_date DATE NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_MOV_dates'
      AND parent_object_id = OBJECT_ID('dbo.MOVIES')
)
BEGIN
    ALTER TABLE dbo.MOVIES
    ADD CONSTRAINT CK_MOV_dates
    CHECK (end_date IS NULL OR end_date >= release_date);
END;
GO

UPDATE dbo.MOVIES
SET status = CASE
        WHEN release_date > CAST(GETDATE() AS DATE) THEN 'COMING_SOON'
        WHEN end_date IS NOT NULL AND end_date < CAST(GETDATE() AS DATE) THEN 'ENDED'
        ELSE 'NOW_SHOWING'
    END,
    last_update = GETDATE()
WHERE status <> CASE
        WHEN release_date > CAST(GETDATE() AS DATE) THEN 'COMING_SOON'
        WHEN end_date IS NOT NULL AND end_date < CAST(GETDATE() AS DATE) THEN 'ENDED'
        ELSE 'NOW_SHOWING'
    END;
GO
