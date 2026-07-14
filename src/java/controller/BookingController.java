package controller;



import dto.BookingDraft;
import dto.BookingDraftView;
import dto.SeatMap;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Branch;
import model.Movie;
import model.User;
import service.BookingService;
import service.BranchService;
import service.CinemaService;
import service.MovieService;
import service.ShowtimeService;

/**
 * Controller điều phối toàn bộ luồng đặt vé online của khách hàng.
 *
 * <p>Luồng đặt vé chính:</p>
 * <ul>
 *   <li>GET {@code /booking/start}: hiển thị danh sách chi nhánh active để user chọn branch.</li>
 *   <li>GET {@code /booking/movies?branchId=...}: hiển thị phim có thể đặt tại chi nhánh.</li>
 *   <li>GET {@code /booking/showtimes?branchId=...&movieId=...&date=...}: hiển thị suất chiếu.</li>
 *   <li>GET {@code /booking/seats?showtimeId=...}: hiển thị sơ đồ ghế và trạng thái ghế.</li>
 *   <li>POST {@code /booking/seats}: validate ghế, tạo {@link BookingDraft} trong session.</li>
 *   <li>GET {@code /booking/confirm}: dựng {@link BookingDraftView} để user kiểm tra booking.</li>
 *   <li>POST {@code /booking/confirm}: tạo booking pending, xóa draft, chuyển sang chi tiết booking.</li>
 * </ul>
 *
 * <p>Ghi chú: mọi route booking đều yêu cầu user đã đăng nhập. Nếu chưa đăng nhập,
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
    "/booking/confirm"
})
public class BookingController extends HttpServlet {


    /*
     * Key dung de luu booking tam trong session.
     * Draft chi ton tai trong luc user dang chon ghe va chua bam xac nhan cuoi.
     */
    private static final String DRAFT_SESSION_KEY = "bookingDraft";

    /*
     * Cac service ben duoi dai dien cho cac nhom nghiep vu khac nhau.
     * Controller chi dieu huong request, validate co ban, roi goi service phu hop.
     */
    private final CinemaService cinemaService = new CinemaService();
    private final BranchService branchService = new BranchService();
    private final MovieService movieService = new MovieService();
    private final ShowtimeService showtimeService = new ShowtimeService();
    private final BookingService bookingService = new BookingService();

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
                showSeats(request, response, null);
                break;
            case "/booking/confirm":
                // Buoc xac nhan thong tin booking truoc khi tao booking that trong DB.
                showConfirm(request, response, null);
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
                submitSeats(request, response);
                break;
            case "/booking/confirm":
                // User bam xac nhan tao booking.
                submitConfirm(user, request, response);
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
     * Bước 1 của booking: hiển thị danh sách chi nhánh active để user chọn nơi xem phim.
     */
    private void showStart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lay danh sach chi nhanh dang hoat dong de user chon noi xem phim.
        request.setAttribute("branches", cinemaService.getActiveBranchViews());
        // forward de JSP doc attribute "branches" va render giao dien.
        request.getRequestDispatcher("/pages/booking/start.jsp").forward(request, response);
    }

    /**
     * Bước 2 của booking: sau khi chọn branch, hiển thị các phim có suất chiếu/được phân bổ
     * tại branch đó.
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
     * Bước 3 của booking: sau khi chọn phim, hiển thị các suất chiếu bookable theo ngày.
     */
    private void showShowtimes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Doc cac tham so user da chon tu URL.
        int branchId = parsePositiveInt(request.getParameter("branchId"));
        int movieId = parsePositiveInt(request.getParameter("movieId"));
        LocalDate date = parseDateOrToday(request.getParameter("date"));

        // Kiem tra branch va movie co hop le khong truoc khi lay suat chieu.
        Branch branch = activeBranch(branchId);
        Movie movie = movieService.getMovieDetail(movieId);
        if (branch == null || movie == null) {
            // Du lieu sai thi dua ve dau luong booking.
            response.sendRedirect(request.getContextPath() + "/booking/start");
            return;
        }

        // Gui thong tin bo loc va ket qua sang JSP chon suat chieu.
        request.setAttribute("branch", branch);
        request.setAttribute("movie", movie);
        request.setAttribute("selectedDate", date);
        request.setAttribute("showtimes", showtimeService.getBookableShowtimes(branchId, movieId, date));
        request.getRequestDispatcher("/pages/booking/select-showtime.jsp").forward(request, response);
    }

    /**
     * Bước 4 của booking: hiển thị sơ đồ ghế của suất chiếu và trạng thái từng ghế.
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

        // Lay so do ghe kem trang thai da dat/bao tri/gia ghe.
        SeatMap seatMap = cinemaService.getSeatMap(showtimeId);

        // seatMap de JSP ve ghe, bookingMode de JSP biet day la man dat ve.
        request.setAttribute("seatMap", seatMap);
        request.setAttribute("bookingMode", Boolean.TRUE);

        // error co the null; neu co thi JSP hien loi validate ghe.
        request.setAttribute("error", error);
        
        dao.SeatTypeDAO seatTypeDAO = new dao.SeatTypeDAO();
        request.setAttribute("allSeatTypes", seatTypeDAO.findAll());
        
        request.getRequestDispatcher("/pages/booking/seats.jsp").forward(request, response);
    }

    /**
     * Xử lý bước chọn ghế: validate showtime/seat, sau đó lưu BookingDraft vào session.
     */
    private void submitSeats(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lay suat chieu va danh sach ghe user vua submit.
        int showtimeId = parsePositiveInt(request.getParameter("showtimeId"));
        List<Integer> seatIds = parseSeatIds(request);

        try {
            /*
             * Goi buildDraftView de service validate showtime/seat.
             * Neu ghe khong hop le, service nem IllegalArgumentException.
             */
            bookingService.buildDraftView(showtimeId, seatIds);

            // Tao draft nhe gom showtimeId + seatIds, chua ghi booking vao DB.
            BookingDraft draft = new BookingDraft();
            draft.setShowtimeId(showtimeId);
            draft.setSeatIds(seatIds);

            // Luu draft vao session de buoc /booking/confirm doc lai.
            request.getSession().setAttribute(DRAFT_SESSION_KEY, draft);

            // Chuyen sang buoc xac nhan thong tin booking.
            response.sendRedirect(request.getContextPath() + "/booking/confirm");
        } catch (IllegalArgumentException e) {
            // Neu validate loi, quay lai man ghe va hien message loi.
            showSeats(request, response, e.getMessage());
        }
    }

    /**
     * Bước xác nhận: dựng BookingDraftView từ draft trong session để user kiểm tra lại
     * phim, suất chiếu, ghế và tổng tiền trước khi tạo booking.
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
            BookingDraftView draftView = bookingService.buildDraftView(
                    draft.getShowtimeId(),
                    draft.getSeatIds()
            );
            // Dua draftView sang JSP confirm de user kiem tra lai truoc khi dat.
            request.setAttribute("draftView", draftView);
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
     * Xử lý xác nhận booking: tạo booking pending trong DB, xóa draft khỏi session và
     * chuyển user sang trang chi tiết booking để theo dõi/thanh toán.
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
            // Validate lai draft lan cuoi truoc khi ghi DB.
            BookingDraftView draftView = bookingService.buildDraftView(
                    draft.getShowtimeId(),
                    draft.getSeatIds()
            );
            /*
             * Tao booking pending trong DB.
             * Service se tao BOOKING, BOOKING_SEATS va PAYMENT pending neu thanh cong.
             */
            int bookingId = bookingService.createPendingBooking(user.getId(), draftView);
            if (bookingId <= 0) {
                showConfirm(request, response, "Không thể tạo booking. Ghế có thể vừa được người khác đặt.");
                return;
            }

            // Tao booking xong thi xoa draft de user khong submit lai booking cu.
            request.getSession().removeAttribute(DRAFT_SESSION_KEY);
            // Dua user sang trang chi tiet booking de xem QR/thanh toan/theo doi trang thai.
            response.sendRedirect(request.getContextPath()
                    + "/my-booking?id=" + bookingId + "&msg=booking_created");
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
