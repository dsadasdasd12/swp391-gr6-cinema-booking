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

@WebServlet(name = "GenreController", urlPatterns = {"/admin/genres"})
public class GenreController extends HttpServlet {

    private final GenreDAO genreDAO = new GenreDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

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
        String action = request.getParameter("action");
        
        if ("add".equals(action)) {
            addGenre(request, response);
        } else if ("update".equals(action)) {
            updateGenre(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/genres");
        }
    }

    private void listGenres(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Genre> list = genreDAO.findAll();
        request.setAttribute("genres", list);
        request.getRequestDispatcher("/pages/admin/genre-list.jsp").forward(request, response);
    }

    private void addGenre(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String name = request.getParameter("name");
        String desc = request.getParameter("description");
        String status = request.getParameter("status");

        Genre c = new Genre();
        c.setName(name);
        c.setDescription(desc);
        c.setStatus(status != null ? status : "ACTIVE");

        if (genreDAO.insert(c)) {
            request.getSession().setAttribute("msgSuccess", "Thêm Genre thành công.");
        } else {
            request.getSession().setAttribute("msgError", "Lỗi khi thêm Genre.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/genres");
    }

    private void updateGenre(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String desc = request.getParameter("description");
        String status = request.getParameter("status");

        Genre c = new Genre();
        c.setId(id);
        c.setName(name);
        c.setDescription(desc);
        c.setStatus(status);

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
