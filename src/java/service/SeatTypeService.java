package service;

import dao.SeatTypeDAO;
import java.util.List;
import model.SeatType;

/**
 * Quy tắc nghiệp vụ cho danh mục loại ghế do ADMIN quản lý. Hệ số giá và trạng
 * thái loại ghế phải được kiểm tra trước khi lưu.
 */
public class SeatTypeService {

    private final SeatTypeDAO seatTypeDAO = new SeatTypeDAO();

    public List<SeatType> getAll() {
        return seatTypeDAO.findAll();
    }

    public void create(SeatType value) {
        // Chuẩn hóa dữ liệu để code, trạng thái và hệ số giá luôn hợp lệ.
        normalizeAndValidate(value);
        if (!seatTypeDAO.insert(value)) {
            throw new IllegalArgumentException("Không thể tạo loại ghế. Mã có thể đã tồn tại.");
        }
    }

    public void update(SeatType value) {
        if (value == null || value.getId() <= 0) {
            throw new IllegalArgumentException("Loại ghế không hợp lệ.");
        }
        SeatType current = seatTypeDAO.findById(value.getId());
        if (current == null) {
            throw new IllegalArgumentException("Không tìm thấy loại ghế cần cập nhật.");
        }
        // STANDARD/VIP/COUPLE là các loại nền tảng, không cho khóa để tránh làm hỏng sơ đồ/vé.
        if (isCoreType(current) && "INACTIVE".equalsIgnoreCase(value.getStatus())) {
            throw new IllegalArgumentException("Không thể khóa loại ghế mặc định.");
        }
        // Không cho đổi code khi cập nhật vì code đã được ghế và dữ liệu giá cũ tham chiếu.
        value.setCode(current.getCode());
        normalizeAndValidate(value);
        if (!seatTypeDAO.update(value)) {
            throw new IllegalArgumentException("Không thể cập nhật loại ghế.");
        }
    }

    public void deactivate(int id) {
        // Khóa mềm thay vì xóa cứng nhằm giữ lịch sử dữ liệu ghế/vé đã bán.
        SeatType current = id <= 0 ? null : seatTypeDAO.findById(id);
        if (current == null || isCoreType(current) || !seatTypeDAO.delete(id)) {
            throw new IllegalArgumentException("Không thể khóa loại ghế này.");
        }
    }

    private void normalizeAndValidate(SeatType value) {
        if (value == null) {
            throw new IllegalArgumentException("Dữ liệu loại ghế không hợp lệ.");
        }
        String code = value.getCode() == null ? "" : value.getCode().trim().toUpperCase();
        String name = value.getName() == null ? "" : value.getName().trim();
        String status = value.getStatus() == null ? "ACTIVE" : value.getStatus().trim().toUpperCase();
        if (!code.matches("^[A-Z0-9_]{1,20}$")) {
            throw new IllegalArgumentException("Mã loại ghế không hợp lệ.");
        }
        if (name.isEmpty() || name.length() > 150) {
            throw new IllegalArgumentException("Tên loại ghế phải từ 1 đến 150 ký tự.");
        }
        if (value.getDefaultPrice() <= 0 || value.getDefaultPrice() > 100) {
            throw new IllegalArgumentException("Hệ số nhân giá phải lớn hơn 0 và không vượt quá 100.");
        }
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new IllegalArgumentException("Trạng thái loại ghế không hợp lệ.");
        }
        value.setCode(code);
        value.setName(name);
        value.setStatus(status);
        // Màu không ảnh hưởng giá, nhưng phải là mã HEX hợp lệ để JSP vẽ sơ đồ an toàn.
        if (value.getColor() == null || !value.getColor().matches("^#[0-9A-Fa-f]{6}$")) {
            value.setColor("#10b981");
        }
    }

    private boolean isCoreType(SeatType value) {
        // Ba code này được coi là loại ghế mặc định của hệ thống.
        return "STANDARD".equalsIgnoreCase(value.getCode())
                || "VIP".equalsIgnoreCase(value.getCode())
                || "COUPLE".equalsIgnoreCase(value.getCode());
    }
}
