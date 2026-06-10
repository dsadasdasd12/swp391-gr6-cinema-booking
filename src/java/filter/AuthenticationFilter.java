package filter;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import model.User;
import java.io.IOException;

@WebFilter(urlPatterns = {"/admin/*", "/manager/*", "/staff/*"})
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);
        String ctx = request.getContextPath();
        String uri = request.getRequestURI();

        User user = (session != null)
                ? (User) session.getAttribute("user") : null;

        // Not logged in → redirect to login
        if (user == null) {
            response.sendRedirect(ctx + "/login");
            return;
        }

        if (user.isAdmin() || user.isManager() || user.isStaff()) {
            request.getSession().setAttribute("adminUser", user);
        }

        String role = user.getRole();

        // STAFF truy cập /admin/* hoặc /manager/* → về staff dashboard
        if ("STAFF".equals(role)
            && (uri.contains("/admin/") || uri.contains("/manager/"))) {
            response.sendRedirect(ctx + "/pages/staff/dashboard.jsp");
            return;
        }

        // ADMIN truy cập /manager/* hoặc /staff/* → về admin
        if ("ADMIN".equals(role)
            && (uri.contains("/manager/") || uri.contains("/staff/"))) {
            response.sendRedirect(ctx + "/admin/movies?action=list");
            return;
        }

        // CUSTOMER cố vào admin/manager/staff → về home
        if ("CUSTOMER".equals(role) || "GUEST".equals(role)) {
            response.sendRedirect(ctx + "/home");
            return;
        }

        chain.doFilter(req, res);
    }
}
