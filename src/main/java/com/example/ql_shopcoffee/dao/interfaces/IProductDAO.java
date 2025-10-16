package com.example.ql_shopcoffee.dao.interfaces;

import com.example.ql_shopcoffee.models.Product;

import java.util.List;

public interface IProductDAO {
    Product findById(int id);
    List<Product> findAll();
    List<Product> findAllIncludingInactive();
    List<Product> findByCategory(int categoryId);
    List<Product> findByName(String name);

    boolean add(Product product);
    boolean update(Product product);
    boolean delete(int id);
    boolean hardDelete(int id);

    int count();

    boolean exists(int id);
}
