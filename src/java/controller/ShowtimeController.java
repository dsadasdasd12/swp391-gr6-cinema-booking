/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import dao.HallDAO;
import dao.MovieDAO;
import dao.StaffBranchDAO;
import dto.BranchHallGroup;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

    private static final String SHOWTIME_LIST_PAGE = "/pages/manager/showtime-list.jsp";
    private static final String SHOWTIME_FORM_PAGE = "/pages/manager/showtime-form.jsp";

    private final ShowtimeService showtimeService = new ShowtimeService();
    private final StaffBranchDAO staffBranchDAO = new StaffBranchDAO();
    private final HallDAO hallDAO = new HallDAO();
    private final MovieDAO movieDAO = new MovieDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
                response.sendRedirect(request.getContextPath() + "/manager/showtimes");
                break;
        }
    }

    private void listShowtimes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        List<Showtime> showtimes = showtimeService.getShowtimesByManagerId(user.getId());

        request.setAttribute("showtimes", showtimes);
        request.getRequestDispatcher(SHOWTIME_LIST_PAGE).forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        Showtime showtime = new Showtime();
        showtime.setStatus("SCHEDULED");
        showtime.setBasePrice(new BigDecimal("80000"));

        prepareFormData(request, user);

        request.setAttribute("showtime", showtime);
        request.setAttribute("formMode", "create");

        request.getRequestDispatcher(SHOWTIME_FORM_PAGE).forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        int id = parseInt(request.getParameter("id"));
        Showtime showtime = showtimeService.getShowtimeByIdAndManagerId(id, user.getId());

        if (showtime == null) {
            setFlash(request, "error", "Không tìm thấy suất chiếu hoặc bạn không có quyền chỉnh sửa.");
            response.sendRedirect(request.getContextPath() + "/manager/showtimes");
            return;
        }

        prepareFormData(request, user);

        request.setAttribute("showtime", showtime);
        request.setAttribute("formMode", "edit");

        request.getRequestDispatcher(SHOWTIME_FORM_PAGE).forward(request, response);
    }

    private void createShowtime(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        Showtime showtime = buildShowtimeFromRequest(request);

        if (!isHallAllowed(user.getId(), showtime.getHallId())) {
            request.setAttribute("error", "Bạn không có quyền tạo suất chiếu cho phòng chiếu này.");
            prepareFormData(request, user);
            request.setAttribute("showtime", showtime);
            request.setAttribute("formMode", "create");
            request.getRequestDispatcher(SHOWTIME_FORM_PAGE).forward(request, response);
            return;
        }

        try {
            boolean success = showtimeService.createShowtime(showtime);

            if (success) {
                setFlash(request, "success", "Tạo suất chiếu thành công.");
                response.sendRedirect(request.getContextPath() + "/manager/showtimes");
                return;
            }

            request.setAttribute("error", "Không thể tạo suất chiếu. Vui lòng thử lại.");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        }

        prepareFormData(request, user);

        request.setAttribute("showtime", showtime);
        request.setAttribute("formMode", "create");

        request.getRequestDispatcher(SHOWTIME_FORM_PAGE).forward(request, response);
    }

    private void updateShowtime(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        Showtime showtime = buildShowtimeFromRequest(request);
        showtime.setId(parseInt(request.getParameter("id")));

        Showtime current = showtimeService.getShowtimeByIdAndManagerId(showtime.getId(), user.getId());

        if (current == null) {
            setFlash(request, "error", "Không tìm thấy suất chiếu hoặc bạn không có quyền chỉnh sửa.");
            response.sendRedirect(request.getContextPath() + "/manager/showtimes");
            return;
        }

        if (!isHallAllowed(user.getId(), showtime.getHallId())) {
            request.setAttribute("error", "Bạn không có quyền gán suất chiếu cho phòng chiếu này.");
            prepareFormData(request, user);
            request.setAttribute("showtime", showtime);
            request.setAttribute("formMode", "edit");
            request.getRequestDispatcher(SHOWTIME_FORM_PAGE).forward(request, response);
            return;
        }

        try {
            boolean success = showtimeService.updateShowtime(showtime);

            if (success) {
                setFlash(request, "success", "Cập nhật suất chiếu thành công.");
                response.sendRedirect(request.getContextPath() + "/manager/showtimes");
                return;
            }

            request.setAttribute("error", "Không thể cập nhật suất chiếu. Vui lòng thử lại.");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        }

        prepareFormData(request, user);

        request.setAttribute("showtime", showtime);
        request.setAttribute("formMode", "edit");

        request.getRequestDispatcher(SHOWTIME_FORM_PAGE).forward(request, response);
    }

    private void cancelShowtime(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        int id = parseInt(request.getParameter("id"));
        boolean success = showtimeService.cancelShowtime(id, user.getId());

        if (success) {
            setFlash(request, "success", "Hủy suất chiếu thành công.");
        } else {
            setFlash(request, "error", "Không thể hủy suất chiếu hoặc bạn không có quyền thao tác.");
        }

        response.sendRedirect(request.getContextPath() + "/manager/showtimes");
    }

    private Showtime buildShowtimeFromRequest(HttpServletRequest request) {
        Showtime showtime = new Showtime();

        showtime.setMovieId(parseInt(request.getParameter("movieId")));
        showtime.setHallId(parseInt(request.getParameter("hallId")));
        showtime.setStartTime(parseDateTime(request.getParameter("startTime")));
        showtime.setBasePrice(parseBigDecimal(request.getParameter("basePrice")));
        showtime.setStatus(request.getParameter("status"));

        return showtime;
    }

    private void prepareFormData(HttpServletRequest request, User user) {
        List<Movie> movies = movieDAO.findAllForShowtime();
        List<Branch> branches = staffBranchDAO.findBranchesByUserId(user.getId());
        List<BranchHallGroup> branchHallGroups = new ArrayList<>();

        for (Branch branch : branches) {
            List<Hall> halls = hallDAO.findByBranchId(branch.getId());
            branchHallGroups.add(new BranchHallGroup(branch, halls));
        }

        request.setAttribute("movies", movies);
        request.setAttribute("branchHallGroups", branchHallGroups);
    }

    private boolean isHallAllowed(int userId, int hallId) {
        if (hallId <= 0) {
            return false;
        }

        List<Branch> branches = staffBranchDAO.findBranchesByUserId(userId);

        for (Branch branch : branches) {
            List<Hall> halls = hallDAO.findByBranchId(branch.getId());

            for (Hall hall : halls) {
                if (hall.getId() == hallId) {
                    return true;
                }
            }
        }

        return false;
    }

    private User getCurrentManager(HttpServletRequest request, HttpServletResponse response)
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

    private int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setFlash(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession();
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }
}