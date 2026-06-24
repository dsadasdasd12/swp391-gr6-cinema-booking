package model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Attendance implements Serializable {
    private int id;
    private int bookingId;
    private int checkedBy;
    private Timestamp checkedAt;

    public Attendance() {}

    public Attendance(int id, int bookingId, int checkedBy, Timestamp checkedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.checkedBy = checkedBy;
        this.checkedAt = checkedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getCheckedBy() { return checkedBy; }
    public void setCheckedBy(int checkedBy) { this.checkedBy = checkedBy; }

    public Timestamp getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Timestamp checkedAt) { this.checkedAt = checkedAt; }
}
