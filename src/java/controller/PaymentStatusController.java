package controller;

import service.BookingService;
import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * API nhỏ cho UI theo dõi trạng thái thanh toán/booking.
 *
 * <p>
 * Luồng UI:</p>
 * <ul>
 * <li>Client gọi GET {@code /payment/status?bookingId=...} khi cần polling
 * trạng thái.</li>
 * <li>Controller đọc {@code bookingId}, hỏi
 * {@link BookingDAO#getBookingStatus(int)}.</li>
 * <li>Nếu booking đã {@code CONFIRMED}, trả JSON {@code {"paid":true}}.</li>
 * <li>Nếu thiếu/sai id hoặc chưa thanh toán, trả {@code {"paid":false}}.</li>
 * </ul>
 *
 * @author HuyPD
 */
@WebServlet("/payment/status")
public class PaymentStatusController extends HttpServlet {

    /*
     * DAO nay dung de hoi DB xem booking hien tai dang o trang thai nao.
     * Controller khong tu query SQL truc tiep, ma goi DAO de tach tang web va tang data.
     */
    private final BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        /*
         * Endpoint nay tra JSON cho JavaScript, khong tra HTML.
         * charset=UTF-8 giup response khong loi font neu sau nay message co tieng Viet.
         */
        response.setContentType("application/json;charset=UTF-8");

        /*
         * bookingId la id cua booking can kiem tra.
         * UI goi dang: /payment/status?bookingId=123
         */
        int bookingId;
        try {
            // Lay bookingId tu query string va ep ve int de truyen xuong DAO.
            bookingId = Integer.parseInt(request.getParameter("bookingId"));
        } catch (Exception e) {
            /*
             * Neu thieu bookingId hoac bookingId khong phai so,
             * controller khong nem loi 500 ma tra paid=false cho UI tiep tuc xu ly an toan.
             */
            response.getWriter().write("{\"paid\":false}");
            return;
        }

        // Hoi DB trang thai booking hien tai, vi du: PENDING, CONFIRMED, CANCELLED.
        /*
         * Service sẽ dọn các booking ONLINE PENDING quá 10 phút trước khi trả
         * status. Vì trang QR polling endpoint này mỗi 3 giây nên UI nhận biết
         * vé hết hạn mà không cần người dùng tải lại trang.
         */
        String status = bookingService.getBookingStatus(bookingId);

        // Chi khi booking da CONFIRMED thi UI moi coi la da thanh toan thanh cong.
        boolean paid = "CONFIRMED".equalsIgnoreCase(status);
        boolean expired = "CANCELLED".equalsIgnoreCase(status);

        /*
         * expired=true giúp trang chi tiết dừng polling và tải lại badge
         * CANCELLED, thay vì tiếp tục hỏi trạng thái vô hạn sau khi hết 10 phút.
         */
        response.getWriter().write(
                "{\"paid\":" + paid + ",\"expired\":" + expired + "}");
    }
}
