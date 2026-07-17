/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Reporting & Analytics —  (Long)
 */
package service;

import dao.ReportDAO;
import dto.ReportDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Lớp nghiệp vụ quản lý báo cáo thống kê cho admin. Xử lý khoảng thời gian mặc
 * định (30 ngày gần nhất) và tổng hợp dữ liệu KPI / Chart.js.
 *
 * @author LONG
 */
public class ReportService {

    private final ReportDAO reportDAO = new ReportDAO();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ── 1. BÁO CÁO DOANH THU ──────────────────────────────────
    public ReportDTO buildRevenueReport(String fromStr, String toStr) {
        String[] dates = resolveDateRange(fromStr, toStr);
        String from = dates[0];
        String to = dates[1];

        ReportDTO report = new ReportDTO("REVENUE", "Báo cáo doanh thu theo ngày", from, to);
        List<Map<String, Object>> rows = reportDAO.getRevenueByDate(from, to);
        report.setRows(rows);

        double totalRevenue = 0;
        int totalTickets = 0;
        int totalBookings = 0;

        for (Map<String, Object> row : rows) {
            // Tính KPI tổng
            Number rev = (Number) row.get("revenue");
            Number tkt = (Number) row.get("ticket_count");
            Number bkg = (Number) row.get("booking_count");

            if (rev != null) {
                totalRevenue += rev.doubleValue();
            }
            if (tkt != null) {
                totalTickets += tkt.intValue();
            }
            if (bkg != null) {
                totalBookings += bkg.intValue();
            }

            // Đưa dữ liệu vào Chart.js
            Object dateObj = row.get("report_date");
            String dateLabel = (dateObj != null) ? dateObj.toString() : "";
            report.getLabels().add(dateLabel);
            report.getData().add(rev != null ? rev.doubleValue() : 0.0);
        }

        report.setTotalRevenue(totalRevenue);
        report.setTotalTickets(totalTickets);
        report.setTotalBookings(totalBookings);

        return report;
    }

    // ── 2. BÁO CÁO DOANH SỐ CHI NHÁNH ──────────────────────────
    public ReportDTO buildSalesReport(String fromStr, String toStr) {
        String[] dates = resolveDateRange(fromStr, toStr);
        String from = dates[0];
        String to = dates[1];

        ReportDTO report = new ReportDTO("SALES", "Báo cáo doanh số theo chi nhánh", from, to);
        List<Map<String, Object>> rows = reportDAO.getSalesByBranch(from, to);
        report.setRows(rows);

        double totalRevenue = 0;
        int totalTickets = 0;
        int totalBookings = 0;

        for (Map<String, Object> row : rows) {
            Number rev = (Number) row.get("revenue");
            Number tkt = (Number) row.get("ticket_count");
            Number bkg = (Number) row.get("booking_count");

            if (rev != null) {
                totalRevenue += rev.doubleValue();
            }
            if (tkt != null) {
                totalTickets += tkt.intValue();
            }
            if (bkg != null) {
                totalBookings += bkg.intValue();
            }

            String branchLabel = (String) row.get("branch_name");
            report.getLabels().add(branchLabel != null ? branchLabel : "Không tên");
            report.getData().add(rev != null ? rev.doubleValue() : 0.0);
        }

        report.setTotalRevenue(totalRevenue);
        report.setTotalTickets(totalTickets);
        report.setTotalBookings(totalBookings);

        return report;
    }

    // ── 3. BÁO CÁO TỈ LỆ LẤP ĐẦY ───────────────────────────────
    public ReportDTO buildOccupancyReport() {
        ReportDTO report = new ReportDTO("OCCUPANCY", "Báo cáo tỉ lệ lấp đầy phòng chiếu", null, null);
        List<Map<String, Object>> rows = reportDAO.getOccupancyRate();
        report.setRows(rows);

        double totalRateSum = 0;
        int activeRooms = 0;

        for (Map<String, Object> row : rows) {
            Number rateObj = (Number) row.get("occupancy_rate");
            double rate = (rateObj != null) ? rateObj.doubleValue() : 0.0;
            totalRateSum += rate;
            activeRooms++;

            String label = row.get("branch_name") + " - " + row.get("hall_name");
            report.getLabels().add(label);
            report.getData().add(rate);
        }

        double avgOccupancy = activeRooms > 0 ? (totalRateSum / activeRooms) : 0.0;
        // Tròn 2 chữ số thập phân
        report.setAverageOccupancy(Math.round(avgOccupancy * 100.0) / 100.0);

        return report;
    }

