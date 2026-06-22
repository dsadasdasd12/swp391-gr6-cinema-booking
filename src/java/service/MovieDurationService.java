/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package service;

import dao.MovieDurationDAO;
import java.util.List;
import model.Movie;

/**
 * Xử lý nghiệp vụ cho chức năng quản lý thời lượng phim.
 */
public class MovieDurationService {

    /*
     * Giới hạn thời lượng hợp lệ của một bộ phim.
     */
    private static final int MIN_DURATION_MIN = 1;
    private static final int MAX_DURATION_MIN = 600;

    private final MovieDurationDAO movieDurationDAO;

    public MovieDurationService() {
        this.movieDurationDAO = new MovieDurationDAO();
    }

    /**
     * Lấy toàn bộ phim để hiển thị trên màn hình
     * quản lý thời lượng.
     */
    public List<Movie> getAllMovies() {
        return movieDurationDAO.findAllForDurationManagement();
    }

    /**
     * Tìm phim theo ID.
     */
    public Movie getMovieById(int movieId) {

        if (movieId <= 0) {
            throw new IllegalArgumentException(
                    "Phim không hợp lệ."
            );
        }

        Movie movie = movieDurationDAO.findById(movieId);

        if (movie == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phim cần cập nhật."
            );
        }

        return movie;
    }

    /**
     * Cập nhật thời lượng phim.
     */
    public boolean updateDuration(
            int movieId,
            int durationMin) {

        /*
         * Kiểm tra ID phim.
         */
        if (movieId <= 0) {
            throw new IllegalArgumentException(
                    "Phim không hợp lệ."
            );
        }

        /*
         * Kiểm tra thời lượng tối thiểu.
         */
        if (durationMin < MIN_DURATION_MIN) {
            throw new IllegalArgumentException(
                    "Thời lượng phim phải lớn hơn 0 phút."
            );
        }

        /*
         * Kiểm tra giới hạn tối đa.
         */
        if (durationMin > MAX_DURATION_MIN) {
            throw new IllegalArgumentException(
                    "Thời lượng phim không được vượt quá "
                    + MAX_DURATION_MIN
                    + " phút."
            );
        }

        /*
         * Kiểm tra phim có tồn tại trong database không.
         */
        Movie currentMovie
                = movieDurationDAO.findById(movieId);

        if (currentMovie == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phim cần cập nhật."
            );
        }

        /*
         * Nếu thời lượng không thay đổi thì coi như thành công,
         * không cần chạy UPDATE.
         */
        if (currentMovie.getDurationMin() == durationMin) {
            return true;
        }

        boolean updated
                = movieDurationDAO.updateDuration(
                        movieId,
                        durationMin
                );

        if (!updated) {
            throw new IllegalArgumentException(
                    "Không thể cập nhật thời lượng phim."
            );
        }

        return true;
    }

    /**
     * Chuyển chuỗi thời lượng người dùng nhập
     * thành số nguyên.
     */
    public int parseDuration(String durationValue) {

        if (durationValue == null
                || durationValue.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập thời lượng phim."
            );
        }

        try {
            return Integer.parseInt(
                    durationValue.trim()
            );

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Thời lượng phim phải là số nguyên."
            );
        }
    }
}