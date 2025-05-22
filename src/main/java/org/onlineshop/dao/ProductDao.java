package org.onlineshop.dao;

import org.onlineshop.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class ProductDao {
    private final JdbcTemplate jdbc;
    public ProductDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Product> mapper = (rs, n) -> {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setActive(rs.getBoolean("active"));
        return p;
    };

    public List<Product> findAllActive() {
        return jdbc.query("SELECT * FROM products WHERE active = TRUE ORDER BY product_id", mapper);
    }

    public void save(Product p) {
        jdbc.update("""
            INSERT INTO products(name, description, price, stock_qty, active)
            VALUES (?,?,?,?,?)
            """,
                p.getName(), p.getDescription(),
                p.getPrice(), p.getStockQty(), p.isActive());
    }
}
