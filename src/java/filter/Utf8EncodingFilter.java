package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Đặt UTF-8 cho mọi request/response để tránh lỗi font tiếng Việt trên JSP.
 */
@WebFilter(urlPatterns = "/*")
public class Utf8EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            HttpServletResponse httpResp = (HttpServletResponse) response;
            String uri = httpReq.getRequestURI();

            if (uri != null && uri.endsWith(".js")) {
                httpResp.setContentType("application/javascript;charset=UTF-8");
            } else if (uri != null && uri.endsWith(".css")) {
                httpResp.setContentType("text/css;charset=UTF-8");
            } else {
                httpResp.setContentType("text/html;charset=UTF-8");
            }
        }

        chain.doFilter(request, response);
    }
}
