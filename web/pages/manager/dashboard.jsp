<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Cổng quản lý và điều hành dành cho quản lý chi nhánh RapViet">
    <title>RapViet Cineplex - Cổng Quản Lý (Manager Portal)</title>
    
    <!-- Modern Google Font -->
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    
    <style>
        :root {
            --bg-color: hsl(222, 47%, 6%);
            --glass-bg: hsla(222, 47%, 12%, 0.7);
            --border-color: hsla(217, 30%, 20%, 0.5);
            --primary: hsl(342, 100%, 60%); /* RapViet Pink */
            --accent: hsl(224, 89%, 60%);
            --text-color: hsl(0, 0%, 100%);
            --muted-text: hsl(215, 20%, 65%);
        }

        body {
            background-color: var(--bg-color);
            color: var(--text-color);
            font-family: 'Outfit', sans-serif;
            margin: 0;
            padding: 0;
            background-image: radial-gradient(circle at 10% 20%, hsla(342, 60%, 15%, 0.3) 0%, transparent 90%),
                              radial-gradient(circle at 90% 80%, hsla(224, 89%, 20%, 0.25) 0%, transparent 90%);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            box-sizing: border-box;
        }

        /* Navbar Styling */
        .navbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 5%;
            background: rgba(11, 14, 20, 0.8);
            border-bottom: 1px solid var(--border-color);
            backdrop-filter: blur(12px);
        }

        .navbar-brand h1 {
            font-size: 24px;
            font-weight: 800;
            margin: 0;
            background: linear-gradient(135deg, var(--primary) 0%, #a5b4fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            letter-spacing: 1px;
        }

        .nav-user {
            display: flex;
            align-items: center;
            gap: 15px;
        }

        .user-info {
            font-size: 14px;
            font-weight: 500;
            color: var(--muted-text);
        }

        .user-info strong {
            color: white;
            font-weight: 700;
        }

        .nav-links a {
            text-decoration: none;
            color: var(--muted-text);
            font-size: 14px;
            font-weight: 600;
            transition: color 0.2s;
            margin-left: 15px;
        }

        .nav-links a:hover {
            color: white;
        }

        .btn-logout {
            color: #ff3366 !important;
            font-weight: 700 !important;
        }

        /* Container & Cards Styling */
        .container {
            max-width: 1200px;
            margin: 60px auto;
            padding: 0 4%;
            text-align: center;
            flex: 1;
        }

        .logo-title {
            font-size: 38px;
            font-weight: 800;
            margin-bottom: 8px;
            letter-spacing: 1px;
            background: linear-gradient(135deg, #ffffff 30%, var(--muted-text) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .sub-text {
            color: var(--muted-text);
            font-size: 15px;
            margin-bottom: 45px;
            font-weight: 400;
            max-width: 600px;
            margin-left: auto;
            margin-right: auto;
        }

        .portal-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 30px;
            margin-top: 20px;
        }

        .portal-card {
            background: var(--glass-bg);
            border: 1px solid var(--border-color);
            border-radius: 20px;
            padding: 40px 30px;
            backdrop-filter: blur(12px);
            transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
            text-decoration: none;
            color: white;
            display: flex;
            flex-direction: column;
            align-items: center;
            cursor: pointer;
            box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2);
        }

        .portal-card:hover {
            transform: translateY(-8px);
            border-color: var(--primary);
            box-shadow: 0 15px 30px rgba(255, 51, 102, 0.15);
        }

        .icon-box {
            width: 70px;
            height: 70px;
            border-radius: 20px;
            background: rgba(255, 51, 102, 0.08);
            color: var(--primary);
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 25px;
            transition: all 0.3s;
            border: 1px solid rgba(255, 51, 102, 0.15);
        }

        .portal-card:hover .icon-box {
            background: var(--primary);
            color: #fff;
            box-shadow: 0 0 20px rgba(255, 51, 102, 0.4);
            border-color: var(--primary);
        }

        .card-title {
            font-size: 18px;
            font-weight: 700;
            margin: 0 0 12px 0;
            letter-spacing: 0.5px;
        }

        .card-desc {
            font-size: 13px;
            color: var(--muted-text);
            line-height: 1.6;
            margin: 0;
        }

        /* Footer */
        .footer-note {
            margin-top: auto;
            padding: 30px 0;
            font-size: 13px;
            color: var(--muted-text);
            text-align: center;
            border-top: 1px solid var(--border-color);
            background: rgba(11, 14, 20, 0.4);
        }
    </style>
