package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import model.User;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String ctx = req.getContextPath();
        String uri = req.getRequestURI();

        // Allow static files
        if (uri.startsWith(ctx + "/assets/")
                || uri.startsWith(ctx + "/css/")
                || uri.startsWith(ctx + "/js/")
                || uri.startsWith(ctx + "/images/")
                || uri.endsWith(".css")
                || uri.endsWith(".js")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg")
                || uri.endsWith(".jpeg")
                || uri.endsWith(".gif")
                || uri.endsWith(".ico")) {

            chain.doFilter(request, response);
            return;
        }

        // Allow public pages
        if (uri.equals(ctx + "/")
                || uri.equals(ctx + "/home")
                || uri.equals(ctx + "/login")
                || uri.equals(ctx + "/register")
                || uri.equals(ctx + "/logout")
                || uri.equals(ctx + "/forgot-password")
                || uri.equals(ctx + "/confirm-reset-otp")
                || uri.equals(ctx + "/reset-password")
                || uri.equals(ctx + "/verify-email")
                || uri.equals(ctx + "/resend-otp")
                || uri.equals(ctx + "/movies")
                || uri.startsWith(ctx + "/movies")
                || uri.startsWith(ctx + "/movie")) {

            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        User user = null;
        String role = null;
        if (session != null) {
            user = (User) session.getAttribute("user");
            if (user != null) {
                role = user.getRole();
            }
        }
        // Block direct JSP access for admin pages
        if (uri.startsWith(ctx + "/pages/admin/")) {
            if (!"ADMIN".equals(role)) {
                res.sendRedirect(ctx + "/home");
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        // Block direct JSP access for manager pages
        if (uri.startsWith(ctx + "/pages/manager/")) {
            if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) {
                res.sendRedirect(ctx + "/home");
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        // Block direct JSP access for staff pages
        if (uri.startsWith(ctx + "/pages/staff/")) {
            if (!"STAFF".equals(role)
                    && !"MANAGER".equals(role)
                    && !"ADMIN".equals(role)) {

                res.sendRedirect(ctx + "/home");
                return;
            }

            chain.doFilter(request, response);
            return;
        }
        // ADMIN only
        if (uri.startsWith(ctx + "/admin")) {

            if (!"ADMIN".equals(role)) {
                res.sendRedirect(ctx + "/home");
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        // MANAGER only
        if (uri.startsWith(ctx + "/manager")) {

            if (!"MANAGER".equals(role)
                    && !"ADMIN".equals(role)) {

                res.sendRedirect(ctx + "/home");
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        // STAFF only
        if (uri.startsWith(ctx + "/staff")) {

            if (!"STAFF".equals(role)
                    && !"MANAGER".equals(role)
                    && !"ADMIN".equals(role)) {

                res.sendRedirect(ctx + "/home");
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        if (uri.startsWith(ctx + "/booking") && user == null) {
            res.sendRedirect(ctx + "/login");
            return;
        }

        // Customer pages
        if (uri.startsWith(ctx + "/booking")
                || uri.startsWith(ctx + "/profile")
                || uri.startsWith(ctx + "/tickets")
                || uri.startsWith(ctx + "/review")) {

            if (!"CUSTOMER".equals(role)
                    && !"STAFF".equals(role)
                    && !"MANAGER".equals(role)
                    && !"ADMIN".equals(role)) {

                res.sendRedirect(ctx + "/home");
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
