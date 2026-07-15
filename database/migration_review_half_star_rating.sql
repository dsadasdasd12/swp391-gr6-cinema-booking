-- Cho phép khách đánh giá theo nửa sao: 0.5, 1.0, 1.5 ... 5.0.
-- Có thể chạy lại an toàn nếu lần chạy trước bị dừng giữa chừng.
USE RapVietDB;
GO

-- Xóa mọi CHECK constraint của bảng REVIEWS có dùng cột rating.
-- Dùng dynamic SQL vì tên constraint có thể khác giữa các bản DB của nhóm.
DECLARE @sql NVARCHAR(MAX) = N'';
SELECT @sql = @sql + N'ALTER TABLE dbo.REVIEWS DROP CONSTRAINT ' + QUOTENAME(cc.name) + N';' + CHAR(13) + CHAR(10)
FROM sys.check_constraints cc
WHERE cc.parent_object_id = OBJECT_ID(N'dbo.REVIEWS')
  AND cc.definition LIKE N'%rating%';

IF LEN(@sql) > 0
    EXEC sys.sp_executesql @sql;
GO

ALTER TABLE dbo.REVIEWS ALTER COLUMN rating DECIMAL(2,1) NOT NULL;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID(N'dbo.REVIEWS')
      AND name = N'CK_REVIEWS_rating_half_star'
)
BEGIN
    ALTER TABLE dbo.REVIEWS
        ADD CONSTRAINT CK_REVIEWS_rating_half_star
        CHECK (rating >= 0.5 AND rating <= 5.0 AND rating * 2 = FLOOR(rating * 2));
END;
GO
