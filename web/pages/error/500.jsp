<%--
    Rạp Việt CMS — 500 Internal Server Error Page
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>500 — Lỗi máy chủ hệ thống | Rạp Việt CMS</title>
        <!-- Bootstrap Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
        <!-- Google Font: Inter -->
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${ctx}/assets/css/variables.css">
        <link rel="stylesheet" href="${ctx}/assets/css/base.css">
        <link rel="stylesheet" href="${ctx}/assets/css/components.css">

        <style>
            body {
                background: radial-gradient(circle at center, var(--n-800) 0%, var(--n-900) 100%);
                height: 100vh;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: var(--s-4);
            }
            .error-card {
                background: var(--surface);
                border: 1px solid var(--border);
                box-shadow: var(--shadow-2xl);
                border-radius: var(--r-xl);
                padding: var(--s-8) var(--s-6);
                max-width: 440px;
                width: 100%;
                text-align: center;
                animation: error-in 0.4s ease;
            }
            @keyframes error-in {
                from {
                    opacity: 0;
                    transform: translateY(20px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }
            .error-badge {
                width: 72px;
                height: 72px;
                background: var(--danger-bg);
                color: var(--danger);
                font-size: 32px;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                margin: 0 auto var(--s-4);
            }
        </style>
    </head>
    <body>

        <div class="error-card">
            <div class="error-badge">
                <i class="bi bi-exclamation-triangle-fill"></i>
            </div>
            <h1 style="font-size: 36px; font-weight: 800; color: var(--n-900); margin-bottom: var(--s-1);">500</h1>
            <h2 style="font-size: var(--text-md); font-weight: 700; color: var(--n-700); margin-bottom: var(--s-3);">LỖI MÁY CHỦ NỘI BỘ</h2>
            <p style="color: var(--n-500); font-size: var(--text-base); line-height: 1.6; margin-bottom: var(--s-6);">
                Đã xảy ra sự cố kỹ thuật với hệ thống máy chiếu hoặc cơ sở dữ liệu. Đừng lo lắng, các kỹ sư phần mềm của chúng tôi đang tiến hành khắc phục sự cố này.
            </p>
            <div style="display: flex; gap: var(--s-3); justify-content: center;">
                <a href="${ctx}/admin/dashboard" class="rv-btn rv-btn--primary">
                    <i class="bi bi-house-door-fill"></i>Quay lại Dashboard
                </a>
                <a href="javascript:location.reload();" class="rv-btn rv-btn--ghost">
                    <i class="bi bi-arrow-clockwise"></i>Tải lại trang
                </a>
            </div>
        </div>

    </body>
</html>
