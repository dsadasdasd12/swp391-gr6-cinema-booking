package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.DBContext;
import util.EncodingUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        Connection conn = DBContext.getInstance().getConnection();
        
        int activeMovies = 0;
        int customerCount = 0;
        int bookingCount = 0;
        double totalRevenue = 0.0;
        
        List<Map<String, Object>> recentBookings = new ArrayList<>();
        List<Map<String, Object>> comingMovies = new ArrayList<>();

        if (conn != null) {
            try {
                // Movie count
                String sqlMovies = "SELECT COUNT(*) FROM dbo.MOVIES WHERE status = 'NOW_SHOWING'";
                try (PreparedStatement ps = conn.prepareStatement(sqlMovies);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) activeMovies = rs.getInt(1);
                }

                // Customer count
                String sqlCust = "SELECT COUNT(*) FROM dbo.[USER] WHERE role = 'CUSTOMER'";
                try (PreparedStatement ps = conn.prepareStatement(sqlCust);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) customerCount = rs.getInt(1);
                }

                // Booking count
                String sqlBookings = "SELECT COUNT(*) FROM dbo.BOOKINGS";
                try (PreparedStatement ps = conn.prepareStatement(sqlBookings);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) bookingCount = rs.getInt(1);
                }

                // Revenue sum
                String sqlRevenue = "SELECT SUM(total_price) FROM dbo.BOOKINGS WHERE status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED')";
                try (PreparedStatement ps = conn.prepareStatement(sqlRevenue);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalRevenue = rs.getDouble(1);
                }

                // Recent Bookings (Top 5)
                String sqlRec = "SELECT TOP 5 b.id, u.full_name, m.title AS movie_title, b.total_price, b.status, b.booked_at " +
                                "FROM dbo.BOOKINGS b " +
                                "JOIN dbo.[USER] u ON b.user_id = u.id " +
                                "JOIN dbo.SHOWTIMES s ON b.showtime_id = s.id " +
                                "JOIN dbo.MOVIES m ON s.movie_id = m.id " +
                                "ORDER BY b.booked_at DESC";
                try (PreparedStatement ps = conn.prepareStatement(sqlRec);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> b = new HashMap<>();
                        b.put("id", rs.getInt("id"));
                        b.put("fullName", EncodingUtil.getString(rs, "full_name"));
                        b.put("movieTitle", EncodingUtil.getString(rs, "movie_title"));
                        b.put("totalPrice", rs.getDouble("total_price"));
                        b.put("status", rs.getString("status"));
                        Timestamp ba = rs.getTimestamp("booked_at");
                        if (ba != null) {
                            b.put("bookedAt", ba.toLocalDateTime().format(DATE_FORMAT));
                        }
                        recentBookings.add(b);
                    }
                }

                // Coming Soon Movies
                String sqlCom = "SELECT TOP 4 id, title, poster_url, release_date " +
                                "FROM dbo.MOVIES WHERE status = 'COMING_SOON' " +
                                "ORDER BY release_date ASC";
                try (PreparedStatement ps = conn.prepareStatement(sqlCom);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("title", EncodingUtil.getString(rs, "title"));
                        m.put("posterUrl", rs.getString("poster_url"));
                        m.put("releaseDate", rs.getDate("release_date").toString());
                        comingMovies.add(m);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        req.setAttribute("activeMovies", activeMovies);
        req.setAttribute("customerCount", customerCount);
        req.setAttribute("bookingCount", bookingCount);
        req.setAttribute("totalRevenue", totalRevenue);
        req.setAttribute("recentBookings", recentBookings);
        req.setAttribute("comingMovies", comingMovies);

        req.getRequestDispatcher("/pages/admin/dashboard.jsp").forward(req, resp);
    }
}
