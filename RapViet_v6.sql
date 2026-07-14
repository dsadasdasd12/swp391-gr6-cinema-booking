-- ============================================================
-- FINAL SHARED DATABASE — RAPVIET CINEMA
-- Run this one file on a development/demo database. Re-running it deletes
-- and recreates RapVietDB, including all tables and demo data.
-- Final schema: 31 tables (30 base tables + BOOKING_STATUS_HISTORY).
-- Added: booking-status timeline and automatic status-history trigger.
-- Updated: BOOKINGS accepts COMPLETED for QR/ticket completion.
-- Removed from the active flow: SEAT_PRICING; price = SHOWTIMES.base_price
-- multiplied by SEAT_TYPES.default_price.
-- Seed included: roles/accounts, categories, languages, mappings, branches,
-- halls, seats, showtimes, bookings and payments.
-- Demo password: 123456. Accounts: admin@cinema.com, manager@cinema.com,
-- staff@cinema.com, customer@cinema.com, customer02@cinema.com ... customer06@cinema.com.
-- ============================================================
-- RapViet Cinema Management System
-- MS SQL Server DDL Script — Version 3.0
-- Schema aligned with UC List v3 (Group 6, Iteration Plan)
-- Tables: 30 total
-- Changes from v2.1:
--   REMOVED : PROMOTIONS, BOOKING_PROMOTIONS, CITIES
--   ADDED   : LANGUAGES, MOVIE_LANGUAGES, CART, CART_ITEMS,
--             FAVORITE_MOVIES, ATTENDANCE, COUNTER_DISCOUNTS, 
--             BRANCH_MOVIES, HALL_MOVIES, SEAT_TYPES, DISCOUNT_CODES, VOUCHER_HISTORY
-- ============================================================

USE master;
GO
/* Final script is intentionally destructive: always start from a clean DB. */
IF DB_ID('RapVietDB') IS NOT NULL
BEGIN
    ALTER DATABASE RapVietDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE RapVietDB;
END;
CREATE DATABASE RapVietDB;
GO
USE RapVietDB;
GO

-- ── Drop all tables (dependency order) ──────────────────────
/* Final-version objects must be removed before BOOKINGS on a rerun. */
IF OBJECT_ID('dbo.TR_BOOKINGS_status_history', 'TR') IS NOT NULL DROP TRIGGER dbo.TR_BOOKINGS_status_history;
IF OBJECT_ID('dbo.BOOKING_STATUS_HISTORY', 'U') IS NOT NULL DROP TABLE dbo.BOOKING_STATUS_HISTORY;
IF OBJECT_ID('dbo.ATTENDANCE',         'U') IS NOT NULL DROP TABLE dbo.ATTENDANCE;
IF OBJECT_ID('dbo.COUNTER_DISCOUNTS',  'U') IS NOT NULL DROP TABLE dbo.COUNTER_DISCOUNTS;
IF OBJECT_ID('dbo.BOOKING_FNB',        'U') IS NOT NULL DROP TABLE dbo.BOOKING_FNB;
IF OBJECT_ID('dbo.FNB_ITEMS',          'U') IS NOT NULL DROP TABLE dbo.FNB_ITEMS;
IF OBJECT_ID('dbo.REVIEWS',            'U') IS NOT NULL DROP TABLE dbo.REVIEWS;
IF OBJECT_ID('dbo.NOTIFICATIONS',      'U') IS NOT NULL DROP TABLE dbo.NOTIFICATIONS;
IF OBJECT_ID('dbo.PAYMENTS',           'U') IS NOT NULL DROP TABLE dbo.PAYMENTS;
IF OBJECT_ID('dbo.VOUCHER_HISTORY',    'U') IS NOT NULL DROP TABLE dbo.VOUCHER_HISTORY;
IF OBJECT_ID('dbo.BOOKING_SEATS',      'U') IS NOT NULL DROP TABLE dbo.BOOKING_SEATS;
IF OBJECT_ID('dbo.BOOKINGS',           'U') IS NOT NULL DROP TABLE dbo.BOOKINGS;
IF OBJECT_ID('dbo.DISCOUNT_CODES',     'U') IS NOT NULL DROP TABLE dbo.DISCOUNT_CODES;
IF OBJECT_ID('dbo.CART_ITEMS',         'U') IS NOT NULL DROP TABLE dbo.CART_ITEMS;
IF OBJECT_ID('dbo.CART',               'U') IS NOT NULL DROP TABLE dbo.CART;
IF OBJECT_ID('dbo.FAVORITE_MOVIES',    'U') IS NOT NULL DROP TABLE dbo.FAVORITE_MOVIES;
IF OBJECT_ID('dbo.SEATS',              'U') IS NOT NULL DROP TABLE dbo.SEATS;
IF OBJECT_ID('dbo.SEAT_TYPES',         'U') IS NOT NULL DROP TABLE dbo.SEAT_TYPES;
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
	end_date		DATE NULL,
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

