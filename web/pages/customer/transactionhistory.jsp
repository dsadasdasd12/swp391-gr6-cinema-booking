<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lịch sử giao dịch - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/customerprofile.css">
</head>
<body>

<jsp:include page="/pages/common/header.jsp">
    <jsp:param name="active" value="customerprofile"/>
</jsp:include>

<div class="profile-page">
    <aside class="profile-sidebar">
        <div class="profile-user-box">
            <div class="profile-avatar">${sessionScope.user.fullName.substring(0,1)}</div>
            <h3>${sessionScope.user.fullName}</h3>
            <p>Thành viên RapViet Cinema</p>
        </div>

        <ul class="profile-menu">
            <li><a href="${ctx}/profile">👤 Personal Detail</a></li>
            <li><a href="${ctx}/favorite-movie">❤️ Favorite Film</a></li>
            <li><a href="${ctx}/transaction-history" class="active">🧾 Transaction History</a></li>
        </ul>
    </aside>

    <section class="profile-content">
        <div class="section-head">
            <h2>Lịch sử giao dịch</h2>
        </div>

        <div class="transaction-table-wrap">
            <table class="transaction-table">
                <thead>
                    <tr>
                        <th>Mã vé</th>
                        <th>Phim</th>
                        <th>Ngày đặt</th>
                        <th>Ghế</th>
                        <th>Tổng tiền</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>

                <tbody>
                    <tr>
                        <td>#RV001</td>
                        <td>Avengers: Endgame</td>
                        <td>09/06/2026</td>
                        <td>G7, G8</td>
                        <td>180.000đ</td>
                        <td><span class="status success">Đã thanh toán</span></td>
                    </tr>

                    <tr>
                        <td>#RV002</td>
                        <td>Inside Out 2</td>
                        <td>07/06/2026</td>
                        <td>C4</td>
                        <td>90.000đ</td>
                        <td><span class="status pending">Đang xử lý</span></td>
                    </tr>

                    <tr>
                        <td>#RV003</td>
                        <td>Mai</td>
                        <td>01/06/2026</td>
                        <td>D5, D6</td>
                        <td>160.000đ</td>
                        <td><span class="status cancel">Đã hủy</span></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </section>
</div>

<jsp:include page="/pages/common/footer.jsp"/>

</body>
</html>