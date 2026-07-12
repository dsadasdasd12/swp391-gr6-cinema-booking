/*
  Booking status tracking
  Safe migration: only adds a history table and trigger; existing BOOKINGS data is preserved.
*/
SET NOCOUNT ON;
GO

IF OBJECT_ID('dbo.BOOKING_STATUS_HISTORY', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.BOOKING_STATUS_HISTORY (
        id              INT IDENTITY(1,1) NOT NULL,
        booking_id      INT NOT NULL,
        previous_status VARCHAR(30) NULL,
        new_status      VARCHAR(30) NOT NULL,
        changed_at      DATETIME2 NOT NULL CONSTRAINT DF_BSH_changed_at DEFAULT SYSDATETIME(),
        note            NVARCHAR(500) NULL,
        CONSTRAINT PK_BOOKING_STATUS_HISTORY PRIMARY KEY (id),
        CONSTRAINT FK_BSH_booking FOREIGN KEY (booking_id) REFERENCES dbo.BOOKINGS(id)
    );

    CREATE INDEX IX_BSH_booking_changed_at
        ON dbo.BOOKING_STATUS_HISTORY (booking_id, changed_at, id);
END;
GO

/* Existing bookings did not have event data. Preserve one honest snapshot for each of them. */
INSERT INTO dbo.BOOKING_STATUS_HISTORY (booking_id, previous_status, new_status, changed_at, note)
SELECT b.id, NULL, b.status, COALESCE(b.last_update, b.booked_at, SYSDATETIME()),
       N'Lịch sử được khởi tạo khi nâng cấp hệ thống.'
FROM dbo.BOOKINGS b
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.BOOKING_STATUS_HISTORY h
    WHERE h.booking_id = b.id
);
GO

/* Capture every future booking insert and every actual status change, regardless of which DAO performs it. */
IF OBJECT_ID('dbo.TR_BOOKINGS_status_history', 'TR') IS NOT NULL
    DROP TRIGGER dbo.TR_BOOKINGS_status_history;
GO

CREATE TRIGGER dbo.TR_BOOKINGS_status_history
ON dbo.BOOKINGS
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.BOOKING_STATUS_HISTORY
        (booking_id, previous_status, new_status, changed_at, note)
    SELECT i.id,
           d.status,
           i.status,
           SYSDATETIME(),
           CASE
               WHEN d.id IS NULL THEN N'Đơn vé được tạo.'
               WHEN d.status = 'PENDING' AND i.status = 'CONFIRMED' THEN N'Thanh toán thành công, đơn vé đã được xác nhận.'
               WHEN i.status = 'CHECKED_IN' THEN N'Khách đã check-in tại rạp.'
               WHEN i.status IN ('USED', 'COMPLETED') THEN N'Vé đã được sử dụng.'
               WHEN i.status = 'CANCELLED' THEN N'Đơn vé đã bị hủy.'
               ELSE N'Trạng thái đơn vé đã được cập nhật.'
           END
    FROM inserted i
    LEFT JOIN deleted d ON d.id = i.id
    WHERE d.id IS NULL
       OR ISNULL(d.status, '') <> ISNULL(i.status, '');
END;
GO

PRINT 'Booking status history migration completed.';
