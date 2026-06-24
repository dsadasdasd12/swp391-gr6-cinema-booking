-- ============================================================
-- RapViet Cinema - DỮ LIỆU MẪU cho module Duyệt phim
-- (Browse / Search / Filter / Xem chi tiết - UC06)
-- Chạy SAU khi đã chạy RapViet_v3.sql để tạo schema.
-- Dùng được nhiều lần: tự xóa dữ liệu mẫu cũ trước khi chèn.
-- Thời gian suất chiếu tính tương đối theo GETDATE() nên luôn ở tương lai.
-- ============================================================
USE RapVietDB;
GO

-- ── Dọn dữ liệu mẫu cũ (đúng thứ tự phụ thuộc) ──────────────
DELETE FROM dbo.REVIEWS;
DELETE FROM dbo.BOOKING_SEATS;
DELETE FROM dbo.PAYMENTS;
DELETE FROM dbo.BOOKINGS;
DELETE FROM dbo.SEAT_PRICING;
DELETE FROM dbo.SHOWTIMES;
DELETE FROM dbo.MOVIE_LANGUAGES;
DELETE FROM dbo.MOVIES_CATEGORY;
DELETE FROM dbo.SEATS;
DELETE FROM dbo.HALLS;
DELETE FROM dbo.STAFF_BRANCH;
DELETE FROM dbo.BRANCHES;
DELETE FROM dbo.CINEMA;
DELETE FROM dbo.MOVIES;
DELETE FROM dbo.LANGUAGES;
DELETE FROM dbo.CATEGORY;
-- USER là bảng dùng chung nhiều module: chỉ xóa các tài khoản demo của seed này.
DELETE FROM dbo.[USER] WHERE email IN ('a@demo.com','b@demo.com','c@demo.com');
GO

-- Reset bộ đếm IDENTITY để mỗi lần chạy lại seed cho ID ổn định (bắt đầu từ 1).
-- Lưu ý quirk của SQL Server: RESEED về 0 trên bảng MỚI TINH (chưa từng insert)
-- sẽ khiến dòng đầu tiên có id = 0. Vì vậy chỉ reseed khi bảng ĐÃ TỪNG có dữ liệu
-- (last_value khác NULL); bảng mới tinh để nguyên nên dòng đầu vẫn bằng 1.
-- (Không reseed dbo.[USER] vì bảng này có thể chứa dữ liệu của module khác.)
DECLARE @tbl SYSNAME, @sql NVARCHAR(200);
DECLARE tbl_cur CURSOR LOCAL FAST_FORWARD FOR
    SELECT name FROM (VALUES
        ('dbo.REVIEWS'),('dbo.BOOKINGS'),('dbo.SHOWTIMES'),('dbo.SEATS'),
        ('dbo.HALLS'),('dbo.BRANCHES'),('dbo.CINEMA'),('dbo.MOVIES'),
        ('dbo.LANGUAGES'),('dbo.CATEGORY')) AS t(name);
