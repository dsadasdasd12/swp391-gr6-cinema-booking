/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package service;

import dao.HallDAO;
import dao.MovieManagementDAO;
import dao.MovieDAO;
import dao.ShowtimeDAO;
import dao.StaffBranchDAO;
import dto.MovieShowtimes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import model.Branch;
import model.Hall;
import model.Movie;
import model.Showtime;

public class ShowtimeService {

    private static final List<String> VALID_STATUS = Arrays.asList(
            "SCHEDULED",
            "ON_SALE"
    );

    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private final MovieDAO movieDAO = new MovieDAO();
    private final MovieManagementDAO movieManagementDAO
            = new MovieManagementDAO();

    private final StaffBranchDAO staffBranchDAO
            = new StaffBranchDAO();

    private final HallDAO hallDAO = new HallDAO();

    public Branch getAssignedBranch(int managerId) {
        if (managerId <= 0) {
            return null;
        }

        return staffBranchDAO.findBranchByManagerId(managerId);
    }

    public List<Showtime> getShowtimesByManagerId(int managerId) {
        Branch branch = getAssignedBranch(managerId);

        if (branch == null) {
            return Collections.emptyList();
        }

        return showtimeDAO.findByBranchId(branch.getId());
    }

    public Showtime getShowtimeByIdAndManagerId(
            int id,
            int managerId
    ) {
        if (id <= 0 || managerId <= 0) {
            return null;
        }

        Branch branch = getAssignedBranch(managerId);

        if (branch == null) {
            return null;
        }

        return showtimeDAO.findByIdAndBranchId(
                id,
                branch.getId()
        );
    }

    @Deprecated
    public boolean createShowtime(Showtime showtime) {
        validateAndPrepare(showtime, false);

        boolean conflict = showtimeDAO.hasScheduleConflict(
                showtime.getHallId(),
        showtime.getStartTime().toLocalDateTime(),
        showtime.getEndTime().toLocalDateTime(),
                0
        );

        if (conflict) {
            throw new IllegalArgumentException(
                    "PhÃ²ng chiáº¿u nÃ y Ä‘Ã£ cÃ³ suáº¥t chiáº¿u "
                    + "trong khoáº£ng thá»i gian Ä‘Ã£ chá»n."
            );
        }

        return showtimeDAO.insert(showtime);
    }

    public boolean createShowtime(
            int managerId,
            Showtime showtime
    ) {
        Branch branch = requireAssignedBranch(managerId);

        ensureHallBelongsToBranch(
                showtime == null ? 0 : showtime.getHallId(),
                branch.getId()
        );

        validateAndPrepare(showtime, false);

        boolean conflict = showtimeDAO.hasScheduleConflict(
                showtime.getHallId(),
        showtime.getStartTime().toLocalDateTime(),
        showtime.getEndTime().toLocalDateTime(),
                0
        );

        if (conflict) {
            throw new IllegalArgumentException(
                    "PhÃ²ng chiáº¿u nÃ y Ä‘Ã£ cÃ³ suáº¥t chiáº¿u "
                    + "trong khoáº£ng thá»i gian Ä‘Ã£ chá»n."
            );
        }

        return showtimeDAO.insert(showtime);
    }

    @Deprecated
    public boolean updateShowtime(Showtime showtime) {
        validateAndPrepare(showtime, true);

        boolean conflict = showtimeDAO.hasScheduleConflict(
                showtime.getHallId(),
        showtime.getStartTime().toLocalDateTime(),
        showtime.getEndTime().toLocalDateTime(),
                showtime.getId()
        );

        if (conflict) {
            throw new IllegalArgumentException(
                    "PhÃ²ng chiáº¿u nÃ y Ä‘Ã£ cÃ³ suáº¥t chiáº¿u "
                    + "trong khoáº£ng thá»i gian Ä‘Ã£ chá»n."
            );
        }

        return showtimeDAO.update(showtime);
    }

