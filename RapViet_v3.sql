-- ============================================================
-- RapViet Cinema Management System
-- MS SQL Server DDL Script — Version 3.0
-- Schema aligned with UC List v3 (Group 6, Iteration Plan)
-- Tables: 27 total
-- Changes from v2.1:
--   REMOVED : PROMOTIONS, BOOKING_PROMOTIONS, CITIES
--   ADDED   : LANGUAGES, MOVIE_LANGUAGES, CART, CART_ITEMS,
--             FAVORITE_MOVIES, ATTENDANCE, COUNTER_DISCOUNTS, BRANCH_MOVIES, HALL_MOVIES
--   MODIFIED: MOVIES (language→FK), BRANCHES (no city_id),
--             BOOKINGS (source), PAYMENTS (type), SEATS (maintenance)
-- ============================================================

USE master;
GO
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'RapVietDB')
    CREATE DATABASE RapVietDB;
GO
USE RapVietDB;
GO

-- ── Drop all tables (dependency order) ──────────────────────
IF OBJECT_ID('dbo.ATTENDANCE',         'U') IS NOT NULL DROP TABLE dbo.ATTENDANCE;
IF OBJECT_ID('dbo.COUNTER_DISCOUNTS',  'U') IS NOT NULL DROP TABLE dbo.COUNTER_DISCOUNTS;
IF OBJECT_ID('dbo.BOOKING_FNB',        'U') IS NOT NULL DROP TABLE dbo.BOOKING_FNB;
IF OBJECT_ID('dbo.FNB_ITEMS',          'U') IS NOT NULL DROP TABLE dbo.FNB_ITEMS;
IF OBJECT_ID('dbo.REVIEWS',            'U') IS NOT NULL DROP TABLE dbo.REVIEWS;
IF OBJECT_ID('dbo.NOTIFICATIONS',      'U') IS NOT NULL DROP TABLE dbo.NOTIFICATIONS;
IF OBJECT_ID('dbo.PAYMENTS',           'U') IS NOT NULL DROP TABLE dbo.PAYMENTS;
IF OBJECT_ID('dbo.BOOKING_SEATS',      'U') IS NOT NULL DROP TABLE dbo.BOOKING_SEATS;
IF OBJECT_ID('dbo.BOOKINGS',           'U') IS NOT NULL DROP TABLE dbo.BOOKINGS;
IF OBJECT_ID('dbo.CART_ITEMS',         'U') IS NOT NULL DROP TABLE dbo.CART_ITEMS;
IF OBJECT_ID('dbo.CART',               'U') IS NOT NULL DROP TABLE dbo.CART;
IF OBJECT_ID('dbo.FAVORITE_MOVIES',    'U') IS NOT NULL DROP TABLE dbo.FAVORITE_MOVIES;
IF OBJECT_ID('dbo.SEAT_PRICING',       'U') IS NOT NULL DROP TABLE dbo.SEAT_PRICING;
IF OBJECT_ID('dbo.SEATS',              'U') IS NOT NULL DROP TABLE dbo.SEATS;
IF OBJECT_ID('dbo.SHOWTIMES',          'U') IS NOT NULL DROP TABLE dbo.SHOWTIMES;
IF OBJECT_ID('dbo.HALL_MOVIES',        'U') IS NOT NULL DROP TABLE dbo.HALL_MOVIES;
IF OBJECT_ID('dbo.BRANCH_MOVIES',      'U') IS NOT NULL DROP TABLE dbo.BRANCH_MOVIES;
IF OBJECT_ID('dbo.HALLS',              'U') IS NOT NULL DROP TABLE dbo.HALLS;
IF OBJECT_ID('dbo.STAFF_BRANCH',       'U') IS NOT NULL DROP TABLE dbo.STAFF_BRANCH;
IF OBJECT_ID('dbo.BRANCHES',           'U') IS NOT NULL DROP TABLE dbo.BRANCHES;
IF OBJECT_ID('dbo.CINEMA',             'U') IS NOT NULL DROP TABLE dbo.CINEMA;
IF OBJECT_ID('dbo.MOVIE_LANGUAGES',    'U') IS NOT NULL DROP TABLE dbo.MOVIE_LANGUAGES;
IF OBJECT_ID('dbo.MOVIES_CATEGORY',    'U') IS NOT NULL DROP TABLE dbo.MOVIES_CATEGORY;
IF OBJECT_ID('dbo.MOVIES',             'U') IS NOT NULL DROP TABLE dbo.MOVIES;
IF OBJECT_ID('dbo.LANGUAGES',          'U') IS NOT NULL DROP TABLE dbo.LANGUAGES;
IF OBJECT_ID('dbo.CATEGORY',           'U') IS NOT NULL DROP TABLE dbo.CATEGORY;
IF OBJECT_ID('dbo.[USER]',             'U') IS NOT NULL DROP TABLE dbo.[USER];
GO

