/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Quản lý phim Admin —  (Long)
 */
package controller;

import dao.CategoryDAO;
import dao.LanguageDAO;
import dto.MovieDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.Category;
import model.Language;
import model.Movie;
import service.MovieService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet xử lý quản lý phim dành cho admin/branch-manager.
 * URL: /admin/movies?action=list|new|add|edit|update|delete|status|upload
 *
 * @author LONG
 */
@WebServlet("/admin/moviesmanagement")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,       // 1 MB trước khi ghi ra disk
    maxFileSize       = 20 * 1024 * 1024,  // tối đa 20 MB mỗi file
    maxRequestSize    = 50 * 1024 * 1024   // tối đa 50 MB mỗi request
)
public class AdminMovieController extends HttpServlet {

    private final MovieService movieService   = new MovieService();
    private final CategoryDAO  categoryDAO    = new CategoryDAO();
    private final LanguageDAO  languageDAO    = new LanguageDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "list"   -> handleList  (req, resp);
            case "new"    -> handleNew   (req, resp);
            case "edit"   -> handleEdit  (req, resp);
            case "detail" -> handleDetail(req, resp);
            case "delete" -> handleDelete(req, resp);
            default       -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "add"    -> handleAdd   (req, resp);
            case "update" -> handleUpdate(req, resp);
            case "delete" -> handleDelete(req, resp);
            case "status" -> handleStatus(req, resp);   // AJAX
            case "upload"         -> handleUpload(req, resp);
            case "update-trailer" -> handleUpdateTrailer(req, resp);
            default       -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // ── GET handlers ─────────────────────────────────────────

    /**  Hiển thị danh sách phim cho admin, có hỗ trợ tìm kiếm và lọc trạng thái. */
    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = trim(req.getParameter("keyword"));
        String status  = trim(req.getParameter("status"));
        // Chuyển chuỗi rỗng thành null để DAO hiểu là "không lọc"
        if (keyword != null && keyword.isBlank()) keyword = null;
        if (status  != null && status.isBlank())  status  = null;

