/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem suất chiếu - nhóm các suất chiếu theo từng phim cho trang lịch chiếu
 */
package dto;

import java.util.ArrayList;
import java.util.List;
import model.Showtime;
import util.MoviePosterFallbacks;

/**
 * Gom các suất chiếu (trong cùng một chi nhánh, cùng một ngày) của cùng một
 * phim lại với nhau để trang lịch chiếu hiển thị theo từng phim: poster + tên
 * phim + danh sách giờ chiếu. Việc gom nhóm được thực hiện ở tầng service, JSP
 * chỉ việc lặp hai vòng for.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class MovieShowtimes {

    private int movieId;
    private String movieTitle;
    private String posterUrl;
    private List<Showtime> showtimes = new ArrayList<>();

    public MovieShowtimes() {
    }

    public MovieShowtimes(int movieId, String movieTitle, String posterUrl) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.posterUrl = posterUrl;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getPosterFallbackUrl() {
        return MoviePosterFallbacks.resolve(movieTitle);
    }

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }
}
