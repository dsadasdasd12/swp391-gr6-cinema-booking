/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: E-Ticket & QR Code —  (Long)
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Ticket;
import service.TicketService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet quản lý e-ticket cho admin. URL:
 * /admin/tickets?action=list|detail|use|retry
 *
 * @author LONG
 */
@WebServlet("/admin/tickets")
public class TicketController extends HttpServlet {

    private final TicketService ticketService = new TicketService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "list" ->
                handleList(req, resp);
            case "detail" ->
                handleDetail(req, resp);
            default ->
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "use" ->
                handleUse(req, resp);
            case "retry" ->
                handleRetry(req, resp);
            case "issue" ->
                handleIssue(req, resp);
            default ->
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // ── GET ───────────────────────────────────────────────────
    /**
     * Danh sách tất cả ticket, có thể lọc theo trạng thái.
     */
    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int page = parsePage(req.getParameter("page"));
        int pageSize = parsePageSize(req.getParameter("pageSize"));
        String keyword = trim(req.getParameter("keyword"));
        String status = trim(req.getParameter("status"));

        var pageResult = ticketService.getTicketsPaged(keyword, status, page, pageSize);
        req.setAttribute("tickets", pageResult.getItems());
        req.setAttribute("currentPage", pageResult.getPage());
        req.setAttribute("totalPages", pageResult.getTotalPages());
        req.setAttribute("totalItems", pageResult.getTotalItems());
        req.setAttribute("pageSize", pageResult.getPageSize());

        // for JSP EL convenience
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.getRequestDispatcher("/pages/admin/ticket-list.jsp").forward(req, resp);
    }

    /**
     * Chi tiết một ticket (hiển thị QR để admin kiểm tra / in lại).
     */
    private void handleDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idStr = req.getParameter("bookingId");
        if (idStr == null) {
            idStr = req.getParameter("id");
        }
        int bookingId = parseId(idStr);
        if (bookingId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/tickets?action=list");
            return;
        }
        Ticket ticket = ticketService.getTicketByBookingId(bookingId);
        if (ticket == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("ticket", ticket);
        req.getRequestDispatcher("/pages/shared/ticket-detail.jsp").forward(req, resp);
    }

    // ── POST ──────────────────────────────────────────────────
    /**
     * Đánh dấu ticket đã sử dụng. Request: POST ?action=use&bookingId=123
     * Response: {"success":true,"message":"..."}
     */
    private void handleUse(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String idStr = req.getParameter("bookingId");
        if (idStr == null) {
            idStr = req.getParameter("id");
        }
        int bookingId = parseId(idStr);
        if (bookingId <= 0) {
            out.print("{\"success\":false,\"message\":\"Booking ID không hợp lệ.\"}");
            return;
        }
        boolean ok = ticketService.useTicket(bookingId);
        if (ok) {
            out.print("{\"success\":true,\"message\":\"Vé đã được check-in thành công.\"}");
        } else {
            out.print("{\"success\":false,\"message\":\"Vé không tồn tại hoặc đã được sử dụng.\"}");
        }
    }

    /**
     * Admin retry sinh QR cho ticket PENDING_MANUAL. Request: POST
     * ?action=retry&bookingId=123 Response: JSON
     */
    /**
     * Phát hành e-ticket sau thanh toán (quầy / xác nhận thủ công). POST
     * ?action=issue&bookingId=123
     */
    private void handleIssue(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        // Lấy thông tin bookingId từ request
        int bookingId = parseId(req.getParameter("bookingId"));
        if (bookingId <= 0) {
            out.print("{\"success\":false,\"message\":\"Booking ID không hợp lệ.\"}");
            return;
        }

        // Gọi service để cập nhật trạng thái thanh toán và sinh mã QR
        // Hàm này sẽ tự động sinh QR, lưu file, và gửi email xác nhận cho khách hàng
        Ticket ticket = ticketService.issueTicketAfterPayment(bookingId, getServletContext());
        if (ticket != null && ticket.getQrCodeBase64() != null) {
            out.print("{\"success\":true,\"message\":\"Đã phát hành e-ticket và gửi email xác nhận.\"}");
        } else if (ticket != null) {
            out.print("{\"success\":true,\"message\":\"Đã xác nhận thanh toán. QR đang chờ tạo lại — dùng nút Retry QR.\"}");
        } else {
            out.print("{\"success\":false,\"message\":\"Không tìm thấy booking hoặc phát hành thất bại.\"}");
        }
    }

    private void handleRetry(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String idStr = req.getParameter("bookingId");
        if (idStr == null) {
            idStr = req.getParameter("id");
        }
        int bookingId = parseId(idStr);
        if (bookingId <= 0) {
            out.print("{\"success\":false,\"message\":\"Booking ID không hợp lệ.\"}");
            return;
        }
        boolean ok = ticketService.retryQrGeneration(bookingId);
        if (ok) {
            out.print("{\"success\":true,\"message\":\"Tạo lại QR thành công. Vui lòng tải lại trang.\"}");
        } else {
            out.print("{\"success\":false,\"message\":\"Tạo QR thất bại sau 3 lần thử. Vui lòng liên hệ kỹ thuật.\"}");
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private int parseId(String s) {
        try {
            return (s != null) ? Integer.parseInt(s.trim()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
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
