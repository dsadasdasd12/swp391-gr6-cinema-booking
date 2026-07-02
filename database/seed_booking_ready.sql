USE RapVietDB;
GO

SET NOCOUNT ON;

PRINT '==> Seed booking-ready data: movies, posters, branches, halls, seats, showtimes';

DECLARE @demoPasswordHash VARCHAR(100) =
    '$2a$10$qbkUBlJzgU2TGGvvpvl2z.NQAlPjx/nb2gO.7JgXvOF.sDUoqbTau'; -- password: 123456

/* Demo customer account for booking flow. */
IF NOT EXISTS (SELECT 1 FROM dbo.[USER] WHERE email = 'customer@demo.com')
BEGIN
    INSERT INTO dbo.[USER] (full_name, email, password_hash, google_id, phone, role, active, email_verified)
    VALUES (N'Demo Customer', 'customer@demo.com', @demoPasswordHash, 'seed-demo-customer', '0900000000', 'CUSTOMER', 1, 1);
END
ELSE
BEGIN
    UPDATE dbo.[USER]
    SET password_hash = @demoPasswordHash,
        role = 'CUSTOMER',
        active = 1,
        email_verified = 1,
        last_update = GETDATE()
    WHERE email = 'customer@demo.com';
END

/* Minimal master data if the database is empty. */
IF NOT EXISTS (SELECT 1 FROM dbo.CINEMA)
BEGIN
    INSERT INTO dbo.CINEMA (name, address, phone, logo_url, status)
    VALUES (N'RapViet Cinema', N'Ha Noi, Viet Nam', '19001000', 'assets/img/logo.png', 'ACTIVE');
END

DECLARE @cinemaId INT = (SELECT TOP 1 id FROM dbo.CINEMA ORDER BY id);

IF NOT EXISTS (SELECT 1 FROM dbo.BRANCHES WHERE name = N'RapViet Demo Branch')
BEGIN
    INSERT INTO dbo.BRANCHES (cinema_id, name, address, phone, open_time, close_time, status)
    VALUES (@cinemaId, N'RapViet Demo Branch', N'123 Demo Street, Ha Noi', '0240000000', '08:00', '23:30', 'ACTIVE');
END

DECLARE @branchId INT = (SELECT TOP 1 id FROM dbo.BRANCHES WHERE status = 'ACTIVE' ORDER BY id);

IF NOT EXISTS (SELECT 1 FROM dbo.HALLS WHERE branch_id = @branchId AND status = 'ACTIVE')
BEGIN
    INSERT INTO dbo.HALLS (branch_id, name, total_seats, hall_type, status) VALUES
    (@branchId, N'Demo Hall 1', 60, 'STANDARD', 'ACTIVE'),
    (@branchId, N'Demo Hall 2', 60, 'VIP', 'ACTIVE');
END

IF NOT EXISTS (SELECT 1 FROM dbo.CATEGORY WHERE name = N'Action')
    INSERT INTO dbo.CATEGORY (name, description, status) VALUES (N'Action', N'Action movies', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.CATEGORY WHERE name = N'Drama')
    INSERT INTO dbo.CATEGORY (name, description, status) VALUES (N'Drama', N'Drama movies', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.CATEGORY WHERE name = N'Comedy')
    INSERT INTO dbo.CATEGORY (name, description, status) VALUES (N'Comedy', N'Comedy movies', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.CATEGORY WHERE name = N'Animation')
    INSERT INTO dbo.CATEGORY (name, description, status) VALUES (N'Animation', N'Animation movies', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.CATEGORY WHERE name = N'Horror')
    INSERT INTO dbo.CATEGORY (name, description, status) VALUES (N'Horror', N'Horror movies', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.CATEGORY WHERE name = N'Sci-Fi')
    INSERT INTO dbo.CATEGORY (name, description, status) VALUES (N'Sci-Fi', N'Science fiction movies', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.LANGUAGES WHERE code = 'VI')
    INSERT INTO dbo.LANGUAGES (name, code, status) VALUES (N'Vietnamese', 'VI', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.LANGUAGES WHERE code = 'EN')
    INSERT INTO dbo.LANGUAGES (name, code, status) VALUES (N'English', 'EN', 'ACTIVE');

/* Upsert demo movies. Placeholder posters are real image URLs, so cards are not blank. */
DECLARE @movies TABLE (
    title NVARCHAR(200) PRIMARY KEY,
    duration_min INT,
    description NVARCHAR(MAX),
    release_date DATE,
    status VARCHAR(20),
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500),
    actor NVARCHAR(500),
    director NVARCHAR(200),
    category_name NVARCHAR(100),
    lang_code VARCHAR(10)
);

