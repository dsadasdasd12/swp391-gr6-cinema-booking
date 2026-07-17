package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DiscountCode implements Serializable {

    private int id;
    private String code;
    private String discountType;
    private double discountValue;
    private Double maxDiscountAmount;
    private double minOrderValue;
    private int maxUses;
    private int usedCount;
    private Timestamp startDate;
    private Timestamp endDate;
    private String status;

    public DiscountCode() {
    }

    public DiscountCode(int id, String code, String discountType, double discountValue,
            Double maxDiscountAmount, double minOrderValue, int maxUses,
            int usedCount, Timestamp startDate, Timestamp endDate, String status) {
        this.id = id;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderValue = minOrderValue;
        this.maxUses = maxUses;
        this.usedCount = usedCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
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

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public Double getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(Double maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public double getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper formatting methods
    public String getFormattedStartDate() {
        if (this.startDate == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(this.startDate);
    }

    public String getFormattedEndDate() {
        if (this.endDate == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(this.endDate);
    }
}
