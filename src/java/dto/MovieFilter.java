/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - tiêu chí tìm kiếm / lọc truyền giữa các tầng
 */
package dto;

/**
 * Đối tượng truyền dữ liệu (DTO) gom toàn bộ tham số duyệt / tìm kiếm / lọc đến
 * từ màn hình danh sách phim. Controller đổ dữ liệu từ request vào đây, Service
 * kiểm tra hợp lệ, còn DAO biến nó thành câu truy vấn SQL động.
 *
 * @author LONG
 */
public class MovieFilter {

    /**
     * Số phim mặc định trên mỗi trang.
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    private String keyword;         // tìm theo tên phim / diễn viên / đạo diễn
    private Integer categoryId;     // lọc theo thể loại (dbo.CATEGORY.id)
    private Integer languageId;     // lọc theo ngôn ngữ (dbo.LANGUAGES.id)
    private String status;          // COMING_SOON | NOW_SHOWING | ENDED
    private String format;          // hall_type: STANDARD|VIP|IMAX|4DX|PREMIUM
    private String sortBy;          // newest | title_asc | title_desc | rating_desc
    private int page = 1;
    private int pageSize = DEFAULT_PAGE_SIZE;

    public MovieFilter() {
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * True khi có ít nhất một điều kiện tìm kiếm/lọc đang được dùng.
     */
    public boolean isActive() {
        return (keyword != null && !keyword.isBlank())
                || categoryId != null
                || languageId != null
                || (status != null && !status.isBlank())
                || (format != null && !format.isBlank());
    }
}
