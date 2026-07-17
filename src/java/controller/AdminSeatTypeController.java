package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.SeatType;
import model.User;
import service.SeatTypeService;

@WebServlet(name = "AdminSeatTypeController", urlPatterns = {"/admin/seat-types"})
public class AdminSeatTypeController extends HttpServlet {

    private final SeatTypeService seatTypeService = new SeatTypeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Danh mục loại ghế và hệ số giá là dữ liệu toàn hệ thống, chỉ ADMIN quản lý.
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        // State changes must use POST; a GET link cannot deactivate data.
        request.setAttribute("allSeatTypes", seatTypeService.getAll());
        request.getRequestDispatcher("/pages/admin/seat-types.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Không cho phép thay đổi loại ghế bằng GET để tránh thao tác ngoài ý muốn.
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String action = request.getParameter("action");
        try {
            if ("add".equals(action)) {
                // Service nhận model đã đọc từ form và chịu trách nhiệm validate nghiệp vụ.
                seatTypeService.create(readSeatType(request, 0));
                request.getSession().setAttribute("msgSuccess", "Thêm loại ghế thành công.");
            } else if ("update".equals(action)) {
                seatTypeService.update(readSeatType(request, parsePositiveInt(request.getParameter("id"))));
                request.getSession().setAttribute("msgSuccess", "Cập nhật loại ghế thành công.");
            } else if ("delete".equals(action)) {
                // Không xóa cứng loại ghế để giữ lịch sử giá vé cũ; chỉ khóa sử dụng mới.
                seatTypeService.deactivate(parsePositiveInt(request.getParameter("id")));
                request.getSession().setAttribute("msgSuccess", "Đã khóa loại ghế.");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("msgError", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/admin/seat-types");
    }

    private SeatType readSeatType(HttpServletRequest request, int id) {
        // Controller chỉ chuyển dữ liệu form thành model, không tự kiểm tra quy tắc hệ số giá.
        SeatType value = new SeatType();
        value.setId(id);
        value.setCode(request.getParameter("code"));
        value.setName(request.getParameter("name"));
        value.setColor(request.getParameter("color"));
        value.setStatus(request.getParameter("status"));
        try {
            value.setDefaultPrice(Double.parseDouble(request.getParameter("defaultPrice")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Hệ số nhân giá không hợp lệ.");
        }
        return value;
    }

    private int parsePositiveInt(String value) {
        try {
            int id = Integer.parseInt(value);
            if (id > 0) {
                return id;
            }
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("Loại ghế không hợp lệ.");
    }

    private boolean isAdmin(HttpServletRequest request) {
        // Kiểm tra role trong session trước mọi thao tác đọc/ghi danh mục ghế.
        Object current = request.getSession(false) == null ? null : request.getSession(false).getAttribute("user");
        return current instanceof User && "ADMIN".equalsIgnoreCase(((User) current).getRole());
    }
}
