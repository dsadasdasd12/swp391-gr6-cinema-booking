/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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

@WebServlet(name = "ShowtimeController", urlPatterns = {
    "/manager/showtimes",
    "/manager/showtimes/create",
    "/manager/showtimes/edit",
    "/manager/showtimes/cancel"
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
            case "/manager/showtimes/create":
                showCreateForm(request, response);
                break;

            case "/manager/showtimes/edit":
                showEditForm(request, response);
                break;

            case "/manager/showtimes":
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
            case "/manager/showtimes/create":
                createShowtime(request, response);
                break;

            case "/manager/showtimes/edit":
                updateShowtime(request, response);
                break;

            case "/manager/showtimes/cancel":
                cancelShowtime(request, response);
                break;

            default:
                response.sendRedirect(
                        request.getContextPath()
                        + "/manager/showtimes"
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
                    + "/manager/showtimes"
            );
            return;
        }

        prepareFormData(
                request,
                branch,
                showtime.getHallId()
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
                        + "/manager/showtimes"
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
                    + "/manager/showtimes"
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
                        + "/manager/showtimes"
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

        int selectedHallId = prepareFormData(
                request,
                branch,
                showtime.getHallId()
        );

        showtime.setHallId(selectedHallId);

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
                + "/manager/showtimes"
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
            halls = hallDAO.findByBranchId(
                    branch.getId()
            );

            if (containsHall(halls, requestedHallId)) {
                selectedHallId = requestedHallId;

            } else if (!halls.isEmpty()) {
                selectedHallId = halls.get(0).getId();
            }

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
                    + "/manager/showtimes"
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