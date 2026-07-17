/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dto.FnbCategoryDTO;
import dto.FnbCategoryFormDTO;
import dto.FnbProductDTO;
import dto.FnbProductFormDTO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import model.User;
import service.AdminFnbService;
import dto.FnbComboFormDTO;
import dto.FnbComboItemDTO;

/**
 *
 * @author tttru
 */
@WebServlet({
    "/admin/fnb-dashboard",
    "/admin/fnb-dashboard/toggle-sale",
    "/admin/fnb-dashboard/category/save",
    "/admin/fnb-dashboard/category/status",
    "/admin/fnb-dashboard/product/save",
    "/admin/fnb-dashboard/product/status",
    "/admin/fnb-dashboard/combo/save",
    "/admin/fnb-dashboard/combo/status",
    "/admin/fnb-dashboard/combo/toggle-sale"

})
public class AdminFnbController extends HttpServlet {

    private final AdminFnbService adminFnbService
            = new AdminFnbService();

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        User user = (User) session.getAttribute("user");

        return user != null
                && "ADMIN".equalsIgnoreCase(user.getRole());
    }

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
            out.println("<title>Servlet AdminFnbController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AdminFnbController at " + request.getContextPath() + "</h1>");
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
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        String path = request.getServletPath();

        if ("/admin/fnb-dashboard".equals(path)) {
            showDashboard(request, response);
            return;
        }

        response.sendRedirect(
                request.getContextPath() + "/admin/fnb-dashboard"
        );
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
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();

        switch (path) {
            case "/admin/fnb-dashboard/category/save":
                saveCategory(request, response);
                break;

            case "/admin/fnb-dashboard/category/status":
                changeCategoryStatus(request, response);
                break;

            case "/admin/fnb-dashboard/product/save":
                saveProduct(request, response);
                break;

            case "/admin/fnb-dashboard/product/status":
                changeProductStatus(request, response);
                break;

            case "/admin/fnb-dashboard/toggle-sale":
                toggleProductSale(request, response);
                break;

            case "/admin/fnb-dashboard/combo/save":
                saveCombo(request, response);
                break;

            case "/admin/fnb-dashboard/combo/status":
                changeComboStatus(request, response);
                break;

            case "/admin/fnb-dashboard/combo/toggle-sale":
                toggleComboSale(request, response);
                break;
            default:
                response.sendRedirect(
                        request.getContextPath() + "/admin/fnb-dashboard"
                );
                break;
        }
    }

    private void showDashboard(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        List<FnbCategoryDTO> categories
                = adminFnbService.getAllCategories();

        int selectedCategoryId = getSelectedCategoryId(
                request,
                categories
        );

        List<FnbProductDTO> products
                = selectedCategoryId > 0
                        ? adminFnbService.getProductsByCategory(
                                selectedCategoryId
                        )
                        : List.of();

        request.setAttribute("categories", categories);
        request.setAttribute(
                "selectedCategoryId",
                selectedCategoryId
        );
        request.setAttribute("products", products);
        request.setAttribute(
                "combos",
                adminFnbService.getAllCombos()
        );

        request.setAttribute(
                "comboProducts",
                adminFnbService.getActiveItemsForCombo()
        );
        request.getRequestDispatcher(
                "/pages/admin/fnb-dashboard.jsp"
        ).forward(request, response);
    }

    private int getSelectedCategoryId(
            HttpServletRequest request,
            List<FnbCategoryDTO> categories) {

        String categoryIdRaw
                = request.getParameter("categoryId");

        if (categoryIdRaw != null
                && !categoryIdRaw.isBlank()) {
            try {
                int categoryId
                        = Integer.parseInt(categoryIdRaw);

                if (categoryId > 0) {
                    return categoryId;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return categories.isEmpty()
                ? 0
                : categories.get(0).getId();
    }

    private void toggleProductSale(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String productIdRaw
                = request.getParameter("productId");

        String categoryIdRaw
                = request.getParameter("categoryId");

        boolean allowed
                = "true".equalsIgnoreCase(
                        request.getParameter("allowed")
                );

        try {
            int productId
                    = Integer.parseInt(productIdRaw);

            adminFnbService.changeAllowedToSell(
                    productId,
                    allowed
            );

            request.getSession().setAttribute(
                    "flashMessage",
                    allowed
                            ? "Đã cho phép bán sản phẩm."
                            : "Đã ngừng cho phép bán sản phẩm."
            );

            request.getSession().setAttribute(
                    "flashType",
                    "success"
            );

        } catch (Exception e) {
            request.getSession().setAttribute(
                    "flashMessage",
                    e.getMessage()
            );

            request.getSession().setAttribute(
                    "flashType",
                    "error"
            );
        }

        response.sendRedirect(
                request.getContextPath()
                + "/admin/fnb-dashboard?categoryId="
                + categoryIdRaw
        );
    }

    private void saveCategory(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String idRaw = request.getParameter("id");

        FnbCategoryFormDTO dto = new FnbCategoryFormDTO();

        if (idRaw != null && !idRaw.isBlank()) {
            dto.setId(Integer.parseInt(idRaw));
        }

        dto.setName(request.getParameter("name"));
        dto.setDescription(request.getParameter("description"));

        try {
            adminFnbService.saveCategory(dto);

            setFlash(
                    request,
                    "Lưu danh mục thành công.",
                    "success"
            );
        } catch (Exception e) {
            setFlash(request, e.getMessage(), "error");
        }

        response.sendRedirect(
                request.getContextPath() + "/admin/fnb-dashboard"
        );
    }

    private void changeCategoryStatus(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String status = request.getParameter("status");

            adminFnbService.changeCategoryStatus(id, status);

            setFlash(
                    request,
                    "Cập nhật trạng thái danh mục thành công.",
                    "success"
            );
        } catch (Exception e) {
            setFlash(request, e.getMessage(), "error");
        }

        response.sendRedirect(
                request.getContextPath() + "/admin/fnb-dashboard"
        );
    }

    private void saveProduct(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String idRaw = request.getParameter("id");
        String categoryIdRaw = request.getParameter("categoryId");

        try {
            FnbProductFormDTO dto = new FnbProductFormDTO();

            if (idRaw != null && !idRaw.isBlank()) {
                dto.setId(Integer.parseInt(idRaw));
            }

            dto.setCategoryId(Integer.parseInt(categoryIdRaw));
            dto.setName(request.getParameter("name"));
            dto.setDescription(request.getParameter("description"));
            dto.setProductType(request.getParameter("productType"));
            dto.setSellingPrice(
                    new BigDecimal(request.getParameter("sellingPrice"))
            );
            dto.setImageUrl(request.getParameter("imageUrl"));
            dto.setAllowedToSell(
                    "true".equals(request.getParameter("allowedToSell"))
            );

            adminFnbService.saveProduct(dto);

            setFlash(
                    request,
                    "Lưu sản phẩm thành công.",
                    "success"
            );
        } catch (Exception e) {
            setFlash(request, e.getMessage(), "error");
        }

        response.sendRedirect(
                request.getContextPath()
                + "/admin/fnb-dashboard?categoryId="
                + categoryIdRaw
        );
    }

    private void changeProductStatus(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String categoryId = request.getParameter("categoryId");

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String status = request.getParameter("status");

            adminFnbService.changeProductStatus(id, status);

            setFlash(
                    request,
                    "Cập nhật trạng thái sản phẩm thành công.",
                    "success"
            );
        } catch (Exception e) {
            setFlash(request, e.getMessage(), "error");
        }

        response.sendRedirect(
                request.getContextPath()
                + "/admin/fnb-dashboard?categoryId="
                + categoryId
        );
    }

    private void saveCombo(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String categoryIdRaw
                = request.getParameter("categoryId");

        try {
            FnbComboFormDTO dto
                    = new FnbComboFormDTO();

            String idRaw
                    = request.getParameter("id");

            if (idRaw != null
                    && !idRaw.isBlank()) {

                dto.setId(
                        Integer.parseInt(idRaw)
                );
            }

            dto.setName(
                    request.getParameter("name")
            );

            dto.setDescription(
                    request.getParameter("description")
            );

            dto.setImageUrl(
                    request.getParameter("imageUrl")
            );

            dto.setSellingPrice(
                    new BigDecimal(
                            request.getParameter(
                                    "sellingPrice"
                            )
                    )
            );

            dto.setAllowedToSell(
                    "true".equalsIgnoreCase(
                            request.getParameter(
                                    "allowedToSell"
                            )
                    )
            );

            String[] productIds
                    = request.getParameterValues(
                            "itemProductId"
                    );

            String[] quantities
                    = request.getParameterValues(
                            "itemQuantity"
                    );

            if (productIds != null
                    && quantities != null) {

                int size = Math.min(
                        productIds.length,
                        quantities.length
                );

                for (int i = 0; i < size; i++) {

                    FnbComboItemDTO item
                            = new FnbComboItemDTO();

                    item.setProductId(
                            Integer.parseInt(
                                    productIds[i]
                            )
                    );

                    item.setQuantity(
                            Integer.parseInt(
                                    quantities[i]
                            )
                    );

                    dto.getItems().add(item);
                }
            }

            adminFnbService.saveCombo(dto);

            setFlash(
                    request,
                    dto.getId() == null
                    ? "Tạo combo thành công."
                    : "Cập nhật combo thành công.",
                    "success"
            );

        } catch (Exception e) {

            setFlash(
                    request,
                    e.getMessage(),
                    "error"
            );
        }

        redirectToDashboard(
                request,
                response,
                categoryIdRaw
        );
    }

    private void changeComboStatus(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String categoryIdRaw
                = request.getParameter("categoryId");

        try {
            int comboId
                    = Integer.parseInt(
                            request.getParameter("id")
                    );

            String status
                    = request.getParameter("status");

            adminFnbService.changeComboStatus(
                    comboId,
                    status
            );

            setFlash(
                    request,
                    "Cập nhật trạng thái combo thành công.",
                    "success"
            );

        } catch (Exception e) {

            setFlash(
                    request,
                    e.getMessage(),
                    "error"
            );
        }

        redirectToDashboard(
                request,
                response,
                categoryIdRaw
        );
    }

    private void redirectToDashboard(
            HttpServletRequest request,
            HttpServletResponse response,
            String categoryIdRaw)
            throws IOException {

        String redirectUrl
                = request.getContextPath()
                + "/admin/fnb-dashboard";

        if (categoryIdRaw != null
                && !categoryIdRaw.isBlank()) {

            try {
                int categoryId
                        = Integer.parseInt(categoryIdRaw);

                if (categoryId > 0) {
                    redirectUrl
                            += "?categoryId="
                            + categoryId;
                }

            } catch (NumberFormatException ignored) {
                // Nếu categoryId không hợp lệ,
                // quay về dashboard mặc định.
            }
        }

        response.sendRedirect(redirectUrl);
    }

    private void toggleComboSale(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String categoryIdRaw
                = request.getParameter("categoryId");

        try {
            int comboId
                    = Integer.parseInt(
                            request.getParameter(
                                    "comboId"
                            )
                    );

            boolean allowed
                    = "true".equalsIgnoreCase(
                            request.getParameter(
                                    "allowed"
                            )
                    );

            adminFnbService
                    .changeComboAllowedToSell(
                            comboId,
                            allowed
                    );

            setFlash(
                    request,
                    allowed
                            ? "Đã cho phép bán combo."
                            : "Đã ngừng cho phép bán combo.",
                    "success"
            );

        } catch (Exception e) {

            setFlash(
                    request,
                    e.getMessage(),
                    "error"
            );
        }

        redirectToDashboard(
                request,
                response,
                categoryIdRaw
        );
    }

    private void setFlash(HttpServletRequest request,
            String message,
            String type) {
        request.getSession().setAttribute("flashMessage", message);
        request.getSession().setAttribute("flashType", type);
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