    public boolean updateShowtime(
            int managerId,
            Showtime showtime
    ) {
        Branch branch = requireAssignedBranch(managerId);

        if (showtime == null || showtime.getId() <= 0) {
            throw new IllegalArgumentException(
                    "KhÃ´ng xÃ¡c Ä‘á»‹nh Ä‘Æ°á»£c suáº¥t chiáº¿u cáº§n cáº­p nháº­t."
            );
        }

        Showtime current = showtimeDAO.findByIdAndBranchId(
                showtime.getId(),
                branch.getId()
        );

        if (current == null) {
            throw new IllegalArgumentException(
                    "Báº¡n khÃ´ng cÃ³ quyá»n cáº­p nháº­t suáº¥t chiáº¿u nÃ y."
            );
        }

        ensureHallBelongsToBranch(
                showtime.getHallId(),
                branch.getId()
        );

        validateAndPrepare(showtime, true);

        boolean conflict = showtimeDAO.hasScheduleConflict(
               showtime.getHallId(),
        showtime.getStartTime().toLocalDateTime(),
        showtime.getEndTime().toLocalDateTime(),
                showtime.getId()
        );

        if (conflict) {
            throw new IllegalArgumentException(
                    "PhÃ²ng chiáº¿u nÃ y Ä‘Ã£ cÃ³ suáº¥t chiáº¿u "
                    + "trong khoáº£ng thá»i gian Ä‘Ã£ chá»n."
            );
        }

        return showtimeDAO.update(showtime);
    }

    public boolean cancelShowtime(
            int id,
            int managerId
    ) {
        if (id <= 0 || managerId <= 0) {
            return false;
        }

        Branch branch = getAssignedBranch(managerId);

        if (branch == null) {
            return false;
        }

        Showtime current = showtimeDAO.findByIdAndBranchId(
                id,
                branch.getId()
        );

        if (current == null) {
            return false;
        }

        if ("CANCELLED".equalsIgnoreCase(
                current.getStatus()
        )) {
            return false;
        }

        return showtimeDAO.cancel(id);
    }

    private Branch requireAssignedBranch(int managerId) {
        if (managerId <= 0) {
            throw new IllegalArgumentException(
                    "TÃ i khoáº£n Manager khÃ´ng há»£p lá»‡."
            );
        }

        Branch branch = staffBranchDAO.findBranchByManagerId(managerId);

        if (branch == null) {
            throw new IllegalArgumentException(
                    "TÃ i khoáº£n Manager chÆ°a Ä‘Æ°á»£c Admin phÃ¢n cÃ´ng chi nhÃ¡nh."
            );
        }

        return branch;
    }

