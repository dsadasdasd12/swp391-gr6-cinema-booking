package dto;

/** Hiệu suất bán vé của một phim tại chi nhánh manager. */
public class ManagerMoviePerformance {
    private final String movieTitle;
    private final int showtimeCount, soldSeats, capacity;
    public ManagerMoviePerformance(String movieTitle, int showtimeCount, int soldSeats, int capacity) {
        this.movieTitle = movieTitle;
        this.showtimeCount = showtimeCount;
        this.soldSeats = soldSeats;
        this.capacity = capacity;
    }
    public String getMovieTitle() { return movieTitle; }
    public int getShowtimeCount() { return showtimeCount; }
    public int getSoldSeats() { return soldSeats; }
    public int getCapacity() { return capacity; }
    public double getOccupancyRate() { return capacity == 0 ? 0 : Math.round(soldSeats * 10000.0 / capacity) / 100.0; }
}
