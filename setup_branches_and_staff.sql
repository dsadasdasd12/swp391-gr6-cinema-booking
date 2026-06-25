-- ============================================================
-- SQL Script: Cáº¥u hÃ¬nh phÃ¢n chi nhÃ¡nh vÃ  gÃ¡n tÃ i khoáº£n nhÃ¢n viÃªn
-- ============================================================
USE RapVietDB;
GO

-- 1. Äá»•i tÃªn chi nhÃ¡nh 1 thÃ nh 'RapViet HÃ  ÄÃ´ng' cho Ä‘á»“ng bá»™ tÃªn gá»i
UPDATE dbo.BRANCHES 
SET name = N'RapViet HÃ  ÄÃ´ng', 
    address = N'Sá»‘ 1 Quang Trung, HÃ  ÄÃ´ng, HÃ  Ná»™i' 
WHERE id = 1;

-- 2. ChÃ¨n chi nhÃ¡nh 2 'RapViet Cáº§u Giáº¥y' náº¿u chÆ°a tá»“n táº¡i
IF NOT EXISTS (SELECT 1 FROM dbo.BRANCHES WHERE name = N'RapViet Cáº§u Giáº¥y' OR id = 2)
BEGIN
    DECLARE @CinemaId INT = (SELECT TOP 1 cinema_id FROM dbo.BRANCHES WHERE id = 1);
    IF @CinemaId IS NULL SET @CinemaId = 1;
    
    SET IDENTITY_INSERT dbo.BRANCHES ON;
    INSERT INTO dbo.BRANCHES (id, cinema_id, name, address, phone, status)
    VALUES (2, @CinemaId, N'RapViet Cáº§u Giáº¥y', N'Sá»‘ 2 Tráº§n ThÃ¡i TÃ´ng, Cáº§u Giáº¥y, HÃ  Ná»™i', '024-2222', 'ACTIVE');
    SET IDENTITY_INSERT dbo.BRANCHES OFF;
END
ELSE
BEGIN
    -- Náº¿u Ä‘Ã£ cÃ³ chi nhÃ¡nh 2 nhÆ°ng tÃªn khÃ¡c, cáº­p nháº­t láº¡i tÃªn
    UPDATE dbo.BRANCHES 
    SET name = N'RapViet Cáº§u Giáº¥y', 
        address = N'Sá»‘ 2 Tráº§n ThÃ¡i TÃ´ng, Cáº§u Giáº¥y, HÃ  Ná»™i' 
    WHERE id = 2;
END
GO

-- 3. Táº¡o phÃ²ng chiáº¿u (Halls) cho chi nhÃ¡nh Cáº§u Giáº¥y (branch_id = 2) náº¿u chÆ°a cÃ³
IF NOT EXISTS (SELECT 1 FROM dbo.HALLS WHERE branch_id = 2)
BEGIN
    INSERT INTO dbo.HALLS (branch_id, name, total_seats, hall_type, status)
    VALUES 
    (2, N'PhÃ²ng Cáº§u Giáº¥y 1', 64, 'STANDARD', 'ACTIVE'),
    (2, N'PhÃ²ng Cáº§u Giáº¥y 2', 48, 'VIP', 'ACTIVE');
END
GO

-- 4. ChÃ¨n gháº¿ (Seats) cho phÃ²ng chiáº¿u má»›i Ä‘á»ƒ trÃ¡nh sÆ¡ Ä‘á»“ trá»‘ng
DECLARE @CG_HallId1 INT = (SELECT id FROM dbo.HALLS WHERE branch_id = 2 AND name = N'PhÃ²ng Cáº§u Giáº¥y 1');
DECLARE @CG_HallId2 INT = (SELECT id FROM dbo.HALLS WHERE branch_id = 2 AND name = N'PhÃ²ng Cáº§u Giáº¥y 2');

-- Gháº¿ phÃ²ng Cáº§u Giáº¥y 1 (8 hÃ ng x 8 cá»™t = 64 gháº¿)
IF @CG_HallId1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.SEATS WHERE hall_id = @CG_HallId1)
BEGIN
    DECLARE @row1 INT = 1;
    WHILE @row1 <= 8
    BEGIN
        DECLARE @rowChar1 CHAR(1) = CHAR(64 + @row1); -- A -> H
        DECLARE @col1 INT = 1;
        WHILE @col1 <= 8
        BEGIN
            INSERT INTO dbo.SEATS (hall_id, seat_row, seat_number, seat_type, maintenance)
            VALUES (@CG_HallId1, @rowChar1, @col1, 
                    CASE WHEN @row1 >= 6 THEN 'VIP' ELSE 'STANDARD' END, 0);
            SET @col1 = @col1 + 1;
        END
        SET @row1 = @row1 + 1;
    END
END

-- Gháº¿ phÃ²ng Cáº§u Giáº¥y 2 (6 hÃ ng x 8 cá»™t = 48 gháº¿)
IF @CG_HallId2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.SEATS WHERE hall_id = @CG_HallId2)
BEGIN
    DECLARE @row2 INT = 1;
    WHILE @row2 <= 6
    BEGIN
        DECLARE @rowChar2 CHAR(1) = CHAR(64 + @row2); -- A -> F
        DECLARE @col2 INT = 1;
        WHILE @col2 <= 8
        BEGIN
            INSERT INTO dbo.SEATS (hall_id, seat_row, seat_number, seat_type, maintenance)
            VALUES (@CG_HallId2, @rowChar2, @col2, 
                    CASE WHEN @row2 >= 5 THEN 'COUPLE' ELSE 'VIP' END, 0);
            SET @col2 = @col2 + 1;
        END
        SET @row2 = @row2 + 1;
    END
END
GO

-- 5. PhÃ¢n cÃ´ng tÃ i khoáº£n Quáº£n lÃ½ & NhÃ¢n viÃªn vÃ o chi nhÃ¡nh tÆ°Æ¡ng á»©ng trong STAFF_BRANCH
-- XÃ³a phÃ¢n cÃ´ng cÅ© cá»§a cÃ¡c tÃ i khoáº£n test Ä‘á»ƒ trÃ¡nh lá»—i trÃ¹ng khÃ³a
DELETE FROM dbo.STAFF_BRANCH WHERE user_id IN (10, 25, 26, 27, 28);

-- GÃ¡n chi nhÃ¡nh HÃ  ÄÃ´ng (branch_id = 1) vÃ  chi nhÃ¡nh Cáº§u Giáº¥y (branch_id = 2)
INSERT INTO dbo.STAFF_BRANCH (user_id, branch_id, position)
VALUES 
(25, 1, N'Branch Manager'), -- hadong_manager@cinema.com -> HÃ  ÄÃ´ng
(26, 1, N'Staff'),          -- hadong_staff@cinema.com -> HÃ  ÄÃ´ng
(10, 1, N'Staff'),          -- staff10@cinema.com -> HÃ  ÄÃ´ng
(27, 2, N'Branch Manager'), -- caugiay_manager@cinema.com -> Cáº§u Giáº¥y
(28, 2, N'Staff');          -- caugiay_staff@cinema.com -> Cáº§u Giáº¥y
GO

PRINT N'ÄÃ£ cáº¥u hÃ¬nh phÃ¢n chi nhÃ¡nh, phÃ²ng chiáº¿u, gháº¿ ngá»“i vÃ  gÃ¡n tÃ i khoáº£n nhÃ¢n viÃªn thÃ nh cÃ´ng!';
GO
