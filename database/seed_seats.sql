-- ============================================================
-- RapViet Cinema - Seed GHẾ + một booking mẫu (Group6 - Huy)
-- Module: View seat availability (sơ đồ ghế của suất chiếu)
--
-- Chạy SAU khi đã chạy RapViet_v3.sql và seed_movie_browsing.sql.
-- Dùng được nhiều lần (idempotent): chỉ sinh ghế cho phòng nào CHƯA có ghế,
-- và chỉ tạo booking mẫu khi suất chiếu đó chưa có vé nào.
-- ============================================================
USE RapVietDB;
GO

-- ── 1) Sinh ghế cho mỗi phòng chiếu (10 ghế / hàng: A, B, C, ...) ─
-- Số hàng = ceil(total_seats / 10). Quy ước loại ghế:
--   - Phòng VIP            -> toàn bộ ghế VIP
--   - Phòng khác, hàng cuối -> ghế đôi (COUPLE), còn lại STANDARD
-- Rải vài ghế bảo trì để minh hoạ trạng thái "Bảo trì".
;WITH nums AS (
    SELECT TOP (300) ROW_NUMBER() OVER (ORDER BY (SELECT 1)) AS n
    FROM sys.all_objects
)
INSERT INTO dbo.SEATS (hall_id, seat_row, seat_number, seat_type, maintenance)
SELECT h.id,
       CHAR(65 + ((nums.n - 1) / 10))               AS seat_row,      -- A, B, C, ...
       ((nums.n - 1) % 10) + 1                       AS seat_number,   -- 1..10 mỗi hàng
       CASE
           WHEN h.hall_type = 'VIP' THEN 'VIP'
           WHEN ((nums.n - 1) / 10) = ((h.total_seats - 1) / 10) THEN 'COUPLE'  -- hàng cuối
           ELSE 'STANDARD'
       END                                           AS seat_type,
       CASE WHEN nums.n % 23 = 0 THEN 1 ELSE 0 END   AS maintenance    -- vài ghế bảo trì
FROM dbo.HALLS h
JOIN nums ON nums.n <= h.total_seats
WHERE NOT EXISTS (SELECT 1 FROM dbo.SEATS s WHERE s.hall_id = h.id);   -- chỉ phòng chưa có ghế
GO

-- ── 2) Đặt sẵn vài ghế cho suất chiếu sớm nhất (minh hoạ "Đã đặt") ─
DECLARE @sid INT = (SELECT TOP 1 id FROM dbo.SHOWTIMES ORDER BY start_time);
DECLARE @uid INT = (SELECT TOP 1 id FROM dbo.[USER] WHERE email = 'a@demo.com');

IF @sid IS NOT NULL AND @uid IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.BOOKINGS WHERE showtime_id = @sid AND status <> 'CANCELLED')
BEGIN
    DECLARE @hall INT = (SELECT hall_id FROM dbo.SHOWTIMES WHERE id = @sid);

    -- Tạo 1 vé đã xác nhận
    INSERT INTO dbo.BOOKINGS (user_id, showtime_id, source, status, total_price)
    VALUES (@uid, @sid, 'ONLINE', 'CONFIRMED', 0);
    DECLARE @bid INT = SCOPE_IDENTITY();

    -- Gắn 6 ghế trống đầu tiên của phòng vào vé này
    INSERT INTO dbo.BOOKING_SEATS (booking_id, seat_id, price)
    SELECT TOP 6 @bid, s.id, 75000
    FROM dbo.SEATS s
    WHERE s.hall_id = @hall AND s.maintenance = 0
    ORDER BY s.seat_row, s.seat_number;

    -- Cập nhật tổng tiền cho khớp số ghế đã đặt
    UPDATE dbo.BOOKINGS
    SET total_price = (SELECT SUM(price) FROM dbo.BOOKING_SEATS WHERE booking_id = @bid)
    WHERE id = @bid;
END
GO

PRINT N'Seed ghế hoàn tất.';
GO
