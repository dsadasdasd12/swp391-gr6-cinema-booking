/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim (Browse / Search / Filter / Xem chi tiết) - UC06
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
import model.Category;
import model.Movie;
import util.DBContext;

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
 * @author Group6 - DuyThai (Module Duyệt phim)
 */
public class MovieDAO {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final LanguageDAO languageDAO = new LanguageDAO();

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
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findById thất bại", e);
        }
        return null;
    }

    // ── Các hàm hỗ trợ ──────────────────────────────────────
    
    
    /**
 * Lấy danh sách phim dùng cho form tạo / sửa suất chiếu.
 * Chỉ lấy phim đang chiếu hoặc sắp chiếu.
 */
    public List<Movie> findAllForShowtime() {
        String sql = "SELECT m.id, m.title, m.duration_min, m.description, m.release_date, "
            + "m.status, m.poster_url, m.trailer_url, m.actor, m.director, m.last_update, "
            + RATING_SUBQUERY
            + "FROM dbo.MOVIES m "
            + "WHERE m.status IN ('NOW_SHOWING', 'COMING_SOON') "
            + "ORDER BY m.title ASC";

        List<Movie> movies = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movies.add(mapRow(rs));
        }

        }   catch (SQLException e) {
            System.getLogger(MovieDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findAllForShowtime thất bại", e);
        }

        return movies;
    }

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
    private Movie mapRow(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setId(rs.getInt("id"));
        m.setTitle(rs.getString("title"));
        m.setDurationMin(rs.getInt("duration_min"));
        m.setDescription(rs.getString("description"));
        m.setReleaseDate(rs.getObject("release_date", LocalDate.class));
        m.setStatus(rs.getString("status"));
        m.setPosterUrl(rs.getString("poster_url"));
        m.setTrailerUrl(rs.getString("trailer_url"));
        m.setActor(rs.getString("actor"));
        m.setDirector(rs.getString("director"));
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
        String sql = "SELECT mc.movie_id, c.id, c.name, c.status "
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
                    c.setName(rs.getString("name"));
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
}
