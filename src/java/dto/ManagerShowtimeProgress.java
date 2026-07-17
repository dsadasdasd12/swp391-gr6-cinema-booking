package dto;

import java.sql.Timestamp;

/** Tiến độ bán ghế của một suất chiếu thuộc chi nhánh manager. */
public class ManagerShowtimeProgress {
    private final String movieTitle;
    private final String hallName;
    private final Timestamp startTime;
    private final int capacity;
    private final int soldSeats;

    public ManagerShowtimeProgress(String movieTitle, String hallName, Timestamp startTime,
            int capacity, int soldSeats) {
        this.movieTitle = movieTitle;
        this.hallName = hallName;
        this.startTime = startTime;
        this.capacity = capacity;
        this.soldSeats = soldSeats;
    }

    public String getMovieTitle() { return movieTitle; }
    public String getHallName() { return hallName; }
    public Timestamp getStartTime() { return startTime; }
    public int getCapacity() { return capacity; }
    public int getSoldSeats() { return soldSeats; }
    public int getRemainingSeats() { return Math.max(0, capacity - soldSeats); }
    public double getOccupancyRate() { return capacity == 0 ? 0 : Math.round(soldSeats * 10000.0 / capacity) / 100.0; }
}
