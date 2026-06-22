/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.util.List;
import model.Branch;
import model.Hall;

public class BranchHallGroup {

    private Branch branch;
    private List<Hall> halls;

    public BranchHallGroup() {
    }

    public BranchHallGroup(Branch branch, List<Hall> halls) {
        this.branch = branch;
        this.halls = halls;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public List<Hall> getHalls() {
        return halls;
    }

    public void setHalls(List<Hall> halls) {
        this.halls = halls;
    }
}