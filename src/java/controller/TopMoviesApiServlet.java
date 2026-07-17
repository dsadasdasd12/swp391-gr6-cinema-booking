/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Reporting & Analytics — API Top Movies (Long)
 */
package controller;

import dao.ReportDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST API trả về Top 5 phim có doanh thu cao nhất.
 * URL: /api/top-movies
 * Params: fromDate (yyyy-MM-dd), toDate (yyyy-MM-dd) — mặc định 30 ngày gần nhất.
 * Response: JSON Array.
 *
 * @author LONG
 */
@WebServlet("/api/top-movies")
public class TopMoviesApiServlet extends HttpServlet {

    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // 1. Đọc param ngày, mặc định 30 ngày gần nhất
        String fromDate = req.getParameter("fromDate");
        String toDate = req.getParameter("toDate");
        if (fromDate == null || fromDate.isBlank()) {
            fromDate = LocalDate.now().minusDays(30).toString();
        }
        if (toDate == null || toDate.isBlank()) {
            toDate = LocalDate.now().toString();
        }

        // 2. Gọi DAO đã có sẵn (getPopularMovies sắp xếp theo ticket_count DESC, revenue DESC)
        List<Map<String, Object>> allMovies = reportDAO.getPopularMovies(fromDate, toDate);

        // 3. Cắt lấy Top 5
        List<Map<String, Object>> top5 = allMovies.stream().limit(5).toList();

        // 4. Trả JSON
        resp.setContentType("application/json; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        PrintWriter out = resp.getWriter();

        out.print("[");
        for (int i = 0; i < top5.size(); i++) {
            Map<String, Object> row = top5.get(i);
            out.print("{");
            out.print("\"rank\":" + (i + 1) + ",");
            out.print("\"movie_id\":" + row.get("movie_id") + ",");
            out.print("\"movie_title\":\"" + escapeJson(String.valueOf(row.get("movie_title"))) + "\",");
            out.print("\"poster_url\":\"" + escapeJson(String.valueOf(row.getOrDefault("poster_url", ""))) + "\",");
            out.print("\"ticket_count\":" + row.get("ticket_count") + ",");
            out.print("\"revenue\":" + row.get("revenue"));
            out.print("}");
            if (i < top5.size() - 1) out.print(",");
        }
        out.print("]");
    }

    /**
     * Escape ký tự đặc biệt trong chuỗi JSON (tránh XSS / broken JSON).
     */
    private String escapeJson(String value) {
        if (value == null || "null".equals(value)) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
