package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Hall;
import model.Seat;
import model.User;
import service.HallService;
import service.SeatLayoutService;
import service.SeatService;
import service.UserService;

@WebServlet(name = "SeatConfigController", urlPatterns = "/manager/seat-config")
public class SeatConfigController extends HttpServlet {

    private final SeatService seatService = new SeatService();
    private final SeatLayoutService layoutService = new SeatLayoutService();
    private final HallService hallService = new HallService();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        render(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Chỉ manager đã đăng nhập mới được sửa sơ đồ ghế.
        User user = manager(request, response);
        if (user == null) {
            return;
        }
        int hallId = selectedHallId(request, user.getId());
        try {
            request.setCharacterEncoding("UTF-8");
            // Controller chỉ nhận dữ liệu; SeatLayoutService mới kiểm tra và áp dụng nghiệp vụ.
            String message = layoutService.apply(hallId, request.getParameter("action"), parameters(request));
            request.getSession().setAttribute("msgSuccess", message);
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("msgError", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/manager/seat-config?hallId=" + hallId);
    }

    private void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = manager(request, response);
        if (user == null) {
            return;
        }
        int hallId = selectedHallId(request, user.getId());
        // Hall chỉ được lấy từ chi nhánh của manager, không tin hallId tùy ý trên URL.
        List<Seat> seats = hallId > 0 ? seatService.getSeatsByHall(hallId) : List.of();
        int max = 8;
        for (Seat seat : seats) {
            max = Math.max(max, seat.getSeatNumber());
        }
        request.setAttribute("seatList", seats);
        request.setAttribute("hallList", halls(user.getId()));
        request.setAttribute("currentHallId", hallId);
        request.setAttribute("maxSeatNumber", max);
        request.setAttribute("activeSeatTypes", seatService.getActiveSeatTypes());
        request.setAttribute("allSeatTypes", seatService.getAllSeatTypes());
        request.setAttribute("isLocked", hallId > 0 && new dao.ShowtimeDAO().hasFutureShowtimes(hallId));
        request.getRequestDispatcher("/pages/manager/seatConfig.jsp").forward(request, response);
    }

    private User manager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object current = session == null ? null : session.getAttribute("user");
        if (!(current instanceof User)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        User user = (User) current;
        // Phân quyền giao diện: không cho staff/admin dùng màn hình cấu hình sơ đồ ghế.
        if (!"MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/home");
            return null;
        }
        return user;
    }

    private List<Hall> halls(int managerId) {
        // UserService lấy chi nhánh được phân công của manager từ STAFF_BRANCH.
        return hallService.getHallsByBranchId(userService.getBranchIdOfStaff(managerId));
    }

    private int selectedHallId(HttpServletRequest request, int managerId) {
        List<Hall> halls = halls(managerId);
        int requested = parseId(request.getParameter("hallId"));
        // Chỉ chấp nhận hallId nằm trong danh sách hall mà manager được phép quản lý.
        for (Hall hall : halls) {
            if (hall.getId() == requested) {
                return requested;
            }
        }
        return halls.isEmpty() ? 0 : halls.get(0).getId();
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private Map<String, String> parameters(HttpServletRequest request) {
        // Chỉ chuyển tiếp các trường được SeatLayoutService cho phép xử lý.
        Map<String, String> values = new HashMap<>();
        for (String key : new String[]{"bulkType", "bulkSeatType", "bulkStatus", "bulkRow", "bulkSeatCount", "bulkCol", "bulkRowStart", "bulkRowEnd", "seatCode", "seatType", "status"}) {
            values.put(key, request.getParameter(key));
        }
        return values;
    }
}
