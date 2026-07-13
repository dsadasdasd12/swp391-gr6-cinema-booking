package controller;

import dao.SeatTypeDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.SeatType;
import model.User;

@WebServlet(name = "AdminSeatTypeController", urlPatterns = {"/admin/seat-types"})
public class AdminSeatTypeController extends HttpServlet {

    private final SeatTypeDAO seatTypeDAO = new SeatTypeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            deleteSeatType(request, response);
        } else {
            List<SeatType> allSeatTypes = seatTypeDAO.findAll();
            request.setAttribute("allSeatTypes", allSeatTypes);
            request.getRequestDispatcher("/pages/admin/seat-types.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        String action = request.getParameter("action");

        if ("add".equals(action)) {
            addSeatType(request, response);
        } else if ("update".equals(action)) {
            updateSeatType(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/seat-types");
        }
    }

    private void addSeatType(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String defaultPriceStr = request.getParameter("defaultPrice");
        String color = request.getParameter("color");
        String status = request.getParameter("status");

        double defaultPrice = 0.0;
        try {
            if (defaultPriceStr != null) {
                defaultPrice = Double.parseDouble(defaultPriceStr);
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        SeatType st = new SeatType();
        st.setCode(code);
        st.setName(name);
        st.setDefaultPrice(defaultPrice);
        st.setColor(color != null ? color : "#10b981");
        st.setStatus(status != null ? status : "ACTIVE");

        if (seatTypeDAO.insert(st)) {
            request.getSession().setAttribute("msgSuccess", "Thêm loại ghế thành công.");
        } else {
            request.getSession().setAttribute("msgError", "Lỗi khi thêm loại ghế (Mã loại ghế có thể đã tồn tại).");
        }

        response.sendRedirect(request.getContextPath() + "/admin/seat-types");
    }

    private void updateSeatType(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String defaultPriceStr = request.getParameter("defaultPrice");
        String color = request.getParameter("color");
        String status = request.getParameter("status");

        double defaultPrice = 0.0;
        try {
            if (defaultPriceStr != null) {
                defaultPrice = Double.parseDouble(defaultPriceStr);
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        SeatType st = new SeatType();
        st.setId(id);
        st.setCode(code);
        st.setName(name);
        st.setDefaultPrice(defaultPrice);
        st.setColor(color != null ? color : "#10b981");
        st.setStatus(status);

        if (seatTypeDAO.update(st)) {
            request.getSession().setAttribute("msgSuccess", "Cập nhật loại ghế thành công.");
        } else {
            request.getSession().setAttribute("msgError", "Lỗi khi cập nhật loại ghế.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/seat-types");
    }

    private void deleteSeatType(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));

        if (seatTypeDAO.delete(id)) {
            request.getSession().setAttribute("msgSuccess", "Xóa hoặc Khóa trạng thái loại ghế thành công.");
        } else {
            request.getSession().setAttribute("msgError", "Lỗi khi thực hiện xóa hoặc khóa loại ghế.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/seat-types");
    }
}
