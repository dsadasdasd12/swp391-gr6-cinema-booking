-- ============================================================
-- SQL Script: ChÃ¨n suáº¥t chiáº¿u máº«u cho cÃ¡c chi nhÃ¡nh khÃ¡c nhau
-- ============================================================
USE RapVietDB;
GO

-- 1. Láº¥y thÃ´ng tin cÃ¡c chi nhÃ¡nh
DECLARE @BranchHaDong INT = (SELECT id FROM dbo.BRANCHES WHERE name = N'RapViet HÃ  ÄÃ´ng');
DECLARE @BranchCauGiay INT = (SELECT id FROM dbo.BRANCHES WHERE name = N'RapViet Cáº§u Giáº¥y');

-- Náº¿u khÃ´ng tÃ¬m tháº¥y báº±ng tÃªn chÃ­nh xÃ¡c, láº¥y cÃ¡c chi nhÃ¡nh Ä‘áº§u tiÃªn
IF @BranchHaDong IS NULL 
    SET @BranchHaDong = (SELECT TOP 1 id FROM dbo.BRANCHES ORDER BY id);
IF @BranchCauGiay IS NULL
    SET @BranchCauGiay = (SELECT TOP 1 id FROM dbo.BRANCHES WHERE id <> @BranchHaDong ORDER BY id);

-- 2. Láº¥y danh sÃ¡ch phim Ä‘ang chiáº¿u
DECLARE @Movie1 INT = (SELECT TOP 1 id FROM dbo.MOVIES WHERE status = 'NOW_SHOWING' ORDER BY id);
DECLARE @Movie2 INT = (SELECT TOP 1 id FROM dbo.MOVIES WHERE status = 'NOW_SHOWING' AND id <> @Movie1 ORDER BY id);
DECLARE @Movie3 INT = (SELECT TOP 1 id FROM dbo.MOVIES WHERE status = 'NOW_SHOWING' AND id NOT IN (@Movie1, @Movie2) ORDER BY id);

-- Fallback phÃ²ng há» trÆ°á»ng há»£p khÃ´ng Ä‘á»§ 3 phim cÃ³ tráº¡ng thÃ¡i NOW_SHOWING trong Database
IF @Movie1 IS NULL 
    SET @Movie1 = (SELECT TOP 1 id FROM dbo.MOVIES ORDER BY id);

IF @Movie2 IS NULL 
    SET @Movie2 = (SELECT TOP 1 id FROM dbo.MOVIES WHERE id <> @Movie1 ORDER BY id);
IF @Movie2 IS NULL 
    SET @Movie2 = @Movie1;

IF @Movie3 IS NULL 
    SET @Movie3 = (SELECT TOP 1 id FROM dbo.MOVIES WHERE id NOT IN (@Movie1, @Movie2) ORDER BY id);
IF @Movie3 IS NULL 
    SET @Movie3 = COALESCE(@Movie2, @Movie1);

-- 3. Láº¥y danh sÃ¡ch phÃ²ng chiáº¿u thuá»™c chi nhÃ¡nh HÃ  Ä Ã´ng
DECLARE @HD_Hall1 INT = (SELECT TOP 1 id FROM dbo.HALLS WHERE branch_id = @BranchHaDong ORDER BY id);
DECLARE @HD_Hall2 INT = (SELECT TOP 1 id FROM dbo.HALLS WHERE branch_id = @BranchHaDong AND id <> @HD_Hall1 ORDER BY id);

-- 4. Láº¥y danh sÃ¡ch phÃ²ng chiáº¿u thuá»™c chi nhÃ¡nh Cáº§u Giáº¥y
DECLARE @CG_Hall1 INT = (SELECT TOP 1 id FROM dbo.HALLS WHERE branch_id = @BranchCauGiay ORDER BY id);
DECLARE @CG_Hall2 INT = (SELECT TOP 1 id FROM dbo.HALLS WHERE branch_id = @BranchCauGiay AND id <> @CG_Hall1 ORDER BY id);

-- Định nghĩa ngày hiện tại để tạo suất chiếu cho hôm nay và ngày mai
DECLARE @today DATETIME = CAST(GETDATE() AS DATE);

