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
import service.ReviewService;


/**
 * Controller cho các luồng phim ở phía khách và một số màn hình phân bổ phim của Manager.
 *
 * <p>Luồng khách:</p>
 * <ul>
 *   <li>View/Search/Filter movies: GET {@code /movies} hoặc {@code /movieslist}.</li>
 *   <li>Controller đọc tham số {@code q}, {@code status}, {@code format}, {@code category},
 *       {@code language}, {@code sort}, {@code page} rồi dựng {@link MovieFilter}.</li>
 *   <li>Gọi {@link MovieService#browseMovies(MovieFilter)} để lấy danh sách phim phân trang.</li>
 *   <li>Forward sang {@code /pages/movie/list.jsp} để render kết quả.</li>
 *   <li>View movie details: GET {@code /movie?id=...}.</li>
 *   <li>Controller lấy chi tiết phim, trạng thái yêu thích của user nếu đã đăng nhập,
 *       và suất chiếu theo chi nhánh rồi forward sang {@code /pages/movie/detail.jsp}.</li>
 * </ul>
 *
 * <p>Luồng Manager:</p>
 * <ul>
 *   <li>Phân bổ phim cho chi nhánh/phòng chiếu và cập nhật thời lượng phim.</li>
 *   <li>Controller luôn lấy branch từ Manager đang đăng nhập, không tin branchId gửi từ form.</li>
 * </ul>
 *
 * @author HuyPD
 */
@WebServlet(
        name = "MovieDetailController",
        urlPatterns = {
            
            "/movie",
            "/movies",
            "/movieslist",
            "/manager/movie-assignments/branches",
            "/manager/movie-assignments/halls",
            "/manager/movie-durations",
            "/manager/movie-durations/update"
        }
)
public class MovieDetailController extends HttpServlet {
    FavoriteMovieDAO favoriteMovieDAO = new FavoriteMovieDAO();
    private final ReviewService reviewService = new ReviewService();

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
            case "/movies":
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

        /*
         * MovieFilter la object gom tat ca dieu kien loc/search/sort/pagination.
         * Dung object nay giup khong phai truyen qua nhieu tham so roi rac xuong service/DAO.
         */
        MovieFilter filter = new MovieFilter();

        // q la keyword user go vao o search.
        filter.setKeyword(request.getParameter("q"));

        // status vi du NOW_SHOWING/COMING_SOON; rong thi doi thanh null de khong loc.
        filter.setStatus(emptyToNull(request.getParameter("status")));

        // format vi du 2D/3D/IMAX; rong thi null.
        filter.setFormat(emptyToNull(request.getParameter("format")));

        // sort quy dinh cach sap xep danh sach phim.
        filter.setSortBy(emptyToNull(request.getParameter("sort")));

        // category/language la id so, parse sai thi null de bo qua filter do.
        filter.setCategoryId(parseNullableInt(request.getParameter("category")));
        filter.setLanguageId(parseNullableInt(request.getParameter("language")));

        // page la trang hien tai cua pagination; khong co thi MovieFilter dung default.
        Integer page = parseNullableInt(request.getParameter("page"));
        if (page != null) {
            filter.setPage(page);
        }

        /*
         * Service tra ve PageResult gom list phim + thong tin phan trang.
         * Controller khong tu query DB o day.
         */
        PageResult<Movie> result = movieService.browseMovies(filter);

        // Lay danh sach category/language de render dropdown filter tren JSP.
        List<Category> categories = movieService.getCategories();
        List<Language> languages = movieService.getLanguages();

        // result la danh sach phim da search/filter/sort/paginate.
        request.setAttribute("result", result);

        // filter giup JSP giu lai gia tri user da chon sau khi submit.
        request.setAttribute("filter", filter);

        // categories/languages dung de render cac option filter.
        request.setAttribute("categories", categories);
        request.setAttribute("languages", languages);

        // queryString giup link pagination giu lai dieu kien loc hien tai.
        request.setAttribute("queryString", buildQueryString(filter));
        
        //Trong http nó có 2 cái để redirect ( truyển về trang) . Cái 1 là respone (res.sendredic), cái này
        // nó k lưu được attribute tại vì khi respone nó sẽ hết 1 phiên của request, nghĩa là các attribute 
        //không dược gửi kèm và trang bị reset, nếu muốn set attribute gửi kèm thì phải dùng dòng dưới, sẽ không reset trang và kèm 
        //attribute của requeust

        request.getRequestDispatcher(MOVIE_LIST_PAGE).forward(request, response);
    }

    /**
     * Hiển thị chi tiết phim và các suất chiếu sắp tới theo chi nhánh.
     */
    private void showMovieDetail(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Doc id phim tu URL /movie?id=...
        int movieId = parseMovieId(request.getParameter("id"));

        // Lay thong tin chi tiet phim theo id.
        Movie movie = movieService.getMovieDetail(movieId);

        if (movie == null) {
            // Khong tim thay phim thi tra 404 nhung van render JSP detail o trang thai notFound.
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("notFound", Boolean.TRUE);
            request.getRequestDispatcher(MOVIE_DETAIL_PAGE).forward(request, response);
            return;
        }

        // Lay user trong session de biet user nay da favorite phim chua.
        User user = (User) request.getSession().getAttribute("user");

        // Mac dinh chua favorite; chi check DB neu user da dang nhap.
        boolean favorite = false;

        if (user != null) {
            // Kiem tra phim nay co trong danh sach yeu thich cua user khong.
            favorite = favoriteMovieDAO.exists(user.getId(), movie.getId());
        }

        // Gui trang thai favorite sang JSP de render nut yeu thich dung trang thai.
        request.setAttribute("favorite", favorite);

        // Gui object movie sang JSP de hien title, poster, description, trailer...
        request.setAttribute("movie", movie);

        // Lay danh sach suat chieu cua phim, gom theo chi nhanh de JSP hien lich chieu.
        request.setAttribute(
                "branchShowtimes",
                movieService.getShowtimesByBranch(movieId)
        );
        request.setAttribute("reviews", reviewService.getMovieReviews(movieId));

        // forward sang detail JSP, giu lai cac attribute vua set.
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
        
        /*Doi hall tren giao dien*/
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
