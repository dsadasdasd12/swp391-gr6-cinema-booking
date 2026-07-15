/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim + Quản lý phim Admin (Long)
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import dto.MovieFilter;
import dto.PageResult;
import java.sql.Statement;
import model.Category;
import model.Language;
import model.Movie;
import util.DBContext;
import util.EncodingUtil;

/**
 * DAO cho danh mục phim (bảng dbo.MOVIES).
 * <p>
 * Hỗ trợ các thao tác duyệt phim của module này:
 * <ul>
 *   <li>duyệt / tìm kiếm / lọc kèm phân trang ({@link #search})</li>
 *   <li>xem chi tiết một phim ({@link #findById})</li>
 * </ul>
 * Mọi giá trị do người dùng nhập đều được truyền qua tham số của
 * {@link PreparedStatement}; chuỗi duy nhất được ghép thẳng vào SQL là mệnh đề
 * ORDER BY và đã nằm trong danh sách trắng (whitelist).
 *
 * @author LONG
 */
public class MovieDAO {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final LanguageDAO languageDAO = new LanguageDAO();
    private Boolean hasEndDateColumn;

    /** Các truy vấn con tính điểm/đếm đánh giá cho từng phim. */
    private static final String RATING_SUBQUERY =
            "(SELECT AVG(CAST(r.rating AS FLOAT)) FROM dbo.REVIEWS r "
            + " WHERE r.movie_id = m.id AND r.status = 'ACTIVE') AS avg_rating, "
            + "(SELECT COUNT(*) FROM dbo.REVIEWS r "
            + " WHERE r.movie_id = m.id AND r.status = 'ACTIVE') AS review_count ";

    /**
     * Duyệt / tìm kiếm / lọc phim. Trả về một trang kết quả kèm tổng số phim
     * khớp để caller vẽ thanh phân trang. Mỗi phim trả về đã được gắn sẵn danh
     * sách thể loại để hiển thị trên thẻ phim.
     */
    public PageResult<Movie> search(MovieFilter f) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(f, params);