-- ============================================================
-- GROUP 1 — Movie Catalog
-- ============================================================

CREATE TABLE dbo.CATEGORY (
    id          INT             NOT NULL IDENTITY(1,1),
    name        NVARCHAR(100)   NOT NULL,
    description NVARCHAR(300)   NULL,
    status      VARCHAR(10)     NOT NULL CONSTRAINT DF_CAT_st  DEFAULT 'ACTIVE',
    last_update DATETIME2       NOT NULL CONSTRAINT DF_CAT_lu  DEFAULT GETDATE(),
    CONSTRAINT PK_CATEGORY      PRIMARY KEY (id),
    CONSTRAINT UQ_CAT_name      UNIQUE (name),
    CONSTRAINT CK_CAT_status    CHECK (status IN ('ACTIVE','INACTIVE'))
);

CREATE TABLE dbo.LANGUAGES (
    id          INT             NOT NULL IDENTITY(1,1),
    name        NVARCHAR(80)    NOT NULL,
    code        VARCHAR(10)     NOT NULL,   -- e.g. VI, EN, KO
    status      VARCHAR(10)     NOT NULL CONSTRAINT DF_LANG_st DEFAULT 'ACTIVE',
    last_update DATETIME2       NOT NULL CONSTRAINT DF_LANG_lu DEFAULT GETDATE(),
    CONSTRAINT PK_LANGUAGES     PRIMARY KEY (id),
    CONSTRAINT UQ_LANG_code     UNIQUE (code),
    CONSTRAINT CK_LANG_status   CHECK (status IN ('ACTIVE','INACTIVE'))
);

CREATE TABLE dbo.MOVIES (
    id              INT             NOT NULL IDENTITY(1,1),
    title           NVARCHAR(200)   NOT NULL,
    duration_min    INT             NOT NULL,
    description     NVARCHAR(MAX)   NULL,
    release_date    DATE            NOT NULL,
    status          VARCHAR(20)     NOT NULL CONSTRAINT DF_MOV_st  DEFAULT 'COMING_SOON',
    poster_url      VARCHAR(500)    NULL,
    trailer_url     VARCHAR(500)    NULL,
    actor           NVARCHAR(500)   NULL,
    director        NVARCHAR(200)   NULL,
    last_update     DATETIME2       NOT NULL CONSTRAINT DF_MOV_lu  DEFAULT GETDATE(),
    CONSTRAINT PK_MOVIES            PRIMARY KEY (id),
    CONSTRAINT CK_MOV_status        CHECK (status IN ('COMING_SOON','NOW_SHOWING','ENDED')),
    CONSTRAINT CK_MOV_duration      CHECK (duration_min > 0)
);

CREATE TABLE dbo.MOVIES_CATEGORY (
    movie_id        INT         NOT NULL,
    category_id     INT         NOT NULL,
    last_update     DATETIME2   NOT NULL CONSTRAINT DF_MC_lu DEFAULT GETDATE(),
    CONSTRAINT PK_MOVIES_CATEGORY   PRIMARY KEY (movie_id, category_id),
    CONSTRAINT FK_MC_movie          FOREIGN KEY (movie_id)    REFERENCES dbo.MOVIES(id),
    CONSTRAINT FK_MC_category       FOREIGN KEY (category_id) REFERENCES dbo.CATEGORY(id)
);

CREATE TABLE dbo.MOVIE_LANGUAGES (
    movie_id        INT         NOT NULL,
    language_id     INT         NOT NULL,
    subtitle        BIT         NOT NULL CONSTRAINT DF_ML_sub DEFAULT 0,  -- 0=dubbed, 1=subtitled
    last_update     DATETIME2   NOT NULL CONSTRAINT DF_ML_lu  DEFAULT GETDATE(),
    CONSTRAINT PK_MOVIE_LANGUAGES   PRIMARY KEY (movie_id, language_id),
    CONSTRAINT FK_ML_movie          FOREIGN KEY (movie_id)    REFERENCES dbo.MOVIES(id),
    CONSTRAINT FK_ML_language       FOREIGN KEY (language_id) REFERENCES dbo.LANGUAGES(id)
);

-- ============================================================
-- GROUP 2 — User & Staff
-- ============================================================

