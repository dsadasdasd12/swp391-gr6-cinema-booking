<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Quản Lý Cấu Hình Ghế</title>
    <style>
        /* CSS phối màu theo layout */
        .screen-container { display: flex; flex-direction: column; align-items: center; background: #0b0f19; padding: 20px; border-radius: 8px;}
        .grid-layout { display: grid; grid-template-columns: repeat(8, 45px); gap: 10px; margin-top: 20px; }
        .seat-btn { width: 45px; height: 45px; border: none; border-radius: 4px; color: white; font-weight: bold; cursor: pointer; text-align: center; line-height: 45px;}
        .STANDARD { background-color: #10b981; } /* Xanh lá */
        .VIP { background-color: #2563eb; }      /* Xanh dương */
        .COUPLE { background-color: #db2777; }   /* Hồng */
        .MAINTENANCE { background-color: #4b5563; } /* Xám */
    </style>
</head>
<body style="background-color: #030712; color: white; font-family: sans-serif;">

    <div style="padding: 20px;">
        <h2>QUẢN LÝ CẤU HÌNH PHÒNG CHIẾU (BACKOFFICE CONSOLE)</h2>
        <p>Servlet Action Target: <b>SeatConfigController</b></p>
        <hr>
        
        <div style="margin: 20px 0; background: #111827; padding: 15px; border-radius: 8px; display: flex; align-items: center; gap: 15px; border: 1px solid #374151;">
            <label style="font-weight: bold; font-size: 15px; color: #a5b4fc;">Chọn Phòng Chiếu để xem/cấu hình:</label>
            <select onchange="location.href='SeatConfigController?hallId=' + this.value" style="padding: 8px 12px; background: #1f2937; color: white; border: 1px solid #4b5563; border-radius: 4px; font-weight: bold; font-size: 14px; cursor: pointer; outline: none;">
                <c:forEach items="${hallList}" var="h">
                    <option value="${h.id}" ${h.id == currentHallId ? 'selected' : ''}>
                        ${h.name} (Tổng ${h.totalSeats} ghế)
                    </option>
                </c:forEach>
            </select>
        </div>

        <div style="display: flex; gap: 30px; margin-top: 20px;">
            <div style="width: 25%; background: #111827; padding: 20px; border-radius: 8px;">
                <h3>1. DANH MỤC LOẠI GHẾ</h3>
                <div style="padding: 10px; background: #10b981; margin-bottom: 5px; border-radius: 4px;">Standard (Ghế Thường)</div>
                <div style="padding: 10px; background: #2563eb; margin-bottom: 5px; border-radius: 4px;">VIP (Ghế Đẹp)</div>
                <div style="padding: 10px; background: #db2777; margin-bottom: 5px; border-radius: 4px;">Couple (Ghế Đôi)</div>
            </div>

            <div class="screen-container" style="width: 50%;">
                <div style="background: #374151; width: 80%; text-align: center; padding: 5px; border-radius: 0 0 15px 15px;">MÀN HÌNH CHIẾU (SCREEN)</div>
                
                <div class="grid-layout" style="grid-template-columns: repeat(${maxSeatNumber}, 45px);">
                    <c:forEach items="${seatList}" var="s">
                        <button class="seat-btn ${s.maintenance ? 'MAINTENANCE' : s.seatType}" 
                                style="grid-column: ${s.seatNumber}; grid-row: ${s.getRowIndex()};"
                                onclick="selectSeat('${s.getSeatCode()}', '${s.seatType}', '${s.maintenance ? 'MAINTENANCE' : 'AVAILABLE'}')">
                            ${s.getSeatCode()}
                        </button>
                    </c:forEach>
                </div>
            </div>

            <div style="width: 25%; background: #111827; padding: 20px; border-radius: 8px;">
                <h3>2. SỬA THÔNG TIN GHẾ</h3>
                <form action="SeatConfigController" method="POST">
                    <input type="hidden" name="hallId" value="${currentHallId}">
                    
                    <label>Mã Vị Trí Ghế (Ví dụ: B7, C1):</label>
                    <input type="text" id="formSeatCode" name="seatCode" placeholder="Click ghế hoặc tự gõ mã..." style="width: 100%; padding: 8px; margin: 10px 0; background: #374151; color: white; border: 1px solid #4b5563; border-radius: 4px;">

                    <label>Thay Đổi Loại Ghế:</label>
                    <select id="formSeatType" name="seatType" style="width: 100%; padding: 8px; margin: 10px 0; background: #374151; color: white; border: none; border-radius: 4px;">
                        <option value="STANDARD">STANDARD</option>
                        <option value="VIP">VIP</option>
                        <option value="COUPLE">COUPLE</option>
                    </select>

                    <label>Trạng Thái Vận Hành:</label>
                    <select id="formStatus" name="status" style="width: 100%; padding: 8px; margin: 10px 0; background: #374151; color: white; border: none; border-radius: 4px;">
                        <option value="AVAILABLE">AVAILABLE</option>
                        <option value="MAINTENANCE">MAINTENANCE</option>
                    </select>

                    <div style="margin-top: 20px; display: flex; flex-direction: column; gap: 10px;">
                        <div style="display: flex; gap: 10px;">
                            <button type="submit" name="action" value="update" style="flex: 1; background: #2563eb; color: white; padding: 10px; border: none; cursor: pointer; font-weight: bold; border-radius: 4px;">
                                Cập Nhật (Save)
                            </button>
                            
                            <button type="submit" name="action" value="add" style="flex: 1; background: #10b981; color: white; padding: 10px; border: none; cursor: pointer; font-weight: bold; border-radius: 4px;">
                                Thêm Ghế (Insert)
                            </button>
                        </div>
                        
                        <button type="submit" name="action" value="delete" style="width: 100%; background: #dc2626; color: white; padding: 10px; border: none; cursor: pointer; font-weight: bold; border-radius: 4px;">
                            Xóa Khỏi Sơ Đồ (Delete Seat)
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script>
        function selectSeat(code, type, status) {
            document.getElementById('formSeatCode').value = code;
            document.getElementById('formSeatType').value = type;
            document.getElementById('formStatus').value = status;
        }
    </script>
</body>
</html>