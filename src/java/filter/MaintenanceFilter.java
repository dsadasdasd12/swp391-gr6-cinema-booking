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
import java.io.IOException;

@WebFilter(filterName = "MaintenanceFilter", urlPatterns = {"/*"})
public class MaintenanceFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        Boolean isMaintenance = (Boolean) req.getServletContext().getAttribute("system.maintenance");

        if (isMaintenance != null && isMaintenance) {
            String path = req.getRequestURI().substring(req.getContextPath().length());

            // Allow admin, login, assets, and the maintenance page itself
            boolean isAllowed = path.startsWith("/admin")
                    || path.startsWith("/login")
                    || path.startsWith("/logout")
                    || path.startsWith("/assets")
                    || path.equals("/maintenance.jsp");
            if (!isAllowed) {
                res.sendRedirect(req.getContextPath() + "/maintenance.jsp");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
