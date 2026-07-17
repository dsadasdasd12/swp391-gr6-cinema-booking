package dto;

/**
 * DTO hiển thị combo F&B tại quầy POS.
 *
 * availableQuantity là số combo tối đa có thể bán dựa trên tồn kho
 * của toàn bộ sản phẩm thành phần.
 */
public class StaffFnbComboDTO {

    private int comboId;
    private String comboName;
    private String description;
    private String imageUrl;
    private String itemSummary;
    private double sellingPrice;
    private int availableQuantity;

    public StaffFnbComboDTO() {
    }

    public StaffFnbComboDTO(
            int comboId,
            String comboName,
            String description,
            String imageUrl,
            String itemSummary,
            double sellingPrice,
            int availableQuantity
    ) {
        this.comboId = comboId;
        this.comboName = comboName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.itemSummary = itemSummary;
        this.sellingPrice = sellingPrice;
        this.availableQuantity = availableQuantity;
    }

    public int getComboId() {
        return comboId;
    }

    public void setComboId(int comboId) {
        this.comboId = comboId;
    }

    public String getComboName() {
        return comboName == null ? "" : comboName;
    }

    public void setComboName(String comboName) {
        this.comboName = comboName;
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

    public String getItemSummary() {
        return itemSummary == null ? "" : itemSummary;
    }

    public void setItemSummary(String itemSummary) {
        this.itemSummary = itemSummary;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public boolean isAvailable() {
        return availableQuantity > 0;
    }
}
