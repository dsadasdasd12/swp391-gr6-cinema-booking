-- =====================================================
-- RapViet: Tạo bảng ROLES, MODULES, ROLE_PERMISSIONS
-- Module: Vai trò & Phân quyền (LongND)
-- =====================================================

USE RapVietDB;
GO

-- ── 1. ROLES ─────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ROLES')
BEGIN
    CREATE TABLE dbo.ROLES (
        id          INT             NOT NULL IDENTITY(1,1),
        role_name   NVARCHAR(50)    NOT NULL,
        description NVARCHAR(255)   NULL,
        scope       NVARCHAR(100)   NOT NULL DEFAULT N'Toàn hệ thống (All Branches)',
        is_system   BIT             NOT NULL DEFAULT 0,
        created_at  DATETIME2       NOT NULL DEFAULT GETDATE(),
        CONSTRAINT PK_ROLES      PRIMARY KEY (id),
        CONSTRAINT UQ_ROLES_name UNIQUE (role_name)
    );
    PRINT N'Created table ROLES';
END
GO

-- ── 2. MODULES ───────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'MODULES')
BEGIN
    CREATE TABLE dbo.MODULES (
        id          INT             NOT NULL IDENTITY(1,1),
        module_key  VARCHAR(50)     NOT NULL,
        module_name NVARCHAR(100)   NOT NULL,
        description NVARCHAR(255)   NULL,
        sort_order  INT             NOT NULL DEFAULT 0,
        CONSTRAINT PK_MODULES      PRIMARY KEY (id),
        CONSTRAINT UQ_MODULES_key  UNIQUE (module_key)
    );
    PRINT N'Created table MODULES';
END
GO

-- ── 3. ROLE_PERMISSIONS ──────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ROLE_PERMISSIONS')
BEGIN
    CREATE TABLE dbo.ROLE_PERMISSIONS (
        id          INT     NOT NULL IDENTITY(1,1),
        role_id     INT     NOT NULL,
        module_id   INT     NOT NULL,
        can_view    BIT     NOT NULL DEFAULT 0,
        can_create  BIT     NOT NULL DEFAULT 0,
        can_edit    BIT     NOT NULL DEFAULT 0,
        can_delete  BIT     NOT NULL DEFAULT 0,
        can_export  BIT     NOT NULL DEFAULT 0,
        can_manage  BIT     NOT NULL DEFAULT 0,
        CONSTRAINT PK_ROLE_PERMS        PRIMARY KEY (id),
        CONSTRAINT FK_RP_role           FOREIGN KEY (role_id)   REFERENCES dbo.ROLES(id)   ON DELETE CASCADE,
        CONSTRAINT FK_RP_module         FOREIGN KEY (module_id) REFERENCES dbo.MODULES(id) ON DELETE CASCADE,
        CONSTRAINT UQ_RP_role_module    UNIQUE (role_id, module_id)
    );
    PRINT N'Created table ROLE_PERMISSIONS';
END
GO

-- ═══════════════════════════════════════════════════════
-- SEED DATA
-- ═══════════════════════════════════════════════════════

-- ── Roles ────────────────────────────────────────────
SET IDENTITY_INSERT dbo.ROLES ON;
MERGE dbo.ROLES AS target
USING (VALUES
    (1, N'ADMIN',   N'Quản lý cấp cao, toàn quyền tất cả chi nhánh', N'Toàn hệ thống (All Branches)', 1),
    (2, N'MANAGER', N'Trưởng chi nhánh, quản lý phòng chiếu',        N'Toàn hệ thống (All Branches)', 1),
    (3, N'STAFF',   N'Vận hành kỹ thuật, soát vé tại chi nhánh',     N'Toàn hệ thống (All Branches)', 1)
) AS src (id, role_name, description, scope, is_system)
ON target.id = src.id
WHEN NOT MATCHED THEN
    INSERT (id, role_name, description, scope, is_system)
    VALUES (src.id, src.role_name, src.description, src.scope, src.is_system);
SET IDENTITY_INSERT dbo.ROLES OFF;
PRINT N'Seeded ROLES';
GO

