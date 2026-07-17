/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Reporting & Analytics —  (Long)
 */
package dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO chứa kết quả báo cáo và thống kê dùng cho hiển thị bảng biểu và vẽ biểu
 * đồ (Chart.js).
 *
 * @author LONG
 */
public class ReportDTO {

    private String reportType; // REVENUE | SALES | OCCUPANCY | POPULAR | PEAK
    private String title;
    private String fromDate;
    private String toDate;

    // Chứa dữ liệu dạng bảng (mỗi map đại diện cho một hàng: tên_cột -> giá_trị)
    private List<Map<String, Object>> rows = new ArrayList<>();

    // Các nhãn trục X dùng cho Chart.js (ví dụ: các ngày, các chi nhánh, tên phim)
    private List<String> labels = new ArrayList<>();

    // Dữ liệu tương ứng trên trục Y dùng cho Chart.js (ví dụ: doanh thu, số lượng vé)
    private List<Number> data = new ArrayList<>();

    // Các giá trị tổng hợp để hiển thị trong KPI card
    private double totalRevenue;
    private int totalTickets;
    private int totalBookings;
    private double averageOccupancy; // Dành riêng cho báo cáo tỉ lệ lấp đầy

    // ── Constructors ─────────────────────────────────────────
    public ReportDTO() {
    }

    public ReportDTO(String reportType, String title, String fromDate, String toDate) {
        this.reportType = reportType;
        this.title = title;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    // ── Getters & Setters ────────────────────────────────────
    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Number> getData() {
        return data;
    }

    public void setData(List<Number> data) {
        this.data = data;
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

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public double getAverageOccupancy() {
        return averageOccupancy;
    }

    public void setAverageOccupancy(double averageOccupancy) {
        this.averageOccupancy = averageOccupancy;
    }

    // Helper tiện ích để lấy dữ liệu dạng chuỗi JSON thô cho Chart.js
    public String getLabelsJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < labels.size(); i++) {
            sb.append("\"").append(labels.get(i).replace("\"", "\\\"")).append("\"");
            if (i < labels.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public String getDataJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < data.size(); i++) {
            sb.append(data.get(i));
            if (i < data.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