    private void ensureHallBelongsToBranch(
            int hallId,
            int branchId
    ) {
        if (hallId <= 0) {
            throw new IllegalArgumentException(
                    "Vui lÃ²ng chá»n phÃ²ng chiáº¿u."
            );
        }

        Hall hall = hallDAO.findByIdAndBranchId(
                hallId,
                branchId
        );

        if (hall == null) {
            throw new IllegalArgumentException(
                    "Báº¡n khÃ´ng cÃ³ quyá»n sá»­ dá»¥ng phÃ²ng chiáº¿u nÃ y."
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(
                hall.getStatus()
        )) {
            throw new IllegalArgumentException(
                    "Chá»‰ cÃ³ thá»ƒ táº¡o hoáº·c cáº­p nháº­t suáº¥t chiáº¿u cho phÃ²ng chiáº¿u Ä‘ang hoáº¡t Ä‘á»™ng."
            );
        }
    }

    private void validateAndPrepare(
            Showtime showtime,
            boolean requireId
    ) {
        if (showtime == null) {
            throw new IllegalArgumentException(
                    "Dá»¯ liá»‡u suáº¥t chiáº¿u khÃ´ng há»£p lá»‡."
            );
        }

        if (requireId && showtime.getId() <= 0) {
            throw new IllegalArgumentException(
                    "KhÃ´ng xÃ¡c Ä‘á»‹nh Ä‘Æ°á»£c suáº¥t chiáº¿u cáº§n cáº­p nháº­t."
            );
        }

        if (showtime.getMovieId() <= 0) {
            throw new IllegalArgumentException(
                    "Vui lÃ²ng chá»n phim."
            );
        }

        if (showtime.getHallId() <= 0) {
            throw new IllegalArgumentException(
                    "Vui lÃ²ng chá»n phÃ²ng chiáº¿u."
            );
        }

        if (showtime.getStartTime() == null) {
            throw new IllegalArgumentException(
                    "Vui lÃ²ng chá»n thá»i gian báº¯t Ä‘áº§u."
            );
        }

        if (showtime.getBasePrice() < 0) {
            throw new IllegalArgumentException(
                    "GiÃ¡ vÃ© cÆ¡ báº£n khÃ´ng Ä‘Æ°á»£c nhá» hÆ¡n 0."
            );
        }

        Movie movie = movieDAO.findById(
                showtime.getMovieId()
        );

        if (movie == null) {
            throw new IllegalArgumentException(
                    "Phim khÃ´ng tá»“n táº¡i."
            );
        }

        boolean assignedToHall
                = movieManagementDAO.isMovieAssignedToHall(
                        showtime.getHallId(),
                        showtime.getMovieId()
                );

        if (!assignedToHall) {
            throw new IllegalArgumentException(
                    "Phim nÃ y chÆ°a Ä‘Æ°á»£c phÃ¢n bá»• cho phÃ²ng chiáº¿u Ä‘Ã£ chá»n."
            );
        }

        if (movie.getDurationMin() <= 0) {
            throw new IllegalArgumentException(
                    "Phim chÆ°a cÃ³ thá»i lÆ°á»£ng há»£p lá»‡."
            );
        }

        String status = normalize(
                showtime.getStatus()
        );

        if (status == null) {
            status = "SCHEDULED";
        }

        if (!VALID_STATUS.contains(status)) {
            throw new IllegalArgumentException(
                    "Tráº¡ng thÃ¡i suáº¥t chiáº¿u khÃ´ng há»£p lá»‡."
            );
        }

        showtime.setStatus(status);

        LocalDateTime endTime = showtime.getStartTime()
        .toLocalDateTime()
        .plusMinutes(movie.getDurationMin());

        showtime.setEndTime(endTime);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String result = value.trim().toUpperCase();

        if (result.isEmpty()) {
            return null;
        }

        return result;
    }
     public List<Showtime> getActiveShowtimesByBranch(int branchId) {
        return showtimeDAO.getActiveShowtimesByBranch(branchId);
    }

    public Showtime getShowtimeById(int id) {
        return showtimeDAO.getShowtimeById(id);
    }

    public Showtime getBookableShowtime(int id) {
        if (id <= 0) {
            return null;
        }

        return showtimeDAO.findBookableById(id);
    }

    public List<Showtime> getBookableShowtimes(int branchId, int movieId, LocalDate date) {
        if (branchId <= 0 || movieId <= 0 || date == null) {
            return Collections.emptyList();
        }

        return showtimeDAO.findBookableByBranchMovieAndDate(branchId, movieId, date);
    }

    public boolean setSeatPricing(int showtimeId, String seatType, double price) {
        return showtimeDAO.setSeatPricing(showtimeId, seatType, price);
    }

    public double getSeatPrice(int showtimeId, String seatType, double basePrice) {
        return showtimeDAO.getSeatPrice(showtimeId, seatType, basePrice);
    }

    public int getBookedSeatsCount(int showtimeId) {
        return showtimeDAO.getBookedSeatsCount(showtimeId);
    }

    public int getTotalSeatsInHall(int showtimeId) {
        return showtimeDAO.getTotalSeatsInHall(showtimeId);
    }

    public double getOccupancyRate(int showtimeId) {
        return showtimeDAO.getOccupancyRate(showtimeId);
    }

    public List<Showtime> getShowtimesByBranchAndDate(int branchId, String dateStr) {
        return showtimeDAO.getShowtimesByBranchAndDate(branchId, dateStr);
    }

    public List<MovieShowtimes> getMovieShowtimesByBranchAndDate(int branchId, LocalDate date) {
        if (branchId <= 0 || date == null) {
            return Collections.emptyList();
        }

        return showtimeDAO.findByBranchAndDate(branchId, date);
    }
}


