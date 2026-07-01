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

@WebServlet(name = "BookingController", urlPatterns = {
    "/booking",
    "/booking/start",
    "/booking/movies",
    "/booking/showtimes",
    "/booking/seats",
    "/booking/confirm"
})
public class BookingController extends HttpServlet {

    private static final String DRAFT_SESSION_KEY = "bookingDraft";

    private final CinemaService cinemaService = new CinemaService();
    private final BranchService branchService = new BranchService();
    private final MovieService movieService = new MovieService();
    private final ShowtimeService showtimeService = new ShowtimeService();
    private final BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();
        switch (path) {
            case "/booking":
                redirectLegacyBooking(request, response);
                break;
            case "/booking/movies":
                showMovies(request, response);
                break;
            case "/booking/showtimes":
                showShowtimes(request, response);
                break;
            case "/booking/seats":
                showSeats(request, response, null);
                break;
            case "/booking/confirm":
                showConfirm(request, response, null);
                break;
            case "/booking/start":
            default:
                showStart(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();
        switch (path) {
            case "/booking/seats":
                submitSeats(request, response);
                break;
            case "/booking/confirm":
                submitConfirm(user, request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/booking/start");
                break;
        }
    }

    private void redirectLegacyBooking(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int showtimeId = parsePositiveInt(request.getParameter("showtimeId"));
        if (showtimeId > 0) {
            response.sendRedirect(request.getContextPath() + "/booking/seats?showtimeId=" + showtimeId);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/booking/start");
    }

    private void showStart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("branches", cinemaService.getActiveBranchViews());
        request.getRequestDispatcher("/pages/booking/start.jsp").forward(request, response);
    }

    private void showMovies(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int branchId = parsePositiveInt(request.getParameter("branchId"));
        Branch branch = activeBranch(branchId);
        if (branch == null) {
            response.sendRedirect(request.getContextPath() + "/booking/start");
            return;
        }

        request.setAttribute("branch", branch);
        request.setAttribute("movies", movieService.getBookableMoviesByBranch(branchId));
        request.getRequestDispatcher("/pages/booking/select-movie.jsp").forward(request, response);
    }

    private void showShowtimes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int branchId = parsePositiveInt(request.getParameter("branchId"));
        int movieId = parsePositiveInt(request.getParameter("movieId"));
        LocalDate date = parseDateOrToday(request.getParameter("date"));

        Branch branch = activeBranch(branchId);
        Movie movie = movieService.getMovieDetail(movieId);
        if (branch == null || movie == null) {
            response.sendRedirect(request.getContextPath() + "/booking/start");
            return;
        }

        request.setAttribute("branch", branch);
        request.setAttribute("movie", movie);
        request.setAttribute("selectedDate", date);
        request.setAttribute("showtimes", showtimeService.getBookableShowtimes(branchId, movieId, date));
        request.getRequestDispatcher("/pages/booking/select-showtime.jsp").forward(request, response);
    }

    private void showSeats(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        int showtimeId = parsePositiveInt(request.getParameter("showtimeId"));
        if (showtimeService.getBookableShowtime(showtimeId) == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("notFound", Boolean.TRUE);
            request.getRequestDispatcher("/pages/booking/seats.jsp").forward(request, response);
            return;
        }

        SeatMap seatMap = cinemaService.getSeatMap(showtimeId);
        request.setAttribute("seatMap", seatMap);
        request.setAttribute("bookingMode", Boolean.TRUE);
        request.setAttribute("error", error);
        request.getRequestDispatcher("/pages/booking/seats.jsp").forward(request, response);
    }

    private void submitSeats(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int showtimeId = parsePositiveInt(request.getParameter("showtimeId"));
        List<Integer> seatIds = parseSeatIds(request);

        try {
            bookingService.buildDraftView(showtimeId, seatIds);

            BookingDraft draft = new BookingDraft();
            draft.setShowtimeId(showtimeId);
            draft.setSeatIds(seatIds);
            request.getSession().setAttribute(DRAFT_SESSION_KEY, draft);

            response.sendRedirect(request.getContextPath() + "/booking/confirm");
        } catch (IllegalArgumentException e) {
            showSeats(request, response, e.getMessage());
        }
    }

    private void showConfirm(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        BookingDraft draft = currentDraft(request);
        if (draft == null) {
            response.sendRedirect(request.getContextPath() + "/booking/start");
            return;
        }

        try {
            BookingDraftView draftView = bookingService.buildDraftView(
                    draft.getShowtimeId(),
                    draft.getSeatIds()
            );
            request.setAttribute("draftView", draftView);
            request.setAttribute("error", error);
        } catch (IllegalArgumentException e) {
            request.setAttribute("draftInvalid", Boolean.TRUE);
            request.setAttribute("error", e.getMessage());
        }

        request.getRequestDispatcher("/pages/booking/confirm.jsp").forward(request, response);
    }

    private void submitConfirm(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BookingDraft draft = currentDraft(request);
        if (draft == null) {
            response.sendRedirect(request.getContextPath() + "/booking/start");
            return;
        }

        try {
            BookingDraftView draftView = bookingService.buildDraftView(
                    draft.getShowtimeId(),
                    draft.getSeatIds()
            );
            int bookingId = bookingService.createPendingBooking(user.getId(), draftView);
            if (bookingId <= 0) {
                showConfirm(request, response, "Không thể tạo booking. Ghế có thể vừa được người khác đặt.");
                return;
            }

            request.getSession().removeAttribute(DRAFT_SESSION_KEY);
            response.sendRedirect(request.getContextPath()
                    + "/my-booking?id=" + bookingId + "&msg=booking_created");
        } catch (IllegalArgumentException e) {
            showConfirm(request, response, e.getMessage());
        }
    }

    private Branch activeBranch(int branchId) {
        Branch branch = branchService.getBranchById(branchId);
        if (branch == null || !"ACTIVE".equalsIgnoreCase(branch.getStatus())) {
            return null;
        }
        return branch;
    }

    private BookingDraft currentDraft(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(DRAFT_SESSION_KEY);
        return value instanceof BookingDraft ? (BookingDraft) value : null;
    }

    private User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("user");
        return value instanceof User ? (User) value : null;
    }

    private int parsePositiveInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate parseDateOrToday(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private List<Integer> parseSeatIds(HttpServletRequest request) {
        List<Integer> ids = new ArrayList<>();

        String[] values = request.getParameterValues("seatIds");
        if (values != null) {
            for (String value : values) {
                int id = parsePositiveInt(value);
                if (id > 0 && !ids.contains(id)) {
                    ids.add(id);
                }
            }
            return ids;
        }

        String selectedSeats = request.getParameter("selectedSeats");
        if (selectedSeats != null) {
            for (String part : selectedSeats.split(",")) {
                int id = parsePositiveInt(part);
                if (id > 0 && !ids.contains(id)) {
                    ids.add(id);
                }
            }
        }

        return ids;
    }
}
