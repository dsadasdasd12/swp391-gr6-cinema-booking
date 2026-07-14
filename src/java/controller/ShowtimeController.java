package controller;

import dao.HallDAO;
import dao.MovieManagementDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Branch;
import model.Hall;
import model.Movie;
import model.Showtime;
import model.User;
import service.ShowtimeService;

/**
 * Controller cho luồng Manager quản lý suất chiếu tại chi nhánh được phân công.
 *
 * <p>Luồng UI:</p>
 * <ul>
 *   <li>GET {@code /manager/showtimesmanagement}: liệt kê suất chiếu của branch Manager.</li>
 *   <li>GET {@code /manager/showtimesmanagement/create}: mở form tạo suất chiếu.</li>
 *   <li>GET {@code /manager/showtimesmanagement/edit?id=...}: mở form sửa suất chiếu.</li>
 *   <li>POST {@code /manager/showtimesmanagement/create}: validate và tạo suất chiếu mới.</li>
 *   <li>POST {@code /manager/showtimesmanagement/edit}: validate và cập nhật suất chiếu.</li>
 *   <li>POST {@code /manager/showtimesmanagement/cancel}: hủy suất chiếu nếu thuộc branch Manager.</li>
 * </ul>
 *
 * <p>Controller luôn lấy branch từ Manager đang đăng nhập và chỉ cho thao tác trên hall/movie
 * thuộc branch đó.</p>
 *
 * @author HuyPD
 */
@WebServlet(name = "ShowtimeController", urlPatterns = {
    "/manager/showtimesmanagement",
    "/manager/showtimesmanagement/create",
    "/manager/showtimesmanagement/edit",
    "/manager/showtimesmanagement/cancel"
})
public class ShowtimeController extends HttpServlet {

    private static final String SHOWTIME_LIST_PAGE
            = "/pages/manager/showtime-list.jsp";

    private static final String SHOWTIME_FORM_PAGE
            = "/pages/manager/showtime-form.jsp";

    private final ShowtimeService showtimeService
            = new ShowtimeService();

    private final HallDAO hallDAO
            = new HallDAO();

    private final MovieManagementDAO movieManagementDAO
            = new MovieManagementDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/manager/showtimesmanagement/create":
                showCreateForm(request, response);
                break;

            case "/manager/showtimesmanagement/edit":
                showEditForm(request, response);
                break;

