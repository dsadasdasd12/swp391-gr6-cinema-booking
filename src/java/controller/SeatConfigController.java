package controller;

import service.SeatService;
import service.HallService;
import service.UserService;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Seat;
import model.Hall;
import model.User;
import model.SeatType;
import dao.SeatTypeDAO;

@WebServlet(name = "SeatConfigController", urlPatterns = ("/manager/seat-config"))
public class SeatConfigController extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = user.getRole();
        if (!"MANAGER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        SeatService seatService = new SeatService();
        HallService hallService = new HallService();
        UserService userService = new UserService();

        int branchId = 0;
        if ("ADMIN".equalsIgnoreCase(role)) {
            String branchIdParam = request.getParameter("branchId");
            if (branchIdParam != null && !branchIdParam.trim().isEmpty()) {
                try {
                    branchId = Integer.parseInt(branchIdParam.trim());
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            if (branchId <= 0) {
                List<Hall> allHalls = seatService.getAllHalls();
                if (allHalls != null && !allHalls.isEmpty()) {
                    branchId = allHalls.get(0).getBranchId();
                }
            }
        } else {
            branchId = userService.getBranchIdOfStaff(user.getId());
        }

        List<Hall> hallList = hallService.getHallsByBranchId(branchId);
        int hallId = 0;
        String hallIdParam = request.getParameter("hallId");
        if (hallIdParam != null && !hallIdParam.trim().isEmpty()) {
            try {
                hallId = Integer.parseInt(hallIdParam.trim());
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        boolean validHall = false;
        if (hallList != null && !hallList.isEmpty()) {
            for (Hall h : hallList) {
                if (h.getId() == hallId) {
                    validHall = true;
                    break;
                }
            }
            if (!validHall) {
                hallId = hallList.get(0).getId();
            }
        }

        String action = request.getParameter("action");
        dao.ShowtimeDAO showtimeDAO = new dao.ShowtimeDAO();
        boolean isLocked = hallId > 0 && showtimeDAO.hasFutureShowtimes(hallId);

        if (action != null && hallId > 0) {
            if (isLocked) {
                request.getSession().setAttribute("msgError", "Không thể chỉnh sửa sơ đồ ghế của phòng này vì đang có suất chiếu chưa diễn ra. Vui lòng hoàn thành hoặc hủy các suất chiếu trước.");
            } else {
                if ("bulkAdd".equals(action)) {
                    try {
                        String bulkType = request.getParameter("bulkType"); // row, col, grid
                        String bulkSeatType = request.getParameter("bulkSeatType");
                        String bulkStatus = request.getParameter("bulkStatus");
                        boolean maintenance = "MAINTENANCE".equalsIgnoreCase(bulkStatus);
                        int insertedCount = 0;

                        if ("row".equals(bulkType)) {
                            String bulkRow = request.getParameter("bulkRow").trim().toUpperCase();
                            int bulkSeatCount = Integer.parseInt(request.getParameter("bulkSeatCount").trim());
                            if (bulkRow.length() == 1 && Character.isLetter(bulkRow.charAt(0)) && bulkSeatCount > 0) {
                                for (int i = 1; i <= bulkSeatCount; i++) {
                                    if (seatService.insertSeat(hallId, bulkRow, i, bulkSeatType, maintenance)) {
                                        insertedCount++;
                                    }
                                }
                            }
                        } 
                        else if ("col".equals(bulkType)) {
                            int bulkCol = Integer.parseInt(request.getParameter("bulkCol").trim());
                            String bulkRowStart = request.getParameter("bulkRowStart").trim().toUpperCase();
                            String bulkRowEnd = request.getParameter("bulkRowEnd").trim().toUpperCase();
                            if (bulkCol > 0 && bulkRowStart.length() == 1 && bulkRowEnd.length() == 1) {
                                char start = bulkRowStart.charAt(0);
                                char end = bulkRowEnd.charAt(0);
                                for (char r = start; r <= end; r++) {
                                    if (seatService.insertSeat(hallId, String.valueOf(r), bulkCol, bulkSeatType, maintenance)) {
                                        insertedCount++;
                                    }
                                }
                            }
                        }
                        else if ("grid".equals(bulkType)) {
                            String bulkRowStart = request.getParameter("bulkRowStart").trim().toUpperCase();
                            String bulkRowEnd = request.getParameter("bulkRowEnd").trim().toUpperCase();
                            int bulkSeatCount = Integer.parseInt(request.getParameter("bulkSeatCount").trim());
                            if (bulkRowStart.length() == 1 && bulkRowEnd.length() == 1 && bulkSeatCount > 0) {
                                char start = bulkRowStart.charAt(0);
                                char end = bulkRowEnd.charAt(0);
                                for (char r = start; r <= end; r++) {
                                    for (int i = 1; i <= bulkSeatCount; i++) {
                                        if (seatService.insertSeat(hallId, String.valueOf(r), i, bulkSeatType, maintenance)) {
                                            insertedCount++;
                                        }
                                    }
                                }
                            }
                        }

                        request.getSession().setAttribute("msgSuccess", "Đã xử lý thêm hàng loạt: tạo thành công " + insertedCount + " ghế mới (đã bỏ qua các vị trí ghế đã tồn tại).");
                    } catch (Exception e) {
                        request.getSession().setAttribute("msgError", "Lỗi khi thêm ghế hàng loạt: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    String seatCode = request.getParameter("seatCode");
                    if (seatCode != null && !seatCode.trim().isEmpty()) {
                        String tempCode = seatCode.trim();
                        if (tempCode.matches("^[A-Za-z]{1}\\d+$")) {
                            try {
                                String seatRow = tempCode.substring(0, 1).toUpperCase();
                                int seatNumber = Integer.parseInt(tempCode.substring(1));
                                String seatType = request.getParameter("seatType");
                                String statusStr = request.getParameter("status");
                                boolean maintenance = "MAINTENANCE".equalsIgnoreCase(statusStr);

                                if ("update".equals(action)) {
                                    if (seatService.updateSeatConfig(hallId, seatRow, seatNumber, seatType, maintenance)) {
                                        request.getSession().setAttribute("msgSuccess", "Đã cập nhật cấu hình ghế " + seatRow + seatNumber + " thành công.");
                                    } else {
                                        request.getSession().setAttribute("msgError", "Lỗi khi cập nhật cấu hình ghế " + seatRow + seatNumber + ".");
                                    }
                                } 
                                else if ("delete".equals(action)) {
                                    seatService.deleteSeat(hallId, seatRow, seatNumber);
                                    request.getSession().setAttribute("msgSuccess", "Đã xóa ghế " + seatRow + seatNumber + " khỏi sơ đồ.");
                                } 
                                else if ("add".equals(action)) {
                                    if (seatService.insertSeat(hallId, seatRow, seatNumber, seatType, maintenance)) {
                                        request.getSession().setAttribute("msgSuccess", "Đã thêm ghế " + seatRow + seatNumber + " thành công.");
                                    } else {
                                        request.getSession().setAttribute("msgError", "Lỗi khi thêm ghế " + seatRow + seatNumber + " (có thể ghế đã tồn tại).");
                                    }
                                }
                            } catch (Exception e) {
                                request.getSession().setAttribute("msgError", "Lỗi xử lý: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            request.getSession().setAttribute("msgError", "Mã ghế '" + tempCode + "' không hợp lệ! Phải bắt đầu bằng 1 chữ cái và theo sau là số.");
                        }
                    }
                }
            }
        }

        List<Seat> seatList = new ArrayList<>();
        if (hallId > 0) {
            seatList = seatService.getSeatsByHall(hallId);
        }
        
        int maxSeatNumber = 8; // Default minimum columns
        for (Seat s : seatList) {
            if (s.getSeatNumber() > maxSeatNumber) {
                maxSeatNumber = s.getSeatNumber();
            }
        }
        
        SeatTypeDAO seatTypeDAO = new SeatTypeDAO();
        List<SeatType> activeSeatTypes = seatTypeDAO.findAllActive();
        List<SeatType> allSeatTypes = seatTypeDAO.findAll();

        request.setAttribute("seatList", seatList);
        request.setAttribute("hallList", hallList);
        request.setAttribute("currentHallId", hallId);
        request.setAttribute("maxSeatNumber", maxSeatNumber);
        request.setAttribute("activeSeatTypes", activeSeatTypes);
        request.setAttribute("allSeatTypes", allSeatTypes);
        request.setAttribute("isLocked", isLocked);
        


        request.getRequestDispatcher("/pages/manager/seatConfig.jsp")
               .forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}