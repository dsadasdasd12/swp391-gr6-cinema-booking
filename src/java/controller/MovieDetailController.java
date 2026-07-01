/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim (Xem chi tiết phim + Suất chiếu) - UC06 / UC12
 */
package controller;

import dao.FavoriteMovieDAO;
import dto.MovieAssignmentItem;
import dto.MovieFilter;
import dto.PageResult;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Branch;
import model.Category;
import model.Hall;
import model.Language;
import model.Movie;
import model.User;
import service.MovieService;


@WebServlet(
        name = "MovieDetailController",
        urlPatterns = {
            
            "/movie",
            "/movieslist",
            "/manager/movie-assignments/branches",
            "/manager/movie-assignments/halls",
            "/manager/movie-durations",
            "/manager/movie-durations/update"
        }
)
public class MovieDetailController extends HttpServlet {
    FavoriteMovieDAO favoriteMovieDAO = new FavoriteMovieDAO();

    private static final String MOVIE_LIST_PAGE
            = "/pages/movie/list.jsp";

    private static final String MOVIE_DETAIL_PAGE
            = "/pages/movie/detail.jsp";

    private static final String BRANCH_ASSIGNMENT_PAGE
            = "/pages/manager/branch-movie-assignment.jsp";

    private static final String HALL_ASSIGNMENT_PAGE
            = "/pages/manager/hall-movie-assignment.jsp";

    private static final String MOVIE_DURATION_PAGE
            = "/pages/manager/movie-duration-list.jsp";