</head>
<body>

    <!-- Header Navigation -->
    <header class="navbar">
        <div class="navbar-brand">
            <h1>RAPVIET CINEPLEX</h1>
        </div>
        <div class="nav-user">
            <span class="user-info">Xin chào, <strong>${sessionScope.user.fullName}</strong> (Quản lý)</span>
            <nav class="nav-links">
                <a href="${ctx}/home">Trang Chủ</a>
                <a href="${ctx}/profile">Hồ Sơ</a>
                <a href="${ctx}/logout" class="btn-logout">Đăng Xuất</a>
            </nav>
        </div>
    </header>

    <!-- Main Content Area -->
    <main class="container">
        
        <h2 class="logo-title">MANAGER CONSOLE</h2>
        <p class="sub-text">Hệ thống điều hành và phân phối suất chiếu, kiểm soát quầy vé POS toàn diện dành cho Quản lý chi nhánh.</p>

        <!-- Lựa chọn phân hệ quản trị -->
        <section class="portal-grid">
            
            <!-- 1. Quản lý suất chiếu & lấp đầy -->
            <a href="${ctx}/ShowtimeManager" class="portal-card">
                <div class="icon-box">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="18" y1="20" x2="18" y2="10"></line>
                        <line x1="12" y1="20" x2="12" y2="4"></line>
                        <line x1="6" y1="20" x2="6" y2="14"></line>
                    </svg>
                </div>
                <h3 class="card-title">Giám Sát Suất Chiếu</h3>
                <p class="card-desc">Thiết lập giá vé rạp, giám sát suất chiếu và theo dõi trực quan tỉ lệ ghế đã đặt/còn trống theo thời gian thực.</p>
            </a>

            <!-- 2. Quản lý mã giảm giá -->
            <a href="${ctx}/DiscountManager" class="portal-card">
                <div class="icon-box">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path>
                        <line x1="7" y1="7" x2="7.01" y2="7"></line>
                    </svg>
                </div>
                <h3 class="card-title">Quản Lý Mã Giảm Giá</h3>
                <p class="card-desc">Cấu hình mã giảm giá (vouchers), thiết lập hạn mức và áp dụng trực tiếp cho các hóa đơn bán tại quầy.</p>
            </a>

            <!-- 3. Quầy bán vé tại chỗ -->
            <a href="${ctx}/CounterBooking" class="portal-card">
                <div class="icon-box">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="2" y="4" width="20" height="16" rx="2" ry="2"></rect>
                        <line x1="12" y1="4" x2="12" y2="20"></line>
                    </svg>
                </div>
                <h3 class="card-title">Quầy Vé POS</h3>
                <p class="card-desc">Bán vé tại chỗ nhanh chóng, chọn ghế trực quan cho khách hàng và xuất in vé nhiệt tức thời.</p>
            </a>

            <!-- 4. Soát vé cổng tự động -->
            <a href="${ctx}/TicketValidation" class="portal-card">
                <div class="icon-box">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                </div>
                <h3 class="card-title">Soát Vé Cổng</h3>
                <p class="card-desc">Quét mã vạch soát vé khách hàng vào phòng chiếu, tự động kiểm tra và khóa các vé sử dụng sai lệch.</p>
            </a>

        </section>

    </main>

    <!-- Footer -->
    <footer class="footer-note">
        <p>Hệ Thống Đặt Vé Phim Chi Nhánh RapViet Cineplex &copy; 2026 - Nhóm 6 Project SWP391</p>
    </footer>

</body>
</html>
