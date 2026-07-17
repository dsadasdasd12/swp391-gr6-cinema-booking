/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dto.MovieAssignmentItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.Movie;
import util.DBContext;

public class MovieManagementDAO {

    /**
     * Lấy toàn bộ phim để hiển thị trên màn hình phân bổ phim cho một chi
     * nhánh. Toàn bộ phim trong bảng MOVIES Đánh dấu phim nào đã nằm trong
     * BRANCH_MOVIES của Branch hiện tại. assigned = true nếu phim đã được gán
     * cho chi nhánh. ( LEFT JOIN )
     */
    public List<MovieAssignmentItem> findItemsForBranch(int branchId) {
        String sql = "SELECT m.id, m.title, m.duration_min, m.status, "
                + "CASE WHEN bm.movie_id IS NULL THEN CAST(0 AS BIT) "
                + "ELSE CAST(1 AS BIT) END AS assigned "
                + "FROM dbo.MOVIES m "
                + "LEFT JOIN dbo.BRANCH_MOVIES bm "
                + "ON bm.movie_id = m.id AND bm.branch_id = ? "
                + "ORDER BY m.title ASC";

        List<MovieAssignmentItem> items = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapAssignmentItem(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Lấy danh sách phim để phân bổ cho phòng chiếu.
     *
     * Chỉ hiển thị những phim đã được phân bổ cho chi nhánh chứa phòng chiếu
     * đó.
     */
    public List<MovieAssignmentItem> findItemsForHall(int hallId) {
        String sql = "SELECT m.id, m.title, m.duration_min, m.status, "
                + "CASE WHEN hm.movie_id IS NULL THEN CAST(0 AS BIT) "
                + "ELSE CAST(1 AS BIT) END AS assigned "
                + "FROM dbo.HALLS h "
                + "JOIN dbo.BRANCH_MOVIES bm "
                + "ON bm.branch_id = h.branch_id "
                + "JOIN dbo.MOVIES m "
                + "ON m.id = bm.movie_id "
                + "LEFT JOIN dbo.HALL_MOVIES hm "
                + "ON hm.hall_id = h.id "
                + "AND hm.movie_id = m.id "
                + "WHERE h.id = ? "
                + "ORDER BY m.title ASC";

        List<MovieAssignmentItem> items = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapAssignmentItem(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    /*
 * Lấy Movie có thể dùng để tạo Showtime cho Hall.
 * Chỉ lấy Movie:
    - Đã được phân bổ vào HALL_MOVIES.
    - Có trạng thái NOW_SHOWING hoặc COMING_SOON.
     */
    public List<Movie> findMoviesAssignedToHall(int hallId) {
        String sql = "SELECT m.id, m.title, m.duration_min, m.status "
                + "FROM dbo.HALL_MOVIES hm "
                + "JOIN dbo.MOVIES m ON m.id = hm.movie_id "
                + "WHERE hm.hall_id = ? "
                + "AND m.status IN ('NOW_SHOWING', 'COMING_SOON') "
                + "ORDER BY m.title ASC";

        List<Movie> movies = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Movie movie = new Movie();

                    movie.setId(rs.getInt("id"));
                    movie.setTitle(rs.getString("title"));
                    movie.setDurationMin(rs.getInt("duration_min"));
                    movie.setStatus(rs.getString("status"));

                    movies.add(movie);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return movies;
    }

    /**
     * Tìm branch_id của một phòng chiếu.
     */
    public int findBranchIdByHallId(int hallId) {
        String sql = "SELECT branch_id "
                + "FROM dbo.HALLS "
                + "WHERE id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("branch_id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Kiểm tra phim đã được gán cho chi nhánh hay chưa.
     */
    public boolean isMovieAssignedToBranch(int branchId, int movieId) {
        String sql = "SELECT COUNT(*) "
                + "FROM dbo.BRANCH_MOVIES "
                + "WHERE branch_id = ? "
                + "AND movie_id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setInt(2, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Kiểm tra phim đã được gán cho phòng chiếu hay chưa.
     */
    public boolean isMovieAssignedToHall(int hallId, int movieId) {
        String sql = "SELECT COUNT(*) "
                + "FROM dbo.HALL_MOVIES "
                + "WHERE hall_id = ? "
                + "AND movie_id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            ps.setInt(2, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Tìm phim bị bỏ khỏi chi nhánh nhưng vẫn còn suất chiếu chưa kết thúc tại
     * chi nhánh đó.
     */
    public String findBranchRemovalConflict(
            int branchId,
            List<Integer> selectedMovieIds) {

        String sql = "SELECT DISTINCT m.id, m.title "
                + "FROM dbo.BRANCH_MOVIES bm "
                + "JOIN dbo.MOVIES m ON m.id = bm.movie_id "
                + "JOIN dbo.HALLS h ON h.branch_id = bm.branch_id "
                + "JOIN dbo.SHOWTIMES s "
                + "ON s.hall_id = h.id AND s.movie_id = bm.movie_id "
                + "WHERE bm.branch_id = ? "
                + "AND s.status IN ('SCHEDULED', 'ON_SALE') "
                + "AND s.end_time > GETDATE() "
                + "ORDER BY m.title ASC";

        return findRemovalConflict(
                sql,
                branchId,
                selectedMovieIds,
                "chi nhánh"
        );
    }

    /**
     * Tìm phim bị bỏ khỏi phòng nhưng vẫn còn suất chiếu chưa kết thúc tại
     * chính phòng đó.
     */
    public String findHallRemovalConflict(
            int hallId,
            List<Integer> selectedMovieIds) {

        String sql = "SELECT DISTINCT m.id, m.title "
                + "FROM dbo.HALL_MOVIES hm "
                + "JOIN dbo.MOVIES m ON m.id = hm.movie_id "
                + "JOIN dbo.SHOWTIMES s "
                + "ON s.hall_id = hm.hall_id AND s.movie_id = hm.movie_id "
                + "WHERE hm.hall_id = ? "
                + "AND s.status IN ('SCHEDULED', 'ON_SALE') "
                + "AND s.end_time > GETDATE() "
                + "ORDER BY m.title ASC";

        return findRemovalConflict(
                sql,
                hallId,
                selectedMovieIds,
                "phòng chiếu"
        );
    }

    private String findRemovalConflict(
            String sql,
            int ownerId,
            List<Integer> selectedMovieIds,
            String ownerName) {

        Set<Integer> selectedIds = sanitizeIds(selectedMovieIds);
        Connection conn = DBContext.getInstance().getConnection();

        if (conn == null) {
            throw new IllegalStateException(
                    "Không thể kết nối cơ sở dữ liệu."
            );
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!selectedIds.contains(rs.getInt("id"))) {
                        return rs.getString("title");
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không thể kiểm tra lịch chiếu của " + ownerName + ".",
                    e
            );
        }

        return null;
    }

    /**
     * Lưu toàn bộ danh sách phim được chọn cho chi nhánh.
     *
     * Phim bị bỏ chọn sẽ bị xóa khỏi BRANCH_MOVIES. Đồng thời phim đó cũng bị
     * xóa khỏi các phòng thuộc chi nhánh.
     */
    public boolean saveBranchAssignments(
            int branchId,
            List<Integer> selectedMovieIds) {

        Connection conn = DBContext.getInstance().getConnection();
        boolean oldAutoCommit = true;

        try {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            Set<Integer> selectedIds = sanitizeIds(selectedMovieIds);
            Set<Integer> currentIds
                    = findCurrentBranchMovieIds(conn, branchId);

            /*
             * Những phim hiện có nhưng không còn được chọn.
             */
            Set<Integer> removedIds = new LinkedHashSet<>(currentIds);
            removedIds.removeAll(selectedIds);

            /*
             * Những phim mới được chọn.
             */
            Set<Integer> addedIds = new LinkedHashSet<>(selectedIds);
            addedIds.removeAll(currentIds);

            String deleteHallSql
                    = "DELETE FROM dbo.HALL_MOVIES "
                    + "WHERE movie_id = ? "
                    + "AND hall_id IN ("
                    + "SELECT id FROM dbo.HALLS WHERE branch_id = ?"
                    + ")";

            String deleteBranchSql
                    = "DELETE FROM dbo.BRANCH_MOVIES "
                    + "WHERE branch_id = ? "
                    + "AND movie_id = ?";

            String insertBranchSql
                    = "INSERT INTO dbo.BRANCH_MOVIES "
                    + "(branch_id, movie_id) "
                    + "VALUES (?, ?)";

            try (PreparedStatement deleteHallPs
                    = conn.prepareStatement(deleteHallSql); PreparedStatement deleteBranchPs
                    = conn.prepareStatement(deleteBranchSql); PreparedStatement insertBranchPs
                    = conn.prepareStatement(insertBranchSql)) {

                /*
                 * Xóa phim khỏi các phòng trước,
                 * sau đó mới xóa khỏi chi nhánh.
                 */
                for (int movieId : removedIds) {
                    deleteHallPs.setInt(1, movieId);
                    deleteHallPs.setInt(2, branchId);
                    deleteHallPs.addBatch();

                    deleteBranchPs.setInt(1, branchId);
                    deleteBranchPs.setInt(2, movieId);
                    deleteBranchPs.addBatch();
                }

                if (!removedIds.isEmpty()) {
                    deleteHallPs.executeBatch();
                    deleteBranchPs.executeBatch();
                }

                /*
                 * Thêm các phim mới vào chi nhánh.
                 */
                for (int movieId : addedIds) {
                    insertBranchPs.setInt(1, branchId);
                    insertBranchPs.setInt(2, movieId);
                    insertBranchPs.addBatch();
                }

                if (!addedIds.isEmpty()) {
                    insertBranchPs.executeBatch();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }

            e.printStackTrace();
            return false;

        } finally {
            try {
                conn.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Lưu toàn bộ phim được chọn cho một phòng chiếu.
     *
     * Chỉ chấp nhận phim đã được gán cho chi nhánh chứa phòng đó.
     */
    public boolean saveHallAssignments(
            int hallId,
            List<Integer> selectedMovieIds) {

        Connection conn = DBContext.getInstance().getConnection();
        boolean oldAutoCommit = true;

        try {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            int branchId = findBranchIdByHallId(conn, hallId);

            if (branchId <= 0) {
                conn.rollback();
                return false;
            }

            Set<Integer> selectedIds = sanitizeIds(selectedMovieIds);

            /*
             * Kiểm tra từng phim đã được gán cho chi nhánh chưa.
             */
            for (int movieId : selectedIds) {
                if (!isMovieAssignedToBranch(
                        conn, branchId, movieId)) {

                    conn.rollback();
                    return false;
                }
            }

            /*
             * Xóa danh sách cũ của phòng.
             */
            String deleteSql
                    = "DELETE FROM dbo.HALL_MOVIES "
                    + "WHERE hall_id = ?";

            try (PreparedStatement deletePs
                    = conn.prepareStatement(deleteSql)) {

                deletePs.setInt(1, hallId);
                deletePs.executeUpdate();
            }

            /*
             * Thêm lại danh sách phim mới được chọn.
             */
            if (!selectedIds.isEmpty()) {
                String insertSql
                        = "INSERT INTO dbo.HALL_MOVIES "
                        + "(hall_id, movie_id) "
                        + "VALUES (?, ?)";

                try (PreparedStatement insertPs
                        = conn.prepareStatement(insertSql)) {

                    for (int movieId : selectedIds) {
                        insertPs.setInt(1, hallId);
                        insertPs.setInt(2, movieId);
                        insertPs.addBatch();
                    }

                    insertPs.executeBatch();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }

            e.printStackTrace();
            return false;

        } finally {
            try {
                conn.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Lấy các movie_id hiện đang được gán cho chi nhánh.
     */
    private Set<Integer> findCurrentBranchMovieIds(
            Connection conn,
            int branchId) throws SQLException {

        String sql = "SELECT movie_id "
                + "FROM dbo.BRANCH_MOVIES "
                + "WHERE branch_id = ?";

        Set<Integer> ids = new LinkedHashSet<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("movie_id"));
                }
            }
        }

        return ids;
    }

    /**
     * Phiên bản dùng trong transaction.
     */
    private int findBranchIdByHallId(
            Connection conn,
            int hallId) throws SQLException {

        String sql = "SELECT branch_id "
                + "FROM dbo.HALLS "
                + "WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("branch_id");
                }
            }
        }

        return 0;
    }

    /**
     * Phiên bản dùng trong transaction.
     */
    private boolean isMovieAssignedToBranch(
            Connection conn,
            int branchId,
            int movieId) throws SQLException {

        String sql = "SELECT COUNT(*) "
                + "FROM dbo.BRANCH_MOVIES "
                + "WHERE branch_id = ? "
                + "AND movie_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setInt(2, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Loại bỏ ID null, ID <= 0 và ID bị trùng.
     */
    private Set<Integer> sanitizeIds(List<Integer> ids) {
        Set<Integer> result = new LinkedHashSet<>();

        if (ids == null) {
            return result;
        }

        for (Integer id : ids) {
            if (id != null && id > 0) {
                result.add(id);
            }
        }

        return result;
    }

    /**
     * Chuyển một dòng trong ResultSet thành DTO.
     */
    private MovieAssignmentItem mapAssignmentItem(
            ResultSet rs) throws SQLException {

        return new MovieAssignmentItem(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getInt("duration_min"),
                rs.getString("status"),
                rs.getBoolean("assigned")
        );
    }

    /**
     * Lấy toàn bộ phim để hiển thị trên màn hình quản lý thời lượng.
     */
    public List<Movie> findAllForDurationManagement() {
        String sql = "SELECT id, title, duration_min, status, "
                + "poster_url, last_update "
                + "FROM dbo.MOVIES "
                + "ORDER BY title ASC";

        List<Movie> movies = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();

        if (conn == null) {
            return movies;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movies.add(mapMovieForDurationManagement(rs));
            }

        } catch (SQLException e) {
            System.getLogger(MovieManagementDAO.class.getName()).log(
                    System.Logger.Level.ERROR,
                    "Không thể lấy danh sách thời lượng phim.",
                    e
            );
        }

        return movies;
    }

    /**
     * Tìm một phim theo ID để kiểm tra trước khi cập nhật thời lượng.
     */
    public Movie findMovieById(int movieId) {
        String sql = "SELECT id, title, duration_min, status, "
                + "poster_url, last_update "
                + "FROM dbo.MOVIES "
                + "WHERE id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        if (conn == null) {
            return null;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMovieForDurationManagement(rs);
                }
            }

        } catch (SQLException e) {
            System.getLogger(MovieManagementDAO.class.getName()).log(
                    System.Logger.Level.ERROR,
                    "Không thể tìm phim theo ID.",
                    e
            );
        }

        return null;
    }

    /**
     * Kiểm tra phim còn suất chiếu chưa kết thúc hay không.
     *
     * Suất đã hủy không ảnh hưởng đến việc sửa thời lượng. Chỉ cần một suất
     * chiếu còn đang diễn ra hoặc nằm trong tương lai thì không nên đổi
     * MOVIES.duration_min, vì SHOWTIMES.end_time đã được lưu cố định.
     */
    public boolean hasUnfinishedShowtimes(int movieId) {
        String sql = "SELECT TOP 1 1 "
                + "FROM dbo.SHOWTIMES "
                + "WHERE movie_id = ? "
                + "AND status <> 'CANCELLED' "
                + "AND end_time > GETDATE()";

        Connection conn = DBContext.getInstance().getConnection();

        if (conn == null) {
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.getLogger(MovieManagementDAO.class.getName()).log(
                    System.Logger.Level.ERROR,
                    "Không thể kiểm tra suất chiếu của phim.",
                    e
            );
        }

        return false;
    }

    /**
     * Cập nhật MOVIES.duration_min và last_update.
     */
    public boolean updateDuration(int movieId, int durationMin) {
        String sql = "UPDATE dbo.MOVIES "
                + "SET duration_min = ?, "
                + "last_update = GETDATE() "
                + "WHERE id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        if (conn == null) {
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, durationMin);
            ps.setInt(2, movieId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.getLogger(MovieManagementDAO.class.getName()).log(
                    System.Logger.Level.ERROR,
                    "Không thể cập nhật thời lượng phim.",
                    e
            );
        }

        return false;
    }

    /**
     * Chuyển dòng ResultSet thành Movie cho màn hình quản lý thời lượng.
     */
    private Movie mapMovieForDurationManagement(ResultSet rs)
            throws SQLException {

        Movie movie = new Movie();
        movie.setId(rs.getInt("id"));
        movie.setTitle(rs.getString("title"));
        movie.setDurationMin(rs.getInt("duration_min"));
        movie.setStatus(rs.getString("status"));
        movie.setPosterUrl(rs.getString("poster_url"));
        movie.setLastUpdate(rs.getObject("last_update", LocalDateTime.class));

        return movie;
    }
}
