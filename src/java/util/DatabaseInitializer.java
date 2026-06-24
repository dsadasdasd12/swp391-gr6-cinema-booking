package util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import controller.StaffAccountServlet;

@WebListener
public class DatabaseInitializer implements ServletContextListener {

    private ServletContextEvent servletContextEvent;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.servletContextEvent = sce;
        Thread seedThread = new Thread(this::runStartupTasks, "rapviet-db-seed");
        seedThread.setDaemon(true);
        seedThread.start();
    }

    private void runStartupTasks() {
        runSeedData();
        finishStartup();
    }

    private void finishStartup() {
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return;
        }
        VietnameseDataRepair.repairAll(conn);
        if (servletContextEvent != null) {
            String webRoot = servletContextEvent.getServletContext().getRealPath("/");
            if (webRoot != null) {
                MoviePosterService.ensurePosters(conn, webRoot);
            }
        }
        BookingQrSeedService.generateMissingQrCodes(conn);
    }

    private void runSeedData() {
        System.out.println("==> Khởi chạy Trình khởi tạo dữ liệu mẫu RapViệt (nền)...");
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            System.err.println("==> Không thể kết nối cơ sở dữ liệu để kiểm tra dữ liệu mẫu!");
            return;
        }

        try {
            conn.setAutoCommit(true);

            // 0. Check & Insert CINEMA
            if (isTableEmpty(conn, "dbo.CINEMA")) {
                System.out.println("==> Đang nạp dữ liệu mẫu cho dbo.CINEMA...");
                executeSql(conn, "SET IDENTITY_INSERT dbo.CINEMA ON; " +
                                 "INSERT INTO dbo.CINEMA (id, name, address, phone, logo_url, status) " +
                                 "VALUES (1, N'Hệ thống Rạp Việt', N'Hà Nội, Việt Nam', '1900.1000', '/assets/img/logo.png', 'ACTIVE'); " +
                                 "SET IDENTITY_INSERT dbo.CINEMA OFF;");
            }

            // 1. Check & Insert BRANCHES
            if (isTableEmpty(conn, "dbo.BRANCHES")) {
                System.out.println("==> Đang nạp dữ liệu mẫu cho dbo.BRANCHES...");
                insertBranch(conn, 1, 1, "Rạp Việt Nguyễn Trãi", "Số 266 Đường Nguyễn Trãi, Thanh Xuân, Hà Nội", "0243.888.999", "08:00:00", "23:30:00");
                insertBranch(conn, 2, 1, "Rạp Việt Nguyễn Du", "Số 116 Đường Nguyễn Du, Quận 1, TP. Hồ Chí Minh", "0283.777.666", "08:30:00", "23:45:00");
                insertBranch(conn, 3, 1, "Rạp Việt Lê Lợi", "Số 88 Đường Lê Lợi, Hải Châu, Đà Nẵng", "0236.555.444", "09:00:00", "23:00:00");
            }

            // 2. Check & Insert CATEGORY
            if (isTableEmpty(conn, "dbo.CATEGORY")) {
                System.out.println("==> Đang nạp dữ liệu mẫu cho dbo.CATEGORY...");
                insertCategory(conn, 1, "Hành Động", "Phim hành động, võ thuật kịch tính", "ACTIVE");
                insertCategory(conn, 2, "Hài Hước", "Phim hài, dí dỏm, vui nhộn", "ACTIVE");
                insertCategory(conn, 3, "Tình Cảm", "Phim tâm lý, tình cảm lãng mạn", "ACTIVE");
                insertCategory(conn, 4, "Kinh Dị", "Phim kinh dị, giật gân rùng rợn", "ACTIVE");
                insertCategory(conn, 5, "Hoạt Hình", "Phim hoạt hình 3D, phiêu lưu gia đình", "ACTIVE");
            }

            // 3. Check & Insert LANGUAGES
            if (isTableEmpty(conn, "dbo.LANGUAGES")) {
                System.out.println("==> Đang nạp dữ liệu mẫu cho dbo.LANGUAGES...");
                insertLanguage(conn, 1, "Tiếng Việt (Lồng tiếng)", "VI", "ACTIVE");
                insertLanguage(conn, 2, "Tiếng Anh (Phụ đề Việt)", "EN", "ACTIVE");
                insertLanguage(conn, 3, "Tiếng Hàn (Phụ đề Việt)", "KO", "ACTIVE");
            }

            // 4. Check & Insert MOVIES
            if (isTableEmpty(conn, "dbo.MOVIES")) {
                System.out.println("==> Đang nạp dữ liệu mẫu cho dbo.MOVIES...");
                insertMovie(conn, 1, "Lật Mặt 7: Một Điều Ước", 138, "Bộ phim gia đình đầy cảm xúc của Lý Hải kể về câu chuyện của người mẹ tảo tần và các con.", "2026-04-30", "NOW_SHOWING", "assets/uploads/movies/1/poster.webp", "https://youtube.com/embed/dQw4w9WgXcQ", "Thanh Hiền, Trương Minh Cường, Đinh Y Nhung", "Lý Hải");
                insertMovie(conn, 2, "Mai - Trấn Thành", 131, "Mai là câu chuyện tình cảm trắc trở của một người phụ nữ ngoài ba mươi phải gánh chịu nhiều bất công cuộc sống.", "2026-02-10", "NOW_SHOWING", "assets/uploads/movies/2/poster.webp", "https://youtube.com/embed/dQw4w9WgXcQ", "Phương Anh Đào, Tuấn Trần, Hồng Đào", "Trấn Thành");
                insertMovie(conn, 3, "Dune: Hành Tinh Cát 2", 166, "Phần tiếp theo của siêu phẩm sử thi viễn tưởng kể về hành trình phục hận của Paul Atreides.", "2026-03-01", "NOW_SHOWING", "assets/uploads/movies/3/poster.webp", "https://youtube.com/embed/dQw4w9WgXcQ", "Timothée Chalamet, Zendaya, Rebecca Ferguson", "Denis Villeneuve");
                insertMovie(conn, 4, "Inside Out 2: Những Mảnh Ghép Cảm Xúc", 96, "Những cảm xúc mới đầy thú vị xuất hiện bên trong đầu của Riley khi bước vào tuổi dậy thì.", "2026-06-15", "COMING_SOON", "assets/uploads/movies/4/poster.webp", "https://youtube.com/embed/dQw4w9WgXcQ", "Amy Poehler, Phyllis Smith, Lewis Black", "Kelsey Mann");

                // Mapping Movies to Categories
                executeSql(conn, "INSERT INTO dbo.MOVIES_CATEGORY (movie_id, category_id) VALUES (1, 3)");
                executeSql(conn, "INSERT INTO dbo.MOVIES_CATEGORY (movie_id, category_id) VALUES (2, 3)");
                executeSql(conn, "INSERT INTO dbo.MOVIES_CATEGORY (movie_id, category_id) VALUES (3, 1)");
                executeSql(conn, "INSERT INTO dbo.MOVIES_CATEGORY (movie_id, category_id) VALUES (4, 5)");

                // Mapping Movies to Languages
                executeSql(conn, "INSERT INTO dbo.MOVIE_LANGUAGES (movie_id, language_id, subtitle) VALUES (1, 1, 0)");
                executeSql(conn, "INSERT INTO dbo.MOVIE_LANGUAGES (movie_id, language_id, subtitle) VALUES (2, 1, 0)");
                executeSql(conn, "INSERT INTO dbo.MOVIE_LANGUAGES (movie_id, language_id, subtitle) VALUES (3, 2, 1)");
                executeSql(conn, "INSERT INTO dbo.MOVIE_LANGUAGES (movie_id, language_id, subtitle) VALUES (4, 1, 0)");
            }

            // 5. Check & Insert Users
            if (isTableEmpty(conn, "dbo.[USER]")) {
                System.out.println("==> Đang nạp dữ liệu mẫu cho dbo.[USER]...");
                String adminPw = util.PasswordUtil.hashPassword("123");
                
                // Admin Account
                insertUser(conn, 1, "Quản trị viên Hệ thống", "admin@rapviet.vn", adminPw, "0987.654.321", "ADMIN", 1, 1);
                
                // Manager Account
                insertUser(conn, 2, "Trưởng Chi Nhánh Hà Nội", "manager@rapviet.vn", adminPw, "0912.345.678", "MANAGER", 1, 1);
                executeSql(conn, "INSERT INTO dbo.STAFF_BRANCH (user_id, branch_id, position) VALUES (2, 1, 'TRƯỞNG CHI NHÁNH')");
                
                // Staff Account
                insertUser(conn, 3, "Nhân Viên Quầy HN", "staff@rapviet.vn", adminPw, "0966.777.888", "STAFF", 1, 1);
                executeSql(conn, "INSERT INTO dbo.STAFF_BRANCH (user_id, branch_id, position) VALUES (3, 1, 'NHÂN VIÊN QUẦY')");

                // Customer Accounts
                insertUser(conn, 4, "Phạm Minh Hoàng", "customer@gmail.com", adminPw, "0944.555.666", "CUSTOMER", 1, 1);
                insertUser(conn, 5, "Lê Thu Trang", "tranglt@gmail.com", adminPw, "0933.222.111", "CUSTOMER", 1, 1);
                insertUser(conn, 6, "Nguyễn Tuấn Anh", "tuananh@gmail.com", adminPw, "0900.111.222", "CUSTOMER", 0, 1); // Blocked user
                insertUser(conn, 7, "Trần Quốc Bảo", "baotq@gmail.com", adminPw, "0988.999.000", "CUSTOMER", 1, 0); // Pending verification user
            }

            // 6. Check & Insert Halls
            if (isTableEmpty(conn, "dbo.HALLS")) {
                System.out.println("==> Đang nạp dữ liệu mẫu cho dbo.HALLS...");
                executeSql(conn, "INSERT INTO dbo.HALLS (branch_id, name, total_seats, hall_type, status) VALUES (1, 'Phòng Chiếu VIP 1', 50, 'VIP', 'ACTIVE')");
                executeSql(conn, "INSERT INTO dbo.HALLS (branch_id, name, total_seats, hall_type, status) VALUES (1, 'Phòng Chiếu IMAX 2', 100, 'IMAX', 'ACTIVE')");
                executeSql(conn, "INSERT INTO dbo.HALLS (branch_id, name, total_seats, hall_type, status) VALUES (2, 'Phòng Chiếu VIP 1', 50, 'VIP', 'ACTIVE')");
            }

            // 7. Check & Insert SHOWTIMES, BOOKINGS, and SEATS
            if (isTableEmpty(conn, "dbo.SHOWTIMES")) {
                System.out.println("==> Đang nạp dữ liệu mẫu cho dbo.SHOWTIMES và BOOKINGS...");
                
                // Let's populate 50 seats for Hall 1
                for (char row = 'A'; row <= 'E'; row++) {
                    for (int num = 1; num <= 10; num++) {
                        String type = (row == 'E') ? "COUPLE" : (row >= 'C') ? "VIP" : "STANDARD";
                        executeSql(conn, "INSERT INTO dbo.SEATS (hall_id, seat_row, seat_number, seat_type, maintenance) VALUES (1, '" + row + "', " + num + ", '" + type + "', 0)");
                    }
                }
                // Let's populate 100 seats for Hall 2
                for (char row = 'A'; row <= 'J'; row++) {
                    for (int num = 1; num <= 10; num++) {
                        String type = (row == 'J') ? "COUPLE" : (row >= 'G') ? "VIP" : "STANDARD";
                        executeSql(conn, "INSERT INTO dbo.SEATS (hall_id, seat_row, seat_number, seat_type, maintenance) VALUES (2, '" + row + "', " + num + ", '" + type + "', 0)");
                    }
                }
                // Let's populate 50 seats for Hall 3 (in branch 2)
                for (char row = 'A'; row <= 'E'; row++) {
                    for (int num = 1; num <= 10; num++) {
                        String type = (row == 'E') ? "COUPLE" : (row >= 'C') ? "VIP" : "STANDARD";
                        executeSql(conn, "INSERT INTO dbo.SEATS (hall_id, seat_row, seat_number, seat_type, maintenance) VALUES (3, '" + row + "', " + num + ", '" + type + "', 0)");
                    }
                }

                // Query seats to map them in memory for booking inserts
                List<Integer> seatsHall1 = getSeatsForHall(conn, 1);
                List<Integer> seatsHall2 = getSeatsForHall(conn, 2);
                List<Integer> seatsHall3 = getSeatsForHall(conn, 3);

                // 14 ngày gần đây + 3 ngày tới (đủ cho báo cáo, deploy nhanh hơn)
                java.util.Random rand = new java.util.Random(42);
                int showtimeIdCounter = 1;

                for (int day = -14; day <= 3; day++) {
                    String dateStr = java.time.LocalDate.now().plusDays(day).toString();

                    // Showtime 1: Movie 1, Hall 1, base price 120000, 10:00
                    insertShowtime(conn, showtimeIdCounter, 1, 1, dateStr + " 10:00:00", dateStr + " 12:18:00", 120000);
                    generateBookings(conn, showtimeIdCounter, seatsHall1, 120000, dateStr + " 10:00:00", rand);
                    showtimeIdCounter++;

                    // Showtime 2: Movie 2, Hall 2, base price 110000, 14:00
                    insertShowtime(conn, showtimeIdCounter, 2, 2, dateStr + " 14:00:00", dateStr + " 16:11:00", 110000);
                    generateBookings(conn, showtimeIdCounter, seatsHall2, 110000, dateStr + " 14:00:00", rand);
                    showtimeIdCounter++;

                    // Showtime 3: Movie 3, Hall 3, base price 150000, 19:00
                    insertShowtime(conn, showtimeIdCounter, 3, 3, dateStr + " 19:00:00", dateStr + " 21:46:00", 150000);
                    generateBookings(conn, showtimeIdCounter, seatsHall3, 150000, dateStr + " 19:00:00", rand);
                    showtimeIdCounter++;
                }
            }

            // 8. Mẫu thông báo email
            seedNotificationSamples(conn);

            System.out.println("==> Khởi chạy dữ liệu mẫu hoàn thành thành công!");

        } catch (Exception e) {
            System.err.println("==> Lỗi nghiêm trọng khi khởi tạo dữ liệu mẫu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}

    // Utility checkers
    private boolean isTableEmpty(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            // Ignore / tables might not exist
        }
        return false;
    }

    private static void setN(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.NVARCHAR);
        } else {
            ps.setNString(index, value);
        }
    }

    private void executeSql(Connection conn, String sql) {
        try (Statement s = conn.createStatement()) {
            s.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("==> Không thể chạy câu lệnh SQL: " + sql + " - " + e.getMessage());
        }
    }

    // Insert Branch
    private void insertBranch(Connection conn, int id, int cinemaId, String name, String address, String phone, String openTime, String closeTime) {
        String sql = "SET IDENTITY_INSERT dbo.BRANCHES ON; " +
                     "INSERT INTO dbo.BRANCHES (id, cinema_id, name, address, phone, open_time, close_time, status, last_update) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', GETDATE); " +
                     "SET IDENTITY_INSERT dbo.BRANCHES OFF;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, cinemaId);
            setN(ps, 3, name);
            setN(ps, 4, address);
            ps.setString(5, phone);
            ps.setString(6, openTime);
            ps.setString(7, closeTime);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Insert Category
    private void insertCategory(Connection conn, int id, String name, String description, String status) {
        String sql = "SET IDENTITY_INSERT dbo.CATEGORY ON; " +
                     "INSERT INTO dbo.CATEGORY (id, name, description, status, last_update) " +
                     "VALUES (?, ?, ?, ?, GETDATE); " +
                     "SET IDENTITY_INSERT dbo.CATEGORY OFF;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            setN(ps, 2, name);
            setN(ps, 3, description);
            ps.setString(4, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Insert Language
    private void insertLanguage(Connection conn, int id, String name, String code, String status) {
        String sql = "SET IDENTITY_INSERT dbo.LANGUAGES ON; " +
                     "INSERT INTO dbo.LANGUAGES (id, name, code, status, last_update) " +
                     "VALUES (?, ?, ?, ?, GETDATE); " +
                     "SET IDENTITY_INSERT dbo.LANGUAGES OFF;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            setN(ps, 2, name);
            ps.setString(3, code);
            ps.setString(4, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Insert Movie
    private void insertMovie(Connection conn, int id, String title, int duration, String description, String relDate, String status, String poster, String trailer, String actor, String director) {
        String sql = "SET IDENTITY_INSERT dbo.MOVIES ON; " +
                     "INSERT INTO dbo.MOVIES (id, title, duration_min, description, release_date, status, poster_url, trailer_url, actor, director, last_update) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE); " +
                     "SET IDENTITY_INSERT dbo.MOVIES OFF;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            setN(ps, 2, title);
            ps.setInt(3, duration);
            setN(ps, 4, description);
            ps.setString(5, relDate);
            ps.setString(6, status);
            ps.setString(7, poster);
            ps.setString(8, trailer);
            setN(ps, 9, actor);
            setN(ps, 10, director);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Insert User
    private void insertUser(Connection conn, int id, String fullName, String email, String pwHash, String phone, String role, int active, int emailVerified) {
        String sql = "SET IDENTITY_INSERT dbo.[USER] ON; " +
                     "INSERT INTO dbo.[USER] (id, full_name, email, password_hash, phone, role, active, email_verified, created_at, last_update) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE, GETDATE); " +
                     "SET IDENTITY_INSERT dbo.[USER] OFF;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            setN(ps, 2, fullName);
            ps.setString(3, email);
            ps.setString(4, pwHash);
            ps.setString(5, phone);
            ps.setString(6, role);
            ps.setInt(7, active);
            ps.setInt(8, emailVerified);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get Seats For Hall
    private List<Integer> getSeatsForHall(Connection conn, int hallId) {
        List<Integer> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM dbo.SEATS WHERE hall_id = ? ORDER BY id ASC")) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Insert Showtime
    private void insertShowtime(Connection conn, int id, int hallId, int movieId, String startTime, String endTime, double basePrice) {
        String sql = "SET IDENTITY_INSERT dbo.SHOWTIMES ON; " +
                     "INSERT INTO dbo.SHOWTIMES (id, hall_id, movie_id, start_time, end_time, base_price, status, last_update) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'ON_SALE', GETDATE); " +
                     "SET IDENTITY_INSERT dbo.SHOWTIMES OFF;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, hallId);
            ps.setInt(3, movieId);
            ps.setString(4, startTime);
            ps.setString(5, endTime);
            ps.setDouble(6, basePrice);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("==> Không thể nạp Showtime ID " + id + ": " + e.getMessage());
        }
    }

    private void seedNotificationSamples(Connection conn) {
        if (!isTableEmpty(conn, "dbo.NOTIFICATIONS")) {
            return;
        }
        System.out.println("==> Đang nạp dữ liệu mẫu cho dbo.NOTIFICATIONS...");
        insertNotification(conn, 4, "BOOKING_CONFIRM", "Booking #1 — Lật Mặt 7: Một Điều Ước",
                "Gửi tới: customer@gmail.com", "SENT");
        insertNotification(conn, 4, "PAYMENT_CONFIRM", "Booking #1 — Thanh toán 240000 VNĐ",
                "Thanh toán VNPAY thành công", "SENT");
        insertNotification(conn, 5, "BOOKING_CONFIRM", "Booking #2 — Mai",
                "Gửi tới: tranglt@gmail.com", "SENT");
        insertNotification(conn, null, "PROMOTION", "Ưu đãi cuối tuần — Giảm 20% combo bắp nước",
                "Gửi broadcast tới khách hàng active", "SENT");
        insertNotification(conn, 4, "SYSTEM", "Nhắc suất chiếu sắp bắt đầu",
                "SMTP chưa cấu hình — email đang chờ [retry=1]", "FAILED");
    }

    private void insertNotification(Connection conn, Integer userId, String type, String title, String message, String status) {
        String sql = "INSERT INTO dbo.NOTIFICATIONS (user_id, branch_id, sent_by, type, title, message, status) "
                + "VALUES (?, NULL, 1, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, type);
            setN(ps, 3, title);
            setN(ps, 4, message);
            ps.setString(5, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("==> Không thể nạp NOTIFICATIONS: " + e.getMessage());
        }
    }

    // Generate Bookings
    private void generateBookings(Connection conn, int showtimeId, List<Integer> seatIds, double price, String showtimeTime, java.util.Random rand) {
        if (seatIds.isEmpty()) return;

        // Generate 3 to 6 bookings per showtime
        int numBookings = 3 + rand.nextInt(4);
        int seatIndex = 0;

        for (int b = 0; b < numBookings; b++) {
            if (seatIndex >= seatIds.size() - 3) break;

            // 1 to 3 seats per booking
            int seatsToBook = 1 + rand.nextInt(3);
            double totalPrice = price * seatsToBook;
            int userId = 4 + rand.nextInt(2); // user 4 or 5

            String source = (rand.nextBoolean()) ? "ONLINE" : "WALKIN";
            String status = (rand.nextInt(10) < 9) ? "CONFIRMED" : "CHECKED_IN";

            String sqlBooking = "INSERT INTO dbo.BOOKINGS (user_id, showtime_id, source, status, total_price, qr_code, booked_at, last_update) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE)";

            int bookingId = -1;
            try (PreparedStatement ps = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, showtimeId);
                ps.setString(3, source);
                ps.setString(4, status);
                ps.setDouble(5, totalPrice);
                ps.setNull(6, java.sql.Types.VARCHAR);
                
                int minusHours = 1 + rand.nextInt(6);
                java.time.LocalDateTime bookedAt = java.time.LocalDateTime.parse(showtimeTime.replace(" ", "T")).minusHours(minusHours);
                java.sql.Timestamp ts = java.sql.Timestamp.valueOf(bookedAt);
                ps.setTimestamp(7, ts);

                ps.executeUpdate();

                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        bookingId = gk.getInt(1);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (bookingId > 0) {
                // Insert Booking seats
                for (int s = 0; s < seatsToBook; s++) {
                    int seatId = seatIds.get(seatIndex++);
                    executeSql(conn, "INSERT INTO dbo.BOOKING_SEATS (booking_id, seat_id, price) VALUES (" + bookingId + ", " + seatId + ", " + price + ")");
                }

                // Insert Payment
                String payMethod = (source.equals("WALKIN")) ? "CASH" : (rand.nextBoolean() ? "VNPAY" : "MOMO");
                String sqlPayment = "INSERT INTO dbo.PAYMENTS (booking_id, type, method, transaction_id, status, amount, paid_at, last_update) " +
                                    "VALUES (?, ?, ?, ?, 'SUCCESS', ?, ?, GETDATE)";
                try (PreparedStatement ps = conn.prepareStatement(sqlPayment)) {
                    ps.setInt(1, bookingId);
                    ps.setString(2, source.equals("WALKIN") ? "CASH" : "ONLINE");
                    ps.setString(3, payMethod);
                    ps.setString(4, "TXN" + System.currentTimeMillis() + rand.nextInt(100));
                    ps.setDouble(5, totalPrice);

                    java.time.LocalDateTime bookedAt = java.time.LocalDateTime.parse(showtimeTime.replace(" ", "T")).minusHours(1);
                    java.sql.Timestamp ts = java.sql.Timestamp.valueOf(bookedAt);
                    ps.setTimestamp(6, ts);

                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
