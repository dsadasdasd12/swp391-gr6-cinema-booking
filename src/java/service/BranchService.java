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
        return branchDAO.insert(branch);
    }

    public boolean updateBranch(Branch branch) {
        validateBranch(branch, true);
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

        if (branch.getPhone() != null && !branch.getPhone().matches("0\\d{9,10}")) {
            throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng 0 và có 10 đến 11 chữ số.");
        }

        if (branch.getStatus() == null) {
            branch.setStatus("ACTIVE");
        }

        if (!VALID_STATUS.contains(branch.getStatus())) {
            throw new IllegalArgumentException("Trạng thái chi nhánh không hợp lệ.");
        }

        LocalTime openTime = branch.getOpenTime();
        LocalTime closeTime = branch.getCloseTime();

        if (openTime != null && closeTime != null && !openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("Giờ mở cửa phải nhỏ hơn giờ đóng cửa.");
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