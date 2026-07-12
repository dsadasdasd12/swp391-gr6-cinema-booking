package controller;

import dao.CategoryDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.Category;
import model.User;

@WebServlet("/admin/categories")
public class AdminCategoryController extends HttpServlet {

    private static final String LIST_PAGE = "/pages/admin/category-list.jsp";
    private static final String FORM_PAGE = "/pages/admin/category-form.jsp";
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireAdmin(request, response)) return;
        String action = valueOrDefault(request.getParameter("action"), "list");
        switch (action) {
            case "new" -> showNew(request, response);
            case "edit" -> showEdit(request, response);
            case "delete" -> delete(request, response);
            default -> list(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!requireAdmin(request, response)) return;
        String action = valueOrDefault(request.getParameter("action"), "");
        switch (action) {
            case "add" -> save(request, response, false);
            case "update" -> save(request, response, true);
            case "delete" -> delete(request, response);
            default -> response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("categories", categoryDAO.findAll());
        request.getRequestDispatcher(LIST_PAGE).forward(request, response);
    }

    private void showNew(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("category", new Category());
        request.setAttribute("formAction", "add");
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }

    private void showEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Category category = categoryDAO.findById(parseId(request.getParameter("id")));
        if (category == null) {
            response.sendRedirect(request.getContextPath() + "/admin/categories");
            return;
        }
        request.setAttribute("category", category);
        request.setAttribute("formAction", "update");
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }

    private void save(HttpServletRequest request, HttpServletResponse response, boolean update)
            throws ServletException, IOException {
        Category category = new Category();
        category.setId(parseId(request.getParameter("id")));
        category.setName(trim(request.getParameter("name")));
        category.setDescription(trim(request.getParameter("description")));
        category.setStatus(trim(request.getParameter("status")));
        if (category.getName() == null) {
            request.setAttribute("error", "Ten the loai khong duoc de trong.");
            request.setAttribute("category", category);
            request.setAttribute("formAction", update ? "update" : "add");
            request.getRequestDispatcher(FORM_PAGE).forward(request, response);
            return;
        }
        boolean success = update ? categoryDAO.update(category) : categoryDAO.insert(category);
        request.getSession().setAttribute(success ? "flashSuccess" : "flashError", success ? "Da luu the loai thanh cong." : "Khong the luu the loai.");
        response.sendRedirect(request.getContextPath() + "/admin/categories");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean success = categoryDAO.delete(parseId(request.getParameter("id")));
        request.getSession().setAttribute(success ? "flashSuccess" : "flashError", success ? "Da an the loai." : "Khong the xoa the loai.");
        response.sendRedirect(request.getContextPath() + "/admin/categories");
    }

    private boolean requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = null;
        if (session != null) {
            Object current = session.getAttribute("user");
            if (!(current instanceof User)) current = session.getAttribute("adminUser");
            if (current instanceof User) user = (User) current;
        }
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/home");
            return false;
        }
        return true;
    }

    private int parseId(String value) {
        try { return value == null ? 0 : Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private String trim(String value) {
        if (value == null) return null;
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