CREATE TABLE dbo.[USER] (
    id              INT             NOT NULL IDENTITY(1,1),
    full_name       NVARCHAR(150)   NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    password_hash   VARCHAR(255)    NULL,                   -- NULL for Google OAuth users
    google_id       VARCHAR(100)    NULL,
    phone           VARCHAR(20)     NULL,
    role            VARCHAR(20)     NOT NULL CONSTRAINT DF_USER_role   DEFAULT 'CUSTOMER',
    active          BIT             NOT NULL CONSTRAINT DF_USER_active DEFAULT 1,
    email_verified  BIT             NOT NULL CONSTRAINT DF_USER_ev     DEFAULT 0,
    created_at      DATETIME2       NOT NULL CONSTRAINT DF_USER_ca     DEFAULT GETDATE(),
    last_update     DATETIME2       NOT NULL CONSTRAINT DF_USER_lu     DEFAULT GETDATE(),
    CONSTRAINT PK_USER              PRIMARY KEY (id),
    CONSTRAINT UQ_USER_email        UNIQUE (email),
    CONSTRAINT UQ_USER_google       UNIQUE (google_id),
    CONSTRAINT CK_USER_role         CHECK (role IN ('GUEST','CUSTOMER','STAFF','MANAGER','ADMIN')),
    CONSTRAINT CK_USER_auth         CHECK (password_hash IS NOT NULL OR google_id IS NOT NULL)
);

-- ============================================================
-- GROUP 3 — Cinema & Branch
-- ============================================================

CREATE TABLE dbo.CINEMA (
    id          INT             NOT NULL IDENTITY(1,1),
    name        NVARCHAR(150)   NOT NULL,
    address     NVARCHAR(300)   NULL,
    phone       VARCHAR(20)     NULL,
    logo_url    VARCHAR(500)    NULL,
    status      VARCHAR(10)     NOT NULL CONSTRAINT DF_CIN_st  DEFAULT 'ACTIVE',
    last_update DATETIME2       NOT NULL CONSTRAINT DF_CIN_lu  DEFAULT GETDATE(),
    CONSTRAINT PK_CINEMA        PRIMARY KEY (id),
    CONSTRAINT CK_CIN_status    CHECK (status IN ('ACTIVE','INACTIVE'))
);

CREATE TABLE dbo.BRANCHES (
    id              INT             NOT NULL IDENTITY(1,1),
    cinema_id       INT             NOT NULL,
    name            NVARCHAR(150)   NOT NULL,
    address         NVARCHAR(300)   NOT NULL,
    phone           VARCHAR(20)     NULL,
    open_time       TIME            NULL,
    close_time      TIME            NULL,
    status          VARCHAR(10)     NOT NULL CONSTRAINT DF_BR_st   DEFAULT 'ACTIVE',
    last_update     DATETIME2       NOT NULL CONSTRAINT DF_BR_lu   DEFAULT GETDATE(),
    CONSTRAINT PK_BRANCHES          PRIMARY KEY (id),
    CONSTRAINT FK_BR_cinema         FOREIGN KEY (cinema_id) REFERENCES dbo.CINEMA(id),
    CONSTRAINT CK_BR_status         CHECK (status IN ('ACTIVE','INACTIVE'))
);

CREATE TABLE dbo.STAFF_BRANCH (
    user_id     INT             NOT NULL,
    branch_id   INT             NOT NULL,
    position    NVARCHAR(100)   NOT NULL,
    assigned_at DATETIME2       NOT NULL CONSTRAINT DF_SB_aa DEFAULT GETDATE(),
    CONSTRAINT PK_STAFF_BRANCH  PRIMARY KEY (user_id, branch_id),
    CONSTRAINT FK_SB_user       FOREIGN KEY (user_id)   REFERENCES dbo.[USER](id),
    CONSTRAINT FK_SB_branch     FOREIGN KEY (branch_id) REFERENCES dbo.BRANCHES(id)
);

CREATE TABLE dbo.HALLS (
    id          INT             NOT NULL IDENTITY(1,1),
    branch_id   INT             NOT NULL,
    name        NVARCHAR(100)   NOT NULL,
    total_seats INT             NOT NULL,
    hall_type   VARCHAR(20)     NOT NULL CONSTRAINT DF_HALL_ht DEFAULT 'STANDARD',
    status      VARCHAR(20)     NOT NULL CONSTRAINT DF_HALL_st DEFAULT 'ACTIVE',
    last_update DATETIME2       NOT NULL CONSTRAINT DF_HALL_lu DEFAULT GETDATE(),
    CONSTRAINT PK_HALLS         PRIMARY KEY (id),
    CONSTRAINT FK_HALL_branch   FOREIGN KEY (branch_id) REFERENCES dbo.BRANCHES(id),
    CONSTRAINT CK_HALL_type     CHECK (hall_type IN ('STANDARD','VIP','IMAX','4DX','PREMIUM')),
    CONSTRAINT CK_HALL_status   CHECK (status IN ('ACTIVE','MAINTENANCE','INACTIVE'))
);

