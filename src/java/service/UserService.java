/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.UserDAO;
import java.util.List;
import model.User;
import util.PasswordUtil;

/**
 *
 * @author Admin
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public List<User> getAllUsersExceptAdmin() {
        return userDAO.getAllUsersExceptAdmin();
    }

    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public boolean createUserByAdmin(User user) {

        if (user == null) {
            return false;
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return false;
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return false;
        }

        if (userDAO.emailExists(user.getEmail())) {
            return false;
        }
        String defaultPassword = "12345678";

        user.setPasswordHash(
                PasswordUtil.hashPassword(defaultPassword)
        );

        user.setFullName(user.getFullName().trim());
        user.setEmail(user.getEmail().trim());
        user.setPhone(user.getPhone() == null ? "" : user.getPhone().trim());

        return userDAO.createUserByAdmin(user);
    }

    public boolean updateUserByAdmin(User user) {

        if (user == null || user.getId() <= 0) {
            return false;
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return false;
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return false;
        }

        user.setFullName(user.getFullName().trim());
        user.setEmail(user.getEmail().trim());
        user.setPhone(user.getPhone() == null ? "" : user.getPhone().trim());

        return userDAO.updateUserByAdmin(user);
    }

    public boolean toggleActive(int id) {

        User user = userDAO.getUserById(id);

        if (user == null) {
            return false;
        }

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return false;
        }

        return userDAO.toggleActive(id);
    }

    public boolean updateProfile(User user) {
        return userDAO.updateCustomerProfile(
                user.getId(),
                user.getFullName(),
                user.getPhone()
        );
    }

    public int getBranchIdOfStaff(int userId) {
        return userDAO.getBranchIdOfStaff(userId);
    }
}
