package service;

import dao.DiscountDAO;
import java.util.List;
import model.DiscountCode;

public class DiscountService {
    private final DiscountDAO discountDAO = new DiscountDAO();

    public List<DiscountCode> getAllDiscountCodes() {
        return discountDAO.getAllDiscountCodes();
    }

    public boolean createDiscountCode(DiscountCode code) {
        return discountDAO.createDiscountCode(code);
    }

    public boolean updateDiscountCodeStatus(int id, String status) {
        return discountDAO.updateDiscountCodeStatus(id, status);
    }

    public boolean deleteDiscountCode(int id) {
        return discountDAO.deleteDiscountCode(id);
    }

    public boolean incrementUsedCount(String code) {
        return discountDAO.incrementUsedCount(code);
    }
}
