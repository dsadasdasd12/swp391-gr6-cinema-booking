package dto;

/**
 * DTO hiển thị sản phẩm F&B bán lẻ tại quầy POS.
 *
 * DTO này chỉ dùng để hiển thị danh sách sản phẩm có thể bán.
 * Khi tạo booking, dữ liệu vẫn phải được chuyển thành BookingFnbLine
 * và kiểm tra lại từ cơ sở dữ liệu.
 */
public class StaffFnbProductDTO {

    private int productId;
    private String productName;
    private String categoryName;
    private String description;
    private String imageUrl;
    private double sellingPrice;
    private int stockQuantity;

    public StaffFnbProductDTO() {
    }

    public StaffFnbProductDTO(
            int productId,
            String productName,
            String categoryName,
            String description,
            String imageUrl,
            double sellingPrice,
            int stockQuantity
    ) {
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName == null ? "" : productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName == null ? "" : categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl == null ? "" : imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isAvailable() {
        return stockQuantity > 0;
    }
}
