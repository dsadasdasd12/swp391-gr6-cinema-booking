USE master;
GO

USE RapVietDB;
GO

SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
SET QUOTED_IDENTIFIER ON;
GO

PRINT '==> Disabling check constraints for safe truncating...';
EXEC sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL';

PRINT '==> Emptying all tables...';
DELETE FROM dbo.ATTENDANCE;
DELETE FROM dbo.COUNTER_DISCOUNTS;
DELETE FROM dbo.BOOKING_FNB;
DELETE FROM dbo.FNB_ITEMS;
DELETE FROM dbo.REVIEWS;
DELETE FROM dbo.NOTIFICATIONS;
DELETE FROM dbo.PAYMENTS;
DELETE FROM dbo.BOOKING_SEATS;
DELETE FROM dbo.BOOKINGS;
DELETE FROM dbo.CART_ITEMS;
DELETE FROM dbo.CART;
DELETE FROM dbo.FAVORITE_MOVIES;
DELETE FROM dbo.SEAT_PRICING;
DELETE FROM dbo.SEATS;
DELETE FROM dbo.SHOWTIMES;
DELETE FROM dbo.HALLS;
DELETE FROM dbo.STAFF_BRANCH;
DELETE FROM dbo.BRANCHES;
DELETE FROM dbo.CINEMA;
DELETE FROM dbo.MOVIE_LANGUAGES;
DELETE FROM dbo.MOVIES_CATEGORY;
DELETE FROM dbo.MOVIES;
DELETE FROM dbo.LANGUAGES;
DELETE FROM dbo.CATEGORY;
DELETE FROM dbo.[USER];

PRINT '==> Re-enabling check constraints...';
EXEC sp_MSforeachtable 'ALTER TABLE ? WITH CHECK CHECK CONSTRAINT ALL';

-- Resetting Identity columns
PRINT '==> Resetting identity seeds...';
DBCC CHECKIDENT ('dbo.CINEMA', RESEED, 0);
DBCC CHECKIDENT ('dbo.BRANCHES', RESEED, 0);
DBCC CHECKIDENT ('dbo.CATEGORY', RESEED, 0);
DBCC CHECKIDENT ('dbo.LANGUAGES', RESEED, 0);
DBCC CHECKIDENT ('dbo.MOVIES', RESEED, 0);
DBCC CHECKIDENT ('dbo.[USER]', RESEED, 0);
DBCC CHECKIDENT ('dbo.HALLS', RESEED, 0);
DBCC CHECKIDENT ('dbo.SEATS', RESEED, 0);
DBCC CHECKIDENT ('dbo.SHOWTIMES', RESEED, 0);
DBCC CHECKIDENT ('dbo.BOOKINGS', RESEED, 0);
DBCC CHECKIDENT ('dbo.BOOKING_SEATS', RESEED, 0);
DBCC CHECKIDENT ('dbo.PAYMENTS', RESEED, 0);
GO

-- 1. Insert CINEMA
PRINT '==> Seeding CINEMA...';
SET IDENTITY_INSERT dbo.CINEMA ON;
INSERT INTO dbo.CINEMA (id, name, address, phone, logo_url, status)
VALUES (1, N'Hệ thống Rạp Việt', N'Hà Nội, Việt Nam', '1900.1000', '/assets/img/logo.png', 'ACTIVE');
SET IDENTITY_INSERT dbo.CINEMA OFF;

