/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem tình trạng ghế (View seat availability)
 */
package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dto.SeatMap;
import service.CinemaService;

/**
 * Hiển thị sơ đồ ghế của một suất chiếu: trạng thái từng ghế (còn trống / đã
 * đặt / bảo trì) cùng số ghế còn trống.
 * <p>
 * URL: {@code /seats?showtimeId=<id>} (GET).
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
@WebServlet(name = "SeatAvailabilityController", urlPatterns = {"/seats"})
public class SeatAvailabilityController extends HttpServlet {

    private final CinemaService cinemaService = new CinemaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1) Đọc id suất chiếu từ tham số
        int showtimeId = parseId(request.getParameter("showtimeId"));

        // 2) Lấy sơ đồ ghế từ tầng nghiệp vụ
        SeatMap seatMap = cinemaService.getSeatMap(showtimeId);

        if (seatMap == null) {
            // suất chiếu không hợp lệ / không tồn tại -> báo 404 thân thiện
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("notFound", Boolean.TRUE);
            request.getRequestDispatcher("/pages/seat/availability.jsp").forward(request, response);
            return;
        }

        // 3) Đẩy dữ liệu sang view
        request.setAttribute("seatMap", seatMap);
        dao.SeatTypeDAO seatTypeDAO = new dao.SeatTypeDAO();
        request.setAttribute("allSeatTypes", seatTypeDAO.findAll());
        request.getRequestDispatcher("/pages/seat/availability.jsp").forward(request, response);
    }

    /** Đọc id từ tham số request, trả về -1 nếu thiếu hoặc sai định dạng. */
    private static int parseId(String s) {
        if (s == null || s.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
