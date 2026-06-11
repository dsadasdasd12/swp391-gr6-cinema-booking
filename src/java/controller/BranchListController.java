/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem chi nhánh rạp (View cinema branches)
 */
package controller;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dto.BranchView;
import service.CinemaService;

/**
 * Hiển thị danh sách hệ thống rạp (các chi nhánh đang hoạt động) kèm địa chỉ,
 * giờ mở cửa và số phòng chiếu.
 * <p>
 * URL: {@code /branches} (GET).
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
@WebServlet(name = "BranchListController", urlPatterns = {"/branches"})
public class BranchListController extends HttpServlet {

    private final CinemaService cinemaService = new CinemaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1) Lấy danh sách chi nhánh đang hoạt động (kèm tên rạp + số phòng)
        List<BranchView> branches = cinemaService.getActiveBranchViews();

        // 2) Đẩy sang view để hiển thị
        request.setAttribute("branches", branches);
        request.getRequestDispatcher("/pages/branch/list.jsp").forward(request, response);
    }
}
