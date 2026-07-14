package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dto.SeatMap;
import service.CinemaService;

/**
 * Controller cho luồng xem tình trạng ghế của một suất chiếu.
 *
 * <p>Luồng UI:</p>
 * <ul>
 *   <li>User mở GET {@code /seats?showtimeId=...} từ danh sách suất chiếu.</li>
 *   <li>Controller đọc showtimeId và gọi {@link CinemaService#getSeatMap(int)}.</li>
 *   <li>Nếu showtime không hợp lệ, trả status 404 và render trang ghế ở trạng thái not found.</li>
 *   <li>Nếu hợp lệ, forward {@code seatMap} sang {@code /pages/seat/availability.jsp}.</li>
 * </ul>
 *
 * @author HuyPD
 */
@WebServlet(name = "SeatAvailabilityController", urlPatterns = {"/seats"})
public class SeatAvailabilityController extends HttpServlet {

    /*
     * CinemaService tra ve du lieu tong hop cho man hinh rap/phong/ghe.
     * O day controller can SeatMap nen chi goi service, khong tu ghep du lieu tu DAO.
     */
    private final CinemaService cinemaService = new CinemaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * showtimeId nam tren URL, vi du: /seats?showtimeId=10.
         * parseId tra -1 neu thieu/sai dinh dang de service khong nhan gia tri rac.
         */
        int showtimeId = parseId(request.getParameter("showtimeId"));

        /*
         * Lay so do ghe cua suat chieu.
         * SeatMap thuong gom thong tin phim, phong, danh sach ghe va trang thai ghe.
         */
        SeatMap seatMap = cinemaService.getSeatMap(showtimeId);

        if (seatMap == null) {
            /*
             * Neu khong tim thay showtime, tra HTTP 404.
             * Van forward sang JSP de hien trang loi than thien thay vi trang loi mac dinh cua Tomcat.
             */
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("notFound", Boolean.TRUE);
            request.getRequestDispatcher("/pages/seat/availability.jsp").forward(request, response);
            return;
        }

        // Dua SeatMap sang JSP de JSP lap qua danh sach ghe va ve so do ghe.
        request.setAttribute("seatMap", seatMap);
        // forward sang view; khong redirect vi can giu attribute "seatMap" trong request.
        dao.SeatTypeDAO seatTypeDAO = new dao.SeatTypeDAO();
        request.setAttribute("allSeatTypes", seatTypeDAO.findAll());
        request.getRequestDispatcher("/pages/seat/availability.jsp").forward(request, response);
    }

    /**
     * Doc id tu tham so request.
     * Return -1 khi value null, rong hoac khong parse duoc thanh so.
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
