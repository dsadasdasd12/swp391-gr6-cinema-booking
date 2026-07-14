<%@ page import="java.sql.*, util.DBContext" %>
<%
    Connection conn = null;
    try {
        conn = DBContext.getInstance().getConnection();
        if (conn != null) {
            Statement stmt = conn.createStatement();
            int rows = stmt.executeUpdate("UPDATE dbo.BOOKINGS SET qr_code = NULL WHERE qr_code LIKE 'DEMO%' OR qr_code LIKE 'RV-WALK-%' OR qr_code LIKE 'RV-ONLINE-%'");
            out.print("Cleared mock QR codes: " + rows + " rows.");
        }
    } catch (Exception e) {
        out.print("Error: " + e.getMessage());
    } finally {
        if (conn != null) try { conn.close(); } catch(Exception e) {}
    }
%>
