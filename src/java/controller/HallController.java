/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import dao.StaffBranchDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
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

        switch (request.getServletPath()) {
            case "/manager/halls/create":
                showCreateForm(request, response);
                break;

            case "/manager/halls/edit":
                showEditForm(request, response);
                break;

            default:
                listHalls(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        switch (request.getServletPath()) {
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

    private void listHalls(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = staffBranchDAO.findBranchByManagerId(manager.getId());

        List<Hall> halls = branch == null
                ? List.of()
                : hallService.getHallsByBranchId(branch.getId());

        request.setAttribute("branch", branch);
        request.setAttribute("halls", halls);

        request.getRequestDispatcher(HALL_LIST_PAGE).forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = getAssignedBranchOrRedirect(manager, request, response);

        if (branch == null) {
            return;
        }

        Hall hall = new Hall();
        hall.setBranchId(branch.getId());
        hall.setBranchName(branch.getName());
        hall.setHallType("STANDARD");
        hall.setStatus("ACTIVE");

        request.setAttribute("branch", branch);
        request.setAttribute("hall", hall);
        request.setAttribute("formMode", "create");

        request.getRequestDispatcher(HALL_FORM_PAGE).forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = getAssignedBranchOrRedirect(manager, request, response);

        if (branch == null) {
            return;
        }

        int hallId = parseInt(request.getParameter("id"));

        Hall hall = hallService.getHallByIdAndBranchId(
                hallId,
                branch.getId()
        );

        if (hall == null) {
            setFlash(
                    request,
                    "error",
                    "Không tìm thấy phòng chiếu thuộc chi nhánh của bạn."
            );

            response.sendRedirect(request.getContextPath() + "/manager/halls");
            return;
        }

        request.setAttribute("branch", branch);
        request.setAttribute("hall", hall);
        request.setAttribute("formMode", "edit");

        request.getRequestDispatcher(HALL_FORM_PAGE).forward(request, response);
    }

    private void createHall(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = getAssignedBranchOrRedirect(manager, request, response);

        if (branch == null) {
            return;
        }

        Hall hall = buildHallFromRequest(request);

        hall.setBranchId(branch.getId());
        hall.setBranchName(branch.getName());

        try {
            if (hallService.createHall(hall)) {
                setFlash(request, "success", "Thêm phòng chiếu mới thành công.");

                response.sendRedirect(request.getContextPath() + "/manager/halls");
                return;
            }

            request.setAttribute(
                    "error",
                    "Không thể thêm phòng chiếu. Vui lòng thử lại."
            );

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        }

        request.setAttribute("branch", branch);
        request.setAttribute("hall", hall);
        request.setAttribute("formMode", "create");

        request.getRequestDispatcher(HALL_FORM_PAGE).forward(request, response);
    }

    private void updateHall(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = getAssignedBranchOrRedirect(manager, request, response);

        if (branch == null) {
            return;
        }

        int hallId = parseInt(request.getParameter("id"));

        Hall existingHall = hallService.getHallByIdAndBranchId(
                hallId,
                branch.getId()
        );

        if (existingHall == null) {
            setFlash(
                    request,
                    "error",
                    "Bạn không có quyền cập nhật phòng chiếu này."
            );

            response.sendRedirect(request.getContextPath() + "/manager/halls");
            return;
        }

        Hall hall = buildHallFromRequest(request);

        hall.setId(hallId);
        hall.setBranchId(branch.getId());
        hall.setBranchName(branch.getName());

        try {
            if (hallService.updateHall(hall)) {
                setFlash(request, "success", "Cập nhật phòng chiếu thành công.");

                response.sendRedirect(request.getContextPath() + "/manager/halls");
                return;
            }

            request.setAttribute(
                    "error",
                    "Không thể cập nhật phòng chiếu. Vui lòng thử lại."
            );

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        }

        request.setAttribute("branch", branch);
        request.setAttribute("hall", hall);
        request.setAttribute("formMode", "edit");

        request.getRequestDispatcher(HALL_FORM_PAGE).forward(request, response);
    }

    private void deleteHall(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = getAssignedBranchOrRedirect(manager, request, response);

        if (branch == null) {
            return;
        }

        int hallId = parseInt(request.getParameter("id"));

        Hall hall = hallService.getHallByIdAndBranchId(
                hallId,
                branch.getId()
        );

        if (hall == null) {
            setFlash(
                    request,
                    "error",
                    "Bạn không có quyền xóa phòng chiếu này."
            );

        } else if (hallService.deleteHall(hallId, branch.getId())) {
            setFlash(request, "success", "Xóa phòng chiếu thành công.");

        } else {
            setFlash(
                    request,
                    "error",
                    "Không thể xóa phòng chiếu. Phòng có thể đang có ghế hoặc suất chiếu liên quan."
            );
        }

        response.sendRedirect(request.getContextPath() + "/manager/halls");
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        Branch branch = getAssignedBranchOrRedirect(manager, request, response);

        if (branch == null) {
            return;
        }

        int hallId = parseInt(request.getParameter("id"));
        String status = request.getParameter("status");

        Hall hall = hallService.getHallByIdAndBranchId(
                hallId,
                branch.getId()
        );

        if (hall == null) {
            setFlash(
                    request,
                    "error",
                    "Bạn không có quyền đổi trạng thái phòng chiếu này."
            );

        } else if (hallService.changeHallStatus(
                hallId,
                branch.getId(),
                status
        )) {
            setFlash(
                    request,
                    "success",
                    "Cập nhật trạng thái phòng chiếu thành công."
            );

        } else {
            setFlash(
                    request,
                    "error",
                    "Không thể cập nhật trạng thái phòng chiếu."
            );
        }

        response.sendRedirect(request.getContextPath() + "/manager/halls");
    }

    private Hall buildHallFromRequest(HttpServletRequest request) {
        Hall hall = new Hall();

        hall.setName(request.getParameter("name"));
        hall.setTotalSeats(parseInt(request.getParameter("totalSeats")));
        hall.setHallType(request.getParameter("hallType"));
        hall.setStatus(request.getParameter("status"));

        return hall;
    }

    private Branch getAssignedBranchOrRedirect(
            User manager,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        Branch branch = staffBranchDAO.findBranchByManagerId(manager.getId());

        if (branch == null) {
            setFlash(
                    request,
                    "error",
                    "Tài khoản Manager chưa được Admin phân công một chi nhánh."
            );

            response.sendRedirect(request.getContextPath() + "/manager/halls");
        }

        return branch;
    }

    private User getCurrentManager(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

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

    private int parseInt(String value) {
        try {
            return value == null || value.trim().isEmpty()
                    ? 0
                    : Integer.parseInt(value.trim());

        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setFlash(
            HttpServletRequest request,
            String type,
            String message
    ) {
        HttpSession session = request.getSession();

        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }
}