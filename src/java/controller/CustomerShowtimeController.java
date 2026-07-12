package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Branch;
import service.BranchService;
import service.ShowtimeService;

/**
 * Controller cho luồng khách xem lịch chiếu theo chi nhánh và ngày.
 *
 * <p>Luồng UI:</p>
 * <ul>
 *   <li>User mở GET {@code /showtimes}.</li>
 *   <li>Nếu có {@code branchId} và {@code date}, controller dùng các giá trị đó để lọc.</li>
 *   <li>Nếu thiếu {@code branchId}, controller tự chọn chi nhánh active đầu tiên.</li>
 *   <li>Gọi {@link ShowtimeService#getMovieShowtimesByBranchAndDate(int, LocalDate)}
 *       để gom suất chiếu theo phim.</li>
 *   <li>Forward sang {@code /pages/showtimes/list.jsp} để user chọn suất chiếu.</li>
 * </ul>
 *
 * @author HuyPD
 */
@WebServlet("/showtimes")
public class CustomerShowtimeController extends HttpServlet {

    /*
     * BranchService phu trach lay danh sach chi nhanh.
     * ShowtimeService phu trach lay danh sach suat chieu.
     */
    private final BranchService branchService = new BranchService();
    private final ShowtimeService showtimeService = new ShowtimeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /*
         * Lay tat ca chi nhanh ACTIVE de hien trong dropdown/tab cho user chon.
         * Chi nhanh inactive khong nen hien cho khach dat ve.
         */
        List<Branch> branches = activeBranches();

        // Doc branchId tu URL: /showtimes?branchId=...
        int branchId = parseId(request.getParameter("branchId"));

        // Doc date tu URL; neu khong co hoac sai format yyyy-MM-dd thi mac dinh hom nay.
        LocalDate date = parseDateOrToday(request.getParameter("date"));
        /*
         * Neu user chua chon chi nhanh, tu chon chi nhanh active dau tien.
         * Cach nay giup trang /showtimes van co du lieu ngay ca khi URL khong co branchId.
         */
        if (branchId <= 0 && !branches.isEmpty()) {
            branchId = branches.get(0).getId();
        }

        // Gui danh sach chi nhanh sang JSP de render bo loc chi nhanh.
        request.setAttribute("branches", branches);
        // Gui branch dang duoc chon de JSP highlight/selected dung option.
        request.setAttribute("selectedBranchId", branchId);
        // Gui ngay dang duoc chon de input date hien dung gia tri.
        request.setAttribute("selectedDate", date);
        /*
         * Lay danh sach phim kem suat chieu theo branch + date.
         * Service se gom nhieu showtime vao tung phim de JSP hien de doc hon.
         */
        request.setAttribute("movieShowtimes", showtimeService.getMovieShowtimesByBranchAndDate(branchId, date));
        // forward sang JSP vi can giu cac attribute vua set o tren.
        request.getRequestDispatcher("/pages/showtimes/list.jsp").forward(request, response);
    }

    private List<Branch> activeBranches() {
        // Tao list moi chi chua cac branch dang ACTIVE.
        List<Branch> result = new ArrayList<>();
        for (Branch branch : branchService.getAllBranches()) {
            if ("ACTIVE".equalsIgnoreCase(branch.getStatus())) {
                result.add(branch);
            }
        }
        return result;
    }

    private int parseId(String value) {
        try {
            // 0 duoc dung nhu gia tri "khong hop le/chua chon".
            return value == null ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate parseDateOrToday(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LocalDate.now();
        }
        try {
            // LocalDate.parse yeu cau format ISO: yyyy-MM-dd, dung voi input type="date".
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}
