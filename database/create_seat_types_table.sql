-- 1. Xóa các check constraint giới hạn loại ghế
IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_SEAT_type' AND parent_object_id = OBJECT_ID('dbo.SEATS'))
    ALTER TABLE dbo.SEATS DROP CONSTRAINT CK_SEAT_type;

IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_SP_type' AND parent_object_id = OBJECT_ID('dbo.SEAT_PRICING'))
    ALTER TABLE dbo.SEAT_PRICING DROP CONSTRAINT CK_SP_type;

-- 2. Tạo bảng danh mục loại ghế mới
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.SEAT_TYPES') AND type in (N'U'))
BEGIN
    CREATE TABLE dbo.SEAT_TYPES (
        id              INT             NOT NULL IDENTITY(1,1),
        code            VARCHAR(20)     NOT NULL,
        name            NVARCHAR(150)   NOT NULL,
        surcharge       DECIMAL(10,2)   NOT NULL CONSTRAINT DF_SEAT_TYPES_surcharge DEFAULT 0.00,
        status          VARCHAR(20)     NOT NULL CONSTRAINT DF_SEAT_TYPES_status    DEFAULT 'ACTIVE',
        last_update     DATETIME2       NOT NULL CONSTRAINT DF_SEAT_TYPES_lu        DEFAULT GETDATE(),
        CONSTRAINT PK_SEAT_TYPES        PRIMARY KEY (id),
        CONSTRAINT UQ_SEAT_TYPES_code   UNIQUE (code),
        CONSTRAINT CK_ST_status_val     CHECK (status IN ('ACTIVE','INACTIVE'))
    );

    -- Seed các loại ghế mặc định từ hệ thống
    INSERT INTO dbo.SEAT_TYPES (code, name, surcharge, status) VALUES
    ('STANDARD', N'Standard (Ghế Thường)', 0.00, 'ACTIVE'),
    ('VIP', N'VIP (Ghế Đẹp)', 50000.00, 'ACTIVE'),
    ('COUPLE', N'Couple (Ghế Đôi)', 100000.00, 'ACTIVE');
END
GO
