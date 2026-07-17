<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RAPVIET - Lịch sử soát vé</title>
    <style>
        :root { --bg:#07101f; --panel:#101b31; --line:#263653; --text:#eef4ff; --muted:#9aa9c4; --primary:#ff3366; --ok:#19c37d; }
        * { box-sizing:border-box; } body { margin:0; min-height:100vh; font-family:Inter,Arial,sans-serif; background:var(--bg); color:var(--text); }
        .navbar { display:flex; align-items:center; justify-content:space-between; padding:20px 40px; background:#111a30; border-bottom:1px solid var(--line); }
        .brand { font-size:20px; font-weight:800; color:var(--primary); } .nav a { color:var(--muted); text-decoration:none; margin-left:22px; font-size:14px; font-weight:700; } .nav a.active,.nav a:hover { color:var(--primary); }
        .container { width:min(1100px, calc(100% - 40px)); margin:46px auto; } h1 { margin:0 0 8px; font-size:28px; } .subtitle { margin:0 0 26px; color:var(--muted); }
        .toolbar,.card { background:var(--panel); border:1px solid var(--line); border-radius:14px; } .toolbar { display:flex; justify-content:space-between; align-items:end; gap:16px; padding:18px; margin-bottom:18px; }
        label { display:block; font-size:12px; color:var(--muted); font-weight:700; margin-bottom:7px; } input,.btn { height:40px; border-radius:8px; font:inherit; } input { color:var(--text); background:#0b1426; border:1px solid var(--line); padding:0 12px; } .btn { border:0; padding:0 16px; background:var(--primary); color:white; font-weight:800; cursor:pointer; }
        table { width:100%; border-collapse:collapse; } th,td { padding:15px 18px; border-bottom:1px solid var(--line); text-align:left; font-size:14px; } th { color:var(--muted); font-size:12px; text-transform:uppercase; } tr:last-child td { border-bottom:0; } .badge { color:var(--ok); font-weight:800; } .empty { padding:42px 20px; text-align:center; color:var(--muted); } .error { color:#ff819e; margin:0 0 12px; }.pager { display:flex; justify-content:flex-end; align-items:center; gap:9px; padding:15px 18px; border-top:1px solid var(--line); }.pager a { color:#fff; background:#253451; border:1px solid #395075; padding:8px 12px; border-radius:7px; text-decoration:none; font-size:13px; font-weight:800; }.pager a:hover { background:#334b72; }.pager span { color:var(--muted); font-size:13px; }
        @media (max-width:700px) { .navbar { padding:16px 20px; } .nav a { margin-left:12px; } .toolbar { align-items:flex-start; flex-direction:column; } .card { overflow-x:auto; } }
    </style>
</head>
<body>
<header class="navbar">
    <div class="brand">RAPVIET CONSOLE</div>
    <nav class="nav"><a href="CounterBooking">Quầy Bán Vé (POS)</a><a href="TicketValidation">Soát Vé Cổng</a><a class="active" href="TicketValidation?action=history">Lịch sử soát vé</a><a href="logout">Đăng Xuất</a></nav>
</header>
<main class="container">
    <h1>Lịch sử soát vé của tôi</h1>
    <p class="subtitle">Hiển thị toàn bộ lịch sử do chính tài khoản staff hiện tại thực hiện, 20 lượt mỗi trang.</p>
    <form class="toolbar" method="get" action="TicketValidation">
        <input type="hidden" name="action" value="history">
        <div><label for="date">Ngày soát vé</label><input id="date" name="date" type="date" value="${checkedDate}"></div>
        <button class="btn" type="submit">Lọc theo ngày</button>
        <a href="TicketValidation?action=history" style="color:var(--muted);text-decoration:none;font-size:13px;font-weight:700;padding:11px 6px;">Xem tất cả</a>
    </form>
    <c:if test="${not empty historyError}"><p class="error"><c:out value="${historyError}"/></p></c:if>
    <section class="card">
        <c:choose>
            <c:when test="${empty attendanceHistory}"><div class="empty">Chưa có lượt soát vé nào trong ngày này.</div></c:when>
            <c:otherwise><table><thead><tr><th>Thời gian soát</th><th>Mã vé</th><th>Phim</th><th>Phòng</th><th>Suất chiếu</th><th>Trạng thái</th></tr></thead><tbody>
                <c:forEach var="item" items="${attendanceHistory}"><tr>
                    <td><fmt:formatDate value="${item.checkedAt}" pattern="dd/MM/yyyy HH:mm"/></td><td><strong><c:out value="${item.qrCode}"/></strong></td><td><c:out value="${item.movieTitle}"/></td><td><c:out value="${item.hallName}"/></td><td><fmt:formatDate value="${item.showtimeStart}" pattern="dd/MM/yyyy HH:mm"/></td><td><span class="badge">Đã cho vào</span></td>
                </tr></c:forEach>
            </tbody></table></c:otherwise>
        </c:choose>
        <div class="pager"><c:if test="${historyPage > 1}"><c:url var="historyPrevUrl" value="/TicketValidation"><c:param name="action" value="history"/><c:param name="date" value="${checkedDate}"/><c:param name="page" value="${historyPage - 1}"/></c:url><a href="${historyPrevUrl}">← Trước</a></c:if><span>Trang ${historyPage} / ${historyTotalPages} · ${historyTotal} lượt</span><c:if test="${historyPage < historyTotalPages}"><c:url var="historyNextUrl" value="/TicketValidation"><c:param name="action" value="history"/><c:param name="date" value="${checkedDate}"/><c:param name="page" value="${historyPage + 1}"/></c:url><a href="${historyNextUrl}">Sau →</a></c:if></div>
    </section>
</main>
</body>
</html>