-- 2. Insert BRANCHES
PRINT '==> Seeding BRANCHES...';
SET IDENTITY_INSERT dbo.BRANCHES ON;
INSERT INTO dbo.BRANCHES (id, cinema_id, name, address, phone, open_time, close_time, status) VALUES
(1, 1, N'Rạp Việt Nguyễn Trãi', N'Số 266 Đường Nguyễn Trãi, Thanh Xuân, Hà Nội', '0243.888.999', '08:00:00', '23:30:00', 'ACTIVE'),
(2, 1, N'Rạp Việt Nguyễn Du', N'Số 116 Đường Nguyễn Du, Quận 1, TP. Hồ Chí Minh', '0283.777.666', '08:30:00', '23:45:00', 'ACTIVE'),
(3, 1, N'Rạp Việt Lê Lợi', N'Số 88 Đường Lê Lợi, Hải Châu, Đà Nẵng', '0236.555.444', '09:00:00', '23:00:00', 'ACTIVE');
SET IDENTITY_INSERT dbo.BRANCHES OFF;

-- 3. Insert CATEGORY
PRINT '==> Seeding CATEGORY...';
SET IDENTITY_INSERT dbo.CATEGORY ON;
INSERT INTO dbo.CATEGORY (id, name, description, status) VALUES
(1, N'Hành Động', N'Phim hành động kịch tính', 'ACTIVE'),
(2, N'Hài Hước', N'Phim hài vui nhộn', 'ACTIVE'),
(3, N'Tình Cảm', N'Phim tình cảm lãng mạn', 'ACTIVE'),
(4, N'Kinh Dị', N'Phim kinh dị rùng rợn', 'ACTIVE'),
(5, N'Hoạt Hình', N'Phim hoạt hình phiêu lưu', 'ACTIVE');
SET IDENTITY_INSERT dbo.CATEGORY OFF;

-- 4. Insert LANGUAGES
PRINT '==> Seeding LANGUAGES...';
SET IDENTITY_INSERT dbo.LANGUAGES ON;
INSERT INTO dbo.LANGUAGES (id, name, code, status) VALUES
(1, N'Tiếng Việt (Lồng tiếng)', 'VI', 'ACTIVE'),
(2, N'Tiếng Anh (Phụ đề Việt)', 'EN', 'ACTIVE'),
(3, N'Tiếng Hàn (Phụ đề Việt)', 'KO', 'ACTIVE');
SET IDENTITY_INSERT dbo.LANGUAGES OFF;