OPEN tbl_cur;
FETCH NEXT FROM tbl_cur INTO @tbl;
WHILE @@FETCH_STATUS = 0
BEGIN
    IF EXISTS (SELECT 1 FROM sys.identity_columns
               WHERE object_id = OBJECT_ID(@tbl) AND last_value IS NOT NULL)
    BEGIN
        SET @sql = N'DBCC CHECKIDENT(''' + @tbl + N''', RESEED, 0) WITH NO_INFOMSGS;';
        EXEC sp_executesql @sql;
    END
    FETCH NEXT FROM tbl_cur INTO @tbl;
END
CLOSE tbl_cur; DEALLOCATE tbl_cur;
GO

-- ── Thể loại (genre) ────────────────────────────────────────
INSERT INTO dbo.CATEGORY (name, description) VALUES
 (N'Hành động',    N'Phim hành động, võ thuật'),
 (N'Viễn tưởng',   N'Khoa học viễn tưởng'),
 (N'Kinh dị',      N'Phim kinh dị, giật gân'),
 (N'Hài',          N'Phim hài, giải trí'),
 (N'Tình cảm',     N'Phim tình cảm, lãng mạn'),
 (N'Hoạt hình',    N'Phim hoạt hình cho mọi lứa tuổi');
GO

-- ── Ngôn ngữ ────────────────────────────────────────────────
INSERT INTO dbo.LANGUAGES (name, code) VALUES
 (N'Tiếng Việt', 'VI'),
 (N'Tiếng Anh',  'EN'),
 (N'Tiếng Hàn',  'KO');
GO

-- ── Cụm rạp & chi nhánh ─────────────────────────────────────
INSERT INTO dbo.CINEMA (name, address, phone) VALUES
 (N'RapViet Cinema', N'Toà nhà RapViet, Hà Nội', '1900-6017');

DECLARE @cinemaId INT = (SELECT TOP 1 id FROM dbo.CINEMA ORDER BY id);

INSERT INTO dbo.BRANCHES (cinema_id, name, address, phone, open_time, close_time) VALUES
 (@cinemaId, N'RapViet Hà Đông',  N'Số 1 Quang Trung, Hà Đông, Hà Nội',   '024-1111', '08:00', '23:30'),
 (@cinemaId, N'RapViet Cầu Giấy', N'Số 2 Trần Thái Tông, Cầu Giấy, Hà Nội','024-2222', '09:00', '23:00');
GO

-- ── Phòng chiếu (mỗi chi nhánh vài phòng, đa dạng định dạng) ─
INSERT INTO dbo.HALLS (branch_id, name, total_seats, hall_type)
SELECT b.id, N'Phòng 1', 80, 'STANDARD' FROM dbo.BRANCHES b
UNION ALL SELECT b.id, N'Phòng 2 IMAX', 120, 'IMAX'    FROM dbo.BRANCHES b
UNION ALL SELECT b.id, N'Phòng 3 VIP',   40, 'VIP'     FROM dbo.BRANCHES b;
GO

-- ── Phim (đủ 3 trạng thái để demo bộ lọc) ───────────────────
INSERT INTO dbo.MOVIES (title, duration_min, description, release_date, status, poster_url, trailer_url, actor, director) VALUES
 (N'Vũ Trụ Song Song', 135, N'Một kỹ sư phát hiện cánh cổng dẫn tới các vũ trụ song song và phải ngăn chặn thảm hoạ dây chuyền.', '2026-05-10', 'NOW_SHOWING', 'https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', N'Minh Anh, Quốc Bảo', N'Trần Hùng'),
 (N'Lằn Ranh Sinh Tử', 118, N'Đặc nhiệm phải giải cứu con tin trong một toà nhà bị chiếm giữ.', '2026-05-20', 'NOW_SHOWING', NULL, 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', N'Hữu Long, Lan Phương', N'Phạm Tuấn'),
 (N'Ngôi Nhà Câm Lặng', 102, N'Gia đình trẻ chuyển tới ngôi nhà cổ và đối mặt thế lực vô hình.', '2026-04-28', 'NOW_SHOWING', NULL, NULL, N'Thu Hà, Đức Anh', N'Lê Vân'),
 (N'Tiếng Cười Mùa Hạ', 96, N'Nhóm bạn thân và mùa hè đáng nhớ với những tình huống dở khóc dở cười.', '2026-05-01', 'NOW_SHOWING', NULL, NULL, N'Gia Bảo, Mỹ Linh', N'Ngô Thanh'),
 (N'Hành Tinh Băng Giá', 110, N'Phi hành đoàn khám phá hành tinh băng và bí ẩn của nền văn minh đã mất.', '2026-06-15', 'COMING_SOON', NULL, 'https://youtu.be/aqz-KE-bpKQ', N'Bảo Nam, Hồng Nhung', N'Đỗ Quang'),
 (N'Trái Tim Mùa Đông', 124, N'Chuyện tình lãng mạn vượt thời gian giữa hai con người xa lạ.', '2026-07-01', 'COMING_SOON', NULL, NULL, N'Khánh Vy, Tuấn Kiệt', N'Vũ Hà'),
 (N'Thế Giới Kỳ Diệu', 88, N'Hành trình phiêu lưu của chú robot nhỏ đi tìm gia đình.', '2026-03-10', 'ENDED', NULL, NULL, N'Lồng tiếng Việt', N'Mai Phương'),
 (N'Bóng Đêm Trở Lại', 129, N'Thám tử lần theo dấu vết kẻ sát nhân hàng loạt trong thành phố.', '2026-02-14', 'ENDED', NULL, NULL, N'Anh Tú, Diễm My', N'Hoàng Sơn');
GO

-- ── Gán thể loại cho phim (tra theo tên để không phụ thuộc id) ─
INSERT INTO dbo.MOVIES_CATEGORY (movie_id, category_id)
SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name IN (N'Hành động', N'Viễn tưởng') WHERE m.title = N'Vũ Trụ Song Song'
UNION ALL SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name = N'Hành động'  WHERE m.title = N'Lằn Ranh Sinh Tử'
UNION ALL SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name = N'Kinh dị'    WHERE m.title = N'Ngôi Nhà Câm Lặng'
UNION ALL SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name = N'Hài'        WHERE m.title = N'Tiếng Cười Mùa Hạ'
UNION ALL SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name = N'Viễn tưởng' WHERE m.title = N'Hành Tinh Băng Giá'
UNION ALL SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name = N'Tình cảm'   WHERE m.title = N'Trái Tim Mùa Đông'
UNION ALL SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name = N'Hoạt hình'  WHERE m.title = N'Thế Giới Kỳ Diệu'
UNION ALL SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name IN (N'Kinh dị', N'Hành động') WHERE m.title = N'Bóng Đêm Trở Lại';
GO

-- ── Gán ngôn ngữ cho phim ───────────────────────────────────
INSERT INTO dbo.MOVIE_LANGUAGES (movie_id, language_id, subtitle)
SELECT m.id, l.id, 1 FROM dbo.MOVIES m JOIN dbo.LANGUAGES l ON l.code = 'EN'
   WHERE m.title IN (N'Vũ Trụ Song Song', N'Hành Tinh Băng Giá', N'Bóng Đêm Trở Lại')
UNION ALL
SELECT m.id, l.id, 0 FROM dbo.MOVIES m JOIN dbo.LANGUAGES l ON l.code = 'VI'
   WHERE m.title IN (N'Lằn Ranh Sinh Tử', N'Ngôi Nhà Câm Lặng', N'Tiếng Cười Mùa Hạ', N'Trái Tim Mùa Đông', N'Thế Giới Kỳ Diệu')
UNION ALL
SELECT m.id, l.id, 1 FROM dbo.MOVIES m JOIN dbo.LANGUAGES l ON l.code = 'KO'
   WHERE m.title = N'Trái Tim Mùa Đông';
GO

-- ── Suất chiếu cho các phim ĐANG CHIẾU ──────────────────────
-- Sinh nhiều suất ở các ngày/giờ tương lai cho mỗi phim đang chiếu.
DECLARE @now DATETIME2 = GETDATE();
INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
SELECT h.id,
       m.id,
       DATEADD(MINUTE, t.mins, DATEADD(DAY, d.day, CAST(CAST(@now AS DATE) AS DATETIME2))) AS start_time,
       DATEADD(MINUTE, t.mins + m.duration_min + 15, DATEADD(DAY, d.day, CAST(CAST(@now AS DATE) AS DATETIME2))) AS end_time,
       CASE h.hall_type WHEN 'IMAX' THEN 120000 WHEN 'VIP' THEN 100000 ELSE 75000 END,
       'ON_SALE'
FROM dbo.MOVIES m
JOIN dbo.HALLS  h ON 1 = 1
JOIN (VALUES (1),(2),(3)) AS d(day) ON 1 = 1                 -- 3 ngày tới
JOIN (VALUES (600),(900),(1200)) AS t(mins) ON 1 = 1         -- 10:00, 15:00, 20:00
WHERE m.status = 'NOW_SHOWING';
GO

-- ── Một ít người dùng + booking + đánh giá để demo điểm rating ─
-- Lưu ý: cột google_id có ràng buộc UNIQUE; SQL Server chỉ cho phép MỘT NULL,
-- nên mỗi user phải có google_id khác nhau (đặt giá trị placeholder cho seed).
INSERT INTO dbo.[USER] (full_name, email, password_hash, google_id, role, email_verified) VALUES
 (N'Nguyễn Văn A', 'a@demo.com', 'x', 'seed-google-a', 'CUSTOMER', 1),
 (N'Trần Thị B',   'b@demo.com', 'x', 'seed-google-b', 'CUSTOMER', 1),
 (N'Lê Văn C',     'c@demo.com', 'x', 'seed-google-c', 'CUSTOMER', 1);
GO

-- Mỗi đánh giá cần gắn với 1 booking (UNIQUE booking_id). Tạo booking tối thiểu
-- cho từng cặp (người dùng, suất chiếu) rồi chèn review tương ứng.
DECLARE @reviews TABLE (email VARCHAR(150), title NVARCHAR(200), rating TINYINT, cmt NVARCHAR(400));
INSERT INTO @reviews VALUES
 ('a@demo.com', N'Vũ Trụ Song Song',  5, N'Hình ảnh mãn nhãn, nội dung cuốn hút!'),
 ('b@demo.com', N'Vũ Trụ Song Song',  4, N'Hay nhưng hơi dài.'),
 ('c@demo.com', N'Lằn Ranh Sinh Tử',  4, N'Hành động dồn dập.'),
 ('a@demo.com', N'Ngôi Nhà Câm Lặng', 3, N'Khá sợ, âm thanh tốt.'),
 ('b@demo.com', N'Tiếng Cười Mùa Hạ', 5, N'Cười không ngậm được mồm.');

DECLARE @email VARCHAR(150), @title NVARCHAR(200), @rating TINYINT, @cmt NVARCHAR(400);
DECLARE cur CURSOR FOR SELECT email, title, rating, cmt FROM @reviews;
OPEN cur;
FETCH NEXT FROM cur INTO @email, @title, @rating, @cmt;
WHILE @@FETCH_STATUS = 0
BEGIN
    DECLARE @uid INT = (SELECT id FROM dbo.[USER] WHERE email = @email);
    DECLARE @mid INT = (SELECT id FROM dbo.MOVIES WHERE title = @title);
    DECLARE @sid INT = (SELECT TOP 1 id FROM dbo.SHOWTIMES WHERE movie_id = @mid ORDER BY start_time);
    IF @uid IS NOT NULL AND @sid IS NOT NULL
    BEGIN
        INSERT INTO dbo.BOOKINGS (user_id, showtime_id, source, status, total_price)
        VALUES (@uid, @sid, 'ONLINE', 'USED', 75000);
        DECLARE @bid INT = SCOPE_IDENTITY();
        INSERT INTO dbo.REVIEWS (user_id, movie_id, booking_id, rating, comment)
        VALUES (@uid, @mid, @bid, @rating, @cmt);
    END
    FETCH NEXT FROM cur INTO @email, @title, @rating, @cmt;
END
CLOSE cur; DEALLOCATE cur;
GO

PRINT N'Seed dữ liệu Duyệt phim hoàn tất.';
GO
