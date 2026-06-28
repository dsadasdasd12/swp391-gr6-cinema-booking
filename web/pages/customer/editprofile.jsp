<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Chỉnh sửa thông tin - RapViet Cinema</title>

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
                    <div class="profile-avatar">
                        ${sessionScope.user.fullName.substring(0,1)}
                    </div>

                    <h3>${sessionScope.user.fullName}</h3>
                    <p>Thành viên RapViet Cinema</p>
                </div>

                <ul class="profile-menu">
                    <li>
                        <a href="${ctx}/profile">
                            👤 Personal Detail
                        </a>
                    </li>

                    <li>
                        <a href="${ctx}/favorite-films">
                            ❤️ Favorite Film
                        </a>
                    </li>

                    <li>
                        <a href="${ctx}/transaction-history">
                            🧾 Transaction History
                        </a>
                    </li>
                </ul>

            </aside>

            <section class="profile-content">

                <div class="section-head">
                    <h2>Chỉnh sửa thông tin</h2>
                </div>
                <c:if test="${not empty error}">
                    <div class="error-message">
                        ${error}
                    </div>
                </c:if>

                <form action="${ctx}/profile/update" method="post" class="profile-form">

                    <div class="form-group">
                        <label>Họ và tên</label>
                        <input type="text"
                               name="fullName"
                               value="${sessionScope.user.fullName}"
                               required>
                    </div>

                    <div class="form-group">
                        <label>Email</label>
                        <input type="email"
                               name="email"
                               value="${sessionScope.user.email}"
                               readonly>
                    </div>

                    <div class="form-group">
                        <label>Số điện thoại</label>
                        <input type="text"
                               name="phone"
                               value="${sessionScope.user.phone}"
                               >
                    </div>

                    <div class="profile-actions">
                        <a href="${ctx}/profile" class="btn btn-ghost">
                            Hủy
                        </a>

                        <button type="submit" class="btn btn-primary">
                            Lưu thay đổi
                        </button>
                    </div>

                </form>

            </section>

        </div>

        <jsp:include page="/pages/common/footer.jsp"/>

    </body>
</html>