package dto;

import java.util.List;

/** Dữ liệu độc lập dành cho dashboard hiệu suất của manager. */
public class ManagerPerformanceReport {
    private final int totalShowtimes;
    private final int totalCapacity;
    private final int soldSeats;
    private final List<ManagerShowtimeProgress> showtimes;
    private final List<ManagerHallOccupancy> halls;

    public ManagerPerformanceReport(int totalShowtimes, int totalCapacity, int soldSeats,
            List<ManagerShowtimeProgress> showtimes, List<ManagerHallOccupancy> halls) {
        this.totalShowtimes = totalShowtimes;
        this.totalCapacity = totalCapacity;
        this.soldSeats = soldSeats;
        this.showtimes = showtimes;
        this.halls = halls;
    }

    public int getTotalShowtimes() { return totalShowtimes; }
    public int getTotalCapacity() { return totalCapacity; }
    public int getSoldSeats() { return soldSeats; }
    public int getRemainingSeats() { return Math.max(0, totalCapacity - soldSeats); }
    public double getOccupancyRate() { return totalCapacity == 0 ? 0 : Math.round(soldSeats * 10000.0 / totalCapacity) / 100.0; }
    public List<ManagerShowtimeProgress> getShowtimes() { return showtimes; }
    public List<ManagerHallOccupancy> getHalls() { return halls; }
}
