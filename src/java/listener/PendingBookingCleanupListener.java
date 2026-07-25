package listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import service.BookingService;

/**
 * Tác vụ nền tự động nhả ghế của booking online không thanh toán đúng hạn.
 *
 * <p>Khi Tomcat khởi động ứng dụng, listener chạy kiểm tra mỗi 30 giây. Luồng
 * dữ liệu: listener -> BookingService -> BookingDAO -> SQL Server. DAO chuyển
 * booking PENDING quá 10 phút thành CANCELLED trong transaction; từ thời điểm
 * đó SeatDAO không còn coi các BOOKING_SEATS của booking này là ghế bận.</p>
 */
@WebListener
public class PendingBookingCleanupListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "pending-booking-cleanup");
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                int cancelled = new BookingService().cleanupExpiredPendingOnlineBookings();
                if (cancelled > 0) {
                    sce.getServletContext().log(
                            "Đã tự hủy " + cancelled
                            + " booking online quá hạn thanh toán và nhả ghế.");
                }
            } catch (Exception e) {
                // Không để một lần lỗi DB làm chết hẳn tác vụ cho các lần sau.
                sce.getServletContext().log(
                        "Không thể dọn booking online quá hạn thanh toán.", e);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
