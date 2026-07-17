/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Notification Management —  (Long)
 * Chỉ lịch sử: xác nhận đặt vé, xác nhận thanh toán, hệ thống.
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.NotificationLog;
import service.NotificationService;

import java.io.IOException;
import java.util.List;

/**
 * Servlet quản lý lịch sử thông báo giao dịch.
 * URL: /admin/notifications?action=list
 */
@WebServlet("/admin/notifications")
public class NotificationController extends HttpServlet {

    private final NotificationService notifService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if (action == null || "list".equals(action)) {
            handleList(req, resp);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/admin/notifications?action=list");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        if ("delete".equals(action)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                boolean ok = notifService.deleteNotification(id);
                if (ok) {
                    req.getSession().setAttribute("flashSuccess", "Đã xóa thông báo thành công!");
                }
            } catch (NumberFormatException ignored) {}
        }
        resp.sendRedirect(req.getContextPath() + "/admin/notifications?action=list");
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int page = parsePage(req.getParameter("page"));
        int pageSize = parsePageSize(req.getParameter("pageSize"));

        String keyword = trim(req.getParameter("keyword"));
        String type = trim(req.getParameter("type"));
        String status = trim(req.getParameter("status"));

        var pageResult = notifService.getLogsPaged(keyword, type, status, page, pageSize);

        req.setAttribute("logs", pageResult.getItems());
        req.setAttribute("currentPage", pageResult.getPage());
        req.setAttribute("totalPages", pageResult.getTotalPages());
        req.setAttribute("totalItems", pageResult.getTotalItems());
        req.setAttribute("pageSize", pageResult.getPageSize());

        // for JSP prefill
        req.setAttribute("keyword", keyword);
        req.setAttribute("type", type);
        req.setAttribute("status", status);
        req.getRequestDispatcher("/pages/admin/notification-list.jsp").forward(req, resp);
    }

    private static int parsePage(String s) {
        try {
            int v = s == null ? 1 : Integer.parseInt(s.trim());
            return v < 1 ? 1 : v;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static int parsePageSize(String s) {
        try {
            int v = s == null ? 10 : Integer.parseInt(s.trim());
            return v < 1 ? 10 : v;
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    private static String trim(String s) {
        return s != null ? s.trim() : null;
    }
}
