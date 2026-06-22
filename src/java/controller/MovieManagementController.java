/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import dto.MovieAssignmentItem;
import java.io.IOException;
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
import model.User;
import service.MovieManagementService;

@WebServlet(
        name = "MovieManagementController",
        urlPatterns = {
            "/manager/movie-assignments/branches",
            "/manager/movie-assignments/halls",
            "/manager/movie-durations",
            "/manager/movie-durations/update"
        }
)
public class MovieManagementController extends HttpServlet {

    private static final String BRANCH_ASSIGNMENT_PAGE
            = "/pages/manager/branch-movie-assignment.jsp";

    private static final String HALL_ASSIGNMENT_PAGE
            = "/pages/manager/hall-movie-assignment.jsp";

    private static final String MOVIE_DURATION_PAGE
            = "/pages/manager/movie-duration-list.jsp";

    private final MovieManagementService movieManagementService
            = new MovieManagementService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
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
                response.sendRedirect(
                        request.getContextPath()
                        + "/manager/dashboard"
                );
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
                response.sendRedirect(
                        request.getContextPath()
                        + "/manager/dashboard"
                );
                break;
        }
    }

    /**
     * Hiển thị màn hình phân bổ phim cho chi nhánh.
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
            List<Branch> branches
                    = movieManagementService.getBranchesByManagerId(
                            manager.getId()
                    );

            int selectedBranchId
                    = parseInt(request.getParameter("branchId"));

            /*
             * Nếu chưa chọn chi nhánh thì mặc định chọn
             * chi nhánh đầu tiên Manager được quản lý.
             */
            if (selectedBranchId <= 0 && !branches.isEmpty()) {
                selectedBranchId = branches.get(0).getId();
            }

            /*
             * Chặn trường hợp truyền branchId không thuộc quyền Manager.
             */
            if (selectedBranchId > 0
                    && !movieManagementService.isManagerAllowedBranch(
                            manager.getId(),
                            selectedBranchId)) {

                setFlash(
                        request,
                        "error",
                        "Bạn không có quyền quản lý chi nhánh này."
                );

                response.sendRedirect(
                        request.getContextPath()
                        + "/manager/movie-assignments/branches"
                );
                return;
            }

            List<MovieAssignmentItem> movieItems
                    = new ArrayList<>();

            if (selectedBranchId > 0) {
                movieItems
                        = movieManagementService.getItemsForBranch(
                                manager.getId(),
                                selectedBranchId
                        );
            }

            request.setAttribute("branches", branches);
            request.setAttribute(
                    "selectedBranchId",
                    selectedBranchId
            );
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
     * Lưu danh sách phim đã chọn cho chi nhánh.
     */
    private void saveBranchAssignments(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        int branchId
                = parseInt(request.getParameter("branchId"));

        List<Integer> selectedMovieIds
                = parseMovieIds(
                        request.getParameterValues("movieIds")
                );

        try {
            boolean success
                    = movieManagementService.saveBranchAssignments(
                            manager.getId(),
                            branchId,
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
            setFlash(
                    request,
                    "error",
                    e.getMessage()
            );
        }

        response.sendRedirect(
                request.getContextPath()
                + "/manager/movie-assignments/branches"
                + "?branchId=" + branchId
        );
    }

    /**
     * Hiển thị màn hình phân bổ phim cho phòng chiếu.
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
            List<Branch> branches
                    = movieManagementService.getBranchesByManagerId(
                            manager.getId()
                    );

            int selectedBranchId
                    = parseInt(request.getParameter("branchId"));

            if (selectedBranchId <= 0 && !branches.isEmpty()) {
                selectedBranchId = branches.get(0).getId();
            }

            if (selectedBranchId > 0
                    && !movieManagementService.isManagerAllowedBranch(
                            manager.getId(),
                            selectedBranchId)) {

                setFlash(
                        request,
                        "error",
                        "Bạn không có quyền quản lý chi nhánh này."
                );

                response.sendRedirect(
                        request.getContextPath()
                        + "/manager/movie-assignments/halls"
                );
                return;
            }

            List<Hall> halls = new ArrayList<>();

            if (selectedBranchId > 0) {
                halls = movieManagementService.getHallsByBranchId(
                        manager.getId(),
                        selectedBranchId
                );
            }

            int selectedHallId
                    = parseInt(request.getParameter("hallId"));

            /*
             * Kiểm tra phòng được chọn có nằm trong chi nhánh
             * hiện tại hay không.
             */
            boolean hallBelongsToSelectedBranch
                    = containsHall(halls, selectedHallId);

            if (!hallBelongsToSelectedBranch) {
                selectedHallId = halls.isEmpty()
                        ? 0
                        : halls.get(0).getId();
            }

            List<MovieAssignmentItem> movieItems
                    = new ArrayList<>();

            if (selectedHallId > 0) {
                movieItems
                        = movieManagementService.getItemsForHall(
                                manager.getId(),
                                selectedHallId
                        );
            }

            request.setAttribute("branches", branches);
            request.setAttribute("halls", halls);
            request.setAttribute(
                    "selectedBranchId",
                    selectedBranchId
            );
            request.setAttribute(
                    "selectedHallId",
                    selectedHallId
            );
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
     * Lưu danh sách phim đã chọn cho phòng chiếu.
     */
    private void saveHallAssignments(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        int branchId
                = parseInt(request.getParameter("branchId"));

        int hallId
                = parseInt(request.getParameter("hallId"));

        List<Integer> selectedMovieIds
                = parseMovieIds(
                        request.getParameterValues("movieIds")
                );

        try {
            boolean success
                    = movieManagementService.saveHallAssignments(
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
            setFlash(
                    request,
                    "error",
                    e.getMessage()
            );
        }

        response.sendRedirect(
                request.getContextPath()
                + "/manager/movie-assignments/halls"
                + "?branchId=" + branchId
                + "&hallId=" + hallId
        );
    }

    /**
     * Hiển thị danh sách phim và thời lượng hiện tại.
     */
    private void showDurationList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        List<Movie> movies = movieManagementService.getAllMovies();

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

        int movieId = parseInt(request.getParameter("movieId"));
        String durationValue = request.getParameter("durationMin");

        try {
            int durationMin = movieManagementService.parseDuration(durationValue);

            boolean success = movieManagementService.updateDuration(
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
                request.getContextPath()
                + "/manager/movie-durations"
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

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return null;
        }

        User user
                = (User) session.getAttribute("user");

        if (!"MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(
                    request.getContextPath() + "/home"
            );
            return null;
        }

        return user;
    }

    /**
     * Chuyển mảng movieIds từ checkbox thành List<Integer>.
     *
     * Khi bỏ chọn toàn bộ checkbox, movieIds sẽ là null.
     */
    private List<Integer> parseMovieIds(String[] values) {
        List<Integer> movieIds = new ArrayList<>();

        if (values == null) {
            return movieIds;
        }

        for (String value : values) {
            int movieId = parseInt(value);

            if (movieId > 0 && !movieIds.contains(movieId)) {
                movieIds.add(movieId);
            }
        }

        return movieIds;
    }

    /**
     * Kiểm tra phòng có nằm trong danh sách phòng
     * của chi nhánh đang chọn hay không.
     */
    private boolean containsHall(
            List<Hall> halls,
            int hallId) {

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

    /**
     * Lưu thông báo tạm thời trong session.
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

