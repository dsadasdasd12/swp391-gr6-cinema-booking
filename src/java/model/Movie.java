/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim (Browse / Search / Filter / Xem chi tiết)
 */
package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ một dòng của bảng dbo.MOVIES, kèm vài trường hiển thị suy diễn
 * (thể loại, ngôn ngữ, điểm đánh giá trung bình) phục vụ các màn hình duyệt phim.
 *
 * Lưu ý: mọi xử lý định dạng để hiển thị đều đặt ở đây (tầng model) để JSP chỉ
 * cần gọi getter, không phải nhúng code Java.
 *
 * @author LONG
 */
public class Movie {

    // ── Các cột của bảng dbo.MOVIES ─────────────────────────
    private int id;
    private String title;
    private int durationMin;
    private String description;
    private LocalDate releaseDate;
    private LocalDate endDate;
    private String status;          // COMING_SOON | NOW_SHOWING | ENDED
    private String posterUrl;
    private String trailerUrl;
    private String actor;
    private String director;
    private LocalDateTime lastUpdate;

    // ── Trường suy diễn / lấy từ bảng liên kết ──────────────
    private List<Category> categories = new ArrayList<>();
    private List<Language> languages = new ArrayList<>();
    private double avgRating;       // AVG(rating) từ dbo.REVIEWS, = 0 nếu chưa có
    private int reviewCount;

    public Movie() {
    }

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

    public int getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(int durationMin) {
        this.durationMin = durationMin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public int getDurationHours() {
    return durationMin / 60;
}

public int getDurationRemainingMinutes() {
    return durationMin % 60;
}
    // ── Các getter hỗ trợ hiển thị (để JSP gọi trực tiếp qua EL) ──

    /** Nhãn trạng thái tiếng Việt, ví dụ "Đang chiếu". */
    public String getStatusLabel() {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "COMING_SOON":
                return "Sắp chiếu";
            case "NOW_SHOWING":
                return "Đang chiếu";
            case "ENDED":
                return "Đã chiếu";
            default:
                return status;
        }
    }

    /** Lớp CSS cho badge trạng thái (now / soon / ended). */
    public String getStatusBadgeClass() {
        if ("NOW_SHOWING".equals(status)) {
            return "now";
        }
        if ("COMING_SOON".equals(status)) {
            return "soon";
        }
        return "ended";
    }

    /** Danh sách tên thể loại nối bằng dấu phẩy, ví dụ "Hành động, Viễn tưởng". */
    public String getCategoryNames() {
        StringBuilder sb = new StringBuilder();
        for (Category c : categories) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(c.getName());
        }
        return sb.toString();
    }

    /** Danh sách ngôn ngữ (kèm Phụ đề/Lồng tiếng) nối bằng dấu phẩy. */
    public String getLanguageNames() {
        StringBuilder sb = new StringBuilder();
        for (Language l : languages) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(l.getDisplayName());
        }
        return sb.toString();
    }

    /** Thời lượng dạng "2h 15m". */
    public String getDurationLabel() {
        int h = durationMin / 60;
        int m = durationMin % 60;
        if (h > 0) {
            return h + "h " + m + "m";
        }
        return m + "m";
    }

    /** Điểm đánh giá làm tròn 1 chữ số thập phân để hiển thị. */
    public double getRatingRounded() {
        return Math.round(avgRating * 10.0) / 10.0;
    }

    /** Số sao đặc (0..5) để vẽ dải sao trên trang chi tiết. */
    public int getRoundedStars() {
        return (int) Math.round(getRatingRounded());
    }

    /** Năm khởi chiếu, hoặc 0 nếu chưa có. */
    public int getReleaseYear() {
        return releaseDate == null ? 0 : releaseDate.getYear();
    }

    /** Ngày khởi chiếu dạng dd/MM/yyyy, hoặc rỗng nếu chưa có. */
    public String getReleaseDateLabel() {
        return releaseDate == null ? ""
                : releaseDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /** Giá trị cho input type="date" (yyyy-MM-dd). */
    public String getReleaseDateForInput() {
        return releaseDate == null ? ""
                : releaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /** Ngày kết thúc chiếu dạng dd/MM/yyyy, hoặc rỗng nếu chưa có. */
    public String getEndDateLabel() {
        return endDate == null ? ""
                : endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /** Giá trị cho input type="date" (yyyy-MM-dd). */
    public String getEndDateForInput() {
        return endDate == null ? ""
                : endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /** Chuẩn hóa đường dẫn lưu DB / hiển thị (bỏ dấu / đầu). */
    public static String normalizePosterPath(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String p = url.trim();
        if (isAbsoluteUrl(p)) {
            return p;
        }
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        return p.isEmpty() ? null : p;
    }

    /** Đường dẫn poster tương đối (không có dấu / đầu) để ghép với contextPath. */
    public String getPosterWebPath() {
        String p = normalizePosterPath(posterUrl);
        return p == null ? "" : p;
    }

    public boolean isPosterExternalUrl() {
        return isAbsoluteUrl(posterUrl);
    }

    public static boolean isAbsoluteUrl(String url) {
        if (url == null) {
            return false;
        }
        String lower = url.trim().toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    /**
     * Giá trị form select ngôn ngữ (Vietnamese, English, …) từ ngôn ngữ đầu tiên của phim.
     */
    public String getPrimaryLanguageFormValue() {
        if (languages == null || languages.isEmpty()) {
            return "";
        }
        Language l = languages.get(0);
        String name = l.getName() != null ? l.getName().toLowerCase() : "";
        String code = l.getCode() != null ? l.getCode().toLowerCase() : "";
        if (name.contains("việt") || "vi".equals(code)) {
            return "Vietnamese";
        }
        if (name.contains("anh") || "en".equals(code)) {
            return "English";
        }
        if (name.contains("hàn") || "ko".equals(code)) {
            return "Korean";
        }
        if (name.contains("nhật") || "ja".equals(code)) {
            return "Japanese";
        }
        return "Mixed";
    }

    /**
     * Chuyển link trailer YouTube (watch?v= / youtu.be / embed) thành URL nhúng
     * iframe. Trả về null nếu không phải link YouTube hợp lệ.
     */
    public String getEmbedUrl() {
        if (trailerUrl == null || trailerUrl.isBlank()) {
            return null;
        }
        String u = trailerUrl.trim();
        if (u.contains("watch?v=")) {
            String vid = u.substring(u.indexOf("watch?v=") + 8);
            int amp = vid.indexOf('&');
            if (amp > -1) {
                vid = vid.substring(0, amp);
            }
            return "https://www.youtube.com/embed/" + vid;
        }
        if (u.contains("youtu.be/")) {
            String vid = u.substring(u.indexOf("youtu.be/") + 9);
            int q = vid.indexOf('?');
            if (q > -1) {
                vid = vid.substring(0, q);
            }
            return "https://www.youtube.com/embed/" + vid;
        }
        if (u.contains("/embed/")) {
            return u;
        }
        return null;
    }
}
