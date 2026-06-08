package model;

import java.io.Serializable;

public class Hall implements Serializable {
    private int id;
    private int branchId;
    private String name;
    private int totalSeats;

    public Hall() {}

    public Hall(int id, int branchId, String name, int totalSeats) {
        this.id = id;
        this.branchId = branchId;
        this.name = name;
        this.totalSeats = totalSeats;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
}
