package controller;

import dto.BookingView;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.BookingService;
import dao.BookingFnbDAO;

/**
 * Controller cho luong xem chi tiet booking, theo doi trang thai va huy
 * booking.
 *
 * <p>
 * GET {@code /my-booking?id=...}: user xem chi tiet ve da dat.</p>
 * <p>
 * POST {@code /my-booking}: user gui form huy booking.</p>
 *
 * @author HuyPD
 */
@WebServlet(name = "BookingDetailController", urlPatterns = {"/my-booking"})
public class BookingDetailController extends HttpServlet {

    /*
     * Thong tin ngan hang dung de tao QR thanh toan VietQR.
     * Khi user xem chi tiet booking dang cho thanh toan, JSP hien QR nay.
     */
    private static final String BANK_CODE = "BIDV";
    private static final String ACCOUNT_NO = "96247GGDYW";
    private static final String ACCOUNT_NAME = "TRAN THE TRUONG";

    /*
     * BookingService gom logic lay chi tiet booking va huy booking.
     * Controller khong goi DAO truc tiep de tranh lap logic kiem tra quyen.
     */
    private final BookingService bookingService = new BookingService();
    private final BookingFnbDAO bookingFnbDAO = new BookingFnbDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Lay user trong session; trang nay bat buoc dang nhap.
        User user = currentUser(request);
        if (user == null) {
            // Chua login thi day ve /login, khong render chi tiet booking.
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Doc id booking tu URL: /my-booking?id=123.
        int id = parseId(request.getParameter("id"));

        /*
         * Lay chi tiet booking theo ca bookingId va userId.
         * userId rat quan trong de user khong xem duoc booking cua nguoi khac.
         */
        BookingView booking = bookingService.getDetail(id, user.getId());

        if (booking == null) {
            /*
             * Khong tim thay booking hoac booking khong thuoc user hien tai.
             * Set status 404 nhung van forward sang JSP de hien trang loi than thien.
             */
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("notFound", Boolean.TRUE);
        } else {
            // Attribute "bk" la object chi tiet booking de JSP render thong tin ve/phim/ghe.
            request.setAttribute("bk", booking);
            request.setAttribute("statusHistory", bookingService.getStatusHistory(booking.getBooking().getId()));
            request.setAttribute("fnbLines", bookingFnbDAO.findByBookingId(booking.getBooking().getId()));

            // Noi dung chuyen khoan co dang RVS + bookingId de webhook tach ra duoc booking can confirm.
            String transferContent = "RVS" + booking.getBooking().getId();

            /*
             * Tao URL anh QR tu dich vu VietQR.
             * amount la tong tien booking, addInfo la noi dung chuyen khoan, accountName la ten chu TK.
             */
            String paymentQr
                    = "https://img.vietqr.io/image/"
                    + BANK_CODE + "-"
                    + ACCOUNT_NO
                    + "-compact2.png"
                    + "?amount=" + (long) booking.getBooking().getTotalPrice()
                    + "&addInfo=" + URLEncoder.encode(transferContent, StandardCharsets.UTF_8)
                    + "&accountName=" + URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8);

            // Gui QR va noi dung chuyen khoan sang JSP.
            request.setAttribute("paymentQr", paymentQr);
            request.setAttribute("transferContent", transferContent);
        }

        // Luon forward ve cung JSP; JSP tu hien chi tiet hoac notFound theo attribute.
        request.getRequestDispatcher("/pages/booking/detail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // POST hien tai dung cho hanh dong huy booking, nen cung bat buoc login.
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Doc booking id tu form huy booking.
        int id = parseId(request.getParameter("id"));

        // Service se kiem tra booking co thuoc user nay va co duoc phep huy hay khong.
        boolean ok = bookingService.cancel(id, user.getId());

        // Quay lai trang chi tiet kem thong bao ket qua de JSP hien alert.
        String msg = ok ? "cancelled" : "cancel_failed";
        response.sendRedirect(request.getContextPath() + "/my-booking?id=" + id + "&msg=" + msg);
    }

    /**
     * Lay user dang dang nhap. Ham nay tai su dung helper cua
     * BookingHistoryController de tat ca controller booking cung doc session
     * theo mot cach thong nhat.
     */
    private static User currentUser(HttpServletRequest request) {
        return BookingHistoryController.currentUser(request);
    }

    /**
     * Doc id tu tham so request. Return -1 neu null, rong hoac khong phai so de
     * cac service xu ly nhu id khong hop le.
     */
    private static int parseId(String s) {
        if (s == null || s.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
