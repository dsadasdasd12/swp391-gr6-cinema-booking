package controller;

import service.ShowtimeService;
import service.UserService;
import dao.BranchDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Showtime;
import model.User;
import model.Branch;

@WebServlet(name = "ShowtimeManagerController", urlPatterns = {"/ShowtimeManager"})
public class ShowtimeManagerController extends HttpServlet {

    private final ShowtimeService showtimeService = new ShowtimeService();
    private final UserService userService = new UserService();
    private final BranchDAO branchDAO = new BranchDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

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

        int branchId = 1;
        if ("ADMIN".equalsIgnoreCase(role)) {
            List<Branch> branchList = branchDAO.getAllBranches();
            request.setAttribute("branchList", branchList);
            if (branchList != null && !branchList.isEmpty()) {
                branchId = branchList.get(0).getId();
            }
            String branchIdStr = request.getParameter("branchId");
            if (branchIdStr != null) {
                try {
                    branchId = Integer.parseInt(branchIdStr);
                } catch (NumberFormatException e) {
                    // Keep default
                }
            }
        } else {
            // MANAGER role is locked to their assigned branch from database assignment
            branchId = userService.getBranchIdOfStaff(user.getId());
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

        Branch currentBranch = branchDAO.getBranchById(branchId);
        String currentBranchName = (currentBranch != null) ? currentBranch.getName() : "Chi nhánh " + branchId;
        request.setAttribute("currentBranchName", currentBranchName);

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
