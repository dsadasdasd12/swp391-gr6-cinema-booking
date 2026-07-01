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

@WebServlet("/showtimes")
public class CustomerShowtimeController extends HttpServlet {

    private final BranchService branchService = new BranchService();
    private final ShowtimeService showtimeService = new ShowtimeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Branch> branches = activeBranches();
        int branchId = parseId(request.getParameter("branchId"));
        LocalDate date = parseDateOrToday(request.getParameter("date"));
        if (branchId <= 0 && !branches.isEmpty()) branchId = branches.get(0).getId();

        request.setAttribute("branches", branches);
        request.setAttribute("selectedBranchId", branchId);
        request.setAttribute("selectedDate", date);
        request.setAttribute("movieShowtimes", showtimeService.getMovieShowtimesByBranchAndDate(branchId, date));
        request.getRequestDispatcher("/pages/showtimes/list.jsp").forward(request, response);
    }

    private List<Branch> activeBranches() {
        List<Branch> result = new ArrayList<>();
        for (Branch branch : branchService.getAllBranches()) {
            if ("ACTIVE".equalsIgnoreCase(branch.getStatus())) result.add(branch);
        }
        return result;
    }

    private int parseId(String value) {
        try { return value == null ? 0 : Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private LocalDate parseDateOrToday(String value) {
        if (value == null || value.trim().isEmpty()) return LocalDate.now();
        try { return LocalDate.parse(value.trim()); }
        catch (Exception e) { return LocalDate.now(); }
    }
}