            case "/manager/showtimesmanagement":
            default:
                listShowtimes(request, response);
                break;
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        switch (path) {
            case "/manager/showtimesmanagement/create":
                createShowtime(request, response);
                break;

            case "/manager/showtimesmanagement/edit":
                updateShowtime(request, response);
                break;

            case "/manager/showtimesmanagement/cancel":
                cancelShowtime(request, response);
                break;

            default:
                response.sendRedirect(
                        request.getContextPath()
                        + "/manager/showtimesmanagement"
                );
                break;
        }
    }

    private void listShowtimes(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = showtimeService.getAssignedBranch(
                manager.getId()
        );

        List<Showtime> showtimes = new ArrayList<>();

        if (branch != null) {
            showtimes = showtimeService.getShowtimesByManagerId(
                    manager.getId()
            );
        }

        request.setAttribute("branch", branch);
        request.setAttribute("showtimes", showtimes);

        request.getRequestDispatcher(SHOWTIME_LIST_PAGE)
                .forward(request, response);
    }

    private void showCreateForm(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = getAssignedBranchOrRedirect(
                manager,
                request,
                response
        );

        if (branch == null) {
            return;
        }

        Showtime showtime = new Showtime();

        showtime.setStatus("SCHEDULED");
        showtime.setBasePrice(new BigDecimal("80000"));

        int selectedHallId = prepareFormData(
                request,
                branch,
                0
        );

        showtime.setHallId(selectedHallId);

        request.setAttribute("showtime", showtime);
        request.setAttribute("formMode", "create");

        request.getRequestDispatcher(SHOWTIME_FORM_PAGE)
                .forward(request, response);
    }

    private void showEditForm(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = getAssignedBranchOrRedirect(
                manager,
                request,
                response
        );

        if (branch == null) {
            return;
        }

        int showtimeId = parseInt(
                request.getParameter("id")
        );
        
        /*check showtime thuoc branch*/
        Showtime showtime = showtimeService.getShowtimeByIdAndManagerId(
                showtimeId,
                manager.getId()
        );

        if (showtime == null) {
            setFlash(
                    request,
                    "error",
                    "Không tìm thấy suất chiếu hoặc bạn không có quyền chỉnh sửa."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/manager/showtimesmanagement"
            );
            return;
        }

            /* lay hall hien tai cua showtime */
        Hall hall = hallDAO.findByIdAndBranchId(
                showtime.getHallId(),
                branch.getId()
        );

        if (!isHallActive(hall)) {
            setFlash(
                    request,
                    "error",
                    "Không thể chỉnh sửa suất chiếu vì phòng chiếu không ở trạng thái hoạt động."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/manager/showtimesmanagement"
            );
            return;
        }

        prepareEditFormData(
                request,
                branch,
                hall
        );

        request.setAttribute("showtime", showtime);
        request.setAttribute("formMode", "edit");

        request.getRequestDispatcher(SHOWTIME_FORM_PAGE)
                .forward(request, response);
    }

    private void createShowtime(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = getAssignedBranchOrRedirect(
                manager,
                request,
                response
        );

        if (branch == null) {
            return;
        }

        Showtime showtime = buildShowtimeFromRequest(request);

        try {
            boolean success = showtimeService.createShowtime(
                    manager.getId(),
                    showtime
            );

            if (success) {
                setFlash(
                        request,
                        "success",
                        "Tạo suất chiếu thành công."
                );

                response.sendRedirect(
                        request.getContextPath()
                        + "/manager/showtimesmanagement"
                );
                return;
            }

            request.setAttribute(
                    "error",
                    "Không thể tạo suất chiếu. Vui lòng thử lại."
            );

        } catch (IllegalArgumentException e) {
            request.setAttribute(
                    "error",
                    e.getMessage()
            );
        }

        int selectedHallId = prepareFormData(
                request,
                branch,
                showtime.getHallId()
        );

        showtime.setHallId(selectedHallId);

        request.setAttribute("showtime", showtime);
        request.setAttribute("formMode", "create");

        request.getRequestDispatcher(SHOWTIME_FORM_PAGE)
                .forward(request, response);
    }

    private void updateShowtime(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = getAssignedBranchOrRedirect(
                manager,
                request,
                response
        );

        if (branch == null) {
            return;
        }

        Showtime showtime = buildShowtimeFromRequest(request);

        showtime.setId(
                parseInt(request.getParameter("id"))
        );

        Showtime current = showtimeService.getShowtimeByIdAndManagerId(
                showtime.getId(),
                manager.getId()
        );

        if (current == null) {
            setFlash(
                    request,
                    "error",
                    "Không tìm thấy suất chiếu hoặc bạn không có quyền chỉnh sửa."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/manager/showtimesmanagement"
            );
            return;
        }

        try {
            boolean success = showtimeService.updateShowtime(
                    manager.getId(),
                    showtime
            );

            if (success) {
                setFlash(
                        request,
                        "success",
                        "Cập nhật suất chiếu thành công."
                );

                response.sendRedirect(
                        request.getContextPath()
                        + "/manager/showtimesmanagement"
                );
                return;
            }

            request.setAttribute(
                    "error",
                    "Không thể cập nhật suất chiếu. Vui lòng thử lại."
            );

        } catch (IllegalArgumentException e) {
            request.setAttribute(
                    "error",
                    e.getMessage()
            );
        }

        Hall lockedHall = hallDAO.findByIdAndBranchId(
                current.getHallId(),
                branch.getId()
        );

        showtime.setHallId(current.getHallId());

        prepareEditFormData(
                request,
                branch,
                lockedHall
        );

        request.setAttribute("showtime", showtime);
        request.setAttribute("formMode", "edit");

        request.getRequestDispatcher(SHOWTIME_FORM_PAGE)
                .forward(request, response);
    }

    private void cancelShowtime(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        int showtimeId = parseInt(
                request.getParameter("id")
        );

        boolean success = showtimeService.cancelShowtime(
                showtimeId,
                manager.getId()
        );

        if (success) {
            setFlash(
                    request,
                    "success",
                    "Hủy suất chiếu thành công."
            );
        } else {
            setFlash(
                    request,
                    "error",
                    "Không thể hủy suất chiếu hoặc bạn không có quyền thao tác."
            );
        }

        response.sendRedirect(
                request.getContextPath()
                + "/manager/showtimesmanagement"
        );
    }

    private Showtime buildShowtimeFromRequest(
            HttpServletRequest request
    ) {
        Showtime showtime = new Showtime();

        showtime.setMovieId(
                parseInt(request.getParameter("movieId"))
        );

        showtime.setHallId(
                parseInt(request.getParameter("hallId"))
        );

        showtime.setStartTime(
                parseDateTime(
                        request.getParameter("startTime")
                )
        );

        showtime.setBasePrice(
                parseBigDecimal(
                        request.getParameter("basePrice")
                )
        );

        showtime.setStatus(
                request.getParameter("status")
        );

        return showtime;
    }
    
    /*form create*/
    private int prepareFormData(
            HttpServletRequest request,
            Branch branch,
            int requestedHallId
    ) {
        List<Hall> halls = new ArrayList<>();
        Map<Integer, List<Movie>> moviesByHall
                = new LinkedHashMap<>();

        List<Movie> selectedHallMovies = new ArrayList<>();

        int selectedHallId = 0;

        if (branch != null) {
            List<Hall> branchHalls = hallDAO.findByBranchId(
                    branch.getId()
            );
            
            /*hall active*/
            for (Hall hall : branchHalls) {
                if (isHallActive(hall)) {
                    halls.add(hall);
                }
            }
            
            /*hall mac dinh*/
            if (containsHall(halls, requestedHallId)) {
                selectedHallId = requestedHallId;

            } else if (!halls.isEmpty()) {
                selectedHallId = halls.get(0).getId();
            }
            
            /*lay danh sach phim*/
            for (Hall hall : halls) {
                List<Movie> assignedMovies
                        = movieManagementDAO.findMoviesAssignedToHall(
                                hall.getId()
                        );

                moviesByHall.put(
                        hall.getId(),
                        assignedMovies
                );
            }

            if (selectedHallId > 0
                    && moviesByHall.containsKey(selectedHallId)) {

                selectedHallMovies = moviesByHall.get(
                        selectedHallId
                );
            }
        }

        request.setAttribute("branch", branch);
        request.setAttribute("halls", halls);
        request.setAttribute("moviesByHall", moviesByHall);
        request.setAttribute("movies", selectedHallMovies);
        request.setAttribute("selectedHallId", selectedHallId);

        return selectedHallId;
    }

        /*form edit*/
    private void prepareEditFormData(
            HttpServletRequest request,
            Branch branch,
            Hall lockedHall
    ) {
        List<Hall> halls = new ArrayList<>();
        Map<Integer, List<Movie>> moviesByHall
                = new LinkedHashMap<>();

        List<Movie> selectedHallMovies = new ArrayList<>();
        int selectedHallId = 0;

        if (branch != null && isHallActive(lockedHall)) {
            halls.add(lockedHall);
            selectedHallId = lockedHall.getId();

            selectedHallMovies
                    = movieManagementDAO.findMoviesAssignedToHall(
                            selectedHallId
                    );

            moviesByHall.put(
                    selectedHallId,
                    selectedHallMovies
            );
        }

        request.setAttribute("branch", branch);
        request.setAttribute("halls", halls);
        request.setAttribute("moviesByHall", moviesByHall);
        request.setAttribute("movies", selectedHallMovies);
        request.setAttribute("selectedHallId", selectedHallId);
        request.setAttribute("lockHallOnEdit", true);
    }

    private boolean isHallActive(Hall hall) {
        return hall != null
                && "ACTIVE".equalsIgnoreCase(
                        hall.getStatus()
                );
    }

    private boolean containsHall(
            List<Hall> halls,
            int hallId
    ) {
        if (hallId <= 0 || halls == null) {
            return false;
        }

        for (Hall hall : halls) {
            if (hall.getId() == hallId) {
                return true;
            }
        }

        return false;
    }

    private Branch getAssignedBranchOrRedirect(
            User manager,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        Branch branch = showtimeService.getAssignedBranch(
                manager.getId()
        );

        if (branch == null) {
            setFlash(
                    request,
                    "error",
                    "Tài khoản Manager chưa được Admin phân công chi nhánh."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/manager/showtimesmanagement"
            );
        }

        return branch;
    }

    private User getCurrentManager(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/login"
            );

            return null;
        }

        User user = (User) session.getAttribute("user");

        if (!"MANAGER".equalsIgnoreCase(
                user.getRole()
        )) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/home"
            );

            return null;
        }

        return user;
    }

    private int parseInt(String value) {
        if (value == null
                || value.trim().isEmpty()) {

            return 0;
        }

        try {
            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {
            return LocalDateTime.parse(
                    value.trim()
            );

        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {
            return new BigDecimal(
                    value.trim()
            );

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setFlash(
            HttpServletRequest request,
            String type,
            String message
    ) {
        HttpSession session = request.getSession();

        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }
}
