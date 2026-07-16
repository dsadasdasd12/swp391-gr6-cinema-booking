package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;
import service.DiscountService;

@WebServlet(name = "DiscountManagerController", urlPatterns = {"/DiscountManager"})
public class DiscountManagerController extends HttpServlet {

    private final DiscountService discountService = new DiscountService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Mã giảm giá ảnh hưởng doanh thu toàn hệ thống nên chỉ ADMIN được xem/quản lý.
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.setAttribute("discountList", discountService.getAllDiscountCodes());
        request.getRequestDispatcher("discountManager.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Tất cả thao tác thay đổi voucher đều bắt buộc kiểm tra ADMIN một lần nữa ở server.
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String action = request.getParameter("action");
        try {
            boolean changed;
            if ("create".equalsIgnoreCase(action)) {
                // Controller chỉ lấy dữ liệu form; DiscountService xác thực ngày, giá trị và giới hạn dùng.
                changed = discountService.createDiscountCode(request.getParameter("code"), request.getParameter("discountType"),
                        request.getParameter("discountValue"), request.getParameter("maxDiscountAmount"),
                        request.getParameter("minOrderValue"), request.getParameter("maxUses"),
                        request.getParameter("startDate"), request.getParameter("endDate"), request.getParameter("status"));
            } else if ("updateStatus".equalsIgnoreCase(action)) {
                changed = discountService.updateDiscountCodeStatus(parseId(request.getParameter("id")), request.getParameter("status"));
            } else if ("delete".equalsIgnoreCase(action)) {
                changed = discountService.deleteDiscountCode(parseId(request.getParameter("id")));
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            request.getSession().setAttribute(changed ? "msgSuccess" : "msgError", changed ? "Thao tác mã giảm giá thành công." : "Không thể thay đổi mã giảm giá.");
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("msgError", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/DiscountManager");
    }

    private int parseId(String value) {
        // Không truyền id không hợp lệ xuống service/DAO.
        try {
            int id = Integer.parseInt(value);
            if (id > 0) {
                return id;
            }
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("Mã giảm giá không hợp lệ.");
    }

    private boolean isAdmin(HttpServletRequest request) {
        // Không dựa vào nút ẩn trên JSP; role phải được kiểm tra từ session tại server.
        HttpSession session = request.getSession(false);
        Object current = session == null ? null : session.getAttribute("user");
        return current instanceof User && "ADMIN".equalsIgnoreCase(((User) current).getRole());
    }
}