CREATE TABLE dbo.BRANCH_MOVIES (
	branch_id	INT NOT NULL,
	movie_id	INT NOT NULL,

	CONSTRAINT PK_BRANCH_MOVIES			PRIMARY KEY (branch_id, movie_id),
	CONSTRAINT FK_BRANCH_MOVIES_BRANCH	FOREIGN KEY ( branch_id) REFERENCES dbo.BRANCHES(id),
	CONSTRAINT FK_BRANCH_MOVIES_MOVIE	FOREIGN KEY (movie_id) REFERENCES dbo.MOVIES(id)
);

CREATE TABLE dbo.HALL_MOVIES (
	hall_id		INT NOT NULL,
	movie_id	INT NOT NULL,

	CONSTRAINT PK_HALL_MOVIES		PRIMARY KEY (hall_id, movie_id),
	CONSTRAINT FK_HALL_MOVIES_HALL	FOREIGN KEY (hall_id) REFERENCES dbo.HALLS (id),
	CONSTRAINT FK_HALL_MOVIES_MOVIE FOREIGN KEY (movie_id) REFERENCES dbo.MOVIES(id)
);

CREATE TABLE dbo.SEATS (
    id              INT             NOT NULL IDENTITY(1,1),
    hall_id         INT             NOT NULL,
    seat_row        VARCHAR(5)      NOT NULL,
    seat_number     INT             NOT NULL,
    seat_type       VARCHAR(20)     NOT NULL CONSTRAINT DF_SEAT_st DEFAULT 'STANDARD',
    maintenance     BIT             NOT NULL CONSTRAINT DF_SEAT_mn DEFAULT 0,  -- 1=locked for maintenance
    last_update     DATETIME2       NOT NULL CONSTRAINT DF_SEAT_lu DEFAULT GETDATE(),
    CONSTRAINT PK_SEATS             PRIMARY KEY (id),
    CONSTRAINT FK_SEAT_hall         FOREIGN KEY (hall_id) REFERENCES dbo.HALLS(id),
    CONSTRAINT UQ_SEAT_pos          UNIQUE (hall_id, seat_row, seat_number),
    CONSTRAINT CK_SEAT_type         CHECK (seat_type IN ('STANDARD','VIP','COUPLE'))
);

-- ============================================================
-- GROUP 4 — Showtime & Pricing
-- ============================================================

CREATE TABLE dbo.SHOWTIMES (
    id          INT             NOT NULL IDENTITY(1,1),
    hall_id     INT             NOT NULL,
    movie_id    INT             NOT NULL,
    start_time  DATETIME2       NOT NULL,
    end_time    DATETIME2       NOT NULL,
    base_price  DECIMAL(12,2)   NOT NULL,
    status      VARCHAR(20)     NOT NULL CONSTRAINT DF_ST_st DEFAULT 'SCHEDULED',
    last_update DATETIME2       NOT NULL CONSTRAINT DF_ST_lu DEFAULT GETDATE(),
    CONSTRAINT PK_SHOWTIMES     PRIMARY KEY (id),
    CONSTRAINT FK_ST_hall       FOREIGN KEY (hall_id)  REFERENCES dbo.HALLS(id),
    CONSTRAINT FK_ST_movie      FOREIGN KEY (movie_id) REFERENCES dbo.MOVIES(id),
    CONSTRAINT CK_ST_status     CHECK (status IN ('SCHEDULED','ON_SALE','CANCELLED','COMPLETED')),
    CONSTRAINT CK_ST_time       CHECK (end_time > start_time),
    CONSTRAINT CK_ST_price      CHECK (base_price >= 0)
);

CREATE TABLE dbo.SEAT_PRICING (
    id          INT             NOT NULL IDENTITY(1,1),
    showtime_id INT             NOT NULL,
    seat_type   VARCHAR(20)     NOT NULL,
    price       DECIMAL(12,2)   NOT NULL,
    last_update DATETIME2       NOT NULL CONSTRAINT DF_SP_lu DEFAULT GETDATE(),
    CONSTRAINT PK_SEAT_PRICING  PRIMARY KEY (id),
    CONSTRAINT FK_SP_showtime   FOREIGN KEY (showtime_id) REFERENCES dbo.SHOWTIMES(id),
    CONSTRAINT UQ_SP_pair       UNIQUE (showtime_id, seat_type),
    CONSTRAINT CK_SP_type       CHECK (seat_type IN ('STANDARD','VIP','COUPLE')),
    CONSTRAINT CK_SP_price      CHECK (price >= 0)
);

-- ============================================================
-- GROUP 5 — Cart (pre-booking)
-- ============================================================

