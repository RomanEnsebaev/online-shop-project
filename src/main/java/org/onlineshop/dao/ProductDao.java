package org.onlineshop.dao;

import org.onlineshop.model.Product;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductDao {
    public List<Product> findAll() {
        return List.of(
                new Product(1, "Телефон", "Смартфон 6,5''", 120_000),
                new Product(2, "Наушники", "Bluetooth‑гарнитура", 35_000),
                new Product(3, "Ноутбук", "14'' 16GB RAM", 450_000)
        );
    }
}
