-- Đặt mật khẩu "123" cho tài khoản mẫu (SHA-256)
-- Chạy trên database RapVietDB trước khi đăng nhập web.

USE RapVietDB;
GO

DECLARE @hash VARCHAR(64) = 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998b83769ca6ca4aa';

UPDATE dbo.[USER]
SET password_hash = @hash,
    active = 1,
    last_update = GETDATE()
WHERE email IN (
    'admin@rapviet.vn',
    'manager@rapviet.vn',
    'staff@rapviet.vn'
);

-- Kiểm tra sau khi update
SELECT id, email, role, active, LEFT(password_hash, 16) AS hash_prefix
FROM dbo.[USER]
WHERE email IN ('admin@rapviet.vn', 'manager@rapviet.vn', 'staff@rapviet.vn');

PRINT N'Xong. Đăng nhập web: email admin@rapviet.vn / mật khẩu: 123';
