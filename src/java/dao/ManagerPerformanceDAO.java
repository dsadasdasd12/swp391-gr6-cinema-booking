package dao;

import dto.ManagerHallOccupancy;
import dto.ManagerPerformanceReport;
import dto.ManagerRevenueSummary;
import dto.ManagerShowtimeProgress;
import dto.ManagerOperationalSnapshot;
import dto.ManagerMoviePerformance;
import dto.ManagerRevenueTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import util.DBContext;

/** Query riêng cho manager; không gọi DAO/servlet báo cáo của admin. */
public class ManagerPerformanceDAO {
    private static final String SOLD_BOOKING_STATUSES = "('CONFIRMED','CHECKED_IN','USED','COMPLETED')";

    public int getAssignedBranchId(int managerId) {
        String sql = "SELECT branch_id FROM dbo.STAFF_BRANCH WHERE user_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("branch_id") : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public ManagerPerformanceReport getReport(int branchId, Timestamp from, Timestamp toExclusive) {
        List<ManagerShowtimeProgress> showtimes = getShowtimeProgress(branchId, from, toExclusive);
        List<ManagerHallOccupancy> halls = getHallOccupancy(branchId, from, toExclusive);
        int totalCapacity = 0;
        int soldSeats = 0;
        for (ManagerShowtimeProgress showtime : showtimes) {
            totalCapacity += showtime.getCapacity();
            soldSeats += showtime.getSoldSeats();
        }
        return new ManagerPerformanceReport(showtimes.size(), totalCapacity, soldSeats, showtimes, halls);
    }

