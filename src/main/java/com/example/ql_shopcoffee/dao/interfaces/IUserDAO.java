package com.example.ql_shopcoffee.dao.interfaces;

import com.example.ql_shopcoffee.models.User;

import java.util.List;

public interface IUserDAO {
    User findByUsername(String username);
    User findById(int id);
    List<User> findAll();
    List<User> findByRole(String role);
    boolean add(User user);
    boolean update(User user);
    boolean delete(int id);
    boolean changePassword(int userId, String newPassword);
    boolean usernameExists(String username);
}