CREATE TABLE dbo.CART (
    id          INT         NOT NULL IDENTITY(1,1),
    user_id     INT         NOT NULL,
    showtime_id INT         NOT NULL,
    created_at  DATETIME2   NOT NULL CONSTRAINT DF_CART_ca  DEFAULT GETDATE(),
    expires_at  DATETIME2   NOT NULL,   -- cart TTL (e.g. 10 min)
    last_update DATETIME2   NOT NULL CONSTRAINT DF_CART_lu  DEFAULT GETDATE(),
    CONSTRAINT PK_CART      PRIMARY KEY (id),
    CONSTRAINT FK_CART_user FOREIGN KEY (user_id)     REFERENCES dbo.[USER](id),
    CONSTRAINT FK_CART_st   FOREIGN KEY (showtime_id) REFERENCES dbo.SHOWTIMES(id)
);

CREATE TABLE dbo.CART_ITEMS (
    id          INT             NOT NULL IDENTITY(1,1),
    cart_id     INT             NOT NULL,
    seat_id     INT             NOT NULL,
    price       DECIMAL(12,2)   NOT NULL,
    locked_until DATETIME2      NOT NULL,   -- seat lock (5 min per UC13)
    last_update DATETIME2       NOT NULL CONSTRAINT DF_CI_lu DEFAULT GETDATE(),
    CONSTRAINT PK_CART_ITEMS    PRIMARY KEY (id),
    CONSTRAINT FK_CI_cart       FOREIGN KEY (cart_id) REFERENCES dbo.CART(id),
    CONSTRAINT FK_CI_seat       FOREIGN KEY (seat_id) REFERENCES dbo.SEATS(id),
    CONSTRAINT UQ_CI_seat       UNIQUE (cart_id, seat_id),
    CONSTRAINT CK_CI_price      CHECK (price >= 0)
);

-- ============================================================
-- GROUP 6 — Booking & Payment
-- ============================================================

CREATE TABLE dbo.BOOKINGS (
    id          INT             NOT NULL IDENTITY(1,1),
    user_id     INT             NOT NULL,
    showtime_id INT             NOT NULL,
    source      VARCHAR(10)     NOT NULL CONSTRAINT DF_BK_src DEFAULT 'ONLINE',  -- ONLINE/WALKIN
    status      VARCHAR(20)     NOT NULL CONSTRAINT DF_BK_st  DEFAULT 'PENDING',
    total_price DECIMAL(12,2)   NOT NULL,
    qr_code     VARCHAR(500)    NULL,
    booked_at   DATETIME2       NOT NULL CONSTRAINT DF_BK_ba  DEFAULT GETDATE(),
    last_update DATETIME2       NOT NULL CONSTRAINT DF_BK_lu  DEFAULT GETDATE(),
    CONSTRAINT PK_BOOKINGS      PRIMARY KEY (id),
    CONSTRAINT FK_BK_user       FOREIGN KEY (user_id)     REFERENCES dbo.[USER](id),
    CONSTRAINT FK_BK_showtime   FOREIGN KEY (showtime_id) REFERENCES dbo.SHOWTIMES(id),
    CONSTRAINT CK_BK_source     CHECK (source IN ('ONLINE','WALKIN')),
    CONSTRAINT CK_BK_status     CHECK (status IN ('PENDING','CONFIRMED','CHECKED_IN','USED','CANCELLED'))
);

CREATE TABLE dbo.BOOKING_SEATS (
    id          INT             NOT NULL IDENTITY(1,1),
    booking_id  INT             NOT NULL,
    seat_id     INT             NOT NULL,
    price       DECIMAL(12,2)   NOT NULL,
    last_update DATETIME2       NOT NULL CONSTRAINT DF_BS_lu DEFAULT GETDATE(),
    CONSTRAINT PK_BOOKING_SEATS PRIMARY KEY (id),
    CONSTRAINT FK_BS_booking    FOREIGN KEY (booking_id) REFERENCES dbo.BOOKINGS(id),
    CONSTRAINT FK_BS_seat       FOREIGN KEY (seat_id)    REFERENCES dbo.SEATS(id),
    CONSTRAINT UQ_BS_seat       UNIQUE (booking_id, seat_id),
    CONSTRAINT CK_BS_price      CHECK (price >= 0)
);

CREATE TABLE dbo.PAYMENTS (
    id              INT             NOT NULL IDENTITY(1,1),
    booking_id      INT             NOT NULL,
    type            VARCHAR(10)     NOT NULL CONSTRAINT DF_PAY_type DEFAULT 'ONLINE', -- ONLINE/CASH
    method          VARCHAR(30)     NULL,  -- VNPAY/MOMO/CASH etc.
    transaction_id  VARCHAR(200)    NULL,
    status          VARCHAR(20)     NOT NULL CONSTRAINT DF_PAY_st   DEFAULT 'PENDING',
    amount          DECIMAL(12,2)   NOT NULL,
    paid_at         DATETIME2       NULL,
    gateway         VARCHAR(50)     NULL,
    last_update     DATETIME2       NOT NULL CONSTRAINT DF_PAY_lu   DEFAULT GETDATE(),
    CONSTRAINT PK_PAYMENTS          PRIMARY KEY (id),
    CONSTRAINT FK_PAY_booking       FOREIGN KEY (booking_id) REFERENCES dbo.BOOKINGS(id),
    CONSTRAINT UQ_PAY_booking       UNIQUE (booking_id),
    CONSTRAINT CK_PAY_type          CHECK (type   IN ('ONLINE','CASH')),
    CONSTRAINT CK_PAY_method        CHECK (method IN ('VNPAY','MOMO','ZALOPAY','CASH','BANKING') OR method IS NULL),
    CONSTRAINT CK_PAY_status        CHECK (status IN ('PENDING','SUCCESS','FAILED')),
    CONSTRAINT CK_PAY_amount        CHECK (amount >= 0)
);

