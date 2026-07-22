package controller;

import dto.BookingDraft;
import dto.BookingDraftView;
import dto.BookingFnbLine;
import dto.SeatMap;
import dto.VoucherQuote;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Branch;
import model.Movie;
import model.Showtime;
import model.User;
import service.BookingService;
import dao.BookingFnbDAO;
import service.BranchService;
import service.CinemaService;
import service.MovieService;
import service.ShowtimeService;

/**
 * Controller điều phối toàn bộ luồng đặt vé online của khách hàng.
 *
 * <p>
 * Luồng đặt vé chính:</p>
 * <ul>
 * <li>GET {@code /booking/start}: hiển thị danh sách chi nhánh active để user
 * chọn branch.</li>
 * <li>GET {@code /booking/movies?branchId=...}: hiển thị phim có thể đặt tại
 * chi nhánh.</li>
 * <li>GET {@code /booking/showtimes?branchId=...&movieId=...&date=...}: hiển
 * thị suất chiếu.</li>
 * <li>GET {@code /booking/seats?showtimeId=...}: hiển thị sơ đồ ghế và trạng
 * thái ghế.</li>
 * <li>POST {@code /booking/seats}: validate ghế, tạo {@link BookingDraft} trong
 * session.</li>
 * <li>GET {@code /booking/confirm}: dựng {@link BookingDraftView} để user kiểm
 * tra booking.</li>
 * <li>POST {@code /booking/confirm}: tạo booking pending, xóa draft, chuyển
 * sang chi tiết booking.</li>
 * </ul>
 *
 * <p>
 * Ghi chú: mọi route booking đều yêu cầu user đã đăng nhập. Nếu chưa đăng nhập,
 * controller redirect về {@code /login}.</p>
 *
 * @author HuyPD
 */
@WebServlet(name = "BookingController", urlPatterns = {
    "/booking",
    "/booking/start",
    "/booking/movies",
    "/booking/showtimes",
    "/booking/seats",
    "/booking/fnb",
    "/booking/confirm"
})
public class BookingController extends HttpServlet {

