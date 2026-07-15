/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - suất chiếu hiển thị ở trang chi tiết phim
 */
/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - suất chiếu hiển thị ở trang chi tiết phim
 */
package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Showtime implements Serializable {
    private int id;
    private int hallId;
    private int movieId;
    private Timestamp startTime;
    private Timestamp endTime;
    private double basePrice;
    private String status;
    
    // Transient fields for UI/UX rendering
    private String movieTitle;
    private String hallName;
    private String moviePoster;
    private String hallType;
    private int branchId;
    private String branchName;
    private String branchAddress;

    public Showtime() {}

    public Showtime(int id, int hallId, int movieId, Timestamp startTime, Timestamp endTime, double basePrice, String status) {
        this.id = id;
        this.hallId = hallId;
        this.movieId = movieId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }
    
    public void setStartTime(LocalDateTime startTime) {
        if (startTime != null) {
            this.startTime = Timestamp.valueOf(startTime);
        } else {
            this.startTime = null;
        }
    }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }
    
    public void setEndTime(LocalDateTime endTime) {
        if (endTime != null) {
            this.endTime = Timestamp.valueOf(endTime);
        } else {
            this.endTime = null;
        }
    }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    
    public void setBasePrice(BigDecimal basePrice) {
        if (basePrice != null) {
            this.basePrice = basePrice.doubleValue();
        } else {
            this.basePrice = 0.0;
        }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    public String getMoviePoster() { return moviePoster; }
    public void setMoviePoster(String moviePoster) { this.moviePoster = moviePoster; }

    public String getHallType() { return hallType; }
    public void setHallType(String hallType) { this.hallType = hallType; }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getBranchAddress() { return branchAddress; }
    public void setBranchAddress(String branchAddress) { this.branchAddress = branchAddress; }

    // Helper formatting methods for JSP (Booking & Showtime Manager)
    public String getFormattedStartTime() {
        if (this.startTime == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(this.startTime);
    }
    
    public String getFormattedEndTime() {
        if (this.endTime == null) return "";
        return new SimpleDateFormat("HH:mm").format(this.endTime);
    }

    // Helper formatting methods for JSP (Movie Browsing - original showtime details)
    public String getShowDate() {
        if (this.startTime == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy").format(this.startTime);
    }

    public String getStartHour() {
        if (this.startTime == null) return "";
        return new SimpleDateFormat("HH:mm").format(this.startTime);
    }

    public String getEndHour() {
        if (this.endTime == null) return "";
        return new SimpleDateFormat("HH:mm").format(this.endTime);
    }

    /**
    Form datetime-local: định dạng yyyy-MM-dd'T'HH:mm.
     */
    public String getStartInputValue() {
    if (this.startTime == null) return "";

    LocalDateTime ldt = this.startTime.toLocalDateTime();
    return ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
}

    private int movieDurationMin;

public int getMovieDurationMin() {
    return movieDurationMin;
}

public void setMovieDurationMin(int movieDurationMin) {
    this.movieDurationMin = movieDurationMin;
}
}
