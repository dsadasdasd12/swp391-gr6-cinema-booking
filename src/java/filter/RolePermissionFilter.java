package filter;

import dao.RoleDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

import java.io.IOException;

/**
 * Filter kiểm tra quyền hạn (Authorization) dựa trên ma trận phân quyền trong Database.
 * Đọc quyền từ bảng ROLE_PERMISSIONS thông qua RoleDAO.
 */
@WebFilter("/admin/*")
public class RolePermissionFilter implements Filter {

    private final RoleDAO roleDAO = new RoleDAO();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        // 1. Kiểm tra đăng nhập
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
        String action = httpRequest.getParameter("action");
        if (action == null) action = "list"; // default is view/list

        // Bỏ qua tài nguyên tĩnh
        if (uri.contains("/assets/") || uri.contains("/css/") || uri.contains("/js/")) {
            chain.doFilter(request, response);
            return;
        }

        // Admin luôn có full quyền - hoặc để an toàn thì query bảng DB
        String roleName = user.getRole(); // 'ADMIN', 'MANAGER', 'STAFF' v.v..
        if ("ADMIN".equalsIgnoreCase(roleName)) {
            // Có thể bỏ qua check DB cho ADMIN để tối ưu tốc độ, nhưng ta cứ check cho đồng bộ
        }

        // 2. Map URI tới Module Key
        String moduleKey = getModuleKeyFromUri(uri);
        
        // Nếu không thuộc module nào quản lý (ví dụ Dashboard), thì cho qua
        if (moduleKey == null) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Map HTTP request tới Action trong ma trận (view, create, edit, delete, export, manage)
        String permAction = mapActionToPermission(httpRequest.getMethod(), action, uri);

        // 4. Query DB kiểm tra quyền
        boolean allowed = roleDAO.hasPermission(roleName, moduleKey, permAction);

        if (!allowed) {
            forwardTo403(httpRequest, httpResponse);
            return;
        }

        chain.doFilter(request, response);
    }

    private String getModuleKeyFromUri(String uri) {
        if (uri.contains("/admin/movies")) return "movies";
        if (uri.contains("/admin/showtimes")) return "showtimes";
        if (uri.contains("/admin/tickets")) return "tickets";
        if (uri.contains("/admin/reports")) return "reports";
        if (uri.contains("/admin/accounts")) return "accounts";
        if (uri.contains("/admin/settings")) return "settings";
        return null; // Các trang khác (dashboard) không kiểm tra qua module
    }

    private String mapActionToPermission(String method, String paramAction, String uri) {
        // Mặc định GET = view, POST = create/edit/delete
        if ("delete".equalsIgnoreCase(paramAction) || "delete-role".equalsIgnoreCase(paramAction)) {
            return "delete";
        }
        if ("edit".equalsIgnoreCase(paramAction) || "update".equalsIgnoreCase(paramAction) || "update-permissions".equalsIgnoreCase(paramAction) || "update-role-info".equalsIgnoreCase(paramAction)) {
            return "edit";
        }
        if ("add".equalsIgnoreCase(paramAction) || "create".equalsIgnoreCase(paramAction) || "add-role".equalsIgnoreCase(paramAction)) {
            return "create";
        }
        if ("export".equalsIgnoreCase(paramAction)) {
            return "export";
        }
        if ("POST".equalsIgnoreCase(method)) {
            // Các thao tác POST khác (như toggle status, use ticket) => quy vào edit/manage
            return "edit";
        }
        return "view"; // Mặc định GET list/detail là view
    }

    private void forwardTo403(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        req.getRequestDispatcher("/pages/error/403.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {}
}