CREATE TABLE dbo.SEAT_TYPES (
    id              INT             NOT NULL IDENTITY(1,1),
    code            VARCHAR(20)     NOT NULL,
    name            NVARCHAR(150)   NOT NULL,
    status          VARCHAR(20)     NOT NULL CONSTRAINT DF_SEAT_TYPES_status DEFAULT 'ACTIVE',
    last_update     DATETIME2       NOT NULL CONSTRAINT DF_SEAT_TYPES_lu DEFAULT GETDATE(),
    default_price   DECIMAL(10,2)   NOT NULL CONSTRAINT DF_SEAT_TYPES_price DEFAULT 0.0,
    color           VARCHAR(20)     NOT NULL CONSTRAINT DF_SEAT_TYPES_color DEFAULT '#10b981',
    CONSTRAINT PK_SEAT_TYPES        PRIMARY KEY (id),
    CONSTRAINT UQ_SEAT_TYPES_code   UNIQUE (code),
    CONSTRAINT CK_ST_status_val     CHECK (status IN ('ACTIVE','INACTIVE'))
);

CREATE TABLE dbo.SEATS (
    id              INT             NOT NULL IDENTITY(1,1),
    hall_id         INT             NOT NULL,
    seat_row        VARCHAR(5)      NOT NULL,
    seat_number     INT             NOT NULL,
    seat_type       VARCHAR(20)     NOT NULL,
    maintenance     BIT             NOT NULL CONSTRAINT DF_SEAT_mn DEFAULT 0,  -- 1=locked for maintenance
    last_update     DATETIME2       NOT NULL CONSTRAINT DF_SEAT_lu DEFAULT GETDATE(),
    CONSTRAINT PK_SEATS             PRIMARY KEY (id),
    CONSTRAINT FK_SEAT_hall         FOREIGN KEY (hall_id) REFERENCES dbo.HALLS(id),
    CONSTRAINT FK_SEAT_type         FOREIGN KEY (seat_type) REFERENCES dbo.SEAT_TYPES(code),
    CONSTRAINT UQ_SEAT_pos          UNIQUE (hall_id, seat_row, seat_number)
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
-- GROUP 6 — Booking & Payment & Discounts
-- ============================================================

CREATE TABLE dbo.DISCOUNT_CODES (
    id                  INT             NOT NULL IDENTITY(1,1),
    code                VARCHAR(50)     NOT NULL,
    discount_type       VARCHAR(20)     NOT NULL,
    discount_value      DECIMAL(18,2)   NOT NULL,
    max_discount_amount DECIMAL(18,2)   NULL,
    min_order_value     DECIMAL(18,2)   NOT NULL CONSTRAINT DF_DC_min_val DEFAULT 0.00,
    max_uses            INT             NOT NULL CONSTRAINT DF_DC_max_uses DEFAULT 100,
    used_count          INT             NOT NULL CONSTRAINT DF_DC_used DEFAULT 0,
    start_date          DATETIME        NOT NULL,
    end_date            DATETIME        NOT NULL,
    status              VARCHAR(20)     NOT NULL CONSTRAINT DF_DC_status DEFAULT 'ACTIVE',
    created_at          DATETIME        NOT NULL CONSTRAINT DF_DC_created DEFAULT GETDATE(),
    last_update         DATETIME        NOT NULL CONSTRAINT DF_DC_lu DEFAULT GETDATE(),
    CONSTRAINT PK_DISCOUNT_CODES        PRIMARY KEY (id),
    CONSTRAINT UQ_DC_code               UNIQUE (code),
    CONSTRAINT CK_DC_type               CHECK (discount_type IN ('FLAT','PERCENT')),
    CONSTRAINT CK_DC_status             CHECK (status IN ('ACTIVE','PAUSED','EXPIRED'))
);

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

CREATE TABLE dbo.VOUCHER_HISTORY (
    id                  INT             NOT NULL IDENTITY(1,1),
    booking_id          INT             NOT NULL,
    user_id             INT             NOT NULL,
    discount_code_id    INT             NOT NULL,
    discount_amount     DECIMAL(18,2)   NOT NULL,
    used_at             DATETIME        NOT NULL CONSTRAINT DF_VH_used DEFAULT GETDATE(),
    CONSTRAINT PK_VOUCHER_HISTORY       PRIMARY KEY (id),
    CONSTRAINT FK_VH_booking            FOREIGN KEY (booking_id) REFERENCES dbo.BOOKINGS(id),
    CONSTRAINT FK_VH_user               FOREIGN KEY (user_id) REFERENCES dbo.[USER](id),
    CONSTRAINT FK_VH_discount           FOREIGN KEY (discount_code_id) REFERENCES dbo.DISCOUNT_CODES(id)
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
-- ============================================================
-- MANAGER - ONE BRANCH RULE
-- A MANAGER account may have 0 or 1 assigned branch only.
-- Zero branch is allowed before Admin assigns a branch.
-- ============================================================

CREATE OR ALTER TRIGGER dbo.TR_STAFF_BRANCH_OneBranchPerManager
ON dbo.STAFF_BRANCH
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    /*
      Check only the Manager accounts affected by the current
      INSERT or UPDATE statement.
    */
    IF EXISTS (
        SELECT sb.user_id
        FROM dbo.STAFF_BRANCH sb
        INNER JOIN inserted i
            ON i.user_id = sb.user_id
        INNER JOIN dbo.[USER] u
            ON u.id = sb.user_id
        WHERE u.role = 'MANAGER'
        GROUP BY sb.user_id
        HAVING COUNT(*) > 1
    )
    BEGIN
        THROW 51000,
              'A Manager account can be assigned to only one Branch.',
              1;
    END
END;
GO

CREATE OR ALTER TRIGGER dbo.TR_USER_OneBranchWhenRoleIsManager
ON dbo.[USER]
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    /*
      This protects the case where a STAFF account was already
      assigned to multiple branches and is later changed to MANAGER.
    */
    IF NOT UPDATE(role)
        RETURN;

    IF EXISTS (
        SELECT u.id
        FROM inserted u
        INNER JOIN dbo.STAFF_BRANCH sb
            ON sb.user_id = u.id
        WHERE u.role = 'MANAGER'
        GROUP BY u.id
        HAVING COUNT(*) > 1
    )
    BEGIN
        THROW 51001,
              'A user with multiple Branch assignments cannot be changed to MANAGER.',
              1;
    END
END;
GO

PRINT 'RapVietDB v3.0 — 30 tables created successfully.';

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

-- Seed data for SEAT_TYPES
INSERT INTO dbo.SEAT_TYPES (code, name, status, default_price, color) VALUES 
('STANDARD', N'Standard (Ghế Thường)', 'ACTIVE', 80000.00, '#10b981'),
('VIP', N'VIP (Ghế Đẹp)', 'ACTIVE', 100000.00, '#2563eb'),
('COUPLE', N'Couple (Ghế Đôi)', 'ACTIVE', 140000.00, '#db2777');

-- Seed data for DISCOUNT_CODES
INSERT INTO dbo.DISCOUNT_CODES (code, discount_type, discount_value, max_discount_amount, min_order_value, max_uses, used_count, start_date, end_date, status) VALUES 
('GIAM20K', 'FLAT', 20000.00, NULL, 100000.00, 200, 0, '2026-01-01', '2027-12-31', 'ACTIVE'),
('KM10PERCENT', 'PERCENT', 10.00, 50000.00, 150000.00, 500, 0, '2026-01-01', '2027-12-31', 'ACTIVE'),
('RAPVIETNEW', 'PERCENT', 20.00, 40000.00, 0.00, 100, 0, '2026-01-01', '2027-12-31', 'ACTIVE');

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

-- ============================================================
-- BASE DATA FOR BRANCH MANAGER TESTING
-- ============================================================

INSERT INTO dbo.CINEMA
(
    name,
    address,
    phone,
    status
)
VALUES
(
    N'RapViet Cinema',
    N'Hà Nội',
    '0240000000',
    'ACTIVE'
);

DECLARE @CinemaId INT = SCOPE_IDENTITY();

INSERT INTO dbo.BRANCHES
(
    cinema_id,
    name,
    address,
    phone,
    open_time,
    close_time,
    status
)
VALUES
(
    @CinemaId,
    N'RapViet Cầu Giấy',
    N'Cầu Giấy, Hà Nội',
    '0241111111',
    '08:00:00',
    '23:00:00',
    'ACTIVE'
),
(
    @CinemaId,
    N'RapViet Hoàn Kiếm',
    N'Hoàn Kiếm, Hà Nội',
    '0242222222',
    '08:00:00',
    '23:00:00',
    'ACTIVE'
);

DECLARE @ManagerId INT = (
    SELECT id
    FROM dbo.[USER]
    WHERE email = 'manager@cinema.com'
);

DECLARE @ManagerBranchId INT = (
    SELECT id
    FROM dbo.BRANCHES
    WHERE name = N'RapViet Cầu Giấy'
);

INSERT INTO dbo.STAFF_BRANCH
(
    user_id,
    branch_id,
    position
)
VALUES
(
    @ManagerId,
    @ManagerBranchId,
    N'Branch Manager'
);

INSERT INTO dbo.HALLS
(
    branch_id,
    name,
    total_seats,
    hall_type,
    status
)
VALUES
(
    @ManagerBranchId,
    N'Phòng 01',
    80,
    'STANDARD',
    'ACTIVE'
),
(
    @ManagerBranchId,
    N'Phòng VIP 01',
    40,
    'VIP',
    'ACTIVE'
);

GO

USE RapVietDB;
GO

/* 1. Thêm hai cột mới để lưu layout phòng chiếu */
ALTER TABLE dbo.HALLS
ADD
    seat_rows INT NULL,
    seats_per_row INT NULL;
GO

/*
 * 2. Tự tạo layout cho các Hall cũ dựa theo total_seats.
 *
 * Ví dụ:
 * 80 ghế  -> 8 hàng × 10 ghế
 * 40 ghế  -> 5 hàng × 8 ghế
 * 120 ghế -> 10 hàng × 12 ghế
 *
 * Vì Hall cũ chỉ lưu tổng ghế nên không thể biết chính xác layout
 * ban đầu. Đoạn này chọn cặp số nhân hợp lý, gần hình vuông nhất.
 */
;WITH Numbers AS (
    SELECT 1 AS number_value

    UNION ALL

    SELECT number_value + 1
    FROM Numbers
    WHERE number_value < 30
),
HallLayouts AS (
    SELECT
        h.id,

        COALESCE(
            (
                SELECT TOP 1 n.number_value
                FROM Numbers n
                WHERE h.total_seats % n.number_value = 0
                  AND h.total_seats / n.number_value <= 30
                ORDER BY
                    ABS(
                        (h.total_seats / n.number_value)
                        - n.number_value
                    ),
                    n.number_value DESC
            ),
            1
        ) AS calculated_rows

    FROM dbo.HALLS h
)
UPDATE h
SET
    seat_rows = l.calculated_rows,
    seats_per_row = h.total_seats / l.calculated_rows
FROM dbo.HALLS h
INNER JOIN HallLayouts l
    ON h.id = l.id
OPTION (MAXRECURSION 30);
GO

/* 3. Kiểm tra trước khi khóa dữ liệu thành NOT NULL */
SELECT
    id,
    name,
    seat_rows,
    seats_per_row,
    total_seats,
    CONCAT(
        seat_rows,
        N' hàng × ',
        seats_per_row,
        N' ghế = ',
        total_seats,
        N' ghế'
    ) AS hall_layout
FROM dbo.HALLS;
GO

/* 4. Sau khi kiểm tra kết quả đúng, bắt buộc hai cột phải có dữ liệu */
ALTER TABLE dbo.HALLS
ALTER COLUMN seat_rows INT NOT NULL;
GO

ALTER TABLE dbo.HALLS
ALTER COLUMN seats_per_row INT NOT NULL;
GO

/* 5. Bảo đảm tổng ghế luôn đúng bằng hàng × ghế mỗi hàng */
ALTER TABLE dbo.HALLS
ADD CONSTRAINT CK_HALL_capacity
CHECK (
    seat_rows > 0
    AND seats_per_row > 0
    AND total_seats = seat_rows * seats_per_row
);
GO

USE RapVietDB;
GO

IF COL_LENGTH('dbo.MOVIES', 'end_date') IS NULL
BEGIN
    ALTER TABLE dbo.MOVIES
    ADD end_date DATE NULL;
END;
GO

-- Cập nhật hệ số nhân cho các loại ghế mặc định
UPDATE dbo.SEAT_TYPES SET default_price = 1.0 WHERE code = 'STANDARD'; -- Ghế thường: giữ nguyên giá gốc (x1.0)
UPDATE dbo.SEAT_TYPES SET default_price = 1.5 WHERE code = 'VIP';      -- Ghế VIP: gấp 1.5 lần giá gốc (x1.5)
UPDATE dbo.SEAT_TYPES SET default_price = 2.0 WHERE code = 'COUPLE';   -- Ghế đôi: gấp đôi giá gốc (x2.0)
/* FINAL DATABASE EXTENSIONS ARE DEFINED BELOW. */
GO
DECLARE @c sysname=(SELECT name FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID('dbo.BOOKINGS') AND name='CK_BK_status');
IF @c IS NOT NULL
BEGIN
    DECLARE @dropBookingConstraint NVARCHAR(400) = N'ALTER TABLE dbo.BOOKINGS DROP CONSTRAINT ' + QUOTENAME(@c) + N';';
    EXEC sp_executesql @dropBookingConstraint;
END;
ALTER TABLE dbo.BOOKINGS ADD CONSTRAINT CK_BK_status CHECK(status IN('PENDING','CONFIRMED','CHECKED_IN','USED','COMPLETED','CANCELLED'));
GO
CREATE TABLE dbo.BOOKING_STATUS_HISTORY(
 id INT IDENTITY PRIMARY KEY, booking_id INT NOT NULL REFERENCES dbo.BOOKINGS(id), previous_status VARCHAR(30) NULL,
 new_status VARCHAR(30) NOT NULL, changed_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(), note NVARCHAR(500) NULL);
CREATE INDEX IX_BSH_booking_changed_at ON dbo.BOOKING_STATUS_HISTORY(booking_id,changed_at,id);
GO
CREATE TRIGGER dbo.TR_BOOKINGS_status_history ON dbo.BOOKINGS AFTER INSERT,UPDATE AS
BEGIN SET NOCOUNT ON;
 INSERT dbo.BOOKING_STATUS_HISTORY(booking_id,previous_status,new_status,note)
 SELECT i.id,d.status,i.status,CASE WHEN d.id IS NULL THEN N'Đơn vé được tạo.' WHEN d.status='PENDING' AND i.status='CONFIRMED' THEN N'Thanh toán thành công.' WHEN i.status='CHECKED_IN' THEN N'Khách đã check-in.' WHEN i.status IN('USED','COMPLETED') THEN N'Vé đã được sử dụng.' WHEN i.status='CANCELLED' THEN N'Đơn vé đã bị hủy.' ELSE N'Trạng thái đơn vé đã được cập nhật.' END
 FROM inserted i LEFT JOIN deleted d ON d.id=i.id WHERE d.id IS NULL OR ISNULL(d.status,'')<>ISNULL(i.status,'');
END;
GO
UPDATE dbo.[USER] SET password_hash='$2a$10$N5GL0.dt8FFdkeB14WCUNerc8PtmMr/fOvkZ6ChIqlODnR8jERZNa',active=1,email_verified=1,last_update=GETDATE()
WHERE email IN('admin@cinema.com','manager@cinema.com','staff@cinema.com','customer@cinema.com');
MERGE dbo.CATEGORY t USING(VALUES(N'Hành động',N'Phim hành động'),(N'Hoạt hình',N'Phim hoạt hình'),(N'Tình cảm',N'Phim tình cảm'),(N'Kinh dị',N'Phim kinh dị'),(N'Hài',N'Phim hài'))s(name,description) ON t.name=s.name WHEN MATCHED THEN UPDATE SET description=s.description,status='ACTIVE' WHEN NOT MATCHED THEN INSERT(name,description,status)VALUES(s.name,s.description,'ACTIVE');
MERGE dbo.LANGUAGES t USING(VALUES(N'Tiếng Việt (Lồng tiếng)','VI'),(N'Tiếng Anh (Phụ đề Việt)','EN'),(N'Tiếng Hàn (Phụ đề Việt)','KO'))s(name,code) ON t.code=s.code WHEN MATCHED THEN UPDATE SET name=s.name,status='ACTIVE' WHEN NOT MATCHED THEN INSERT(name,code,status)VALUES(s.name,s.code,'ACTIVE');
GO
INSERT dbo.MOVIES_CATEGORY(movie_id,category_id)
SELECT x.movie_id,c.id FROM(VALUES(1,N'Hoạt hình'),(2,N'Hành động'),(3,N'Tình cảm'),(4,N'Hoạt hình'),(5,N'Hành động'))x(movie_id,name) JOIN dbo.MOVIES m ON m.id=x.movie_id JOIN dbo.CATEGORY c ON c.name=x.name WHERE NOT EXISTS(SELECT 1 FROM dbo.MOVIES_CATEGORY z WHERE z.movie_id=x.movie_id AND z.category_id=c.id);
INSERT dbo.MOVIE_LANGUAGES(movie_id,language_id,subtitle) SELECT m.id,l.id,CASE WHEN l.code='VI' THEN 0 ELSE 1 END FROM dbo.MOVIES m CROSS JOIN dbo.LANGUAGES l WHERE m.id BETWEEN 1 AND 5 AND NOT EXISTS(SELECT 1 FROM dbo.MOVIE_LANGUAGES x WHERE x.movie_id=m.id AND x.language_id=l.id);
DECLARE @staff INT=(SELECT id FROM dbo.[USER] WHERE email='staff@cinema.com'),@branch INT=(SELECT TOP 1 id FROM dbo.BRANCHES ORDER BY id);
IF NOT EXISTS(SELECT 1 FROM dbo.STAFF_BRANCH WHERE user_id=@staff AND branch_id=@branch) INSERT dbo.STAFF_BRANCH(user_id,branch_id,position)VALUES(@staff,@branch,N'Nhân viên quầy');
INSERT dbo.BRANCH_MOVIES(branch_id,movie_id) SELECT b.id,m.id FROM dbo.BRANCHES b CROSS JOIN dbo.MOVIES m WHERE NOT EXISTS(SELECT 1 FROM dbo.BRANCH_MOVIES x WHERE x.branch_id=b.id AND x.movie_id=m.id);
INSERT dbo.HALL_MOVIES(hall_id,movie_id) SELECT h.id,m.id FROM dbo.HALLS h CROSS JOIN dbo.MOVIES m WHERE NOT EXISTS(SELECT 1 FROM dbo.HALL_MOVIES x WHERE x.hall_id=h.id AND x.movie_id=m.id);
GO
;WITH n AS(SELECT 1 v UNION ALL SELECT v+1 FROM n WHERE v<30),p AS(SELECT h.id,CHAR(64+r.v) row_,c.v no_,CASE WHEN h.hall_type='VIP' AND c.v>h.seats_per_row-2 THEN 'VIP' ELSE 'STANDARD' END typ FROM dbo.HALLS h JOIN n r ON r.v<=h.seat_rows JOIN n c ON c.v<=h.seats_per_row)
INSERT dbo.SEATS(hall_id,seat_row,seat_number,seat_type,maintenance) SELECT id,row_,no_,typ,0 FROM p WHERE NOT EXISTS(SELECT 1 FROM dbo.SEATS s WHERE s.hall_id=p.id AND s.seat_row=p.row_ AND s.seat_number=p.no_) OPTION(MAXRECURSION 30);
GO
DECLARE @h1 INT=(SELECT TOP 1 id FROM dbo.HALLS ORDER BY id),@h2 INT=(SELECT TOP 1 id FROM dbo.HALLS ORDER BY id DESC),@d DATETIME2=CAST(CAST(GETDATE() AS DATE) AS DATETIME2);
INSERT dbo.SHOWTIMES(hall_id,movie_id,start_time,end_time,base_price,status) SELECT @h1,1,DATEADD(HOUR,10,DATEADD(DAY,-2,@d)),DATEADD(HOUR,12,DATEADD(DAY,-2,@d)),80000,'COMPLETED' WHERE NOT EXISTS(SELECT 1 FROM dbo.SHOWTIMES);
INSERT dbo.SHOWTIMES(hall_id,movie_id,start_time,end_time,base_price,status) SELECT @h1,2,DATEADD(HOUR,10,DATEADD(DAY,1,@d)),DATEADD(HOUR,12,DATEADD(DAY,1,@d)),90000,'ON_SALE' WHERE NOT EXISTS(SELECT 1 FROM dbo.SHOWTIMES WHERE movie_id=2);
INSERT dbo.SHOWTIMES(hall_id,movie_id,start_time,end_time,base_price,status) SELECT @h2,3,DATEADD(HOUR,14,DATEADD(DAY,1,@d)),DATEADD(HOUR,16,DATEADD(DAY,1,@d)),100000,'ON_SALE' WHERE NOT EXISTS(SELECT 1 FROM dbo.SHOWTIMES WHERE movie_id=3);
GO
DECLARE @u INT=(SELECT id FROM dbo.[USER] WHERE email='customer@cinema.com'),@past INT=(SELECT TOP 1 id FROM dbo.SHOWTIMES WHERE end_time<GETDATE() ORDER BY end_time DESC),@future INT=(SELECT TOP 1 id FROM dbo.SHOWTIMES WHERE start_time>GETDATE() ORDER BY start_time),@s1 INT,@s2 INT,@b INT;
SELECT TOP 1 @s1=s.id FROM dbo.SEATS s JOIN dbo.SHOWTIMES t ON t.hall_id=s.hall_id WHERE t.id=@past ORDER BY s.id; SELECT TOP 1 @s2=s.id FROM dbo.SEATS s JOIN dbo.SHOWTIMES t ON t.hall_id=s.hall_id WHERE t.id=@future ORDER BY s.id DESC;
IF NOT EXISTS(SELECT 1 FROM dbo.BOOKINGS WHERE user_id=@u AND showtime_id=@past) BEGIN INSERT dbo.BOOKINGS(user_id,showtime_id,source,status,total_price,qr_code)VALUES(@u,@past,'ONLINE','USED',80000,'DEMO-USED');SET @b=SCOPE_IDENTITY();INSERT dbo.BOOKING_SEATS(booking_id,seat_id,price)VALUES(@b,@s1,80000);INSERT dbo.PAYMENTS(booking_id,type,method,transaction_id,status,amount,paid_at,gateway)VALUES(@b,'ONLINE','VNPAY','DEMO-USED','SUCCESS',80000,GETDATE(),'VNPAY');END;
IF NOT EXISTS(SELECT 1 FROM dbo.BOOKINGS WHERE user_id=@u AND showtime_id=@future) BEGIN INSERT dbo.BOOKINGS(user_id,showtime_id,source,status,total_price,qr_code)VALUES(@u,@future,'ONLINE','PENDING',90000,'DEMO-CONFIRMED');SET @b=SCOPE_IDENTITY();INSERT dbo.BOOKING_SEATS(booking_id,seat_id,price)VALUES(@b,@s2,90000);INSERT dbo.PAYMENTS(booking_id,type,method,transaction_id,status,amount,paid_at,gateway)VALUES(@b,'ONLINE','VNPAY','DEMO-CONFIRMED','SUCCESS',90000,GETDATE(),'VNPAY');UPDATE dbo.BOOKINGS SET status='CONFIRMED' WHERE id=@b;END;
GO
/* Extra demo volume: movies, customers, branches, halls, showtimes and bookings. */
DECLARE @hash VARCHAR(255)='$2a$10$N5GL0.dt8FFdkeB14WCUNerc8PtmMr/fOvkZ6ChIqlODnR8jERZNa';
MERGE dbo.[USER] t USING(VALUES
 (N'Khách hàng Demo 02','customer02@cinema.com','0900000005'),(N'Khách hàng Demo 03','customer03@cinema.com','0900000006'),
 (N'Khách hàng Demo 04','customer04@cinema.com','0900000007'),(N'Khách hàng Demo 05','customer05@cinema.com','0900000008'),
 (N'Khách hàng Demo 06','customer06@cinema.com','0900000009')
)s(full_name,email,phone) ON t.email=s.email
WHEN MATCHED THEN UPDATE SET full_name=s.full_name,phone=s.phone,password_hash=@hash,active=1,email_verified=1
WHEN NOT MATCHED THEN INSERT(full_name,email,password_hash,google_id,phone,role,active,email_verified) VALUES(s.full_name,s.email,@hash,CONCAT('local_',REPLACE(s.email,'@cinema.com','')),s.phone,'CUSTOMER',1,1);

MERGE dbo.MOVIES t USING(VALUES
 (N'Chuyến Tàu Sao Băng',118,N'Cuộc phiêu lưu của nhóm bạn trên chuyến tàu bí ẩn.',-10,20,'NOW_SHOWING'),
 (N'Mật Mã Cuối Cùng',105,N'Một chuyên gia giải mã phải ngăn chặn cuộc tấn công toàn cầu.',-5,25,'NOW_SHOWING'),
 (N'Nhà Có Khách',98,N'Bộ phim hài gia đình nhẹ nhàng và ấm áp.',-2,30,'NOW_SHOWING'),
 (N'Bóng Đêm Thành Phố',122,N'Một vụ án bí ẩn xảy ra giữa lòng thành phố.',0,35,'COMING_SOON'),
 (N'Hành Tinh Xanh',110,N'Hành trình khám phá một thế giới mới.',3,40,'COMING_SOON'),
 (N'Ngày Mai Rực Rỡ',115,N'Câu chuyện về ước mơ của những người trẻ.',-20,-1,'ENDED'),
 (N'Kẻ Canh Gác',130,N'Phim hành động gay cấn.',-15,15,'NOW_SHOWING'),
 (N'Vương Quốc Mây',100,N'Hoạt hình phiêu lưu dành cho cả gia đình.',1,45,'COMING_SOON')
)s(title,duration_min,description,start_offset,end_offset,status) ON t.title=s.title
WHEN NOT MATCHED THEN INSERT(title,duration_min,description,release_date,end_date,status,actor,director)
 VALUES(s.title,s.duration_min,s.description,DATEADD(DAY,s.start_offset,CAST(GETDATE() AS DATE)),DATEADD(DAY,s.end_offset,CAST(GETDATE() AS DATE)),s.status,N'Diễn viên RapViet',N'Đạo diễn RapViet');
GO

DECLARE @cinema INT=(SELECT TOP 1 id FROM dbo.CINEMA ORDER BY id);
MERGE dbo.BRANCHES t USING(VALUES
 (N'RapViet Hai Bà Trưng',N'458 Bạch Mai, Hai Bà Trưng, Hà Nội','0243333333'),
 (N'RapViet Thanh Xuân',N'125 Nguyễn Trãi, Thanh Xuân, Hà Nội','0244444444')
)s(name,address,phone) ON t.name=s.name
WHEN NOT MATCHED THEN INSERT(cinema_id,name,address,phone,open_time,close_time,status) VALUES(@cinema,s.name,s.address,s.phone,'08:00','23:00','ACTIVE');

INSERT dbo.HALLS(branch_id,name,total_seats,hall_type,status,seat_rows,seats_per_row)
SELECT b.id,N'Phòng Standard 01',60,'STANDARD','ACTIVE',6,10 FROM dbo.BRANCHES b
WHERE b.name IN(N'RapViet Hai Bà Trưng',N'RapViet Thanh Xuân') AND NOT EXISTS(SELECT 1 FROM dbo.HALLS h WHERE h.branch_id=b.id AND h.name=N'Phòng Standard 01');
GO

;WITH n AS(SELECT 1 v UNION ALL SELECT v+1 FROM n WHERE v<30),p AS(SELECT h.id,CHAR(64+r.v) row_,c.v no_ FROM dbo.HALLS h JOIN n r ON r.v<=h.seat_rows JOIN n c ON c.v<=h.seats_per_row)
INSERT dbo.SEATS(hall_id,seat_row,seat_number,seat_type,maintenance) SELECT id,row_,no_,'STANDARD',0 FROM p WHERE NOT EXISTS(SELECT 1 FROM dbo.SEATS s WHERE s.hall_id=p.id AND s.seat_row=p.row_ AND s.seat_number=p.no_) OPTION(MAXRECURSION 30);
INSERT dbo.BRANCH_MOVIES(branch_id,movie_id) SELECT b.id,m.id FROM dbo.BRANCHES b CROSS JOIN dbo.MOVIES m WHERE NOT EXISTS(SELECT 1 FROM dbo.BRANCH_MOVIES x WHERE x.branch_id=b.id AND x.movie_id=m.id);
INSERT dbo.HALL_MOVIES(hall_id,movie_id) SELECT h.id,m.id FROM dbo.HALLS h CROSS JOIN dbo.MOVIES m WHERE NOT EXISTS(SELECT 1 FROM dbo.HALL_MOVIES x WHERE x.hall_id=h.id AND x.movie_id=m.id);
GO

DECLARE @base DATETIME2=CAST(CAST(GETDATE() AS DATE) AS DATETIME2);
INSERT dbo.SHOWTIMES(hall_id,movie_id,start_time,end_time,base_price,status)
SELECT h.id,m.id,DATEADD(HOUR,10+((m.id+h.id)%8),DATEADD(DAY,1+(m.id%6),@base)),DATEADD(HOUR,12+((m.id+h.id)%8),DATEADD(DAY,1+(m.id%6),@base)),80000+((m.id%4)*10000),'ON_SALE'
FROM dbo.HALLS h CROSS JOIN dbo.MOVIES m WHERE m.id>5 AND m.status<>'ENDED'
AND NOT EXISTS(SELECT 1 FROM dbo.SHOWTIMES s WHERE s.hall_id=h.id AND s.movie_id=m.id);
GO

DECLARE @i INT=2,@uid INT,@stid INT,@seat INT,@bid INT;
WHILE @i<=6
BEGIN
 SELECT @uid=id FROM dbo.[USER] WHERE email=CONCAT('customer0',@i,'@cinema.com');
 SELECT @stid=MIN(id) FROM dbo.SHOWTIMES WHERE start_time>GETDATE() AND id%5=@i%5;
 SELECT TOP 1 @seat=s.id FROM dbo.SEATS s JOIN dbo.SHOWTIMES st ON st.hall_id=s.hall_id WHERE st.id=@stid ORDER BY s.id;
 IF @uid IS NOT NULL AND @stid IS NOT NULL AND @seat IS NOT NULL AND NOT EXISTS(SELECT 1 FROM dbo.BOOKINGS WHERE user_id=@uid AND showtime_id=@stid)
 BEGIN
  INSERT dbo.BOOKINGS(user_id,showtime_id,source,status,total_price,qr_code) VALUES(@uid,@stid,'ONLINE','CONFIRMED',90000,CONCAT('DEMO-',@i)); SET @bid=SCOPE_IDENTITY();
  INSERT dbo.BOOKING_SEATS(booking_id,seat_id,price) VALUES(@bid,@seat,90000);
  INSERT dbo.PAYMENTS(booking_id,type,method,transaction_id,status,amount,paid_at,gateway) VALUES(@bid,'ONLINE','VNPAY',CONCAT('DEMO-',@bid),'SUCCESS',90000,GETDATE(),'VNPAY');
 END;
 SET @i=@i+1;
END;
GO
PRINT 'RapViet FINAL database created. Demo password: 123456';
