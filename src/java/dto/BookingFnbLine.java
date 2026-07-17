package dto;

import java.io.Serializable;

public class BookingFnbLine implements Serializable {

    private String itemType;
    private int itemId;
    private String name;
    private String imageUrl;
    private String description;
    private int quantity;
    private int availableQuantity;
    private double unitPrice;

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getLineTotal() {
        return unitPrice * quantity;
    }

    public String getUnitPriceLabel() {
        return money(unitPrice);
    }

    public String getLineTotalLabel() {
        return money(getLineTotal());
    }

    private String money(double value) {
        return String.format("%,.0f", value).replace(',', '.') + " đ";
    }
}
