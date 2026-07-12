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

/**
 * Controller cho luồng Admin quản lý ngôn ngữ phim.
 *
 * <p>Luồng UI:</p>
 * <ul>
 *   <li>GET {@code /admin/languages}: hiển thị danh sách language.</li>
 *   <li>GET {@code action=new}: mở form tạo language mới.</li>
 *   <li>GET {@code action=edit&id=...}: mở form sửa language hiện có.</li>
 *   <li>POST {@code action=add|update}: validate tên/mã ngôn ngữ rồi lưu vào DB.</li>
 *   <li>GET/POST {@code action=delete}: ẩn/xóa language rồi redirect về danh sách.</li>
 * </ul>
 *
 * <p>Controller yêu cầu user role {@code ADMIN}; user không hợp lệ sẽ bị redirect.</p>
 *
 * @author HuyPD
 */
@WebServlet("/admin/languages")
public class AdminLanguageController extends HttpServlet {

    // JSP hien danh sach ngon ngu.
    private static final String LIST_PAGE = "/pages/admin/language-list.jsp";

    // JSP hien form them/sua ngon ngu.
    private static final String FORM_PAGE = "/pages/admin/language-form.jsp";

    // DAO lam viec truc tiep voi bang language trong DB.
    private final LanguageDAO languageDAO = new LanguageDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Tat ca route admin language deu bat buoc role ADMIN.
        if (!requireAdmin(request, response)) return;

        // Neu URL khong co action thi mac dinh la list.
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
        // POST form co the co tieng Viet nen set UTF-8 truoc khi doc parameter.
        request.setCharacterEncoding("UTF-8");

        // Kiem tra admin truoc khi cho ghi du lieu.
        if (!requireAdmin(request, response)) return;

        // action quyet dinh form dang add, update hay delete.
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
        // Lay tat ca language de JSP render bang danh sach.
        request.setAttribute("languages", languageDAO.findAll());
        request.getRequestDispatcher(LIST_PAGE).forward(request, response);
    }

    private void showNew(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Tao object rong de form co object language khi render.
        request.setAttribute("language", new Language());

        // formAction=add giup JSP submit ve action add.
        request.setAttribute("formAction", "add");
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }

    private void showEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lay language theo id tren URL de dua vao form sua.
        Language language = languageDAO.findById(parseId(request.getParameter("id")));
        if (language == null) {
            // Id sai thi quay lai danh sach, khong render form rong.
            response.sendRedirect(request.getContextPath() + "/admin/languages");
            return;
        }
        request.setAttribute("language", language);
        request.setAttribute("formAction", "update");
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }

    private void save(HttpServletRequest request, HttpServletResponse response, boolean update)
            throws ServletException, IOException {
        // Gom du lieu form vao model Language.
        Language language = new Language();
        language.setId(parseId(request.getParameter("id")));
        language.setName(trim(request.getParameter("name")));
        language.setCode(trim(request.getParameter("code")));
        language.setStatus(trim(request.getParameter("status")));
        if (language.getName() == null || language.getCode() == null) {
            // Validate bat buoc co ten va ma ngon ngu; neu loi thi forward lai form.
            request.setAttribute("error", "Ten ngon ngu va ma ngon ngu khong duoc de trong.");
            request.setAttribute("language", language);
            request.setAttribute("formAction", update ? "update" : "add");
            request.getRequestDispatcher(FORM_PAGE).forward(request, response);
            return;
        }

        // update=true thi cap nhat, update=false thi them moi.
        boolean success = update ? languageDAO.update(language) : languageDAO.insert(language);

        // Luu flash vao session vi sau do redirect se tao request moi.
        request.getSession().setAttribute(success ? "flashSuccess" : "flashError", success ? "Da luu ngon ngu thanh cong." : "Khong the luu ngon ngu.");
        response.sendRedirect(request.getContextPath() + "/admin/languages");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Xoa/an language theo id; DAO quyet dinh hard delete hay soft delete.
        boolean success = languageDAO.delete(parseId(request.getParameter("id")));
        request.getSession().setAttribute(success ? "flashSuccess" : "flashError", success ? "Da an ngon ngu." : "Khong the xoa ngon ngu.");
        response.sendRedirect(request.getContextPath() + "/admin/languages");
    }

    private boolean requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Khong tao session moi; chi kiem tra session hien co.
        HttpSession session = request.getSession(false);
        User user = null;
        if (session != null) {
            // Login thuong luu "user"; mot so trang admin cu co the luu "adminUser".
            Object current = session.getAttribute("user");
            if (!(current instanceof User)) current = session.getAttribute("adminUser");
            if (current instanceof User) user = (User) current;
        }
        if (user == null) {
            // Chua dang nhap thi ve login.
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            // Dang nhap nhung khong phai admin thi khong cho vao trang admin.
            response.sendRedirect(request.getContextPath() + "/home");
            return false;
        }
        return true;
    }

    private int parseId(String value) {
        // Convert id tu String sang int; loi thi tra 0.
        try { return value == null ? 0 : Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private String trim(String value) {
        // Chuan hoa input: null/rong -> null, con lai bo khoang trang dau/cuoi.
        if (value == null) return null;
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    private String valueOrDefault(String value, String defaultValue) {
        // Neu action khong co thi dung default de tranh switch tren null.
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
