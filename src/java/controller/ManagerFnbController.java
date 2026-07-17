/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dto.ManagerFnbComboDTO;
import dto.ManagerFnbItemDTO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.User;
import service.ManagerFnbService;

/**
 *
 * @author tttru
 */
@WebServlet(urlPatterns = {
    "/manager/fnb",
    "/manager/fnb/update-stock",
    "/manager/fnb/toggle-item",
    "/manager/fnb/toggle-combo"
})
public class ManagerFnbController extends HttpServlet {

    private final ManagerFnbService managerFnbService
            = new ManagerFnbService();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ManagerFnbController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ManagerFnbController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        User manager = getAuthenticatedManager(request, response);

        if (manager == null) {
            return;
        }

        try {
            int managerId = manager.getId();

            // Xác định chi nhánh mà Manager đang quản lý
            int branchId
                    = managerFnbService.getManagerBranchId(managerId);

            // Lấy dữ liệu món lẻ và combo tại chi nhánh
            List<ManagerFnbItemDTO> items
                    = managerFnbService.getItemsByBranch(branchId);

            List<ManagerFnbComboDTO> combos
                    = managerFnbService.getCombosByBranch(branchId);

            String branchName
                    = managerFnbService.getBranchName(branchId);

            // Tab mặc định là món lẻ
            String activeTab
                    = normalizeTab(request.getParameter("tab"));

            // Thống kê tổng quan
            long enabledItems = items.stream()
                    .filter(ManagerFnbItemDTO::isEnabledAtBranch)
                    .count();

            long lowStockItems = items.stream()
                    .filter(item -> item.getStockQuantity() <= 5)
                    .count();

            // Truyền dữ liệu sang JSP
            request.setAttribute("branchId", branchId);
            request.setAttribute("branchName", branchName);

            request.setAttribute("items", items);
            request.setAttribute("combos", combos);

            request.setAttribute("totalItems", items.size());
            request.setAttribute("enabledItems", enabledItems);
            request.setAttribute("totalCombos", combos.size());
            request.setAttribute("lowStockItems", lowStockItems);

            request.setAttribute("activeTab", activeTab);

            request.getRequestDispatcher(
                    "/pages/manager/fnb-management.jsp"
            ).forward(request, response);

        } catch (IllegalStateException exception) {
            setFlashMessage(
                    request,
                    "error",
                    exception.getMessage()
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/manager/dashboard"
            );

        } catch (Exception exception) {
            exception.printStackTrace();

            setFlashMessage(
                    request,
                    "error",
                    "Không thể tải dữ liệu quản lý kho F&B."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/manager/dashboard"
            );
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        User manager = getAuthenticatedManager(request, response);

        if (manager == null) {
            return;
        }

        String servletPath = request.getServletPath();
        String activeTab
                = normalizeTab(request.getParameter("tab"));

        try {
            int branchId
                    = managerFnbService.getManagerBranchId(
                            manager.getId()
                    );

            switch (servletPath) {

                case "/manager/fnb/update-stock":
                    updateStock(request, branchId);
                    activeTab = "items";

                    setFlashMessage(
                            request,
                            "success",
                            "Cập nhật tồn kho thành công."
                    );
                    break;

                case "/manager/fnb/toggle-item":
                    toggleItem(request, branchId);
                    activeTab = "items";

                    setFlashMessage(
                            request,
                            "success",
                            "Cập nhật trạng thái bán món thành công."
                    );
                    break;

                case "/manager/fnb/toggle-combo":
                    toggleCombo(request, branchId);
                    activeTab = "combos";

                    setFlashMessage(
                            request,
                            "success",
                            "Cập nhật trạng thái bán combo thành công."
                    );
                    break;

                default:
                    response.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
                    return;
            }

        } catch (IllegalArgumentException
                | IllegalStateException exception) {
            setFlashMessage(
                    request,
                    "error",
                    exception.getMessage()
            );

        } catch (Exception exception) {
            exception.printStackTrace();

            setFlashMessage(
                    request,
                    "error",
                    "Có lỗi xảy ra khi cập nhật kho F&B."
            );
        }

        response.sendRedirect(
                request.getContextPath()
                + "/manager/fnb?tab="
                + activeTab
        );
    }

    /**
     * Cập nhật số lượng tồn kho của món lẻ.
     */
    private void updateStock(
            HttpServletRequest request,
            int branchId
    ) {

        int productId = parsePositiveInt(
                request.getParameter("productId"),
                "Món F&B không hợp lệ."
        );

        int stockChange;

        try {
            stockChange = Integer.parseInt(
                    request.getParameter("stockChange")
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Số lượng thay đổi không hợp lệ."
            );
        }

        managerFnbService.changeStock(
                branchId,
                productId,
                stockChange
        );
    }

    /**
     * Bật hoặc tắt bán món tại chi nhánh.
     */
    private void toggleItem(
            HttpServletRequest request,
            int branchId
    ) {
        int productId = parsePositiveInt(
                request.getParameter("productId"),
                "Món F&B không hợp lệ."
        );

        boolean enabled
                = Boolean.parseBoolean(
                        request.getParameter("enabled")
                );

        managerFnbService.setItemEnabledAtBranch(
                branchId,
                productId,
                enabled
        );
    }

    /**
     * Bật hoặc tắt bán combo tại chi nhánh.
     */
    private void toggleCombo(
            HttpServletRequest request,
            int branchId
    ) {
        int comboId = parsePositiveInt(
                request.getParameter("comboId"),
                "Combo không hợp lệ."
        );

        boolean enabled
                = Boolean.parseBoolean(
                        request.getParameter("enabled")
                );

        managerFnbService.setComboEnabledAtBranch(
                branchId,
                comboId,
                enabled
        );
    }

    /**
     * Kiểm tra người dùng đã đăng nhập và có role MANAGER.
     */
    private User getAuthenticatedManager(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        HttpSession session
                = request.getSession(false);

        if (session == null
                || session.getAttribute("user") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return null;
        }

        Object sessionUser
                = session.getAttribute("user");

        if (!(sessionUser instanceof User)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return null;
        }

        User user = (User) sessionUser;

        if (user.getRole() == null
                || !"MANAGER".equalsIgnoreCase(
                        user.getRole()
                )) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return null;
        }

        return user;
    }

    /**
     * Chỉ chấp nhận hai tab items và combos.
     */
    private String normalizeTab(String tab) {
        if ("combos".equalsIgnoreCase(tab)) {
            return "combos";
        }

        return "items";
    }

    /**
     * Chuyển chuỗi thành số nguyên dương.
     */
    private int parsePositiveInt(
            String value,
            String errorMessage
    ) {
        try {
            int number = Integer.parseInt(value);

            if (number <= 0) {
                throw new NumberFormatException();
            }

            return number;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    errorMessage
            );
        }
    }

    /**
     * Chuyển chuỗi thành số nguyên không âm.
     */
    private int parseNonNegativeInt(
            String value,
            String errorMessage
    ) {
        try {
            int number = Integer.parseInt(value);

            if (number < 0) {
                throw new NumberFormatException();
            }

            return number;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    errorMessage
            );
        }
    }

    /**
     * Lưu thông báo tạm thời vào session.
     */
    private void setFlashMessage(
            HttpServletRequest request,
            String type,
            String message
    ) {
        HttpSession session
                = request.getSession();

        session.setAttribute(
                "flashType",
                type
        );

        session.setAttribute(
                "flashMessage",
                message
        );
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
