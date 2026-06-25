package controller;

import service.SeatService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Seat;
import model.Hall;

@WebServlet(name = "SeatConfigController", urlPatterns = {"/SeatConfigController"})
public class SeatConfigController extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        SeatService seatService = new SeatService();
        
        int hallId = 1;
        String hallIdParam = request.getParameter("hallId");
        if (hallIdParam != null && !hallIdParam.trim().isEmpty()) {
            try {
                hallId = Integer.parseInt(hallIdParam.trim());
            } catch (NumberFormatException e) {
                // Default to 1
            }
        }
        
        String action = request.getParameter("action");

        if (action != null) {
            String seatCode = request.getParameter("seatCode");

            if (seatCode != null && !seatCode.trim().isEmpty()) {
                String tempCode = seatCode.trim();
                
                // RÀNG BUỘC: Bắt đầu bằng đúng 1 chữ cái (A-Z hoặc a-z), theo sau hoàn toàn là số
                if (tempCode.matches("^[A-Za-z]{1}\\d+$")) {
                    try {
                        String seatRow = tempCode.substring(0, 1).toUpperCase();
                        int seatNumber = Integer.parseInt(tempCode.substring(1));

                        String seatType = request.getParameter("seatType"); 
                        String statusStr = request.getParameter("status"); 
                        boolean maintenance = "MAINTENANCE".equalsIgnoreCase(statusStr);

                        System.out.println("--- LOG RUNTIME ---");
                        System.out.println("Action: " + action + " | Vi tri: " + seatRow + seatNumber + " | Type: " + seatType + " | Maintenance: " + maintenance);

                        if ("update".equals(action)) {
                            boolean isUpdated = seatService.updateSeatConfig(hallId, seatRow, seatNumber, seatType, maintenance);
                            System.out.println("Ket qua Update DB: " + isUpdated);
                        } 
                        else if ("delete".equals(action)) {
                            seatService.deleteSeat(hallId, seatRow, seatNumber);
                        } 
                        else if ("add".equals(action)) {
                            boolean isInserted = seatService.insertSeat(hallId, seatRow, seatNumber, seatType, maintenance);
                            System.out.println("Ket qua Insert DB: " + isInserted);
                        }
                        
                    } catch (Exception e) {
                        System.out.println("Loi xu ly logic Database: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("RANG BUOC THAT BAI: '" + tempCode + "' khong hop le! Phai bat dau bang chu cai.");
                }
            }
        }

        List<Seat> seatList = seatService.getSeatsByHall(hallId);
        List<Hall> hallList = seatService.getAllHalls();
        
        int maxSeatNumber = 8; // Default minimum columns
        for (Seat s : seatList) {
            if (s.getSeatNumber() > maxSeatNumber) {
                maxSeatNumber = s.getSeatNumber();
            }
        }
        
        request.setAttribute("seatList", seatList);
        request.setAttribute("hallList", hallList);
        request.setAttribute("currentHallId", hallId);
        request.setAttribute("maxSeatNumber", maxSeatNumber);
        request.getRequestDispatcher("seatConfig.jsp").forward(request, response);
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