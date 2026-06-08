package controller;

import service.DiscountService;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.DiscountCode;

@WebServlet(name = "DiscountManagerController", urlPatterns = {"/DiscountManager"})
public class DiscountManagerController extends HttpServlet {

    private final DiscountService discountService = new DiscountService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String action = request.getParameter("action");

        if ("create".equalsIgnoreCase(action)) {
            try {
                String code = request.getParameter("code");
                String discountType = request.getParameter("discountType");
                double discountValue = Double.parseDouble(request.getParameter("discountValue"));
                
                String maxDiscountStr = request.getParameter("maxDiscountAmount");
                Double maxDiscountAmount = null;
                if (maxDiscountStr != null && !maxDiscountStr.trim().isEmpty()) {
                    maxDiscountAmount = Double.parseDouble(maxDiscountStr.trim());
                }

                double minOrderValue = Double.parseDouble(request.getParameter("minOrderValue"));
                int maxUses = Integer.parseInt(request.getParameter("maxUses"));

                // Phân tích định dạng thời gian từ HTML datetime-local (yyyy-MM-dd'T'HH:mm)
                String startStr = request.getParameter("startDate");
                String endStr = request.getParameter("endDate");
                
                // Thay thế ký tự 'T' bằng dấu cách để dễ parse thành Timestamp
                startStr = startStr.replace("T", " ") + ":00";
                endStr = endStr.replace("T", " ") + ":00";
                
                Timestamp startDate = Timestamp.valueOf(startStr);
                Timestamp endDate = Timestamp.valueOf(endStr);
                
                String status = request.getParameter("status");
                if (status == null || status.trim().isEmpty()) status = "ACTIVE";

                DiscountCode dc = new DiscountCode(0, code, discountType, discountValue, 
                                                   maxDiscountAmount, minOrderValue, maxUses, 
                                                   0, startDate, endDate, status);
                
                boolean success = discountService.createDiscountCode(dc);
                if (success) {
                    request.getSession().setAttribute("msgSuccess", "Tạo mã giảm giá mới thành công!");
                } else {
                    request.getSession().setAttribute("msgError", "Tạo mã thất bại. Tên mã có thể đã bị trùng!");
                }
            } catch (Exception e) {
                request.getSession().setAttribute("msgError", "Lỗi dữ liệu đầu vào: " + e.getMessage());
            }
            response.sendRedirect("DiscountManager");
            return;
        }

        if ("updateStatus".equalsIgnoreCase(action)) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                String status = request.getParameter("status");
                boolean success = discountService.updateDiscountCodeStatus(id, status);
                if (success) {
                    request.getSession().setAttribute("msgSuccess", "Cập nhật trạng thái thành công!");
                } else {
                    request.getSession().setAttribute("msgError", "Cập nhật trạng thái thất bại.");
                }
            } catch (Exception e) {
                request.getSession().setAttribute("msgError", "Lỗi: " + e.getMessage());
            }
            response.sendRedirect("DiscountManager");
            return;
        }

        if ("delete".equalsIgnoreCase(action)) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                boolean success = discountService.deleteDiscountCode(id);
                if (success) {
                    request.getSession().setAttribute("msgSuccess", "Xóa mã giảm giá thành công!");
                } else {
                    request.getSession().setAttribute("msgError", "Xóa mã thất bại. Có thể mã này đã có dữ liệu sử dụng lịch sử.");
                }
            } catch (Exception e) {
                request.getSession().setAttribute("msgError", "Lỗi xóa mã: " + e.getMessage());
            }
            response.sendRedirect("DiscountManager");
            return;
        }

        // Đọc danh sách mã giảm giá và hiển thị giao diện chính
        List<DiscountCode> discountList = discountService.getAllDiscountCodes();
        request.setAttribute("discountList", discountList);
        request.getRequestDispatcher("discountManager.jsp").forward(request, response);
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
