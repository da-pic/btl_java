package com.example.ql_shopcoffee.dao.interfaces;

import com.example.ql_shopcoffee.models.Category;

import java.util.List;

public interface ICategoryDAO {
    Category findById(int id);
    List<Category> findAll();
    boolean add(Category category);
    boolean update(Category category);
    boolean delete(int id);
    boolean nameExists(String name);
    int count();
}
