-- ============================================================
-- SQL Script: Chèn hàng loạt Suất Chiếu mẫu cho dự án RapViet
-- Cấu hình số ngày và khoảng cách các khung giờ linh hoạt
-- ============================================================
USE RapVietDB;
GO

-- Xóa dữ liệu cũ nếu muốn làm sạch trước khi chèn mới (Tùy chọn, mặc định không chạy)
-- DELETE FROM dbo.SEAT_PRICING;
-- DELETE FROM dbo.SHOWTIMES;

DECLARE @StartDate DATE = CAST(GETDATE() AS DATE);
DECLARE @DaysToInsert INT = 30; -- Số ngày cần tạo suất chiếu (Ví dụ: 30 ngày tiếp theo)

PRINT N'Bắt đầu chuẩn bị dữ liệu tạo suất chiếu...';

-- 1. Sử dụng bảng tạm hoặc CTE để sinh chuỗi ngày
WITH DateSeries AS (
    SELECT 0 AS DayOffset
    UNION ALL
    SELECT DayOffset + 1
    FROM DateSeries
    WHERE DayOffset + 1 < @DaysToInsert
),
-- 2. Định nghĩa các khung giờ chiếu trong ngày (Tính bằng số phút từ 00:00)
-- 7 suất chiếu một ngày, cách nhau khoảng 2.5 tiếng để tránh đè lấn giờ chiếu
SlotSeries AS (
    SELECT SlotIndex, StartMinutes
    FROM (
        VALUES 
            (0, 510),   -- 08:30
            (1, 660),   -- 11:00
            (2, 810),   -- 13:30
            (3, 960),   -- 16:00
            (4, 1110),  -- 18:30
            (5, 1260),  -- 21:00
            (6, 1410)   -- 23:30
    ) AS s(SlotIndex, StartMinutes)
),
-- 3. Lấy danh sách các rạp và phòng chiếu hoạt động
ActiveHalls AS (
    SELECT id AS hall_id, hall_type, ROW_NUMBER() OVER (ORDER BY id) - 1 AS hall_idx
    FROM dbo.HALLS
    WHERE status = 'ACTIVE'
),
-- 4. Lấy danh sách phim đang chiếu
ActiveMovies AS (
    SELECT id AS movie_id, duration_min, title, ROW_NUMBER() OVER (ORDER BY id) - 1 AS movie_idx
    FROM dbo.MOVIES
    WHERE status = 'NOW_SHOWING'
),
-- 5. Đếm tổng số phim đang chiếu để làm công thức chia suất
MovieCount AS (
    SELECT COUNT(*) AS total_movies FROM ActiveMovies
),
-- 6. Tổ hợp dữ liệu suất chiếu dự kiến (không xung đột thời gian trong cùng một phòng)
ProposedShowtimes AS (
    SELECT 
        h.hall_id,
        m.movie_id,
        -- Giờ bắt đầu: Ngày hiện tại + offset ngày + số phút trong ngày
        DATEADD(MINUTE, s.StartMinutes, CAST(DATEADD(DAY, dt.DayOffset, @StartDate) AS DATETIME2)) AS start_time,
        -- Giờ kết thúc: Giờ bắt đầu + thời lượng phim + 15 phút dọn phòng
        DATEADD(MINUTE, s.StartMinutes + m.duration_min + 15, CAST(DATEADD(DAY, dt.DayOffset, @StartDate) AS DATETIME2)) AS end_time,
        -- Giá vé cơ bản tùy theo loại phòng chiếu
        CASE h.hall_type 
            WHEN 'IMAX' THEN 150000 
            WHEN 'VIP' THEN 120000 
            ELSE 80000 
        END AS base_price,
        'ON_SALE' AS status
    FROM ActiveHalls h
    CROSS JOIN DateSeries dt
    CROSS JOIN SlotSeries s
    CROSS JOIN MovieCount mc
    JOIN ActiveMovies m ON m.movie_idx = (h.hall_idx + s.SlotIndex + dt.DayOffset) % mc.total_movies
    WHERE mc.total_movies > 0
)
-- 7. Chèn dữ liệu vào bảng SHOWTIMES nếu chưa tồn tại suất chiếu cùng phòng vào đúng giờ đó
INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
SELECT ps.hall_id, ps.movie_id, ps.start_time, ps.end_time, ps.base_price, ps.status
FROM ProposedShowtimes ps
WHERE NOT EXISTS (
    SELECT 1 
    FROM dbo.SHOWTIMES st 
    WHERE st.hall_id = ps.hall_id 
      AND st.start_time = ps.start_time
)
OPTION (MAXRECURSION 32767); -- Cho phép đệ quy tạo chuỗi ngày dài (tối đa 32767 ngày)

-- 8. Tự động chèn bảng giá vé SEAT_PRICING cho các suất chiếu mới chèn ở trên (nếu chưa có)
-- Chèn mức giá riêng biệt cho STANDARD, VIP và COUPLE dựa theo giá vé cơ bản của suất chiếu
INSERT INTO dbo.SEAT_PRICING (showtime_id, seat_type, price)
SELECT 
    st.id AS showtime_id,
    type.seat_type,
    CASE type.seat_type
        WHEN 'STANDARD' THEN st.base_price
        WHEN 'VIP'      THEN st.base_price + 20000
        WHEN 'COUPLE'   THEN st.base_price * 1.8
    END AS price
FROM dbo.SHOWTIMES st
CROSS JOIN (
    VALUES ('STANDARD'), ('VIP'), ('COUPLE')
) AS type(seat_type)
WHERE NOT EXISTS (
    SELECT 1 
    FROM dbo.SEAT_PRICING sp 
    WHERE sp.showtime_id = st.id 
      AND sp.seat_type = type.seat_type
);

PRINT N'Hoàn thành tự động chèn dữ liệu suất chiếu và bảng giá chi tiết!';
GO
