package controller;

import dao.BranchDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Branch;
import util.DBContext;
import util.EncodingUtil;

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

@WebServlet("/admin/reports/system")
public class SystemReportServlet extends HttpServlet {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final BranchDAO branchDAO = new BranchDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String fromStr = req.getParameter("fromDate");
        String toStr = req.getParameter("toDate");
        String branchIdStr = req.getParameter("branchId");
        String action = req.getParameter("action");

        LocalDate today = LocalDate.now();
        if (toStr == null || toStr.isBlank()) {
            toStr = today.format(DATE_FORMAT);
        }
        if (fromStr == null || fromStr.isBlank()) {
            fromStr = today.minusDays(30).format(DATE_FORMAT);
        }

        Integer branchId = null;
        if (branchIdStr != null && !branchIdStr.isBlank()) {
            try { branchId = Integer.parseInt(branchIdStr); } catch (NumberFormatException ignored) {}
        }

        // Fetch reporting data
        SystemReportData report = fetchSystemReportData(fromStr, toStr, branchId);

        // Export Excel (XLSX)
        if ("export".equalsIgnoreCase(action)) {
            handleExportXlsx(resp, report);
            return;
        }

        List<Branch> branches = branchDAO.findAllActive();

        req.setAttribute("report", report);
        req.setAttribute("branches", branches);
        req.getRequestDispatcher("/pages/reports/system.jsp").forward(req, resp);
    }

    private SystemReportData fetchSystemReportData(String from, String to, Integer branchId) {
        SystemReportData data = new SystemReportData();
        data.setFromDate(from);
        data.setToDate(to);

        String fromTs = from + " 00:00:00";
        String toTs = to + " 23:59:59";

        Connection conn = DBContext.getInstance().getConnection();

        // ── Query 1: KPIs ──
        StringBuilder kpiSql = new StringBuilder(
            "SELECT ISNULL(SUM(b.total_price), 0) AS total_revenue, " +
            "       ISNULL(SUM(bs_count.cnt), 0) AS total_tickets " +
            "FROM dbo.BOOKINGS b " +
            "OUTER APPLY ( " +
            "    SELECT COUNT(*) AS cnt FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id " +
            ") bs_count " +
            "JOIN dbo.SHOWTIMES s ON b.showtime_id = s.id " +
            "JOIN dbo.HALLS h ON s.hall_id = h.id " +
            "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') " +
            "  AND b.booked_at >= ? AND b.booked_at <= ? "
        );

        if (branchId != null && branchId > 0) {
            kpiSql.append("AND h.branch_id = ? ");
        }

        try (PreparedStatement ps = conn.prepareStatement(kpiSql.toString())) {
            ps.setString(1, fromTs);
            ps.setString(2, toTs);
            if (branchId != null && branchId > 0) {
                ps.setInt(3, branchId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data.setTotalRevenue(rs.getDouble("total_revenue"));
                    data.setTotalTickets(rs.getInt("total_tickets"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        data.setAverageOccupancy(computeAverageOccupancy(conn, fromTs, toTs, branchId));

        // ── Query 2: Daily Revenue and Ticket Data for Charts ──
        StringBuilder chartSql = new StringBuilder(
            "SELECT CAST(b.booked_at AS DATE) AS r_date, " +
            "       SUM(b.total_price) AS revenue, " +
            "       SUM(bs_count.cnt) AS tickets " +
            "FROM dbo.BOOKINGS b " +
            "OUTER APPLY ( " +
            "    SELECT COUNT(*) AS cnt FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id " +
            ") bs_count " +
            "JOIN dbo.SHOWTIMES s ON b.showtime_id = s.id " +
            "JOIN dbo.HALLS h ON s.hall_id = h.id " +
            "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') " +
            "  AND b.booked_at >= ? AND b.booked_at <= ? "
        );
        if (branchId != null && branchId > 0) {
            chartSql.append("AND h.branch_id = ? ");
        }
        chartSql.append("GROUP BY CAST(b.booked_at AS DATE) ORDER BY r_date ASC");

        List<String> revLabels = new ArrayList<>();
        List<Double> revData = new ArrayList<>();
        List<Integer> ticketData = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(chartSql.toString())) {
            ps.setString(1, fromTs);
            ps.setString(2, toTs);
            if (branchId != null && branchId > 0) {
                ps.setInt(3, branchId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    revLabels.add(rs.getDate("r_date").toString());
                    revData.add(rs.getDouble("revenue"));
                    ticketData.add(rs.getInt("tickets"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        data.setRevenueLabelsJson(toJsonArrayString(revLabels));
        data.setRevenueDataJson(toJsonArrayNumber(revData));
        data.setTicketLabelsJson(toJsonArrayString(revLabels));
        data.setTicketDataJson(toJsonArrayNumber(ticketData));

        // ── Query 3: Hall Occupancy for Horizontal Bar Chart ──
        StringBuilder occSql = new StringBuilder(
            "SELECT h.name() AS hall_name, br.name() AS branch_name, " +
            "       CASE WHEN SUM(h.total_seats) > 0 THEN CAST((COUNT(bs.id) * 100.0) / (COUNT(DISTINCT s.id) * h.total_seats) AS DECIMAL(5,2)) ELSE 0.0 END AS occupancy_rate " +
            "FROM dbo.HALLS h " +
            "JOIN dbo.BRANCHES br ON h.branch_id = br.id " +
            "LEFT JOIN dbo.SHOWTIMES s ON h.id = s.hall_id AND s.status != 'CANCELLED' " +
            "LEFT JOIN dbo.BOOKINGS b ON s.id = b.showtime_id AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') " +
            "LEFT JOIN dbo.BOOKING_SEATS bs ON b.id = bs.booking_id " +
            "WHERE 1=1 "
        );
        if (branchId != null && branchId > 0) {
            occSql.append("AND br.id = ? ");
        }
        occSql.append("GROUP BY br.name(), h.id, h.name(), h.total_seats ORDER BY occupancy_rate DESC");

        List<String> occLabels = new ArrayList<>();
        List<Double> occData = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(occSql.toString())) {
            if (branchId != null && branchId > 0) {
                ps.setInt(1, branchId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                int limit = 5;
                while (rs.next() && limit > 0) {
                    occLabels.add(EncodingUtil.getString(rs, "branch_name") + " - " + EncodingUtil.getString(rs, "hall_name"));
                    occData.add(rs.getDouble("occupancy_rate"));
                    limit--;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        data.setOccupancyLabelsJson(toJsonArrayString(occLabels));
        data.setOccupancyDataJson(toJsonArrayNumber(occData));

        // ── Query 4: Heatmap Matrix (đặt vé theo thứ × khung giờ, chuẩn hóa 0–100%) ──
        data.setHeatmapMatrixJson(matrixToJson(buildHeatmapMatrix(conn, fromTs, toTs, branchId)));

        // ── Query 5: Table Rows ──
        StringBuilder tableSql = new StringBuilder(
            "SELECT CAST(b.booked_at AS DATE) AS report_date, " +
            "       br.name() AS branch_name, " +
            "       COUNT(DISTINCT s.id) AS showtime_count, " +
            "       SUM(bs_count.cnt) AS ticket_count, " +
            "       SUM(b.total_price) AS revenue, " +
            "       CASE WHEN SUM(h.total_seats) > 0 THEN CAST((SUM(bs_count.cnt) * 100.0) / SUM(h.total_seats) AS DECIMAL(5,2)) ELSE 0.0 END AS occupancy_rate " +
            "FROM dbo.BOOKINGS b " +
            "OUTER APPLY ( " +
            "    SELECT COUNT(*) AS cnt FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id " +
            ") bs_count " +
            "JOIN dbo.SHOWTIMES s ON b.showtime_id = s.id " +
            "JOIN dbo.HALLS h ON s.hall_id = h.id " +
            "JOIN dbo.BRANCHES br ON h.branch_id = br.id " +
            "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') " +
            "  AND b.booked_at >= ? AND b.booked_at <= ? "
        );
        if (branchId != null && branchId > 0) {
            tableSql.append("AND br.id = ? ");
        }
        tableSql.append("GROUP BY CAST(b.booked_at AS DATE), br.name() ORDER BY report_date DESC, br.name() ASC");

        List<Map<String, Object>> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(tableSql.toString())) {
            ps.setString(1, fromTs);
            ps.setString(2, toTs);
            if (branchId != null && branchId > 0) {
                ps.setInt(3, branchId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("report_date", rs.getDate("report_date").toString());
                    row.put("branch_name", EncodingUtil.getString(rs, "branch_name"));
                    row.put("showtime_count", rs.getInt("showtime_count"));
                    row.put("ticket_count", rs.getInt("ticket_count"));
                    row.put("revenue", rs.getDouble("revenue"));
                    row.put("occupancy_rate", rs.getDouble("occupancy_rate"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        data.setRows(rows);

        return data;
    }

    private void handleExportXlsx(HttpServletResponse resp, SystemReportData report) throws IOException {
        String filename = "system_report_" + report.getFromDate() + "_to_" + report.getToDate() + ".xlsx";
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        java.util.List<String> headers = java.util.List.of(
                "Ngày báo cáo",
                "Chi nhánh",
                "Số vé bán",
                "Số suất chiếu",
                "Tỷ lệ lấp đầy",
                "Doanh thu (VND)"
        );

        java.util.List<java.util.List<String>> data = new java.util.ArrayList<>();
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");

        for (Map<String, Object> row : report.getRows()) {
            java.util.List<String> line = new java.util.ArrayList<>();
            line.add(String.valueOf(row.getOrDefault("report_date", "")));
            line.add(String.valueOf(row.getOrDefault("branch_name", "")));
            line.add(String.valueOf(row.getOrDefault("ticket_count", 0)));
            line.add(String.valueOf(row.getOrDefault("showtime_count", 0)));
            Object occRate = row.getOrDefault("occupancy_rate", 0.0);
            line.add(df.format(occRate instanceof Number ? ((Number) occRate).doubleValue() : 0.0) + "%");
            line.add(String.valueOf(row.getOrDefault("revenue", 0)));
            data.add(line);
        }

        util.XlsxExportUtil.writeSingleSheetXlsx(resp.getOutputStream(), "System Report", headers, data);
    }

    // JSON String Utility
    private String toJsonArrayString(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i).replace("\"", "\\\"")).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonArrayNumber(List<? extends Number> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String matrixToJson(int[][] matrix) {
        StringBuilder sb = new StringBuilder("[");
        for (int d = 0; d < 7; d++) {
            sb.append("[");
            for (int h = 0; h < 9; h++) {
                sb.append(matrix[d][h]);
                if (h < 8) sb.append(",");
            }
            sb.append("]");
            if (d < 6) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static final int[] HEATMAP_HOURS = {8, 10, 12, 14, 16, 18, 20, 22, 0};

    private double computeAverageOccupancy(Connection conn, String fromTs, String toTs, Integer branchId) {
        StringBuilder sql = new StringBuilder(
            "SELECT AVG(CAST(occ AS FLOAT)) AS avg_occ FROM ( "
            + "  SELECT CASE WHEN h.total_seats > 0 AND COUNT(DISTINCT s.id) > 0 "
            + "    THEN (COUNT(bs.id) * 100.0) / (COUNT(DISTINCT s.id) * h.total_seats) ELSE 0 END AS occ "
            + "  FROM dbo.HALLS h "
            + "  JOIN dbo.SHOWTIMES s ON h.id = s.hall_id AND s.status != 'CANCELLED' "
            + "  LEFT JOIN dbo.BOOKINGS b ON s.id = b.showtime_id "
            + "    AND b.status IN ('CONFIRMED','CHECKED_IN','USED','COMPLETED') "
            + "    AND b.booked_at >= ? AND b.booked_at <= ? "
            + "  LEFT JOIN dbo.BOOKING_SEATS bs ON b.id = bs.booking_id "
            + "  WHERE 1=1 ");
        if (branchId != null && branchId > 0) {
            sql.append("AND h.branch_id = ? ");
        }
        sql.append("  GROUP BY h.id, h.total_seats ) x");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, fromTs);
            ps.setString(2, toTs);
            if (branchId != null && branchId > 0) {
                ps.setInt(3, branchId);
            }
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

    private int[][] buildHeatmapMatrix(Connection conn, String fromTs, String toTs, Integer branchId) {
        int[][] counts = new int[7][9];
        StringBuilder sql = new StringBuilder(
            "SELECT ((DATEPART(WEEKDAY, b.booked_at) + 5) % 7) AS dow, "
            + "       DATEPART(HOUR, b.booked_at) AS hr, "
            + "       COUNT(*) AS cnt "
            + "FROM dbo.BOOKINGS b "
            + "JOIN dbo.SHOWTIMES s ON b.showtime_id = s.id "
            + "JOIN dbo.HALLS h ON s.hall_id = h.id "
            + "WHERE b.status IN ('CONFIRMED','CHECKED_IN','USED','COMPLETED') "
            + "  AND b.booked_at >= ? AND b.booked_at <= ? ");
        if (branchId != null && branchId > 0) {
            sql.append("AND h.branch_id = ? ");
        }
        sql.append("GROUP BY ((DATEPART(WEEKDAY, b.booked_at) + 5) % 7), DATEPART(HOUR, b.booked_at)");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, fromTs);
            ps.setString(2, toTs);
            if (branchId != null && branchId > 0) {
                ps.setInt(3, branchId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dow = rs.getInt("dow");
                    int hour = rs.getInt("hr");
                    int cnt = rs.getInt("cnt");
                    if (dow < 0 || dow > 6) continue;
                    int slot = hourSlotIndex(hour);
                    if (slot >= 0) {
                        counts[dow][slot] += cnt;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int max = 1;
        for (int d = 0; d < 7; d++) {
            for (int h = 0; h < 9; h++) {
                if (counts[d][h] > max) max = counts[d][h];
            }
        }

        int[][] matrix = new int[7][9];
        for (int d = 0; d < 7; d++) {
            for (int h = 0; h < 9; h++) {
                matrix[d][h] = (int) Math.round(100.0 * counts[d][h] / max);
            }
        }
        return matrix;
    }

    private static int hourSlotIndex(int hour) {
        for (int i = 0; i < HEATMAP_HOURS.length; i++) {
            if (HEATMAP_HOURS[i] == hour) return i;
        }
        return -1;
    }

    public static class SystemReportData {
        private String fromDate;
        private String toDate;
        private double totalRevenue;
        private int totalTickets;
        private double averageOccupancy;
        private String revenueLabelsJson;
        private String revenueDataJson;
        private String ticketLabelsJson;
        private String ticketDataJson;
        private String occupancyLabelsJson;
        private String occupancyDataJson;
        private String heatmapMatrixJson;
        private List<Map<String, Object>> rows;

        // Getters and Setters
        public String getFromDate() { return fromDate; }
        public void setFromDate(String fromDate) { this.fromDate = fromDate; }
        public String getToDate() { return toDate; }
        public void setToDate(String toDate) { this.toDate = toDate; }
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        public int getTotalTickets() { return totalTickets; }
        public void setTotalTickets(int totalTickets) { this.totalTickets = totalTickets; }
        public double getAverageOccupancy() { return averageOccupancy; }
        public void setAverageOccupancy(double averageOccupancy) { this.averageOccupancy = averageOccupancy; }
        public String getRevenueLabelsJson() { return revenueLabelsJson; }
        public void setRevenueLabelsJson(String revenueLabelsJson) { this.revenueLabelsJson = revenueLabelsJson; }
        public String getRevenueDataJson() { return revenueDataJson; }
        public void setRevenueDataJson(String revenueDataJson) { this.revenueDataJson = revenueDataJson; }
        public String getTicketLabelsJson() { return ticketLabelsJson; }
        public void setTicketLabelsJson(String ticketLabelsJson) { this.ticketLabelsJson = ticketLabelsJson; }
        public String getTicketDataJson() { return ticketDataJson; }
        public void setTicketDataJson(String ticketDataJson) { this.ticketDataJson = ticketDataJson; }
        public String getOccupancyLabelsJson() { return occupancyLabelsJson; }
        public void setOccupancyLabelsJson(String occupancyLabelsJson) { this.occupancyLabelsJson = occupancyLabelsJson; }
        public String getOccupancyDataJson() { return occupancyDataJson; }
        public void setOccupancyDataJson(String occupancyDataJson) { this.occupancyDataJson = occupancyDataJson; }
        public String getHeatmapMatrixJson() { return heatmapMatrixJson; }
        public void setHeatmapMatrixJson(String heatmapMatrixJson) { this.heatmapMatrixJson = heatmapMatrixJson; }
        public List<Map<String, Object>> getRows() { return rows; }
        public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
    }
}
