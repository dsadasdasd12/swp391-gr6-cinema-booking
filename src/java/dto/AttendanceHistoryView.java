package dto;

import java.sql.Timestamp;

/** Một lần soát vé để hiển thị trong lịch sử của nhân viên cổng. */
public class AttendanceHistoryView {
    private final int bookingId;
    private final String qrCode;
    private final String movieTitle;
    private final String hallName;
    private final Timestamp showtimeStart;
    private final Timestamp checkedAt;

    public AttendanceHistoryView(int bookingId, String qrCode, String movieTitle,
            String hallName, Timestamp showtimeStart, Timestamp checkedAt) {
        this.bookingId = bookingId;
        this.qrCode = qrCode;
        this.movieTitle = movieTitle;
        this.hallName = hallName;
        this.showtimeStart = showtimeStart;
        this.checkedAt = checkedAt;
    }

    public int getBookingId() { return bookingId; }
    public String getQrCode() { return qrCode; }
    public String getMovieTitle() { return movieTitle; }
    public String getHallName() { return hallName; }
    public Timestamp getShowtimeStart() { return showtimeStart; }
    public Timestamp getCheckedAt() { return checkedAt; }
}
