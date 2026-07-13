/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Đánh giá phim - truy xuất bảng dbo.REVIEWS
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import dto.ReviewView;
import model.Review;
import util.DBContext;

/**
 * DAO cho đánh giá phim. Hỗ trợ liệt kê đánh giá của một phim (kèm tên người
 * đánh giá), kiểm tra điều kiện được đánh giá, và thêm/sửa/xóa đánh giá của
 * chính khách. Mỗi đơn đặt vé chỉ đánh giá một lần (booking_id UNIQUE).
 *
 * Lưu ý: DBContext của nhánh này dùng chung một Connection (singleton) nên chỉ
 * đóng PreparedStatement/ResultSet, KHÔNG đóng Connection.
 *
 * @author Group6 - Huy (Module Đánh giá)
 */
public class ReviewDAO {

    /** Các đánh giá ACTIVE của một phim (kèm tên người đánh giá), mới nhất lên đầu. */
    public List<ReviewView> findActiveByMovieId(int movieId) {
        String sql = "SELECT r.id, r.user_id, r.movie_id, r.booking_id, r.rating, r.comment, "
                + "r.status, r.created_at, r.last_update, u.full_name "
                + "FROM dbo.REVIEWS r JOIN dbo.[USER] u ON u.id = r.user_id "
                + "WHERE r.movie_id = ? AND r.status = 'ACTIVE' "
                + "ORDER BY r.created_at DESC";
        List<ReviewView> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ReviewView(mapReview(rs), rs.getString("full_name")));
                }
            }
        } catch (SQLException e) {
            System.getLogger(ReviewDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findActiveByMovieId thất bại", e);
        }
        return list;
    }

    /** Đánh giá gắn với một đơn đặt vé (hoặc null) — để biết khách đã đánh giá chưa. */
    public Review findByBookingId(int bookingId) {
        return findOne("SELECT * FROM dbo.REVIEWS WHERE booking_id = ?", bookingId);
    }

    /** Đánh giá theo id nhưng phải là của chính khách (kiểm tra quyền sở hữu). */
    public Review findByIdAndUser(int reviewId, int userId) {
        String sql = "SELECT * FROM dbo.REVIEWS WHERE id = ? AND user_id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReview(rs);
                }
            }
        } catch (SQLException e) {
            System.getLogger(ReviewDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByIdAndUser thất bại", e);
        }
        return null;
    }

    /**
     * Khách có được đánh giá phim qua đơn này không: đơn phải thuộc về khách,
     * đúng phim, và đã thực sự xem (CHECKED_IN/USED).
     */
    public boolean canReview(int userId, int movieId, int bookingId) {
        String sql = "SELECT COUNT(*) FROM dbo.BOOKINGS bk "
                + "JOIN dbo.SHOWTIMES s ON s.id = bk.showtime_id "
                + "WHERE bk.id = ? AND bk.user_id = ? AND s.movie_id = ? "
                + "AND bk.status IN ('CHECKED_IN','USED')";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, userId);
            ps.setInt(3, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.getLogger(ReviewDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "canReview thất bại", e);
        }
        return false;
    }

    /** Thêm một đánh giá mới (status mặc định ACTIVE theo DB). */
    public boolean insert(Review r) {
        String sql = "INSERT INTO dbo.REVIEWS (user_id, movie_id, booking_id, rating, comment) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getUserId());
            ps.setInt(2, r.getMovieId());
            ps.setInt(3, r.getBookingId());
            ps.setInt(4, r.getRating());
            ps.setString(5, r.getComment());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(ReviewDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "insert thất bại", e);
        }
        return false;
    }

    /** Sửa đánh giá của chính khách (chỉ điểm + nội dung). */
    public boolean update(int reviewId, int userId, int rating, String comment) {
        String sql = "UPDATE dbo.REVIEWS SET rating = ?, comment = ?, last_update = GETDATE() "
                + "WHERE id = ? AND user_id = ? "
                + "AND created_at >= DATEADD(MINUTE, -30, GETDATE())";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setString(2, comment);
            ps.setInt(3, reviewId);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(ReviewDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "update thất bại", e);
        }
        return false;
    }

    /** Chi co the sua trong 30 phut ke tu luc review duoc tao. */
    public boolean canEdit(int reviewId, int userId) {
        String sql = "SELECT COUNT(*) FROM dbo.REVIEWS WHERE id = ? AND user_id = ? "
                + "AND created_at >= DATEADD(MINUTE, -30, GETDATE())";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.getLogger(ReviewDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "canEdit review failed", e);
        }
        return false;
    }

    /** Xóa đánh giá của chính khách. */
    public boolean delete(int reviewId, int userId) {
        String sql = "DELETE FROM dbo.REVIEWS WHERE id = ? AND user_id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(ReviewDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "delete thất bại", e);
        }
        return false;
    }

    /** Truy vấn một Review theo 1 tham số int (dùng chung). */
    private Review findOne(String sql, int param) {
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReview(rs);
                }
            }
        } catch (SQLException e) {
            System.getLogger(ReviewDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findOne thất bại", e);
        }
        return null;
    }

    /** Ánh xạ một dòng ResultSet sang đối tượng Review. */
    private Review mapReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setMovieId(rs.getInt("movie_id"));
        r.setBookingId(rs.getInt("booking_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        r.setLastUpdate(rs.getObject("last_update", LocalDateTime.class));
        return r;
    }
}
