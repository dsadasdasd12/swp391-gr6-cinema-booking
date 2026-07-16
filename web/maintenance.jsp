<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bảo trì hệ thống — Rạp Việt</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100vh;
            text-align: center;
            font-family: 'Inter', sans-serif;
        }
        .maintenance-container {
            max-width: 600px;
            padding: 3rem;
            background: #fff;
            border-radius: 12px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.05);
        }
        .maintenance-icon {
            font-size: 5rem;
            color: #ffc107;
            margin-bottom: 1.5rem;
        }
        h1 {
            font-weight: 700;
            color: #212529;
            margin-bottom: 1rem;
        }
        p {
            color: #6c757d;
            font-size: 1.1rem;
            margin-bottom: 2rem;
        }
        .admin-link {
            text-decoration: none;
            color: #dc3545;
            font-weight: 500;
            font-size: 0.9rem;
        }
        .admin-link:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
<div class="maintenance-container">
    <i class="bi bi-tools maintenance-icon"></i>
    <h1>Hệ thống đang bảo trì</h1>
    <p>
        Rạp Việt hiện đang tạm ngưng hoạt động để nâng cấp hệ thống và mang lại trải nghiệm tốt nhất cho bạn. 
        Vui lòng quay lại sau ít phút. Xin lỗi vì sự bất tiện này!
    </p>
    <a href="${pageContext.request.contextPath}/login" class="admin-link">Đăng nhập Quản trị viên</a>
</div>
</body>
</html>
