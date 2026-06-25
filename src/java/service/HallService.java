/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.HallDAO;
import java.util.Arrays;
import java.util.List;
import model.Hall;

public class HallService {

    private static final List<String> VALID_HALL_TYPES = Arrays.asList(
            "STANDARD", "VIP", "IMAX", "4DX", "PREMIUM"
    );

    private static final List<String> VALID_STATUS = Arrays.asList(
            "ACTIVE", "MAINTENANCE", "INACTIVE"
    );

    private final HallDAO hallDAO = new HallDAO();

    public List<Hall> getHallsByBranchId(int branchId) {
        if (branchId <= 0) {
            throw new IllegalArgumentException("Chi nhánh không hợp lệ.");
        }

        return hallDAO.findByBranchId(branchId);
    }

    public Hall getHallByIdAndBranchId(int id, int branchId) {
        if (id <= 0 || branchId <= 0) {
            return null;
        }

        return hallDAO.findByIdAndBranchId(id, branchId);
    }

    public boolean createHall(Hall hall) {
        validateHall(hall, false);

        if (hallDAO.existsByNameAndBranchId(hall.getName(), hall.getBranchId())) {
            throw new IllegalArgumentException("Phòng chiếu này đã tồn tại trong chi nhánh này.");
        }

        return hallDAO.insert(hall);
    }

    public boolean updateHall(Hall hall) {
        validateHall(hall, true);

        if (hallDAO.existsByNameAndBranchIdExceptId(
                hall.getName(),
                hall.getBranchId(),
                hall.getId())) {
            throw new IllegalArgumentException("Phòng chiếu này đã tồn tại trong chi nhánh này.");
        }

        return hallDAO.update(hall);
    }

    public boolean deleteHall(int id, int branchId) {
        if (id <= 0 || branchId <= 0) {
            return false;
        }

        return hallDAO.delete(id, branchId);
    }

    public boolean changeHallStatus(int id, int branchId, String status) {
        if (id <= 0 || branchId <= 0) {
            return false;
        }

        status = normalize(status);

        if (!VALID_STATUS.contains(status)) {
            return false;
        }

        return hallDAO.updateStatus(id, branchId, status);
    }

    private void validateHall(Hall hall, boolean requireId) {
        if (hall == null) {
            throw new IllegalArgumentException("Dữ liệu phòng chiếu không hợp lệ.");
        }

        if (requireId && hall.getId() <= 0) {
            throw new IllegalArgumentException("Không xác định được phòng chiếu cần cập nhật.");
        }

        if (hall.getBranchId() <= 0) {
            throw new IllegalArgumentException("Chi nhánh không hợp lệ.");
        }

        hall.setName(trimToNull(hall.getName()));
        hall.setHallType(normalize(hall.getHallType()));
        hall.setStatus(normalize(hall.getStatus()));

        if (hall.getName() == null) {
            throw new IllegalArgumentException("Tên phòng chiếu không được để trống.");
        }

        if (hall.getTotalSeats() <= 0) {
            throw new IllegalArgumentException("Tổng số ghế phải lớn hơn 0.");
        }

        if (hall.getHallType() == null) {
            hall.setHallType("STANDARD");
        }

        if (!VALID_HALL_TYPES.contains(hall.getHallType())) {
            throw new IllegalArgumentException("Loại phòng chiếu không hợp lệ.");
        }

        if (hall.getStatus() == null) {
            hall.setStatus("ACTIVE");
        }

        if (!VALID_STATUS.contains(hall.getStatus())) {
            throw new IllegalArgumentException("Trạng thái phòng chiếu không hợp lệ.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String result = value.trim();

        if (result.isEmpty()) {
            return null;
        }

        return result;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        return value.trim().toUpperCase();
    }
}