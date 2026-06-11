/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem chi nhánh rạp / Suất chiếu / Sơ đồ ghế
 */
package service;

import java.util.List;
import dao.BranchDAO;
import model.Branch;

/**
 * Tầng nghiệp vụ cho nhóm chức năng liên quan tới rạp: xem chi nhánh, (sau này)
 * xem suất chiếu theo chi nhánh và sơ đồ ghế. Controller chỉ làm việc với lớp
 * này, không gọi trực tiếp DAO.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class CinemaService {

    private final BranchDAO branchDAO = new BranchDAO();

    /** Danh sách chi nhánh đang hoạt động cho trang "Hệ thống rạp". */
    public List<Branch> getActiveBranches() {
        return branchDAO.findAllActive();
    }

    /** Chi tiết một chi nhánh, hoặc {@code null} nếu id không hợp lệ / không tồn tại. */
    public Branch getBranch(int branchId) {
        if (branchId <= 0) {
            return null;
        }
        return branchDAO.findById(branchId);
    }
}
