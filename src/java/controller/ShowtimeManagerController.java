package controller;

import service.ShowtimeService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Showtime;

@WebServlet(name = "ShowtimeManagerController", urlPatterns = {"/ShowtimeManager"})
public class ShowtimeManagerController extends HttpServlet {

    private final ShowtimeService showtimeService = new ShowtimeService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        int branchId = 1; // Mặc định chi nhánh 1 để test nhanh
        String branchIdStr = request.getParameter("branchId");
        if (branchIdStr != null) {
            try {
                branchId = Integer.parseInt(branchIdStr);
            } catch (NumberFormatException e) {
                // Keep default
            }
        }

        String dateStr = request.getParameter("date");
        if (dateStr == null || dateStr.trim().isEmpty()) {
            dateStr = java.time.LocalDate.now().toString(); // Mặc định ngày hôm nay
        }

        String action = request.getParameter("action");
        if ("setPricing".equalsIgnoreCase(action)) {
            try {
                int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
                String seatType = request.getParameter("seatType");
                double price = Double.parseDouble(request.getParameter("price"));
                
                boolean success = showtimeService.setSeatPricing(showtimeId, seatType, price);
                if (success) {
                    request.getSession().setAttribute("msgSuccess", "Đã thiết lập giá vé thành công!");
                } else {
                    request.getSession().setAttribute("msgError", "Thiết lập giá vé thất bại. Vui lòng thử lại!");
                }
            } catch (Exception e) {
                request.getSession().setAttribute("msgError", "Dữ liệu nhập vào không hợp lệ: " + e.getMessage());
            }
            response.sendRedirect("ShowtimeManager?branchId=" + branchId + "&date=" + dateStr);
            return;
        }

        // Đọc danh sách suất chiếu của chi nhánh theo ngày chọn
        List<Showtime> showtimeList = showtimeService.getShowtimesByBranchAndDate(branchId, dateStr);
        
        // Thêm thông tin tỷ lệ lấp đầy trực tiếp cho từng suất chiếu
        request.setAttribute("showtimeList", showtimeList);
        request.setAttribute("showtimeService", showtimeService); // Gửi Service sang để gọi helper methods trên JSP nếu cần
        request.setAttribute("currentBranchId", branchId);
        request.setAttribute("selectedDate", dateStr);
        
        request.getRequestDispatcher("showtimeManager.jsp").forward(request, response);
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
