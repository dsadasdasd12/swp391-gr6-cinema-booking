package controller;

import dao.GenreDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Genre;

/**
 * Controller cho luồng Admin quản lý genre/thể loại phim.
 *
 * <p>
 * Luồng UI:</p>
 * <ul>
 * <li>GET {@code /admin/genres}: lấy toàn bộ genre và forward sang
 * {@code genre-list.jsp}.</li>
 * <li>POST {@code action=add}: đọc name/description/status từ form và thêm
 * genre mới.</li>
 * <li>POST {@code action=update}: đọc id và dữ liệu form để cập nhật
 * genre.</li>
 * <li>GET {@code action=delete}: xóa hoặc ẩn genre theo id, sau đó redirect về
 * danh sách.</li>
 * </ul>
 *
 * @author HuyPD
 */
@WebServlet(name = "GenreController", urlPatterns = {"/admin/genres"})
public class GenreController extends HttpServlet {

    // GenreDAO phu trach lay/them/sua/xoa genre trong DB.
    private final GenreDAO genreDAO = new GenreDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // action nam tren query string, vi du /admin/genres?action=delete&id=1.
        String action = request.getParameter("action");
        if (action == null) {
            // Khong co action thi mac dinh hien danh sach.
            action = "list";
        }

        // GET chi xu ly list hoac delete theo code hien tai.
        switch (action) {
            case "delete":
                deleteGenre(request, response);
                break;
            default:
                listGenres(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // action cua form POST quyet dinh them moi hay cap nhat.
        String action = request.getParameter("action");

        if ("add".equals(action)) {
            // Submit form them genre.
            addGenre(request, response);
        } else if ("update".equals(action)) {
            // Submit form sua genre.
            updateGenre(request, response);
        } else {
            // Action khong hop le thi quay lai danh sach.
            response.sendRedirect(request.getContextPath() + "/admin/genres");
        }
    }

    private void listGenres(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lay toan bo genre de JSP render bang danh sach.
        List<Genre> list = genreDAO.findAll();
        // Attribute "genres" duoc genre-list.jsp su dung.
        request.setAttribute("genres", list);
        request.getRequestDispatcher("/pages/admin/genre-list.jsp").forward(request, response);
    }

    private void addGenre(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Doc du lieu user nhap tu form them genre.
        String name = request.getParameter("name");
        String desc = request.getParameter("description");
        String status = request.getParameter("status");

        // Dua du lieu form vao model Genre.
        Genre c = new Genre();
        c.setName(name);
        c.setDescription(desc);
        // Neu form khong gui status thi mac dinh ACTIVE.
        c.setStatus(status != null ? status : "ACTIVE");

        // Goi DAO insert va luu message vao session de hien sau redirect.
        if (genreDAO.insert(c)) {
            request.getSession().setAttribute("msgSuccess", "Thêm Genre thành công.");
        } else {
            request.getSession().setAttribute("msgError", "Lỗi khi thêm Genre.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/genres");
    }

    private void updateGenre(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Doc id va cac field moi tu form update.
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String desc = request.getParameter("description");
        String status = request.getParameter("status");

        // Tao model Genre co id de DAO biet cap nhat row nao.
        Genre c = new Genre();
        c.setId(id);
        c.setName(name);
        c.setDescription(desc);
        c.setStatus(status);

        // Goi DAO update va luu message vao session de hien sau redirect.
        if (genreDAO.update(c)) {
            request.getSession().setAttribute("msgSuccess", "Cập nhật Genre thành công.");
        } else {
            request.getSession().setAttribute("msgError", "Lỗi khi cập nhật Genre.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/genres");
    }

    private void deleteGenre(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));

        // Cần bắt lỗi Reference Constraint nếu thể loại đã được gán cho phim
        if (genreDAO.delete(id)) {
            request.getSession().setAttribute("msgSuccess", "Xóa Genre thành công.");
        } else {
            request.getSession().setAttribute("msgError", "Không thể xóa Genre. Có thể Genre này đang được dùng cho các phim.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/genres");
    }
}
