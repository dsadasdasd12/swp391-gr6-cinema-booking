package controller;

import java.io.IOException;
import java.time.LocalTime;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Branch;
import service.BranchService;

/**
 * Controller cho luồng Admin xem và quản lý chi nhánh rạp.
 *
 * <p>
 * Luồng UI:</p>
 * <ul>
 * <li>GET {@code /admin/branches}: lấy danh sách chi nhánh và forward sang
 * {@code branch-list.jsp}.</li>
 * <li>GET {@code /admin/branches/create}: mở form tạo chi nhánh.</li>
 * <li>GET {@code /admin/branches/edit?id=...}: mở form sửa chi nhánh.</li>
 * <li>POST {@code /admin/branches/create}: đọc dữ liệu form, validate ở service
 * và tạo branch.</li>
 * <li>POST {@code /admin/branches/edit}: cập nhật thông tin branch.</li>
 * <li>POST {@code /admin/branches/status}: đổi trạng thái ACTIVE/INACTIVE.</li>
 * <li>POST {@code /admin/branches/delete}: xóa branch nếu không bị ràng buộc dữ
 * liệu.</li>
 * </ul>
 *
 * <p>
 * Ghi chú: route public {@code /branches} đang được JSP nhắc tới nhưng chưa có
 * controller tương ứng trong source hiện tại.</p>
 *
 * @author
 */
@WebServlet(name = "BranchController", urlPatterns = {
    "/admin/branches",
    "/admin/branches/create",
    "/admin/branches/edit",
    "/admin/branches/delete",
    "/admin/branches/status"
})
public class BranchController extends HttpServlet {

    private final BranchService branchService = new BranchService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        switch (path) {
            case "/admin/branches/create":
                showCreateForm(request, response);
                break;

            case "/admin/branches/edit":
                showEditForm(request, response);
                break;

            case "/admin/branches":
            default:
                listBranches(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        switch (path) {
            case "/admin/branches/create":
                createBranch(request, response);
                break;

            case "/admin/branches/edit":
                updateBranch(request, response);
                break;

            case "/admin/branches/delete":
                deleteBranch(request, response);
                break;

            case "/admin/branches/status":
                updateStatus(request, response);
                break;

            default:
                response.sendRedirect(request.getContextPath() + "/admin/branches");
                break;
        }
    }

    private void listBranches(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("branches", branchService.getAllBranches());
        request.getRequestDispatcher("/pages/admin/branch-list.jsp").forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Branch branch = new Branch();
        branch.setCinemaId(1);
        branch.setStatus("ACTIVE");

        request.setAttribute("branch", branch);
        request.setAttribute("formMode", "create");

        request.getRequestDispatcher("/pages/admin/branch-form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseInt(request.getParameter("id"));
        Branch branch = branchService.getBranchById(id);

        if (branch == null) {
            setFlash(request, "error", "Không tìm thấy chi nhánh cần chỉnh sửa.");
            response.sendRedirect(request.getContextPath() + "/admin/branches");
            return;
        }

        request.setAttribute("branch", branch);
        request.setAttribute("formMode", "edit");

        request.getRequestDispatcher("/pages/admin/branch-form.jsp").forward(request, response);
    }

    private void createBranch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Branch branch = buildBranchFromRequest(request);

        try {
            boolean success = branchService.createBranch(branch);

            if (success) {
                setFlash(request, "success", "Thêm chi nhánh mới thành công.");
                response.sendRedirect(request.getContextPath() + "/admin/branches");
                return;
            }

            request.setAttribute("error", "Không thể thêm chi nhánh. Vui lòng thử lại.");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        }

        request.setAttribute("branch", branch);
        request.setAttribute("formMode", "create");

        request.getRequestDispatcher("/pages/admin/branch-form.jsp").forward(request, response);
    }

    private void updateBranch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Branch branch = buildBranchFromRequest(request);
        branch.setId(parseInt(request.getParameter("id")));

        try {
            boolean success = branchService.updateBranch(branch);

            if (success) {
                setFlash(request, "success", "Cập nhật chi nhánh thành công.");
                response.sendRedirect(request.getContextPath() + "/admin/branches");
                return;
            }

            request.setAttribute("error", "Không thể cập nhật chi nhánh. Vui lòng thử lại.");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        }

        request.setAttribute("branch", branch);
        request.setAttribute("formMode", "edit");

        request.getRequestDispatcher("/pages/admin/branch-form.jsp").forward(request, response);
    }

    private void deleteBranch(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = parseInt(request.getParameter("id"));

        boolean success = branchService.deleteBranch(id);

        if (success) {
            setFlash(request, "success", "Xóa chi nhánh thành công.");
        } else {
            setFlash(request, "error",
                    "Không thể xóa chi nhánh. Chi nhánh có thể đang được sử dụng bởi phòng chiếu, nhân viên hoặc suất chiếu.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/branches");
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = parseInt(request.getParameter("id"));
        String status = request.getParameter("status");

        try {
            boolean success = branchService.changeBranchStatus(id, status);

            if (success) {
                setFlash(request, "success", "Cập nhật trạng thái chi nhánh thành công.");
            } else {
                setFlash(request, "error", "Không thể cập nhật trạng thái chi nhánh.");
            }

        } catch (IllegalArgumentException e) {
            setFlash(request, "error", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/branches");
    }

    private Branch buildBranchFromRequest(HttpServletRequest request) {
        Branch branch = new Branch();

        branch.setCinemaId(parseIntWithDefault(request.getParameter("cinemaId"), 1));
        branch.setName(request.getParameter("name"));
        branch.setAddress(request.getParameter("address"));
        branch.setPhone(request.getParameter("phone"));
        branch.setOpenTime(parseTime(request.getParameter("openTime")));
        branch.setCloseTime(parseTime(request.getParameter("closeTime")));
        branch.setStatus(request.getParameter("status"));

        return branch;
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.length() > 5) {
            normalized = normalized.substring(0, 5);
        }

        return LocalTime.parse(normalized);
    }

    private int parseInt(String value) {
        return parseIntWithDefault(value, 0);
    }

    private int parseIntWithDefault(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void setFlash(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession();
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }
}
