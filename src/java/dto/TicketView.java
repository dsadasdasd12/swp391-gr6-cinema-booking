package dto;

import java.util.ArrayList;
import java.util.List;

public class TicketView {

    // Booking
    private int bookingId;
    private String bookingStatus;
    private String paymentStatus;

    // Movie
    private String movieTitle;
    private String moviePoster;

    // Showtime
    private String showDate;
    private String showTime;

    // Cinema
    private String branchName;
    private String branchAddress;
    private String hallName;

    // Payment
    private String paymentMethod;
    private String paymentGateway;
    private String transactionId;

    // Price
    private double totalPrice;
    private double discountAmount;
    private double finalAmount;

    // QR
    private String qrCode;

    // Seats
    private List<TicketSeatView> seats = new ArrayList<>();

    public TicketView() {
    }

    // =========================
    // Booking
    // =========================
    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    // =========================
    // Movie
    // =========================
    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMoviePoster() {
        return moviePoster;
    }

    public void setMoviePoster(String moviePoster) {
        this.moviePoster = moviePoster;
    }

    // =========================
    // Showtime
    // =========================
    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    // =========================
    // Cinema
    // =========================
    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public void setBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    // =========================
    // Payment
    // =========================
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    // =========================
    // Price
    // =========================
    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(double finalAmount) {
        this.finalAmount = finalAmount;
    }

    // =========================
    // QR
    // =========================
    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    // =========================
    // Seats
    // =========================
    public List<TicketSeatView> getSeats() {
        return seats;
    }

    public void setSeats(List<TicketSeatView> seats) {
        this.seats = seats;
    }

    public void addSeat(TicketSeatView seat) {
        this.seats.add(seat);
    }
}