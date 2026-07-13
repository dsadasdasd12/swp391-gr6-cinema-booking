/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Security Role and Permission Authorization Filter
 */
package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

import java.io.IOException;

/**
 * Filter kiểm tra quyền hạn (Authorization) dựa trên vai trò của người dùng (Role-based access control - RBAC).
 * Phân phối quyền truy cập tới các module:
 * - Admin: Toàn quyền
 * - Branch Manager: Phim (chỉ xem), Báo cáo (chỉ xem chi nhánh mình), Tài khoản khách hàng.
 */
@WebFilter("/admin/*")
public class RolePermissionFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        // Nếu không có session hoặc chưa đăng nhập => chuyển hướng về trang đăng nhập
        if (session == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            user = (User) session.getAttribute("adminUser");
        }
        if (user == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        String uri = httpRequest.getRequestURI();
        String type = httpRequest.getParameter("type");
        String action = httpRequest.getParameter("action");

        // Bỏ qua tài nguyên tĩnh
        if (uri.contains("/assets/") || uri.contains("/css/") || uri.contains("/js/")) {
            chain.doFilter(request, response);
            return;
        }

        // ── CHẶN CÁC MODULE THEO VAI TRÒ (Authorization checks) ──
        if (user.isManager()) {

            // 1. Chỉ Admin được xóa phim vĩnh viễn
            if (uri.contains("/admin/movies") && "delete".equals(action)) {
                forwardTo403(httpRequest, httpResponse);
                return;
            }

            // 2. Manager không xem báo cáo tổng hệ thống (dashboard system)
            if (uri.contains("/admin/reports/system")) {
                forwardTo403(httpRequest, httpResponse);
                return;
            }

            // 3. Chặn quản lý Tài khoản nhân sự (staff) và Phân quyền (roles)
            if (uri.contains("/admin/accounts/staff") || uri.contains("/admin/accounts/roles")) {
                forwardTo403(httpRequest, httpResponse);
                return;
            }
            
            // 4. Chặn các cài đặt hệ thống cấp cao
            if (uri.contains("/admin/settings")) {
                forwardTo403(httpRequest, httpResponse);
                return;
            }
        } 
        
        // Nếu là vai trò khác (ví dụ: nhân viên thường) bị giới hạn hơn nữa
        else if (!user.isAdmin()) {
            // Nhân viên thường không có quyền admin nào cả
            if (uri.contains("/admin/")) {
                forwardTo403(httpRequest, httpResponse);
                return;
            }
        }

        // Đạt yêu cầu phân quyền, chuyển tiếp request
        chain.doFilter(request, response);
    }

    private void forwardTo403(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        req.getRequestDispatcher("/pages/error/403.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {}
}
