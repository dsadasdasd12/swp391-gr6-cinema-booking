package controller;

import dao.LanguageDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Language;

/**
 * @author HuyPD
 */
@WebServlet(name = "LanguageController", urlPatterns = {"/admin/languages"})
public class LanguageController extends HttpServlet {

    private final LanguageDAO languageDAO = new LanguageDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "delete":
                deleteLanguage(request, response);
                break;
            default:
                listLanguages(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("add".equals(action)) {
            addLanguage(request, response);
        } else if ("update".equals(action)) {
            updateLanguage(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/languages");
        }
    }

    private void listLanguages(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Language> list = languageDAO.findAll();
        request.setAttribute("languages", list);
        request.getRequestDispatcher("/pages/admin/language-list.jsp").forward(request, response);
    }

    private void addLanguage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String status = request.getParameter("status");

        Language l = new Language();
        l.setName(name);
        l.setCode(code);
        l.setStatus(status != null ? status : "ACTIVE");

        if (languageDAO.insert(l)) {
            request.getSession().setAttribute("msgSuccess", "Thêm ngôn ngữ thành công.");
        } else {
            request.getSession().setAttribute("msgError", "Lỗi khi thêm ngôn ngữ.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/languages");
    }

    private void updateLanguage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String status = request.getParameter("status");

        Language l = new Language();
        l.setId(id);
        l.setName(name);
        l.setCode(code);
        l.setStatus(status);

        if (languageDAO.update(l)) {
            request.getSession().setAttribute("msgSuccess", "Cập nhật ngôn ngữ thành công.");
        } else {
            request.getSession().setAttribute("msgError", "Lỗi khi cập nhật ngôn ngữ.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/languages");
    }

    private void deleteLanguage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        
        // Cần bắt lỗi Reference Constraint nếu ngôn ngữ đã được gán cho phim
        if (languageDAO.delete(id)) {
            request.getSession().setAttribute("msgSuccess", "Xóa ngôn ngữ thành công.");
        } else {
            request.getSession().setAttribute("msgError", "Không thể xóa ngôn ngữ. Có thể ngôn ngữ này đang được dùng cho các phim.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/languages");
    }
}
