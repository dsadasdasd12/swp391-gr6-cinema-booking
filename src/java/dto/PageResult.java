/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - lớp bao kết quả phân trang dùng chung
 */
package dto;

import java.util.List;

/**
 * Một trang kết quả kèm các thông tin meta mà view cần để vẽ thanh phân trang.
 * Mọi tính toán phân trang đặt ở đây để JSP chỉ việc đọc getter.
 *
 * @param <T> kiểu phần tử trong trang
 * @author LONG
 */
public class PageResult<T> {

    /** Số nút trang hiển thị về mỗi phía của trang hiện tại. */
    private static final int WINDOW = 2;

    private List<T> items;
    private long totalItems;
    private int page;
    private int pageSize;

    public PageResult() {
    }

    public PageResult(List<T> items, long totalItems, int page, int pageSize) {
        this.items = items;
        this.totalItems = totalItems;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
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

    /** Tổng số trang. */
    public int getTotalPages() {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public boolean isHasPrev() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }

    /** Số thứ tự (bắt đầu từ 1) của phần tử đầu trang — cho dòng "X–Y / Z". */
    public long getFromIndex() {
        return totalItems == 0 ? 0 : (long) (page - 1) * pageSize + 1;
    }

    /** Số thứ tự của phần tử cuối trang. */
    public long getToIndex() {
        return Math.min((long) page * pageSize, totalItems);
    }

    /** Trang đầu của cửa sổ phân trang quanh trang hiện tại. */
    public int getStartPage() {
        return Math.max(1, page - WINDOW);
    }

    /** Trang cuối của cửa sổ phân trang quanh trang hiện tại. */
    public int getEndPage() {
        return Math.min(getTotalPages(), page + WINDOW);
    }
}
