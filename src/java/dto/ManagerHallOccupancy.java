package dto;

/** Tổng hợp lấp đầy của một phòng trong khoảng ngày manager chọn. */
public class ManagerHallOccupancy {
    private final String hallName;
    private final int showtimeCount;
    private final int capacity;
    private final int soldSeats;

    public ManagerHallOccupancy(String hallName, int showtimeCount, int capacity, int soldSeats) {
        this.hallName = hallName;
        this.showtimeCount = showtimeCount;
        this.capacity = capacity;
        this.soldSeats = soldSeats;
    }

    public String getHallName() { return hallName; }
    public int getShowtimeCount() { return showtimeCount; }
    public int getCapacity() { return capacity; }
    public int getSoldSeats() { return soldSeats; }
    public double getOccupancyRate() { return capacity == 0 ? 0 : Math.round(soldSeats * 10000.0 / capacity) / 100.0; }
}
