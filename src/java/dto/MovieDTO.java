/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Quản lý phim Admin —  (Long)
 */
package dto;

/**
 * DTO nhẹ phục vụ trang danh sách phim của admin (movie-list.jsp). Chỉ chứa các
 * trường cần hiển thị trong bảng; tránh đưa toàn bộ model xuống JSP.
 *
 * @author LONG
 */
public class MovieDTO {

    private int id;
    private String title;
    private String status;            // COMING_SOON | NOW_SHOWING | ENDED
    private String statusLabel;       // Nhãn tiếng Việt
    private String categoryNames;     // "Hành động, Viễn tưởng"
    private String durationLabel;     // "2h 15m"
    private String posterUrl;
    private String director;
    private String actor;             // Thêm cột diễn viên
    private boolean hasActiveShowtimes; // true → không cho xóa

    // ── Constructors ─────────────────────────────────────────
    public MovieDTO() {
    }

    public MovieDTO(int id, String title, String status, String statusLabel,
            String categoryNames, String durationLabel,
            String posterUrl, String director, String actor, boolean hasActiveShowtimes) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.statusLabel = statusLabel;
        this.categoryNames = categoryNames;
        this.durationLabel = durationLabel;
        this.posterUrl = posterUrl;
        this.director = director;
        this.actor = actor;
        this.hasActiveShowtimes = hasActiveShowtimes;
    }

    // ── Getters & Setters ────────────────────────────────────
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String s) {
        this.statusLabel = s;
    }

    public String getCategoryNames() {
        return categoryNames;
    }

    public void setCategoryNames(String s) {
        this.categoryNames = s;
    }

    public String getDurationLabel() {
        return durationLabel;
    }

    public void setDurationLabel(String s) {
        this.durationLabel = s;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String s) {
        this.posterUrl = s;
    }

    public boolean isPosterExternalUrl() {
        return posterUrl != null && (posterUrl.startsWith("http://") || posterUrl.startsWith("https://"));
    }

    /**
     * Đường dẫn poster không có dấu / đầu — dùng trong &lt;img
     * src="${ctx}/..."&gt;.
     */
    public String getPosterWebPath() {
        if (posterUrl == null || posterUrl.isBlank()) {
            return "";
        }
        String p = posterUrl.trim();
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        return p;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public boolean isHasActiveShowtimes() {
        return hasActiveShowtimes;
    }

    public void setHasActiveShowtimes(boolean b) {
        this.hasActiveShowtimes = b;
    }


    /**
     * CSS class dùng cho badge trạng thái trong Bootstrap 5. NOW_SHOWING →
     * success, COMING_SOON → warning, ENDED → secondary
     */
    public String getStatusBadgeClass() {
        if (status == null) {
            return "secondary";
        }
        switch (status) {
            case "NOW_SHOWING":
                return "success";
            case "COMING_SOON":
                return "warning";
            default:
                return "secondary";
        }
    }
}
