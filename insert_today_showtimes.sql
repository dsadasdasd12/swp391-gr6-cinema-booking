-- ============================================================
-- SQL Script: Chèn suất chiếu không trùng lịch cho 7 ngày tới
-- ============================================================
USE RapVietDB;
GO

DECLARE @today DATE = CAST(GETDATE() AS DATE);

-- Sử dụng CTE để lập lịch chiếu không xung đột (mỗi phòng chỉ chiếu tối đa 1 phim tại một khung giờ)
WITH MovieCTE AS (
    SELECT id, duration_min, ROW_NUMBER() OVER (ORDER BY id) - 1 AS movie_idx
    FROM dbo.MOVIES
    WHERE status = 'NOW_SHOWING'
),
HallCTE AS (
    SELECT id, hall_type, ROW_NUMBER() OVER (ORDER BY id) - 1 AS hall_idx
    FROM dbo.HALLS
    WHERE status = 'ACTIVE'
),
DateCTE AS (
    -- Chèn cho 8 ngày (từ ngày hôm nay đến 7 ngày tiếp theo)
    SELECT day_offset
    FROM (VALUES (0),(1),(2),(3),(4),(5),(6),(7)) AS d(day_offset)
),
SlotCTE AS (
    -- 5 khung giờ chiếu trong ngày
    SELECT slot_idx, mins
    FROM (
        VALUES 
            (0, 540),   -- 09:00
            (1, 720),   -- 12:00
            (2, 900),   -- 15:00
            (3, 1080),  -- 18:00
            (4, 1260)   -- 21:00
    ) AS t(slot_idx, mins)
),
MovieCount AS (
    SELECT COUNT(*) AS total_movies FROM MovieCTE
)
INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
SELECT 
    h.id AS hall_id,
    m.id AS movie_id,
    -- Giờ bắt đầu: Ngày hiện tại + offset ngày + số phút trong ngày
    DATEADD(MINUTE, s.mins, CAST(DATEADD(DAY, dt.day_offset, @today) AS DATETIME2)) AS start_time,
    -- Giờ kết thúc: Giờ bắt đầu + thời lượng phim + 15 phút dọn dẹp
    DATEADD(MINUTE, s.mins + m.duration_min + 15, CAST(DATEADD(DAY, dt.day_offset, @today) AS DATETIME2)) AS end_time,
    -- Giá vé tùy theo loại phòng chiếu
    CASE h.hall_type 
        WHEN 'IMAX' THEN 150000 
        WHEN 'VIP' THEN 120000 
        ELSE 80000 
    END AS base_price,
    'ON_SALE' AS status
FROM HallCTE h
CROSS JOIN DateCTE dt
CROSS JOIN SlotCTE s
CROSS JOIN MovieCount mc
JOIN MovieCTE m ON m.movie_idx = (h.hall_idx + s.slot_idx + dt.day_offset) % mc.total_movies
WHERE mc.total_movies > 0
  -- Tránh trùng lặp nếu chạy lại script nhiều lần
  AND NOT EXISTS (
      SELECT 1 
      FROM dbo.SHOWTIMES st 
      WHERE st.hall_id = h.id 
        AND st.start_time = DATEADD(MINUTE, s.mins, CAST(DATEADD(DAY, dt.day_offset, @today) AS DATETIME2))
  );

PRINT N'Đã tự động chèn suất chiếu mẫu không xung đột cho 7 ngày tới thành công!';
GO