    /** Single service for all Movie functions: browse, detail, assignment and duration. */
    private final MovieService movieService = new MovieService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {

            case "/movie":
                showMovieDetail(request, response);
                break;
                
                
            case "/movieslist":
                showMovieList(request, response);
                break;

            case "/manager/movie-assignments/branches":
                showBranchAssignmentPage(request, response);
                break;

            case "/manager/movie-assignments/halls":
                showHallAssignmentPage(request, response);
                break;

            case "/manager/movie-durations":
                showDurationList(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        switch (path) {
            case "/manager/movie-assignments/branches":
                saveBranchAssignments(request, response);
                break;

            case "/manager/movie-assignments/halls":
                saveHallAssignments(request, response);
                break;

            case "/manager/movie-durations/update":
                updateDuration(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                break;
        }
    }

    /**
     * Hiển thị danh sách phim công khai: search, filter, sort và pagination.
     */
    private void showMovieList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        MovieFilter filter = new MovieFilter();
        filter.setKeyword(request.getParameter("q"));
        filter.setStatus(emptyToNull(request.getParameter("status")));
        filter.setFormat(emptyToNull(request.getParameter("format")));
        filter.setSortBy(emptyToNull(request.getParameter("sort")));
        filter.setCategoryId(parseNullableInt(request.getParameter("category")));
        filter.setLanguageId(parseNullableInt(request.getParameter("language")));

        Integer page = parseNullableInt(request.getParameter("page"));
        if (page != null) {
            filter.setPage(page);
        }

        PageResult<Movie> result = movieService.browseMovies(filter);
        List<Category> categories = movieService.getCategories();
        List<Language> languages = movieService.getLanguages();

        request.setAttribute("result", result);
        request.setAttribute("filter", filter);
        request.setAttribute("categories", categories);
        request.setAttribute("languages", languages);
        request.setAttribute("queryString", buildQueryString(filter));

        request.getRequestDispatcher(MOVIE_LIST_PAGE).forward(request, response);
    }

    /**
     * Hiển thị chi tiết phim và các suất chiếu sắp tới theo chi nhánh.
     */
    private void showMovieDetail(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int movieId = parseMovieId(request.getParameter("id"));
        Movie movie = movieService.getMovieDetail(movieId);

        if (movie == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("notFound", Boolean.TRUE);
            request.getRequestDispatcher(MOVIE_DETAIL_PAGE).forward(request, response);
            return;
        }
         User user = (User) request.getSession().getAttribute("user");

        boolean favorite = false;

        if (user != null) {
            favorite = favoriteMovieDAO.exists(user.getId(), movie.getId());
        }

        request.setAttribute("favorite", favorite);
        request.setAttribute("movie", movie);
        request.setAttribute(
                "branchShowtimes",
                movieService.getShowtimesByBranch(movieId)
        );

        request.getRequestDispatcher(MOVIE_DETAIL_PAGE).forward(request, response);
    }

    /**
 * Hiển thị màn hình phân bổ phim cho Branch duy nhất của Manager.
 */
    private void showBranchAssignmentPage(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {

    User manager = getCurrentManager(request, response);

    if (manager == null) {
        return;
    }

    try {
        Branch branch = movieService.getAssignedBranch(manager.getId());

        List<MovieAssignmentItem> movieItems = new ArrayList<>();

        if (branch != null) {
            movieItems = movieService.getItemsForBranch(
                    manager.getId(),
                    branch.getId()
            );
        }

        request.setAttribute("branch", branch);
        request.setAttribute("movieItems", movieItems);

        request.getRequestDispatcher(BRANCH_ASSIGNMENT_PAGE)
                .forward(request, response);

    } catch (IllegalArgumentException e) {
        request.setAttribute("error", e.getMessage());

        request.getRequestDispatcher(BRANCH_ASSIGNMENT_PAGE)
                .forward(request, response);
    }
}

    /**
 * Lưu Movie Assignment cho Branch duy nhất của Manager.
 * branchId không được đọc từ request.
 */
    private void saveBranchAssignments(
        HttpServletRequest request,
        HttpServletResponse response)
        throws IOException {

    User manager = getCurrentManager(request, response);

    if (manager == null) {
        return;
    }

    Branch branch = movieService.getAssignedBranch(manager.getId());

    if (branch == null) {
        setFlash(
                request,
                "error",
                "Tài khoản Manager chưa được Admin phân công chi nhánh."
        );

        response.sendRedirect(
                request.getContextPath()
                + "/manager/movie-assignments/branches"
        );
        return;
    }

    List<Integer> selectedMovieIds = parseMovieIds(
            request.getParameterValues("movieIds")
    );

    try {
        boolean success = movieService.saveBranchAssignments(
                manager.getId(),
                branch.getId(),
                selectedMovieIds
        );

        if (success) {
            setFlash(
                    request,
                    "success",
                    "Lưu phân bổ phim cho chi nhánh thành công."
            );
        } else {
            setFlash(
                    request,
                    "error",
                    "Không thể lưu phân bổ phim cho chi nhánh."
            );
        }

    } catch (IllegalArgumentException e) {
        setFlash(request, "error", e.getMessage());
    }

    response.sendRedirect(
            request.getContextPath()
            + "/manager/movie-assignments/branches"
    );
}

    /**
 * Hiển thị màn hình phân bổ phim cho Hall.
 * Manager chỉ thấy Hall thuộc Branch được Admin phân công.
 */
    private void showHallAssignmentPage(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {

    User manager = getCurrentManager(request, response);

    if (manager == null) {
        return;
    }

    try {
        Branch branch = movieService.getAssignedBranch(manager.getId());

        List<Hall> halls = new ArrayList<>();
        List<MovieAssignmentItem> movieItems = new ArrayList<>();

        int selectedHallId = parsePositiveInt(
                request.getParameter("hallId")
        );

        if (branch != null) {
            halls = movieService.getHallsByBranchId(
                    manager.getId(),
                    branch.getId()
            );

            /*
             * Nếu hallId trên URL không thuộc Branch của Manager,
             * tự chọn Hall đầu tiên trong Branch đó.
             */
            if (!containsHall(halls, selectedHallId)) {
                selectedHallId = halls.isEmpty()
                        ? 0
                        : halls.get(0).getId();
            }

            if (selectedHallId > 0) {
                movieItems = movieService.getItemsForHall(
                        manager.getId(),
                        selectedHallId
                );
            }
        }

        request.setAttribute("branch", branch);
        request.setAttribute("halls", halls);
        request.setAttribute("selectedHallId", selectedHallId);
        request.setAttribute("movieItems", movieItems);

        request.getRequestDispatcher(HALL_ASSIGNMENT_PAGE)
                .forward(request, response);

    } catch (IllegalArgumentException e) {
        request.setAttribute("error", e.getMessage());

        request.getRequestDispatcher(HALL_ASSIGNMENT_PAGE)
                .forward(request, response);
    }
}

    /**
 * Lưu Movie Assignment cho Hall thuộc Branch của Manager.
 */
    private void saveHallAssignments(
        HttpServletRequest request,
        HttpServletResponse response)
        throws IOException {

    User manager = getCurrentManager(request, response);

    if (manager == null) {
        return;
    }

    Branch branch = movieService.getAssignedBranch(manager.getId());

    if (branch == null) {
        setFlash(
                request,
                "error",
                "Tài khoản Manager chưa được Admin phân công chi nhánh."
        );

        response.sendRedirect(
                request.getContextPath()
                + "/manager/movie-assignments/halls"
        );
        return;
    }

    int hallId = parsePositiveInt(request.getParameter("hallId"));

    List<Integer> selectedMovieIds = parseMovieIds(
            request.getParameterValues("movieIds")
    );

    try {
        /*
         * MovieService sẽ kiểm tra Hall có thuộc Branch của Manager hay không.
         */
        boolean success = movieService.saveHallAssignments(
                manager.getId(),
                hallId,
                selectedMovieIds
        );

        if (success) {
            setFlash(
                    request,
                    "success",
                    "Lưu phân bổ phim cho phòng chiếu thành công."
            );
        } else {
            setFlash(
                    request,
                    "error",
                    "Không thể lưu phân bổ phim cho phòng chiếu."
            );
        }

    } catch (IllegalArgumentException e) {
        setFlash(request, "error", e.getMessage());
    }

    response.sendRedirect(
            request.getContextPath()
            + "/manager/movie-assignments/halls"
            + "?hallId=" + hallId
    );
}

    /**
     * Hiển thị danh sách phim cùng thời lượng hiện tại để Manager chỉnh sửa.
     */
    private void showDurationList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User manager = getCurrentManager(request, response);
        if (manager == null) {
            return;
        }

        List<Movie> movies = movieService.getAllMovies();
        request.setAttribute("movies", movies);

        request.getRequestDispatcher(MOVIE_DURATION_PAGE)
                .forward(request, response);
    }

    /**
     * Cập nhật thời lượng của một phim.
     */
    private void updateDuration(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        User manager = getCurrentManager(request, response);
        if (manager == null) {
            return;
        }

        int movieId = parsePositiveInt(request.getParameter("movieId"));
        String durationValue = request.getParameter("durationMin");

        try {
            int durationMin = movieService.parseDuration(durationValue);

            boolean success = movieService.updateDuration(
                    movieId,
                    durationMin
            );

            if (success) {
                setFlash(
                        request,
                        "success",
                        "Cập nhật thời lượng phim thành công."
                );
            } else {
                setFlash(
                        request,
                        "error",
                        "Không thể cập nhật thời lượng phim."
                );
            }

        } catch (IllegalArgumentException e) {
            setFlash(request, "error", e.getMessage());
        }

        response.sendRedirect(
                request.getContextPath() + "/manager/movie-durations"
        );
    }

    /**
     * Lấy tài khoản Manager đang đăng nhập.
     */
    private User getCurrentManager(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }

        User user = (User) session.getAttribute("user");

        if (!"MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/home");
            return null;
        }

        return user;
    }