CREATE TABLE dbo.COUNTER_DISCOUNTS (
    id          INT             NOT NULL IDENTITY(1,1),
    booking_id  INT             NOT NULL,
    applied_by  INT             NOT NULL,   -- FK to USER (Branch Staff)
    reason      NVARCHAR(200)   NOT NULL,
    amount      DECIMAL(12,2)   NOT NULL,
    applied_at  DATETIME2       NOT NULL CONSTRAINT DF_CD_aa DEFAULT GETDATE(),
    CONSTRAINT PK_COUNTER_DISCOUNTS PRIMARY KEY (id),
    CONSTRAINT FK_CD_booking    FOREIGN KEY (booking_id) REFERENCES dbo.BOOKINGS(id),
    CONSTRAINT FK_CD_staff      FOREIGN KEY (applied_by) REFERENCES dbo.[USER](id),
    CONSTRAINT CK_CD_amount     CHECK (amount >= 0)
);

-- ============================================================
-- GROUP 7 — F&B
-- ============================================================

CREATE TABLE dbo.FNB_ITEMS (
    id          INT             NOT NULL IDENTITY(1,1),
    branch_id   INT             NOT NULL,
    name        NVARCHAR(150)   NOT NULL,
    description NVARCHAR(300)   NULL,
    price       DECIMAL(12,2)   NOT NULL,
    category    VARCHAR(10)     NOT NULL CONSTRAINT DF_FNB_cat DEFAULT 'FOOD',
    stock       INT             NOT NULL CONSTRAINT DF_FNB_stk DEFAULT 0,
    image_url   VARCHAR(500)    NULL,
    available   BIT             NOT NULL CONSTRAINT DF_FNB_av  DEFAULT 1,
    last_update DATETIME2       NOT NULL CONSTRAINT DF_FNB_lu  DEFAULT GETDATE(),
    CONSTRAINT PK_FNB_ITEMS     PRIMARY KEY (id),
    CONSTRAINT FK_FNB_branch    FOREIGN KEY (branch_id) REFERENCES dbo.BRANCHES(id),
    CONSTRAINT CK_FNB_cat       CHECK (category IN ('FOOD','DRINK','COMBO','SNACK')),
    CONSTRAINT CK_FNB_price     CHECK (price >= 0),
    CONSTRAINT CK_FNB_stock     CHECK (stock >= 0)
);

CREATE TABLE dbo.BOOKING_FNB (
    id          INT             NOT NULL IDENTITY(1,1),
    booking_id  INT             NOT NULL,
    fnb_item_id INT             NOT NULL,
    quantity    INT             NOT NULL,
    unit_price  DECIMAL(12,2)   NOT NULL,
    status      VARCHAR(20)     NOT NULL CONSTRAINT DF_BFNB_st DEFAULT 'PENDING',
    last_update DATETIME2       NOT NULL CONSTRAINT DF_BFNB_lu DEFAULT GETDATE(),
    CONSTRAINT PK_BOOKING_FNB   PRIMARY KEY (id),
    CONSTRAINT FK_BFNB_booking  FOREIGN KEY (booking_id)  REFERENCES dbo.BOOKINGS(id),
    CONSTRAINT FK_BFNB_item     FOREIGN KEY (fnb_item_id) REFERENCES dbo.FNB_ITEMS(id),
    CONSTRAINT CK_BFNB_qty     CHECK (quantity > 0),
    CONSTRAINT CK_BFNB_price   CHECK (unit_price >= 0),
    CONSTRAINT CK_BFNB_status  CHECK (status IN ('PENDING','PREPARING','READY','DELIVERED'))
);

-- ============================================================
-- GROUP 8 — Reviews
-- ============================================================