    private static final DateTimeFormatter WEEK_DAY_LABEL_FORMAT
            = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));


    /*
     * Key dung de luu booking tam trong session.
     * Draft chi ton tai trong luc user dang chon ghe va chua bam xac nhan cuoi.
     */
    private static final String DRAFT_SESSION_KEY = "bookingDraft";
    private static final String VOUCHER_SESSION_KEY = "bookingVoucherCode";

    /*
     * Cac service ben duoi dai dien cho cac nhom nghiep vu khac nhau.
     * Controller chi dieu huong request, validate co ban, roi goi service phu hop.
     */
    private final CinemaService cinemaService = new CinemaService();
    private final BranchService branchService = new BranchService();
    private final MovieService movieService = new MovieService();
    private final ShowtimeService showtimeService = new ShowtimeService();
    private final BookingService bookingService = new BookingService();
    private final BookingFnbDAO bookingFnbDAO = new BookingFnbDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Tat ca man hinh booking deu yeu cau user da dang nhap.
        User user = currentUser(request);
        if (user == null) {
            // Neu chua login, chuyen sang trang login va dung xu ly tiep.
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // servletPath la route hien tai, vi du /booking/start hoac /booking/seats.
        String path = request.getServletPath();
        switch (path) {
            case "/booking":
                // Route cu /booking duoc redirect ve route moi phu hop.
                redirectLegacyBooking(request, response);
                break;
            case "/booking/movies":
                // Buoc chon phim sau khi da chon branch.
                showMovies(request, response);
                break;
            case "/booking/showtimes":
                // Buoc chon suat chieu sau khi da chon branch + movie.
                showShowtimes(request, response);
                break;
            case "/booking/seats":
                // Buoc xem so do ghe cua mot suat chieu.
                releaseHoldWhenChangingSeats(user, request);
                showSeats(request, response, null);
                break;
            case "/booking/confirm":
                // Buoc xac nhan thong tin booking truoc khi tao booking that trong DB.
                showConfirm(request, response, null);
                break;
            case "/booking/fnb":
                showFnb(request, response, null);
                break;
            case "/booking/start":
            default:
                // Buoc dau tien: chon chi nhanh.
                showStart(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Dam bao form POST doc dung tieng Viet neu co input text.
        request.setCharacterEncoding("UTF-8");

        // POST cung bat buoc dang nhap de tranh tao booking an danh.
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Route POST quyet dinh form nao dang duoc submit.
        String path = request.getServletPath();
        switch (path) {
            case "/booking/seats":
                // User submit danh sach ghe da chon.
                submitSeats(user, request, response);
                break;
            case "/booking/confirm":
                // User bam xac nhan tao booking.
                submitConfirm(user, request, response);
                break;
            case "/booking/fnb":
                submitFnb(request, response);
                break;
            default:
                // POST sai route thi dua ve dau luong booking.
                response.sendRedirect(request.getContextPath() + "/booking/start");
                break;
        }
    }

    private void redirectLegacyBooking(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        /*
         * Ho tro link cu dang /booking?showtimeId=...
         * Neu co showtimeId hop le thi dua thang sang man chon ghe.
         */
        int showtimeId = parsePositiveInt(request.getParameter("showtimeId"));
        if (showtimeId > 0) {
            response.sendRedirect(request.getContextPath() + "/booking/seats?showtimeId=" + showtimeId);
            return;
        }
        // Neu khong co showtimeId thi bat dau lai tu man chon chi nhanh.
        response.sendRedirect(request.getContextPath() + "/booking/start");
    }

    /**
     * Bước 1 của booking: hiển thị danh sách chi nhánh active để user chọn nơi
     * xem phim.
     */
    private void showStart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lay danh sach chi nhanh dang hoat dong de user chon noi xem phim.
        request.setAttribute("branches", cinemaService.getActiveBranchViews());
        // forward de JSP doc attribute "branches" va render giao dien.
        request.getRequestDispatcher("/pages/booking/start.jsp").forward(request, response);
    }

    /**
     * Bước 2 của booking: sau khi chọn branch, hiển thị các phim có suất
     * chiếu/được phân bổ tại branch đó.
     */
    private void showMovies(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // branchId duoc gui tu man chon chi nhanh.
        int branchId = parsePositiveInt(request.getParameter("branchId"));

        // Chi chap nhan branch ton tai va dang ACTIVE.
        Branch branch = activeBranch(branchId);
        if (branch == null) {
            // Branch sai thi quay lai buoc dau, khong cho di tiep.
            response.sendRedirect(request.getContextPath() + "/booking/start");
            return;
        }

        // Gui branch dang chon va danh sach phim co the dat tai branch do sang JSP.
        request.setAttribute("branch", branch);
        request.setAttribute("movies", movieService.getBookableMoviesByBranch(branchId));
        request.getRequestDispatcher("/pages/booking/select-movie.jsp").forward(request, response);
    }

    /**
     * Bước 3 của booking: sau khi chọn phim, hiển thị các suất chiếu bookable
     * theo ngày.
     */
    private void showShowtimes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Doc cac tham so user da chon tu URL.
        int branchId = parsePositiveInt(request.getParameter("branchId"));
        int movieId = parsePositiveInt(request.getParameter("movieId"));
        LocalDate date = parseDateOrToday(request.getParameter("date"));
        // The weekly calendar always starts on Monday, regardless of selected day.
        LocalDate weekStart = date.with(DayOfWeek.MONDAY);

        // Kiem tra branch va movie co hop le khong truoc khi lay suat chieu.
        Branch branch = activeBranch(branchId);
        Movie movie = movieService.getMovieDetail(movieId);
        if (branch == null || movie == null) {
            // Du lieu sai thi dua ve dau luong booking.
            response.sendRedirect(request.getContextPath() + "/booking/start");
            return;
        }

        // Keep all seven days in display order, including days with no available showtime.
        Map<String, List<Showtime>> weeklyShowtimes = new LinkedHashMap<>();
        for (int offset = 0; offset < 7; offset++) {
            LocalDate day = weekStart.plusDays(offset);
            weeklyShowtimes.put(day.format(WEEK_DAY_LABEL_FORMAT), new ArrayList<>());
        }
        for (Showtime showtime : showtimeService.getBookableShowtimesForWeek(branchId, movieId, weekStart)) {
            LocalDate showDate = showtime.getStartTime().toLocalDateTime().toLocalDate();
            List<Showtime> dayShowtimes = weeklyShowtimes.get(showDate.format(WEEK_DAY_LABEL_FORMAT));
            if (dayShowtimes != null) {
                dayShowtimes.add(showtime);
            }
        }

        // Send the full Monday-Sunday calendar to the JSP, not a single-day result.
        request.setAttribute("branch", branch);
        request.setAttribute("movie", movie);
        request.setAttribute("selectedDate", date);
        request.setAttribute("selectedDayLabel", date.format(WEEK_DAY_LABEL_FORMAT));
        request.setAttribute("weekStart", weekStart);
        request.setAttribute("weeklyShowtimes", weeklyShowtimes);
        request.getRequestDispatcher("/pages/booking/select-showtime.jsp").forward(request, response);
    }

    /**
     * Bước 4 của booking: hiển thị sơ đồ ghế của suất chiếu và trạng thái từng
     * ghế.
     */
    private void showSeats(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        // showtimeId la suat chieu user chon o buoc truoc.
        int showtimeId = parsePositiveInt(request.getParameter("showtimeId"));

        // Chi cho dat ghe neu showtime con bookable.
        if (showtimeService.getBookableShowtime(showtimeId) == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("notFound", Boolean.TRUE);
            request.getRequestDispatcher("/pages/booking/seats.jsp").forward(request, response);
            return;
        }

        /*
         * Browser Back tu F&B khong dong nghia customer muon bo ghe cu. Neu
         * draft cua session dang giu ghe cho dung showtime, bo qua dung cart
         * do va tick san ghe cu de customer co the bam Tiep tuc ngay.
         */
        BookingDraft current = currentDraft(request);
        int ownCartId = current != null && current.getShowtimeId() == showtimeId
                ? current.getCartId() : -1;
        SeatMap seatMap = cinemaService.getSeatMap(showtimeId, ownCartId);

        // seatMap de JSP ve ghe, bookingMode de JSP biet day la man dat ve.
        request.setAttribute("seatMap", seatMap);
        request.setAttribute("bookingMode", Boolean.TRUE);
        if (current != null && ownCartId > 0) {
            request.setAttribute("selectedSeatIds", current.getSeatIds());
        }

        // error co the null; neu co thi JSP hien loi validate ghe.
        request.setAttribute("error", error);

        dao.SeatTypeDAO seatTypeDAO = new dao.SeatTypeDAO();
        request.setAttribute("allSeatTypes", seatTypeDAO.findAll());

        request.getRequestDispatcher("/pages/booking/seats.jsp").forward(request, response);
    }

    /**
     * Xu ly viec quay lai man chon ghe tu buoc F&B.
     *
     * <p>Customer da chon mot tap ghe thi CART_ITEMS dang khoa tap ghe do. Neu
     * muon doi ghe, lock cu phai duoc giai phong truoc khi ve so do; neu khong
     * giao dien se tu coi cac ghe cua chinh customer la da dat. Chi khi URL co
     * {@code changeSeats=1}. Browser Back lai chi gui URL /booking/seats cu,
     * nen method cung nhan ra draft co cung showtime va xu ly nhu mot yeu cau
     * doi ghe. Reload o F&B/confirm khong di qua method nay nen lock van giu
     * nguyen trong luc customer dang tiep tuc thanh toan.</p>
     */
    private void releaseHoldWhenChangingSeats(User user, HttpServletRequest request) {
        if (!"1".equals(request.getParameter("changeSeats"))) {
            return;
        }
        BookingDraft draft = currentDraft(request);

        /*
         * Browser Back quay tu /booking/fnb ve dung URL /booking/seats cu,
         * khong mang theo changeSeats=1. Vi vay khong duoc chi dua vao nut
         * "Quay lại chọn ghế" cua JSP: bat cu GET nao quay lai dung seat map
         * cua cart hien tai deu duoc xem la y dinh chon lai ghe.
         */
        if (draft != null && draft.getCartId() > 0) {
            bookingService.releaseOnlineSeatHold(user.getId(), draft.getCartId());
        }
        // Voucher/F&B gan voi draft cu; khi doi ghe phai tinh lai tong tien tu server.
        request.getSession().removeAttribute(DRAFT_SESSION_KEY);
        request.getSession().removeAttribute(VOUCHER_SESSION_KEY);
    }

    /**
     * Xử lý bước chọn ghế: validate showtime/seat, sau đó lưu BookingDraft vào
     * session.
     */
    private void submitSeats(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lay suat chieu va danh sach ghe user vua submit.
        int showtimeId = parsePositiveInt(request.getParameter("showtimeId"));
        List<Integer> seatIds = parseSeatIds(request);

        try {
            /*
             * Goi buildDraftView de service validate showtime/seat.
             * Neu ghe khong hop le, service nem IllegalArgumentException.
             */
            BookingDraft oldDraft = currentDraft(request);
            int ownCartId = oldDraft != null && oldDraft.getShowtimeId() == showtimeId
                    ? oldDraft.getCartId() : -1;
            BookingDraftView draftView = bookingService.buildDraftView(showtimeId, seatIds, ownCartId);

            /*
             * Neu session cu da giu ghe, customer dang doi lua chon. Giai phong
             * lock cu truoc khi tao lock moi de khong bo lai cart rac trong DB.
             */
            if (oldDraft != null && oldDraft.getCartId() > 0) {
                bookingService.releaseOnlineSeatHold(user.getId(), oldDraft.getCartId());
            }

            /*
             * Tao CART/CART_ITEMS truoc khi sang F&B. Day la lock tam co TTL,
             * nen staff va customer khac thay ghe da ban trong suot thoi gian
             * customer dang chon F&B va xem xac nhan.
             */
            int cartId = bookingService.holdSeatsForOnlineBooking(user.getId(), draftView);
            if (cartId <= 0) {
                throw new IllegalArgumentException("Ghế vừa được người khác giữ hoặc đặt. Vui lòng chọn ghế khác.");
            }

            // Tao draft nhe gom showtimeId + seatIds, chua ghi booking vao DB.
            BookingDraft draft = new BookingDraft();
            draft.setShowtimeId(showtimeId);
            draft.setSeatIds(seatIds);
            draft.setCartId(cartId);

            // Luu draft vao session de buoc /booking/confirm doc lai.
            request.getSession().setAttribute(DRAFT_SESSION_KEY, draft);

            // Chuyen sang buoc xac nhan thong tin booking.
            response.sendRedirect(request.getContextPath() + "/booking/fnb");
        } catch (IllegalArgumentException e) {
            // Neu validate loi, quay lai man ghe va hien message loi.
            showSeats(request, response, e.getMessage());
        }
    }

    private void showFnb(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        BookingDraft draft = currentDraft(request);
        if (draft == null) {
            response.sendRedirect(request.getContextPath() + "/booking/start");
            return;
        }
        try {
            if (draft.getCartId() <= 0 || !bookingService.refreshOnlineSeatHold(currentUser(request).getId(), draft.getCartId())) {
                throw new IllegalArgumentException("Thời gian giữ ghế đã hết. Vui lòng chọn ghế lại.");
            }
            BookingDraftView view = bookingService.buildDraftView(draft.getShowtimeId(), draft.getSeatIds(), draft.getCartId());
            request.setAttribute("draftView", view);
            request.setAttribute("fnbOptions", bookingFnbDAO.findSellableByBranch(view.getShowtime().getBranchId()));
            request.setAttribute("selectedFnb", draft.getFnbQuantities());
            request.setAttribute("error", error);
            request.getRequestDispatcher("/pages/booking/fnb.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            showSeats(request, response, e.getMessage());
        }
    }

    private void submitFnb(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BookingDraft draft = currentDraft(request);
        if (draft == null) {
            response.sendRedirect(request.getContextPath() + "/booking/start");
            return;
        }
        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (!entry.getKey().startsWith("qty_")) {
                continue;
            }
            String key = entry.getKey().substring(4).replaceFirst("_", ":");
            int qty = parseNonNegativeInt(entry.getValue().length == 0 ? null : entry.getValue()[0]);
            if (qty > 0) {
                quantities.put(key, qty);
            }
        }
        try {
            if (draft.getCartId() <= 0 || !bookingService.refreshOnlineSeatHold(currentUser(request).getId(), draft.getCartId())) {
                throw new IllegalArgumentException("Thời gian giữ ghế đã hết. Vui lòng chọn ghế lại.");
            }
            BookingDraftView view = bookingService.buildDraftView(draft.getShowtimeId(), draft.getSeatIds(), draft.getCartId());
            bookingFnbDAO.resolveSelection(view.getShowtime().getBranchId(), quantities);
            draft.setFnbQuantities(quantities);
            response.sendRedirect(request.getContextPath() + "/booking/confirm");
        } catch (IllegalArgumentException e) {
            showFnb(request, response, e.getMessage());
        }
    }

    /**
     * Bước xác nhận: dựng BookingDraftView từ draft trong session để user kiểm
     * tra lại phim, suất chiếu, ghế và tổng tiền trước khi tạo booking.
     */
    private void showConfirm(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        /*
         * Lay draft tu session.
         * Neu khong co draft nghia la user vao thang /booking/confirm ma chua chon ghe.
         */
        BookingDraft draft = currentDraft(request);
        if (draft == null) {
            response.sendRedirect(request.getContextPath() + "/booking/start");
            return;
        }

        try {
            /*
             * BuildDraftView tao ban hien thi day du cho JSP:
             * phim, rap, phong, ngay gio, ghe, gia tung ghe, tong tien.
             */
            if (draft.getCartId() <= 0 || !bookingService.refreshOnlineSeatHold(currentUser(request).getId(), draft.getCartId())) {
                throw new IllegalArgumentException("Thời gian giữ ghế đã hết. Vui lòng chọn ghế lại.");
            }
            BookingDraftView draftView = bookingService.buildDraftView(
                    draft.getShowtimeId(), draft.getSeatIds(), draft.getCartId());
            draftView.setFnbLines(bookingFnbDAO.resolveSelection(
                    draftView.getShowtime().getBranchId(), draft.getFnbQuantities()));
            // Dua draftView sang JSP confirm de user kiem tra lai truoc khi dat.
            request.setAttribute("draftView", draftView);
            String voucherCode = currentVoucherCode(request);
            if (voucherCode != null) {
                VoucherQuote voucherQuote = bookingService.quoteVoucher(voucherCode, draftView.getTotalPrice() + draftView.getFnbSubtotal());
                if (voucherQuote.isValid()) {
                    draftView.setVoucherDiscount(voucherQuote.getDiscountAmount());
                    request.setAttribute("voucherQuote", voucherQuote);
                } else {
                    request.getSession().removeAttribute(VOUCHER_SESSION_KEY);
                    request.setAttribute("voucherError", voucherQuote.getMessage());
                }
            }
            // error co the null; neu submit confirm loi thi hien lai o man confirm.
            request.setAttribute("error", error);
        } catch (IllegalArgumentException e) {
            /*
             * Draft trong session co the bi cu, vi du ghe vua bi nguoi khac dat.
             * Khi do danh dau draftInvalid de JSP hien canh bao.
             */
            request.setAttribute("draftInvalid", Boolean.TRUE);
            request.setAttribute("error", e.getMessage());
        }

        // forward vi can giu draftView/error trong request.
        request.getRequestDispatcher("/pages/booking/confirm.jsp").forward(request, response);
    }

    /**
     * Xử lý xác nhận booking: tạo booking pending trong DB, xóa draft khỏi
     * session và chuyển user sang trang chi tiết booking để theo dõi/thanh
     * toán.
     */
    private void submitConfirm(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lay lai draft da luu sau khi user chon ghe.
        BookingDraft draft = currentDraft(request);
        if (draft == null) {
            // Khong co draft thi khong co gi de tao booking.
            response.sendRedirect(request.getContextPath() + "/booking/start");
            return;
        }

        try {
            /*
             * Xac nhan lan cuoi chi duoc phep neu cart cua session van con han.
             * Khong bo qua buoc nay: buildDraftView co bo qua cart cua chinh user
             * de hien thi UI, con DAO se dung cart nay de chuyen doi nguyen tu.
             */
            if (draft.getCartId() <= 0
                    || !bookingService.refreshOnlineSeatHold(user.getId(), draft.getCartId())) {
                throw new IllegalArgumentException("Thời gian giữ ghế đã hết. Vui lòng chọn ghế lại.");
            }
            // Validate lai draft lan cuoi truoc khi ghi DB.
            BookingDraftView draftView = bookingService.buildDraftView(
                    draft.getShowtimeId(), draft.getSeatIds(), draft.getCartId());
            draftView.setFnbLines(bookingFnbDAO.resolveSelection(
                    draftView.getShowtime().getBranchId(), draft.getFnbQuantities()));
            String requestedAction = request.getParameter("action");
            String submittedVoucher = request.getParameter("voucherCode");
            if ("removeVoucher".equals(requestedAction)) {
                request.getSession().removeAttribute(VOUCHER_SESSION_KEY);
                showConfirm(request, response, null);
                return;
            }
            if ("applyVoucher".equals(requestedAction)) {
                if (submittedVoucher == null || submittedVoucher.trim().isEmpty()) {
                    request.getSession().removeAttribute(VOUCHER_SESSION_KEY);
                    showConfirm(request, response, "Vui lòng nhập mã giảm giá.");
                    return;
                }
                VoucherQuote voucherQuote = bookingService.quoteVoucher(submittedVoucher, draftView.getTotalPrice() + draftView.getFnbSubtotal());
                if (!voucherQuote.isValid()) {
                    showConfirm(request, response, voucherQuote.getMessage());
                    return;
                }
                request.getSession().setAttribute(VOUCHER_SESSION_KEY, voucherQuote.getCode());
                showConfirm(request, response, null);
                return;
            }

            VoucherQuote voucherQuote = null;
            String voucherCode = currentVoucherCode(request);
            if (voucherCode != null) {
                voucherQuote = bookingService.quoteVoucher(voucherCode, draftView.getTotalPrice() + draftView.getFnbSubtotal());
                if (!voucherQuote.isValid()) {
                    request.getSession().removeAttribute(VOUCHER_SESSION_KEY);
                    showConfirm(request, response, voucherQuote.getMessage());
                    return;
                }
            }
            if (voucherQuote != null && voucherQuote.isValid()) {
                draftView.setVoucherDiscount(voucherQuote.getDiscountAmount());
            }
            /*
             * Tao booking pending trong DB.
             * Service se tao BOOKING, BOOKING_SEATS va PAYMENT pending neu thanh cong.
             */
            int bookingId = bookingService.createPendingBooking(
                    user.getId(), draftView, voucherQuote, draft.getCartId());
            if (bookingId <= 0) {
                showConfirm(request, response, "Không thể tạo booking. Ghế có thể vừa được người khác đặt.");
                return;
            }

            // Tao booking xong thi xoa draft de user khong submit lai booking cu.
            request.getSession().removeAttribute(DRAFT_SESSION_KEY);
            request.getSession().removeAttribute(VOUCHER_SESSION_KEY);
            // Dua user sang trang chi tiet booking de xem QR/thanh toan/theo doi trang thai.
            boolean complimentary = draftView.getTotalPrice() + draftView.getFnbSubtotal()
                    - draftView.getVoucherDiscount() <= 0.0001d;
            response.sendRedirect(request.getContextPath()
                    + "/my-booking?id=" + bookingId + "&msg="
                    + (complimentary ? "booking_free" : "booking_created"));
        } catch (IllegalArgumentException e) {
            // Loi validate thi quay lai man confirm va hien message.
            showConfirm(request, response, e.getMessage());
        }
    }

    private Branch activeBranch(int branchId) {
        // Lay branch theo id tu DB/service.
        Branch branch = branchService.getBranchById(branchId);
        // Branch null hoac khong ACTIVE thi coi nhu khong hop le de booking.
        if (branch == null || !"ACTIVE".equalsIgnoreCase(branch.getStatus())) {
            return null;
        }
        return branch;
    }

    private BookingDraft currentDraft(HttpServletRequest request) {
        /*
         * getSession(false) de khong tao session moi khi user chua co session.
         * Neu tao session moi o day thi van khong co draft va gay kho debug.
         */
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        // Lay object bookingDraft trong session va ep kieu an toan.
        Object value = session.getAttribute(DRAFT_SESSION_KEY);
        return value instanceof BookingDraft ? (BookingDraft) value : null;
    }

    private String currentVoucherCode(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(VOUCHER_SESSION_KEY);
        return value instanceof String && !((String) value).trim().isEmpty()
                ? ((String) value).trim() : null;
    }

    private User currentUser(HttpServletRequest request) {
        // Lay session hien co; false nghia la khong tao session moi.
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        // AuthController luu user dang nhap trong attribute "user".
        Object value = session.getAttribute("user");
        return value instanceof User ? (User) value : null;
    }

    private int parsePositiveInt(String value) {
        // Neu thieu/rong thi tra 0, coi nhu khong hop le.
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            // trim de bo khoang trang truoc/sau truoc khi parse.
            int parsed = Integer.parseInt(value.trim());
            // Chi chap nhan so duong, id <= 0 bi coi la khong hop le.
            return parsed > 0 ? parsed : 0;
        } catch (NumberFormatException e) {
            // Chuoi khong phai so thi khong nem loi 500, tra 0 de controller xu ly.
            return 0;
        }
    }

    private int parseNonNegativeInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate parseDateOrToday(String value) {
        // Neu user khong chon ngay thi mac dinh la ngay hien tai.
        if (value == null || value.trim().isEmpty()) {
            return LocalDate.now();
        }
        try {
            // input type="date" gui format yyyy-MM-dd, LocalDate.parse doc duoc format nay.
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            // Sai format ngay thi quay ve hom nay thay vi nem loi.
            return LocalDate.now();
        }
    }

    private List<Integer> parseSeatIds(HttpServletRequest request) {
        // Danh sach ket qua sau khi loc id hop le va bo trung.
        List<Integer> ids = new ArrayList<>();

        /*
         * Cach 1: form gui nhieu checkbox/input cung ten seatIds.
         * request.getParameterValues("seatIds") se tra ve mang String.
         */
        String[] values = request.getParameterValues("seatIds");
        if (values != null) {
            for (String value : values) {
                // Ep tung value ve id duong.
                int id = parsePositiveInt(value);
                if (id > 0 && !ids.contains(id)) {
                    // Chi them id hop le va chua ton tai de tranh trung ghe.
                    ids.add(id);
                }
            }
            return ids;
        }

        /*
         * Cach 2: frontend gui mot chuoi "1,2,3" trong hidden input selectedSeats.
         * Doan nay de ho tro UI custom khong dung checkbox truyen thong.
         */
        String selectedSeats = request.getParameter("selectedSeats");
        if (selectedSeats != null) {
            for (String part : selectedSeats.split(",")) {
                int id = parsePositiveInt(part);
                if (id > 0 && !ids.contains(id)) {
                    // Them id hop le sau khi tach chuoi bang dau phay.
                    ids.add(id);
                }
            }
        }

        // Tra ve list rong neu user chua chon ghe hoac request khong co ghe hop le.
        return ids;
    }
}
