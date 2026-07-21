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
import dto.ManagerRevenueSummary;
import dto.ManagerOperationalSnapshot;
import dto.ManagerMoviePerformance;
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
        // Lần đầu mở dashboard không áp đặt mốc ngày: manager thấy toàn bộ suất
        // của chính chi nhánh mình. Ngày chỉ trở thành điều kiện khi người dùng nhập.
        LocalDate from = parseOptionalDate(request.getParameter("fromDate"));
        LocalDate to = parseOptionalDate(request.getParameter("toDate"));
        if (from != null && to != null && to.isBefore(from)) to = from;
        if (from != null && to == null) to = from;
        if (from == null && to != null) from = to;
        ManagerPerformanceReport report = performanceDAO.getReport(branchId,
                from == null ? null : Timestamp.valueOf(from.atStartOfDay()),
                to == null ? null : Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        ManagerRevenueSummary revenue = performanceDAO.getRevenueSummary(branchId,
                from == null ? null : Timestamp.valueOf(from.atStartOfDay()),
                to == null ? null : Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        ManagerOperationalSnapshot operations = performanceDAO.getOperationalSnapshot(branchId,
                from == null ? null : Timestamp.valueOf(from.atStartOfDay()),
                to == null ? null : Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        List<ManagerMoviePerformance> topMovies = performanceDAO.getTopMoviePerformance(branchId,
                from == null ? null : Timestamp.valueOf(from.atStartOfDay()),
                to == null ? null : Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        String selectedMovie = request.getParameter("movie");
        if (selectedMovie == null) selectedMovie = "";

        Set<String> movies = new TreeSet<>();
        List<ManagerShowtimeProgress> filtered = new ArrayList<>();
        for (ManagerShowtimeProgress showtime : report.getShowtimes()) {
            movies.add(showtime.getMovieTitle());
            if (selectedMovie.isBlank() || selectedMovie.equals(showtime.getMovieTitle())) filtered.add(showtime);
        }
        // KPI phải dùng cùng danh sách đang hiển thị sau khi lọc phim.
        int filteredCapacity = 0;
        int filteredSoldSeats = 0;
        for (ManagerShowtimeProgress showtime : filtered) {
            filteredCapacity += showtime.getCapacity();
            filteredSoldSeats += showtime.getSoldSeats();
        }
        ManagerPerformanceReport displayedReport = new ManagerPerformanceReport(
                filtered.size(), filteredCapacity, filteredSoldSeats, filtered, report.getHalls());
        int pageSize = 8;
        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) pageSize));
        int currentPage = parsePage(request.getParameter("page"), totalPages);
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        Branch branch = branchDAO.getBranchById(branchId);

        request.setAttribute("performanceBranchName", branch == null ? "Chi nhánh" : branch.getName());
        request.setAttribute("performanceFromDate", from == null ? "" : from.toString());
        request.setAttribute("performanceToDate", to == null ? "" : to.toString());
        request.setAttribute("performanceReport", displayedReport);
        request.setAttribute("performanceRevenue", revenue);
        request.setAttribute("performanceOperations", operations);
        request.setAttribute("performanceTopMovies", topMovies);
        request.setAttribute("performanceMovies", movies);
        request.setAttribute("performanceSelectedMovie", selectedMovie);
        request.setAttribute("performanceRows", filtered.subList(fromIndex, toIndex));
        request.setAttribute("performanceTotal", filtered.size());
        request.setAttribute("performancePage", currentPage);
        request.setAttribute("performanceTotalPages", totalPages);
    }

    private LocalDate parseOptionalDate(String value) {
        if (value == null || value.isBlank()) return null;
        try { return LocalDate.parse(value); } catch (DateTimeParseException ignored) { return null; }
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
