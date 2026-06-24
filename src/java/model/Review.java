/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Đánh giá phim (Rate / Write / Edit / Delete reviews)
 */
package model;

import java.time.LocalDateTime;

/**
 * Một đánh giá phim - ánh xạ đúng các cột của bảng dbo.REVIEWS. Mỗi đánh giá
 * gắn với một đơn đặt vé (booking_id là UNIQUE) nên khách chỉ đánh giá được
 * phim mình đã đặt/đã xem. Dữ liệu hiển thị thêm (tên người đánh giá) để ở DTO.
 *
 * @author Group6 - Huy (Module Đánh giá)
 */
public class Review {

    private int id;
    private int userId;
    private int movieId;
    private int bookingId;
    private int rating;             // 1..5 (dbo.REVIEWS.rating)
    private String comment;
    private String status;          // ACTIVE | HIDDEN
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdate;

    public Review() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