        String sql = "SELECT m.id, m.title, m.duration_min, m.description, m.release_date, "
                + endDateSelect() + ", "
                + "       m.status, m.poster_url, m.trailer_url, m.actor, m.director, m.last_update, "
                + RATING_SUBQUERY
                + "FROM dbo.MOVIES m "
                + where
                + " ORDER BY " + orderBy(f.getSortBy())
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        int offset = (f.getPage() - 1) * f.getPageSize();
        List<Movie> movies = new ArrayList<>();
        long total = count(f);

        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Object p : params) {
                ps.setObject(i++, p);
            }
            ps.setInt(i++, offset);
            ps.setInt(i, f.getPageSize());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movies.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "search thất bại", e);
        }

        attachCategories(movies);
        return new PageResult<>(movies, total, f.getPage(), f.getPageSize());
    }

    /** Tổng số phim khớp với bộ lọc (bỏ qua phân trang). */
    public long count(MovieFilter f) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(f, params);
        String sql = "SELECT COUNT(*) FROM dbo.MOVIES m " + where;
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Object p : params) {
                ps.setObject(i++, p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "count thất bại", e);
        }
        return 0;
    }

    /**
     * Nạp một phim kèm đầy đủ dữ liệu chi tiết: thể loại, ngôn ngữ và thống kê
     * đánh giá. Trả về {@code null} nếu không có phim nào ứng với id.
     */
    public Movie findById(int id) {
        String sql = "SELECT m.id, m.title, m.duration_min, m.description, m.release_date, "
                + endDateSelect() + ", "
                + "       m.status, m.poster_url, m.trailer_url, m.actor, m.director, m.last_update, "
                + RATING_SUBQUERY
                + "FROM dbo.MOVIES m WHERE m.id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Movie m = mapRow(rs);
                    m.setCategories(categoryDAO.findByMovieId(id));
                    m.setLanguages(languageDAO.findByMovieId(id));
                    return m;
                }
            }
        } catch (SQLException e) {
            System.err.println(
                    "[MovieDAO.findById] Lỗi truy vấn phim ID "
                            + id
                            + ": "
                            + e.getMessage()
            );
            
            e.printStackTrace();
            
            throw new RuntimeException(
                    "Không thể truy vấn dữ liệu phim ID " + id,
                    e
            );
        }
        
        return null;
    }

    public List<Movie> findBookableByBranch(int branchId) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT DISTINCT m.id, m.title, m.duration_min, m.description, m.release_date, "
                + endDateSelect() + ", "
                + "       m.status, m.poster_url, m.trailer_url, m.actor, m.director, m.last_update, "
                + RATING_SUBQUERY
                + "FROM dbo.MOVIES m "
                + "JOIN dbo.SHOWTIMES s ON s.movie_id = m.id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "WHERE h.branch_id = ? "
                + "AND s.status IN ('SCHEDULED','ON_SALE') "
                + "AND s.start_time > GETDATE() "
                + "ORDER BY m.title";

        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movies.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findBookableByBranch that bai", e);
        }

        attachCategories(movies);
        return movies;
    }

    // ── Các hàm hỗ trợ ──────────────────────────────────────

    /** Dựng mệnh đề WHERE và thêm các giá trị tương ứng vào {@code params}. */
    private String buildWhere(MovieFilter f, List<Object> params) {
        StringBuilder w = new StringBuilder(" WHERE 1 = 1 ");

        if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
            String kw = "%" + f.getKeyword().trim() + "%";
            w.append(" AND (m.title LIKE ? OR m.actor LIKE ? OR m.director LIKE ?) ");
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (f.getStatus() != null && !f.getStatus().isBlank()) {
            w.append(" AND m.status = ? ");
            params.add(f.getStatus());
        }
        if (f.getCategoryId() != null) {
            w.append(" AND EXISTS (SELECT 1 FROM dbo.MOVIES_CATEGORY mc "
                    + " WHERE mc.movie_id = m.id AND mc.category_id = ?) ");
            params.add(f.getCategoryId());
        }
        if (f.getLanguageId() != null) {
            w.append(" AND EXISTS (SELECT 1 FROM dbo.MOVIE_LANGUAGES ml "
                    + " WHERE ml.movie_id = m.id AND ml.language_id = ?) ");
            params.add(f.getLanguageId());
        }
        if (f.getFormat() != null && !f.getFormat().isBlank()) {
            // "Định dạng" tương ứng với loại phòng chiếu (hall_type) chiếu phim đó.
            w.append(" AND EXISTS (SELECT 1 FROM dbo.SHOWTIMES s "
                    + " JOIN dbo.HALLS h ON h.id = s.hall_id "
                    + " WHERE s.movie_id = m.id AND h.hall_type = ?) ");
            params.add(f.getFormat());
        }
        return w.toString();
    }

    /** Mệnh đề ORDER BY theo danh sách trắng — không bao giờ ghép từ input thô. */
    private String orderBy(String sortBy) {
        if (sortBy == null) {
            sortBy = "";
        }
        switch (sortBy) {
            case "title_asc":
                return "m.title ASC, m.id DESC";
            case "title_desc":
                return "m.title DESC, m.id DESC";
            case "rating_desc":
                return "avg_rating DESC, m.id DESC";
            case "newest":
            default:
                return "m.release_date DESC, m.id DESC";
        }
    }

    /** Ánh xạ một dòng ResultSet sang đối tượng Movie. */
    private String endDateSelect() {
        return hasEndDateColumn()
                ? "m.end_date"
                : "CAST(NULL AS DATE) AS end_date";
    }

    private boolean hasEndDateColumn() {
        if (hasEndDateColumn != null) {
            return hasEndDateColumn;
        }

        String sql = "SELECT CASE WHEN COL_LENGTH('dbo.MOVIES', 'end_date') IS NULL THEN 0 ELSE 1 END";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            hasEndDateColumn = rs.next() && rs.getInt(1) == 1;
        } catch (SQLException e) {
            hasEndDateColumn = false;
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.WARNING, "Cannot inspect MOVIES.end_date column", e);
        }
        return hasEndDateColumn;
    }

    private Movie mapRow(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setId(rs.getInt("id"));
        m.setTitle(EncodingUtil.getString(rs, "title"));
        m.setDurationMin(rs.getInt("duration_min"));
        m.setDescription(EncodingUtil.getString(rs, "description"));
        m.setReleaseDate(rs.getObject("release_date", LocalDate.class));
        m.setEndDate(rs.getObject("end_date", LocalDate.class));
        m.setStatus(rs.getString("status"));
        m.setPosterUrl(Movie.normalizePosterPath(rs.getString("poster_url")));
        m.setTrailerUrl(rs.getString("trailer_url"));
        m.setActor(EncodingUtil.getString(rs, "actor"));
        m.setDirector(EncodingUtil.getString(rs, "director"));
        m.setLastUpdate(rs.getObject("last_update", LocalDateTime.class));
        m.setAvgRating(rs.getDouble("avg_rating"));   // = 0.0 khi NULL
        m.setReviewCount(rs.getInt("review_count"));
        return m;
    }

    /** Nạp thể loại cho cả trang phim trong một truy vấn (tránh lỗi N+1). */
    private void attachCategories(List<Movie> movies) {
        if (movies.isEmpty()) {
            return;
        }
        StringBuilder in = new StringBuilder();
        for (int k = 0; k < movies.size(); k++) {
            in.append(k == 0 ? "?" : ",?");
        }
        String sql = "SELECT mc.movie_id, c.id, c.name AS name, c.status "
        + "FROM dbo.MOVIES_CATEGORY mc "
        + "JOIN dbo.CATEGORY c ON c.id = mc.category_id "
        + "WHERE mc.movie_id IN (" + in + ") ORDER BY c.name";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int k = 0; k < movies.size(); k++) {
                ps.setInt(k + 1, movies.get(k).getId());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int movieId = rs.getInt("movie_id");
                    Category c = new Category();
                    c.setId(rs.getInt("id"));
                    c.setName(EncodingUtil.getString(rs, "name"));
                    c.setStatus(rs.getString("status"));
                    for (Movie m : movies) {
                        if (m.getId() == movieId) {
                            m.getCategories().add(c);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "attachCategories thất bại", e);
        }
    }
    
     // ════════════════════════════════════════════════════════
    // ADMIN CRUD — Long
    // ════════════════════════════════════════════════════════

    /**
     * Trả về toàn bộ phim (không phân trang) cho trang quản lý admin.
     * Sắp theo ngày cập nhật mới nhất trước.
     */
    public List<Movie> findAll() {
        return findAll(null, null);
    }

    /**
     * Trả về danh sách phim cho admin, có hỗ trợ lọc theo từ khóa và trạng thái.
     * @param keyword  tìm trong title, director, actor (null = không lọc)
     * @param status   COMING_SOON / NOW_SHOWING / ENDED (null = không lọc)
     */
    public List<Movie> findAll(String keyword, String status) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT m.id, m.title, m.duration_min, m.description, m.release_date, "
                + endDateSelect() + ", "
                + "m.status, m.poster_url, m.trailer_url, m.actor, m.director, m.last_update, "
                + "0.0 AS avg_rating, 0 AS review_count "
                + "FROM dbo.MOVIES m WHERE 1=1 ");

        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim() + "%";
            sql.append("AND (m.title LIKE ? OR m.director LIKE ?) ");
            params.add(kw); params.add(kw);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND m.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY m.last_update DESC");

        List<Movie> movies = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movies.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findAll admin thất bại", e);
        }
        attachCategories(movies);
        return movies;
    }

    /**
     * Kiểm tra xem phim có suất chiếu đang hoạt động không
     * (status = SCHEDULED hoặc ON_SALE, thời gian trong tương lai).
     * Dùng để chặn xóa phim.
     */
    public boolean hasActiveShowtimes(int movieId) {
        String sql = "SELECT TOP 1 1 FROM dbo.SHOWTIMES "
                + "WHERE movie_id = ? AND status IN ('SCHEDULED','ON_SALE') "
                + "AND start_time >= ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setObject(2, LocalDateTime.now());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();   // true = có suất chiếu đang hoạt động
            }
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "hasActiveShowtimes thất bại", e);
        }
        return false;
    }

    /**
     * Kiểm tra tiêu đề phim đã tồn tại chưa (case insensitive, skip excludeMovieId).
     */
    public boolean isTitleExists(String title, int excludeMovieId) {
        String sql = "SELECT TOP 1 1 FROM dbo.MOVIES WHERE LOWER(title) = LOWER(?) AND id != ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setInt(2, excludeMovieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "isTitleExists thất bại", e);
        }
        return false;
    }

    /**
     * Lấy ngày chiếu sớm nhất của một phim, trả về null nếu chưa có suất chiếu.
     */
    public LocalDate getEarliestShowtimeDate(int movieId) {
        String sql = "SELECT MIN(start_time) FROM dbo.SHOWTIMES WHERE movie_id = ? AND status != 'CANCELLED'";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime earliest = rs.getObject(1, LocalDateTime.class);
                    return earliest != null ? earliest.toLocalDate() : null;
                }
            }
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "getEarliestShowtimeDate thất bại", e);
        }
        return null;
    }

    /**
     * Thêm mới một phim + ghi vào bảng MOVIES_CATEGORY và MOVIE_LANGUAGES.
     * @return id tự sinh của bản ghi mới, hoặc -1 nếu thất bại
     */
    public int insert(Movie m, List<Integer> categoryIds, List<Integer> languageIds) {
        String sql = "INSERT INTO dbo.MOVIES "
                + "(title, duration_min, description, release_date, end_date, status, poster_url, trailer_url, actor, director) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getTitle());
            ps.setInt   (2, m.getDurationMin());
            ps.setString(3, m.getDescription());
            ps.setObject(4, m.getReleaseDate());          // LocalDate
            ps.setObject(5, m.getEndDate());              // LocalDate
            ps.setString(6, m.getStatus() != null ? m.getStatus() : "COMING_SOON");
            ps.setString(7, m.getPosterUrl());
            ps.setString(8, m.getTrailerUrl());
            ps.setString(9, m.getActor());
            ps.setString(10, m.getDirector());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    syncCategories(conn, newId, categoryIds);
                    syncLanguages (conn, newId, languageIds);
                    return newId;
                }
            }
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "insert movie thất bại", e);
        }
        return -1;
    }

    /**
     * Cập nhật thông tin phim. Xóa và ghi lại bảng junction cho thể loại / ngôn ngữ.
     * @return true nếu thành công
     */
    public boolean update(Movie m, List<Integer> categoryIds, List<Integer> languageIds) {
        String sql = "UPDATE dbo.MOVIES SET "
                + "title=?, duration_min=?, description=?, release_date=?, end_date=?, status=?, "
                + "poster_url=?, trailer_url=?, actor=?, director=?, last_update=GETDATE() "
                + "WHERE id=?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getTitle());
            ps.setInt   (2, m.getDurationMin());
            ps.setString(3, m.getDescription());
            ps.setObject(4, m.getReleaseDate());
            ps.setObject(5, m.getEndDate());
            ps.setString(6, m.getStatus());
            ps.setString(7, m.getPosterUrl());
            ps.setString(8, m.getTrailerUrl());
            ps.setString(9, m.getActor());
            ps.setString(10, m.getDirector());
            ps.setInt   (11, m.getId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                syncCategories(conn, m.getId(), categoryIds);
                syncLanguages (conn, m.getId(), languageIds);
                return true;
            }
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "update movie thất bại", e);
        }
        return false;
    }

    /**
     * Xóa phim. Caller phải đã gọi hasActiveShowtimes trước.
     * Bảng junction xóa cascade qua FK — đảm bảo DB đã có ON DELETE CASCADE
     * hoặc xóa thủ công trước (đã làm bên dưới để an toàn).
     */
    public boolean delete(int id) {
        Connection conn = DBContext.getInstance().getConnection();
        try {
            // Xóa bảng junction trước để tránh vi phạm FK
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM dbo.MOVIES_CATEGORY WHERE movie_id=?")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM dbo.MOVIE_LANGUAGES WHERE movie_id=?")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
            // Xóa phim chính
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM dbo.MOVIES WHERE id=?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "delete movie thất bại", e);
        }
        return false;
    }

    /**
     * Cập nhật chỉ trường status.
     */
    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE dbo.MOVIES SET status=?, last_update=GETDATE() WHERE id=?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "updateStatus thất bại", e);
        }
        return false;
    }

    /**
     * Đồng bộ trạng thái phim theo ngày hiện tại.
     * Rule: chưa tới release_date => COMING_SOON; quá end_date => ENDED;
     * còn lại => NOW_SHOWING. end_date NULL nghĩa là chưa đặt ngày kết thúc.
     */
    public int refreshStatusesByDate(LocalDate today) {
        if (today == null) {
            today = LocalDate.now();
        }

        String computedStatus = hasEndDateColumn()
                ? "CASE "
                + "WHEN release_date > ? THEN 'COMING_SOON' "
                + "WHEN end_date IS NOT NULL AND end_date < ? THEN 'ENDED' "
                + "ELSE 'NOW_SHOWING' "
                + "END"
                : "CASE "
                + "WHEN release_date > ? THEN 'COMING_SOON' "
                + "ELSE 'NOW_SHOWING' "
                + "END";

        String sql = "UPDATE dbo.MOVIES "
                + "SET status = " + computedStatus + ", last_update = GETDATE() "
                + "WHERE status <> " + computedStatus;

        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, today);
            if (hasEndDateColumn()) {
                ps.setObject(2, today);
                ps.setObject(3, today);
                ps.setObject(4, today);
            } else {
                ps.setObject(2, today);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "refreshStatusesByDate thất bại", e);
        }
        return 0;
    }

    /** Cập nhật đường dẫn poster sau khi upload. */
    public boolean updatePoster(int id, String posterUrl) {
        String sql = "UPDATE dbo.MOVIES SET poster_url=?, last_update=GETDATE() WHERE id=?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, posterUrl);
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "updatePoster thất bại", e);
        }
        return false;
    }

    /** Cập nhật đường dẫn trailer sau khi upload. */
    public boolean updateTrailer(int id, String trailerUrl) {
        String sql = "UPDATE dbo.MOVIES SET trailer_url=?, last_update=GETDATE() WHERE id=?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trailerUrl);
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "updateTrailer thất bại", e);
        }
        return false;
    }

    // ── Junction-table helpers ───────────────────────────────

    /**
     * Xóa sạch và ghi lại liên kết thể loại cho một phim.
     * Gọi trong cùng Connection để đồng nhất transaction ngầm.
     */
    private void syncCategories(Connection conn, int movieId, List<Integer> categoryIds)
            throws SQLException {
        try (PreparedStatement del = conn.prepareStatement(
                "DELETE FROM dbo.MOVIES_CATEGORY WHERE movie_id=?")) {
            del.setInt(1, movieId);
            del.executeUpdate();
        }
        if (categoryIds == null || categoryIds.isEmpty()) return;
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO dbo.MOVIES_CATEGORY (movie_id, category_id) VALUES (?, ?)")) {
            for (int catId : categoryIds) {
                ins.setInt(1, movieId);
                ins.setInt(2, catId);
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    /**
     * Xóa sạch và ghi lại liên kết ngôn ngữ cho một phim.
     */
    private void syncLanguages(Connection conn, int movieId, List<Integer> languageIds)
            throws SQLException {
        try (PreparedStatement del = conn.prepareStatement(
                "DELETE FROM dbo.MOVIE_LANGUAGES WHERE movie_id=?")) {
            del.setInt(1, movieId);
            del.executeUpdate();
        }
        if (languageIds == null || languageIds.isEmpty()) return;
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO dbo.MOVIE_LANGUAGES (movie_id, language_id) VALUES (?, ?)")) {
            for (int langId : languageIds) {
                ins.setInt(1, movieId);
                ins.setInt(2, langId);
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

}