-- 5. Insert MOVIES
PRINT '==> Seeding MOVIES...';
SET IDENTITY_INSERT dbo.MOVIES ON;
INSERT INTO dbo.MOVIES (id, title, duration_min, description, release_date, status, poster_url, trailer_url, actor, director) VALUES
(1, N'Lật Mặt 7: Một Điều Ước', 138, N'Bộ phim gia đình đầy cảm xúc của Lý Hải kể về câu chuyện của người mẹ tảo tần và các con.', '2026-04-30', 'NOW_SHOWING', 'assets/uploads/movies/1/poster.webp', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Thanh Hiền, Trương Minh Cường, Đinh Y Nhung', N'Lý Hải'),
(2, N'Mai', 131, N'Mai là câu chuyện tình cảm trắc trở của một người phụ nữ ngoài ba mươi phải gánh chịu nhiều bất công cuộc sống.', '2026-02-10', 'NOW_SHOWING', 'assets/uploads/movies/2/poster.webp', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Phương Anh Đào, Tuấn Trần, Hồng Đào', N'Trấn Thành'),
(3, N'Dune: Hành Tinh Cát 2', 166, N'Phần tiếp theo của siêu phẩm sử thi viễn tưởng kể về hành trình phục hận của Paul Atreides.', '2026-03-01', 'NOW_SHOWING', 'assets/uploads/movies/3/poster.webp', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Timothée Chalamet, Zendaya, Rebecca Ferguson', N'Denis Villeneuve'),
(4, N'Inside Out 2', 96, N'Những cảm xúc mới đầy thú vị xuất hiện bên trong đầu của Riley khi bước vào tuổi dậy thì.', '2026-06-15', 'COMING_SOON', 'assets/uploads/movies/4/poster.webp', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Amy Poehler, Phyllis Smith, Lewis Black', N'Kelsey Mann');
SET IDENTITY_INSERT dbo.MOVIES OFF;

INSERT INTO dbo.MOVIES_CATEGORY (movie_id, category_id) VALUES (1, 3), (2, 3), (3, 1), (4, 5);
INSERT INTO dbo.MOVIE_LANGUAGES (movie_id, language_id, subtitle) VALUES (1, 1, 0), (2, 1, 0), (3, 2, 1), (4, 1, 0);

-- 6. Insert Users
PRINT '==> Seeding USERS (password plain text when login: 123)...';
SET IDENTITY_INSERT dbo.[USER] ON;
INSERT INTO dbo.[USER] (id, full_name, email, password_hash, phone, role, active, email_verified) VALUES
(1, N'Quản trị viên Hệ thống', 'admin@rapviet.vn', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998b83769ca6ca4aa', '0987.654.321', 'ADMIN', 1, 1),
(2, N'Trưởng Chi Nhánh Hà Nội', 'manager@rapviet.vn', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998b83769ca6ca4aa', '0912.345.678', 'MANAGER', 1, 1),
(3, N'Nhân Viên Quầy HN', 'staff@rapviet.vn', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998b83769ca6ca4aa', '0966.777.888', 'STAFF', 1, 1),
(4, N'Phạm Minh Hoàng', 'customer@gmail.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998b83769ca6ca4aa', '0944.555.666', 'CUSTOMER', 1, 1),
(5, N'Lê Thu Trang', 'tranglt@gmail.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998b83769ca6ca4aa', '0933.222.111', 'CUSTOMER', 1, 1),
(6, N'Nguyễn Tuấn Anh', 'tuananh@gmail.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998b83769ca6ca4aa', '0900.111.222', 'CUSTOMER', 0, 1),
(7, N'Trần Quốc Bảo', 'baotq@gmail.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998b83769ca6ca4aa', '0988.999.000', 'CUSTOMER', 1, 0);
SET IDENTITY_INSERT dbo.[USER] OFF;

INSERT INTO dbo.STAFF_BRANCH (user_id, branch_id, position) VALUES 
(2, 1, N'TRƯỞNG CHI NHÁNH'),
(3, 1, N'NHÂN VIÊN QUẦY');

-- 7. Insert Halls
PRINT '==> Seeding HALLS...';
SET IDENTITY_INSERT dbo.HALLS ON;
INSERT INTO dbo.HALLS (id, branch_id, name, total_seats, hall_type, status) VALUES
(1, 1, N'Phòng Chiếu VIP 1', 50, 'VIP', 'ACTIVE'),
(2, 1, N'Phòng Chiếu IMAX 2', 100, 'IMAX', 'ACTIVE'),
(3, 2, N'Phòng Chiếu VIP 1', 50, 'VIP', 'ACTIVE');
SET IDENTITY_INSERT dbo.HALLS OFF;

-- 8. Insert Seats
PRINT '==> Seeding SEATS (A1-J10 for Halls)...';
DECLARE @r INT = 1;
DECLARE @c INT = 1;
DECLARE @row_char VARCHAR(1);

-- Hall 1: 50 seats (A-E, 1-10)
WHILE @r <= 5
BEGIN
    SET @row_char = SUBSTRING('ABCDE', @r, 1);
    SET @c = 1;
    WHILE @c <= 10
    BEGIN
        INSERT INTO dbo.SEATS (hall_id, seat_row, seat_number, seat_type, maintenance)
        VALUES (1, @row_char, @c, CASE WHEN @r = 5 THEN 'COUPLE' WHEN @r >= 3 THEN 'VIP' ELSE 'STANDARD' END, 0);
        SET @c = @c + 1;
    END
    SET @r = @r + 1;
END;

-- Hall 2: 100 seats (A-J, 1-10)
SET @r = 1;
WHILE @r <= 10
BEGIN
    SET @row_char = SUBSTRING('ABCDEFGHIJ', @r, 1);
    SET @c = 1;
    WHILE @c <= 10
    BEGIN
        INSERT INTO dbo.SEATS (hall_id, seat_row, seat_number, seat_type, maintenance)
        VALUES (2, @row_char, @c, CASE WHEN @r = 10 THEN 'COUPLE' WHEN @r >= 7 THEN 'VIP' ELSE 'STANDARD' END, 0);
        SET @c = @c + 1;
    END
    SET @r = @r + 1;
END;

-- Hall 3: 50 seats (A-E, 1-10)
SET @r = 1;
WHILE @r <= 5
BEGIN
    SET @row_char = SUBSTRING('ABCDE', @r, 1);
    SET @c = 1;
    WHILE @c <= 10
    BEGIN
        INSERT INTO dbo.SEATS (hall_id, seat_row, seat_number, seat_type, maintenance)
        VALUES (3, @row_char, @c, CASE WHEN @r = 5 THEN 'COUPLE' WHEN @r >= 3 THEN 'VIP' ELSE 'STANDARD' END, 0);
        SET @c = @c + 1;
    END
    SET @r = @r + 1;
END;

-- 9. Insert Showtimes and Bookings dynamically using T-SQL loop for last 30 days
PRINT '==> Generating 30 days of showtimes, bookings, and payments...';
DECLARE @day INT = -30;
DECLARE @showtime_id INT = 1;
DECLARE @date_str VARCHAR(10);
DECLARE @start_dt DATETIME2;
DECLARE @end_dt DATETIME2;

-- Keep variables for dynamically mapping seat IDs
DECLARE @seat1 INT, @seat2 INT, @seat3 INT, @seat4 INT, @seat5 INT, @seat6 INT;
SELECT @seat1 = MIN(id) FROM dbo.SEATS WHERE hall_id = 1;
SELECT @seat2 = MIN(id) + 1 FROM dbo.SEATS WHERE hall_id = 1;
SELECT @seat3 = MIN(id) FROM dbo.SEATS WHERE hall_id = 2;
SELECT @seat4 = MIN(id) + 1 FROM dbo.SEATS WHERE hall_id = 2;
SELECT @seat5 = MIN(id) FROM dbo.SEATS WHERE hall_id = 3;
SELECT @seat6 = MIN(id) + 1 FROM dbo.SEATS WHERE hall_id = 3;

WHILE @day <= 3
BEGIN
    SET @date_str = CONVERT(VARCHAR(10), DATEADD(day, @day, GETDATE()), 120);
    
    -- Showtime 1: Movie 1, Hall 1, base price 120000, 10:00
    SET @start_dt = CAST(@date_str + ' 10:00:00' AS DATETIME2);
    SET @end_dt = DATEADD(minute, 138, @start_dt);
    
    SET IDENTITY_INSERT dbo.SHOWTIMES ON;
    INSERT INTO dbo.SHOWTIMES (id, hall_id, movie_id, start_time, end_time, base_price, status)
    VALUES (@showtime_id, 1, 1, @start_dt, @end_dt, 120000, 'ON_SALE');
    SET IDENTITY_INSERT dbo.SHOWTIMES OFF;
    
    -- Booking 1: User 4, 2 tickets
    INSERT INTO dbo.BOOKINGS (user_id, showtime_id, source, status, total_price, qr_code, booked_at)
    VALUES (4, @showtime_id, 'ONLINE', 'CONFIRMED', 240000, 'iVBORw0KGgoAAAANSUhEUgAAAIAAAACAAQMAAAD58NuIAAAABlBMVEUAAAD///+l2Z/dAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAFnRFWHRDcmVhdGlvbiBUaW1lADA4LzE1LzExN6beNtwAAAAHdElNRQfhCBMDFTcjX61SAAAAPklEQVRIx2NgGAWjYBSMglEwCkbBKBgFo2AUjIJRMApGwSgYBaNgFIyCUTAKRsEoGAWjYBSMglEwCkbBKBgFAAD//wADAAHaS2D8AAAAAElFTkSuQmCC', DATEADD(hour, -2, @start_dt));
    DECLARE @bk_id1 INT = SCOPE_IDENTITY();
    
    INSERT INTO dbo.BOOKING_SEATS (booking_id, seat_id, price) VALUES 
    (@bk_id1, @seat1, 120000),
    (@bk_id1, @seat2, 120000);
    
    INSERT INTO dbo.PAYMENTS (booking_id, type, method, status, amount, paid_at)
    VALUES (@bk_id1, 'ONLINE', 'VNPAY', 'SUCCESS', 240000, DATEADD(hour, -2, @start_dt));
    
    -- Booking 2: User 5, 1 ticket
    INSERT INTO dbo.BOOKINGS (user_id, showtime_id, source, status, total_price, qr_code, booked_at)
    VALUES (5, @showtime_id, 'WALKIN', 'CONFIRMED', 120000, 'iVBORw0KGgoAAAANSUhEUgAAAIAAAACAAQMAAAD58NuIAAAABlBMVEUAAAD///+l2Z/dAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAFnRFWHRDcmVhdGlvbiBUaW1lADA4LzE1LzExN6beNtwAAAAHdElNRQfhCBMDFTcjX61SAAAAPklEQVRIx2NgGAWjYBSMglEwCkbBKBgFo2AUjIJRMApGwSgYBaNgFIyCUTAKRsEoGAWjYBSMglEwCkbBKBgFAAD//wADAAHaS2D8AAAAAElFTkSuQmCC', DATEADD(minute, -30, @start_dt));
    DECLARE @bk_id2 INT = SCOPE_IDENTITY();
    
    INSERT INTO dbo.BOOKING_SEATS (booking_id, seat_id, price) VALUES 
    (@bk_id2, @seat2 + 1, 120000);
    
    INSERT INTO dbo.PAYMENTS (booking_id, type, method, status, amount, paid_at)
    VALUES (@bk_id2, 'CASH', 'CASH', 'SUCCESS', 120000, DATEADD(minute, -30, @start_dt));

    SET @showtime_id = @showtime_id + 1;
    
    -- Showtime 2: Movie 2, Hall 2, base price 110000, 14:00
    SET @start_dt = CAST(@date_str + ' 14:00:00' AS DATETIME2);
    SET @end_dt = DATEADD(minute, 131, @start_dt);
    
    SET IDENTITY_INSERT dbo.SHOWTIMES ON;
    INSERT INTO dbo.SHOWTIMES (id, hall_id, movie_id, start_time, end_time, base_price, status)
    VALUES (@showtime_id, 2, 2, @start_dt, @end_dt, 110000, 'ON_SALE');
    SET IDENTITY_INSERT dbo.SHOWTIMES OFF;
    
    -- Booking for Showtime 2
    INSERT INTO dbo.BOOKINGS (user_id, showtime_id, source, status, total_price, qr_code, booked_at)
    VALUES (4, @showtime_id, 'ONLINE', 'CONFIRMED', 220000, 'iVBORw0KGgoAAAANSUhEUgAAAIAAAACAAQMAAAD58NuIAAAABlBMVEUAAAD///+l2Z/dAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAFnRFWHRDcmVhdGlvbiBUaW1lADA4LzE1LzExN6beNtwAAAAHdElNRQfhCBMDFTcjX61SAAAAPklEQVRIx2NgGAWjYBSMglEwCkbBKBgFo2AUjIJRMApGwSgYBaNgFIyCUTAKRsEoGAWjYBSMglEwCkbBKBgFAAD//wADAAHaS2D8AAAAAElFTkSuQmCC', DATEADD(hour, -3, @start_dt));
    DECLARE @bk_id3 INT = SCOPE_IDENTITY();
    
    INSERT INTO dbo.BOOKING_SEATS (booking_id, seat_id, price) VALUES 
    (@bk_id3, @seat3, 110000),
    (@bk_id3, @seat4, 110000);
    
    INSERT INTO dbo.PAYMENTS (booking_id, type, method, status, amount, paid_at)
    VALUES (@bk_id3, 'ONLINE', 'MOMO', 'SUCCESS', 220000, DATEADD(hour, -3, @start_dt));

    SET @showtime_id = @showtime_id + 1;

    -- Showtime 3: Movie 3, Hall 3, base price 150000, 19:00
    SET @start_dt = CAST(@date_str + ' 19:00:00' AS DATETIME2);
    SET @end_dt = DATEADD(minute, 166, @start_dt);
    
    SET IDENTITY_INSERT dbo.SHOWTIMES ON;
    INSERT INTO dbo.SHOWTIMES (id, hall_id, movie_id, start_time, end_time, base_price, status)
    VALUES (@showtime_id, 3, 3, @start_dt, @end_dt, 150000, 'ON_SALE');
    SET IDENTITY_INSERT dbo.SHOWTIMES OFF;
    
    -- Booking for Showtime 3
    INSERT INTO dbo.BOOKINGS (user_id, showtime_id, source, status, total_price, qr_code, booked_at)
    VALUES (5, @showtime_id, 'ONLINE', 'CHECKED_IN', 300000, 'iVBORw0KGgoAAAANSUhEUgAAAIAAAACAAQMAAAD58NuIAAAABlBMVEUAAAD///+l2Z/dAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAFnRFWHRDcmVhdGlvbiBUaW1lADA4LzE1LzExN6beNtwAAAAHdElNRQfhCBMDFTcjX61SAAAAPklEQVRIx2NgGAWjYBSMglEwCkbBKBgFo2AUjIJRMApGwSgYBaNgFIyCUTAKRsEoGAWjYBSMglEwCkbBKBgFAAD//wADAAHaS2D8AAAAAElFTkSuQmCC', DATEADD(hour, -1, @start_dt));
    DECLARE @bk_id4 INT = SCOPE_IDENTITY();
    
    INSERT INTO dbo.BOOKING_SEATS (booking_id, seat_id, price) VALUES 
    (@bk_id4, @seat5, 150000),
    (@bk_id4, @seat6, 150000);
    
    INSERT INTO dbo.PAYMENTS (booking_id, type, method, status, amount, paid_at)
    VALUES (@bk_id4, 'ONLINE', 'VNPAY', 'SUCCESS', 300000, DATEADD(hour, -1, @start_dt));

    SET @showtime_id = @showtime_id + 1;
    
    SET @day = @day + 1;
END;
GO

-- 10. Sample notifications (UC 2.4.x)
PRINT '==> Seeding NOTIFICATIONS...';
INSERT INTO dbo.NOTIFICATIONS (user_id, branch_id, sent_by, type, title, message, status) VALUES
(4, NULL, 1, 'BOOKING_CONFIRM', N'Booking #1 — Lật Mặt 7: Một Điều Ước', N'Gửi tới: customer@gmail.com', 'SENT'),
(4, NULL, 1, 'PAYMENT_CONFIRM', N'Booking #1 — Thanh toán 240000 VNĐ', N'Thanh toán VNPAY thành công', 'SENT'),
(5, NULL, 1, 'BOOKING_CONFIRM', N'Booking #2 — Mai', N'Gửi tới: tranglt@gmail.com', 'SENT'),
(NULL, NULL, 1, 'PROMOTION', N'Ưu đãi cuối tuần — Giảm 20% combo bắp nước', N'Gửi broadcast tới khách hàng active', 'SENT'),
(4, NULL, 1, 'SYSTEM', N'Nhắc suất chiếu sắp bắt đầu', N'SMTP chưa cấu hình — email đang chờ [retry=1]', 'FAILED');

PRINT '==> Database seeded successfully with showtimes, bookings & notifications.';
