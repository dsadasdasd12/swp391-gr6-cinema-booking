package model;

import java.io.Serializable;

public class Seat implements Serializable {
    private int id;
    private int hallId;
    private String seatRow;
    private int seatNumber;
    private String seatType;
    private boolean maintenance;

    public Seat() {}

    public Seat(int id, int hallId, String seatRow, int seatNumber, String seatType, boolean maintenance) {
        this.id = id;
        this.hallId = hallId;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.maintenance = maintenance;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getSeatRow() { return seatRow; }
    public void setSeatRow(String seatRow) { this.seatRow = seatRow; }

    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }

    public boolean isMaintenance() { return maintenance; }
    public void setMaintenance(boolean maintenance) { this.maintenance = maintenance; }
    
    // Tiện ích để hiển thị mã ghế (Ví dụ: B3)
    public String getSeatCode() {
        return this.seatRow + this.seatNumber;
    }

    // Lấy chỉ số dòng để định vị trong CSS Grid (A=1, B=2, C=3,...)
    public int getRowIndex() {
        if (seatRow == null || seatRow.isEmpty()) return 1;
        char ch = seatRow.toUpperCase().charAt(0);
        return ch - 'A' + 1;
    }
}
