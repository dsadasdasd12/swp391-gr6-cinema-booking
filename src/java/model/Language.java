/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - ngôn ngữ phim dùng để lọc (bảng dbo.LANGUAGES)
 */
package model;

/**
 * Ngôn ngữ phim được hỗ trợ. Khi nạp theo ngữ cảnh của một phim cụ thể
 * (qua bảng dbo.MOVIE_LANGUAGES) thì cờ {@code subtitle} cũng được gán.
 *
 * @author LONG
 */
public class Language {

    private int id;
    private String name;
    private String code;            // ví dụ VI, EN, KO
    private String status;          // ACTIVE | INACTIVE
    private boolean subtitle;       // theo phim: true = phụ đề, false = lồng tiếng

    public Language() {
    }

    public Language(int id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSubtitle() {
        return subtitle;
    }

    public void setSubtitle(boolean subtitle) {
        this.subtitle = subtitle;
    }

    /** "Tiếng Việt (Phụ đề)" / "Tiếng Anh (Lồng tiếng)" để hiển thị. */
    public String getDisplayName() {
        return name + (subtitle ? " (Phụ đề)" : " (Lồng tiếng)");
    }
}