CREATE TABLE dbo.REVIEWS (
    id          INT             NOT NULL IDENTITY(1,1),
    user_id     INT             NOT NULL,
    movie_id    INT             NOT NULL,
    booking_id  INT             NOT NULL,
    rating      TINYINT         NOT NULL,
    comment     NVARCHAR(MAX)   NULL,
    status      VARCHAR(10)     NOT NULL CONSTRAINT DF_RV_st DEFAULT 'ACTIVE',
    created_at  DATETIME2       NOT NULL CONSTRAINT DF_RV_ca DEFAULT GETDATE(),
    last_update DATETIME2       NOT NULL CONSTRAINT DF_RV_lu DEFAULT GETDATE(),
    CONSTRAINT PK_REVIEWS       PRIMARY KEY (id),
    CONSTRAINT FK_RV_user       FOREIGN KEY (user_id)   REFERENCES dbo.[USER](id),
    CONSTRAINT FK_RV_movie      FOREIGN KEY (movie_id)  REFERENCES dbo.MOVIES(id),
    CONSTRAINT FK_RV_booking    FOREIGN KEY (booking_id)REFERENCES dbo.BOOKINGS(id),
    CONSTRAINT UQ_RV_booking    UNIQUE (booking_id),
    CONSTRAINT CK_RV_rating     CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT CK_RV_status     CHECK (status IN ('ACTIVE','HIDDEN'))
);

-- ============================================================
-- GROUP 9 — Favorites
-- ============================================================

CREATE TABLE dbo.FAVORITE_MOVIES (
    user_id     INT         NOT NULL,
    movie_id    INT         NOT NULL,
    created_at  DATETIME2   NOT NULL CONSTRAINT DF_FAV_ca DEFAULT GETDATE(),
    CONSTRAINT PK_FAVORITE_MOVIES   PRIMARY KEY (user_id, movie_id),
    CONSTRAINT FK_FAV_user          FOREIGN KEY (user_id)  REFERENCES dbo.[USER](id),
    CONSTRAINT FK_FAV_movie         FOREIGN KEY (movie_id) REFERENCES dbo.MOVIES(id)
);

-- ============================================================
-- GROUP 10 — Notifications
-- ============================================================

CREATE TABLE dbo.NOTIFICATIONS (
    id          INT             NOT NULL IDENTITY(1,1),
    user_id     INT             NULL,       -- NULL = broadcast to all
    branch_id   INT             NULL,
    sent_by     INT             NOT NULL,
    type        VARCHAR(30)     NOT NULL,
    title       NVARCHAR(200)   NOT NULL,
    message     NVARCHAR(2000)  NOT NULL,
    status      VARCHAR(10)     NOT NULL CONSTRAINT DF_NOTIF_st DEFAULT 'SENT',
    sent_at     DATETIME2       NOT NULL CONSTRAINT DF_NOTIF_sa DEFAULT GETDATE(),
    CONSTRAINT PK_NOTIFICATIONS     PRIMARY KEY (id),
    CONSTRAINT FK_NOTIF_user        FOREIGN KEY (user_id)   REFERENCES dbo.[USER](id),
    CONSTRAINT FK_NOTIF_branch      FOREIGN KEY (branch_id) REFERENCES dbo.BRANCHES(id),
    CONSTRAINT FK_NOTIF_sender      FOREIGN KEY (sent_by)   REFERENCES dbo.[USER](id),
    CONSTRAINT CK_NOTIF_type        CHECK (type IN ('BOOKING_CONFIRM','PAYMENT_CONFIRM','PROMOTION','SYSTEM')),
    CONSTRAINT CK_NOTIF_status      CHECK (status IN ('SENT','FAILED','DRAFT'))
);

-- ============================================================
-- GROUP 11 — Attendance
-- ============================================================

