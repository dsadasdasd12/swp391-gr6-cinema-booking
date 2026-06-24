package model;

/**
 * Thông tin chuỗi rạp (bảng dbo.CINEMA).
 */
public class Cinema {

    private int id;
    private String name;
    private String address;
    private String phone;
    private String logoUrl;
    private String status;

    public Cinema() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
