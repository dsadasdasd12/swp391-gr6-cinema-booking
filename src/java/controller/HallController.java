/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import dao.StaffBranchDAO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Branch;
import model.Hall;
import model.User;
import service.HallService;

@WebServlet(name = "HallController", urlPatterns = {
    "/manager/halls",
    "/manager/halls/create",
    "/manager/halls/edit",
    "/manager/halls/delete",
    "/manager/halls/status"
})
public class HallController extends HttpServlet {

    private static final String HALL_LIST_PAGE = "/pages/manager/hall-list.jsp";
    private static final String HALL_FORM_PAGE = "/pages/manager/hall-form.jsp";

    private final HallService hallService = new HallService();
    private final StaffBranchDAO staffBranchDAO = new StaffBranchDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/manager/halls/create":
                showCreateForm(request, response);
                break;

            case "/manager/halls/edit":
                showEditForm(request, response);
                break;

            case "/manager/halls":
            default:
                listHalls(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        switch (path) {
            case "/manager/halls/create":
                createHall(request, response);
                break;

            case "/manager/halls/edit":
                updateHall(request, response);
                break;

            case "/manager/halls/delete":
                deleteHall(request, response);
                break;

            case "/manager/halls/status":
                updateStatus(request, response);
                break;

            default:
                response.sendRedirect(request.getContextPath() + "/manager/halls");
                break;
        }
    }

    private void listHalls(HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {

    User user = getCurrentManager(request, response);

    if (user == null) {
        return;
    }

    Branch branch = staffBranchDAO.findBranchByManagerId(user.getId());

    List<Hall> halls = new ArrayList<>();

    if (branch != null) {
        halls = hallService.getHallsByBranchId(branch.getId());
    }

    request.setAttribute("branch", branch);
    request.setAttribute("halls", halls);

    request.getRequestDispatcher(HALL_LIST_PAGE).forward(request, response);
}

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        int branchId = parseInt(request.getParameter("branchId"));

        if (!isAllowedBranch(user.getId(), branchId)) {
            setFlash(request, "error", "Bạn không có quyền thêm phòng chiếu cho chi nhánh này.");
            response.sendRedirect(request.getContextPath() + "/manager/halls");
            return;
        }

        Branch branch = findBranchForManager(user.getId(), branchId);

        Hall hall = new Hall();
        hall.setBranchId(branchId);
        hall.setBranchName(branch.getName());
        hall.setHallType("STANDARD");
        hall.setStatus("ACTIVE");

        request.setAttribute("hall", hall);
        request.setAttribute("branch", branch);
        request.setAttribute("formMode", "create");

        request.getRequestDispatcher(HALL_FORM_PAGE).forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        int id = parseInt(request.getParameter("id"));
        int branchId = parseInt(request.getParameter("branchId"));

        if (!isAllowedBranch(user.getId(), branchId)) {
            setFlash(request, "error", "Bạn không có quyền sửa phòng chiếu của chi nhánh này.");
            response.sendRedirect(request.getContextPath() + "/manager/halls");
            return;
        }

        Branch branch = findBranchForManager(user.getId(), branchId);
        Hall hall = hallService.getHallByIdAndBranchId(id, branchId);

        if (hall == null) {
            setFlash(request, "error", "Không tìm thấy phòng chiếu trong chi nhánh này.");
            response.sendRedirect(request.getContextPath() + "/manager/halls");
            return;
        }

        request.setAttribute("hall", hall);
        request.setAttribute("branch", branch);
        request.setAttribute("formMode", "edit");

        request.getRequestDispatcher(HALL_FORM_PAGE).forward(request, response);
    }

    private void createHall(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        int branchId = parseInt(request.getParameter("branchId"));

        if (!isAllowedBranch(user.getId(), branchId)) {
            setFlash(request, "error", "Bạn không có quyền thêm phòng chiếu cho chi nhánh này.");
            response.sendRedirect(request.getContextPath() + "/manager/halls");
            return;
        }

        Branch branch = findBranchForManager(user.getId(), branchId);
        Hall hall = buildHallFromRequest(request);
        hall.setBranchId(branchId);
        hall.setBranchName(branch.getName());

        try {
            boolean success = hallService.createHall(hall);

            if (success) {
                setFlash(request, "success", "Thêm phòng chiếu mới thành công.");
                response.sendRedirect(request.getContextPath() + "/manager/halls");
                return;
            }

            request.setAttribute("error", "Không thể thêm phòng chiếu. Vui lòng thử lại.");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        }

        request.setAttribute("hall", hall);
        request.setAttribute("branch", branch);
        request.setAttribute("formMode", "create");

        request.getRequestDispatcher(HALL_FORM_PAGE).forward(request, response);
    }

