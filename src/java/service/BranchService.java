/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.BranchDAO;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import model.Branch;

public class BranchService {

    private static final List<String> VALID_STATUS = Arrays.asList("ACTIVE", "INACTIVE");

    private final BranchDAO branchDAO = new BranchDAO();

    public List<Branch> getAllBranches() {
        return branchDAO.findAll();
    }

    public Branch getBranchById(int id) {
        if (id <= 0) {
            return null;
        }

        return branchDAO.findById(id);
    }

    public boolean createBranch(Branch branch) {
        validateBranch(branch, false);

        if (branchDAO.existsByNameAndAddress(branch.getName(), branch.getAddress())) {
            throw new IllegalArgumentException("Chi nhánh này đã tồn tại với cùng tên và địa chỉ.");
        }

        if (branchDAO.existsByPhone(branch.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại này đã được dùng cho chi nhánh khác.");
        }

        return branchDAO.insert(branch);
    }

    public boolean updateBranch(Branch branch) {
        validateBranch(branch, true);

        Branch current = branchDAO.findById(branch.getId());
        if (current == null) {
            throw new IllegalArgumentException("Không tìm thấy chi nhánh cần cập nhật.");
        }

        if (isChangingToInactive(current.getStatus(), branch.getStatus())
                && branchDAO.hasUnfinishedShowtimes(branch.getId())) {
            throw new IllegalArgumentException(
                    "Không thể ngừng hoạt động chi nhánh vì vẫn còn suất chiếu chưa kết thúc."
            );
        }

        if (branchDAO.existsByNameAndAddressExceptId(
                branch.getName(),
                branch.getAddress(),
                branch.getId())) {
            throw new IllegalArgumentException("Chi nhánh này đã tồn tại với cùng tên và địa chỉ.");
        }

        if (branchDAO.existsByPhoneExceptId(branch.getPhone(), branch.getId())) {
            throw new IllegalArgumentException("Số điện thoại này đã được dùng cho chi nhánh khác.");
        }

        return branchDAO.update(branch);
    }

    public boolean deleteBranch(int id) {
        if (id <= 0) {
            return false;
        }

        return branchDAO.delete(id);
    }

    public boolean changeBranchStatus(int id, String status) {
        if (id <= 0) {
            return false;
        }

        status = normalize(status);

        if (!VALID_STATUS.contains(status)) {
            return false;
        }

        Branch current = branchDAO.findById(id);
        if (current == null) {
            return false;
        }

        if (isChangingToInactive(current.getStatus(), status)
                && branchDAO.hasUnfinishedShowtimes(id)) {
            throw new IllegalArgumentException(
                    "Không thể ngừng hoạt động chi nhánh vì vẫn còn suất chiếu chưa kết thúc."
            );
        }

        return branchDAO.updateStatus(id, status);
    }

    private void validateBranch(Branch branch, boolean requireId) {
        if (branch == null) {
            throw new IllegalArgumentException("Dữ liệu chi nhánh không hợp lệ.");
        }

        if (requireId && branch.getId() <= 0) {
            throw new IllegalArgumentException("Không xác định được chi nhánh cần cập nhật.");
        }

        if (branch.getCinemaId() <= 0) {
            branch.setCinemaId(1);
        }

        branch.setName(trimToNull(branch.getName()));
        branch.setAddress(trimToNull(branch.getAddress()));
        branch.setPhone(trimToNull(branch.getPhone()));
        branch.setStatus(normalize(branch.getStatus()));

        if (branch.getName() == null) {
            throw new IllegalArgumentException("Tên chi nhánh không được để trống.");
        }

        if (branch.getAddress() == null) {
            throw new IllegalArgumentException("Địa chỉ chi nhánh không được để trống.");
        }
        
        if (branch.getPhone() != null
                && !branch.getPhone().matches("0\\d{9,10}")) {
            throw new IllegalArgumentException(
                    "Số điện thoại phải bắt đầu bằng 0 và có 10 đến 11 chữ số."
            );
        }

        if (branch.getStatus() == null) {
            branch.setStatus("ACTIVE");
        }

        if (!VALID_STATUS.contains(branch.getStatus())) {
            throw new IllegalArgumentException("Trạng thái chi nhánh không hợp lệ.");
        }

        LocalTime openTime = branch.getOpenTime();
        LocalTime closeTime = branch.getCloseTime();

        if ((openTime == null) != (closeTime == null)) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập đầy đủ cả giờ mở cửa và giờ đóng cửa, hoặc để trống cả hai."
            );
        }

        if (openTime != null && openTime.equals(closeTime)) {
            throw new IllegalArgumentException("Giờ mở cửa phải nhỏ hơn giờ đóng cửa.");
        }
    }

    private boolean isChangingToInactive(String currentStatus, String newStatus) {
        return !"INACTIVE".equalsIgnoreCase(currentStatus)
                && "INACTIVE".equalsIgnoreCase(newStatus);
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