    /**
     * Chỉ tính tiền đã thanh toán SUCCESS và booking chưa hủy tại chi nhánh manager.
     * Phạm vi ngày dựa trên thời điểm thanh toán (hoặc booked_at khi dữ liệu cũ thiếu paid_at).
     */
    public ManagerRevenueSummary getRevenueSummary(int branchId, Timestamp from, Timestamp toExclusive) {
        String dateCondition = buildPaymentDateCondition(from, toExclusive);
        String sql = "SELECT COUNT(DISTINCT b.id) AS paid_bookings, "
                + "COALESCE(SUM(p.amount), 0) AS net_revenue, "
                + "COALESCE(SUM(seat_total.amount), 0) AS ticket_revenue, "
                + "COALESCE(SUM(fnb_total.amount), 0) AS fnb_revenue, "
                + "COALESCE(SUM(voucher_total.amount), 0) + COALESCE(SUM(counter_total.amount), 0) AS discount_amount "
                + "FROM dbo.BOOKINGS b "
                + "JOIN dbo.SHOWTIMES st ON st.id = b.showtime_id "
                + "JOIN dbo.HALLS h ON h.id = st.hall_id "
                + "JOIN dbo.PAYMENTS p ON p.booking_id = b.id "
                + "OUTER APPLY (SELECT SUM(bs.price) AS amount FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id) seat_total "
                + "OUTER APPLY (SELECT SUM(bf.quantity * bf.unit_price) AS amount FROM dbo.BOOKING_FNB bf "
                + "             WHERE bf.booking_id = b.id AND bf.status <> 'CANCELLED') fnb_total "
                + "OUTER APPLY (SELECT SUM(vh.discount_amount) AS amount FROM dbo.VOUCHER_HISTORY vh WHERE vh.booking_id = b.id) voucher_total "
                + "OUTER APPLY (SELECT SUM(cd.amount) AS amount FROM dbo.COUNTER_DISCOUNTS cd WHERE cd.booking_id = b.id) counter_total "
                + "WHERE h.branch_id = ? AND p.status = 'SUCCESS' AND b.status <> 'CANCELLED' "
                + dateCondition;
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindBranchAndDateRange(ps, branchId, from, toExclusive);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ManagerRevenueSummary(rs.getInt("paid_bookings"),
                            rs.getBigDecimal("net_revenue"), rs.getBigDecimal("ticket_revenue"),
                            rs.getBigDecimal("fnb_revenue"), rs.getBigDecimal("discount_amount"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ManagerRevenueSummary(0, null, null, null, null);
    }

    public ManagerOperationalSnapshot getOperationalSnapshot(int branchId, Timestamp from, Timestamp toExclusive) {
        int upcoming = 0, completed = 0, cancelled = 0, lowSales = 0;
        int activeHalls = 0, maintenanceHalls = 0, availableSeats = 0;
        int lowStock = 0, outOfStock = 0;
        String dateCondition = buildDateCondition(from, toExclusive);
        String showtimeSql = "SELECT "
                + "COALESCE(SUM(CASE WHEN st.start_time >= GETDATE() AND st.status IN ('SCHEDULED','ON_SALE') THEN 1 ELSE 0 END),0) AS upcoming, "
                + "COALESCE(SUM(CASE WHEN st.status='COMPLETED' THEN 1 ELSE 0 END),0) AS completed, "
                + "COALESCE(SUM(CASE WHEN st.status='CANCELLED' THEN 1 ELSE 0 END),0) AS cancelled "
                + "FROM dbo.SHOWTIMES st JOIN dbo.HALLS h ON h.id=st.hall_id WHERE h.branch_id=? " + dateCondition;
        String lowSalesSql = "SELECT COUNT(*) FROM (SELECT st.id FROM dbo.SHOWTIMES st "
                + "JOIN dbo.HALLS h ON h.id=st.hall_id "
                + "LEFT JOIN dbo.BOOKINGS b ON b.showtime_id=st.id AND b.status IN " + SOLD_BOOKING_STATUSES + " "
                + "LEFT JOIN dbo.BOOKING_SEATS bs ON bs.booking_id=b.id "
                + "WHERE h.branch_id=? AND st.status IN ('SCHEDULED','ON_SALE') "
                + "AND st.start_time>=GETDATE() AND st.start_time<DATEADD(HOUR,24,GETDATE()) "
                + "GROUP BY st.id,h.total_seats HAVING COUNT(bs.id)*100.0/NULLIF(h.total_seats,0)<20) low_sale";
        String hallSql = "SELECT "
                + "COUNT(DISTINCT CASE WHEN h.status='ACTIVE' THEN h.id END) AS active_halls, "
                + "COUNT(DISTINCT CASE WHEN h.status='MAINTENANCE' THEN h.id END) AS maintenance_halls, "
                + "COALESCE(SUM(CASE WHEN h.status='ACTIVE' AND s.maintenance=0 THEN 1 ELSE 0 END),0) AS available_seats "
                + "FROM dbo.HALLS h LEFT JOIN dbo.SEATS s ON s.hall_id=h.id WHERE h.branch_id=?";
        String stockSql = "SELECT "
                + "COALESCE(SUM(CASE WHEN stock_quantity BETWEEN 1 AND 9 THEN 1 ELSE 0 END),0) AS low_stock, "
                + "COALESCE(SUM(CASE WHEN stock_quantity=0 THEN 1 ELSE 0 END),0) AS out_stock "
                + "FROM dbo.BRANCH_FNB_INVENTORY WHERE branch_id=? AND enabled_at_branch=1";
        try (Connection conn = new DBContext().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(showtimeSql)) {
                bindBranchAndDateRange(ps, branchId, from, toExclusive);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { upcoming=rs.getInt("upcoming"); completed=rs.getInt("completed"); cancelled=rs.getInt("cancelled"); } }
            }
            try (PreparedStatement ps = conn.prepareStatement(lowSalesSql); ResultSet rs = executeBranchQuery(ps, branchId)) { if (rs.next()) lowSales=rs.getInt(1); }
            try (PreparedStatement ps = conn.prepareStatement(hallSql); ResultSet rs = executeBranchQuery(ps, branchId)) { if (rs.next()) { activeHalls=rs.getInt("active_halls"); maintenanceHalls=rs.getInt("maintenance_halls"); availableSeats=rs.getInt("available_seats"); } }
            try (PreparedStatement ps = conn.prepareStatement(stockSql); ResultSet rs = executeBranchQuery(ps, branchId)) { if (rs.next()) { lowStock=rs.getInt("low_stock"); outOfStock=rs.getInt("out_stock"); } }
        } catch (Exception e) { e.printStackTrace(); }
        return new ManagerOperationalSnapshot(upcoming, completed, cancelled, lowSales,
                activeHalls, maintenanceHalls, availableSeats, lowStock, outOfStock);
    }