INSERT INTO @movies VALUES
(N'Lat Mat 7: Mot Dieu Uoc', 138, N'Family drama about a mother and her children.', DATEADD(DAY, -25, CAST(GETDATE() AS DATE)), 'NOW_SHOWING', 'https://upload.wikimedia.org/wikipedia/en/thumb/d/d0/John_Wick_-_Chapter_4_promotional_poster.jpg/250px-John_Wick_-_Chapter_4_promotional_poster.jpg', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Thanh Hien, Truong Minh Cuong', N'Ly Hai', N'Drama', 'VI'),
(N'Mai', 131, N'Emotional Vietnamese drama about love, family, and second chances.', DATEADD(DAY, -40, CAST(GETDATE() AS DATE)), 'NOW_SHOWING', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/36/Mai_2024_poster.jpg/250px-Mai_2024_poster.jpg', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Phuong Anh Dao, Tuan Tran', N'Tran Thanh', N'Drama', 'VI'),
(N'Dune: Part Two', 166, N'Sci-fi epic on the desert planet Arrakis.', DATEADD(DAY, -35, CAST(GETDATE() AS DATE)), 'NOW_SHOWING', 'https://upload.wikimedia.org/wikipedia/en/thumb/5/52/Dune_Part_Two_poster.jpeg/250px-Dune_Part_Two_poster.jpeg', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Timothee Chalamet, Zendaya', N'Denis Villeneuve', N'Sci-Fi', 'EN'),
(N'Inside Out 2', 96, N'New emotions arrive as Riley grows up.', DATEADD(DAY, 18, CAST(GETDATE() AS DATE)), 'COMING_SOON', 'https://upload.wikimedia.org/wikipedia/en/thumb/f/f7/Inside_Out_2_poster.jpg/250px-Inside_Out_2_poster.jpg', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Amy Poehler, Maya Hawke', N'Kelsey Mann', N'Animation', 'EN'),
(N'Vu Tru Song Song', 135, N'A young engineer discovers a portal to parallel universes.', DATEADD(DAY, -20, CAST(GETDATE() AS DATE)), 'NOW_SHOWING', 'https://upload.wikimedia.org/wikipedia/en/1/1e/Everything_Everywhere_All_at_Once.jpg', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Minh Anh, Quoc Bao', N'Tran Hung', N'Sci-Fi', 'VI'),
(N'Lan Ranh Sinh Tu', 118, N'A rescue mission inside a captured skyscraper.', DATEADD(DAY, -18, CAST(GETDATE() AS DATE)), 'NOW_SHOWING', 'https://upload.wikimedia.org/wikipedia/en/thumb/0/02/Extraction_2_poster.jpg/250px-Extraction_2_poster.jpg', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Huu Long, Lan Phuong', N'Pham Tuan', N'Action', 'VI'),
(N'Ngoi Nha Cam Lang', 102, N'A family moves into an old house and discovers a silent presence.', DATEADD(DAY, -15, CAST(GETDATE() AS DATE)), 'NOW_SHOWING', 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e7/A_Quiet_Place_Day_One_%282024%29_poster.jpg/250px-A_Quiet_Place_Day_One_%282024%29_poster.jpg', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Thu Ha, Duc Anh', N'Le Van', N'Horror', 'VI'),
(N'Tieng Cuoi Mua Ha', 96, N'A group of friends share a chaotic summer full of comedy.', DATEADD(DAY, -10, CAST(GETDATE() AS DATE)), 'NOW_SHOWING', 'https://upload.wikimedia.org/wikipedia/en/thumb/c/c4/Anyone_but_You_%282023%29_official_poster.webp/250px-Anyone_but_You_%282023%29_official_poster.webp.png', 'https://youtube.com/embed/dQw4w9WgXcQ', N'Gia Bao, My Linh', N'Ngo Thanh', N'Comedy', 'VI');

UPDATE m
SET m.duration_min = s.duration_min,
    m.description = s.description,
    m.release_date = s.release_date,
    m.status = s.status,
    m.poster_url = CASE WHEN NULLIF(LTRIM(RTRIM(m.poster_url)), '') IS NULL THEN s.poster_url ELSE m.poster_url END,
    m.trailer_url = CASE WHEN NULLIF(LTRIM(RTRIM(m.trailer_url)), '') IS NULL THEN s.trailer_url ELSE m.trailer_url END,
    m.actor = s.actor,
    m.director = s.director,
    m.last_update = GETDATE()
FROM dbo.MOVIES m
JOIN @movies s ON s.title = m.title;

INSERT INTO dbo.MOVIES (title, duration_min, description, release_date, status, poster_url, trailer_url, actor, director)
SELECT s.title, s.duration_min, s.description, s.release_date, s.status, s.poster_url, s.trailer_url, s.actor, s.director
FROM @movies s
WHERE NOT EXISTS (SELECT 1 FROM dbo.MOVIES m WHERE m.title = s.title);

/* Fill all movies that still have no poster, including old/manual records. */
UPDATE dbo.MOVIES
SET poster_url = 'https://upload.wikimedia.org/wikipedia/en/thumb/5/52/Dune_Part_Two_poster.jpeg/250px-Dune_Part_Two_poster.jpeg',
    last_update = GETDATE()
WHERE NULLIF(LTRIM(RTRIM(poster_url)), '') IS NULL;

/* Keep demo movie category/language links present without duplicating. */
INSERT INTO dbo.MOVIES_CATEGORY (movie_id, category_id)
SELECT m.id, c.id
FROM @movies s
JOIN dbo.MOVIES m ON m.title = s.title
JOIN dbo.CATEGORY c ON c.name = s.category_name
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.MOVIES_CATEGORY mc
    WHERE mc.movie_id = m.id AND mc.category_id = c.id
);

INSERT INTO dbo.MOVIE_LANGUAGES (movie_id, language_id, subtitle)
SELECT m.id, l.id, CASE WHEN s.lang_code = 'EN' THEN 1 ELSE 0 END
FROM @movies s
JOIN dbo.MOVIES m ON m.title = s.title
JOIN dbo.LANGUAGES l ON l.code = s.lang_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.MOVIE_LANGUAGES ml
    WHERE ml.movie_id = m.id AND ml.language_id = l.id
);

/* Create seats for any active hall that has none. */
DECLARE @hallId INT;
DECLARE hall_cur CURSOR LOCAL FAST_FORWARD FOR
    SELECT h.id
    FROM dbo.HALLS h
    WHERE h.status = 'ACTIVE'
      AND NOT EXISTS (SELECT 1 FROM dbo.SEATS s WHERE s.hall_id = h.id);

OPEN hall_cur;
FETCH NEXT FROM hall_cur INTO @hallId;
WHILE @@FETCH_STATUS = 0
BEGIN
    DECLARE @r INT = 1;
    DECLARE @c INT;
    DECLARE @row VARCHAR(1);

    WHILE @r <= 6
    BEGIN
        SET @row = SUBSTRING('ABCDEF', @r, 1);
        SET @c = 1;
        WHILE @c <= 10
        BEGIN
            INSERT INTO dbo.SEATS (hall_id, seat_row, seat_number, seat_type, maintenance)
            VALUES (@hallId, @row, @c,
                    CASE WHEN @r = 6 THEN 'COUPLE' WHEN @r >= 4 THEN 'VIP' ELSE 'STANDARD' END,
                    0);
            SET @c += 1;
        END
        SET @r += 1;
    END

    UPDATE dbo.HALLS SET total_seats = 60 WHERE id = @hallId AND total_seats < 60;
    FETCH NEXT FROM hall_cur INTO @hallId;
END
CLOSE hall_cur;
DEALLOCATE hall_cur;

/* Generate bookable future showtimes for NOW_SHOWING movies for the next 7 days. */
;WITH movie_slots AS (
    SELECT m.id AS movie_id,
           m.duration_min,
           ROW_NUMBER() OVER (ORDER BY m.id) AS rn
    FROM dbo.MOVIES m
    WHERE m.status = 'NOW_SHOWING'
),
hall_slots AS (
    SELECT h.id AS hall_id,
           h.hall_type,
           ROW_NUMBER() OVER (ORDER BY h.id) AS rn,
           COUNT(*) OVER () AS hall_count
    FROM dbo.HALLS h
    WHERE h.status = 'ACTIVE'
),
days AS (
    SELECT 1 AS day_offset UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL
    SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
),
schedule AS (
    SELECT h.hall_id,
           m.movie_id,
           DATEADD(MINUTE,
                   600 + ((m.rn - 1) % 4) * 210,
                   DATEADD(DAY, d.day_offset, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))) AS start_time,
           DATEADD(MINUTE,
                   600 + ((m.rn - 1) % 4) * 210 + m.duration_min + 15,
                   DATEADD(DAY, d.day_offset, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))) AS end_time,
           CASE h.hall_type
               WHEN 'IMAX' THEN 130000
               WHEN 'VIP' THEN 110000
               ELSE 85000
           END AS base_price
    FROM movie_slots m
    JOIN hall_slots h ON h.rn = ((m.rn - 1) % h.hall_count) + 1
    CROSS JOIN days d
)
INSERT INTO dbo.SHOWTIMES (hall_id, movie_id, start_time, end_time, base_price, status)
SELECT s.hall_id, s.movie_id, s.start_time, s.end_time, s.base_price, 'ON_SALE'
FROM schedule s
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.SHOWTIMES st
    WHERE st.hall_id = s.hall_id
      AND st.movie_id = s.movie_id
      AND st.status IN ('SCHEDULED', 'ON_SALE')
      AND CONVERT(date, st.start_time) = CONVERT(date, s.start_time)
      AND DATEPART(hour, st.start_time) = DATEPART(hour, s.start_time)
);

/* Add price rows so seat selection has standard/VIP/couple prices available. */
INSERT INTO dbo.SEAT_PRICING (showtime_id, seat_type, price)
SELECT st.id, v.seat_type, st.base_price + v.extra_price
FROM dbo.SHOWTIMES st
CROSS JOIN (VALUES
    ('STANDARD', 0),
    ('VIP', 25000),
    ('COUPLE', 60000)
) v(seat_type, extra_price)
WHERE st.start_time > GETDATE()
  AND st.status IN ('SCHEDULED', 'ON_SALE')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.SEAT_PRICING sp
      WHERE sp.showtime_id = st.id AND sp.seat_type = v.seat_type
  );

PRINT '==> Done. Login demo: customer@demo.com / 123456';
PRINT '==> Try: /cinema/booking/start or /cinema/movies';
GO