    private void updateHall(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        int branchId = parseInt(request.getParameter("branchId"));

        if (!isAllowedBranch(user.getId(), branchId)) {
            setFlash(request, "error", "Bạn không có quyền cập nhật phòng chiếu của chi nhánh này.");
            response.sendRedirect(request.getContextPath() + "/manager/halls");
            return;
        }

        Branch branch = findBranchForManager(user.getId(), branchId);
        Hall hall = buildHallFromRequest(request);
        hall.setId(parseInt(request.getParameter("id")));
        hall.setBranchId(branchId);
        hall.setBranchName(branch.getName());

        try {
            boolean success = hallService.updateHall(hall);

            if (success) {
                setFlash(request, "success", "Cập nhật phòng chiếu thành công.");
                response.sendRedirect(request.getContextPath() + "/manager/halls");
                return;
            }

            request.setAttribute("error", "Không thể cập nhật phòng chiếu. Vui lòng thử lại.");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        }

        request.setAttribute("hall", hall);
        request.setAttribute("branch", branch);
        request.setAttribute("formMode", "edit");

        request.getRequestDispatcher(HALL_FORM_PAGE).forward(request, response);
    }

    private void deleteHall(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        int id = parseInt(request.getParameter("id"));
        int branchId = parseInt(request.getParameter("branchId"));

        if (!isAllowedBranch(user.getId(), branchId)) {
            setFlash(request, "error", "Bạn không có quyền xóa phòng chiếu của chi nhánh này.");
            response.sendRedirect(request.getContextPath() + "/manager/halls");
            return;
        }

        boolean success = hallService.deleteHall(id, branchId);

        if (success) {
            setFlash(request, "success", "Xóa phòng chiếu thành công.");
        } else {
            setFlash(request, "error",
                    "Không thể xóa phòng chiếu. Phòng chiếu có thể đang có ghế hoặc suất chiếu liên quan.");
        }

        response.sendRedirect(request.getContextPath() + "/manager/halls");
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = getCurrentManager(request, response);

        if (user == null) {
            return;
        }

        int id = parseInt(request.getParameter("id"));
        int branchId = parseInt(request.getParameter("branchId"));
        String status = request.getParameter("status");

        if (!isAllowedBranch(user.getId(), branchId)) {
            setFlash(request, "error", "Bạn không có quyền đổi trạng thái phòng chiếu của chi nhánh này.");
            response.sendRedirect(request.getContextPath() + "/manager/halls");
            return;
        }

        boolean success = hallService.changeHallStatus(id, branchId, status);

        if (success) {
            setFlash(request, "success", "Cập nhật trạng thái phòng chiếu thành công.");
        } else {
            setFlash(request, "error", "Không thể cập nhật trạng thái phòng chiếu.");
        }

        response.sendRedirect(request.getContextPath() + "/manager/halls");
    }

    private Hall buildHallFromRequest(HttpServletRequest request) {
        Hall hall = new Hall();

        int seatRows = parseInt(
                request.getParameter("seatRows")
        );

        int seatsPerRow = parseInt(
                request.getParameter("seatsPerRow")
        );

        hall.setName(request.getParameter("name"));
        hall.setSeatRows(seatRows);
        hall.setSeatsPerRow(seatsPerRow);
        hall.setTotalSeats(seatRows * seatsPerRow);
        hall.setHallType(request.getParameter("hallType"));
        hall.setStatus(request.getParameter("status"));

        return hall;
    }

    private User getCurrentManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }

        User user = (User) session.getAttribute("user");

        if (!"MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/home");
            return null;
        }

        return user;
    }

    private boolean isAllowedBranch(int userId, int branchId) {
        if (branchId <= 0) {
            return false;
        }

        return staffBranchDAO.isManagerAssignedToBranch(userId, branchId);
    }

    private Branch findBranchForManager(int userId, int branchId) {
        List<Branch> branches = staffBranchDAO.findBranchesByUserId(userId);

        for (Branch branch : branches) {
            if (branch.getId() == branchId) {
                return branch;
            }
        }

        return null;
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
