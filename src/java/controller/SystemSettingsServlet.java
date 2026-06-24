package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.DBContext;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/admin/settings")
public class SystemSettingsServlet extends HttpServlet {

    public static final String ATTR_MAINTENANCE = "system.maintenance";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) return;
        loadViewData(req);
        req.getRequestDispatcher("/pages/admin/settings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) return;

        req.setCharacterEncoding("UTF-8");
        String section = req.getParameter("section");
        if (section == null) section = "";

        HttpSession session = req.getSession();
        String ctx = req.getContextPath();
        var servletCtx = req.getServletContext();

        switch (section) {
            case "cinema" -> {
                int cinemaId = parseInt(req.getParameter("cinemaId"), 0);
                String name = trim(req.getParameter("name"));
                String phone = trim(req.getParameter("phone"));
                String address = trim(req.getParameter("address"));
                String status = "INACTIVE".equals(trim(req.getParameter("status")))
                        ? "INACTIVE" : "ACTIVE";
                if (cinemaId > 0 && updateCinema(cinemaId, name, phone, address, status)) {
                    session.setAttribute("flashSuccess", "Đã lưu thông tin rạp.");
                } else {
                    session.setAttribute("flashError", "Lưu thông tin rạp thất bại.");
                }
                resp.sendRedirect(ctx + "/admin/settings#panel-cinema");
            }
            case "smtp" -> {
                servletCtx.setAttribute("mail.smtp.host", trim(req.getParameter("smtpHost")));
                servletCtx.setAttribute("mail.smtp.port", trim(req.getParameter("smtpPort")));
                servletCtx.setAttribute("mail.smtp.user", trim(req.getParameter("smtpUser")));
                servletCtx.setAttribute("mail.smtp.auth", trim(req.getParameter("smtpAuth")));
                String pwd = req.getParameter("smtpPassword");
                if (pwd != null && !pwd.isBlank()) {
                    servletCtx.setAttribute("mail.smtp.password", pwd.trim());
                }
                session.setAttribute("flashSuccess", "Đã lưu cấu hình SMTP.");
                resp.sendRedirect(ctx + "/admin/settings#panel-smtp");
            }
            case "maintenance" -> {
                boolean on = "on".equals(req.getParameter("maintenance"));
                servletCtx.setAttribute(ATTR_MAINTENANCE, on);
                session.setAttribute("flashSuccess",
                        on ? "Đã bật chế độ bảo trì." : "Đã tắt chế độ bảo trì.");
                resp.sendRedirect(ctx + "/admin/settings#panel-maint");
            }
            default -> resp.sendRedirect(ctx + "/admin/settings");
        }
    }

    private void loadViewData(HttpServletRequest req) {
        req.setAttribute("cinema", loadCinema());

        var ctx = req.getServletContext();
        req.setAttribute("smtpHost", initOrAttr(ctx, "mail.smtp.host"));
        req.setAttribute("smtpPort", initOrAttr(ctx, "mail.smtp.port"));
        req.setAttribute("smtpUser", initOrAttr(ctx, "mail.smtp.user"));
        req.setAttribute("smtpAuth", initOrAttr(ctx, "mail.smtp.auth"));

        String pwd = initOrAttr(ctx, "mail.smtp.password");
        req.setAttribute("smtpPasswordSet", pwd != null && !pwd.isBlank());

        Object maint = ctx.getAttribute(ATTR_MAINTENANCE);
        req.setAttribute("maintenanceMode", Boolean.TRUE.equals(maint));
    }

    private CinemaDTO loadCinema() {
        String sql = "SELECT TOP 1 id, name, phone, address, status FROM dbo.CINEMA ORDER BY id";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                CinemaDTO c = new CinemaDTO();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setPhone(rs.getString("phone"));
                c.setAddress(rs.getString("address"));
                c.setStatus(rs.getString("status"));
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean updateCinema(int id, String name, String phone, String address, String status) {
        String sql = "UPDATE dbo.CINEMA SET name=?, phone=?, address=?, status=?, last_update=GETDATE WHERE id=?";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, name);
            ps.setString(2, phone);
            ps.setNString(3, address);
            ps.setString(4, status);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String initOrAttr(jakarta.servlet.ServletContext ctx, String key) {
        Object attr = ctx.getAttribute(key);
        if (attr instanceof String s && !s.isBlank()) return s.trim();
        String init = ctx.getInitParameter(key);
        return init != null ? init.trim() : "";
    }

    private boolean ensureAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user != null && "ADMIN".equals(user.getRole())) return true;
        resp.sendRedirect(req.getContextPath() + "/login");
        return false;
    }

    private static int parseInt(String s, int def) {
        try { return s != null ? Integer.parseInt(s.trim()) : def; }
        catch (NumberFormatException e) { return def; }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    public static class CinemaDTO {
        private int id;
        private String name;
        private String phone;
        private String address;
        private String status;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