        List<MovieDTO> movies = movieService.getAllMoviesForAdmin(keyword, status);
        req.setAttribute("movies",  movies);
        req.setAttribute("keyword", keyword != null ? keyword : "");
        req.setAttribute("status",  status  != null ? status  : "");
        req.setAttribute("totalItems", movies.size());
        req.getRequestDispatcher("/pages/admin/movie-list.jsp").forward(req, resp);
    }

    /** Form thêm phim mới (hiển thị form rỗng). */
    private void handleNew(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("movie",      new Movie());
        req.setAttribute("categories", categoryDAO.findAllActive());
        req.setAttribute("languages",  languageDAO.findAllActive());
        req.setAttribute("formAction", "add");
        req.getRequestDispatcher("/pages/admin/movie-form.jsp").forward(req, resp);
    }

    /** Form chỉnh sửa phim (load sẵn dữ liệu phim). */
    private void handleEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = parseId(req.getParameter("id"));
        if (id <= 0) { resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=list"); return; }

        Movie movie = movieService.getMovieById(id);
        if (movie == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }

        req.setAttribute("movie",      movie);
        req.setAttribute("categories", categoryDAO.findAllActive());
        req.setAttribute("languages",  languageDAO.findAllActive());
        req.setAttribute("formAction", "update");
        req.getRequestDispatcher("/pages/admin/movie-form.jsp").forward(req, resp);
    }

    /** Trang chi tiết admin của một phim. */
    private void handleDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = parseId(req.getParameter("id"));
        if (id <= 0) { resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=list"); return; }
        Movie movie = movieService.getMovieById(id);
        if (movie == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        req.setAttribute("movie", movie);
        req.getRequestDispatcher("/pages/admin/movie-detail.jsp").forward(req, resp);
    }

    // ── POST handlers ────────────────────────────────────────

    /**  Xử lý thêm mới phim. */
    private void handleAdd(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Movie m = buildMovieFromRequest(req);
        List<Category> categories = categoryDAO.findAllActive();
        List<Language> languages = languageDAO.findAllActive();
        List<Integer> catIds  = resolveCategoryIds(req, categories);
        List<Integer> langIds = resolveLanguageIds(req, languages);

        boolean hasNewPoster = hasPosterFile(req);
        List<String> errors = movieService.validateMovie(m, catIds, langIds, !hasNewPoster);
        if (!errors.isEmpty()) {
            req.setAttribute("errors",     errors);
            req.setAttribute("movie",      m);
            req.setAttribute("categories", categories);
            req.setAttribute("languages",  languages);
            req.setAttribute("formAction", "add");
            req.getRequestDispatcher("/pages/admin/movie-form.jsp").forward(req, resp);
            return;
        }

        int newId = movieService.addMovie(m, catIds, langIds);
        if (newId > 0) {
            String uploaded = savePosterPart(req, newId);
            if (uploaded != null) {
                movieService.updatePoster(newId, uploaded);
            }
            req.getSession().setAttribute("flashSuccess", "Thêm phim thành công!");
            resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=list");
        } else {
            req.setAttribute("errors", List.of("Lỗi hệ thống, không thể thêm phim."));
            req.setAttribute("movie",      m);
            req.setAttribute("categories", categoryDAO.findAllActive());
            req.setAttribute("languages",  languageDAO.findAllActive());
            req.setAttribute("formAction", "add");
            req.getRequestDispatcher("/pages/admin/movie-form.jsp").forward(req, resp);
        }
    }

    /**  Xử lý cập nhật phim. */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Movie m = buildMovieFromRequest(req);
        m.setId(parseId(req.getParameter("id")));
        List<Category> categories = categoryDAO.findAllActive();
        List<Language> languages = languageDAO.findAllActive();
        List<Integer> catIds  = resolveCategoryIds(req, categories);
        List<Integer> langIds = resolveLanguageIds(req, languages);

        boolean hasNewPoster = hasPosterFile(req);
        boolean requirePoster = !hasNewPoster
                && (m.getPosterUrl() == null || m.getPosterUrl().isBlank());
        List<String> errors = movieService.editMovie(m, catIds, langIds, requirePoster);
        if (!errors.isEmpty()) {
            req.setAttribute("errors",     errors);
            req.setAttribute("movie",      m);
            req.setAttribute("categories", categories);
            req.setAttribute("languages",  languages);
            req.setAttribute("formAction", "update");
            req.getRequestDispatcher("/pages/admin/movie-form.jsp").forward(req, resp);
            return;
        }
        String uploaded = savePosterPart(req, m.getId());
        if (uploaded != null) {
            movieService.updatePoster(m.getId(), uploaded);
        }
        req.getSession().setAttribute("flashSuccess", "Cập nhật phim thành công!");
        resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=list");
    }

    /**  Xóa phim (chặn nếu có suất chiếu). */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int id = parseId(req.getParameter("id"));
        String error = movieService.deleteMovie(id);
        if (error != null) {
            req.getSession().setAttribute("flashError", error);
        } else {
            req.getSession().setAttribute("flashSuccess", "Xóa phim thành công!");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=list");
    }

    /**
     *  Cập nhật trạng thái phim — trả về JSON để AJAX gọi.
     * Request: POST ?action=status&id=X&status=NOW_SHOWING
     * Response: {"success":true,"message":"..."} hoặc {"success":false,"message":"..."}
     */
    private void handleStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        int    id        = parseId(req.getParameter("id"));
        String newStatus = req.getParameter("status");

        String result = movieService.changeStatus(id, newStatus);
        // result: null = OK, String = cảnh báo (nhưng vẫn đã update)
        if (result == null) {
            out.print("{\"success\":true,\"message\":\"Cập nhật trạng thái thành công.\"}");
        } else if (result.startsWith("Cảnh báo")) {
            out.print("{\"success\":true,\"message\":\"" + escapeJson(result) + "\"}");
        } else {
            out.print("{\"success\":false,\"message\":\"" + escapeJson(result) + "\"}");
        }
    }

    /**
     *  Upload poster hoặc trailer.
     * Request: POST, multipart/form-data; field "type" = poster|trailer, "movieId" = id
     * File lưu vào: {contextPath}/assets/uploads/movies/{movieId}/
     */
    private void handleUpload(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int    movieId = parseId(req.getParameter("movieId"));
        String type    = req.getParameter("type");    // "poster" | "trailer"
        Part   part    = req.getPart("file");

        if (movieId <= 0) {
            req.getSession().setAttribute("flashError", "Không xác định được phim cần upload.");
            resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=list");
            return;
        }
        if (part == null || part.getSize() == 0) {
            req.getSession().setAttribute("flashError", "Vui lòng chọn file trước khi bấm Upload.");
            resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=detail&id=" + movieId);
            return;
        }

        String fileName      = getFileName(part);
        String fileExtension = getExtension(fileName).toLowerCase();

        // Validate định dạng file ảnh/video trước khi lưu
        if (!"poster".equals(type)) {
            req.getSession().setAttribute("flashError",
                    "Chỉ hỗ trợ upload poster. Trailer: dùng link YouTube trên form sửa phim hoặc ô bên dưới.");
            resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=detail&id=" + movieId);
            return;
        }
        boolean validPoster = fileExtension.equals("jpg") || fileExtension.equals("png")
                || fileExtension.equals("jpeg") || fileExtension.equals("webp");
        if (!validPoster) {
            req.getSession().setAttribute("flashError", "Poster chỉ nhận jpg, png hoặc webp.");
            resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=detail&id=" + movieId);
            return;
        }

        // Tạo thư mục lưu trữ: webRoot/assets/uploads/movies/{movieId}/
        String uploadRoot = getServletContext().getRealPath("/assets/uploads/movies/" + movieId);
        File dir = new File(uploadRoot);
        if (!dir.exists()) dir.mkdirs();

        String savedName = type + "_" + System.currentTimeMillis() + "." + fileExtension;
        part.write(uploadRoot + File.separator + savedName);

        String relativeUrl = "assets/uploads/movies/" + movieId + "/" + savedName;

        movieService.updatePoster(movieId, relativeUrl);
        req.getSession().setAttribute("flashSuccess", "Upload poster thành công.");
        resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=detail&id=" + movieId);
    }

    /** Cập nhật link trailer YouTube từ trang chi tiết / sửa. */
    private void handleUpdateTrailer(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int movieId = parseId(req.getParameter("movieId"));
        if (movieId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=list");
            return;
        }
        String url = trim(req.getParameter("trailerUrl"));
        if (url != null && !url.isBlank()) {
            Movie probe = new Movie();
            probe.setTrailerUrl(url);
            if (probe.getEmbedUrl() == null) {
                req.getSession().setAttribute("flashError",
                        "Link YouTube không hợp lệ. Ví dụ: https://www.youtube.com/watch?v=...");
                resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=detail&id=" + movieId);
                return;
            }
        }
        movieService.updateTrailer(movieId, (url == null || url.isBlank()) ? null : url);
        req.getSession().setAttribute("flashSuccess", "Đã lưu link trailer YouTube.");
        resp.sendRedirect(req.getContextPath() + "/admin/moviesmanagement?action=detail&id=" + movieId);
    }

    // ── Utility helpers ──────────────────────────────────────

    /** Dựng Movie từ các tham số form (dùng cho cả add và update). */
    private Movie buildMovieFromRequest(HttpServletRequest req) {
        Movie m = new Movie();
        m.setTitle      (trim(req.getParameter("title")));
        m.setDescription(trim(req.getParameter("description")));
        m.setActor      (trim(req.getParameter("actor")));
        m.setDirector   (trim(req.getParameter("director")));
        m.setStatus     (trim(req.getParameter("status")));
        String poster = trim(req.getParameter("posterUrl"));
        if (poster == null || poster.isBlank()) {
            poster = trim(req.getParameter("existingPosterUrl"));
        }
        m.setPosterUrl(MovieService.normalizePosterPath(poster));
        m.setTrailerUrl (trim(req.getParameter("trailerUrl")));
        // duration
        try { m.setDurationMin(Integer.parseInt(req.getParameter("durationMin"))); }
        catch (NumberFormatException e) { m.setDurationMin(0); }
        // releaseDate
        try { m.setReleaseDate(LocalDate.parse(req.getParameter("releaseDate"))); }
        catch (Exception e) { m.setReleaseDate(null); }
        return m;
    }

    /** Parse int; trả về 0 nếu null hoặc không phải số. */
    private int parseId(String s) {
        try { return (s != null) ? Integer.parseInt(s.trim()) : 0; }
        catch (NumberFormatException e) { return 0; }
    }

    /** Chuyển mảng String → List<Integer>, bỏ qua các giá trị không hợp lệ. */
    private List<Integer> parseIds(String[] values) {
        List<Integer> list = new ArrayList<>();
        if (values == null) return list;
        for (String v : values) {
            try { list.add(Integer.parseInt(v.trim())); }
            catch (NumberFormatException ignored) {}
        }
        return list;
    }

    /** Map hidden genres (tên thể loại) hoặc categoryIds[] từ form. */
    private List<Integer> resolveCategoryIds(HttpServletRequest req, List<Category> allCats) {
        List<Integer> ids = parseIds(req.getParameterValues("categoryIds"));
        if (!ids.isEmpty()) return ids;
        String genres = trim(req.getParameter("genres"));
        if (genres == null || genres.isBlank()) return ids;
        for (String part : genres.split(",")) {
            String name = part.trim();
            if (name.isEmpty()) continue;
            for (Category c : allCats) {
                if (c.getName() != null && c.getName().equalsIgnoreCase(name)) {
                    ids.add(c.getId());
                    break;
                }
            }
        }
        return ids;
    }

    /** Map select language từ form sang language_id trong DB. */
    private List<Integer> resolveLanguageIds(HttpServletRequest req, List<Language> allLangs) {
        List<Integer> ids = parseIds(req.getParameterValues("languageIds"));
        if (!ids.isEmpty()) return ids;
        String lang = trim(req.getParameter("language"));
        if (lang == null || lang.isBlank()) return ids;
        Integer matched = null;
        for (Language l : allLangs) {
            String name = l.getName() != null ? l.getName().toLowerCase() : "";
            String code = l.getCode() != null ? l.getCode().toLowerCase() : "";
            switch (lang) {
                case "Vietnamese" -> {
                    if (name.contains("việt") || "vi".equals(code)) matched = l.getId();
                }
                case "English" -> {
                    if (name.contains("anh") || "en".equals(code)) matched = l.getId();
                }
                case "Korean" -> {
                    if (name.contains("hàn") || "ko".equals(code)) matched = l.getId();
                }
                case "Japanese" -> {
                    if (name.contains("nhật") || "ja".equals(code)) matched = l.getId();
                }
                case "Mixed" -> {
                    if (name.contains("việt") || "vi".equals(code)) matched = l.getId();
                }
                default -> {}
            }
            if (matched != null) break;
        }
        if (matched != null) {
            ids.add(matched);
        }
        return ids;
    }

    private boolean hasPosterFile(HttpServletRequest req) throws ServletException, IOException {
        Part part = req.getPart("posterFile");
        return part != null && part.getSize() > 0;
    }

    private String savePosterPart(HttpServletRequest req, int movieId) throws IOException, ServletException {
        Part part = req.getPart("posterFile");
        if (part == null || part.getSize() == 0) {
            return null;
        }
        return savePosterPart(req, movieId, part);
    }

    private String savePosterPart(HttpServletRequest req, int movieId, Part part) throws IOException {
        String fileName = getFileName(part);
        String ext = getExtension(fileName).toLowerCase();
        if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png") && !ext.equals("webp")) {
            return null;
        }
        String uploadRoot = getServletContext().getRealPath("/assets/uploads/movies/" + movieId);
        File dir = new File(uploadRoot);
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        String savedName = "poster_" + System.currentTimeMillis() + "." + ext;
        part.write(uploadRoot + File.separator + savedName);
        return "assets/uploads/movies/" + movieId + "/" + savedName;
    }

    /** Lấy tên file từ Part header Content-Disposition. */
    private String getFileName(Part part) {
        String header = part.getHeader("content-disposition");
        if (header == null) {
            return "upload";
        }
        for (String cd : header.split(";")) {
            if (cd.trim().startsWith("filename")) {
                String name = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
                if (slash >= 0) {
                    name = name.substring(slash + 1);
                }
                return name;
            }
        }
        return "upload";
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot >= 0) ? fileName.substring(dot + 1) : "";
    }

    /** Kiểm tra form có poster upload mới hay không. */
    private boolean hasPosterFile(HttpServletRequest req) throws ServletException, IOException {
        Part part = req.getPart("posterFile");
        return part != null && part.getSize() > 0;
    }

    /** Lưu poster local và trả về path tương đối để ghi vào MOVIES.poster_url. */
    private String savePosterPart(HttpServletRequest req, int movieId) throws IOException, ServletException {
        Part part = req.getPart("posterFile");
        if (part == null || part.getSize() == 0) return null;

        String ext = getExtension(getFileName(part)).toLowerCase();
        if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png") && !ext.equals("webp")) {
            return null;
        }
        String uploadRoot = getServletContext().getRealPath("/assets/uploads/movies/" + movieId);
        File dir = new File(uploadRoot);
        if (!dir.exists() && !dir.mkdirs()) return null;

        String savedName = "poster_" + System.currentTimeMillis() + "." + ext;
        part.write(uploadRoot + File.separator + savedName);
        return "assets/uploads/movies/" + movieId + "/" + savedName;
    }

    private String getFileName(Part part) {
        String header = part.getHeader("content-disposition");
        if (header == null) return "upload";
        for (String cd : header.split(";")) {
            if (cd.trim().startsWith("filename")) {
                String name = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
                return slash >= 0 ? name.substring(slash + 1) : name;
            }
        }
        return "upload";
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1) : "";
    }

    private String trim(String s) { return (s != null) ? s.trim() : null; }

    /** Escape tối thiểu cho chuỗi lồng vào JSON inline. */
    private String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
