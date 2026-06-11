/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem suất chiếu (View showtimes)
 */
package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dto.MovieShowtimes;
import model.Branch;
import service.CinemaService;

/**
 * Hiển thị lịch chiếu (suất chiếu) của một chi nhánh trong một ngày, nhóm theo
 * phim. Người dùng chọn chi nhánh và ngày; nếu thiếu, mặc định lấy chi nhánh
 * đầu tiên và ngày hôm nay.
 * <p>
 * URL: {@code /showtimes?branchId=<id>&date=<yyyy-MM-dd>} (GET).
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
@WebServlet(name = "ShowtimeListController", urlPatterns = {"/showtimes"})
public class ShowtimeListController extends HttpServlet {

    /** Định dạng ngày trao đổi với view (khớp <input type="date">). */
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final CinemaService cinemaService = new CinemaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1) Danh sách chi nhánh để đổ vào ô chọn
        List<Branch> branches = cinemaService.getActiveBranches();

        // 2) Xác định chi nhánh đang chọn: ưu tiên tham số, nếu không thì lấy
        //    chi nhánh đầu tiên trong danh sách (nếu có)
        int branchId = parseBranchId(request.getParameter("branchId"), branches);

        // 3) Xác định ngày đang xem: tham số hợp lệ hoặc mặc định hôm nay
        LocalDate date = parseDate(request.getParameter("date"));

        // 4) Lấy suất chiếu đã gom nhóm theo phim từ tầng nghiệp vụ
        List<MovieShowtimes> movieShowtimes = cinemaService.getShowtimesByMovie(branchId, date);

        // 5) Đẩy dữ liệu sang view (giữ lại lựa chọn để hiển thị lại trong form)
        request.setAttribute("branches", branches);
        request.setAttribute("selectedBranchId", branchId);
        request.setAttribute("selectedDate", date.format(ISO_DATE));
        request.setAttribute("movieShowtimes", movieShowtimes);
        request.getRequestDispatcher("/pages/showtime/list.jsp").forward(request, response);
    }

    /**
     * Đọc branchId từ tham số; nếu thiếu/không hợp lệ thì trả về id chi nhánh
     * đầu tiên trong danh sách, hoặc -1 khi không có chi nhánh nào.
     */
    private static int parseBranchId(String raw, List<Branch> branches) {
        if (raw != null && !raw.isBlank()) {
            try {
                return Integer.parseInt(raw.trim());
            } catch (NumberFormatException ignored) {
                // rơi xuống dùng mặc định
            }
        }
        return branches.isEmpty() ? -1 : branches.get(0).getId();
    }

    /** Đọc ngày dạng yyyy-MM-dd; trả về hôm nay nếu thiếu hoặc sai định dạng. */
    private static LocalDate parseDate(String raw) {
        if (raw != null && !raw.isBlank()) {
            try {
                return LocalDate.parse(raw.trim(), ISO_DATE);
            } catch (DateTimeParseException ignored) {
                // rơi xuống dùng hôm nay
            }
        }
        return LocalDate.now();
    }
}