-- Chèn suất chiếu cho Chi nhánh Hà Đông
IF @BranchHaDong IS NOT NULL AND @Movie1 IS NOT NULL AND @HD_Hall1 IS NOT NULL
BEGIN
    -- Suất chiếu ngày hôm nay - Hà Đông - Phòng 1
    INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
    SELECT @HD_Hall1, @Movie1, DATEADD(hour, 9, @today), DATEADD(minute, 540 + 120, @today), 80000, 'ON_SALE'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @HD_Hall1 AND start_time = DATEADD(hour, 9, @today));

    INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
    SELECT @HD_Hall1, @Movie2, DATEADD(hour, 12, @today), DATEADD(minute, 720 + 120, @today), 80000, 'ON_SALE'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @HD_Hall1 AND start_time = DATEADD(hour, 12, @today));

    -- Suất chiếu muộn hôm nay lúc 23:00 - Hà Đông - Phòng 1
    INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
    SELECT @HD_Hall1, @Movie3, DATEADD(hour, 23, @today), DATEADD(minute, 1380 + 120, @today), 80000, 'ON_SALE'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @HD_Hall1 AND start_time = DATEADD(hour, 23, @today));

    -- Suất chiếu ngày hôm nay - Hà Đông - Phòng 2 (IMAX/VIP)
    IF @HD_Hall2 IS NOT NULL
    BEGIN
        INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
        SELECT @HD_Hall2, @Movie3, DATEADD(hour, 10, @today), DATEADD(minute, 600 + 120, @today), 120000, 'ON_SALE'
        WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @HD_Hall2 AND start_time = DATEADD(hour, 10, @today));

        INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
        SELECT @HD_Hall2, @Movie1, DATEADD(hour, 14, @today), DATEADD(minute, 840 + 120, @today), 120000, 'ON_SALE'
        WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @HD_Hall2 AND start_time = DATEADD(hour, 14, @today));
    END
END

-- Chèn suất chiếu cho Chi nhánh Cầu Giấy
IF @BranchCauGiay IS NOT NULL AND @Movie1 IS NOT NULL AND @CG_Hall1 IS NOT NULL
BEGIN
    -- Suất chiếu ngày hôm nay - Cầu Giấy - Phòng 1
    INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
    SELECT @CG_Hall1, @Movie2, DATEADD(hour, 9, @today), DATEADD(minute, 540 + 120, @today), 85000, 'ON_SALE'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @CG_Hall1 AND start_time = DATEADD(hour, 9, @today));

    INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
    SELECT @CG_Hall1, @Movie3, DATEADD(hour, 13, @today), DATEADD(minute, 780 + 120, @today), 85000, 'ON_SALE'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @CG_Hall1 AND start_time = DATEADD(hour, 13, @today));

    -- Suất chiếu muộn hôm nay lúc 23:00 - Cầu Giấy - Phòng 1
    INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
    SELECT @CG_Hall1, @Movie1, DATEADD(hour, 23, @today), DATEADD(minute, 1380 + 120, @today), 85000, 'ON_SALE'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @CG_Hall1 AND start_time = DATEADD(hour, 23, @today));

    -- Suất chiếu ngày hôm nay - Cầu Giấy - Phòng 2 (IMAX/VIP)
    IF @CG_Hall2 IS NOT NULL
    BEGIN
        INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
        SELECT @CG_Hall2, @Movie1, DATEADD(hour, 11, @today), DATEADD(minute, 660 + 120, @today), 130000, 'ON_SALE'
        WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @CG_Hall2 AND start_time = DATEADD(hour, 11, @today));

        INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
        SELECT @CG_Hall2, @Movie2, DATEADD(hour, 15, @today), DATEADD(minute, 900 + 120, @today), 130000, 'ON_SALE'
        WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @CG_Hall2 AND start_time = DATEADD(hour, 15, @today));

        -- Suất chiếu muộn hôm nay lúc 22:30 - Cầu Giấy - Phòng 2
        INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
        SELECT @CG_Hall2, @Movie3, DATEADD(minute, 1350, @today), DATEADD(minute, 1350 + 120, @today), 130000, 'ON_SALE'
        WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @CG_Hall2 AND start_time = DATEADD(minute, 1350, @today));
    END
