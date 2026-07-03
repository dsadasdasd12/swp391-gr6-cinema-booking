package model;

import java.time.LocalDateTime;

public class SeatType {

    private int id;
    private String code;
    private String name;
    private double defaultPrice;
    private String color;
    private String status;
    private LocalDateTime lastUpdate;

    public SeatType() {
    }

    public SeatType(int id, String code, String name, double defaultPrice, String color, String status, LocalDateTime lastUpdate) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.defaultPrice = defaultPrice;
        this.color = color;
        this.status = status;
        this.lastUpdate = lastUpdate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(double defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