    /**
     * Chuyển mảng checkbox movieIds thành danh sách ID không trùng lặp.
     */
    private List<Integer> parseMovieIds(String[] values) {
        List<Integer> movieIds = new ArrayList<>();

        if (values == null) {
            return movieIds;
        }

        for (String value : values) {
            int movieId = parsePositiveInt(value);
            if (movieId > 0 && !movieIds.contains(movieId)) {
                movieIds.add(movieId);
            }
        }

        return movieIds;
    }

    /**
     * Kiểm tra hallId có thuộc danh sách Hall của Branch đang chọn hay không.
     */
    private boolean containsHall(List<Hall> halls, int hallId) {
        if (halls == null || hallId <= 0) {
            return false;
        }

        for (Hall hall : halls) {
            if (hall.getId() == hallId) {
                return true;
            }
        }

        return false;
    }

    /**
     * Dựng query-string giữ lại các tiêu chí lọc, trừ tham số page.
     */
    private String buildQueryString(MovieFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getKeyword() != null) {
            sb.append("q=").append(encode(filter.getKeyword())).append("&");
        }
        if (filter.getCategoryId() != null) {
            sb.append("category=").append(filter.getCategoryId()).append("&");
        }
        if (filter.getLanguageId() != null) {
            sb.append("language=").append(filter.getLanguageId()).append("&");
        }
        if (filter.getStatus() != null) {
            sb.append("status=").append(encode(filter.getStatus())).append("&");
        }
        if (filter.getFormat() != null) {
            sb.append("format=").append(encode(filter.getFormat())).append("&");
        }

        sb.append("sort=").append(encode(filter.getSortBy())).append("&");
        return sb.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static Integer parseNullableInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Trả về ID dương, hoặc 0 khi thiếu/sai định dạng.
     */
    private static int parsePositiveInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }

        try {
            int parsedValue = Integer.parseInt(value.trim());
            return parsedValue > 0 ? parsedValue : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Đọc movie ID cho URL detail; trả về -1 để MovieService xử lý not-found.
     */
    private static int parseMovieId(String value) {
        int movieId = parsePositiveInt(value);
        return movieId > 0 ? movieId : -1;
    }

    /**
     * Lưu thông báo tạm thời trong session để hiển thị sau redirect.
     */
    private void setFlash(
            HttpServletRequest request,
            String type,
            String message) {

        HttpSession session = request.getSession();
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }
}