    public List<ManagerMoviePerformance> getTopMoviePerformance(int branchId, Timestamp from, Timestamp toExclusive) {
        List<ManagerMoviePerformance> rows = new ArrayList<>();
        String dateCondition = buildDateCondition(from, toExclusive);
        String sql = "WITH per_showtime AS (SELECT st.id,m.title,h.total_seats,COUNT(bs.id) sold_seats "
                + "FROM dbo.SHOWTIMES st JOIN dbo.HALLS h ON h.id=st.hall_id JOIN dbo.MOVIES m ON m.id=st.movie_id "
                + "LEFT JOIN dbo.BOOKINGS b ON b.showtime_id=st.id AND b.status IN " + SOLD_BOOKING_STATUSES + " "
                + "LEFT JOIN dbo.BOOKING_SEATS bs ON bs.booking_id=b.id WHERE h.branch_id=? " + dateCondition
                + " GROUP BY st.id,m.title,h.total_seats) "
                + "SELECT TOP 3 title,COUNT(*) showtime_count,SUM(sold_seats) sold_seats,SUM(total_seats) capacity "
                + "FROM per_showtime GROUP BY title ORDER BY SUM(sold_seats) DESC, SUM(sold_seats)*1.0/NULLIF(SUM(total_seats),0) DESC";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindBranchAndDateRange(ps, branchId, from, toExclusive);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) rows.add(new ManagerMoviePerformance(rs.getString("title"), rs.getInt("showtime_count"), rs.getInt("sold_seats"), rs.getInt("capacity"))); }
        } catch (Exception e) { e.printStackTrace(); }
        return rows;
    }

    public int countRevenueTransactions(int branchId, Timestamp from, Timestamp toExclusive) {
        String sql = "SELECT COUNT(*) FROM dbo.BOOKINGS b JOIN dbo.SHOWTIMES st ON st.id=b.showtime_id "
                + "JOIN dbo.HALLS h ON h.id=st.hall_id JOIN dbo.PAYMENTS p ON p.booking_id=b.id "
                + "WHERE h.branch_id=? AND p.status='SUCCESS' AND b.status<>'CANCELLED' " + buildPaymentDateCondition(from, toExclusive);
        try (Connection conn=new DBContext().getConnection(); PreparedStatement ps=conn.prepareStatement(sql)) {
            bindBranchAndDateRange(ps,branchId,from,toExclusive); try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getInt(1):0;}
        } catch(Exception e){e.printStackTrace(); return 0;}
    }

    public List<ManagerRevenueTransaction> getRevenueTransactions(int branchId, Timestamp from, Timestamp toExclusive, int offset, int limit) {
        List<ManagerRevenueTransaction> rows=new ArrayList<>();
        String sql="SELECT b.id,COALESCE(p.paid_at,b.booked_at) paid_at,m.title,b.source,COALESCE(p.method,p.type) payment_method,p.amount, "
                + "COALESCE(seat_total.amount,0) ticket_amount,COALESCE(fnb_total.amount,0) fnb_amount, "
                + "COALESCE(voucher_total.amount,0)+COALESCE(counter_total.amount,0) discount_amount "
                + "FROM dbo.BOOKINGS b JOIN dbo.SHOWTIMES st ON st.id=b.showtime_id JOIN dbo.HALLS h ON h.id=st.hall_id "
                + "JOIN dbo.MOVIES m ON m.id=st.movie_id JOIN dbo.PAYMENTS p ON p.booking_id=b.id "
                + "OUTER APPLY (SELECT SUM(price) amount FROM dbo.BOOKING_SEATS WHERE booking_id=b.id) seat_total "
                + "OUTER APPLY (SELECT SUM(quantity*unit_price) amount FROM dbo.BOOKING_FNB WHERE booking_id=b.id AND status<>'CANCELLED') fnb_total "
                + "OUTER APPLY (SELECT SUM(discount_amount) amount FROM dbo.VOUCHER_HISTORY WHERE booking_id=b.id) voucher_total "
                + "OUTER APPLY (SELECT SUM(amount) amount FROM dbo.COUNTER_DISCOUNTS WHERE booking_id=b.id) counter_total "
                + "WHERE h.branch_id=? AND p.status='SUCCESS' AND b.status<>'CANCELLED' " + buildPaymentDateCondition(from,toExclusive)
                + " ORDER BY COALESCE(p.paid_at,b.booked_at) DESC,b.id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try(Connection conn=new DBContext().getConnection(); PreparedStatement ps=conn.prepareStatement(sql)){
            int index=bindBranchAndDateRangeReturnIndex(ps,branchId,from,toExclusive); ps.setInt(index++,offset); ps.setInt(index,limit);
            try(ResultSet rs=ps.executeQuery()){while(rs.next())rows.add(new ManagerRevenueTransaction(rs.getInt("id"),rs.getTimestamp("paid_at"),rs.getString("title"),rs.getString("source"),rs.getString("payment_method"),rs.getBigDecimal("amount"),rs.getBigDecimal("ticket_amount"),rs.getBigDecimal("fnb_amount"),rs.getBigDecimal("discount_amount")));}
        } catch(Exception e){e.printStackTrace();}
        return rows;
    }

    private List<ManagerShowtimeProgress> getShowtimeProgress(int branchId, Timestamp from, Timestamp toExclusive) {
        List<ManagerShowtimeProgress> rows = new ArrayList<>();
        String dateCondition = buildDateCondition(from, toExclusive);
        String sql = "SELECT st.id, m.title AS movie_title, h.name AS hall_name, st.start_time, h.total_seats, "
                + "COUNT(bs.id) AS sold_seats "
                + "FROM dbo.SHOWTIMES st "
                + "JOIN dbo.HALLS h ON h.id = st.hall_id "
                + "JOIN dbo.MOVIES m ON m.id = st.movie_id "
                + "LEFT JOIN dbo.BOOKINGS b ON b.showtime_id = st.id AND b.status IN " + SOLD_BOOKING_STATUSES + " "
                + "LEFT JOIN dbo.BOOKING_SEATS bs ON bs.booking_id = b.id "
                + "WHERE h.branch_id = ? " + dateCondition
                + "GROUP BY st.id, m.title, h.name, st.start_time, h.total_seats "
                + "ORDER BY st.start_time ASC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindBranchAndDateRange(ps, branchId, from, toExclusive);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ManagerShowtimeProgress(rs.getString("movie_title"),
                            rs.getString("hall_name"), rs.getTimestamp("start_time"),
                            rs.getInt("total_seats"), rs.getInt("sold_seats")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    private List<ManagerHallOccupancy> getHallOccupancy(int branchId, Timestamp from, Timestamp toExclusive) {
        List<ManagerHallOccupancy> rows = new ArrayList<>();
        String dateCondition = buildDateCondition(from, toExclusive);
        String sql = "SELECT h.name AS hall_name, COUNT(DISTINCT st.id) AS showtime_count, "
                + "MAX(h.total_seats) * COUNT(DISTINCT st.id) AS total_capacity, COUNT(bs.id) AS sold_seats "
                + "FROM dbo.SHOWTIMES st "
                + "JOIN dbo.HALLS h ON h.id = st.hall_id "
                + "LEFT JOIN dbo.BOOKINGS b ON b.showtime_id = st.id AND b.status IN " + SOLD_BOOKING_STATUSES + " "
                + "LEFT JOIN dbo.BOOKING_SEATS bs ON bs.booking_id = b.id "
                + "WHERE h.branch_id = ? " + dateCondition
                + "GROUP BY h.id, h.name ORDER BY h.name";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindBranchAndDateRange(ps, branchId, from, toExclusive);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ManagerHallOccupancy(rs.getString("hall_name"),
                            rs.getInt("showtime_count"), rs.getInt("total_capacity"),
                            rs.getInt("sold_seats")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    private String buildDateCondition(Timestamp from, Timestamp toExclusive) {
        StringBuilder condition = new StringBuilder();
        if (from != null) condition.append(" AND st.start_time >= ? ");
        if (toExclusive != null) condition.append(" AND st.start_time < ? ");
        return condition.toString();
    }

    private String buildPaymentDateCondition(Timestamp from, Timestamp toExclusive) {
        StringBuilder condition = new StringBuilder();
        if (from != null) condition.append(" AND COALESCE(p.paid_at, b.booked_at) >= ? ");
        if (toExclusive != null) condition.append(" AND COALESCE(p.paid_at, b.booked_at) < ? ");
        return condition.toString();
    }

    private void bindBranchAndDateRange(PreparedStatement ps, int branchId,
            Timestamp from, Timestamp toExclusive) throws Exception {
        bindBranchAndDateRangeReturnIndex(ps, branchId, from, toExclusive);
    }

    private int bindBranchAndDateRangeReturnIndex(PreparedStatement ps, int branchId,
            Timestamp from, Timestamp toExclusive) throws Exception {
        int parameterIndex = 1;
        ps.setInt(parameterIndex++, branchId);
        if (from != null) ps.setTimestamp(parameterIndex++, from);
        if (toExclusive != null) ps.setTimestamp(parameterIndex, toExclusive);
        return toExclusive == null ? parameterIndex : parameterIndex + 1;
    }

    private ResultSet executeBranchQuery(PreparedStatement ps, int branchId) throws Exception {
        ps.setInt(1, branchId);
        return ps.executeQuery();
    }
}
