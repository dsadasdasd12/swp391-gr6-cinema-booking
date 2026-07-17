package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import dao.BranchDAO;
import dao.ManagerPerformanceDAO;
import dto.ManagerPerformanceReport;
import dto.ManagerShowtimeProgress;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import model.Branch;
import model.User;

@WebServlet(name = "ManagerController", urlPatterns = {"/manager/dashboard"})
public class ManagerController extends HttpServlet {
    private final ManagerPerformanceDAO performanceDAO = new ManagerPerformanceDAO();
    private final BranchDAO branchDAO = new BranchDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = user.getRole();
        if (!"MANAGER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        if ("MANAGER".equalsIgnoreCase(role)) {
            populatePerformanceDashboard(request, user);
        }

        request.getRequestDispatcher("/pages/manager/dashboard.jsp").forward(request, response);
    }

    private void populatePerformanceDashboard(HttpServletRequest request, User manager) {
        int branchId = performanceDAO.getAssignedBranchId(manager.getId());
        if (branchId <= 0) {
            request.setAttribute("performanceUnavailable", true);
            return;
        }
        LocalDate from = parseDate(request.getParameter("fromDate"), LocalDate.now());
        LocalDate to = parseDate(request.getParameter("toDate"), from.plusDays(6));
        if (to.isBefore(from)) to = from;
        ManagerPerformanceReport report = performanceDAO.getReport(branchId,
                Timestamp.valueOf(from.atStartOfDay()), Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        String selectedMovie = request.getParameter("movie");
        if (selectedMovie == null) selectedMovie = "";

        Set<String> movies = new TreeSet<>();
        List<ManagerShowtimeProgress> filtered = new ArrayList<>();
        for (ManagerShowtimeProgress showtime : report.getShowtimes()) {
            movies.add(showtime.getMovieTitle());
            if (selectedMovie.isBlank() || selectedMovie.equals(showtime.getMovieTitle())) filtered.add(showtime);
        }
        int pageSize = 8;
        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) pageSize));
        int currentPage = parsePage(request.getParameter("page"), totalPages);
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        Branch branch = branchDAO.getBranchById(branchId);

        request.setAttribute("performanceBranchName", branch == null ? "Chi nhánh" : branch.getName());
        request.setAttribute("performanceFromDate", from.toString());
        request.setAttribute("performanceToDate", to.toString());
        request.setAttribute("performanceReport", report);
        request.setAttribute("performanceMovies", movies);
        request.setAttribute("performanceSelectedMovie", selectedMovie);
        request.setAttribute("performanceRows", filtered.subList(fromIndex, toIndex));
        request.setAttribute("performanceTotal", filtered.size());
        request.setAttribute("performancePage", currentPage);
        request.setAttribute("performanceTotalPages", totalPages);
    }

    private LocalDate parseDate(String value, LocalDate fallback) {
        if (value == null || value.isBlank()) return fallback;
        try { return LocalDate.parse(value); } catch (DateTimeParseException ignored) { return fallback; }
    }

    private int parsePage(String value, int totalPages) {
        try { return Math.max(1, Math.min(Integer.parseInt(value), totalPages)); }
        catch (Exception ignored) { return 1; }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
