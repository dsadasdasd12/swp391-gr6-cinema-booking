/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Đặt vé - nghiệp vụ phần KHÁCH (lịch sử / chi tiết / hủy đơn)
 */
package service;

import java.util.ArrayList;
import java.util.List;
import dao.BookingDAO;
import dto.BookingView;

/**
 * Tầng nghiệp vụ cho phần khách thao tác với đơn đặt vé của chính mình: xem
 * lịch sử, xem chi tiết, theo dõi trạng thái và tự hủy đơn. Controller chỉ làm
 * việc với lớp này; mọi truy vấn ràng buộc theo userId để khách chỉ thấy/sửa
 * đúng đơn của mình.
 *
 * @author Group6 - Huy (Module Đặt vé)
 */
public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();

    /** Lịch sử đặt vé của khách (mới nhất lên đầu). */
    public List<BookingView> getHistory(int userId) {
        if (userId <= 0) {
            return new ArrayList<>();
        }
        return bookingDAO.findByUserId(userId);
    }

    /**
     * Chi tiết một đơn của khách (kèm theo dõi trạng thái). Trả về {@code null}
     * nếu tham số không hợp lệ hoặc đơn không thuộc về khách này.
     */
    public BookingView getDetail(int bookingId, int userId) {
        if (bookingId <= 0 || userId <= 0) {
            return null;
        }
        return bookingDAO.findDetailByIdAndUser(bookingId, userId);
    }

    /**
     * Khách tự hủy đơn của mình; chỉ thành công khi đơn đang ở trạng thái cho
     * phép hủy (PENDING/CONFIRMED) và đúng là đơn của khách.
     */
    public boolean cancel(int bookingId, int userId) {
        if (bookingId <= 0 || userId <= 0) {
            return false;
        }
        return bookingDAO.cancelByUser(bookingId, userId);
    }
}
