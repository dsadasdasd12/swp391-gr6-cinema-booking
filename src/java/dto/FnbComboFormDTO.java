package dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FnbComboFormDTO {

    private Integer id;
    private String name;
    private String description;
    private BigDecimal sellingPrice;
    private String imageUrl;
    private boolean allowedToSell;

    private List<FnbComboItemDTO> items = new ArrayList<>();

    public FnbComboFormDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public List<FnbComboItemDTO> getItems() {
        return items;
    }

    public void setItems(List<FnbComboItemDTO> items) {
        this.items = items;
    }
}