    // ── 4. BÁO CÁO PHIM ĂN KHÁCH ───────────────────────────────
    public ReportDTO buildPopularMoviesReport(String fromStr, String toStr) {
        String[] dates = resolveDateRange(fromStr, toStr);
        String from = dates[0];
        String to = dates[1];

        ReportDTO report = new ReportDTO("POPULAR", "Báo cáo phim ăn khách nhất", from, to);
        List<Map<String, Object>> rows = reportDAO.getPopularMovies(from, to);
        report.setRows(rows);

        double totalRevenue = 0;
        int totalTickets = 0;

        int count = 0;
        for (Map<String, Object> row : rows) {
            Number rev = (Number) row.get("revenue");
            Number tkt = (Number) row.get("ticket_count");

            if (rev != null) {
                totalRevenue += rev.doubleValue();
            }
            if (tkt != null) {
                totalTickets += tkt.intValue();
            }

            if (count < 5) {
                String title = (String) row.get("movie_title");
                report.getLabels().add(title != null ? title : "Không tên");
                report.getData().add(tkt != null ? tkt.intValue() : 0);
                count++;
            }
        }

        report.setTotalRevenue(totalRevenue);
        report.setTotalTickets(totalTickets);

        return report;
    }

    // ── 5. BÁO CÁO HOẠT ĐỘNG KHÁCH HÀNG ───────────────────────
    public ReportDTO buildCustomerActivityReport(String fromStr, String toStr) {
        String[] dates = resolveDateRange(fromStr, toStr);
        String from = dates[0];
        String to = dates[1];

        ReportDTO report = new ReportDTO("ACTIVITY", "Báo cáo hoạt động khách hàng", from, to);
        List<Map<String, Object>> rows = reportDAO.getCustomerActivity(from, to);
        report.setRows(rows);

        double totalRevenue = 0;
        int totalTickets = 0;
        int totalBookings = 0;

        for (Map<String, Object> row : rows) {
            Number rev = (Number) row.get("total_spent");
            Number tkt = (Number) row.get("ticket_count");
            Number bkg = (Number) row.get("booking_count");

            if (rev != null) {
                totalRevenue += rev.doubleValue();
            }
            if (tkt != null) {
                totalTickets += tkt.intValue();
            }
            if (bkg != null) {
                totalBookings += bkg.intValue();
            }

            String name = (String) row.get("customer_name");
            report.getLabels().add(name != null ? name : "Khách");
            report.getData().add(tkt != null ? tkt.intValue() : 0);
        }

        report.setTotalRevenue(totalRevenue);
        report.setTotalTickets(totalTickets);
        report.setTotalBookings(totalBookings);
        return report;
    }

    // ── 6. BÁO CÁO GIỜ CAO ĐIỂM (PEAK HOURS) ───────────────────
    public ReportDTO buildPeakHoursReport(String fromStr, String toStr) {
        String[] dates = resolveDateRange(fromStr, toStr);
        String from = dates[0];
        String to = dates[1];

        ReportDTO report = new ReportDTO("PEAK", "Báo cáo giờ đặt vé cao điểm", from, to);
        List<Map<String, Object>> rows = reportDAO.getPeakHours(from, to);
        report.setRows(rows);

        double totalRevenue = 0;
        int totalTickets = 0;
        int totalBookings = 0;

        // Điền trước 24 giờ của ngày để biểu đồ đều đặn
        double[] hourRevenue = new double[24];
        int[] hourBookings = new int[24];
        int[] hourTickets = new int[24];

        for (Map<String, Object> row : rows) {
            Number hr = (Number) row.get("booking_hour");
            Number rev = (Number) row.get("revenue");
            Number tkt = (Number) row.get("ticket_count");
            Number bkg = (Number) row.get("booking_count");

            if (hr != null) {
                int h = hr.intValue();
                if (h >= 0 && h < 24) {
                    hourRevenue[h] = rev != null ? rev.doubleValue() : 0.0;
                    hourTickets[h] = tkt != null ? tkt.intValue() : 0;
                    hourBookings[h] = bkg != null ? bkg.intValue() : 0;
                }
            }

            if (rev != null) {
                totalRevenue += rev.doubleValue();
            }
            if (tkt != null) {
                totalTickets += tkt.intValue();
            }
            if (bkg != null) {
                totalBookings += bkg.intValue();
            }
        }

        for (int h = 0; h < 24; h++) {
            report.getLabels().add(String.format("%02d:00", h));
            report.getData().add(hourBookings[h]); // Sử dụng số lượng giao dịch làm thước đo trục Y
        }

        report.setTotalRevenue(totalRevenue);
        report.setTotalTickets(totalTickets);
        report.setTotalBookings(totalBookings);

        return report;
    }

    // ── Helpers ───────────────────────────────────────────────
    /**
     * Tự động giải quyết khoảng thời gian. Mặc định: 30 ngày qua (từ 30 ngày
     * trước đến ngày hôm nay).
     */
    private String[] resolveDateRange(String fromStr, String toStr) {
        String from = fromStr;
        String to = toStr;
        LocalDate today = LocalDate.now();

        if (to == null || to.isBlank()) {
            to = today.format(DATE_FORMAT);
        }
        if (from == null || from.isBlank()) {
            from = today.minusDays(30).format(DATE_FORMAT);
        }
        return new String[]{from, to};
    }
}
