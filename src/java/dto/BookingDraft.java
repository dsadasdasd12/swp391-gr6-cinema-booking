package dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookingDraft implements Serializable {

    private int showtimeId;
    /*
     * ID cua CART dang giu ghe tam thoi trong DB. Khac voi session draft,
     * cart nay duoc cac luong customer va staff cung kiem tra, nen ghe da
     * chon khong the bi ban trung trong luc customer dang chon F&B.
     */
    private int cartId;
    private List<Integer> seatIds = new ArrayList<>();
    private LocalDateTime createdAt = LocalDateTime.now();
    private Map<String, Integer> fnbQuantities = new LinkedHashMap<>();

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public List<Integer> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Integer> seatIds) {
        this.seatIds = seatIds == null ? new ArrayList<>() : new ArrayList<>(seatIds);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Integer> getFnbQuantities() {
        return fnbQuantities;
    }

    public void setFnbQuantities(Map<String, Integer> values) {
        fnbQuantities = values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values);
    }
}