CREATE TABLE dbo.ATTENDANCE (
    id          INT         NOT NULL IDENTITY(1,1),
    booking_id  INT         NOT NULL,
    checked_by  INT         NOT NULL,   -- FK to USER (Branch Staff)
    checked_at  DATETIME2   NOT NULL CONSTRAINT DF_ATT_ca DEFAULT GETDATE(),
    CONSTRAINT PK_ATTENDANCE    PRIMARY KEY (id),
    CONSTRAINT FK_ATT_booking   FOREIGN KEY (booking_id) REFERENCES dbo.BOOKINGS(id),
    CONSTRAINT FK_ATT_staff     FOREIGN KEY (checked_by) REFERENCES dbo.[USER](id),
    CONSTRAINT UQ_ATT_booking   UNIQUE (booking_id)     -- prevent duplicate entry
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX IX_MOV_status      ON dbo.MOVIES        (status);
CREATE INDEX IX_ST_hall         ON dbo.SHOWTIMES      (hall_id, start_time);
CREATE INDEX IX_ST_movie        ON dbo.SHOWTIMES      (movie_id, start_time);
CREATE INDEX IX_BK_user         ON dbo.BOOKINGS       (user_id);
CREATE INDEX IX_BK_showtime     ON dbo.BOOKINGS       (showtime_id);
CREATE INDEX IX_BK_source       ON dbo.BOOKINGS       (source, status);
CREATE INDEX IX_BS_seat         ON dbo.BOOKING_SEATS  (seat_id);
CREATE INDEX IX_BS_lock         ON dbo.CART_ITEMS     (seat_id, locked_until);
CREATE INDEX IX_PAY_status      ON dbo.PAYMENTS       (status);
CREATE INDEX IX_FNB_branch      ON dbo.FNB_ITEMS      (branch_id, available);
CREATE INDEX IX_RV_movie        ON dbo.REVIEWS        (movie_id);
CREATE INDEX IX_NOTIF_user      ON dbo.NOTIFICATIONS  (user_id, sent_at DESC);
CREATE INDEX IX_FAV_user        ON dbo.FAVORITE_MOVIES(user_id);
CREATE INDEX IX_ATT_checked     ON dbo.ATTENDANCE     (checked_at DESC);

GO
PRINT 'RapVietDB v3.0 — 22 tables created successfully.';

INSERT INTO [USER]
(
    full_name,
    email,
    password_hash,
    google_id,
    phone,
    role,
    active,
    email_verified,
    created_at,
    last_update
)
VALUES
(
    'System Admin',
    'admin@cinema.com',
    '123456',
    'local_admin',
    '0900000001',
    'ADMIN',
    1,
    1,
    GETDATE(),
    GETDATE()
);

INSERT INTO [USER]
(
    full_name,
    email,
    password_hash,
    google_id,
    phone,
    role,
    active,
    email_verified,
    created_at,
    last_update
)
VALUES
(
    'Branch Manager',
    'manager@cinema.com',
    '123456',
    'local_manager',
    '0900000002',
    'MANAGER',
    1,
    1,
    GETDATE(),
    GETDATE()
);

INSERT INTO [USER]
(
    full_name,
    email,
    password_hash,
    google_id,
    phone,
    role,
    active,
    email_verified,
    created_at,
    last_update
)
VALUES
(
    'Branch Staff',
    'staff@cinema.com',
    '123456',
    'local_staff',
    '0900000003',
    'STAFF',
    1,
    1,
    GETDATE(),
    GETDATE()
);

INSERT INTO [USER]
(
    full_name,
    email,
    password_hash,
    google_id,
    phone,
    role,
    active,
    email_verified,
    created_at,
    last_update
)
VALUES
(
    'Customer Test',
    'customer@cinema.com',
    '123456',
    'local_customer',
    '0900000004',
    'CUSTOMER',
    1,
    1,
    GETDATE(),
    GETDATE()
);


USE RapVietDB;
GO

INSERT INTO dbo.MOVIES
(
    title,
    description,
    duration_min,
    release_date,
    status,
    poster_url,
    trailer_url,
    actor,
    director
)
VALUES
(
    N'Doraemon: Nobita và Bản Giao Hưởng Địa Cầu',
    N'Nobita và nhóm bạn bước vào một chuyến phiêu lưu âm nhạc để bảo vệ Trái Đất.',
    115,
    '2026-06-01',
    'NOW_SHOWING',
    NULL,
    NULL,
    N'Wasabi Mizuta, Megumi Ohara',
    N'Kazuaki Imai'
),
(
    N'Thám Tử Lừng Danh Conan: Ngôi Sao 5 Cánh Một Triệu Đô',
    N'Conan tiếp tục phá giải một vụ án bí ẩn liên quan đến kho báu và các manh mối nguy hiểm.',
    110,
    '2026-06-05',
    'NOW_SHOWING',
    NULL,
    NULL,
    N'Minami Takayama, Wakana Yamazaki',
    N'Chika Nagaoka'
),
(
    N'Lật Mặt 8: Vòng Tay Nắng',
    N'Một câu chuyện gia đình Việt Nam xoay quanh tình thân, ước mơ và những lựa chọn trong cuộc sống.',
    120,
    '2026-06-10',
    'NOW_SHOWING',
    NULL,
    NULL,
    N'Thanh Thức, Đoàn Thế Vinh',
    N'Lý Hải'
),
(
    N'Inside Out 2',
    N'Câu chuyện tiếp tục bên trong tâm trí Riley khi những cảm xúc mới xuất hiện.',
    96,
    '2026-06-15',
    'NOW_SHOWING',
    NULL,
    NULL,
    N'Amy Poehler, Maya Hawke',
    N'Kelsey Mann'
),
(
    N'Avengers: Secret Wars',
    N'Một cuộc chiến đa vũ trụ quy mô lớn giữa các siêu anh hùng và những thế lực mới.',
    150,
    '2026-07-01',
    'COMING_SOON',
    NULL,
    NULL,
    N'Robert Downey Jr., Chris Hemsworth',
    N'Anthony Russo, Joe Russo'
);
GO

