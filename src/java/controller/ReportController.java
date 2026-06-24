/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Reporting & Analytics —  (Long)
 */
package controller;

import dto.ReportDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ReportService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Servlet xử lý các chức năng báo cáo & phân tích số liệu cho admin.
 * URL: /admin/reports?type=revenue|sales|occupancy|popular|peak
 * 
 * Hỗ trợ xuất dữ liệu ra định dạng CSV (tương thích Excel) 100% không phụ thuộc thư viện ngoài.
 *
 * @author LONG
 */
@WebServlet("/admin/reports")
public class ReportController extends HttpServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String type = req.getParameter("type");
        if (type == null) type = "revenue";

        String action = req.getParameter("action");
        String fromDate = req.getParameter("fromDate");
        String toDate = req.getParameter("toDate");

        // ── Điều hướng theo loại báo cáo ───────────────────────
        ReportDTO report = null;
        String jspPath = "";

        switch (type) {
            case "revenue" -> {
                report = reportService.buildRevenueReport(fromDate, toDate);
                jspPath = "/pages/admin/report-revenue.jsp";
            }
            case "sales" -> {
                report = reportService.buildSalesReport(fromDate, toDate);
                jspPath = "/pages/admin/report-sales.jsp";
            }
            case "occupancy" -> {
                report = reportService.buildOccupancyReport();
                jspPath = "/pages/admin/report-occupancy.jsp";
            }
            case "popular" -> {
                report = reportService.buildPopularMoviesReport(fromDate, toDate);
                jspPath = "/pages/admin/report-popular.jsp";
            }
            case "peak" -> {
                report = reportService.buildPeakHoursReport(fromDate, toDate);
                jspPath = "/pages/admin/report-peak.jsp";
            }
            case "activity" -> {
                report = reportService.buildCustomerActivityReport(fromDate, toDate);
                jspPath = "/pages/admin/report-activity.jsp";
            }
            default -> {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        // ── Nếu yêu cầu export ───────────────────────────────
        if ("export".equals(action) && report != null) {
            handleExportXlsx(resp, report);
            return;
        }

        // ── Trả về giao diện JSP ─────────────────────────────
        req.setAttribute("report", report);
        req.getRequestDispatcher(jspPath).forward(req, resp);
    }

    /**
     * Xuất dữ liệu báo cáo ra file XLSX (1 sheet) để Excel mở được dấu tiếng Việt.
     * (Không phụ thuộc thư viện Apache POI.)
     */
    private void handleExportXlsx(HttpServletResponse resp, ReportDTO report) throws IOException {
        String filename = report.getReportType().toLowerCase() + "_report.xlsx";

        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        List<Map<String, Object>> rows = report.getRows();
        List<String> headers = new java.util.ArrayList<>();
        java.util.List<java.util.List<String>> data = new java.util.ArrayList<>();

        int cols = 0;
        if (!rows.isEmpty()) {
            Map<String, Object> firstRow = rows.get(0);
            cols = firstRow.size();
            for (String key : firstRow.keySet()) {
                headers.add(translateHeader(key));
            }

            DecimalFormat df = new DecimalFormat("#.##");
            for (Map<String, Object> row : rows) {
                java.util.List<String> line = new java.util.ArrayList<>();
                for (String key : firstRow.keySet()) {
                    Object val = row.get(key);
                    if (val == null) {
                        line.add("");
                    } else if (val instanceof Number) {
                        line.add(df.format(val));
                    } else {
                        line.add(val.toString());
                    }
                }
                data.add(line);
            }
        } else {
            headers.add("Không có dữ liệu");
            cols = 1;
        }

        // KPI summary rows (append at end)
        DecimalFormat df = new DecimalFormat("#.##");
        if ("OCCUPANCY".equals(report.getReportType())) {
            java.util.List<String> s = new java.util.ArrayList<>();
            for (int i = 0; i < cols; i++) s.add("");
            if (cols >= 1) s.set(0, "Tỷ lệ lấp đầy trung bình (%)");
            if (cols >= 2) s.set(1, report.getAverageOccupancy() + "%");
            data.add(s);
        } else {
            java.util.List<String> s1 = new java.util.ArrayList<>();
            java.util.List<String> s2 = new java.util.ArrayList<>();
            java.util.List<String> s3 = new java.util.ArrayList<>();
            for (int i = 0; i < cols; i++) { s1.add(""); s2.add(""); s3.add(""); }
            if (cols >= 1) s1.set(0, "Tổng số giao dịch");
            if (cols >= 2) s1.set(1, String.valueOf(report.getTotalBookings()));
            if (cols >= 1) s2.set(0, "Tổng số vé bán ra");
            if (cols >= 2) s2.set(1, String.valueOf(report.getTotalTickets()));
            if (cols >= 1) s3.set(0, "Tổng doanh thu");
            if (cols >= 2) s3.set(1, df.format(report.getTotalRevenue()) + " VNĐ");
            data.add(s1);
            data.add(s2);
            data.add(s3);
        }

        util.XlsxExportUtil.writeSingleSheetXlsx(resp.getOutputStream(), "Report", headers, data);
    }

    /**
     * Dịch tiêu đề cột cơ sở dữ liệu sang tiếng Việt thân thiện để xuất báo cáo.
     */
    private String translateHeader(String key) {
        if (key == null) return "";
        return switch (key) {
            case "report_date"         -> "Ngày";
            case "booking_count"       -> "Số giao dịch";
            case "ticket_count"        -> "Số vé bán";
            case "revenue"             -> "Doanh thu (VNĐ)";
            case "branch_id"           -> "Mã chi nhánh";
            case "branch_name"         -> "Tên chi nhánh";
            case "hall_name"           -> "Tên phòng chiếu";
            case "hall_seat_capacity"  -> "Sức chứa ghế";
            case "showtime_count"      -> "Số suất chiếu";
            case "total_capacity"      -> "Tổng chỗ ngồi cung cấp";
            case "booked_seats"        -> "Số ghế đã bán";
            case "occupancy_rate"      -> "Tỷ lệ lấp đầy (%)";
            case "movie_id"            -> "Mã phim";
            case "movie_title"         -> "Tên phim";
            case "booking_hour"        -> "Khung giờ";
            case "customer_id"         -> "Mã KH";
            case "customer_name"       -> "Tên khách hàng";
            case "customer_email"      -> "Email";
            case "total_spent"           -> "Tổng chi tiêu (VNĐ)";
            case "last_booking_at"     -> "Đặt vé gần nhất";
            default                    -> key;
        };
    }
}
