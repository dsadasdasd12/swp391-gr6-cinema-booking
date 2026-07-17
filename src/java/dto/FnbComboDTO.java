package dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FnbComboDTO {

    private int id;
    private String name;
    private String description;
    private BigDecimal sellingPrice;
    private BigDecimal originalPrice;
    private String imageUrl;
    private boolean allowedToSell;
    private String status;

    private List<FnbComboItemDTO> items = new ArrayList<>();

    public FnbComboDTO() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAllowedToSell() {
        return allowedToSell;
    }

    public void setAllowedToSell(boolean allowedToSell) {
        this.allowedToSell = allowedToSell;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<FnbComboItemDTO> getItems() {
        return items;
    }

    public void setItems(List<FnbComboItemDTO> items) {
        this.items = items;
    }
}