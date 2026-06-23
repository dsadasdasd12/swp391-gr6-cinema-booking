package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Ghi đè dữ liệu tiếng Việt chuẩn Unicode (NVARCHAR) — sửa lỗi font do seed SQL / encoding cũ.
 */
public final class VietnameseDataRepair {

    private VietnameseDataRepair() {}

    public static void repairAll(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            System.out.println("==> Đang đồng bộ dữ liệu tiếng Việt (NVARCHAR) trong database...");
            repairBranches(conn);
            repairCategories(conn);
            repairLanguages(conn);
            repairMovies(conn);
            repairUsers(conn);
            repairHalls(conn);
            repairStaffBranch(conn);
            repairCinema(conn);
            System.out.println("==> Sửa dữ liệu tiếng Việt hoàn tất.");
        } catch (SQLException e) {
            System.err.println("==> Lỗi sửa tiếng Việt: " + e.getMessage());
        }
    }

    private static void repairCinema(Connection conn) throws SQLException {
        update(conn, "UPDATE dbo.CINEMA SET name = ?, address = ? WHERE id = 1",
                "Hệ thống Rạp Việt", "Hà Nội, Việt Nam");
    }

    private static void repairBranches(Connection conn) throws SQLException {
        update(conn, "UPDATE dbo.BRANCHES SET name = ?, address = ? WHERE id = 1",
                "Rạp Việt Nguyễn Trãi", "Số 266 Đường Nguyễn Trãi, Thanh Xuân, Hà Nội");
        update(conn, "UPDATE dbo.BRANCHES SET name = ?, address = ? WHERE id = 2",
                "Rạp Việt Nguyễn Du", "Số 116 Đường Nguyễn Du, Quận 1, TP. Hồ Chí Minh");
        update(conn, "UPDATE dbo.BRANCHES SET name = ?, address = ? WHERE id = 3",
                "Rạp Việt Lê Lợi", "Số 88 Đường Lê Lợi, Hải Châu, Đà Nẵng");
    }

    private static void repairCategories(Connection conn) throws SQLException {
        String[][] rows = {
                {"1", "Hành Động", "Phim hành động, võ thuật kịch tính"},
                {"2", "Hài Hước", "Phim hài, dí dỏm, vui nhộn"},
                {"3", "Tình Cảm", "Phim tâm lý, tình cảm lãng mạn"},
                {"4", "Kinh Dị", "Phim kinh dị, giật gân rùng rợn"},
                {"5", "Hoạt Hình", "Phim hoạt hình 3D, phiêu lưu gia đình"}
        };
        for (String[] r : rows) {
            update(conn, "UPDATE dbo.CATEGORY SET name = ?, description = ? WHERE id = ?",
                    r[1], r[2], Integer.parseInt(r[0]));
        }
    }

    private static void repairLanguages(Connection conn) throws SQLException {
        update(conn, "UPDATE dbo.LANGUAGES SET name = ? WHERE id = 1", "Tiếng Việt (Lồng tiếng)");
        update(conn, "UPDATE dbo.LANGUAGES SET name = ? WHERE id = 2", "Tiếng Anh (Phụ đề Việt)");
        update(conn, "UPDATE dbo.LANGUAGES SET name = ? WHERE id = 3", "Tiếng Hàn (Phụ đề Việt)");
    }

    private static void repairMovies(Connection conn) throws SQLException {
        update(conn, "UPDATE dbo.MOVIES SET title = ?, description = ?, actor = ?, director = ? WHERE id = 1",
                "Lật Mặt 7: Một Điều Ước",
                "Bộ phim gia đình đầy cảm xúc của Lý Hải kể về câu chuyện của người mẹ tảo tần và các con.",
                "Thanh Hiền, Trương Minh Cường, Đinh Y Nhung", "Lý Hải");
        update(conn, "UPDATE dbo.MOVIES SET title = ?, description = ?, actor = ?, director = ? WHERE id = 2",
                "Mai - Trấn Thành",
                "Mai là câu chuyện tình cảm trắc trở của một người phụ nữ ngoài ba mươi.",
                "Phương Anh Đào, Tuấn Trần, Hồng Đào", "Trấn Thành");
        update(conn, "UPDATE dbo.MOVIES SET title = ?, description = ?, actor = ?, director = ? WHERE id = 3",
                "Dune: Hành Tinh Cát 2",
                "Phần tiếp theo của siêu phẩm sử thi viễn tưởng về Paul Atreides.",
                "Timothée Chalamet, Zendaya, Rebecca Ferguson", "Denis Villeneuve");
        update(conn, "UPDATE dbo.MOVIES SET title = ?, description = ?, actor = ?, director = ? WHERE id = 4",
                "Inside Out 2: Những Mảnh Ghép Cảm Xúc",
                "Những cảm xúc mới xuất hiện bên trong đầu Riley khi bước vào tuổi dậy thì.",
                "Amy Poehler, Phyllis Smith, Lewis Black", "Kelsey Mann");
    }

    private static void repairUsers(Connection conn) throws SQLException {
        update(conn, "UPDATE dbo.[USER] SET full_name = ? WHERE id = 1", "Quản trị viên Hệ thống");
        update(conn, "UPDATE dbo.[USER] SET full_name = ? WHERE id = 2", "Trưởng Chi Nhánh Hà Nội");
        update(conn, "UPDATE dbo.[USER] SET full_name = ? WHERE id = 3", "Nhân Viên Quầy HN");
        update(conn, "UPDATE dbo.[USER] SET full_name = ? WHERE id = 4", "Phạm Minh Hoàng");
        update(conn, "UPDATE dbo.[USER] SET full_name = ? WHERE id = 5", "Lê Thu Trang");
        update(conn, "UPDATE dbo.[USER] SET full_name = ? WHERE id = 6", "Nguyễn Tuấn Anh");
        update(conn, "UPDATE dbo.[USER] SET full_name = ? WHERE id = 7", "Trần Quốc Bảo");
    }

    private static void repairHalls(Connection conn) throws SQLException {
        update(conn, "UPDATE dbo.HALLS SET name = ? WHERE branch_id = 1 AND hall_type = 'VIP'",
                "Phòng Chiếu VIP 1");
        update(conn, "UPDATE dbo.HALLS SET name = ? WHERE branch_id = 1 AND hall_type = 'IMAX'",
                "Phòng Chiếu IMAX 2");
        update(conn, "UPDATE dbo.HALLS SET name = ? WHERE branch_id = 2 AND hall_type = 'VIP'",
                "Phòng Chiếu VIP 1");
    }

    private static void repairStaffBranch(Connection conn) throws SQLException {
        update(conn, "UPDATE dbo.STAFF_BRANCH SET position = ? WHERE user_id = 2", "TRƯỞNG CHI NHÁNH");
        update(conn, "UPDATE dbo.STAFF_BRANCH SET position = ? WHERE user_id = 3", "NHÂN VIÊN QUẦY");
    }

    private static void update(Connection conn, String sql, String... values) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                ps.setNString(i + 1, values[i]);
            }
            ps.executeUpdate();
        }
    }

    private static void update(Connection conn, String sql, String v1, String v2, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, v1);
            ps.setNString(2, v2);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }
}