-- ── Modules ──────────────────────────────────────────
SET IDENTITY_INSERT dbo.MODULES ON;
MERGE dbo.MODULES AS target
USING (VALUES
    (1, 'movies',    N'Quản lý phim',       N'Danh sách phim, thêm mới, cập nhật, upload poster và trailer', 1),
    (2, 'showtimes', N'Quản lý suất chiếu',  N'Lên lịch chiếu phim, phòng chiếu, quản lý giá vé cơ bản',    2),
    (3, 'tickets',   N'Quản lý vé & Booking',N'Thực hiện check vé tại quầy trực tiếp, suất tuần đen vé, in vé', 3),
    (4, 'reports',   N'Báo cáo thống kê',    N'Xem doanh thu, ngày, doanh số rạp, tỷ lệ lấp đầy, giá cao điểm', 4),
    (5, 'accounts',  N'Tài khoản nhân sự',   N'Quản lý danh sách nhân viên, trưởng rạp, phân vai trò',       5),
    (6, 'settings',  N'Cài đặt hệ thống',    N'Cấu hình rạp chiếu, cài thiết SMTP Server, bảo trì hệ thống', 6)
) AS src (id, module_key, module_name, description, sort_order)
ON target.id = src.id
WHEN NOT MATCHED THEN
    INSERT (id, module_key, module_name, description, sort_order)
    VALUES (src.id, src.module_key, src.module_name, src.description, src.sort_order);
SET IDENTITY_INSERT dbo.MODULES OFF;
PRINT N'Seeded MODULES';
GO

-- ── ADMIN permissions (toàn quyền) ──────────────────
MERGE dbo.ROLE_PERMISSIONS AS target
USING (
    SELECT r.id AS role_id, m.id AS module_id
    FROM dbo.ROLES r CROSS JOIN dbo.MODULES m
    WHERE r.role_name = 'ADMIN'
) AS src ON target.role_id = src.role_id AND target.module_id = src.module_id
WHEN NOT MATCHED THEN
    INSERT (role_id, module_id, can_view, can_create, can_edit, can_delete, can_export, can_manage)
    VALUES (src.role_id, src.module_id, 1, 1, 1, 1, 1, 1);
PRINT N'Seeded ADMIN permissions (full access)';
GO

-- ── MANAGER permissions ─────────────────────────────
MERGE dbo.ROLE_PERMISSIONS AS target
USING (
    SELECT r.id AS role_id, m.id AS module_id,
        CASE WHEN m.module_key IN ('movies','showtimes','tickets','reports','accounts') THEN 1 ELSE 0 END AS v,
        CASE WHEN m.module_key IN ('movies','showtimes','tickets') THEN 1 ELSE 0 END AS c,
        CASE WHEN m.module_key IN ('movies','showtimes','tickets') THEN 1 ELSE 0 END AS e,
        CASE WHEN m.module_key IN ('movies') THEN 1 ELSE 0 END AS d,
        CASE WHEN m.module_key IN ('reports') THEN 1 ELSE 0 END AS x,
        CASE WHEN m.module_key IN ('movies','showtimes','tickets') THEN 1 ELSE 0 END AS mg
    FROM dbo.ROLES r CROSS JOIN dbo.MODULES m
    WHERE r.role_name = 'MANAGER'
) AS src ON target.role_id = src.role_id AND target.module_id = src.module_id
WHEN NOT MATCHED THEN
    INSERT (role_id, module_id, can_view, can_create, can_edit, can_delete, can_export, can_manage)
    VALUES (src.role_id, src.module_id, src.v, src.c, src.e, src.d, src.x, src.mg);
PRINT N'Seeded MANAGER permissions';
GO

-- ── STAFF permissions ────────────────────────────────
MERGE dbo.ROLE_PERMISSIONS AS target
USING (
    SELECT r.id AS role_id, m.id AS module_id,
        CASE WHEN m.module_key IN ('movies','showtimes','tickets') THEN 1 ELSE 0 END AS v,
        0 AS c, 0 AS e, 0 AS d, 0 AS x, 0 AS mg
    FROM dbo.ROLES r CROSS JOIN dbo.MODULES m
    WHERE r.role_name = 'STAFF'
) AS src ON target.role_id = src.role_id AND target.module_id = src.module_id
WHEN NOT MATCHED THEN
    INSERT (role_id, module_id, can_view, can_create, can_edit, can_delete, can_export, can_manage)
    VALUES (src.role_id, src.module_id, src.v, src.c, src.e, src.d, src.x, src.mg);
PRINT N'Seeded STAFF permissions';
GO

PRINT N'=== Hoàn tất tạo bảng ROLES + MODULES + ROLE_PERMISSIONS ===';
GO
