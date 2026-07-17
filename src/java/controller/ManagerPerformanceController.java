package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Giữ URL cũ hoạt động nhưng luôn đưa manager về dashboard thống nhất. */
@WebServlet("/manager/performance")
public class ManagerPerformanceController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/manager/dashboard#performance");
    }
}
