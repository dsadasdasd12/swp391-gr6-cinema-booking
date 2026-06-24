-- ============================================================
-- SQL Script: Chèn thêm Phim và Suất Chiếu mẫu vào CSDL RapVietDB
-- ============================================================
USE RapVietDB;
GO

-- 1. Chèn thêm thể loại nếu chưa có
IF NOT EXISTS (SELECT 1 FROM dbo.CATEGORY WHERE name = N'Kịch tính')
    INSERT INTO dbo.CATEGORY (name, description) VALUES (N'Kịch tính', N'Phim chính kịch, kịch tính');
IF NOT EXISTS (SELECT 1 FROM dbo.CATEGORY WHERE name = N'Gia đình')
    INSERT INTO dbo.CATEGORY (name, description) VALUES (N'Gia đình', N'Phim dành cho gia đình');
GO

-- 2. Chèn các bộ phim mới
-- Trạng thái: 'NOW_SHOWING' hoặc 'COMING_SOON'
INSERT INTO dbo.MOVIES (title, duration_min, description, release_date, status, poster_url, trailer_url, actor, director) VALUES
 (N'Lật Mặt 7: Một Điều Ước', 138, N'Câu chuyện cảm động về tình mẫu tử của người mẹ già cùng 5 người con lớn khôn đi làm ăn xa.', '2026-04-26', 'NOW_SHOWING', 'https://image.tmdb.org/t/p/w500/lFM16f5q474u.jpg', 'https://www.youtube.com/watch?v=kYJqD9XFwN0', N'Thanh Hằng, Trương Minh Cường, Đinh Y Nhung', N'Lý Hải'),
 (N'Ký Sinh Trùng (Parasite)', 132, N'Một gia đình nghèo tìm cách thâm nhập vào cuộc sống của một gia đình giàu có thông qua các vai trò gia sư, người giúp việc.', '2026-05-15', 'NOW_SHOWING', 'https://image.tmdb.org/t/p/w500/7IiTTjV77u1cOL5C5gBR864g8jF.jpg', 'https://www.youtube.com/watch?v=isOGD_7hNIY', N'Song Kang-ho, Lee Sun-kyun, Cho Yeo-jeong', N'Bong Joon Ho'),
 (N'Avengers: Hồi Kết', 181, N'Trận chiến cuối cùng của các siêu anh hùng nhằm khôi phục lại một nửa vũ trụ sau cái búng tay của Thanos.', '2026-05-25', 'NOW_SHOWING', 'https://image.tmdb.org/t/p/w500/or06509k52gBi3LzHzHi6SL4Ftf.jpg', 'https://www.youtube.com/watch?v=TcMBFSGVi1c', N'Robert Downey Jr., Chris Evans, Mark Ruffalo', N'Anthony Russo, Joe Russo'),
 (N'Đoraemon: Bản Tình Ca Đất Nước', 115, N'Nobita và những người bạn cùng chú mèo máy Đoraemon bước vào cuộc hành trình âm nhạc kỳ diệu để cứu thế giới khỏi nguy hiểm.', '2026-06-20', 'COMING_SOON', 'https://image.tmdb.org/t/p/w500/doraemon.jpg', 'https://www.youtube.com/watch?v=doraemon', N'Lồng tiếng Việt, Wasabi Mizuta', N'Kazuaki Imai'),
 (N'Kẻ Hủy Diệt 2', 137, N'Kẻ hủy diệt được tái lập trình bảo vệ John Connor khỏi phiên bản T-1000 nâng cao.', '2026-06-30', 'COMING_SOON', 'https://image.tmdb.org/t/p/w500/terminator2.jpg', 'https://www.youtube.com/watch?v=terminator2', N'Arnold Schwarzenegger, Linda Hamilton', N'James Cameron');
GO

