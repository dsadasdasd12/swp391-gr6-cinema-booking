package dto;

/** Các chỉ số vận hành tức thời, chỉ thuộc một chi nhánh manager. */
public class ManagerOperationalSnapshot {
    private final int upcomingShowtimes, completedShowtimes, cancelledShowtimes, lowSalesShowtimes;
    private final int activeHalls, maintenanceHalls, availableSeats;
    private final int lowStockItems, outOfStockItems;

    public ManagerOperationalSnapshot(int upcomingShowtimes, int completedShowtimes, int cancelledShowtimes,
            int lowSalesShowtimes, int activeHalls, int maintenanceHalls, int availableSeats,
            int lowStockItems, int outOfStockItems) {
        this.upcomingShowtimes = upcomingShowtimes;
        this.completedShowtimes = completedShowtimes;
        this.cancelledShowtimes = cancelledShowtimes;
        this.lowSalesShowtimes = lowSalesShowtimes;
        this.activeHalls = activeHalls;
        this.maintenanceHalls = maintenanceHalls;
        this.availableSeats = availableSeats;
        this.lowStockItems = lowStockItems;
        this.outOfStockItems = outOfStockItems;
    }
    public int getUpcomingShowtimes() { return upcomingShowtimes; }
    public int getCompletedShowtimes() { return completedShowtimes; }
    public int getCancelledShowtimes() { return cancelledShowtimes; }
    public int getLowSalesShowtimes() { return lowSalesShowtimes; }
    public int getActiveHalls() { return activeHalls; }
    public int getMaintenanceHalls() { return maintenanceHalls; }
    public int getAvailableSeats() { return availableSeats; }
    public int getLowStockItems() { return lowStockItems; }
    public int getOutOfStockItems() { return outOfStockItems; }
}
