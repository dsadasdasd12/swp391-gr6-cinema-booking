package dto;

import java.math.BigDecimal;

public class FnbProductFormDTO {

    private Integer id;
    private int categoryId;
    private String name;
    private String description;
    private String productType;
    private BigDecimal sellingPrice;
    private String imageUrl;
    private boolean allowedToSell;

    public FnbProductFormDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
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
}