-- 3. Gán thể loại cho các phim mới
INSERT INTO dbo.MOVIES_CATEGORY (movie_id, category_id)
SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name IN (N'Gia đình', N'Tình cảm') WHERE m.title = N'Lật Mặt 7: Một Điều Ước'
UNION ALL SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name IN (N'Kịch tính', N'Hài') WHERE m.title = N'Ký Sinh Trùng (Parasite)'
UNION ALL SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name IN (N'Hành động', N'Viễn tưởng') WHERE m.title = N'Avengers: Hồi Kết'
UNION ALL SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name IN (N'Hoạt hình', N'Gia đình') WHERE m.title = N'Đoraemon: Bản Tình Ca Đất Nước'
UNION ALL SELECT m.id, c.id FROM dbo.MOVIES m JOIN dbo.CATEGORY c ON c.name IN (N'Hành động', N'Viễn tưởng') WHERE m.title = N'Kẻ Hủy Diệt 2';
GO

-- 4. Gán ngôn ngữ cho các phim mới
INSERT INTO dbo.MOVIE_LANGUAGES (movie_id, language_id, subtitle)
SELECT m.id, l.id, 0 FROM dbo.MOVIES m JOIN dbo.LANGUAGES l ON l.code = 'VI' WHERE m.title = N'Lật Mặt 7: Một Điều Ước'
UNION ALL SELECT m.id, l.id, 1 FROM dbo.MOVIES m JOIN dbo.LANGUAGES l ON l.code = 'KO' WHERE m.title = N'Ký Sinh Trùng (Parasite)'
UNION ALL SELECT m.id, l.id, 1 FROM dbo.MOVIES m JOIN dbo.LANGUAGES l ON l.code = 'EN' WHERE m.title = N'Avengers: Hồi Kết'
UNION ALL SELECT m.id, l.id, 0 FROM dbo.MOVIES m JOIN dbo.LANGUAGES l ON l.code = 'VI' WHERE m.title = N'Đoraemon: Bản Tình Ca Đất Nước'
UNION ALL SELECT m.id, l.id, 1 FROM dbo.MOVIES m JOIN dbo.LANGUAGES l ON l.code = 'EN' WHERE m.title = N'Kẻ Hủy Diệt 2';
GO

-- 5. Chèn suất chiếu cho các phim ĐANG CHIẾU (Lật Mặt 7, Ký Sinh Trùng, Avengers: Hồi Kết)
-- Chạy cho các phòng chiếu (halls) tại các chi nhánh cho 5 ngày tới, các khung giờ: 09:00, 13:00, 16:30, 19:30, 22:00
DECLARE @now DATETIME2 = GETDATE();

INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
SELECT h.id,
       m.id,
       DATEADD(MINUTE, t.mins, DATEADD(DAY, d.day, CAST(CAST(@now AS DATE) AS DATETIME2))) AS start_time,
       DATEADD(MINUTE, t.mins + m.duration_min + 15, DATEADD(DAY, d.day, CAST(CAST(@now AS DATE) AS DATETIME2))) AS end_time,
       CASE h.hall_type WHEN 'IMAX' THEN 130000 WHEN 'VIP' THEN 110000 ELSE 80000 END,
       'ON_SALE'
FROM dbo.MOVIES m
CROSS JOIN dbo.HALLS h
CROSS JOIN (VALUES (0),(1),(2),(3),(4)) AS d(day)               -- Hôm nay và 4 ngày tới
CROSS JOIN (VALUES (540),(780),(990),(1170),(1320)) AS t(mins)  -- 09:00, 13:00, 16:30, 19:30, 22:00
WHERE m.title IN (N'Lật Mặt 7: Một Điều Ước', N'Ký Sinh Trùng (Parasite)', N'Avengers: Hồi Kết')
  AND NOT EXISTS (
      -- Tránh trùng lặp suất chiếu nếu chạy lại script này
      SELECT 1 FROM dbo.SHOWTIMES st 
      WHERE st.hall_id = h.id 
        AND st.movie_id = m.id 
        AND st.start_time = DATEADD(MINUTE, t.mins, DATEADD(DAY, d.day, CAST(CAST(@now AS DATE) AS DATETIME2)))
  );
GO

PRINT N'Chèn thêm phim và suất chiếu thành công!';
GO