END

-- Thêm một số suất chiếu vào ngày mai (tomorrow) để hiển thị đầy đủ trên màn hình staff
DECLARE @tomorrow DATETIME = DATEADD(day, 1, @today);

-- Chi nhánh Hà Đông - Ngày mai
IF @BranchHaDong IS NOT NULL AND @Movie1 IS NOT NULL AND @HD_Hall1 IS NOT NULL
BEGIN
    -- Phòng 1
    INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
    SELECT @HD_Hall1, @Movie1, DATEADD(hour, 10, @tomorrow), DATEADD(minute, 600 + 120, @tomorrow), 80000, 'ON_SALE'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @HD_Hall1 AND start_time = DATEADD(hour, 10, @tomorrow));

    INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
    SELECT @HD_Hall1, @Movie2, DATEADD(hour, 15, @tomorrow), DATEADD(minute, 900 + 120, @tomorrow), 80000, 'ON_SALE'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @HD_Hall1 AND start_time = DATEADD(hour, 15, @tomorrow));

    -- Phòng 2
    IF @HD_Hall2 IS NOT NULL
    BEGIN
        INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
        SELECT @HD_Hall2, @Movie3, DATEADD(hour, 12, @tomorrow), DATEADD(minute, 720 + 120, @tomorrow), 120000, 'ON_SALE'
        WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @HD_Hall2 AND start_time = DATEADD(hour, 12, @tomorrow));

        INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
        SELECT @HD_Hall2, @Movie1, DATEADD(hour, 18, @tomorrow), DATEADD(minute, 1080 + 120, @tomorrow), 120000, 'ON_SALE'
        WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @HD_Hall2 AND start_time = DATEADD(hour, 18, @tomorrow));
    END
END

-- Chi nhánh Cầu Giấy - Ngày mai
IF @BranchCauGiay IS NOT NULL AND @Movie1 IS NOT NULL AND @CG_Hall1 IS NOT NULL
BEGIN
    -- Phòng 1
    INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
    SELECT @CG_Hall1, @Movie2, DATEADD(hour, 9, @tomorrow), DATEADD(minute, 540 + 120, @tomorrow), 85000, 'ON_SALE'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @CG_Hall1 AND start_time = DATEADD(hour, 9, @tomorrow));

    INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
    SELECT @CG_Hall1, @Movie1, DATEADD(hour, 14, @tomorrow), DATEADD(minute, 840 + 120, @tomorrow), 85000, 'ON_SALE'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @CG_Hall1 AND start_time = DATEADD(hour, 14, @tomorrow));

    INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
    SELECT @CG_Hall1, @Movie3, DATEADD(hour, 19, @tomorrow), DATEADD(minute, 1140 + 120, @tomorrow), 85000, 'ON_SALE'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @CG_Hall1 AND start_time = DATEADD(hour, 19, @tomorrow));

    -- Phòng 2
    IF @CG_Hall2 IS NOT NULL
    BEGIN
        INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
        SELECT @CG_Hall2, @Movie1, DATEADD(hour, 11, @tomorrow), DATEADD(minute, 660 + 120, @tomorrow), 130000, 'ON_SALE'
        WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @CG_Hall2 AND start_time = DATEADD(hour, 11, @tomorrow));

        INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
        SELECT @CG_Hall2, @Movie2, DATEADD(hour, 16, @tomorrow), DATEADD(minute, 960 + 120, @tomorrow), 130000, 'ON_SALE'
        WHERE NOT EXISTS (SELECT 1 FROM dbo.SHOWTIMES WHERE hall_id = @CG_Hall2 AND start_time = DATEADD(hour, 16, @tomorrow));
    END
END

PRINT N'ÄÃ£ thÃªm suáº¥t chiáº¿u cho cÃ¡c chi nhÃ¡nh (HÃ  ÄÃ´ng, Cáº§u Giáº¥y) thÃ nh cÃ´ng!';
GO
