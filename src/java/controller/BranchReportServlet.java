package controller;

import dao.AdminUserDAO;
import dao.BranchDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Branch;
import model.User;
import util.DBContext;
import util.EncodingUtil;

import service.UserService;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/admin/reports/branch", "/manager/reports/branch"})
public class BranchReportServlet extends HttpServlet {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final BranchDAO branchDAO = new BranchDAO();
    private final AdminUserDAO adminUserDAO = new AdminUserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            user = (User) req.getSession().getAttribute("adminUser");
        }
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<Branch> allBranches = branchDAO.findAllActive();
        int branchId = resolveBranchId(req, user, allBranches);

        Branch branch = branchDAO.findById(branchId);
        String assignedBranchName = (branch != null) ? branch.getName() : "Chi nhánh";
        boolean adminAllBranches = user.isAdmin();

        String fromStr = req.getParameter("fromDate");
        String toStr = req.getParameter("toDate");
        String action = req.getParameter("action");

        LocalDate today = LocalDate.now();
        if (toStr == null || toStr.isBlank()) {
            toStr = today.format(DATE_FORMAT);
        }
        if (fromStr == null || fromStr.isBlank()) {
            fromStr = today.minusDays(30).format(DATE_FORMAT);
        }

        // Fetch branch report details
        BranchReportData report = fetchBranchReportData(branchId, fromStr, toStr);

        // Export XLSX logic
        if ("export".equalsIgnoreCase(action)) {
            handleExportXlsx(resp, report, assignedBranchName);
            return;
        }

        req.setAttribute("assignedBranchName", assignedBranchName);
        req.setAttribute("adminAllBranches", adminAllBranches);
        req.setAttribute("allBranches", allBranches);
        req.setAttribute("selectedBranchId", branchId);
        req.setAttribute("report", report);
        req.setAttribute("reportUrl", req.getContextPath() + req.getServletPath());
        req.getRequestDispatcher("/pages/reports/branch.jsp").forward(req, resp);
    }

    private int resolveBranchId(HttpServletRequest req, User user, List<Branch> allBranches) {
        if (user.isAdmin()) {
            String branchParam = req.getParameter("branchId");
            if (branchParam != null && !branchParam.isBlank()) {
                try {
                    int id = Integer.parseInt(branchParam.trim());
                    for (Branch b : allBranches) {
                        if (b.getId() == id) {
                            return id;
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            return allBranches.isEmpty() ? 1 : allBranches.get(0).getId();
        }
        var managed = adminUserDAO.findById(user.getId());
        if (managed != null && managed.getBranchId() > 0) {
            return managed.getBranchId();
        }
        if ("MANAGER".equalsIgnoreCase(user.getRole())) {
            int fallbackId = new UserService().getBranchIdOfStaff(user.getId());
            if (fallbackId > 0) {
                return fallbackId;
            }
        }
        return allBranches.isEmpty() ? 1 : allBranches.get(0).getId();
    }

    private BranchReportData fetchBranchReportData(int branchId, String from, String to) {
        BranchReportData data = new BranchReportData();
        data.setFromDate(from);
        data.setToDate(to);

        String fromTs = from + " 00:00:00";
        String toTs = to + " 23:59:59";

        Connection conn = DBContext.getInstance().getConnection();

        // ── Query 1: KPI Statistics ──
        String kpiSql = "SELECT ISNULL(SUM(b.total_price), 0) AS total_revenue, "
                + "       ISNULL(SUM(bs_count.cnt), 0) AS total_tickets, "
                + "       COUNT(DISTINCT s.id) AS total_showtimes "
                + "FROM dbo.BOOKINGS b "
                + "OUTER APPLY ( "
                + "    SELECT COUNT(*) AS cnt FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id "
                + ") bs_count "
                + "JOIN dbo.SHOWTIMES s ON b.showtime_id = s.id "
                + "JOIN dbo.HALLS h ON s.hall_id = h.id "
                + "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') "
                + "  AND h.branch_id = ? "
                + "  AND b.booked_at >= ? AND b.booked_at <= ? ";

        try (PreparedStatement ps = conn.prepareStatement(kpiSql)) {
            ps.setInt(1, branchId);
            ps.setString(2, fromTs);
            ps.setString(3, toTs);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data.setTotalRevenue(rs.getDouble("total_revenue"));
                    data.setTotalTickets(rs.getInt("total_tickets"));
                    data.setTotalShowtimes(rs.getInt("total_showtimes"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        data.setAverageOccupancy(computeBranchAverageOccupancy(conn, branchId, fromTs, toTs));

        // ── Query 2: Daily Line Data ──
        String dailySql = "SELECT CAST(b.booked_at AS DATE) AS r_date, "
                + "       SUM(b.total_price) AS daily_rev, "
                + "       COUNT(DISTINCT s.id) AS daily_shows, "
                + "       SUM(bs_count.cnt) AS daily_tickets "
                + "FROM dbo.BOOKINGS b "
                + "OUTER APPLY ( "
                + "    SELECT COUNT(*) AS cnt FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id "
                + ") bs_count "
                + "JOIN dbo.SHOWTIMES s ON b.showtime_id = s.id "
                + "JOIN dbo.HALLS h ON s.hall_id = h.id "
                + "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') "
                + "  AND h.branch_id = ? "
                + "  AND b.booked_at >= ? AND b.booked_at <= ? "
                + "GROUP BY CAST(b.booked_at AS DATE) ORDER BY r_date ASC";

        List<String> daysList = new ArrayList<>();
        List<Double> occOverTime = new ArrayList<>();
        List<Integer> ticketDaily = new ArrayList<>();
        List<Integer> showtimeDaily = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(dailySql)) {
            ps.setInt(1, branchId);
            ps.setString(2, fromTs);
            ps.setString(3, toTs);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daysList.add(rs.getDate("r_date").toString());
                    int tickets = rs.getInt("daily_tickets");
                    int shows = rs.getInt("daily_shows");
                    double occ = shows > 0 ? Math.min(100.0, (tickets * 100.0) / (shows * 50.0)) : 0;
                    occOverTime.add(Math.round(occ * 10.0) / 10.0);
                    ticketDaily.add(rs.getInt("daily_tickets"));
                    showtimeDaily.add(rs.getInt("daily_shows"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        data.setOccupancyLabelsJson(toJsonArrayString(daysList));
        data.setOccupancyDataJson(toJsonArrayNumber(occOverTime));
        data.setComboLabelsJson(toJsonArrayString(daysList));
        data.setComboTicketsJson(toJsonArrayNumber(ticketDaily));
        data.setComboShowtimesJson(toJsonArrayNumber(showtimeDaily));

        // ── Query 3: Occupancy by Hall ──
        String hallSql = "SELECT h.name() AS hall_name, "
                + "       CASE WHEN SUM(h.total_seats) > 0 THEN CAST((COUNT(bs.id) * 100.0) / (COUNT(DISTINCT s.id) * h.total_seats) AS DECIMAL(5,2)) ELSE 0.0 END AS occupancy_rate "
                + "FROM dbo.HALLS h "
                + "LEFT JOIN dbo.SHOWTIMES s ON h.id = s.hall_id AND s.status != 'CANCELLED' "
                + "LEFT JOIN dbo.BOOKINGS b ON s.id = b.showtime_id AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') "
                + "LEFT JOIN dbo.BOOKING_SEATS bs ON b.id = bs.booking_id "
                + "WHERE h.branch_id = ? "
                + "GROUP BY h.id, h.name(), h.total_seats ORDER BY occupancy_rate DESC";

        List<String> hallLabels = new ArrayList<>();
        List<Double> hallData = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(hallSql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hallLabels.add(EncodingUtil.getString(rs, "hall_name"));
                    hallData.add(rs.getDouble("occupancy_rate"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        data.setHallLabelsJson(toJsonArrayString(hallLabels));
        data.setHallDataJson(toJsonArrayNumber(hallData));

        // ── Query 4: Top 5 Popular Movies ──
        String movieSql = "SELECT TOP 5 m.title, "
                + "       SUM(bs_count.cnt) AS ticket_count "
                + "FROM dbo.MOVIES m "
                + "JOIN dbo.SHOWTIMES s ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON s.hall_id = h.id "
                + "JOIN dbo.BOOKINGS b ON s.id = b.showtime_id "
                + "OUTER APPLY ( "
                + "    SELECT COUNT(*) AS cnt FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id "
                + ") bs_count "
                + "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') "
                + "  AND h.branch_id = ? "
                + "  AND b.booked_at >= ? AND b.booked_at <= ? "
                + "GROUP BY m.id, m.title ORDER BY ticket_count DESC";

        List<PopularMovie> popMovies = new ArrayList<>();
        int maxTickets = 0;
        try (PreparedStatement ps = conn.prepareStatement(movieSql)) {
            ps.setInt(1, branchId);
            ps.setString(2, fromTs);
            ps.setString(3, toTs);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PopularMovie pm = new PopularMovie();
                    pm.setTitle(EncodingUtil.getString(rs, "title"));
                    pm.setTicketCount(rs.getInt("ticket_count"));
                    popMovies.add(pm);
                    if (pm.getTicketCount() > maxTickets) {
                        maxTickets = pm.getTicketCount();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (PopularMovie pm : popMovies) {
            double pct = maxTickets > 0 ? (pm.getTicketCount() * 100.0) / maxTickets : 0.0;
            pm.setPercentage(Math.round(pct * 10.0) / 10.0);
        }
        data.setPopularMovies(popMovies);

        // ── Query 5: Daily Details table ──
        String tableSql = "SELECT CAST(b.booked_at AS DATE) AS report_date, "
                + "       COUNT(DISTINCT s.id) AS showtime_count, "
                + "       SUM(bs_count.cnt) AS ticket_count, "
                + "       SUM(b.total_price) AS revenue, "
                + "       CASE WHEN SUM(h.total_seats) > 0 THEN CAST((SUM(bs_count.cnt) * 100.0) / SUM(h.total_seats) AS DECIMAL(5,2)) ELSE 0.0 END AS occupancy_rate, "
                + "       CASE WHEN SUM(bs_count.cnt) > 0 THEN SUM(b.total_price) / SUM(bs_count.cnt) ELSE 0.0 END AS avg_ticket_price "
                + "FROM dbo.BOOKINGS b "
                + "OUTER APPLY ( "
                + "    SELECT COUNT(*) AS cnt FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id "
                + ") bs_count "
                + "JOIN dbo.SHOWTIMES s ON b.showtime_id = s.id "
                + "JOIN dbo.HALLS h ON s.hall_id = h.id "
                + "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') "
                + "  AND h.branch_id = ? "
                + "  AND b.booked_at >= ? AND b.booked_at <= ? "
                + "GROUP BY CAST(b.booked_at AS DATE) ORDER BY report_date DESC";

        List<Map<String, Object>> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(tableSql)) {
            ps.setInt(1, branchId);
            ps.setString(2, fromTs);
            ps.setString(3, toTs);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("report_date", rs.getDate("report_date").toString());
                    row.put("showtime_count", rs.getInt("showtime_count"));
                    row.put("ticket_count", rs.getInt("ticket_count"));
                    row.put("revenue", rs.getDouble("revenue"));
                    row.put("occupancy_rate", rs.getDouble("occupancy_rate"));
                    row.put("avg_ticket_price", rs.getDouble("avg_ticket_price"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        data.setRows(rows);

        return data;
    }

    private double computeBranchAverageOccupancy(Connection conn, int branchId, String fromTs, String toTs) {
        String sql = "SELECT AVG(CAST(occ AS FLOAT)) AS avg_occ FROM ( "
                + "  SELECT CASE WHEN h.total_seats > 0 AND COUNT(DISTINCT s.id) > 0 "
                + "    THEN (COUNT(bs.id) * 100.0) / (COUNT(DISTINCT s.id) * h.total_seats) ELSE 0 END AS occ "
                + "  FROM dbo.HALLS h "
                + "  JOIN dbo.SHOWTIMES s ON h.id = s.hall_id AND s.status != 'CANCELLED' "
                + "  LEFT JOIN dbo.BOOKINGS b ON s.id = b.showtime_id "
                + "    AND b.status IN ('CONFIRMED','CHECKED_IN','USED','COMPLETED') "
                + "    AND b.booked_at >= ? AND b.booked_at <= ? "
                + "  LEFT JOIN dbo.BOOKING_SEATS bs ON b.id = bs.booking_id "
                + "  WHERE h.branch_id = ? "
                + "  GROUP BY h.id, h.total_seats ) x";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fromTs);
            ps.setString(2, toTs);
            ps.setInt(3, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Math.round(rs.getDouble("avg_occ") * 100.0) / 100.0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void handleExportXlsx(HttpServletResponse resp, BranchReportData report, String branchName) throws IOException {
        String filename = "branch_" + branchName.toLowerCase().replace(" ", "_") + "_report_" + report.getFromDate() + "_to_" + report.getToDate() + ".xlsx";
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        java.util.List<String> headers = java.util.List.of(
                "Ngày chiếu",
                "Số suất chiếu",
                "Tổng vé bán",
                "Tỷ lệ lấp đầy",
                "Giá vé trung bình",
                "Doanh thu (VND)"
        );

        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        java.util.List<java.util.List<String>> data = new java.util.ArrayList<>();

        for (Map<String, Object> row : report.getRows()) {
            java.util.List<String> line = new java.util.ArrayList<>();
            Object reportDate = row.get("report_date");
            Object showtimeCount = row.get("showtime_count");
            Object ticketCount = row.get("ticket_count");
            Object occupancyRate = row.get("occupancy_rate");
            Object avgTicketPrice = row.get("avg_ticket_price");
            Object revenue = row.get("revenue");

            line.add(reportDate == null ? "" : reportDate.toString());
            line.add(showtimeCount == null ? "" : String.valueOf(showtimeCount));
            line.add(ticketCount == null ? "" : String.valueOf(ticketCount));
            if (occupancyRate == null) {
                line.add("");
            } else if (occupancyRate instanceof Number) {
                line.add(df.format(((Number) occupancyRate).doubleValue()) + "%");
            } else {
                line.add(occupancyRate.toString() + "%");
            }
            if (avgTicketPrice == null) {
                line.add("");
            } else if (avgTicketPrice instanceof Number) {
                line.add(df.format(((Number) avgTicketPrice).doubleValue()));
            } else {
                line.add(avgTicketPrice.toString());
            }
            line.add(revenue == null ? "" : revenue.toString());

            data.add(line);
        }

        util.XlsxExportUtil.writeSingleSheetXlsx(resp.getOutputStream(), "Branch Report", headers, data);
    }

    // JSON String Helpers
    private String toJsonArrayString(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i).replace("\"", "\\\"")).append("\"");
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonArrayNumber(List<? extends Number> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static class PopularMovie {

        private String title;
        private int ticketCount;
        private double percentage;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getTicketCount() {
            return ticketCount;
        }

        public void setTicketCount(int ticketCount) {
            this.ticketCount = ticketCount;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }

    public static class BranchReportData {

        private String fromDate;
        private String toDate;
        private double totalRevenue;
        private int totalTickets;
        private int totalShowtimes;
        private double averageOccupancy;
        private String occupancyLabelsJson;
        private String occupancyDataJson;
        private String hallLabelsJson;
        private String hallDataJson;
        private String comboLabelsJson;
        private String comboTicketsJson;
        private String comboShowtimesJson;
        private List<PopularMovie> popularMovies;
        private List<Map<String, Object>> rows;

        public String getFromDate() {
            return fromDate;
        }

        public void setFromDate(String fromDate) {
            this.fromDate = fromDate;
        }

        public String getToDate() {
            return toDate;
        }

        public void setToDate(String toDate) {
            this.toDate = toDate;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public int getTotalTickets() {
            return totalTickets;
        }

        public void setTotalTickets(int totalTickets) {
            this.totalTickets = totalTickets;
        }

        public int getTotalShowtimes() {
            return totalShowtimes;
        }

        public void setTotalShowtimes(int totalShowtimes) {
            this.totalShowtimes = totalShowtimes;
        }

        public double getAverageOccupancy() {
            return averageOccupancy;
        }

        public void setAverageOccupancy(double averageOccupancy) {
            this.averageOccupancy = averageOccupancy;
        }

        public String getOccupancyLabelsJson() {
            return occupancyLabelsJson;
        }

        public void setOccupancyLabelsJson(String occupancyLabelsJson) {
            this.occupancyLabelsJson = occupancyLabelsJson;
        }

        public String getOccupancyDataJson() {
            return occupancyDataJson;
        }

        public void setOccupancyDataJson(String occupancyDataJson) {
            this.occupancyDataJson = occupancyDataJson;
        }

        public String getHallLabelsJson() {
            return hallLabelsJson;
        }

        public void setHallLabelsJson(String hallLabelsJson) {
            this.hallLabelsJson = hallLabelsJson;
        }

        public String getHallDataJson() {
            return hallDataJson;
        }

        public void setHallDataJson(String hallDataJson) {
            this.hallDataJson = hallDataJson;
        }

        public String getComboLabelsJson() {
            return comboLabelsJson;
        }

        public void setComboLabelsJson(String comboLabelsJson) {
            this.comboLabelsJson = comboLabelsJson;
        }

        public String getComboTicketsJson() {
            return comboTicketsJson;
        }

        public void setComboTicketsJson(String comboTicketsJson) {
            this.comboTicketsJson = comboTicketsJson;
        }

        public String getComboShowtimesJson() {
            return comboShowtimesJson;
        }

        public void setComboShowtimesJson(String comboShowtimesJson) {
            this.comboShowtimesJson = comboShowtimesJson;
        }

        public List<PopularMovie> getPopularMovies() {
            return popularMovies;
        }

        public void setPopularMovies(List<PopularMovie> popularMovies) {
            this.popularMovies = popularMovies;
        }

        public List<Map<String, Object>> getRows() {
            return rows;
        }

        public void setRows(List<Map<String, Object>> rows) {
            this.rows = rows;
        }
    }
}
