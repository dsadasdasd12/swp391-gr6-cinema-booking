package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Đặt UTF-8 cho mọi request/response — tránh lỗi font tiếng Việt trên JSP.
 */
@WebFilter(urlPatterns = "/*")
public class Utf8EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpReq) {
            httpReq.setCharacterEncoding("UTF-8");
            if (response instanceof HttpServletResponse httpResp) {
                httpResp.setCharacterEncoding("UTF-8");
                String uri = httpReq.getRequestURI();
                if (uri != null) {
                    if (uri.endsWith(".js")) {
                        httpResp.setContentType("application/javascript;charset=UTF-8");
                    } else if (uri.endsWith(".css")) {
                        httpResp.setContentType("text/css;charset=UTF-8");
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }
}
