package controller;

import dao.LanguageDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.Language;
import model.User;

@WebServlet("/admin/languages")
public class AdminLanguageController extends HttpServlet {

    private static final String LIST_PAGE = "/pages/admin/language-list.jsp";
    private static final String FORM_PAGE = "/pages/admin/language-form.jsp";
    private final LanguageDAO languageDAO = new LanguageDAO();

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
        request.setAttribute("languages", languageDAO.findAll());
        request.getRequestDispatcher(LIST_PAGE).forward(request, response);
    }

    private void showNew(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("language", new Language());
        request.setAttribute("formAction", "add");
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }

    private void showEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Language language = languageDAO.findById(parseId(request.getParameter("id")));
        if (language == null) {
            response.sendRedirect(request.getContextPath() + "/admin/languages");
            return;
        }
        request.setAttribute("language", language);
        request.setAttribute("formAction", "update");
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }

    private void save(HttpServletRequest request, HttpServletResponse response, boolean update)
            throws ServletException, IOException {
        Language language = new Language();
        language.setId(parseId(request.getParameter("id")));
        language.setName(trim(request.getParameter("name")));
        language.setCode(trim(request.getParameter("code")));
        language.setStatus(trim(request.getParameter("status")));
        if (language.getName() == null || language.getCode() == null) {
            request.setAttribute("error", "Ten ngon ngu va ma ngon ngu khong duoc de trong.");
            request.setAttribute("language", language);
            request.setAttribute("formAction", update ? "update" : "add");
            request.getRequestDispatcher(FORM_PAGE).forward(request, response);
            return;
        }
        boolean success = update ? languageDAO.update(language) : languageDAO.insert(language);
        request.getSession().setAttribute(success ? "flashSuccess" : "flashError", success ? "Da luu ngon ngu thanh cong." : "Khong the luu ngon ngu.");
        response.sendRedirect(request.getContextPath() + "/admin/languages");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean success = languageDAO.delete(parseId(request.getParameter("id")));
        request.getSession().setAttribute(success ? "flashSuccess" : "flashError", success ? "Da an ngon ngu." : "Khong the xoa ngon ngu.");
        response.sendRedirect(request.getContextPath() + "/admin/languages");
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
