-- ============================================================
-- RapViet Cinema — Long's Modules DDL Patch
-- Run AFTER RapViet_v3.sql (depends on BOOKINGS table)
-- Adds: dbo.TICKETS, dbo.NOTIFICATION_LOG
-- ============================================================
USE RapVietDB;
GO

-- ── Drop if re-running ───────────────────────────────────────
IF OBJECT_ID('dbo.NOTIFICATION_LOG', 'U') IS NOT NULL DROP TABLE dbo.NOTIFICATION_LOG;
IF OBJECT_ID('dbo.TICKETS',          'U') IS NOT NULL DROP TABLE dbo.TICKETS;
GO

-- ============================================================
-- Table: dbo.TICKETS
-- One ticket is generated per confirmed booking.
-- qr_code_base64 stores the ZXing-generated PNG as Base64.
-- ticket_status:
--   ISSUED         → normal, ticket sent to customer
--   USED           → scanned at gate
--   PENDING_MANUAL → QR generation failed after 3 retries
-- ============================================================
CREATE TABLE dbo.TICKETS (
    id              INT             NOT NULL IDENTITY(1,1),
    booking_id      INT             NOT NULL,
    ticket_uuid     VARCHAR(36)     NOT NULL,           -- UUID e.g. 550e8400-e29b-...
    qr_code_base64  NVARCHAR(MAX)   NULL,               -- Base64 PNG from ZXing
    is_used         BIT             NOT NULL CONSTRAINT DF_TK_used   DEFAULT 0,
    ticket_status   VARCHAR(20)     NOT NULL CONSTRAINT DF_TK_st     DEFAULT 'ISSUED',
    created_at      DATETIME2       NOT NULL CONSTRAINT DF_TK_ca     DEFAULT GETDATE(),
    last_update     DATETIME2       NOT NULL CONSTRAINT DF_TK_lu     DEFAULT GETDATE(),
    CONSTRAINT PK_TICKETS           PRIMARY KEY (id),
    CONSTRAINT FK_TK_booking        FOREIGN KEY (booking_id) REFERENCES dbo.BOOKINGS(id),
    CONSTRAINT UQ_TK_booking        UNIQUE (booking_id),        -- one ticket per booking
    CONSTRAINT UQ_TK_uuid           UNIQUE (ticket_uuid),
    CONSTRAINT CK_TK_status         CHECK (ticket_status IN ('ISSUED','USED','PENDING_MANUAL'))
);

-- ============================================================
-- Table: dbo.NOTIFICATION_LOG
-- Tracks every email send attempt (booking confirmations, promos).
-- type:
--   BOOKING_CONFIRM   → post-booking email
--   PAYMENT_CONFIRM   → post-payment email
--   PROMOTION         → batch promotional email
-- status: SENT | FAILED | PENDING
-- ============================================================
CREATE TABLE dbo.NOTIFICATION_LOG (
    id              INT             NOT NULL IDENTITY(1,1),
    booking_id      INT             NULL,               -- NULL for promotions
    notification_type VARCHAR(30)   NOT NULL,
    recipient_email VARCHAR(150)    NOT NULL,
    subject         NVARCHAR(300)   NOT NULL,
    status          VARCHAR(10)     NOT NULL CONSTRAINT DF_NL_st   DEFAULT 'PENDING',
    retry_count     INT             NOT NULL CONSTRAINT DF_NL_rc   DEFAULT 0,
    error_message   NVARCHAR(500)   NULL,               -- last error if FAILED
    sent_at         DATETIME2       NOT NULL CONSTRAINT DF_NL_sa   DEFAULT GETDATE(),
    last_update     DATETIME2       NOT NULL CONSTRAINT DF_NL_lu   DEFAULT GETDATE(),
    CONSTRAINT PK_NOTIFICATION_LOG  PRIMARY KEY (id),
    CONSTRAINT FK_NL_booking        FOREIGN KEY (booking_id) REFERENCES dbo.BOOKINGS(id),
    CONSTRAINT CK_NL_type           CHECK (notification_type IN ('BOOKING_CONFIRM','PAYMENT_CONFIRM','PROMOTION','SYSTEM')),
    CONSTRAINT CK_NL_status         CHECK (status IN ('SENT','FAILED','PENDING'))
);

-- Indexes for common lookups
CREATE INDEX IX_TK_booking     ON dbo.TICKETS          (booking_id);
CREATE INDEX IX_TK_uuid        ON dbo.TICKETS          (ticket_uuid);
CREATE INDEX IX_NL_email       ON dbo.NOTIFICATION_LOG (recipient_email, sent_at DESC);
CREATE INDEX IX_NL_status      ON dbo.NOTIFICATION_LOG (status, retry_count);
GO

PRINT 'Long patch applied: TICKETS + NOTIFICATION_LOG created.';
