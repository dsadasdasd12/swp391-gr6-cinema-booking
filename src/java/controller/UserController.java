/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

/**
 *
 * @author tttru
 */
@WebServlet(urlPatterns = {
    "/profile",
    "/profile/edit",
    "/favorite-films",
    "/transaction-history"
})
public class UserController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet UserController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet UserController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        User user = (User) request.getSession().getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        switch (path) {

            case "/profile":
                if (user.getRole().equals("CUSTOMER")) {
                    request.getRequestDispatcher("/pages/customer/customerprofile.jsp")
                            .forward(request, response);
                    return;
                }
                request.getRequestDispatcher("/pages/profile.jsp")
                        .forward(request, response);
                return;

            case "/profile/edit":
                request.getRequestDispatcher("/pages/customer/editprofile.jsp")
                        .forward(request, response);
                return;
            case "/favorite-films":
                request.getRequestDispatcher("/pages/customer/favoritefilms.jsp")
                        .forward(request, response);
                return;

            case "/transaction-history":
                request.getRequestDispatcher("/pages/customer/transactionhistory.jsp")
                        .forward(request, response);
                return;

        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/profile/edit".equals(path)) {

            User user = (User) request.getSession().getAttribute("user");

            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            String fullName = request.getParameter("fullName");
            String phone = request.getParameter("phone");

            fullName = fullName == null ? "" : fullName.trim();
            phone = phone == null ? "" : phone.trim();
            String nameRegex
                    = "^[\\p{L} ]{2,50}$";
            String phoneRegex = "^0\\d{9}$";
            if (fullName == null
                    || fullName.trim().isEmpty()
                    || !fullName.matches(nameRegex)) {

                request.setAttribute(
                        "error",
                        "Họ và tên không hợp lệ"
                );

                request.getRequestDispatcher(
                        "/pages/customer/editprofile.jsp")
                        .forward(request, response);

                return;
            }
            if (!phone.matches(phoneRegex)) {
                request.setAttribute("error", "Số điện thoại phải gồm 10 số và bắt đầu bằng 0.");
                request.getRequestDispatcher("/pages/customer/editprofile.jsp").forward(request, response);
                return;
            }
            UserDAO userDAO = new UserDAO();

            boolean updated = userDAO.updateCustomerProfile(
                    user.getId(),
                    fullName,
                    phone
            );

            if (updated) {
                user.setFullName(fullName);
                user.setPhone(phone);

                request.getSession().setAttribute("user", user);

                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }

            request.setAttribute("error", "Cập nhật thông tin thất bại.");
            request.getRequestDispatcher("/pages/customer/editprofile.jsp")
                    .forward(request, response);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
