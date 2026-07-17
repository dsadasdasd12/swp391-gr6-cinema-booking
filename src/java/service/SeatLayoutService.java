package service;

import dao.ShowtimeDAO;
import java.util.Map;

/**
 * Chứa toàn bộ quy tắc kiểm tra và thay đổi sơ đồ ghế của manager. Controller
 * chỉ chuyển action/dữ liệu form vào service này.
 */
public class SeatLayoutService {

    private final SeatService seatService = new SeatService();
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    public String apply(int hallId, String action, Map<String, String> values) {
        // Không cho phép cấu hình một phòng không xác định.
        if (hallId <= 0) {
            throw new IllegalArgumentException("Phòng chiếu không hợp lệ.");
        }
        // Sơ đồ ghế bị khóa khi phòng đã có suất chiếu hiện tại/tương lai để bảo toàn dữ liệu vé.
        if (showtimeDAO.hasFutureShowtimes(hallId)) {
            throw new IllegalArgumentException("Không thể sửa sơ đồ ghế khi phòng có suất chiếu đang diễn ra hoặc sắp diễn ra.");
        }
        if ("clearAll".equals(action)) {
            // Xóa toàn bộ chỉ được thực hiện khi qua được điều kiện khóa suất chiếu ở trên.
            if (!seatService.deleteSeatsOfHall(hallId)) {
                throw new IllegalArgumentException("Không thể xóa sơ đồ ghế.");
            }
            return "Đã xóa toàn bộ sơ đồ ghế.";
        }
        if ("bulkAdd".equals(action)) {
            return bulkAdd(hallId, values);
        }
        return changeOne(hallId, action, values);
    }

    private String bulkAdd(int hallId, Map<String, String> values) {
        // Hỗ trợ thêm nhanh theo một hàng, một cột hoặc cả vùng lưới.
        String kind = value(values, "bulkType");
        String type = value(values, "bulkSeatType");
        boolean maintenance = "MAINTENANCE".equalsIgnoreCase(value(values, "bulkStatus"));
        int added = 0;
        if ("row".equals(kind)) {
            String row = row(value(values, "bulkRow"));
            int count = seatNumber(value(values, "bulkSeatCount"));
            for (int i = 1; i <= count; i++) {
                if (seatService.insertSeat(hallId, row, i, type, maintenance)) {
                    added++;
                }
            }
        } else if ("col".equals(kind)) {
            int column = seatNumber(value(values, "bulkCol"));
            for (char r : rows(value(values, "bulkRowStart"), value(values, "bulkRowEnd"))) {
                if (seatService.insertSeat(hallId, String.valueOf(r), column, type, maintenance)) {
                    added++;
                }
            }
        } else if ("grid".equals(kind)) {
            int count = seatNumber(value(values, "bulkSeatCount"));
            for (char r : rows(value(values, "bulkRowStart"), value(values, "bulkRowEnd"))) {
                for (int i = 1; i <= count; i++) {
                    if (seatService.insertSeat(hallId, String.valueOf(r), i, type, maintenance)) {
                        added++;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Kiểu thêm ghế không hợp lệ.");
        }
        return "Đã xử lý sơ đồ ghế: thêm " + added + " ghế mới.";
    }

    private String changeOne(int hallId, String action, Map<String, String> values) {
        // Mã ghế phải có dạng A1, B12... trước khi tách thành hàng và số ghế.
        String code = value(values, "seatCode").toUpperCase();
        if (!code.matches("^[A-Z]\\d+$")) {
            throw new IllegalArgumentException("Mã ghế không hợp lệ.");
        }
        String row = code.substring(0, 1);
        int number = seatNumber(code.substring(1));
        boolean maintenance = "MAINTENANCE".equalsIgnoreCase(value(values, "status"));
        boolean changed;
        if ("add".equals(action)) {
            changed = seatService.insertSeat(hallId, row, number, value(values, "seatType"), maintenance);
        } else if ("update".equals(action)) {
            changed = seatService.updateSeatConfig(hallId, row, number, value(values, "seatType"), maintenance);
        } else if ("delete".equals(action)) {
            changed = seatService.deleteSeat(hallId, row, number);
        } else {
            throw new IllegalArgumentException("Thao tác sơ đồ ghế không hợp lệ.");
        }
        if (!changed) {
            throw new IllegalArgumentException("Không thể thay đổi ghế " + code + ".");
        }
        return "Đã cập nhật ghế " + code + ".";
    }

    private String value(Map<String, String> values, String key) {
        // Không để action cấu hình tiếp tục khi thiếu trường bắt buộc.
        String value = values.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu dữ liệu cấu hình ghế.");
        }
        return value.trim();
    }

    private String row(String value) {
        String normalized = value.trim().toUpperCase();
        if (!normalized.matches("^[A-Z]$")) {
            throw new IllegalArgumentException("Hàng ghế phải là một chữ cái.");
        }
        return normalized;
    }

    private char[] rows(String start, String end) {
        char from = row(start).charAt(0), to = row(end).charAt(0);
        if (from > to) {
            throw new IllegalArgumentException("Hàng bắt đầu phải đứng trước hàng kết thúc.");
        }
        // Tạo dãy hàng liên tục, ví dụ A đến C thành A, B, C.
        char[] result = new char[to - from + 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = (char) (from + i);
        }
        return result;
    }

    private int seatNumber(String value) {
        // Giới hạn 1..30 tránh tạo sơ đồ quá lớn hoặc mã ghế không hợp lệ.
        try {
            int number = Integer.parseInt(value.trim());
            if (number >= 1 && number <= 30) {
                return number;
            }
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("Số ghế phải từ 1 đến 30.");
    }
}
