package controller;

import service.UserService;
import dao.AdminUserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ManagedUser;
import model.User;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/admin/accounts/customers")
public class CustomerAccountServlet extends HttpServlet {

    private final AdminUserDAO userDAO = new AdminUserDAO();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("detail".equalsIgnoreCase(action)) {
            handleDetail(req, resp);
            return;
        }

        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");
        String createdDateParam = req.getParameter("createdDate");
        LocalDate createdDate = null;
        if (createdDateParam != null && !createdDateParam.isBlank()) {
            try {
                createdDate = LocalDate.parse(createdDateParam);
            } catch (DateTimeParseException e) {
                req.setAttribute("filterError", "Ngày tạo tài khoản không hợp lệ.");
            }
        }

        int currentPage = 1;
        String pageStr = req.getParameter("page");
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try {
                currentPage = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                currentPage = 1;
            }
        }

        int pageSize = 10;
        int offset = (currentPage - 1) * pageSize;

        List<ManagedUser> customers = userDAO.findCustomersPaged(keyword, status, createdDate, offset, pageSize);
        int totalItems = userDAO.countCustomers(keyword, status, createdDate);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) {
            totalPages = 1;
        }

        req.setAttribute("customers", customers);
        req.setAttribute("currentPage", currentPage);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalItems", totalItems);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("selectedCreatedDate", createdDateParam == null ? "" : createdDateParam);
        String topBuyerName = userService.getTopTicketBuyerName();

        req.setAttribute("topBuyerName", topBuyerName);
        req.getRequestDispatcher("/pages/accounts/customers.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        String idStr = req.getParameter("id");
        int userId = 0;
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                userId = Integer.parseInt(idStr);
            } catch (NumberFormatException ignored) {
            }
        }

        if ("block".equalsIgnoreCase(action)) {
            if (userId > 0) {
                userDAO.updateActiveStatus(userId, false);
                req.getSession().setAttribute("flashSuccess", "Đã khóa tài khoản khách hàng thành công!");
            } else {
                req.getSession().setAttribute("flashError", "Tài khoản không hợp lệ!");
            }
        } else if ("unblock".equalsIgnoreCase(action)) {
            if (userId > 0) {
                userDAO.updateActiveStatus(userId, true);
                req.getSession().setAttribute("flashSuccess", "Đã mở khóa tài khoản khách hàng thành công!");
            } else {
                req.getSession().setAttribute("flashError", "Tài khoản không hợp lệ!");
            }
        }

        resp.sendRedirect(req.getContextPath() + "/admin/accounts/customers");
    }

    private void handleDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        int userId = 0;
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                userId = Integer.parseInt(idStr);
            } catch (NumberFormatException ignored) {
            }
        }
        User customer = userDAO.findById(userId);
        if (customer != null) {
            req.setAttribute("c", customer);
            req.getSession().setAttribute("flashInfo", "Thông tin khách hàng: " + customer.getFullName() + " - " + customer.getEmail());
        } else {
            req.getSession().setAttribute("flashError", "Không tìm thấy khách hàng!");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/accounts/customers");
    }
}
